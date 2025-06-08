package com.cognizant.lineage.database.constants;

public class PyvisLibraryQueryConstant {
	
	private PyvisLibraryQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}
	public static final String GET_FILTER_DROPDOWN_FOR_NODE = "select distinct node_name from nodes "
			+ "where trim(project_name)=? and  island_id not in (select distinct island_id from edges where trim(project_name) =? and trim(script_type)='SHELL') order by 1";

	public static final String GET_FILTER_DROPDOWN_FOR_TECH = "select distinct script_type from edges where trim(project_name)=? and trim(script_type) !='SHELL' order by 1";

	public static final String GET_FILTER_DROPDOWN_FOR_SCRIPT = "select distinct node_name from nodes "
			+ "where trim(project_name)=? and  island_id in (select distinct island_id from edges where trim(project_name) =? and trim(script_type)='SHELL') order by 1";
}
