package gr.cite.intelcomp.graphexplorer.service.elasticedge;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.edge-index")
public class EdgeIndexCacheOptions extends CacheOptions {
}
