package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.GraphEntity;
import gr.cite.intelcomp.graphexplorer.model.Graph;
import gr.cite.intelcomp.graphexplorer.model.GraphAccess;
import gr.cite.intelcomp.graphexplorer.model.GraphEdge;
import gr.cite.intelcomp.graphexplorer.model.GraphNode;
import gr.cite.intelcomp.graphexplorer.query.GraphAccessQuery;
import gr.cite.intelcomp.graphexplorer.query.GraphEdgeQuery;
import gr.cite.intelcomp.graphexplorer.query.GraphNodeQuery;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GraphBuilder extends BaseBuilder<Graph, GraphEntity> {

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private final JsonHandlingService jsonHandlingService;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public GraphBuilder(
			ConventionService conventionService,
			QueryFactory queryFactory, BuilderFactory builderFactory, JsonHandlingService jsonHandlingService) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(GraphBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
		this.jsonHandlingService = jsonHandlingService;
	}

	public GraphBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<Graph> build(FieldSet fields, List<GraphEntity> datas) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(datas).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || datas == null || fields.isEmpty()) return new ArrayList<>();

		List<Graph> models = new ArrayList<>();

		FieldSet graphAccessesFields = fields.extractPrefixed(this.asPrefix(Graph._graphAccesses));
		Map<UUID, List<GraphAccess>> graphAccessesMap = this.collectGraphAccesses(graphAccessesFields, datas);

		FieldSet graphNodesFields = fields.extractPrefixed(this.asPrefix(Graph._graphNodes));
		Map<UUID, List<GraphNode>> graphNodesMap = this.collectGraphNodes(graphNodesFields, datas);

		FieldSet graphEdgesFields = fields.extractPrefixed(this.asPrefix(Graph._graphEdges));
		Map<UUID, List<GraphEdge>> graphEdgesMap = this.collectGraphEdges(graphEdgesFields, datas);
		
		for (GraphEntity d : datas) {
			Graph m = new Graph();
			if (fields.hasField(this.asIndexer(Graph._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(Graph._name))) m.setName(d.getName());
			if (fields.hasField(this.asIndexer(Graph._description))) m.setDescription(d.getDescription());
			if (fields.hasField(this.asIndexer(Graph._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(Graph._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(Graph._isActive))) m.setIsActive(d.getIsActive());
			if (fields.hasField(this.asIndexer(Graph._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
			if (!graphAccessesFields.isEmpty() && graphAccessesMap != null && graphAccessesMap.containsKey(d.getId())) m.setGraphAccesses(graphAccessesMap.get(d.getId()));
			if (!graphNodesFields.isEmpty() && graphNodesMap != null && graphNodesMap.containsKey(d.getId())) m.setGraphNodes(graphNodesMap.get(d.getId()));
			if (!graphEdgesFields.isEmpty() && graphEdgesMap != null && graphEdgesMap.containsKey(d.getId())) m.setGraphEdges(graphEdgesMap.get(d.getId()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
		return models;
	}

	private Map<UUID, List<GraphAccess>> collectGraphAccesses(FieldSet fields, List<GraphEntity> datas) throws MyApplicationException {
		if (fields.isEmpty() || datas.isEmpty()) return null;
		this.logger.debug("checking related - {}", GraphAccess.class.getSimpleName());

		Map<UUID, List<GraphAccess>> itemMap = null;
		FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(GraphAccess._graph, Graph._id));
		GraphAccessQuery query = this.queryFactory.query(GraphAccessQuery.class).authorize(this.authorize).graphIds(datas.stream().map(x -> x.getId()).distinct().collect(Collectors.toList()));
		itemMap = this.builderFactory.builder(GraphAccessBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getGraph().getId());

		if (!fields.hasField(this.asIndexer(GraphAccess._graph, Graph._id))) {
			itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getGraph() != null).map(x -> {
				x.getGraph().setId(null);
				return x;
			}).collect(Collectors.toList());
		}
		return itemMap;
	}

	private Map<UUID, List<GraphNode>> collectGraphNodes(FieldSet fields, List<GraphEntity> datas) throws MyApplicationException {
		if (fields.isEmpty() || datas.isEmpty()) return null;
		this.logger.debug("checking related - {}", GraphNode.class.getSimpleName());

		Map<UUID, List<GraphNode>> itemMap = null;
		FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(GraphNode._graph, Graph._id));
		GraphNodeQuery query = this.queryFactory.query(GraphNodeQuery.class).authorize(this.authorize).graphIds(datas.stream().map(x -> x.getId()).distinct().collect(Collectors.toList()));
		itemMap = this.builderFactory.builder(GraphNodeBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getGraph().getId());

		if (!fields.hasField(this.asIndexer(GraphNode._graph, Graph._id))) {
			itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getGraph() != null).map(x -> {
				x.getGraph().setId(null);
				return x;
			}).collect(Collectors.toList());
		}
		return itemMap;
	}



	private Map<UUID, List<GraphEdge>> collectGraphEdges(FieldSet fields, List<GraphEntity> datas) throws MyApplicationException {
		if (fields.isEmpty() || datas.isEmpty()) return null;
		this.logger.debug("checking related - {}", GraphEdge.class.getSimpleName());

		Map<UUID, List<GraphEdge>> itemMap = null;
		FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(GraphEdge._graph, Graph._id));
		GraphEdgeQuery query = this.queryFactory.query(GraphEdgeQuery.class).authorize(this.authorize).graphIds(datas.stream().map(x -> x.getId()).distinct().collect(Collectors.toList()));
		itemMap = this.builderFactory.builder(GraphEdgeBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getGraph().getId());

		if (!fields.hasField(this.asIndexer(GraphEdge._graph, Graph._id))) {
			itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getGraph() != null).map(x -> {
				x.getGraph().setId(null);
				return x;
			}).collect(Collectors.toList());
		}
		return itemMap;
	}
}
