package gr.cite.intelcomp.graphexplorer.authorization.cache;

import gr.cite.intelcomp.graphexplorer.authorization.NodeRolesResource;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class PrincipalNodeResourceCacheService extends CacheService<PrincipalNodeResourceCacheService.PrincipalNodeResourceCacheValue> {

	public static class PrincipalNodeResourceCacheValue {

		public PrincipalNodeResourceCacheValue() {
		}

		public PrincipalNodeResourceCacheValue(UUID userId, UUID entityId, String entity, NodeRolesResource nodeRolesResource) {
			this.userId = userId;
			this.entityId = entityId;
			this.entity = entity;
			this.nodeRolesResource = nodeRolesResource;
		}

		private UUID userId;
		private UUID entityId;
		private String entity;
		private NodeRolesResource nodeRolesResource;

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

		public NodeRolesResource getNodeRolesResource() {
			return nodeRolesResource;
		}

		public void setNodeRolesResource(NodeRolesResource nodeRolesResource) {
			this.nodeRolesResource = nodeRolesResource;
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
	public PrincipalNodeResourceCacheService(PrincipalNodeResourceCacheOptions options, ConventionService conventionService) {
		super(options);
		this.conventionService = conventionService;
	}

	@Override
	protected Class<PrincipalNodeResourceCacheValue> valueClass() {
		return PrincipalNodeResourceCacheValue.class;
	}

	@Override
	public String keyOf(PrincipalNodeResourceCacheValue value) {
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
