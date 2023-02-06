package gr.cite.intelcomp.graphexplorer.common.types.graphconfig;

import java.util.List;

public class NodeConfigEntity {
	private List<FieldDefinitionEntity> fields;

	private List<String> clusterFields;

	private Double minX;
	private Double minY;
	private Double maxX;
	private Double maxY;
	private String defaultOrderField;

	public List<FieldDefinitionEntity> getFields() {
		return fields;
	}

	public void setFields(List<FieldDefinitionEntity> fields) {
		this.fields = fields;
	}

	public List<String> getClusterFields() {
		return clusterFields;
	}

	public void setClusterFields(List<String> clusterFields) {
		this.clusterFields = clusterFields;
	}

	public String getDefaultOrderField() {
		return defaultOrderField;
	}

	public void setDefaultOrderField(String defaultOrderField) {
		this.defaultOrderField = defaultOrderField;
	}

	public Double getMinX() {
		return minX;
	}

	public void setMinX(Double minX) {
		this.minX = minX;
	}

	public Double getMinY() {
		return minY;
	}

	public void setMinY(Double minY) {
		this.minY = minY;
	}

	public Double getMaxX() {
		return maxX;
	}

	public void setMaxX(Double maxX) {
		this.maxX = maxX;
	}

	public Double getMaxY() {
		return maxY;
	}

	public void setMaxY(Double maxY) {
		this.maxY = maxY;
	}
}
