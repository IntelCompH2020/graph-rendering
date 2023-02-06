package gr.cite.intelcomp.graphexplorer.common.types.graphdata;

import gr.cite.intelcomp.graphexplorer.elastic.query.EdgeDataQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphDataEntity {
	public final static String _nodes = "nodes";
	private List<NodeDataEntity> nodes;
	public final static String _size = "size";
	private long size;
	
	public final static String _edges = "edges";
	private List<EdgeDataEntity> edges;

	public List<NodeDataEntity> getNodes() {
		return nodes;
	}

	public void setNodes(List<NodeDataEntity> nodes) {
		this.nodes = nodes;
	}

	public List<EdgeDataEntity> getEdges() {
		return edges;
	}

	public void setEdges(List<EdgeDataEntity> edges) {
		this.edges = edges;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public static GraphDataEntity buildByNodesWithNestedEdges(List<Map<String, Object>> graph){
		GraphDataEntity m = new GraphDataEntity();
		List<NodeDataEntity> nodeEntityList = new ArrayList<>();
		List<EdgeDataEntity> edgeDataEntityList = new ArrayList<>();
		for (Map<String, Object> item : graph){
			nodeEntityList.add(NodeDataEntity.build(item));
		}
		HashSet<String> edgeKeys = new HashSet<>(graph.size() * 5);
		HashSet<String> nodeKeys = new HashSet<>(nodeEntityList.stream().map(x-> x.getId()).collect(Collectors.toList()));
		for (Map<String, Object> item : graph){
			Object itemEdges = item.getOrDefault(GraphDataEntity._edges, null);
			if (itemEdges != null) {
				for (Map<String, Object> edgeData : (List<Map<String,Object>>)itemEdges){
					String sourceId = (String) edgeData.getOrDefault(EdgeDataEntity._sourceId, "");
					String targetId = (String) edgeData.getOrDefault(EdgeDataEntity._targetId, "");
					String edgeKey = sourceId + targetId;
					if (nodeKeys.contains(sourceId) &&  nodeKeys.contains(targetId) && !edgeKeys.contains(edgeKey)){
						edgeKeys.add(edgeKey);
						edgeDataEntityList.add(EdgeDataEntity.build(edgeData));
					}
				}
			}
		}
		m.setNodes(nodeEntityList);
		m.setEdges(edgeDataEntityList);
		return m;
	}

	public static GraphDataEntity buildFromEdges(List<NodeDataEntity> nodes){
		GraphDataEntity m = new GraphDataEntity();
		List<EdgeDataEntity> edgeDataEntityList = new ArrayList<>();
		HashSet<String> edgeKeys = new HashSet<>(nodes.size() * 5);
		HashSet<String> nodeKeys = new HashSet<>(nodes.stream().map(x-> x.getId()).collect(Collectors.toList()));
		for (NodeDataEntity item : nodes){
			if (item.getEdges() != null) {
				for (EdgeDataEntity edgeDataEntity : item.getEdges()){
					String sourceId = edgeDataEntity.getSourceId();
					String targetId = edgeDataEntity.getSourceId();
					String edgeKey = sourceId + targetId;
					if (nodeKeys.contains(sourceId) &&  nodeKeys.contains(targetId) && !edgeKeys.contains(edgeKey)){
						edgeKeys.add(edgeKey);
						edgeDataEntityList.add(edgeDataEntity);
					}
				}
			}
		}
		m.setNodes(nodes);
		m.setEdges(edgeDataEntityList);
		return m;
	}

	public static GraphDataEntity buildFromEElastic(List<gr.cite.intelcomp.graphexplorer.elastic.data.NodeDataEntity> nodes, List<gr.cite.intelcomp.graphexplorer.elastic.data.EdgeDataEntity> edges){
		GraphDataEntity m = new GraphDataEntity();
		List<EdgeDataEntity> edgeDataEntityList = new ArrayList<>();
		List<NodeDataEntity> nodeDataEntityList = new ArrayList<>();
		for (gr.cite.intelcomp.graphexplorer.elastic.data.NodeDataEntity item : nodes){
			NodeDataEntity nodeDataEntity = new NodeDataEntity();
			nodeDataEntity.setId(item.getId());
			nodeDataEntity.setName(item.getName());
			nodeDataEntity.setLabel(item.getLabel());
			nodeDataEntity.setX(item.getX());
			nodeDataEntity.setY(item.getY());
			nodeDataEntity.setProperties(item.getProperties());
			nodeDataEntityList.add(nodeDataEntity);
		}

		for (gr.cite.intelcomp.graphexplorer.elastic.data.EdgeDataEntity item : edges){
			EdgeDataEntity edgeDataEntity = new EdgeDataEntity();
			edgeDataEntity.setId(item.getId());
			edgeDataEntity.setLabel(item.getLabel());
			edgeDataEntity.setSourceId(item.getSourceId());
			edgeDataEntity.setTargetId(item.getTargetId());
			edgeDataEntity.setWeight(item.getWeight());
			edgeDataEntity.setProperties(item.getProperties());
			edgeDataEntityList.add(edgeDataEntity);
		}
		m.setNodes(nodeDataEntityList);
		m.setEdges(edgeDataEntityList);
		return m;
	}
}
