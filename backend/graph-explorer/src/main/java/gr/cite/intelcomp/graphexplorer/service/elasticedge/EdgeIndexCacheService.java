package gr.cite.intelcomp.graphexplorer.service.elasticedge;

import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.event.EdgeTouchedEvent;
import gr.cite.tools.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class EdgeIndexCacheService extends CacheService<EdgeIndexCacheService.EdgeIndexCacheValue> {

	public static class EdgeIndexCacheValue {

		public EdgeIndexCacheValue() {
		}

		public EdgeIndexCacheValue(UUID id, String indexName) {
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
	public EdgeIndexCacheService(EdgeIndexCacheOptions options, ConventionService conventionService) {
		super(options);
		this.conventionService = conventionService;
	}

	@EventListener
	public void handleEdgeTouchedEvent(EdgeTouchedEvent event) {
		if (event.getId() != null) this.evict(this.buildKey(event.getId()));
	}

	@Override
	protected Class<EdgeIndexCacheValue> valueClass() {
		return EdgeIndexCacheValue.class;
	}

	@Override
	public String keyOf(EdgeIndexCacheValue value) {
		return this.buildKey(value.getId());
	}

	public String buildKey(UUID edge) {
		return this.generateKey(new HashMap<>() {{
			put("$edge$", edge.toString().toLowerCase(Locale.ROOT));
		}});
	}
}
