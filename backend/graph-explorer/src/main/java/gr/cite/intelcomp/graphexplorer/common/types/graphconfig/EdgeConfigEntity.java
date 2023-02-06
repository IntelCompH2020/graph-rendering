package gr.cite.intelcomp.graphexplorer.common.types.graphconfig;

import java.util.List;

public class EdgeConfigEntity {
	private List<FieldDefinitionEntity> fields;

	public List<FieldDefinitionEntity> getFields() {
		return fields;
	}

	public void setFields(List<FieldDefinitionEntity> fields) {
		this.fields = fields;
	}
}
