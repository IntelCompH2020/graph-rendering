package gr.cite.intelcomp.graphexplorer.service.graph;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.NodeConfigEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.GraphEdgeEntity;
import gr.cite.intelcomp.graphexplorer.data.GraphEntity;
import gr.cite.intelcomp.graphexplorer.data.GraphNodeEntity;
import gr.cite.intelcomp.graphexplorer.errorcode.ErrorThesaurusProperties;
import gr.cite.intelcomp.graphexplorer.event.EventBroker;
import gr.cite.intelcomp.graphexplorer.model.GraphInfo;
import gr.cite.intelcomp.graphexplorer.model.GraphInfoLookup;
import gr.cite.intelcomp.graphexplorer.model.builder.GraphBuilder;
import gr.cite.intelcomp.graphexplorer.model.deleter.EdgeDeleter;
import gr.cite.intelcomp.graphexplorer.model.deleter.GraphEdgeDeleter;
import gr.cite.intelcomp.graphexplorer.model.deleter.GraphNodeDeleter;
import gr.cite.intelcomp.graphexplorer.model.persist.GraphPersist;
import gr.cite.intelcomp.graphexplorer.query.GraphEdgeQuery;
import gr.cite.intelcomp.graphexplorer.query.GraphNodeQuery;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigService;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigService;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.management.InvalidApplicationException;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseGraphServiceImpl implements GraphService {
	protected abstract NodeConfigEntity recalculateNodeSizePrivate(UUID nodeId);
	
	private final EntityManager entityManager;
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(BaseGraphServiceImpl.class));
	private final BuilderFactory builderFactory;
	private final QueryFactory queryFactory;
	private final ErrorThesaurusProperties errors;
	private final MessageSource messageSource;
	private final JsonHandlingService jsonHandlingService;
	private final AuthorizationContentResolver authorizationContentResolver;
	private final NodeConfigService nodeConfigService;
	private final EdgeConfigService edgeConfigService;
	private final GraphProperties graphProperties;
	private final AuthorizationService authorizationService;
	private final DeleterFactory deleterFactory;
	private final ConventionService conventionService;
	private final EventBroker eventBroker;

	public BaseGraphServiceImpl(EntityManager entityManager,
	                            BuilderFactory builderFactory,
	                            QueryFactory queryFactory,
	                            ErrorThesaurusProperties errors,
	                            MessageSource messageSource,
	                            JsonHandlingService jsonHandlingService,
	                            AuthorizationContentResolver authorizationContentResolver,
	                            NodeConfigService nodeConfigService,
	                            EdgeConfigService edgeConfigService,
	                            GraphProperties graphProperties,
	                            AuthorizationService authorizationService,
	                            DeleterFactory deleterFactory,
	                            ConventionService conventionService,
	                            EventBroker eventBroker) {
		this.entityManager = entityManager;
		this.builderFactory = builderFactory;
		this.queryFactory = queryFactory;
		this.errors = errors;
		this.messageSource = messageSource;
		this.jsonHandlingService = jsonHandlingService;
		this.authorizationContentResolver = authorizationContentResolver;
		this.nodeConfigService = nodeConfigService;
		this.edgeConfigService = edgeConfigService;
		this.graphProperties = graphProperties;
		this.authorizationService = authorizationService;
		this.deleterFactory = deleterFactory;
		this.conventionService = conventionService;
		this.eventBroker = eventBroker;
	}

	@Override
	public gr.cite.intelcomp.graphexplorer.model.Graph persist(GraphPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting data Edge").And("model", model).And("fields", fields));

		Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

		GraphEntity data;
		if (isUpdate) {
			data = this.entityManager.find(GraphEntity.class, model.getId());
			if (data == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), gr.cite.intelcomp.graphexplorer.model.Graph.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		} else {
			data = new GraphEntity();
			data.setId(UUID.randomUUID());
			data.setIsActive(IsActive.ACTIVE);
			data.setCreatedAt(Instant.now());
		}

		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.edgeAffiliation(data.getId())), Permission.EditGraph);

		data.setName(model.getName());
		data.setDescription(model.getDescription());
		data.setUpdatedAt(Instant.now());
		if (isUpdate) this.entityManager.merge(data);
		else this.entityManager.persist(data);

		this.persistGraphEdgeEntity(data.getId(), model.getEdgeIds());
		this.persistGraphNodeEntity(data.getId(), model.getNodeIds());
		this.entityManager.flush();

		return this.builderFactory.builder(GraphBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(BaseFieldSet.build(fields, gr.cite.intelcomp.graphexplorer.model.Graph._id), data);
	}
	private void persistGraphEdgeEntity(UUID graphId, List<UUID> edgeIds) throws MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		List<GraphEdgeEntity> graphItems = this.queryFactory.query(GraphEdgeQuery.class).graphIds(graphId).collect();
		Map<UUID, GraphEdgeEntity> graphItemsLookup = graphItems.stream().collect(Collectors.toMap(x -> x.getEdgeId(), x -> x));
		if (edgeIds != null){
			for (UUID edgeId : edgeIds) {
				GraphEdgeEntity data = graphItemsLookup.getOrDefault(edgeId, null);
				Boolean isUpdate = data != null;

				if (!isUpdate) {
					data = new GraphEdgeEntity();
					data.setId(UUID.randomUUID());
					data.setGraphId(graphId);
					data.setEdgeId(edgeId);
					data.setCreatedAt(Instant.now());
				}

				data.setIsActive(IsActive.ACTIVE);
				data.setUpdatedAt(Instant.now());

				if (isUpdate) this.entityManager.merge(data);
				else this.entityManager.persist(data);
			}

		}
		List<GraphEdgeEntity> toDelete = graphItems.stream().filter(x -> !edgeIds.contains(x.getEdgeId())).collect(Collectors.toList());
		this.deleterFactory.deleter(GraphEdgeDeleter.class).delete(toDelete);
		
		this.entityManager.flush();
	}

	private void persistGraphNodeEntity(UUID graphId, List<UUID> nodeIds) throws MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		List<GraphNodeEntity> graphItems = this.queryFactory.query(GraphNodeQuery.class).graphIds(graphId).collect();
		Map<UUID, GraphNodeEntity> graphItemsLookup = graphItems.stream().collect(Collectors.toMap(x -> x.getNodeId(), x -> x));
		if (nodeIds != null){
			for (UUID nodeId : nodeIds) {
				GraphNodeEntity data = graphItemsLookup.getOrDefault(nodeId, null);
				Boolean isUpdate = data != null;

				if (!isUpdate) {
					data = new GraphNodeEntity();
					data.setId(UUID.randomUUID());
					data.setGraphId(graphId);
					data.setNodeId(nodeId);
					data.setCreatedAt(Instant.now());
				}

				data.setIsActive(IsActive.ACTIVE);
				data.setUpdatedAt(Instant.now());

				if (isUpdate) this.entityManager.merge(data);
				else this.entityManager.persist(data);
			}

		}
		List<GraphNodeEntity> toDelete = graphItems.stream().filter(x -> !nodeIds.contains(x.getNodeId())).collect(Collectors.toList());
		this.deleterFactory.deleter(GraphNodeDeleter.class).delete(toDelete);

		this.entityManager.flush();
	}

	@Override
	public GraphInfo getGraphInfo(GraphInfoLookup lookup) {
		List<UUID> allowedNodeIds = null;
		if (!this.authorizationService.authorize(Permission.BrowseNodeData)){
			allowedNodeIds = this.authorizationContentResolver.affiliatedNodes(Permission.BrowseNodeData);
			if (allowedNodeIds == null) {
				lookup.setNodeIds(new ArrayList<>());
			} else {
				Set<UUID> result = lookup.getNodeIds().stream().distinct().filter(allowedNodeIds::contains).collect(Collectors.toSet());
				lookup.setNodeIds(result.stream().collect(Collectors.toList()));
			}
		}

		Double ymax = null;
		Double xmax = null;
		Double ymin = null;
		Double xmin = null;
		for (UUID nodeId: lookup.getNodeIds()) {
			NodeConfigItem nodeConfigItem = this.nodeConfigService.getConfig(nodeId);
			NodeConfigEntity nodeConfigEntity = null;
			if (nodeConfigItem.getConfigEntity() == null || nodeConfigItem.getConfigEntity().getMaxX() == null || nodeConfigItem.getConfigEntity().getMaxY() == null
					|| nodeConfigItem.getConfigEntity().getMinX() == null || nodeConfigItem.getConfigEntity().getMinY() == null) {
				nodeConfigEntity = this.recalculateNodeSizePrivate(nodeId);
			}else {
				nodeConfigEntity = nodeConfigItem.getConfigEntity();
			}
			if (ymax == null || ymax < nodeConfigEntity.getMaxY()) ymax = nodeConfigEntity.getMaxY();
			if (xmax == null || xmax < nodeConfigEntity.getMaxX()) xmax = nodeConfigEntity.getMaxX();
			if (ymin == null || ymin > nodeConfigEntity.getMinY()) ymin = nodeConfigEntity.getMinY();
			if (xmin == null || xmin > nodeConfigEntity.getMinX()) xmin = nodeConfigEntity.getMinX();
		}

		
		GraphInfo graphInfo = new GraphInfo(xmin, ymin, xmax,ymax);

		return graphInfo;
	}

	@Override
	public void recalculateNodeSize(UUID nodeId)  {
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.nodeAffiliation(nodeId)), Permission.EditNode);
		this.recalculateNodeSizePrivate(nodeId);
	}

	public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
		logger.debug("deleting dataset: {}", id);
		GraphEntity data = this.entityManager.find(GraphEntity.class, id);
		if (data == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{id, gr.cite.intelcomp.graphexplorer.model.Edge.class.getSimpleName()}, LocaleContextHolder.getLocale()));

		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.graphAffiliation(data.getId())), Permission.DeleteGraph);

		this.deleterFactory.deleter(EdgeDeleter.class).deleteAndSaveByIds(List.of(id));
	}
}
