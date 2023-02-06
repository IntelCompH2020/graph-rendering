package gr.cite.intelcomp.graphexplorer.common.enums;

import gr.cite.tools.elastic.query.CompareOperator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.jetbrains.annotations.NotNull;

public enum CompareType {
	EQUAL,
	NOT_EQUAL,
	GREATER,
	GREATER_EQUAL,
	LESS,
	LESS_EQUAL;
	
	public CompareOperator toElasticCompare(){
		switch (this) {
			case GREATER_EQUAL:
				return CompareOperator.GreaterEqual;
			case GREATER:
				return CompareOperator.GreaterThan;
			case LESS_EQUAL:
				return CompareOperator.LessEqual;
			case LESS:
				return CompareOperator.LessThan;
			case EQUAL:
				return CompareOperator.Equal;
			case NOT_EQUAL:
				return CompareOperator.NotEqual;
			default:
				throw new IllegalArgumentException("Invalid type " + this);
		}
	}
}
