package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.EdgeDataEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.NodeDataEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.model.NodeData;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigService;
import gr.cite.intelcomp.graphexplorer.service.gremlin.query.types.DoubleCompare;
import gr.cite.intelcomp.graphexplorer.service.gremlin.common.GremlinFactory;
import gr.cite.intelcomp.graphexplorer.service.gremlin.query.GremlinQueryBase;
import gr.cite.intelcomp.graphexplorer.service.gremlin.query.GremlinVertexQueryBase;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("gremlinNodeDataQuery")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NodeDataQuery extends GremlinVertexQueryBase<NodeDataEntity> {

	private Collection<UUID> edgeIds;
	private Collection<UUID> nodeIds;
	private String like;
	private Collection<String> ids;
	private Collection<String> excludedIds;

	private Collection<DoubleCompare> x;
	
	private Collection<DoubleCompare> y;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	public NodeDataQuery like(String value) {
		this.like = value;
		return this;
	}

	public NodeDataQuery ids(String value) {
		this.ids = List.of(value);
		return this;
	}

	public NodeDataQuery ids(String... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public NodeDataQuery ids(Collection<String> values) {
		this.ids = values;
		return this;
	}


	public NodeDataQuery x(DoubleCompare x) {
		this.x = List.of(x);
		return this;
	}

	public NodeDataQuery x(DoubleCompare... x) {
		this.x = Arrays.asList(x);
		return this;
	}

	public NodeDataQuery x(Collection<DoubleCompare> x) {
		this.x = x;
		return this;
	}

	public NodeDataQuery y(DoubleCompare y) {
		this.y = List.of(y);
		return this;
	}

	public NodeDataQuery y(DoubleCompare... y) {
		this.y = Arrays.asList(y);
		return this;
	}

	public NodeDataQuery y(Collection<DoubleCompare> y) {
		this.y = y;
		return this;
	}

	public NodeDataQuery excludedIds(Collection<String> values) {
		this.excludedIds = values;
		return this;
	}

	public NodeDataQuery excludedIds(String value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public NodeDataQuery excludedIds(String... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public NodeDataQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}
	public NodeDataQuery nodeIds(UUID value) {
		this.nodeIds = List.of(value);
		this.updateNodeConfigItems();
		return this;
	}

	public NodeDataQuery nodeIds(UUID... value) {
		this.nodeIds = Arrays.asList(value);
		this.updateNodeConfigItems();
		return this;
	}

	public NodeDataQuery nodeIds(Collection<UUID> values) {
		this.nodeIds = values;
		this.updateNodeConfigItems();
		return this;
	}
	public NodeDataQuery edgeIds(UUID value) {
		this.edgeIds = List.of(value);
		this.updateEdgeConfigItems();
		return this;
	}

	public NodeDataQuery edgeIds(UUID... value) {
		this.edgeIds = Arrays.asList(value);
		this.updateEdgeConfigItems();
		return this;
	}

	public NodeDataQuery edgeIds(Collection<UUID> values) {
		this.edgeIds = values;
		this.updateEdgeConfigItems();
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;
	private final AuthorizationContentResolver authorizationContentResolver;
	private final NodeConfigService nodeConfigService;
	private final EdgeConfigService edgeConfigService;
	private final QueryFactory queryFactory;
	private List<EdgeConfigItem> edgeConfigItems;
	private List<NodeConfigItem> nodeConfigItems;
	public NodeDataQuery(
			UserScope userScope,
			AuthorizationService authService,
			GremlinFactory gremlinFactory,
			AuthorizationContentResolver authorizationContentResolver,
			NodeConfigService nodeConfigService,
			EdgeConfigService edgeConfigService,
			QueryFactory queryFactory, 
			ConventionService conventionService) {
		super(gremlinFactory, conventionService);
		this.userScope = userScope;
		this.authService = authService;
		this.authorizationContentResolver = authorizationContentResolver;
		this.nodeConfigService = nodeConfigService;
		this.edgeConfigService = edgeConfigService;
		this.queryFactory = queryFactory;
	}
	
	private void updateEdgeConfigItems() {
		List<EdgeConfigItem> items = new ArrayList<>();
		if (this.edgeIds == null || this.edgeIds.isEmpty()) {
			items = null;
		} else {
			for (UUID EdgeId : this.edgeIds) items.add(this.edgeConfigService.getConfig(EdgeId));
		}
		this.edgeConfigItems = items;
	}

	private void updateNodeConfigItems() {
		List<NodeConfigItem> items = new ArrayList<>();
		if (this.nodeIds == null || this.nodeIds.isEmpty()) {
			items = null;
		} else {
			for (UUID nodeId : this.nodeIds) items.add(this.nodeConfigService.getConfig(nodeId));
		}
		this.nodeConfigItems = items;
	}

	@Override
	protected String[] getLabels() {
		if (this.nodeConfigItems != null && !this.nodeConfigItems.isEmpty()) {
				List<String> nodeNames = new ArrayList<>();
				for (NodeConfigItem nodeConfigItems : this.nodeConfigItems) nodeNames.add(nodeConfigItems.getCode());
				return nodeNames.toArray(new String[nodeNames.size()]);
		} else {
			throw new RuntimeException("Node Label Not Set");
		}
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.excludedIds)|| this.isEmpty(this.x)|| this.isEmpty(this.y);
	}

	@Override
	protected GraphTraversal<Vertex, Vertex> applyAuthZ() {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseNodeData)) return null;
		List<UUID> allowedNodeIds = null;
		if (this.authorize.contains(AuthorizationFlags.Affiliated)) allowedNodeIds = this.authorizationContentResolver.affiliatedNodes(Permission.BrowseNodeData);

		if (this.nodeIds != null) {
			if (allowedNodeIds == null) {
				this.nodeIds(new ArrayList<>());
			} else {
				Set<UUID> result = this.nodeIds.stream().distinct().filter(allowedNodeIds::contains).collect(Collectors.toSet());
				this.nodeIds(result);
			}
		} else {
			this.nodeIds(allowedNodeIds);
		}

		return null;
	}
	
	@Override
	protected GraphTraversal<Vertex, Vertex> applyFilters() {
		List<GraphTraversal<?, ?>> predicates = new ArrayList<>();
		
		if (this.ids != null) {
			predicates.add(__.has(NodeDataEntity._id, P.within(this.ids)));
		}
		if (this.excludedIds != null) {
			predicates.add(__.has(NodeDataEntity._id, P.without(this.ids)));
		}
		if (this.like != null && !this.like.isBlank()) {
			predicates.add(__.has(NodeDataEntity._name, this.like)); //TODO
		}
		if (this.x != null) {
			for (DoubleCompare item: this.x ) {
				predicates.add(__.has(NodeDataEntity._x, this.getCompare(item.getCompareType(), item.getValue())));

			}
		}
		if (this.y != null) {
			for (DoubleCompare item: this.y ) {
				predicates.add(__.has(NodeDataEntity._y, this.getCompare(item.getCompareType(), item.getValue())));

			}
		}
		if (predicates.size() > 0) {
			return __.and(predicates.toArray(new GraphTraversal<?, ?>[predicates.size()]));
		} else {
			return null;
		}
	}
	

	@Override
	protected List<NodeDataEntity> convert(List<Map<String, Object>> datas, FieldSet projection, Set<String> columns) {
		List<NodeDataEntity> results = new ArrayList();
		FieldSet edgeFieldSet = projection.extractPrefixed(this.conventionService.asPrefix(NodeData._edges));
		EdgeDataQuery edgeDataQuery = this.queryFactory.query(EdgeDataQuery.class);
		List<FieldDefinitionEntity> fieldDefinitionEntities = new ArrayList<>();
		if (this.nodeConfigItems != null) {
			for (NodeConfigItem configItem : this.nodeConfigItems) {
				if (configItem.getConfigEntity() == null || configItem.getConfigEntity().getFields() == null) continue;
				for (FieldDefinitionEntity prop : configItem.getConfigEntity().getFields()) {
					fieldDefinitionEntities.add(prop);
				}
			}
		}
		for (Map<String, Object> objectMap : datas) {
			NodeDataEntity item = this.convert(objectMap, edgeFieldSet, columns, edgeDataQuery, fieldDefinitionEntities);
			if (item != null) results.add(item);
		}
		return results;
	}
	
	protected NodeDataEntity convert(Map<String, Object> map, FieldSet edgeFieldSet, Set<String> columns, EdgeDataQuery edgeDataQuery, List<FieldDefinitionEntity> fieldDefinitionEntities) {
		NodeDataEntity item = new NodeDataEntity();
		item.setId(GremlinQueryBase.convertSafe(map, columns, NodeDataEntity._id, String.class));
		item.setLabel(GremlinQueryBase.convertSafe(map, columns, NodeDataEntity._label, String.class));
		item.setX(GremlinQueryBase.convertSafe(map, columns, NodeDataEntity._x, Double.class));
		item.setY(GremlinQueryBase.convertSafe(map, columns, NodeDataEntity._y, Double.class));
		item.setName(GremlinQueryBase.convertSafe(map, columns, NodeDataEntity._name, String.class));
		item.setEdges(this.convertNested(map, edgeFieldSet, columns, edgeDataQuery, NodeDataEntity._edges));
		Map<String, java.lang.Object> properties = new HashMap<>();
		for (FieldDefinitionEntity prop : fieldDefinitionEntities) {
			java.lang.Object alreadyParsedPropertyValue = properties.getOrDefault(prop.getCode(), null);
			if (alreadyParsedPropertyValue == null && map.containsKey(prop.getCode())) {
				switch (prop.getType()) {
					case String:
						properties.put(prop.getCode(), GremlinQueryBase.convertSafe(map, columns, prop.getCode(), String.class));
						break;
					case Date:
						properties.put(prop.getCode(), GremlinQueryBase.convertSafe(map, columns, prop.getCode(), Date.class));
						break;
					case Double:
						properties.put(prop.getCode(), GremlinQueryBase.convertSafe(map, columns, prop.getCode(), Double.class));
						break;
					case Integer:
						properties.put(prop.getCode(), GremlinQueryBase.convertSafe(map, columns, prop.getCode(), Integer.class));
						break;
					default:
						throw new MyApplicationException("invalid type " + prop.getType());
				}
			}
		}
		if (!properties.isEmpty()) item.setProperties(properties);
		
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(NodeData._id)) return NodeDataEntity._id;
		else if (item.match(NodeData._label)) return NodeDataEntity._label;
		else if (item.match(NodeData._x)) return NodeDataEntity._x;
		else if (item.match(NodeData._y)) return NodeDataEntity._y;
		else if (item.match(NodeData._name)) return NodeDataEntity._name;
		else if (item.prefix(NodeData._edges)) return NodeDataEntity._edges;
		else if (this.containsFieldDefinitionEntity(item.getField())) return item.getField();
		else return null;
	}

	@Override
	protected FieldSet fullDataFieldSet() {
		FieldSet baseFieldSet = new BaseFieldSet().ensure(NodeData._id).ensure(NodeData._x, NodeData._y).ensure(NodeData._name).ensure(NodeData._edges + "." + EdgeDataEntity._sourceId)
				.ensure(NodeData._edges + "." + EdgeDataEntity._weight)
				.ensure(NodeData._edges + "." + EdgeDataEntity._targetId);
		if (this.nodeConfigItems != null) {
			for (NodeConfigItem nodeConfigItem : this.nodeConfigItems) {
				if (nodeConfigItem.getConfigEntity() == null || nodeConfigItem.getConfigEntity().getFields() == null) continue;
				for (FieldDefinitionEntity prop : nodeConfigItem.getConfigEntity().getFields()) {
					baseFieldSet.ensure(prop.getCode());
				}
			}
		}
		return baseFieldSet;
	}

	@Override
	protected GraphTraversal<?, ?> fieldProjection(FieldResolver item, FieldSet nestedFields) {
		if (item.match(NodeData._id)) return __.coalesce(__.values(NodeDataEntity._id), __.constant(""));
		else if (item.match(NodeData._label)) return __.coalesce(__.values(NodeDataEntity._label), __.constant(""));
		else if (item.match(NodeData._x)) return __.coalesce(__.values(NodeDataEntity._x), __.constant(0));
		else if (item.match(NodeData._y)) return __.coalesce(__.values(NodeDataEntity._y), __.constant(0));
		else if (item.match(NodeData._name)) return __.coalesce(__.values(NodeDataEntity._name), __.constant(""));
		else if (item.prefix(NodeData._edges)) return this.buildSelectSubQuery(this.queryFactory.query(EdgeDataQuery.class).nodeSubQuery(this).edgeIds(this.edgeIds), nestedFields, __.bothE()).fold();
		else if (this.containsFieldDefinitionEntity(item.getField())) return __.coalesce(__.values(item.getField()), __.constant("")); //TODO: Maybe Type
		else return null;
	}

	private FieldDefinitionEntity getFieldDefinitionEntity(String field) {
		FieldDefinitionEntity fieldEntity = this.getOrDefaultFieldDefinitionEntity(field, null);
		if (fieldEntity == null) throw new MyApplicationException("invalid field " + field);
		return fieldEntity;
	}

	private boolean containsFieldDefinitionEntity(String field) {
		FieldDefinitionEntity fieldEntity = this.getOrDefaultFieldDefinitionEntity(field, null);
		return fieldEntity != null;
	}

	private FieldDefinitionEntity getOrDefaultFieldDefinitionEntity(String field, FieldDefinitionEntity defaultValue) {
		if (this.nodeConfigItems == null) return defaultValue;
		NodeConfigItem configItem = this.nodeConfigItems.stream().filter(x -> x.getConfigEntity() != null && x.getConfigEntity().getFields() != null &&
						x.getConfigEntity().getFields().stream().filter(z-> z.getCode().equals(field)).count() > 0
				).findFirst().orElse(null);
		if (configItem == null) return defaultValue;
		FieldDefinitionEntity fieldDefinitionEntity = configItem.getConfigEntity().getFields().stream().filter(z-> z.getCode().equals(field)).findFirst().orElse(null);
		if (fieldDefinitionEntity == null) return defaultValue;
		return fieldDefinitionEntity;
	}

}
