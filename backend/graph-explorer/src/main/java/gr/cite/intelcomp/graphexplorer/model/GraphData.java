package gr.cite.intelcomp.graphexplorer.model;

import java.util.List;

public class GraphData {
	public final static String _nodes = "nodes";
	private List<NodeData> nodes;
	
	public final static String _edges = "edges";
	private List<EdgeData> edges;
	public final static String _size = "size";
	private Long size;

	public List<NodeData> getNodes() {
		return nodes;
	}

	public void setNodes(List<NodeData> nodes) {
		this.nodes = nodes;
	}

	public List<EdgeData> getEdges() {
		return edges;
	}

	public void setEdges(List<EdgeData> edges) {
		this.edges = edges;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
}
