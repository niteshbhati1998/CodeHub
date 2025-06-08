package com.cognizant.lineage.upload.constants;

public class SummaryQueryConstant {

	private SummaryQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}

	public static final String GET_SCRIPT_DETAILS = "select distinct script_name, script_type from presentation.edges where project_name = ?";
	
	public static final String GET_SCRIPT_TYPE_COUNT_DETAILS = "select script_type, count(script_type) from(" + 
			"select distinct script_name,script_type from presentation.edges " + 
			"where project_name = ?)a " + 
			"group by 1";
	
	public static final String GET_TECHNOLOGY_LIST = "select distinct script_type from presentation.edges where project_name = ?";
	
	public static final String FETCH_TECHNOLOGY_DETAILS = "select distinct script_name, from_node as source, to_node as target, sql_text, statement_type from presentation.edges " + 
			"where project_name = ? and script_type = ?";
}
