package indi.shine.boot.base.model.search;

import java.util.List;

public class QueryCondition {

	public enum RELATION {
		// 或
		OR("or"),
		AND("and");
		private final String name;
		private RELATION(final String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	public enum CONDITION {
		// 等于
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
		private CONDITION(final String name) {
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

    public void setFilter(boolean filter) {
        isFilter = filter;
    }

    public List<String> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<String> fieldList) {
        this.fieldList = fieldList;
    }

    public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public List<String> getvList() {
		return vList;
	}
	public void setvList(List<String> vList) {
		this.vList = vList;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public boolean getIsFilter() {
		return isFilter;
	}
	public void setIsFilter(boolean isFilter) {
		this.isFilter = isFilter;
	}


}
