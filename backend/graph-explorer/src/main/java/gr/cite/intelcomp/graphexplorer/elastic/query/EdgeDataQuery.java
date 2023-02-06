package gr.cite.intelcomp.graphexplorer.elastic.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.GraphFieldType;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.elastic.data.EdgeDataEntity;
import gr.cite.intelcomp.graphexplorer.elastic.data.EdgeDataEntity;
import gr.cite.intelcomp.graphexplorer.model.EdgeData;
import gr.cite.intelcomp.graphexplorer.model.NodeData;
import gr.cite.intelcomp.graphexplorer.service.elasticedge.ElasticEdgeService;
import gr.cite.intelcomp.graphexplorer.service.gremlin.query.types.DoubleCompare;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigService;
import gr.cite.tools.data.query.FieldResolver;
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

@Component("elasticEdgeDataQuery")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EdgeDataQuery extends ElasticQuery<EdgeDataEntity, String> {
	private Collection<UUID> edgeIds;
	private Collection<String> ids;
	private Collection<String> excludedIds;
	private Collection<String> sourceTargetIds;

	private List<EdgeConfigItem> edgeConfigItems;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	private final ElasticEdgeService elasticEdgeService;
	private final AuthorizationService authService;
	private final AuthorizationContentResolver authorizationContentResolver;
	private final EdgeConfigService edgeConfigService;

	public EdgeDataQuery(
			ElasticsearchRestTemplate elasticsearchRestTemplate,
			ElasticEdgeService elasticEdgeService,
			AuthorizationService authService,
			AuthorizationContentResolver authorizationContentResolver,
			ElasticProperties elasticProperties,
			EdgeConfigService edgeConfigService) {
		super(elasticsearchRestTemplate, elasticProperties);
		this.elasticEdgeService = elasticEdgeService;
		this.authService = authService;
		this.authorizationContentResolver = authorizationContentResolver;
		this.edgeConfigService = edgeConfigService;
	}

	public EdgeDataQuery ids(String value) {
		this.ids = List.of(value);
		return this;
	}

	public EdgeDataQuery ids(String... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public EdgeDataQuery ids(Collection<String> values) {
		this.ids = values;
		return this;
	}

	public EdgeDataQuery excludedIds(Collection<String> values) {
		this.excludedIds = values;
		return this;
	}

	public EdgeDataQuery excludedIds(String value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public EdgeDataQuery excludedIds(String... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public EdgeDataQuery sourceTargetIds(String value) {
		this.sourceTargetIds = List.of(value);
		return this;
	}

	public EdgeDataQuery sourceTargetIds(String... value) {
		this.sourceTargetIds = Arrays.asList(value);
		return this;
	}

	public EdgeDataQuery sourceTargetIds(Collection<String> values) {
		this.sourceTargetIds = values;
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

	@Override
	protected String[] getIndex() {
		if (this.edgeIds != null && !this.edgeIds.isEmpty()) {
			try {
				List<String> indexNames = new ArrayList<>();
				for (UUID edgeId : this.edgeIds) indexNames.add(this.elasticEdgeService.getIndexName(edgeId));
				return indexNames.toArray(new String[indexNames.size()]);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return super.getIndex();
		}
	}

	private void updateEdgeConfigItems() {
		List<EdgeConfigItem> items = new ArrayList<>();
		if (this.edgeIds == null || this.edgeIds.isEmpty()) {
			items = null;
		} else {
			for (UUID edgeId : this.edgeIds) items.add(this.edgeConfigService.getConfig(edgeId));
		}
		this.edgeConfigItems = items;
	}

	@Override
	protected Class<EdgeDataEntity> entityClass() {
		return EdgeDataEntity.class;
	}


	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.edgeIds)|| this.isEmpty(this.excludedIds);
	}

	@Override
	protected QueryBuilder applyAuthZ() {
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
	protected QueryBuilder applyFilters() {
		List<QueryBuilder> predicates = new ArrayList<>();
		if (ids != null) {
			predicates.add(this.containsString(this.elasticFieldOf(EdgeDataEntity.Fields.id), ids));
		}
		if (this.excludedIds != null) {
			predicates.add(this.not(this.containsString(this.elasticFieldOf(EdgeDataEntity.Fields.id), excludedIds)));
		}

		if (sourceTargetIds != null) {
			predicates.add(this.and(this.containsString(this.elasticFieldOf(EdgeDataEntity.Fields.sourceId), sourceTargetIds),
					this.containsString(this.elasticFieldOf(EdgeDataEntity.Fields.targetId), sourceTargetIds)
					));
		}
		if (predicates.size() > 0) {
			return this.and(predicates);
		} else {
			return null;
		}
	}


	@Override
	public EdgeDataEntity convert(Map<String, Object> rawData, Set<String> columns) {
		EdgeDataEntity mocDoc = new EdgeDataEntity();
		if (columns.contains(EdgeDataEntity.Fields.id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(EdgeDataEntity.Fields.id), String.class));
		if (columns.contains(EdgeDataEntity.Fields.sourceId)) mocDoc.setSourceId(FieldBasedMapper.shallowSafeConversion(rawData.get(EdgeDataEntity.Fields.sourceId), String.class));
		if (columns.contains(EdgeDataEntity.Fields.label)) mocDoc.setLabel(FieldBasedMapper.shallowSafeConversion(rawData.get(EdgeDataEntity.Fields.label), String.class));
		if (columns.contains(EdgeDataEntity.Fields.targetId)) mocDoc.setTargetId(FieldBasedMapper.shallowSafeConversion(rawData.get(EdgeDataEntity.Fields.targetId), String.class));
		if (columns.contains(EdgeDataEntity.Fields.weight)) mocDoc.setWeight(FieldBasedMapper.shallowSafeConversion(rawData.get(EdgeDataEntity.Fields.weight), Double.class));

		ObjectMapper mapper = new ObjectMapper();
		if (this.edgeConfigItems != null) {
			Map<String, Object> properties = new HashMap<>();
			for (EdgeConfigItem edgeConfigItem : this.edgeConfigItems) {
				if (edgeConfigItem.getConfigEntity() == null || edgeConfigItem.getConfigEntity().getFields() == null) continue;
				
				for (FieldDefinitionEntity prop : edgeConfigItem.getConfigEntity().getFields()) {
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
		if (item.match(EdgeData._id)) return this.elasticFieldOf(EdgeDataEntity.Fields.id);
		else if (item.match(EdgeData._sourceId)) return this.elasticFieldOf(EdgeDataEntity.Fields.sourceId);
		else if (item.match(EdgeData._label)) return this.elasticFieldOf(EdgeDataEntity.Fields.label);
		else if (item.match(EdgeData._targetId)) return this.elasticFieldOf(EdgeDataEntity.Fields.targetId);
		else if (item.match(EdgeData._weight)) return this.elasticFieldOf(EdgeDataEntity.Fields.weight);
		else if (this.containsFieldEntity(item.getField())) return this.elasticFieldOf(this.getFieldEntity(item.getField()).getCode()).disableInfer(true);
		else return null;
	}

	@Override
	protected Boolean supportsMetricAggregate(MetricAggregateType metricAggregateType, FieldResolver resolver) {
		FieldDefinitionEntity fieldEntity = this.getOrDefaultFieldEntity(resolver.getField(), null);
		if (resolver.match(EdgeData._weight)) return true;
		return fieldEntity != null && (fieldEntity.getType() == GraphFieldType.Double || fieldEntity.getType() == GraphFieldType.Integer);
	}
	@Override
	protected String toKey(String key) {
		return key;
	}

	@Override
	protected ElasticField getKeyField() {
		return this.elasticFieldOf(EdgeDataEntity.Fields.id);
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
		if (this.edgeConfigItems == null) return defaultValue;
		FieldDefinitionEntity fieldDefinitionEntity = this.edgeConfigItems.stream().map(x-> x.getConfigEntity()).map(x-> x.getFields()).flatMap(Collection::stream).filter(x -> x.getCode().equalsIgnoreCase(field)).findFirst().orElse(null);
		if (fieldDefinitionEntity == null) return defaultValue;
		return fieldDefinitionEntity;
	}


}

