package gr.cite.intelcomp.graphexplorer.model;


import java.util.List;

public class NodeConfig {
	public final static String _fields = "fields";
	private List<FieldDefinition> fields;
	
	public final static String _clusterFields = "clusterFields";
	private List<String> clusterFields;

	public final static String _defaultOrderField = "defaultOrderField";
	private String defaultOrderField;

	private List<FieldDefinition> getFields() {
		return fields;
	}

	public void setFields(List<FieldDefinition> fields) {
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
}
