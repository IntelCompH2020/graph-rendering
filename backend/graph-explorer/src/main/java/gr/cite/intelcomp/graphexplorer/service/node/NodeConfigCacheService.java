package gr.cite.intelcomp.graphexplorer.service.node;

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
public class NodeConfigCacheService extends CacheService<NodeConfigCacheService.NodeConfigCacheValue> {

	public static class NodeConfigCacheValue {

		public NodeConfigCacheValue() {
		}

		public NodeConfigCacheValue(UUID id, NodeConfigItem config) {
			this.id = id;
			this.config = config;
		}

		private UUID id;

		private NodeConfigItem config;

		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public NodeConfigItem getConfig() {
			return config;
		}

		public void setConfig(NodeConfigItem config) {
			this.config = config;
		}
	}

	private final ConventionService conventionService;

	@Autowired
	public NodeConfigCacheService(NodeConfigCacheOptions options, ConventionService conventionService) {
		super(options);
		this.conventionService = conventionService;
	}

	@EventListener
	public void handleNodeTouchedEvent(NodeTouchedEvent event) {
		if (event.getId() != null) this.evict(this.buildKey(event.getId()));
	}

	@Override
	protected Class<NodeConfigCacheValue> valueClass() {
		return NodeConfigCacheValue.class;
	}

	@Override
	public String keyOf(NodeConfigCacheValue value) {
		return this.buildKey(value.getId());
	}

	public String buildKey(UUID nodeId) {
		return this.generateKey(new HashMap<>() {{
			put("$node$", nodeId.toString().toLowerCase(Locale.ROOT));
		}});
	}
}
