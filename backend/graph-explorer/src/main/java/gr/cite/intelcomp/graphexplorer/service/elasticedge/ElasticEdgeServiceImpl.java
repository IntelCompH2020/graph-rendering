package gr.cite.intelcomp.graphexplorer.service.elasticedge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.enums.GraphFieldType;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.config.elastic.AppElasticProperties;
import gr.cite.intelcomp.graphexplorer.data.EdgeEntity;
import gr.cite.intelcomp.graphexplorer.elastic.data.EdgeDataEntity;
import gr.cite.intelcomp.graphexplorer.errorcode.ErrorThesaurusProperties;
import gr.cite.intelcomp.graphexplorer.event.EventBroker;
import gr.cite.intelcomp.graphexplorer.model.deleter.EdgeDeleter;
import gr.cite.intelcomp.graphexplorer.query.EdgeQuery;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ElasticEdgeServiceImpl implements ElasticEdgeService {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ElasticEdgeServiceImpl.class));

	private final EntityManager entityManager;
	private final ElasticsearchRestTemplate elasticsearchRestTemplate;
	private final ObjectMapper mapper;

	private final EventBroker eventBroker;

	private final AuthorizationService authorizationService;

	private final BuilderFactory builderFactory;
	private final QueryFactory queryFactory;
	private final RestHighLevelClient restHighLevelClient;
	private final EdgeConfigService edgeConfigService;
	private final EdgeIndexCacheService edgeIndexCacheService;
	private final MessageSource messageSource;
	private final ErrorThesaurusProperties errors;
	private final JsonHandlingService jsonHandlingService;
	private final AppElasticProperties appElasticProperties;
	private final DeleterFactory deleterFactory;

	@Autowired
	public ElasticEdgeServiceImpl(EntityManager entityManager,
	                              ElasticsearchRestTemplate elasticsearchRestTemplate,
	                              EventBroker eventBroker,
	                              AuthorizationService authorizationService,
	                              BuilderFactory builderFactory,
	                              QueryFactory queryFactory,
	                              RestHighLevelClient restHighLevelClient,
	                              EdgeConfigService edgeConfigService, 
	                              EdgeIndexCacheService edgeIndexCacheService,
	                              ErrorThesaurusProperties errors,
	                              MessageSource messageSource,
	                              JsonHandlingService jsonHandlingService,
	                              AppElasticProperties appElasticProperties,
	                              DeleterFactory deleterFactory) {
		this.entityManager = entityManager;
		this.elasticsearchRestTemplate = elasticsearchRestTemplate;
		this.eventBroker = eventBroker;
		this.edgeConfigService = edgeConfigService;
		this.queryFactory = queryFactory;
		this.jsonHandlingService = jsonHandlingService;
		this.appElasticProperties = appElasticProperties;
		this.deleterFactory = deleterFactory;
		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new JavaTimeModule());
		this.authorizationService = authorizationService;
		this.builderFactory = builderFactory;
		this.restHighLevelClient = restHighLevelClient;
		this.edgeIndexCacheService = edgeIndexCacheService;
		this.errors = errors;
		this.messageSource = messageSource;
	}



	public String calculateIndexName(String code, boolean ensureUnique) throws IOException {
		logger.debug(new MapLogEntry("calculate index name").And("code", code).And("ensureUnique", ensureUnique));
		String index = this.appElasticProperties.getEdgeDataIndexNamePattern().replace(this.appElasticProperties.getEdgeCodeKey(), code.toLowerCase(Locale.ROOT));
		if (ensureUnique && restHighLevelClient.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT)) throw new MyApplicationException(this.errors.getEdgeAlreadyExists().getCode(), this.errors.getEdgeAlreadyExists().getMessage());
		return index;
	}

	public String getIndexName(UUID edgeId) throws IOException {
		logger.debug(new MapLogEntry("get index name").And("edgeId", edgeId));
		EdgeIndexCacheService.EdgeIndexCacheValue cacheValue = this.edgeIndexCacheService.lookup(this.edgeIndexCacheService.buildKey(edgeId));
		if (cacheValue == null) {
			EdgeEntity data = this.entityManager.find(EdgeEntity.class, edgeId);
			if (data == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{edgeId, EdgeEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			cacheValue = new EdgeIndexCacheService.EdgeIndexCacheValue(edgeId, this.calculateIndexName(data.getCode(), false));
			this.edgeIndexCacheService.put(cacheValue);
		}
		return cacheValue.getIndexName();
	}

	public String ensureIndex(UUID edgeId) throws IOException {
		String index = this.getIndexName(edgeId);
		Boolean exists = restHighLevelClient.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
		if (!exists) {
			EdgeConfigItem edgeConfigItem = this.edgeConfigService.getConfig(edgeId);
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder.startObject();
			{
				builder.startObject("properties");
				{
					this.addFieldToIndexTemplate(builder, EdgeDataEntity.Fields.id, GraphFieldType.String);
					this.addFieldToIndexTemplate(builder, EdgeDataEntity.Fields.sourceId, GraphFieldType.String);
					this.addFieldToIndexTemplate(builder, EdgeDataEntity.Fields.targetId, GraphFieldType.String);
					this.addFieldToIndexTemplate(builder, EdgeDataEntity.Fields.weight, GraphFieldType.Double);

					for (FieldDefinitionEntity prop : edgeConfigItem.getConfigEntity().getFields()) {
						this.addFieldToIndexTemplate(builder, this.edgeConfigService.ensurePropertyName(prop.getCode()), prop.getType());
					}
				}
				builder.endObject();
			}
			builder.endObject();

			Settings.Builder settingsBuilder = Settings.builder()
					.put("index.analysis.filter.english_stemmer.type", "stemmer")
					.put("index.analysis.filter.english_stemmer.language", "english")
					.put("index.analysis.filter.english_stop.type", "stop")
					.put("index.analysis.filter.english_stop.language", "english");
			if (this.appElasticProperties.isEnableIcuAnalysisPlugin()){
				settingsBuilder.putList("index.analysis.analyzer.icu_analyzer_text.filter", "icu_folding", "english_stop", "english_stemmer")
						.put("index.analysis.analyzer.icu_analyzer_text.tokenizer", "icu_tokenizer");
			} else {
				settingsBuilder.putList("index.analysis.analyzer.icu_analyzer_text.filter", "english_stop", "english_stemmer")
						.put("index.analysis.analyzer.icu_analyzer_text.type", "standard");
			}
					
			CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(new CreateIndexRequest(index).mapping(builder).settings(settingsBuilder), RequestOptions.DEFAULT);
		}
		return index;
	}

	private void addFieldToIndexTemplate(XContentBuilder builder, String name, GraphFieldType type) throws IOException {
		FieldType typeString = FieldType.Auto;

		switch (type) {
			case String:
				typeString = FieldType.Keyword;
				break;
			case Date:
				typeString = FieldType.Date;
				break;
			case Double:
				typeString = FieldType.Double;
				break;
			case Integer:
				typeString = FieldType.Integer;
				break;
			default:
				throw new MyApplicationException("invalid type " + type);
		}

		builder.startObject(name);
		{
			builder.field("type", typeString.getMappedName());
		}
		builder.endObject();
	}

	public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException, IOException {
		logger.debug("deleting dataset: {}", id);

		this.authorizationService.authorizeForce(Permission.DeleteEdge);
		EdgeEntity data = this.queryFactory.query(EdgeQuery.class).ids(id).first();
		if (data == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{id, EdgeEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

		String index = this.getIndexName(id);

		this.deleterFactory.deleter(EdgeDeleter.class).deleteAndSaveByIds(List.of(id));
		this.restHighLevelClient.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
	}
}
