package gr.cite.intelcomp.graphexplorer.service.elasticnode;

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
public class NodeIndexCacheService extends CacheService<NodeIndexCacheService.NodeIndexCacheValue> {

	public static class NodeIndexCacheValue {

		public NodeIndexCacheValue() {
		}

		public NodeIndexCacheValue(UUID id, String indexName) {
			this.id = id;
			this.indexName = indexName;
		}

		private UUID id;

		private String indexName;

		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public String getIndexName() {
			return indexName;
		}

		public void setIndexName(String indexName) {
			this.indexName = indexName;
		}
	}

	private final ConventionService conventionService;

	@Autowired
	public NodeIndexCacheService(NodeIndexCacheOptions options, ConventionService conventionService) {
		super(options);
		this.conventionService = conventionService;
	}

	@EventListener
	public void handleNodeTouchedEvent(NodeTouchedEvent event) {
		if (event.getId() != null) this.evict(this.buildKey(event.getId()));
	}

	@Override
	protected Class<NodeIndexCacheValue> valueClass() {
		return NodeIndexCacheValue.class;
	}

	@Override
	public String keyOf(NodeIndexCacheValue value) {
		return this.buildKey(value.getId());
	}

	public String buildKey(UUID node) {
		return this.generateKey(new HashMap<>() {{
			put("$node$", node.toString().toLowerCase(Locale.ROOT));
		}});
	}
}
