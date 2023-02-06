package gr.cite.intelcomp.graphexplorer.service.edge;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.EdgeConfigEntity;
import gr.cite.intelcomp.graphexplorer.data.EdgeEntity;
import gr.cite.intelcomp.graphexplorer.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.persistence.EntityManager;
import java.util.UUID;

@Service
@RequestScope
public class EdgeConfigServiceImpl implements EdgeConfigService {
	private final EntityManager entityManager;
	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private final AuthorizationService authorizationService;
	private final ErrorThesaurusProperties errors;
	private final MessageSource messageSource;
	private final JsonHandlingService jsonHandlingService;
	private final EdgeConfigCacheService edgeConfigCacheService;

	public EdgeConfigServiceImpl(
			EntityManager entityManager,
			QueryFactory queryFactory,
			BuilderFactory builderFactory,
			AuthorizationService authorizationService,
			ErrorThesaurusProperties errors,
			MessageSource messageSource,
			JsonHandlingService jsonHandlingService, 
			EdgeConfigCacheService edgeConfigCacheService) {
		this.entityManager = entityManager;
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
		this.authorizationService = authorizationService;
		this.errors = errors;
		this.messageSource = messageSource;
		this.jsonHandlingService = jsonHandlingService;
		this.edgeConfigCacheService = edgeConfigCacheService;
	}

	@Override
	public EdgeConfigItem getConfig(@NotNull UUID edgeId) {
		EdgeConfigCacheService.EdgeConfigCacheValue nodeConfigItem = this.edgeConfigCacheService.lookup(this.edgeConfigCacheService.buildKey(edgeId));
		if (nodeConfigItem == null) {

			EdgeEntity edgeEntity = this.entityManager.find(EdgeEntity.class, edgeId);
			if (edgeEntity == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{edgeId, EdgeEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			EdgeConfigEntity configEntity = this.jsonHandlingService.fromJsonSafe(EdgeConfigEntity.class, edgeEntity.getConfig());
			if (configEntity == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{edgeId, EdgeConfigEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			
			
			nodeConfigItem = new EdgeConfigCacheService.EdgeConfigCacheValue(edgeId, new EdgeConfigItem(edgeId, edgeEntity.getCode(), configEntity));
			this.edgeConfigCacheService.put(nodeConfigItem);
		}
		return nodeConfigItem.getConfig();
	}

	@Override
	public String ensurePropertyName(@NotNull String prop) {
		return prop.replace(".", "_dot_");
	}
}
