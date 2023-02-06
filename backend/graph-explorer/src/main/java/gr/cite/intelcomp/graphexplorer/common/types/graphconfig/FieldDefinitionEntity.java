package gr.cite.intelcomp.graphexplorer.common.types.graphconfig;

import gr.cite.intelcomp.graphexplorer.common.enums.GraphFieldType;

public class FieldDefinitionEntity {
	private String code;
	private GraphFieldType type;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public GraphFieldType getType() {
		return type;
	}

	public void setType(GraphFieldType type) {
		this.type = type;
	}
}
