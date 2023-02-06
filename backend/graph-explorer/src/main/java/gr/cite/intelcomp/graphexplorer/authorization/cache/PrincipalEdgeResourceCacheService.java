package gr.cite.intelcomp.graphexplorer.authorization.cache;

import gr.cite.intelcomp.graphexplorer.authorization.EdgeRolesResource;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class PrincipalEdgeResourceCacheService extends CacheService<PrincipalEdgeResourceCacheService.PrincipalEdgeResourceCacheValue> {

	public static class PrincipalEdgeResourceCacheValue {

		public PrincipalEdgeResourceCacheValue() {
		}

		public PrincipalEdgeResourceCacheValue(UUID userId, UUID entityId, String entity, EdgeRolesResource EdgeRolesResource) {
			this.userId = userId;
			this.entityId = entityId;
			this.entity = entity;
			this.edgeRolesResource = EdgeRolesResource;
		}

		private UUID userId;
		private UUID entityId;
		private String entity;
		private EdgeRolesResource edgeRolesResource;

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

		public EdgeRolesResource getEdgeRolesResource() {
			return edgeRolesResource;
		}

		public void setEdgeRolesResource(EdgeRolesResource edgeRolesResource) {
			this.edgeRolesResource = edgeRolesResource;
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
	public PrincipalEdgeResourceCacheService(PrincipalEdgeResourceCacheOptions options, ConventionService conventionService) {
		super(options);
		this.conventionService = conventionService;
	}

	@Override
	protected Class<PrincipalEdgeResourceCacheValue> valueClass() {
		return PrincipalEdgeResourceCacheValue.class;
	}

	@Override
	public String keyOf(PrincipalEdgeResourceCacheValue value) {
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
