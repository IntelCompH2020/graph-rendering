package gr.cite.intelcomp.graphexplorer.service.gremlin.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.apache.tinkerpop.gremlin.driver.ser.Serializers;

@ConfigurationProperties(prefix = "gremlin")
public class GremlinProperties {
	private boolean enabled;
	private String endpoint;

	private int port;
	
	private long evaluationTimeout;

	private String username;

	private String password;

	private boolean sslEnabled;
	private int maxContentLength; 
	private String serializer = Serializers.GRAPHBINARY_V1D0.toString();

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSslEnabled() {
		return sslEnabled;
	}

	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public String getSerializer() {
		return serializer;
	}

	public void setSerializer(String serializer) {
		this.serializer = serializer;
	}

	public int getMaxContentLength() {
		return maxContentLength;
	}

	public void setMaxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
	}

	public long getEvaluationTimeout() {
		return evaluationTimeout;
	}

	public void setEvaluationTimeout(long evaluationTimeout) {
		this.evaluationTimeout = evaluationTimeout;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
