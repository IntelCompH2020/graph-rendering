package gr.cite.intelcomp.graphexplorer.common.types.graphdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeDataEntity {
	public final static String _id = "id";
	private String id;
	
	public final static String _name = "name";
	private String name;

	public final static String _label = "label";
	private String label;

	public final static String _x = "x";
	private Double x;

	public final static String _y = "y";
	private Double y;

	public final static String _edges = "edges";
	private List<EdgeDataEntity> edges;
	
	private Map<String, Object> properties;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<EdgeDataEntity> getEdges() {
		return edges;
	}

	public void setEdges(List<EdgeDataEntity> edges) {
		this.edges = edges;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public static NodeDataEntity build(Map<String, Object> d ){
		NodeDataEntity m = new NodeDataEntity();
		m.setId((String) d.getOrDefault(NodeDataEntity._id, null));
		m.setName((String) d.getOrDefault(NodeDataEntity._name, null));
		m.setName((String) d.getOrDefault(NodeDataEntity._label, null));
		m.setX((Double) d.getOrDefault(NodeDataEntity._x, null));
		m.setY((Double) d.getOrDefault(NodeDataEntity._y, null));
		Map<String, Object> properties = new HashMap<>();
		for (String prop : d.keySet()) {
			if (!prop.equals(NodeDataEntity._y) && !prop.equals(NodeDataEntity._x) && !prop.equals(NodeDataEntity._id)) properties.put(prop, d.get(prop));
		}
		if (properties.size() > 0) m.setProperties(properties);
		return m;
	}
}
