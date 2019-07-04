package indi.shine.boot.base.model.search;

import lombok.Data;

import java.util.List;

@Data
public class QueryCondition {

	public enum RELATION {
		// 或
		OR("or"),
		AND("and");
		private final String name;
		RELATION(final String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	public enum CONDITION {
		// 条件符
		EQ("eq"),
		GT("gt"),
		GTE("gte"),
		LT("lt"),
		LTE("lte"),
		IN("in"),
		RANGE("range"),
		TERM("term"),
		QUERYSTRING("queryString"),
		PREFIX("prefix");
		private final String name;
		CONDITION(final String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}

	private String field;
	private String condition;
	private List<String> vList;
	private String relation;
	private boolean isFilter = true;
    private List<String> fieldList;

    public boolean isFilter() {
        return isFilter;
    }
}
