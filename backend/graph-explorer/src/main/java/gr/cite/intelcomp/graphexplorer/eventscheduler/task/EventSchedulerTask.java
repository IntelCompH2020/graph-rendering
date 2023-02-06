package gr.cite.intelcomp.graphexplorer.eventscheduler.task;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.enums.ScheduledEventStatus;
import gr.cite.intelcomp.graphexplorer.common.scope.fake.FakeRequestScope;
import gr.cite.intelcomp.graphexplorer.data.ScheduledEventEntity;
import gr.cite.intelcomp.graphexplorer.eventscheduler.EventSchedulerProperties;
import gr.cite.intelcomp.graphexplorer.eventscheduler.processing.EventProcessingStatus;
import gr.cite.intelcomp.graphexplorer.eventscheduler.processing.ScheduledEventHandler;
import gr.cite.intelcomp.graphexplorer.query.ScheduledEventQuery;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class EventSchedulerTask {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EventSchedulerTask.class));

	private final ApplicationContext applicationContext;
	private final EventSchedulerProperties properties;

	public EventSchedulerTask(ApplicationContext applicationContext, EventSchedulerProperties properties) {
		this.applicationContext = applicationContext;
		this.properties = properties;
		long intervalSeconds = properties.getTask().getProcessor().getIntervalSeconds();
		if (properties.getTask().getProcessor().getEnable() && intervalSeconds > 0) {
			logger.info("Task '{}' will be scheduled to run every {} seconds", properties.getTask().getName(), intervalSeconds);

			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
			//GK: Fixed rate is heavily unpredictable, and it will not scale well on a very heavy workload
			scheduler.scheduleWithFixedDelay(this::process, 10, intervalSeconds, TimeUnit.SECONDS);
		}
	}

	public void process() {
		try {
			Instant lastCandidateCreationTimestamp = null;
			while (true) {
				CandidateInfo candidateInfo = this.candidateEventToRun(lastCandidateCreationTimestamp);
				if (candidateInfo == null) break;

				lastCandidateCreationTimestamp = candidateInfo.getCreatedAt();
				Boolean shouldOmit = this.shouldOmit(candidateInfo);
				if (shouldOmit) {
					continue;
				}
				Boolean shouldAwait = this.shouldWait(candidateInfo);
				if (shouldAwait) {
					continue;
				}
				this.handle(candidateInfo.getId());
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	private CandidateInfo candidateEventToRun(Instant lastCandidateNotificationCreationTimestamp) {
		EntityManager entityManager = null;
		EntityTransaction transaction = null;
		CandidateInfo candidateInfo = null;
		try (FakeRequestScope ignored = new FakeRequestScope()) {
			EntityManagerFactory entityManagerFactory = this.applicationContext.getBean(EntityManagerFactory.class);

			entityManager = entityManagerFactory.createEntityManager();
			transaction = entityManager.getTransaction();
			transaction.begin();
			ScheduledEventQuery scheduledEventQuery = applicationContext.getBean(ScheduledEventQuery.class);

			//Get currently running tasks
			logger.debug("Checking running tasks...");
			Long currentlyRunningTasksCount;
			scheduledEventQuery = scheduledEventQuery
					.isActives(IsActive.ACTIVE)
					.status(ScheduledEventStatus.PROCESSING)
					.retryThreshold(Math.toIntExact(this.properties.getTask().getProcessor().getOptions().getRetryThreshold()))
					.shouldRunBefore(Instant.now())
					.createdAfter(lastCandidateNotificationCreationTimestamp)
					.ordering(new Ordering().addAscending(ScheduledEventEntity._createdAt));
			currentlyRunningTasksCount = scheduledEventQuery.count();

			logger.debug("Currently running tasks count -> {}", currentlyRunningTasksCount);
			if (currentlyRunningTasksCount >= properties.getTask().getProcessor().getOptions().getParallelTasksThreshold()) {
				logger.debug("Running tasks cannot be more than {}, returning no candidate", currentlyRunningTasksCount);
				return null;
			}

			ScheduledEventEntity candidate;
			scheduledEventQuery = scheduledEventQuery
					.isActives(IsActive.ACTIVE)
					.status(ScheduledEventStatus.PENDING, ScheduledEventStatus.ERROR)
					.retryThreshold(Math.toIntExact(this.properties.getTask().getProcessor().getOptions().getRetryThreshold()))
					.shouldRunBefore(Instant.now())
					.createdAfter(lastCandidateNotificationCreationTimestamp)
					.ordering(new Ordering().addAscending(ScheduledEventEntity._createdAt));
			candidate = scheduledEventQuery.first();
			if (candidate != null) {
				ScheduledEventStatus previousState = candidate.getStatus();
				candidate.setStatus(ScheduledEventStatus.PROCESSING);
				candidate = entityManager.merge(candidate);
				entityManager.persist(candidate);
				entityManager.flush();

				candidateInfo = new CandidateInfo(candidate.getId(), previousState, candidate.getCreatedAt());
			}
			transaction.commit();

		} catch (OptimisticLockException e) {
			logger.error("Optimistic Lock Error occurred on Notification persist");
			if (transaction != null) transaction.rollback();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			if (transaction != null) transaction.rollback();
		} finally {
			if (entityManager != null) entityManager.close();
		}
		return candidateInfo;
	}

	private Boolean shouldWait(CandidateInfo candidateInfo) {
		EntityManager entityManager = null;
		EntityTransaction transaction = null;
		boolean shouldWait = false;
		try (FakeRequestScope ignored = new FakeRequestScope()) {
			EntityManagerFactory entityManagerFactory = this.applicationContext.getBean(EntityManagerFactory.class);

			entityManager = entityManagerFactory.createEntityManager();
			transaction = entityManager.getTransaction();
			transaction.begin();
			ScheduledEventEntity scheduledEventEntity = entityManager.find(ScheduledEventEntity.class, candidateInfo.getId());
			if (scheduledEventEntity.getRetryCount() != null && scheduledEventEntity.getRetryCount() >= 1) {
				int accumulatedRetry = 0;
				int pastAccumulateRetry = 0;
				EventSchedulerProperties.Task.Processor.Options options = properties.getTask().getProcessor().getOptions();
				for (int i = 1; i <= scheduledEventEntity.getRetryCount() + 1; i += 1)
					accumulatedRetry += (i * options.getRetryThreshold());
				for (int i = 1; i <= scheduledEventEntity.getRetryCount(); i += 1)
					pastAccumulateRetry += (i * options.getRetryThreshold());
				int randAccumulatedRetry = ThreadLocalRandom.current().nextInt((int) (accumulatedRetry / 2), accumulatedRetry + 1);
				long additionalTime = randAccumulatedRetry > options.getMaxRetryDelaySeconds() ? options.getMaxRetryDelaySeconds() : randAccumulatedRetry;
				long retry = pastAccumulateRetry + additionalTime;

				Instant retryOn = scheduledEventEntity.getCreatedAt().plusSeconds(retry);
				boolean itIsTime = retryOn.isBefore(Instant.now());

				if (!itIsTime) {
					scheduledEventEntity.setStatus(candidateInfo.getPreviousState());
					//notification.setUpdatedAt(Instant.now());
					scheduledEventEntity = entityManager.merge(scheduledEventEntity);
					entityManager.persist(scheduledEventEntity);

				}
				shouldWait = !itIsTime;
			}
			transaction.commit();
		} catch (OptimisticLockException e) {
			logger.error("Optimistic Lock Error occurred on Notification persist");
			if (transaction != null) transaction.rollback();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			if (transaction != null) transaction.rollback();
		} finally {
			if (entityManager != null) entityManager.close();
		}
		return shouldWait;
	}

	private Boolean shouldOmit(CandidateInfo candidateInfo) {
		EntityManager entityManager = null;
		EntityTransaction transaction = null;
		boolean shouldOmit = false;
		try (FakeRequestScope ignored = new FakeRequestScope()) {
			EntityManagerFactory entityManagerFactory = this.applicationContext.getBean(EntityManagerFactory.class);

			entityManager = entityManagerFactory.createEntityManager();
			transaction = entityManager.getTransaction();
			transaction.begin();

			ScheduledEventEntity scheduledEventEntity = entityManager.find(ScheduledEventEntity.class, candidateInfo.getId());
			long age = Instant.now().getEpochSecond() - scheduledEventEntity.getCreatedAt().getEpochSecond();
			long omitSeconds = properties.getTask().getProcessor().getOptions().getTooOldToHandleSeconds();
			if (age >= omitSeconds) {
				scheduledEventEntity.setStatus(ScheduledEventStatus.OMITTED);
				scheduledEventEntity = entityManager.merge(scheduledEventEntity);
				entityManager.persist(scheduledEventEntity);
				shouldOmit = true;
			}
			transaction.commit();
		} catch (OptimisticLockException e) {
			logger.error("Optimistic Lock Error occurred on Notification persist");
			if (transaction != null) transaction.rollback();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			if (transaction != null) transaction.rollback();
		} finally {
			if (entityManager != null) entityManager.close();
		}
		return shouldOmit;
	}

	private void handle(UUID eventId) {
		EntityManager entityManager = null;
		EntityTransaction transaction = null;
		try (FakeRequestScope ignored = new FakeRequestScope()) {
			EntityManagerFactory entityManagerFactory = this.applicationContext.getBean(EntityManagerFactory.class);

			entityManager = entityManagerFactory.createEntityManager();
			transaction = entityManager.getTransaction();
			transaction.begin();

			ScheduledEventQuery scheduledEventQuery = applicationContext.getBean(ScheduledEventQuery.class);
			ScheduledEventEntity scheduledEvent = scheduledEventQuery.ids(eventId).first();
			if (scheduledEvent == null) throw new IllegalArgumentException("scheduledEvent is null");

			EventProcessingStatus status = this.process(scheduledEvent);
			switch (status) {
				case Success: {
					scheduledEvent.setStatus(ScheduledEventStatus.SUCCESSFUL);
					break;
				}
				case Postponed: {
					scheduledEvent.setStatus(ScheduledEventStatus.PARKED);
					break;
				}
				case Error: {
					scheduledEvent.setStatus(ScheduledEventStatus.ERROR);
					scheduledEvent.setRetryCount(scheduledEvent.getRetryCount() + 1);
					break;
				}
				case Discard:
				default: {
					scheduledEvent.setStatus(ScheduledEventStatus.DISCARD);
					break;
				}
			}

			ScheduledEventEntity entity = entityManager.merge(scheduledEvent);
			entityManager.persist(entity);

			entityManager.flush();

			transaction.commit();
		} catch (OptimisticLockException e) {
			logger.error("Optimistic Lock Error occurred on Notification persist");
			if (transaction != null) transaction.rollback();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			if (transaction != null) transaction.rollback();
		} finally {
			if (entityManager != null) entityManager.close();
		}
	}

	protected EventProcessingStatus process(ScheduledEventEntity scheduledEventMessage) {
		try {
			ScheduledEventHandler handler;
			switch (scheduledEventMessage.getEventType()) {
//				case CHECK_RUNNING_TASKS:
//					handler = applicationContext.getBean(CheckTasksScheduledEventHandler.class);
//					break;
				default:
					return EventProcessingStatus.Discard;
			}

//			return handler.handle(scheduledEventMessage);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return EventProcessingStatus.Error;
		}
	}
}
