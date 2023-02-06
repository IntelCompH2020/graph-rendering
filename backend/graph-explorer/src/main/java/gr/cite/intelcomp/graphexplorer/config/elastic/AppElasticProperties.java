package gr.cite.intelcomp.graphexplorer.config.elastic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "app-elastic")
public class AppElasticProperties {
	private String nodeDataIndexNamePattern;
	private String nodeCodeKey;
	private String edgeDataIndexNamePattern;
	private String edgeCodeKey;
	private boolean enableIcuAnalysisPlugin;

	public String getNodeDataIndexNamePattern() {
		return nodeDataIndexNamePattern;
	}

	public void setNodeDataIndexNamePattern(String nodeDataIndexNamePattern) {
		this.nodeDataIndexNamePattern = nodeDataIndexNamePattern;
	}

	public String getNodeCodeKey() {
		return nodeCodeKey;
	}

	public void setNodeCodeKey(String nodeCodeKey) {
		this.nodeCodeKey = nodeCodeKey;
	}

	public String getEdgeDataIndexNamePattern() {
		return edgeDataIndexNamePattern;
	}

	public void setEdgeDataIndexNamePattern(String edgeDataIndexNamePattern) {
		this.edgeDataIndexNamePattern = edgeDataIndexNamePattern;
	}

	public String getEdgeCodeKey() {
		return edgeCodeKey;
	}

	public void setEdgeCodeKey(String edgeCodeKey) {
		this.edgeCodeKey = edgeCodeKey;
	}

	public boolean isEnableIcuAnalysisPlugin() {
		return enableIcuAnalysisPlugin;
	}

	public void setEnableIcuAnalysisPlugin(boolean enableIcuAnalysisPlugin) {
		this.enableIcuAnalysisPlugin = enableIcuAnalysisPlugin;
	}
}
