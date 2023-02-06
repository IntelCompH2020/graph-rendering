package gr.cite.intelcomp.graphexplorer.service.graph;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.NodeConfigEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.GraphDataEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.NodeEntity;
import gr.cite.intelcomp.graphexplorer.elastic.data.EdgeDataEntity;
import gr.cite.intelcomp.graphexplorer.elastic.data.NodeDataEntity;
import gr.cite.intelcomp.graphexplorer.elastic.query.EdgeDataQuery;
import gr.cite.intelcomp.graphexplorer.elastic.query.NodeDataQuery;
import gr.cite.intelcomp.graphexplorer.errorcode.ErrorThesaurusProperties;
import gr.cite.intelcomp.graphexplorer.event.EventBroker;
import gr.cite.intelcomp.graphexplorer.event.NodeTouchedEvent;
import gr.cite.intelcomp.graphexplorer.model.GraphData;
import gr.cite.intelcomp.graphexplorer.model.builder.GraphDataBuilder;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgeDataPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.NodeDataPersist;
import gr.cite.intelcomp.graphexplorer.query.NodeQuery;
import gr.cite.intelcomp.graphexplorer.query.lookup.GraphDataLookup;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigService;
import gr.cite.intelcomp.graphexplorer.service.elasticedge.ElasticEdgeService;
import gr.cite.intelcomp.graphexplorer.service.elasticnode.ElasticNodeService;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.query.Aggregation.*;
import gr.cite.tools.elastic.query.ScrollResponse;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequestScope
@ConditionalOnProperty(prefix = "elastic", name = "enabled", havingValue = "true")
public class ElasticGraphServiceImpl extends BaseGraphServiceImpl {
	private final EntityManager entityManager;
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ElasticGraphServiceImpl.class));
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
	private final ElasticsearchRestTemplate elasticsearchTemplate;
	private final ElasticNodeService elasticNodeService;
	private final ElasticEdgeService elasticEdgeService;
	private final ElasticProperties elasticProperties;
	
	public ElasticGraphServiceImpl(EntityManager entityManager,
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
	                               EventBroker eventBroker,
	                               ElasticsearchRestTemplate elasticsearchTemplate,
	                               ElasticNodeService elasticNodeService,
	                               ElasticEdgeService elasticEdgeService, 
	                               ElasticProperties elasticProperties) {
		super(entityManager,
				builderFactory,
				queryFactory,
				errors,
				messageSource,
				jsonHandlingService,
				authorizationContentResolver,
				nodeConfigService,
				edgeConfigService,
				graphProperties,
				authorizationService,
				deleterFactory,
				conventionService,
				eventBroker
				);

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
		this.elasticsearchTemplate = elasticsearchTemplate;
		this.elasticNodeService = elasticNodeService;
		this.elasticEdgeService = elasticEdgeService;
		this.elasticProperties = elasticProperties;
	}

	
	protected NodeConfigEntity recalculateNodeSizePrivate(UUID nodeId)  {
		logger.debug(new MapLogEntry("recalculate node size").And("nodeId", nodeId));

		NodeEntity data = this.queryFactory.query(NodeQuery.class).ids(nodeId).first();
		NodeConfigEntity nodeConfigEntity = this.jsonHandlingService.fromJsonSafe(NodeConfigEntity.class, data.getConfig());
		if (nodeConfigEntity == null) {
			nodeConfigEntity = new NodeConfigEntity();
		}
		AggregationQuery aggregationQuery = new AggregationQuery();
		List<Metric> metrics = new ArrayList<>();
		metrics.add(new Metric(NodeDataEntity.Fields.y, MetricAggregateType.Max));
		metrics.add(new Metric(NodeDataEntity.Fields.y, MetricAggregateType.Min));
		metrics.add(new Metric(NodeDataEntity.Fields.x, MetricAggregateType.Max));
		metrics.add(new Metric(NodeDataEntity.Fields.x, MetricAggregateType.Min));
		aggregationQuery.setMetrics(metrics);

		AggregateResponse aggregateResponse = this.queryFactory.query(NodeDataQuery.class).nodeIds(nodeId).collectAggregate(aggregationQuery);
		for (AggregateResponseItem aggregateResponseItem : aggregateResponse.getItems()) {
			if (aggregateResponseItem.getValues() == null) continue;
			for (AggregateResponseValue aggregateResponseValue : aggregateResponseItem.getValues()) {
				if (aggregateResponseValue.getAggregateType() == MetricAggregateType.Max && aggregateResponseValue.getField().equalsIgnoreCase(NodeDataEntity.Fields.y)) nodeConfigEntity.setMaxY(aggregateResponseValue.getValue());
				if (aggregateResponseValue.getAggregateType() == MetricAggregateType.Min && aggregateResponseValue.getField().equalsIgnoreCase(NodeDataEntity.Fields.y)) nodeConfigEntity.setMinY(aggregateResponseValue.getValue());
				if (aggregateResponseValue.getAggregateType() == MetricAggregateType.Max && aggregateResponseValue.getField().equalsIgnoreCase(NodeDataEntity.Fields.x)) nodeConfigEntity.setMaxX(aggregateResponseValue.getValue());
				if (aggregateResponseValue.getAggregateType() == MetricAggregateType.Min && aggregateResponseValue.getField().equalsIgnoreCase(NodeDataEntity.Fields.x)) nodeConfigEntity.setMinX(aggregateResponseValue.getValue());
			}
		}
		data.setUpdatedAt(Instant.now());
		data.setConfig(jsonHandlingService.toJsonSafe(nodeConfigEntity));
		this.entityManager.merge(data);

		this.entityManager.flush();
		this.eventBroker.emit(new NodeTouchedEvent(data.getId()));
		return nodeConfigEntity;
	}

	@Override
	public void persistNode(UUID nodeId, NodeDataPersist model) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException {
		logger.debug(new MapLogEntry("persisting dataset").And("model", model));
		
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.nodeAffiliation(nodeId)), Permission.EditNodeData);
		this.elasticNodeService.ensureIndex(nodeId);

		NodeConfigItem nodeConfigItem = this.nodeConfigService.getConfig(nodeId);
		NodeDataEntity data = this.buildNode(nodeId, nodeConfigItem, model);

		data = elasticsearchTemplate.save(data, IndexCoordinates.of(this.elasticNodeService.getIndexName(nodeId)));
	}

	@Override
	public void persistNodes(UUID nodeId, List<NodeDataPersist> models) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException {
		logger.debug(new MapLogEntry("persisting dataset").And("models", models));
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.nodeAffiliation(nodeId)), Permission.EditNodeData);

		NodeConfigItem nodeConfigItem = this.nodeConfigService.getConfig(nodeId);
		this.elasticNodeService.ensureIndex(nodeId);
		List<NodeDataEntity> items = new ArrayList<>();
		for (NodeDataPersist model : models) {
			NodeDataEntity data = this.buildNode(nodeId, nodeConfigItem, model);
			items.add(data);
			if (items.size() > this.graphProperties.getNodeImportBatchSize()) {
				elasticsearchTemplate.save(items, IndexCoordinates.of(this.elasticNodeService.getIndexName(nodeId)));
				items = new ArrayList<>();
			}
		}
		if (items.size() > 0) elasticsearchTemplate.save(items, IndexCoordinates.of(this.elasticNodeService.getIndexName(nodeId)));
	}

	private NodeDataEntity buildNode(UUID nodeId, NodeConfigItem nodeConfigItem, NodeDataPersist model) {
		NodeDataEntity data = new NodeDataEntity();
		data.setId(model.getId());
		data.setNodeId(nodeId);
		data.setLabel(nodeConfigItem.getCode());
		data.setName(model.getName());
		data.setX(model.getX());
		data.setY(model.getY());

		if (model.getProperties() != null && nodeConfigItem.getConfigEntity() != null && nodeConfigItem.getConfigEntity().getFields() != null) {
			data.setProperties(this.applyExtraProperties(nodeConfigItem.getConfigEntity().getFields(), model.getProperties()));
		}
		return data;
	}

	private Map<String, Object> applyExtraProperties(List<FieldDefinitionEntity> fieldDefinitions, Map<String, Object> propertiesMap) {
		if (fieldDefinitions == null || propertiesMap == null) return null;
		Map<String, Object> properties = new HashMap<>();
		for (Map.Entry<String, Object> prop : propertiesMap.entrySet()) {
			FieldDefinitionEntity fieldEntity = fieldDefinitions.stream().filter(x-> x.getCode().equalsIgnoreCase(prop.getKey())).findFirst().orElse(null);
			if (fieldEntity != null) {
				switch (fieldEntity.getType()) {
					case String:
						try {
							if (prop.getValue() == null) {
								properties.put(prop.getKey(), (Map<String, Double>) null);
							} else {
								properties.put(prop.getKey(), String.class.cast(prop.getValue()));
							}
						} catch (Exception e) {
							throw e;
						}
						break;
					case Date:
						if (prop.getValue() == null) {
							properties.put(prop.getKey(), (Date) null);
						} else {
							ZonedDateTime zonedDateTime = ZonedDateTime.parse(String.class.cast(prop.getValue()));
							properties.put(prop.getKey(), new Date(zonedDateTime.toInstant().toEpochMilli()));
						}
						break;
					case Double:
						if (prop.getValue() == null) {
							properties.put(prop.getKey(), (Double) null);
						} else {
							if (prop.getValue().getClass().equals(Integer.class)) properties.put(prop.getKey(), Double.valueOf(Integer.class.cast(prop.getValue())));
							else properties.put(prop.getKey(), Double.class.cast(prop.getValue()));
						}
						break;
					case Integer:
						if (prop.getValue() == null) {
							properties.put(prop.getKey(), (Integer) null);
						} else {
							properties.put(prop.getKey(), Integer.class.cast(prop.getValue()));
						}
						break;
					default:
						throw new MyApplicationException("invalid type " + fieldEntity.getCode());
				}
			}
		}
		
		return properties;
	}

	@Override
	public void persistEdge(UUID nodeId, UUID edgeId, EdgeDataPersist model) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException {
		logger.debug(new MapLogEntry("persisting dataset").And("model", model));

		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.edgeAffiliation(edgeId)), Permission.EditEdgeData);
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.nodeAffiliation(nodeId)), Permission.EditNode);
		
		this.elasticEdgeService.ensureIndex(edgeId);
		
		EdgeConfigItem edgeConfigItem = this.edgeConfigService.getConfig(edgeId);

		EdgeDataEntity data = this.buildEdge(edgeId, edgeConfigItem, model);

		data = elasticsearchTemplate.save(data, IndexCoordinates.of(this.elasticEdgeService.getIndexName(nodeId)));
	}

	@Override
	public void persistEdges(UUID nodeId, UUID edgeId, List<EdgeDataPersist> models) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException {
		logger.debug(new MapLogEntry("persisting dataset").And("models", models));
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.edgeAffiliation(edgeId)), Permission.EditEdgeData);
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.nodeAffiliation(nodeId)), Permission.EditNode);

		this.elasticEdgeService.ensureIndex(edgeId);

		EdgeConfigItem edgeConfigItem = this.edgeConfigService.getConfig(edgeId);
		NodeConfigItem nodeConfigItem = this.nodeConfigService.getConfig(nodeId);

		List<EdgeDataEntity> items = new ArrayList<>();
		for (EdgeDataPersist model : models) {
			EdgeDataEntity data = this.buildEdge(edgeId, edgeConfigItem, model);
			items.add(data);
			if (items.size() > this.graphProperties.getNodeImportBatchSize()) {
				elasticsearchTemplate.save(items, IndexCoordinates.of(this.elasticEdgeService.getIndexName(edgeId)));
				items = new ArrayList<>();
			}
		}
		if (items.size() > 0) elasticsearchTemplate.save(items, IndexCoordinates.of(this.elasticEdgeService.getIndexName(edgeId)));
	}

	private EdgeDataEntity buildEdge(UUID edgeId, EdgeConfigItem edgeConfigItem, EdgeDataPersist model) {
		EdgeDataEntity data = new EdgeDataEntity();
		data.setId(UUID.randomUUID().toString().toLowerCase());
		data.setLabel(edgeConfigItem.getCode());
		data.setEdgeId(edgeId);
		data.setSourceId(model.getSourceId());
		data.setTargetId(model.getTargetId());
		data.setWeight(model.getWeight());

		if (model.getProperties() != null && edgeConfigItem.getConfigEntity() != null && edgeConfigItem.getConfigEntity().getFields() != null) {
			data.setProperties(this.applyExtraProperties(edgeConfigItem.getConfigEntity().getFields(), model.getProperties()));
		}
		return data;
	}

	@Override
	public GraphData getGraphData(GraphDataLookup lookup) {
		FieldSet edgesFields = lookup.getProject().extractPrefixed(this.conventionService.asPrefix(GraphData._edges));
		FieldSet nodesFields = lookup.getProject().extractPrefixed(this.conventionService.asPrefix(GraphData._nodes));

		NodeDataQuery nodeDataQuery = lookup.enrichElastic(this.queryFactory).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated);
		List<NodeDataEntity> nodeDataEntities = nodeDataQuery.collectAs(nodesFields);
		EdgeDataQuery edgeDataQuery = this.queryFactory.query(EdgeDataQuery.class).edgeIds(lookup.getEdgeIds()).sourceTargetIds(nodeDataEntities.stream().map(x-> x.getId()).distinct().collect(Collectors.toList())).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated);
		List<EdgeDataEntity> edgeDataEntities = new ArrayList<>();
		ScrollResponse<EdgeDataEntity> edgeDataScrollResponse = edgeDataQuery.collectProjectedWithScroll(edgesFields, this.graphProperties.getEdgeScrollSize(), (long)this.elasticProperties.getDefaultScrollSeconds());
		do {
			if (edgeDataScrollResponse == null) break;
			edgeDataEntities.addAll(edgeDataScrollResponse.getItems());
			edgeDataScrollResponse = edgeDataQuery.scroll(edgeDataScrollResponse.getScrollId());
		} while (edgeDataScrollResponse != null && !edgeDataScrollResponse.getItems().isEmpty() && edgeDataEntities.size() < this.graphProperties.getMaxEdgeResultSize());
		
		GraphDataEntity graph = GraphDataEntity.buildFromEElastic(nodeDataEntities, edgeDataEntities);
		if (lookup.getProject() != null && lookup.getProject().hasField(GraphDataEntity._size)) graph.setSize(nodeDataQuery.count());
		GraphData models = this.builderFactory.builder(GraphDataBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(lookup.getProject(), graph);
		return models;
	}
}
