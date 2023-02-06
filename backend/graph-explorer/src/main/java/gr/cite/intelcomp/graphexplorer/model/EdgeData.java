package gr.cite.intelcomp.graphexplorer.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.Map;

public class EdgeData {
	public final static String _id = "id";
	private String id;

	public final static String _label = "label";
	private String label;
	public final static String _sourceId = "sourceId";
	private String sourceId;
	
	public final static String _targetId = "targetId";
	private String targetId;

	public final static String _weight = "weight";
	private Double weight;

	private Map<String, Object> properties;

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
