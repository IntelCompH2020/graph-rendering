package gr.cite.intelcomp.graphexplorer.service.edge;

import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.event.NodeTouchedEvent;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class EdgeConfigCacheService extends CacheService<EdgeConfigCacheService.EdgeConfigCacheValue> {

	public static class EdgeConfigCacheValue {

		public EdgeConfigCacheValue() {
		}

		public EdgeConfigCacheValue(UUID id, EdgeConfigItem config) {
			this.id = id;
			this.config = config;
		}

		private UUID id;

		private EdgeConfigItem config;

		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public EdgeConfigItem getConfig() {
			return config;
		}

		public void setConfig(EdgeConfigItem config) {
			this.config = config;
		}
	}

	private final ConventionService conventionService;

	@Autowired
	public EdgeConfigCacheService(EdgeConfigCacheOptions options, ConventionService conventionService) {
		super(options);
		this.conventionService = conventionService;
	}

	@EventListener
	public void handleNodeTouchedEvent(NodeTouchedEvent event) {
		if (event.getId() != null) this.evict(this.buildKey(event.getId()));
	}

	@Override
	protected Class<EdgeConfigCacheValue> valueClass() {
		return EdgeConfigCacheValue.class;
	}

	@Override
	public String keyOf(EdgeConfigCacheValue value) {
		return this.buildKey(value.getId());
	}

	public String buildKey(UUID nodeId) {
		return this.generateKey(new HashMap<>() {{
			put("$edge$", nodeId.toString().toLowerCase(Locale.ROOT));
		}});
	}
}
