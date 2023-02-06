package gr.cite.intelcomp.graphexplorer.authorization.cache;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.authorization-principal-node-resource")
public class PrincipalNodeResourceCacheOptions extends CacheOptions {
}
