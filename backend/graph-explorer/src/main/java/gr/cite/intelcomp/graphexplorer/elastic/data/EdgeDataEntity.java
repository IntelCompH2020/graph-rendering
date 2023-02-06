package gr.cite.intelcomp.graphexplorer.elastic.data;

import net.minidev.json.annotate.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;
import java.util.UUID;

@Document(indexName = "node")
public class EdgeDataEntity {

	public static final class Fields {
		public static final String id = "id";
		public static final String label = "label";
		public static final String sourceId = "sourceId";
		public static final String targetId = "targetId";
		public static final String weight = "weight";
	}

	@Id
	@Field(value = Fields.id, type = FieldType.Keyword)
	private String id;
	
	@Field(value = Fields.label, type = FieldType.Keyword)
	private String label;

	@Field(value = Fields.sourceId, type = FieldType.Keyword)
	private String sourceId;

	@Field(value = Fields.targetId, type = FieldType.Keyword)
	private String targetId;
	
	@Field(value = Fields.weight, type = FieldType.Double)
	private Double weight;

	@JsonIgnore
	private Map<String, Object> properties;
	
	@JsonIgnore
	private UUID edgeId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public UUID getEdgeId() {
		return edgeId;
	}

	public void setEdgeId(UUID edgeId) {
		this.edgeId = edgeId;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}

