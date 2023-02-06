package gr.cite.intelcomp.graphexplorer.service.graph;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "graph")
public class GraphProperties {
	private int nodeImportBatchSize;
	private int edgeImportBatchSize;
	private int edgeScrollSize;
	private int maxEdgeResultSize;

	public int getNodeImportBatchSize() {
		return nodeImportBatchSize;
	}

	public void setNodeImportBatchSize(int nodeImportBatchSize) {
		this.nodeImportBatchSize = nodeImportBatchSize;
	}

	public int getEdgeImportBatchSize() {
		return edgeImportBatchSize;
	}

	public void setEdgeImportBatchSize(int edgeImportBatchSize) {
		this.edgeImportBatchSize = edgeImportBatchSize;
	}

	public int getEdgeScrollSize() {
		return edgeScrollSize;
	}

	public void setEdgeScrollSize(int edgeScrollSize) {
		this.edgeScrollSize = edgeScrollSize;
	}

	public int getMaxEdgeResultSize() {
		return maxEdgeResultSize;
	}

	public void setMaxEdgeResultSize(int maxEdgeResultSize) {
		this.maxEdgeResultSize = maxEdgeResultSize;
	}
}
