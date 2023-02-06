package gr.cite.intelcomp.graphexplorer.authorization.cache;

import gr.cite.tools.cache.CacheService;
import gr.cite.tools.cipher.CipherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AffiliatedGraphsCacheService extends CacheService<AffiliatedGraphsCacheService.AffiliatedGraphsCacheValue> {

	public static class AffiliatedGraphsCacheValue {

		public AffiliatedGraphsCacheValue() {
		}


		public AffiliatedGraphsCacheValue(UUID userId, List<UUID> graphIds, List<String> permissions) {
			this.userId = userId;
			this.graphIds = graphIds;
			this.permissions = permissions;
		}

		private UUID userId;

		private List<UUID> graphIds;
		private List<String> permissions;

		public UUID getUserId() {
			return userId;
		}

		public void setUserId(UUID userId) {
			this.userId = userId;
		}

		public List<UUID> getGraphIds() {
			return graphIds;
		}

		public void setGraphIds(List<UUID> graphIds) {
			this.graphIds = graphIds;
		}

		public List<String> getPermissions() {
			return permissions;
		}

		public void setPermissions(List<String> permissions) {
			this.permissions = permissions;
		}
	}

	private final CipherService cipherService;

	@Autowired
	public AffiliatedGraphsCacheService(AffiliatedGraphsCacheOptions options, CipherService cipherService) {
		super(options);
		this.cipherService = cipherService;
	}

	@Override
	protected Class<AffiliatedGraphsCacheValue> valueClass() {
		return AffiliatedGraphsCacheValue.class;
	}

	@Override
	public String keyOf(AffiliatedGraphsCacheValue value) {
		return this.buildKey(value.getUserId(), value.getPermissions());
	}

	public String buildKey(UUID userId, List<String> permissions) {
		String permissionsHash = "";
		if (permissions != null && permissions.size() > 0) {
			try {
				permissionsHash = this.cipherService.toSha1(String.join("", permissions.stream().sorted().map(x -> x.toLowerCase(Locale.ROOT)).collect(Collectors.toList())));
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}
		String finalPermissionsHash = permissionsHash;
		return this.generateKey(new HashMap<>() {{
			put("$user_id$", userId.toString().toLowerCase(Locale.ROOT));
			put("$permissions$", finalPermissionsHash);
		}});
	}
}
