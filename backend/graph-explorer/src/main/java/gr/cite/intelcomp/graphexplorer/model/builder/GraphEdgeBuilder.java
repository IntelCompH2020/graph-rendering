package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.GraphEdgeEntity;
import gr.cite.intelcomp.graphexplorer.model.Graph;
import gr.cite.intelcomp.graphexplorer.model.GraphEdge;
import gr.cite.intelcomp.graphexplorer.model.Edge;
import gr.cite.intelcomp.graphexplorer.query.GraphQuery;
import gr.cite.intelcomp.graphexplorer.query.EdgeQuery;
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
public class GraphEdgeBuilder extends BaseBuilder<GraphEdge, GraphEdgeEntity> {

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public GraphEdgeBuilder(
			ConventionService conventionService,
			QueryFactory queryFactory, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(GraphEdgeBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
	}

	public GraphEdgeBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<GraphEdge> build(FieldSet fields, List<GraphEdgeEntity> data) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

		FieldSet graphFields = fields.extractPrefixed(this.asPrefix(GraphEdge._graph));
		Map<UUID, Graph> graphItemsMap = this.collectGraphs(graphFields, data);

		FieldSet edgeFields = fields.extractPrefixed(this.asPrefix(GraphEdge._edge));
		Map<UUID, Edge> edgeItemsMap = this.collectEdges(edgeFields, data);

		List<GraphEdge> models = new ArrayList<>();

		for (GraphEdgeEntity d : data) {
			GraphEdge m = new GraphEdge();
			if (fields.hasField(this.asIndexer(GraphEdge._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(GraphEdge._isActive))) m.setIsActive(d.getIsActive());
			if (fields.hasField(this.asIndexer(GraphEdge._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(GraphEdge._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(GraphEdge._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
			if (!graphFields.isEmpty() && graphItemsMap != null && graphItemsMap.containsKey(d.getGraphId())) m.setGraph(graphItemsMap.get(d.getGraphId()));
			if (!edgeFields.isEmpty() && edgeItemsMap != null && edgeItemsMap.containsKey(d.getEdgeId())) m.setEdge(edgeItemsMap.get(d.getEdgeId()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
		return models;
	}

	private Map<UUID, Edge> collectEdges(FieldSet fields, List<GraphEdgeEntity> data) throws MyApplicationException {
		if (fields.isEmpty() || data.isEmpty()) return null;
		this.logger.debug("checking related - {}", Edge.class.getSimpleName());

		Map<UUID, Edge> itemMap;
		if (!fields.hasOtherField(this.asIndexer(Edge._id))) {
			itemMap = this.asEmpty(
					data.stream().map(x -> x.getEdgeId()).distinct().collect(Collectors.toList()),
					x -> {
						Edge item = new Edge();
						item.setId(x);
						return item;
					},
					Edge::getId);
		} else {
			FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Edge._id);
			EdgeQuery q = this.queryFactory.query(EdgeQuery.class).authorize(this.authorize).ids(data.stream().map(x -> x.getEdgeId()).distinct().collect(Collectors.toList()));
			itemMap = this.builderFactory.builder(EdgeBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Edge::getId);
		}
		if (!fields.hasField(Edge._id)) {
			itemMap.values().stream().filter(Objects::nonNull).peek(x -> x.setId(null)).collect(Collectors.toList());
		}

		return itemMap;
	}

	private Map<UUID, Graph> collectGraphs(FieldSet fields, List<GraphEdgeEntity> data) throws MyApplicationException {
		if (fields.isEmpty() || data.isEmpty()) return null;
		this.logger.debug("checking related - {}", Graph.class.getSimpleName());

		Map<UUID, Graph> itemMap;
		if (!fields.hasOtherField(this.asIndexer(Graph._id))) {
			itemMap = this.asEmpty(
					data.stream().map(x -> x.getGraphId()).distinct().collect(Collectors.toList()),
					x -> {
						Graph item = new Graph();
						item.setId(x);
						return item;
					},
					Graph::getId);
		} else {
			FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Graph._id);
			GraphQuery q = this.queryFactory.query(GraphQuery.class).authorize(this.authorize).ids(data.stream().map(x -> x.getGraphId()).distinct().collect(Collectors.toList()));
			itemMap = this.builderFactory.builder(GraphBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Graph::getId);
		}
		if (!fields.hasField(Graph._id)) {
			itemMap.values().stream().filter(Objects::nonNull).peek(x -> x.setId(null)).collect(Collectors.toList());
		}

		return itemMap;
	}

}
