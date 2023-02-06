package gr.cite.intelcomp.graphexplorer.elastic.data;

import net.minidev.json.annotate.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Document(indexName = "node")
public class NodeDataEntity {

	public static final class Fields {
		public static final String id = "id";
		public static final String label = "label";
		public static final String name = "name";
		public static final String x = "x";
		public static final String y = "y";
	}

	@Id
	@Field(value = Fields.id, type = FieldType.Keyword)
	private String id;

	@Field(value = Fields.label, type = FieldType.Keyword)
	private String label;
	
	@Field(value = Fields.name, type = FieldType.Keyword)
	private String name;

	@Field(value = Fields.x, type = FieldType.Double)
	private Double x;

	@Field(value = Fields.y, type = FieldType.Double)
	private Double y;
	@JsonIgnore
	private Map<String, Object> properties;

	@JsonIgnore
	private UUID nodeId;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public UUID getNodeId() {
		return nodeId;
	}

	public void setNodeId(UUID nodeId) {
		this.nodeId = nodeId;
	}
}

