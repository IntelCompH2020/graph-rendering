package gr.cite.intelcomp.graphexplorer.elastic.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.GraphFieldType;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.elastic.data.NodeDataEntity;
import gr.cite.intelcomp.graphexplorer.model.NodeData;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigService;
import gr.cite.intelcomp.graphexplorer.service.elasticnode.ElasticNodeService;
import gr.cite.intelcomp.graphexplorer.service.gremlin.query.types.DoubleCompare;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.Aggregation.MetricAggregateType;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import gr.cite.tools.elastic.query.ElasticQuery;
import gr.cite.tools.exception.MyApplicationException;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component("elasticNodeDataQuery")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NodeDataQuery extends ElasticQuery<NodeDataEntity, String> {
	private Collection<UUID> nodeIds;
	private String like;
	private Collection<String> ids;
	private Collection<String> excludedIds;

	private Collection<DoubleCompare> x;

	private Collection<DoubleCompare> y;
	
	private List<NodeConfigItem> nodeConfigItems;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	private final ElasticNodeService elasticNodeService;
	private final AuthorizationService authService;
	private final AuthorizationContentResolver authorizationContentResolver;
	private final NodeConfigService nodeConfigService;

	public NodeDataQuery(
			ElasticsearchRestTemplate elasticsearchRestTemplate,
			ElasticNodeService elasticNodeService,
			AuthorizationService authService,
			AuthorizationContentResolver authorizationContentResolver,
			ElasticProperties elasticProperties,
			NodeConfigService nodeConfigService) {
		super(elasticsearchRestTemplate, elasticProperties);
		this.elasticNodeService = elasticNodeService;
		this.authService = authService;
		this.authorizationContentResolver = authorizationContentResolver;
		this.nodeConfigService = nodeConfigService;
	}
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

	@Override
	protected String[] getIndex() {
		if (this.nodeIds != null && !this.nodeIds.isEmpty()) {
			try {
				List<String> indexNames = new ArrayList<>();
				for (UUID nodeId : this.nodeIds) indexNames.add(this.elasticNodeService.getIndexName(nodeId));
				return indexNames.toArray(new String[indexNames.size()]);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return super.getIndex();
		}
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
	protected Class<NodeDataEntity> entityClass() {
		return NodeDataEntity.class;
	}


	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.nodeIds)|| this.isEmpty(this.excludedIds);
	}

	@Override
	protected QueryBuilder applyAuthZ() {
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
	protected QueryBuilder applyFilters() {
		List<QueryBuilder> predicates = new ArrayList<>();
		if (ids != null) {
			predicates.add(this.containsString(this.elasticFieldOf(NodeDataEntity.Fields.id), ids));
		}
		if (this.excludedIds != null) {
			predicates.add(this.not(this.containsString(this.elasticFieldOf(NodeDataEntity.Fields.id), excludedIds)));
		}
		if (this.like != null && !this.like.isBlank()) {
			predicates.add(this.like(this.elasticFieldsOf().add(NodeDataEntity.Fields.name), List.of(this.like))); 
		}
		if (this.x != null) {
			for (DoubleCompare item: this.x ) {
				predicates.add(this.valueCompareQuery(this.elasticFieldOf(NodeDataEntity.Fields.x), item.getValue(), item.getCompareType().toElasticCompare()));
			}
		}
		if (this.y != null) {
			for (DoubleCompare item: this.y ) {
				predicates.add(this.valueCompareQuery(this.elasticFieldOf(NodeDataEntity.Fields.y), item.getValue(), item.getCompareType().toElasticCompare()));
			}
		}

		if (predicates.size() > 0) {
			return this.and(predicates);
		} else {
			return null;
		}
	}


	@Override
	public NodeDataEntity convert(Map<String, Object> rawData, Set<String> columns) {
		NodeDataEntity mocDoc = new NodeDataEntity();
		if (columns.contains(NodeDataEntity.Fields.id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NodeDataEntity.Fields.id), String.class));
		if (columns.contains(NodeDataEntity.Fields.name)) mocDoc.setName(FieldBasedMapper.shallowSafeConversion(rawData.get(NodeDataEntity.Fields.name), String.class));
		if (columns.contains(NodeDataEntity.Fields.label)) mocDoc.setLabel(FieldBasedMapper.shallowSafeConversion(rawData.get(NodeDataEntity.Fields.label), String.class));
		if (columns.contains(NodeDataEntity.Fields.x)) mocDoc.setX(FieldBasedMapper.shallowSafeConversion(rawData.get(NodeDataEntity.Fields.x), Double.class));
		if (columns.contains(NodeDataEntity.Fields.y)) mocDoc.setY(FieldBasedMapper.shallowSafeConversion(rawData.get(NodeDataEntity.Fields.y), Double.class));

		ObjectMapper mapper = new ObjectMapper();
		if (this.nodeConfigItems != null) {
			Map<String, Object> properties = new HashMap<>();
			for (NodeConfigItem nodeConfigItem : this.nodeConfigItems) {
				if (nodeConfigItem.getConfigEntity() == null || nodeConfigItem.getConfigEntity().getFields() == null) continue;
				
				for (FieldDefinitionEntity prop : nodeConfigItem.getConfigEntity().getFields()) {
					Object alreadyParsedPropertyValue = properties.getOrDefault(prop.getCode(), null);
					if (alreadyParsedPropertyValue == null && rawData.containsKey(prop.getCode())) {
						switch (prop.getType()) {
							case String:
								properties.put(prop.getCode(), FieldBasedMapper.shallowSafeConversion(rawData.get(prop.getCode()), String.class));
								break;
							case Date:
								properties.put(prop.getCode(), FieldBasedMapper.shallowSafeConversion(rawData.get(prop.getCode()), Date.class));
								break;
							case Double:
								properties.put(prop.getCode(), FieldBasedMapper.shallowSafeConversion(rawData.get(prop.getCode()), Double.class));
								break;
							case Integer:
								properties.put(prop.getCode(), FieldBasedMapper.shallowSafeConversion(rawData.get(prop.getCode()), Integer.class));
								break;
							default:
								throw new MyApplicationException("invalid type " + prop.getType());
						}
					}
				}
			}
			mocDoc.setProperties(properties);
		}
		return mocDoc;
	}

	@Override
	protected ElasticField fieldNameOf(FieldResolver item) {
		if (item.match(NodeData._id)) return this.elasticFieldOf(NodeDataEntity.Fields.id);
		else if (item.match(NodeData._name)) return this.elasticFieldOf(NodeDataEntity.Fields.name);
		else if (item.match(NodeData._label)) return this.elasticFieldOf(NodeDataEntity.Fields.label);
		else if (item.match(NodeData._x)) return this.elasticFieldOf(NodeDataEntity.Fields.x);
		else if (item.match(NodeData._y)) return this.elasticFieldOf(NodeDataEntity.Fields.y);
		else if (this.containsFieldEntity(item.getField())) return this.elasticFieldOf(this.getFieldEntity(item.getField()).getCode()).disableInfer(true);
		else return null;
	}

	@Override
	protected Boolean supportsMetricAggregate(MetricAggregateType metricAggregateType, FieldResolver resolver) {
		FieldDefinitionEntity fieldEntity = this.getOrDefaultFieldEntity(resolver.getField(), null);
		if (resolver.match(NodeData._x)) return true;
		else if (resolver.match(NodeData._y)) return true;
		return fieldEntity != null && (fieldEntity.getType() == GraphFieldType.Double || fieldEntity.getType() == GraphFieldType.Integer);
	}

	@Override
	protected String toKey(String key) {
		return key;
	}

	@Override
	protected ElasticField getKeyField() {
		return this.elasticFieldOf(NodeDataEntity.Fields.id);
	}

	@Override
	protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
		return null;
	}

	private FieldDefinitionEntity getFieldEntity(String field) {
		FieldDefinitionEntity fieldEntity = this.getOrDefaultFieldEntity(field, null);
		if (fieldEntity == null) throw new MyApplicationException("invalid field " + field);
		return fieldEntity;
	}

	private boolean containsFieldEntity(String field) {
		FieldDefinitionEntity fieldEntity = this.getOrDefaultFieldEntity(field, null);
		return fieldEntity != null;
	}

	private FieldDefinitionEntity getOrDefaultFieldEntity(String field, FieldDefinitionEntity defaultValue) {
		if (this.nodeConfigItems == null) return defaultValue;
		FieldDefinitionEntity fieldDefinitionEntity = this.nodeConfigItems.stream().map(x-> x.getConfigEntity()).map(x-> x.getFields()).flatMap(Collection::stream).filter(x -> x.getCode().equalsIgnoreCase(field)).findFirst().orElse(null);
		if (fieldDefinitionEntity == null) return defaultValue;
		return fieldDefinitionEntity;
	}


}

