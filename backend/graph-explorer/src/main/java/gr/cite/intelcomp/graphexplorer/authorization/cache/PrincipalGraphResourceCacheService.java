package gr.cite.intelcomp.graphexplorer.authorization.cache;

import gr.cite.intelcomp.graphexplorer.authorization.GraphRolesResource;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class PrincipalGraphResourceCacheService extends CacheService<PrincipalGraphResourceCacheService.PrincipalGraphResourceCacheValue> {

	public static class PrincipalGraphResourceCacheValue {

		public PrincipalGraphResourceCacheValue() {
		}

		public PrincipalGraphResourceCacheValue(UUID userId, UUID entityId, String entity, GraphRolesResource graphRolesResource) {
			this.userId = userId;
			this.entityId = entityId;
			this.entity = entity;
			this.graphRolesResource = graphRolesResource;
		}

		private UUID userId;
		private UUID entityId;
		private String entity;
		private GraphRolesResource graphRolesResource;

		public UUID getUserId() {
			return userId;
		}

		public void setUserId(UUID userId) {
			this.userId = userId;
		}

		public String getEntity() {
			return entity;
		}

		public void setEntity(String entity) {
			this.entity = entity;
		}

		public GraphRolesResource getGraphRolesResource() {
			return graphRolesResource;
		}

		public void setGraphRolesResource(GraphRolesResource graphRolesResource) {
			this.graphRolesResource = graphRolesResource;
		}

		public UUID getEntityId() {
			return entityId;
		}

		public void setEntityId(UUID entityId) {
			this.entityId = entityId;
		}
	}

	private final ConventionService conventionService;

	@Autowired
	public PrincipalGraphResourceCacheService(PrincipalGraphResourceCacheOptions options, ConventionService conventionService) {
		super(options);
		this.conventionService = conventionService;
	}

	@Override
	protected Class<PrincipalGraphResourceCacheValue> valueClass() {
		return PrincipalGraphResourceCacheValue.class;
	}

	@Override
	public String keyOf(PrincipalGraphResourceCacheValue value) {
		return this.buildKey(value.getUserId(), value.getEntityId(), value.getEntity());
	}

	public String buildKey(UUID userId, UUID entityId, String entity) {
		return this.generateKey(new HashMap<>() {{
			put("$user_id$", userId.toString().toLowerCase(Locale.ROOT));
			put("$entity_id$", entityId.toString().toLowerCase(Locale.ROOT));
			put("$entity_type$", entity.toLowerCase(Locale.ROOT));
		}});
	}
}
