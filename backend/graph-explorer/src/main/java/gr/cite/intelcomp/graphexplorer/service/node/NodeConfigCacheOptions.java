package gr.cite.intelcomp.graphexplorer.service.node;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.node-config")
public class NodeConfigCacheOptions extends CacheOptions {
}
