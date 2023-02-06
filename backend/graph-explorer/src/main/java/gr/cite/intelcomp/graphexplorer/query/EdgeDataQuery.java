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
import gr.cite.intelcomp.graphexplorer.model.EdgeData;
import gr.cite.intelcomp.graphexplorer.model.NodeData;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigService;
import gr.cite.intelcomp.graphexplorer.service.gremlin.common.GremlinFactory;
import gr.cite.intelcomp.graphexplorer.service.gremlin.query.GremlinEdgeQueryBase;
import gr.cite.intelcomp.graphexplorer.service.gremlin.query.GremlinQueryBase;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("gremlinEdgeDataQuery")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EdgeDataQuery extends GremlinEdgeQueryBase<EdgeDataEntity> {

	private Collection<UUID> edgeIds;
	private NodeDataQuery nodeSubQuery;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	public EdgeDataQuery nodeSubQuery(NodeDataQuery nodeSubQuery) {
		this.nodeSubQuery = nodeSubQuery;
		return this;
	}

	public EdgeDataQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}
	public EdgeDataQuery edgeIds(UUID value) {
		this.edgeIds = List.of(value);
		this.updateEdgeConfigItems();
		return this;
	}

	public EdgeDataQuery edgeIds(UUID... value) {
		this.edgeIds = Arrays.asList(value);
		this.updateEdgeConfigItems();
		return this;
	}

	public EdgeDataQuery edgeIds(Collection<UUID> values) {
		this.edgeIds = values;
		this.updateEdgeConfigItems();
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;
	private final AuthorizationContentResolver authorizationContentResolver;
	private final EdgeConfigService edgeConfigService;
	private List<EdgeConfigItem> edgeConfigItems;

	public EdgeDataQuery(
			UserScope userScope,
			AuthorizationService authService,
			GremlinFactory gremlinFactory,
			AuthorizationContentResolver authorizationContentResolver, 
			EdgeConfigService edgeConfigService,
			ConventionService conventionService) {
		super(gremlinFactory, conventionService);
		this.userScope = userScope;
		this.authService = authService;
		this.authorizationContentResolver = authorizationContentResolver;
		this.edgeConfigService = edgeConfigService;
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

	@Override
	protected String[] getLabels() {
		if (this.edgeConfigItems != null && !this.edgeConfigItems.isEmpty()) {
				List<String> EdgeNames = new ArrayList<>();
				for (EdgeConfigItem EdgeConfigItems : this.edgeConfigItems) EdgeNames.add(EdgeConfigItems.getCode());
				return EdgeNames.toArray(new String[EdgeNames.size()]);
		} else {
			throw new RuntimeException("Edge Label Not Set");
		}
	}
	@Override
	protected Boolean isFalseQuery() {
		return (this.nodeSubQuery != null && this.nodeSubQuery.isFalseQuery());
	}
	
	@Override
	protected GraphTraversal<Edge, Edge> applyAuthZ() {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseEdgeData)) return null;
		List<UUID> allowedEdgeIds = null;
		if (this.authorize.contains(AuthorizationFlags.Affiliated)) allowedEdgeIds = this.authorizationContentResolver.affiliatedEdges(Permission.BrowseEdgeData);

		if (this.edgeIds != null) {
			if (allowedEdgeIds == null) {
				this.edgeIds(new ArrayList<>());
			} else {
				Set<UUID> result = this.edgeIds.stream().distinct().filter(allowedEdgeIds::contains).collect(Collectors.toSet());
				this.edgeIds(result);
			}
		} else {
			this.edgeIds(allowedEdgeIds);
		}
		
		return null;
	}
	
	@Override
	protected GraphTraversal<Edge, Edge> applyFilters() {
		List<GraphTraversal<?, ?>> predicates = new ArrayList<>();
		if (this.nodeSubQuery != null){
			GraphTraversal<Edge, Vertex>  q = __.bothV();
			this.applySubQuery(this.nodeSubQuery, q);
			predicates.add(q);
		}
		if (predicates.size() > 0) {
			return __.and(predicates.toArray(new GraphTraversal<?, ?>[predicates.size()]));
		} else {
			return null;
		}
	}
	
	@Override
	protected List<EdgeDataEntity> convert(List<Map<String, Object>> datas, FieldSet projection, Set<String> columns) {
		List<EdgeDataEntity> results = new ArrayList();
		List<FieldDefinitionEntity> fieldDefinitionEntities = new ArrayList<>();
		if (this.edgeConfigItems != null) {
			for (EdgeConfigItem configItem : this.edgeConfigItems) {
				if (configItem.getConfigEntity() == null || configItem.getConfigEntity().getFields() == null) continue;
				for (FieldDefinitionEntity prop : configItem.getConfigEntity().getFields()) {
					fieldDefinitionEntities.add(prop);
				}
			}
		}
		for (Map<String, Object> objectMap : datas) {
			EdgeDataEntity item = this.convert(objectMap, columns, fieldDefinitionEntities);
			if (item != null) results.add(item);
		}
		return results;
	}

	protected EdgeDataEntity convert(Map<String, Object> map, Set<String> columns, List<FieldDefinitionEntity> fieldDefinitionEntities) {
		EdgeDataEntity item = new EdgeDataEntity();
		item.setId(GremlinQueryBase.convertSafe(map, columns, EdgeDataEntity._id, String.class));
		item.setLabel(GremlinQueryBase.convertSafe(map, columns, EdgeDataEntity._label, String.class));
		item.setWeight(GremlinQueryBase.convertSafe(map, columns, EdgeDataEntity._weight, Double.class));
		item.setSourceId(GremlinQueryBase.convertSafe(map, columns, EdgeDataEntity._sourceId, String.class));
		item.setTargetId(GremlinQueryBase.convertSafe(map, columns, EdgeDataEntity._targetId, String.class));
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
		if (item.match(NodeData._id)) return EdgeDataEntity._id;
		else if (item.match(NodeData._label)) return EdgeDataEntity._label;
		else if (item.match(EdgeData._weight)) return EdgeDataEntity._weight;
		else if (item.match(EdgeData._targetId)) return EdgeDataEntity._targetId;
		else if (item.match(EdgeData._sourceId)) return EdgeDataEntity._sourceId;
		else if (this.containsFieldDefinitionEntity(item.getField())) return item.getField();
		else return null;
	}

	@Override
	protected FieldSet fullDataFieldSet() {
		FieldSet baseFieldSet = new BaseFieldSet().ensure(EdgeData._weight).ensure(EdgeData._targetId, EdgeData._sourceId);
		if (this.edgeConfigItems != null) {
			for (EdgeConfigItem edgeConfigItem : this.edgeConfigItems) {
				if (edgeConfigItem.getConfigEntity() == null || edgeConfigItem.getConfigEntity().getFields() == null) continue;
				for (FieldDefinitionEntity prop : edgeConfigItem.getConfigEntity().getFields()) {
					baseFieldSet.ensure(prop.getCode());
				}
			}
		}
		return baseFieldSet;
	}

	@Override
	protected GraphTraversal<?, ?> fieldProjection(FieldResolver item, FieldSet nestedFields) {
		if (item.match(NodeData._id)) return __.coalesce(__.values(EdgeDataEntity._id), __.constant(""));
		else if (item.match(NodeData._label)) return __.coalesce(__.values(EdgeDataEntity._label), __.constant(""));
		else if (item.match(EdgeData._weight)) return __.coalesce(__.values(EdgeDataEntity._weight), __.constant(0));
		else if (item.match(EdgeData._sourceId)) return __.coalesce(__.values(EdgeDataEntity._sourceId), __.constant(""));
		else if (item.match(EdgeData._targetId)) return __.coalesce(__.values(EdgeDataEntity._targetId), __.constant(""));
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
		if (this.edgeConfigItems == null) return defaultValue;
		EdgeConfigItem configItem = this.edgeConfigItems.stream().filter(x -> x.getConfigEntity() != null && x.getConfigEntity().getFields() != null &&
						x.getConfigEntity().getFields().stream().filter(z-> z.getCode().equals(field)).count() > 0
				).findFirst().orElse(null);
		if (configItem == null) return defaultValue;
		FieldDefinitionEntity fieldDefinitionEntity = configItem.getConfigEntity().getFields().stream().filter(z-> z.getCode().equals(field)).findFirst().orElse(null);
		if (fieldDefinitionEntity == null) return defaultValue;
		return fieldDefinitionEntity;
	}

}
