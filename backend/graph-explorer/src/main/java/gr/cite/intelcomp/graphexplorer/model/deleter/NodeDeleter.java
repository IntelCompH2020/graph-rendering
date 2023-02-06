package gr.cite.intelcomp.graphexplorer.model.deleter;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.data.GraphNodeEntity;
import gr.cite.intelcomp.graphexplorer.data.NodeAccessEntity;
import gr.cite.intelcomp.graphexplorer.data.NodeEntity;
import gr.cite.intelcomp.graphexplorer.query.GraphNodeQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeAccessQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeQuery;
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
public class NodeDeleter implements Deleter {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(NodeDeleter.class));

	private final EntityManager entityManager;
	protected final QueryFactory queryFactory;
	protected final DeleterFactory deleterFactory;

	@Autowired
	public NodeDeleter(
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
		List<NodeEntity> data = this.queryFactory.query(NodeQuery.class).ids(ids).collect();
		logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
		this.deleteAndSave(data);
	}

	public void deleteAndSave(List<NodeEntity> data) throws InvalidApplicationException {
		logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
		this.delete(data);
		logger.trace("saving changes");
		this.entityManager.flush();
		logger.trace("changes saved");
	}

	public void delete(List<NodeEntity> data) throws InvalidApplicationException {
		logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
		if (data == null || data.isEmpty()) return;

		Instant now = Instant.now();

		List<UUID> ids = data.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
		{
			logger.debug("checking related - {}", NodeAccessEntity.class.getSimpleName());
			List<NodeAccessEntity> items = this.queryFactory.query(NodeAccessQuery.class).nodeIds(ids).collect();
			NodeAccessDeleter deleter = this.deleterFactory.deleter(NodeAccessDeleter.class);
			deleter.delete(items);
		}

		{
			logger.debug("checking related - {}", GraphNodeEntity.class.getSimpleName());
			List<GraphNodeEntity> items = this.queryFactory.query(GraphNodeQuery.class).nodeIds(ids).collect();
			GraphNodeDeleter deleter = this.deleterFactory.deleter(GraphNodeDeleter.class);
			deleter.delete(items);
		}

		for (NodeEntity item : data) {
			logger.trace("deleting item {}", item.getId());
			item.setIsActive(IsActive.INACTIVE);
			item.setUpdatedAt(now);
			logger.trace("updating item");
			this.entityManager.merge(item);
			logger.trace("updated item");
		}
	}

}
