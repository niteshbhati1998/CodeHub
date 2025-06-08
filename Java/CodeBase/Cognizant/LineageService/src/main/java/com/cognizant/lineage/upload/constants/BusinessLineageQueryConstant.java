package com.cognizant.lineage.upload.constants;

public class BusinessLineageQueryConstant {
	private BusinessLineageQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}
	
	public static final String GET_PROJECT_NAMES_LIST = "select distinct project_name from semantic.module_wise_objects_name";
}