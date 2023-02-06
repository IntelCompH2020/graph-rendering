package gr.cite.intelcomp.graphexplorer.model;


import java.util.List;

public class EdgeConfig {
	public final static String _fields = "fields";
	private List<FieldDefinition> fields;

	public List<FieldDefinition> getFields() {
		return fields;
	}

	public void setFields(List<FieldDefinition> fields) {
		this.fields = fields;
	}
}
