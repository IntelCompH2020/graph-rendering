package gr.cite.intelcomp.graphexplorer.service.elasticnode;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.node-index")
public class NodeIndexCacheOptions extends CacheOptions {
}
