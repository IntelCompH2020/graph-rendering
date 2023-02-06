package gr.cite.intelcomp.graphexplorer.service.gremlin.query.types;

import gr.cite.intelcomp.graphexplorer.common.enums.CompareType;

public class DoubleCompare {
	private Double value;
	private CompareType compareType;

	public DoubleCompare(Double value, CompareType compareType) {
		this.value = value;
		this.compareType = compareType;
	}
	public DoubleCompare() {
	}


	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public CompareType getCompareType() {
		return compareType;
	}

	public void setCompareType(CompareType compareType) {
		this.compareType = compareType;
	}
}
