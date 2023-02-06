package gr.cite.intelcomp.graphexplorer.service.edge;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.edge-config")
public class EdgeConfigCacheOptions extends CacheOptions {
}
