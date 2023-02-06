package gr.cite.intelcomp.graphexplorer.model.deleter;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.data.GraphAccessEntity;
import gr.cite.intelcomp.graphexplorer.data.GraphEdgeEntity;
import gr.cite.intelcomp.graphexplorer.data.GraphEntity;
import gr.cite.intelcomp.graphexplorer.data.GraphNodeEntity;
import gr.cite.intelcomp.graphexplorer.query.GraphAccessQuery;
import gr.cite.intelcomp.graphexplorer.query.GraphEdgeQuery;
import gr.cite.intelcomp.graphexplorer.query.GraphNodeQuery;
import gr.cite.intelcomp.graphexplorer.query.GraphQuery;
import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GraphDeleter implements Deleter {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(GraphDeleter.class));

	private final EntityManager entityManager;
	protected final QueryFactory queryFactory;
	protected final DeleterFactory deleterFactory;

	@Autowired
	public GraphDeleter(
			EntityManager entityManager,
			QueryFactory queryFactory,
			DeleterFactory deleterFactory
	) {
		this.entityManager = entityManager;
		this.queryFactory = queryFactory;
		this.deleterFactory = deleterFactory;
	}

	public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
		logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
		List<GraphEntity> data = this.queryFactory.query(GraphQuery.class).ids(ids).collect();
		logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
		this.deleteAndSave(data);
	}

	public void deleteAndSave(List<GraphEntity> data) throws InvalidApplicationException {
		logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
		this.delete(data);
		logger.trace("saving changes");
		this.entityManager.flush();
		logger.trace("changes saved");
	}

	public void delete(List<GraphEntity> data) throws InvalidApplicationException {
		logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
		if (data == null || data.isEmpty()) return;

		Instant now = Instant.now();

		List<UUID> ids = data.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
		{
			logger.debug("checking related - {}", GraphAccessEntity.class.getSimpleName());
			List<GraphAccessEntity> items = this.queryFactory.query(GraphAccessQuery.class).graphIds(ids).collect();
			GraphAccessDeleter deleter = this.deleterFactory.deleter(GraphAccessDeleter.class);
			deleter.delete(items);
		}

		{
			logger.debug("checking related - {}", GraphNodeEntity.class.getSimpleName());
			List<GraphNodeEntity> items = this.queryFactory.query(GraphNodeQuery.class).graphIds(ids).collect();
			GraphNodeDeleter deleter = this.deleterFactory.deleter(GraphNodeDeleter.class);
			deleter.delete(items);
		}

		{
			logger.debug("checking related - {}", GraphEdgeEntity.class.getSimpleName());
			List<GraphEdgeEntity> items = this.queryFactory.query(GraphEdgeQuery.class).graphIds(ids).collect();
			GraphEdgeDeleter deleter = this.deleterFactory.deleter(GraphEdgeDeleter.class);
			deleter.delete(items);
		}

		for (GraphEntity item : data) {
			logger.trace("deleting item {}", item.getId());
			item.setIsActive(IsActive.INACTIVE);
			item.setUpdatedAt(now);
			logger.trace("updating item");
			this.entityManager.merge(item);
			logger.trace("updated item");
		}
	}

}
