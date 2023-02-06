package gr.cite.intelcomp.graphexplorer.model;

import gr.cite.intelcomp.graphexplorer.common.enums.GraphFieldType;

import java.util.List;
import java.util.UUID;

public class FieldDefinition {

	public final static String _code = "code";
	private String code;

	public final static String _type = "type";
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
