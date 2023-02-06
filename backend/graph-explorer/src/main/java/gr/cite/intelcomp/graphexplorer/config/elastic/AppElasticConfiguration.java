package gr.cite.intelcomp.graphexplorer.config.elastic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppElasticProperties.class)
public class AppElasticConfiguration {
	private final AppElasticProperties properties;

	@Autowired
	public AppElasticConfiguration(AppElasticProperties properties) {
		this.properties = properties;
	}

	public AppElasticProperties getProperties() {
		return properties;
	}
}
