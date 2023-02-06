package gr.cite.intelcomp.graphexplorer.common.enums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum GraphFieldType {
	String("string"),
	Integer("integer"),
	Double("double"),
	Date("date"),
	;
	private static final Map<String, GraphFieldType> values = new HashMap<>();

	private final String mappedName;

	//For jackson parsing (used by MVC)
	@JsonValue
	public java.lang.String getMappedName() {
		return mappedName;
	}

	static {
		for (GraphFieldType e : values()) {
			values.put(e.asString(), e);
		}
	}

	private GraphFieldType(String mappedName) {
		this.mappedName = mappedName;
	}

	public String asString() {
		return this.mappedName;
	}

	public static GraphFieldType fromString(String value) {
		return values.getOrDefault(value, String);
	}
}
