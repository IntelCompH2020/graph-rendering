package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.GraphNodeEntity;
import gr.cite.intelcomp.graphexplorer.model.Graph;
import gr.cite.intelcomp.graphexplorer.model.GraphNode;
import gr.cite.intelcomp.graphexplorer.model.Node;
import gr.cite.intelcomp.graphexplorer.model.Node;
import gr.cite.intelcomp.graphexplorer.query.GraphQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeQuery;
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
public class GraphNodeBuilder extends BaseBuilder<GraphNode, GraphNodeEntity> {

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public GraphNodeBuilder(
			ConventionService conventionService,
			QueryFactory queryFactory, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(GraphNodeBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
	}

	public GraphNodeBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<GraphNode> build(FieldSet fields, List<GraphNodeEntity> data) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

		FieldSet graphFields = fields.extractPrefixed(this.asPrefix(GraphNode._graph));
		Map<UUID, Graph> graphItemsMap = this.collectGraphs(graphFields, data);

		FieldSet nodeFields = fields.extractPrefixed(this.asPrefix(GraphNode._node));
		Map<UUID, Node> nodeItemsMap = this.collectNodes(nodeFields, data);

		List<GraphNode> models = new ArrayList<>();

		for (GraphNodeEntity d : data) {
			GraphNode m = new GraphNode();
			if (fields.hasField(this.asIndexer(GraphNode._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(GraphNode._isActive))) m.setIsActive(d.getIsActive());
			if (fields.hasField(this.asIndexer(GraphNode._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(GraphNode._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(GraphNode._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
			if (!graphFields.isEmpty() && graphItemsMap != null && graphItemsMap.containsKey(d.getGraphId())) m.setGraph(graphItemsMap.get(d.getGraphId()));
			if (!nodeFields.isEmpty() && nodeItemsMap != null && nodeItemsMap.containsKey(d.getNodeId())) m.setNode(nodeItemsMap.get(d.getNodeId()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
		return models;
	}

	private Map<UUID, Node> collectNodes(FieldSet fields, List<GraphNodeEntity> data) throws MyApplicationException {
		if (fields.isEmpty() || data.isEmpty()) return null;
		this.logger.debug("checking related - {}", Node.class.getSimpleName());

		Map<UUID, Node> itemMap;
		if (!fields.hasOtherField(this.asIndexer(Node._id))) {
			itemMap = this.asEmpty(
					data.stream().map(x -> x.getNodeId()).distinct().collect(Collectors.toList()),
					x -> {
						Node item = new Node();
						item.setId(x);
						return item;
					},
					Node::getId);
		} else {
			FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Node._id);
			NodeQuery q = this.queryFactory.query(NodeQuery.class).authorize(this.authorize).ids(data.stream().map(x -> x.getNodeId()).distinct().collect(Collectors.toList()));
			itemMap = this.builderFactory.builder(NodeBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Node::getId);
		}
		if (!fields.hasField(Node._id)) {
			itemMap.values().stream().filter(Objects::nonNull).peek(x -> x.setId(null)).collect(Collectors.toList());
		}

		return itemMap;
	}

	private Map<UUID, Graph> collectGraphs(FieldSet fields, List<GraphNodeEntity> data) throws MyApplicationException {
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
