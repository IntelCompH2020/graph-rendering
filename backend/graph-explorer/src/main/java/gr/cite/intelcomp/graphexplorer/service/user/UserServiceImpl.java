package gr.cite.intelcomp.graphexplorer.service.user;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.*;
import gr.cite.intelcomp.graphexplorer.errorcode.ErrorThesaurusProperties;
import gr.cite.intelcomp.graphexplorer.event.EventBroker;
import gr.cite.intelcomp.graphexplorer.event.UserTouchedEvent;
import gr.cite.intelcomp.graphexplorer.locale.LocaleService;
import gr.cite.intelcomp.graphexplorer.model.Edge;
import gr.cite.intelcomp.graphexplorer.model.Node;
import gr.cite.intelcomp.graphexplorer.model.NodeAccess;
import gr.cite.intelcomp.graphexplorer.model.User;
import gr.cite.intelcomp.graphexplorer.model.builder.UserBuilder;
import gr.cite.intelcomp.graphexplorer.model.deleter.EdgeAccessDeleter;
import gr.cite.intelcomp.graphexplorer.model.deleter.NodeAccessDeleter;
import gr.cite.intelcomp.graphexplorer.model.deleter.UserDeleter;
import gr.cite.intelcomp.graphexplorer.model.persist.UserAccessPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.UserPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.UserTouchedIntegrationEventPersist;
import gr.cite.intelcomp.graphexplorer.query.EdgeAccessQuery;
import gr.cite.intelcomp.graphexplorer.query.EdgeQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeAccessQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeQuery;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequestScope
public class UserServiceImpl implements UserService {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserServiceImpl.class));
	private final EntityManager entityManager;
	private final AuthorizationService authorizationService;
	private final DeleterFactory deleterFactory;
	private final BuilderFactory builderFactory;
	private final ConventionService conventionService;
	private final ErrorThesaurusProperties errors;
	private final MessageSource messageSource;
	private final EventBroker eventBroker;
	private final LocaleService localeService;
	private final QueryFactory queryFactory;

	@Autowired
	public UserServiceImpl(
			EntityManager entityManager,
			AuthorizationService authorizationService,
			DeleterFactory deleterFactory,
			BuilderFactory builderFactory,
			ConventionService conventionService,
			ErrorThesaurusProperties errors,
			MessageSource messageSource,
			EventBroker eventBroker,
			LocaleService localeService,
			QueryFactory queryFactory) {
		this.entityManager = entityManager;
		this.authorizationService = authorizationService;
		this.deleterFactory = deleterFactory;
		this.builderFactory = builderFactory;
		this.conventionService = conventionService;
		this.errors = errors;
		this.messageSource = messageSource;
		this.eventBroker = eventBroker;
		this.localeService = localeService;
		this.queryFactory = queryFactory;
	}

	@Override
	public User persist(UserPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting User").And("model", model).And("fields", fields));

		this.authorizationService.authorizeForce(Permission.EditUser);

		Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

		UserEntity data = null;
		if (isUpdate) {
			data = this.entityManager.find(UserEntity.class, model.getId());
			if (data == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
		} else {
			data = new UserEntity();
			data.setId(UUID.randomUUID());
			data.setIsActive(IsActive.ACTIVE);
			data.setCreatedAt(Instant.now());
		}
		String previousSubjectId = data.getSubjectId();

		data.setFirstName(model.getFirstName());
		data.setLastName(model.getLastName());
		data.setTimezone(model.getTimezone());
		data.setCulture(model.getCulture());
		data.setLanguage(model.getLanguage());
		data.setSubjectId(model.getSubjectId());
		data.setUpdatedAt(Instant.now());

		if (isUpdate) this.entityManager.merge(data);
		else this.entityManager.persist(data);

		this.entityManager.flush();

		this.eventBroker.emit(new UserTouchedEvent(data.getId(), data.getSubjectId(), previousSubjectId));

		User persisted = this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.OwnerOrPermission).build(BaseFieldSet.build(fields, User._id, User._hash), data);
		return persisted;
	}

	@Override
	public User persist(UserAccessPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting User").And("model", model).And("fields", fields));

		this.authorizationService.authorizeForce(Permission.EditUser);

		Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

		UserEntity data = null;
		if (isUpdate) {
			data = this.entityManager.find(UserEntity.class, model.getId());
			if (data == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
		} else {
			throw new MyApplicationException("Create Not supported");
		}
		this.persistEdgeAccess(data.getId(), model.getEdgeIds());
		this.persistNodeAccess(data.getId(), model.getNodeIds());
		data.setUpdatedAt(Instant.now());

		this.entityManager.merge(data);

		this.entityManager.flush();

		this.eventBroker.emit(new UserTouchedEvent(data.getId(), data.getSubjectId(), data.getSubjectId()));

		User persisted = this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.OwnerOrPermission).build(BaseFieldSet.build(fields, User._id, User._hash), data);
		return persisted;
	}

	private void persistNodeAccess(UUID userId, List<UUID> nodeIds) throws MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		if (nodeIds == null) nodeIds = new ArrayList<>();

		List<NodeEntity> nodeEntities = this.queryFactory.query(NodeQuery.class).ids(nodeIds).isActive(IsActive.ACTIVE)
				.collectAs(new BaseFieldSet().ensure(Node._id).ensure(Node._hash));
		Map<UUID, NodeEntity> nodeItemsLookup = nodeEntities.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));

		List<NodeAccessEntity> existingNodeAccesses = this.queryFactory.query(NodeAccessQuery.class)
				.nodeIds(nodeIds).userIds(userId).collect();
		Map<UUID, NodeAccessEntity> existingNodeAccessesLookup = existingNodeAccesses.stream().collect(Collectors.toMap(x -> x.getNodeId(), x -> x));

		final List<UUID> nodeIdsFinal = nodeIds;
		List<NodeAccessEntity> toDelete = existingNodeAccesses.stream().filter(x -> !nodeIdsFinal.contains(x.getId())).collect(Collectors.toList());
		this.deleterFactory.deleter(NodeAccessDeleter.class).delete(toDelete);
		
		for (UUID nodeId : nodeIds) {
			if (!nodeItemsLookup.containsKey(nodeId)) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{nodeId, Node.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			NodeEntity masterItem = nodeItemsLookup.get(nodeId);
			if (masterItem == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{nodeId, Node.class.getSimpleName()}, LocaleContextHolder.getLocale()));

			NodeAccessEntity data = existingNodeAccessesLookup.getOrDefault(nodeId, null);
			boolean isUpdate = data != null;
			if (!isUpdate){
				data = new NodeAccessEntity();
				data.setId(UUID.randomUUID());
				data.setUserId(userId);
				data.setNodeId(nodeId);
				data.setIsActive(IsActive.ACTIVE);
				data.setCreatedAt(Instant.now());
			}

			data.setIsActive(IsActive.ACTIVE);
			data.setUpdatedAt(Instant.now());

			if (isUpdate) this.entityManager.merge(data);
			else this.entityManager.persist(data);
		}
		this.entityManager.flush();
	}

	private void persistEdgeAccess(UUID userId, List<UUID> edgeIds) throws MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		if (edgeIds == null) edgeIds = new ArrayList<>();

		List<EdgeEntity> edgeEntities = this.queryFactory.query(EdgeQuery.class).ids(edgeIds).isActive(IsActive.ACTIVE)
				.collectAs(new BaseFieldSet().ensure(Edge._id).ensure(Edge._hash));
		Map<UUID, EdgeEntity> edgeItemsLookup = edgeEntities.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));

		List<EdgeAccessEntity> existingEdgeAccesses = this.queryFactory.query(EdgeAccessQuery.class)
				.edgeIds(edgeIds).userIds(userId).collect();
		Map<UUID, EdgeAccessEntity> existingEdgeAccessesLookup = existingEdgeAccesses.stream().collect(Collectors.toMap(x -> x.getEdgeId(), x -> x));

		final List<UUID> edgeIdsFinal = edgeIds;
		List<EdgeAccessEntity> toDelete = existingEdgeAccesses.stream().filter(x -> !edgeIdsFinal.contains(x.getId())).collect(Collectors.toList());
		this.deleterFactory.deleter(EdgeAccessDeleter.class).delete(toDelete);

		for (UUID edgeId : edgeIds) {
			if (!edgeItemsLookup.containsKey(edgeId)) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{edgeId, Edge.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			EdgeEntity masterItem = edgeItemsLookup.get(edgeId);
			if (masterItem == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{edgeId, Edge.class.getSimpleName()}, LocaleContextHolder.getLocale()));

			EdgeAccessEntity data = existingEdgeAccessesLookup.getOrDefault(edgeId, null);
			boolean isUpdate = data != null;
			if (!isUpdate){
				data = new EdgeAccessEntity();
				data.setId(UUID.randomUUID());
				data.setUserId(userId);
				data.setEdgeId(edgeId);
				data.setIsActive(IsActive.ACTIVE);
				data.setCreatedAt(Instant.now());
			}

			data.setIsActive(IsActive.ACTIVE);
			data.setUpdatedAt(Instant.now());

			if (isUpdate) this.entityManager.merge(data);
			else this.entityManager.persist(data);
		}
		this.entityManager.flush();
	}

	@Override
	public User persist(UserTouchedIntegrationEventPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting User").And("model", model).And("fields", fields));

		this.authorizationService.authorizeForce(Permission.EditUser);

		Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

		UserEntity data = null;
		if (isUpdate) {
			data = this.entityManager.find(UserEntity.class, model.getId());
			if (data == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		} else {
			data = new UserEntity();
			data.setId(model.getId());
			data.setIsActive(IsActive.ACTIVE);
			data.setCreatedAt(Instant.now());
		}
		String previousSubjectId = data.getSubjectId();

		data.setFirstName(model.getFirstName());
		data.setLastName(model.getLastName());
		data.setTimezone(localeService.timezoneName());
		data.setCulture(localeService.cultureName());
		data.setLanguage(localeService.language());
		data.setUpdatedAt(Instant.now());

		if (isUpdate) this.entityManager.merge(data);
		else this.entityManager.persist(data);

		this.entityManager.flush();

		this.eventBroker.emit(new UserTouchedEvent(data.getId(), data.getSubjectId(), previousSubjectId));

		User persisted = this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.OwnerOrPermission).build(BaseFieldSet.build(fields, User._id, User._hash), data);
		return persisted;
	}

	public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
		logger.debug("deleting User: {}", id);

		this.authorizationService.authorizeForce(Permission.DeleteUser);

		this.deleterFactory.deleter(UserDeleter.class).deleteAndSaveByIds(List.of(id));
	}
}
