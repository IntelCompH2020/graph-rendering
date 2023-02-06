package gr.cite.intelcomp.graphexplorer.service.graph;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.NodeConfigEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.GraphDataEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.NodeDataEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.GraphEdgeEntity;
import gr.cite.intelcomp.graphexplorer.data.GraphEntity;
import gr.cite.intelcomp.graphexplorer.data.GraphNodeEntity;
import gr.cite.intelcomp.graphexplorer.data.NodeEntity;
import gr.cite.intelcomp.graphexplorer.errorcode.ErrorThesaurusProperties;
import gr.cite.intelcomp.graphexplorer.event.EventBroker;
import gr.cite.intelcomp.graphexplorer.event.NodeTouchedEvent;
import gr.cite.intelcomp.graphexplorer.model.*;
import gr.cite.intelcomp.graphexplorer.model.builder.GraphBuilder;
import gr.cite.intelcomp.graphexplorer.model.builder.GraphDataBuilder;
import gr.cite.intelcomp.graphexplorer.model.deleter.EdgeDeleter;
import gr.cite.intelcomp.graphexplorer.model.deleter.GraphEdgeDeleter;
import gr.cite.intelcomp.graphexplorer.model.deleter.GraphNodeDeleter;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgeDataPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.GraphPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.NodeDataPersist;
import gr.cite.intelcomp.graphexplorer.query.GraphEdgeQuery;
import gr.cite.intelcomp.graphexplorer.query.GraphNodeQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeDataQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeQuery;
import gr.cite.intelcomp.graphexplorer.query.lookup.GraphDataLookup;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigService;
import gr.cite.intelcomp.graphexplorer.service.gremlin.common.GremlinFactory;
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
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequestScope
@ConditionalOnProperty(prefix = "gremlin", name = "enabled", havingValue = "true")
public class GremlinGraphServiceImpl extends BaseGraphServiceImpl {
	private final EntityManager entityManager;
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(GremlinGraphServiceImpl.class));
	private final GremlinFactory gremlinFactory;
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

	public GremlinGraphServiceImpl(EntityManager entityManager,
	                               GremlinFactory gremlinFactory,
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
		this.gremlinFactory = gremlinFactory;
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

	
	protected NodeConfigEntity recalculateNodeSizePrivate(UUID nodeId)  {
		logger.debug(new MapLogEntry("recalculate node size").And("nodeId", nodeId));

		GraphTraversalSource g = gremlinFactory.getGraphTraversalSource();
		NodeEntity data = this.queryFactory.query(NodeQuery.class).ids(nodeId).first();
		NodeConfigEntity nodeConfigEntity = this.jsonHandlingService.fromJsonSafe(NodeConfigEntity.class, data.getConfig());
		if (nodeConfigEntity == null) {
			nodeConfigEntity = new NodeConfigEntity();
		}
		String[] nodeLabels = this.getNodeLabels(List.of(nodeId));
		nodeConfigEntity.setMaxY((Double)g.V().hasLabel(P.within(nodeLabels)).values(NodeDataEntity._y).max().next());
		nodeConfigEntity.setMaxX((Double)g.V().hasLabel(P.within(nodeLabels)).values(NodeDataEntity._x).max().next());
		nodeConfigEntity.setMinY((Double)g.V().hasLabel(P.within(nodeLabels)).values(NodeDataEntity._y).min().next());
		nodeConfigEntity.setMinX((Double)g.V().hasLabel(P.within(nodeLabels)).values(NodeDataEntity._x).min().next());

		data.setUpdatedAt(Instant.now());
		data.setConfig(jsonHandlingService.toJsonSafe(nodeConfigEntity));
		this.entityManager.merge(data);

		this.entityManager.flush();
		this.eventBroker.emit(new NodeTouchedEvent(data.getId()));
		return nodeConfigEntity;
	}

	private String[] getNodeLabels(List<UUID> nodeIds) {
		if (nodeIds != null && !nodeIds.isEmpty()) {
			List<String> nodeNames = new ArrayList<>();
			for (UUID nodeId : nodeIds) nodeNames.add(this.nodeConfigService.getConfig(nodeId).getCode());
			return nodeNames.toArray(new String[nodeNames.size()]);
		} else {
			throw new RuntimeException("Node Label Not Set");
		}
	}

	@Override
	public void persistNode(UUID nodeId, NodeDataPersist model) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting dataset").And("model", model));

		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.nodeAffiliation(nodeId)), Permission.EditNodeData);
		NodeConfigItem nodeConfigItem = this.nodeConfigService.getConfig(nodeId);

		GraphTraversalSource g = gremlinFactory.getGraphTraversalSource();
		Transaction tx = null;
		try {
			tx = this.gremlinFactory.openTransaction(g);
			GraphTraversal<Vertex, Vertex> data = this.buildNode(g, nodeId, nodeConfigItem, model, null);
			Vertex v = data.next();
			this.gremlinFactory.commitTransaction(tx);
		} catch (Exception ex) {
			this.gremlinFactory.rollbackTransaction(tx);
			throw ex;
		}
	}

	@Override
	public void persistNodes(UUID nodeId, List<NodeDataPersist> models) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting dataset").And("models", models));
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.nodeAffiliation(nodeId)), Permission.EditNodeData);

		NodeConfigItem nodeConfigItem = this.nodeConfigService.getConfig(nodeId);
		GraphTraversalSource graphTraversalSource = gremlinFactory.getGraphTraversalSource();
		Transaction tx = null;
		try {
			tx = this.gremlinFactory.openTransaction(graphTraversalSource);
			int items = 0;
			GraphTraversal<Vertex, Vertex> data = null;
			for (NodeDataPersist model : models) {
				data = this.buildNode(graphTraversalSource, nodeId, nodeConfigItem, model, data);
				items++;
				if (items > this.graphProperties.getNodeImportBatchSize()) {
					data.iterate();
					data = null;
					items = 0;
				}
			}
			if (data != null) data.iterate();

			this.gremlinFactory.commitTransaction(tx);
		} catch (Exception ex) {
			this.gremlinFactory.rollbackTransaction(tx);
			throw ex;
		}
	}

	private GraphTraversal<Vertex, Vertex> buildNode(GraphTraversalSource graphTraversalSource, UUID nodeId, NodeConfigItem nodeConfigItem, NodeDataPersist model, GraphTraversal<Vertex, Vertex> item) {
		if (item == null) {
			item = graphTraversalSource.addV(nodeConfigItem.getCode());
		} else {
			item.addV(nodeConfigItem.getCode());
		}
		item.property(NodeData._id, model.getId());
		item.property(NodeData._label, nodeConfigItem.getCode());
		item.property(NodeData._x, model.getX());
		item.property(NodeData._y, model.getY());
		item.property(NodeData._name, model.getName());

		if (model.getProperties() != null && nodeConfigItem.getConfigEntity() != null && nodeConfigItem.getConfigEntity().getFields() != null) {
			item = this.applyExtraProperties(nodeConfigItem.getConfigEntity().getFields(), model.getProperties(), item);
		}
		return item;
	}

	private <T> GraphTraversal<T, T> applyExtraProperties(List<FieldDefinitionEntity> fieldDefinitions, Map<String, Object> properties, GraphTraversal<T, T> item) {
		if (fieldDefinitions == null || properties == null) return item;
		Map<String, FieldDefinitionEntity> fieldDefinitionEntityByCode = fieldDefinitions.stream().collect(Collectors.toMap(x -> x.getCode(), x -> x));
		List<Map.Entry<String, List<String>>> validationErrors = new ArrayList<>();
		for (Map.Entry<String, Object> prop : properties.entrySet()) {
			FieldDefinitionEntity fieldEntity = fieldDefinitionEntityByCode.getOrDefault(prop.getKey(), null);
			if (fieldEntity != null) {
				switch (fieldEntity.getType()) {
					case String:
						try {
							if (prop.getValue() == null) {
								item.property(prop.getKey(), (Map<String, Double>) null);
							} else {
								item.property(prop.getKey(), String.class.cast(prop.getValue()));
							}
						} catch (Exception e) {
							throw e;
						}
						break;
					case Date:
						if (prop.getValue() == null) {
							item.property(prop.getKey(), (Date) null);
						} else {
							ZonedDateTime zonedDateTime = ZonedDateTime.parse(String.class.cast(prop.getValue()));
							item.property(prop.getKey(), new Date(zonedDateTime.toInstant().toEpochMilli()));
						}
						break;
					case Double:
						if (prop.getValue() == null) {
							item.property(prop.getKey(), (Double) null);
						} else {
							if (prop.getValue().getClass().equals(Integer.class)) item.property(prop.getKey(), Double.valueOf(Integer.class.cast(prop.getValue())));
							else item.property(prop.getKey(), Double.class.cast(prop.getValue()));
						}
						break;
					case Integer:
						if (prop.getValue() == null) {
							item.property(prop.getKey(), (Integer) null);
						} else {
							item.property(prop.getKey(), Integer.class.cast(prop.getValue()));
						}
						break;
					default:
						throw new MyApplicationException("invalid type " + fieldEntity.getType());
				}
			}
		}
		return item;
	}

	@Override
	public void persistEdge(UUID nodeId, UUID edgeId, EdgeDataPersist model) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting dataset").And("model", model));

		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.edgeAffiliation(edgeId)), Permission.EditEdgeData);
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.nodeAffiliation(nodeId)), Permission.EditNode);
		EdgeConfigItem edgeConfigItem = this.edgeConfigService.getConfig(edgeId);
		NodeConfigItem nodeConfigItem = this.nodeConfigService.getConfig(nodeId);

		GraphTraversalSource graphTraversalSource = gremlinFactory.getGraphTraversalSource();
		Transaction tx = null;
		try {
			tx = this.gremlinFactory.openTransaction(graphTraversalSource);
			GraphTraversal<Edge, Edge> data = this.buildEdge(graphTraversalSource, edgeId, nodeConfigItem, edgeConfigItem, model, null, null);

			Edge v = data.next();
			this.gremlinFactory.commitTransaction(tx);
		} catch (Exception ex) {
			this.gremlinFactory.rollbackTransaction(tx);
			throw ex;
		}
	}

	@Override
	public void persistEdges(UUID nodeId, UUID edgeId, List<EdgeDataPersist> models) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting dataset").And("models", models));
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.edgeAffiliation(edgeId)), Permission.EditEdgeData);
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.nodeAffiliation(nodeId)), Permission.EditNode);

		EdgeConfigItem edgeConfigItem = this.edgeConfigService.getConfig(edgeId);
		NodeConfigItem nodeConfigItem = this.nodeConfigService.getConfig(nodeId);
		GraphTraversalSource graphTraversalSource = gremlinFactory.getGraphTraversalSource();
		Transaction tx = null;
		try {
			tx = this.gremlinFactory.openTransaction(graphTraversalSource);
			GraphTraversal<Edge, Edge> data = null;
			int itemsCount = 0;
			Map<String, Vertex> vertexMap = this.getVertexMap(graphTraversalSource, nodeConfigItem, models);
			for (EdgeDataPersist model : models) {
				data = this.buildEdge(graphTraversalSource, edgeId, nodeConfigItem, edgeConfigItem, model, vertexMap, data);
				itemsCount++;
				if (itemsCount > this.graphProperties.getEdgeImportBatchSize()) {
					data.iterate();
					data = null;
					itemsCount = 0;
				}
			}
			if (data != null) data.iterate();
			this.gremlinFactory.commitTransaction(tx);
		} catch (Exception ex) {
			this.gremlinFactory.rollbackTransaction(tx);
			throw ex;
		}
	}

	private GraphTraversal<Edge, Edge> buildEdge(GraphTraversalSource graphTraversalSource, UUID edgeId, NodeConfigItem nodeConfigItem, EdgeConfigItem edgeConfigItem, EdgeDataPersist model, Map<String, Vertex> vertexMap, GraphTraversal<Edge, Edge> item) {
		if (vertexMap == null) vertexMap = new HashMap<>();
		if (item == null) {
			item = graphTraversalSource.addE(edgeConfigItem.getCode());
		} else {
			item.addE(edgeConfigItem.getCode());
		}
		item.property(EdgeData._id, UUID.randomUUID().toString().toLowerCase(Locale.ROOT));
		item.property(EdgeData._label, edgeConfigItem.getCode());
		item.property(EdgeData._weight, model.getWeight());
		item.property(EdgeData._sourceId, model.getSourceId());
		item.property(EdgeData._targetId, model.getTargetId());

		Vertex sourceNode = vertexMap.get(model.getSourceId());
		if (sourceNode == null) {
			sourceNode = graphTraversalSource.V().hasLabel(nodeConfigItem.getCode()).has(NodeData._id, model.getSourceId()).next();
			vertexMap.put(model.getSourceId(), sourceNode);

		}
		Vertex destinationNode = vertexMap.get(model.getTargetId());
		if (destinationNode == null) {
			destinationNode = graphTraversalSource.V().hasLabel(nodeConfigItem.getCode()).has(NodeData._id, model.getTargetId()).next();
			vertexMap.put(model.getTargetId(), destinationNode);
		}
		item.from(sourceNode).to(destinationNode);

		if (model.getProperties() != null && edgeConfigItem.getConfigEntity() != null && edgeConfigItem.getConfigEntity().getFields() != null) {
			item = this.applyExtraProperties(edgeConfigItem.getConfigEntity().getFields(), model.getProperties(), item);
		}
		return item;
	}

	private Map<String, Vertex> getVertexMap(GraphTraversalSource graphTraversalSource, NodeConfigItem nodeConfigItem, List<EdgeDataPersist> models) {
		Map<String, Vertex> map = new HashMap<>();

		for (EdgeDataPersist model : models) {
			if (!map.containsKey(model.getTargetId())) {
				Vertex node = graphTraversalSource.V().hasLabel(nodeConfigItem.getCode()).has(NodeData._id, model.getTargetId()).next();
				map.put(model.getTargetId(), node);
			}
			if (!map.containsKey(model.getSourceId())) {
				Vertex node = graphTraversalSource.V().hasLabel(nodeConfigItem.getCode()).has(NodeData._id, model.getSourceId()).next();
				map.put(model.getSourceId(), node);
			}
		}
		return map;
	}



	@Override
	public GraphData getGraphData(GraphDataLookup lookup) {
		FieldSet fieldSet = new BaseFieldSet();
		if (lookup.getProject() != null){
			FieldSet edgesFields = lookup.getProject().extractPrefixed(this.conventionService.asPrefix(GraphData._edges));
			FieldSet nodesFields = lookup.getProject().extractPrefixed(this.conventionService.asPrefix(GraphData._nodes));
			fieldSet = new BaseFieldSet(nodesFields.getFields());
			if (!edgesFields.isEmpty()){
				fieldSet = fieldSet.merge(new BaseFieldSet(this.conventionService.withPrefix(NodeData._edges, edgesFields.getFields())));
			}
		}

		NodeDataQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated);
		List<NodeDataEntity> data = query.collectAs(fieldSet);
		GraphDataEntity graph = GraphDataEntity.buildFromEdges(data);
		if (lookup.getProject() != null && lookup.getProject().hasField(GraphDataEntity._size)) graph.setSize(query.count());
		GraphData models = this.builderFactory.builder(GraphDataBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(lookup.getProject(), graph);
		return models;
	}




	public void Test() {
		GraphTraversalSource g = gremlinFactory.getGraphTraversalSource();
		Boolean supportsTransactions = g.getGraph().features().graph().supportsTransactions();
		Transaction tx = null;
		if (supportsTransactions) {
			tx = g.tx();
			tx.open();
		}
		try {
			//Map<String, Vertex> vertexMap = this.importVertexs(g, "C:\\Users\\sgiannopoulos\\Desktop\\CORDIS_0078K_nodes\\CORDIS_50_nodes.csv", "CORDIS_Node");
			//this.importEdges(g, "C:\\Users\\sgiannopoulos\\Desktop\\CORDIS_0078K_nodes\\CORDIS_50_edges.csv", new HashMap<>(), "CORDIS_Edge");

//			Object marko = g.addV("person").property(T.id.name(), "1").property("surname","surname1").property("name","marko").id().next();
//			Object stephen = g.addV("person").property(T.id.name(), "2").property("surname","surname2").property("name","stephen").id().next();
//			Edge aa = g.V().has("name","marko").addE("knows").to(g.V().has("name","stephen")).property("type", "friend").property("weight",0.75).next();
			//var results = g.V().hasLabel("CORDIS_Node").range(0, 2).valueMap("title", "x", "y", "id").toList();
//			var results11 = g.V().hasLabel("CORDIS_Node").project("id")
//							.by("id", __.constant("")).
//					toList();
//
//			List<Map<String, Object>> nodes = g.V().hasLabel("CORDIS_Node")
//					.project(Node._id, Node._x ,Node._y, "title")
////					.by(__.identity())
//					.by(__.coalesce(__.values("id"), __.constant("")))
//					.by(__.coalesce(__.values("x"), __.constant("")))
//					.by(__.coalesce(__.values("y"), __.constant("")))
//					.by(__.coalesce(__.values("title"), __.constant(""))).next(1000);
//
//			List<String> nodeIds = nodes.stream().map(x-> x.getOrDefault("id", "0").toString()).collect(Collectors.toList());
//			List<Map<String, Object>> edges = g.E().hasLabel("CORDIS_Edge")
//					.filter(__.or(__.inV().has("id", P.within(nodeIds)), __.outV().has("id", P.within(nodeIds))))
//					.project("weight", gr.cite.intelcomp.graphexplorer.model.Edge._sourceId, gr.cite.intelcomp.graphexplorer.model.Edge._targetId)
//					.by(__.coalesce(__.values("weight"), __.constant("weight")))
//					.by(__.outV().project(Node._id).by(__.coalesce(__.values("id"), __.constant(""))))
//					.by(__.inV().project(Node._id).by(__.coalesce(__.values("id"), __.constant(""))))
//					.next(10000);
//			List<gr.cite.intelcomp.graphexplorer.model.Edge> res = new EdgeBuilder(new ConventionServiceImpl(null)).build(new BaseFieldSet().ensure(gr.cite.intelcomp.graphexplorer.model.Edge._targetId)
//					.ensure(gr.cite.intelcomp.graphexplorer.model.Edge._targetId)
//					.ensure(gr.cite.intelcomp.graphexplorer.model.Edge._sourceId)
//					.ensure("weight"), edges);
//			List<gr.cite.intelcomp.graphexplorer.model.Node> res2 = new NodeBuilder(new ConventionServiceImpl(null)).build(new BaseFieldSet()
//					.ensure(Node._id)
//					.ensure(Node._x)
//					.ensure(Node._y)
//					.ensure("title"), nodes);
//		query = query.order().by(__.inE(lookup.getEdgeNames().toArray(new String[lookup.getEdgeNames().size()])).count(), Order.desc).limit(lookup.getMaxNodes());
//		String queryText = GroovyTranslator.of("g").translate(query.asAdmin().getBytecode());

//		var datas1 = query.limit(lookup.getMaxNodes()).bothE().
//				project("from", "edge", "to").
//				by(__.outV()).
//				by(__.coalesce(__.values(gr.cite.intelcomp.graphexplorer.model.Edge._weight), __.constant(0))).
//				by(__.inV()).local(
//						__.union(__.select("edge").unfold(),
//								__.project("from").by(__.select("from")).unfold(),
//								__.project("to").by(__.select("to")).unfold()).fold()).toList();


//		var aa = g.V().has("id", "8569").emit().repeat(__.both().dedup()).toSet();

			//		GraphTraversalSource g = gremlinFactory.getGraphTraversalSource();
//		GraphTraversal<Vertex, Vertex> query = g.V().hasLabel(P.within(lookup.getNodeNames()));
//		query = this.applyDimensionQuery(lookup, query);
//		query = query.order().by(NodeDataEntity._label, Order.asc).limit(lookup.getMaxNodes());

//		List<Map<String, Object>> datas = query.project(NodeDataEntity._id, GraphDataEntity._edges, NodeDataEntity._x, NodeDataEntity._y, NodeDataEntity._label)
//				.by(__.coalesce(__.values(NodeDataEntity._id), __.constant(0)))
//				.by(__.bothE(lookup.getEdgeNames().toArray(new String[lookup.getEdgeNames().size()])).filter(this.applyDimensionQuery(lookup, __.bothV()))
//						.project(EdgeDataEntity._weight, EdgeDataEntity._sourceId, EdgeDataEntity._targetId)
//						.by(__.coalesce(__.values(EdgeDataEntity._weight), __.constant(0)))
//						.by(__.outV().values(NodeDataEntity._id))
//						.by(__.inV().values(NodeDataEntity._id))
//						.fold())
//				.by(__.coalesce(__.values(NodeDataEntity._x), __.constant(0)))
//				.by(__.coalesce(__.values(NodeDataEntity._y), __.constant(0)))
//				.by(__.coalesce(__.values(NodeDataEntity._label), __.constant(""))).toList();
//		GraphDataEntity graph = GraphDataEntity.buildByNodesWithNestedEdges(datas);
//		GraphData model = this.builderFactory.builder(GraphDataBuilder.class).build(new BaseFieldSet()
//						.ensure(GraphData._nodes + "." + NodeData._id)
//						.ensure(GraphData._nodes + "." + NodeData._x)
//						.ensure(GraphData._nodes + "." + NodeData._y)
//						.ensure(GraphData._nodes + "." + NodeData._label)
//						.ensure(GraphData._edges + "." + EdgeData._sourceId)
//						.ensure(GraphData._edges + "." + EdgeData._targetId)
//						.ensure(GraphData._edges + "." + EdgeData._weight)
//				, graph);
			if (supportsTransactions) tx.commit();
		} catch (Exception ex) {
			if (supportsTransactions) tx.rollback();
			throw ex;
		}


	}


//	private <V> GraphTraversal<V, Vertex> applyDimensionQuery(GraphDataLookup lookup, GraphTraversal<V, Vertex> query) {
//		if (lookup.getX1() != null && lookup.getX2() != null && lookup.getY1() != null && lookup.getY2() != null) {
//			query = query.filter(__.and(__.has(NodeDataEntity._x, P.gte(lookup.getX1())), __.has(NodeDataEntity._x, P.lte(lookup.getX2())),
//					__.has(NodeDataEntity._y, P.lte(lookup.getY1())), __.has(NodeDataEntity._y, P.gte(lookup.getY2()))));
//		}
//		return query;
//	}
}
