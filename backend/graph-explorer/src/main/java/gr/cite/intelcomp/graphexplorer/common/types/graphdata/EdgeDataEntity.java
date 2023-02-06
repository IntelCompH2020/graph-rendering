package gr.cite.intelcomp.graphexplorer.common.types.graphdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdgeDataEntity {
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

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public String getDirectedKey(){
		return this.sourceId + ":" + this.targetId;
	}

	public static EdgeDataEntity build(Map<String, Object> d){
		EdgeDataEntity m = new EdgeDataEntity();
		m.setWeight((Double) d.getOrDefault(EdgeDataEntity._weight, null));
		m.setSourceId((String) d.getOrDefault(EdgeDataEntity._sourceId, null));
		m.setTargetId((String) d.getOrDefault(EdgeDataEntity._targetId, null));
		m.setTargetId((String) d.getOrDefault(EdgeDataEntity._label, null));
		m.setTargetId((String) d.getOrDefault(EdgeDataEntity._id, null));
		Map<String, Object> properties = new HashMap<>();
		for (String prop : d.keySet()) {
			if (!prop.equals(EdgeDataEntity._weight) 
					&& !prop.equals(EdgeDataEntity._sourceId) 
					&& !prop.equals(EdgeDataEntity._targetId)){ properties.put(prop, d.get(prop));
			}
		}
		if (properties.size() > 0) m.setProperties(properties);
		return m;
	}
}
