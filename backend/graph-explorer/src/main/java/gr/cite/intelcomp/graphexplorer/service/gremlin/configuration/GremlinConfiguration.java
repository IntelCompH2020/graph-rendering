package gr.cite.intelcomp.graphexplorer.service.gremlin.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GremlinProperties.class)
@ConditionalOnProperty(prefix = "gremlin", name = "enabled", havingValue = "true")
public class GremlinConfiguration {
}
