package gr.cite.intelcomp.graphexplorer.service.node;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.NodeConfigEntity;
import gr.cite.intelcomp.graphexplorer.data.NodeEntity;
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
public class NodeConfigServiceImpl implements NodeConfigService {
	private final EntityManager entityManager;
	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private final AuthorizationService authorizationService;
	private final ErrorThesaurusProperties errors;
	private final MessageSource messageSource;
	private final JsonHandlingService jsonHandlingService;
	private final NodeConfigCacheService nodeConfigCacheService;

	public NodeConfigServiceImpl(
			EntityManager entityManager,
			QueryFactory queryFactory,
			BuilderFactory builderFactory,
			AuthorizationService authorizationService,
			ErrorThesaurusProperties errors,
			MessageSource messageSource,
			JsonHandlingService jsonHandlingService, 
			NodeConfigCacheService nodeConfigCacheService) {
		this.entityManager = entityManager;
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
		this.authorizationService = authorizationService;
		this.errors = errors;
		this.messageSource = messageSource;
		this.jsonHandlingService = jsonHandlingService;
		this.nodeConfigCacheService = nodeConfigCacheService;
	}

	@Override
	public NodeConfigItem getConfig(@NotNull UUID nodeId) {
		NodeConfigCacheService.NodeConfigCacheValue nodeConfigItem = this.nodeConfigCacheService.lookup(this.nodeConfigCacheService.buildKey(nodeId));
		if (nodeConfigItem == null) {

			NodeEntity nodeEntity = this.entityManager.find(NodeEntity.class, nodeId);
			if (nodeEntity == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{nodeId, NodeEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			NodeConfigEntity configEntity = this.jsonHandlingService.fromJsonSafe(NodeConfigEntity.class, nodeEntity.getConfig());
			if (configEntity == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{nodeId, NodeConfigEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
			
			
			nodeConfigItem = new NodeConfigCacheService.NodeConfigCacheValue(nodeId, new NodeConfigItem(nodeId, nodeEntity.getCode(), configEntity));
			this.nodeConfigCacheService.put(nodeConfigItem);
		}
		return nodeConfigItem.getConfig();
	}

	@Override
	public String ensurePropertyName(@NotNull String prop) {
		return prop.replace(".", "_dot_");
	}
}
