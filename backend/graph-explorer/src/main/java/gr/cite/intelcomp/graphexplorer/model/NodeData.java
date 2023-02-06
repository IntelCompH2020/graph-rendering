package gr.cite.intelcomp.graphexplorer.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.List;
import java.util.Map;

public class NodeData {
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
	private Map<String, Object> properties;

	public final static String _edges = "edges";
	private List<EdgeData> edges;

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

	public List<EdgeData> getEdges() {
		return edges;
	}

	public void setEdges(List<EdgeData> edges) {
		this.edges = edges;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@JsonAnyGetter
	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}
