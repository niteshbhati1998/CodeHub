package com.cognizant.lineage.upload.constants;

/**
 * The QueryConstant
 * 
 * @author
 *
 */
public class BteqQueryConstant {

	private BteqQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}

	public static final String DB_SCRIPT_TABLE_NAME = "DB_SCRIPT_CLEANSING_EXECUTION_SUMMARY";
	public static final String DB_SCRIPT_FILES = "script_files_info";
	public static final String DB_BEFORE_PARSING = "before_parsing_output";
	public static final String DB_PARSING_OUTPUT = "parsing_output";
	public static final String DB_UNNEST_OUTPUT = "unnest_output";
	public static final String DB_EXECUTION_SUMMARY = "lineage_execution_summary";
	public static final String DB_VOLATILE_OUTPUT="volatile_output";
	//public static final String DB_UNNEST_FOR_ALIAS = "unnest_output";
	public static final String GREMLIN_STATUS_BTEQ= "presentation.delta_data_loading_status";  //"bteq_gremlin_loading_status";
	public static final String ALIAS_HANDLING_OUTPUT="alias_handling_output";
		
	public static final String GET_TOTAL_RECORD_COUNT = "select count(*) from " + DB_SCRIPT_TABLE_NAME + 
			" where execution_id=(select max(execution_id) from " + DB_SCRIPT_TABLE_NAME + ") and step_number=?";

	public static final String GET_TOTAL_PROCESSED_COUNT = "select count(*) from " + DB_SCRIPT_TABLE_NAME
			+ " where execution_id=(select max(execution_id) from " + DB_SCRIPT_TABLE_NAME + ")"
			+ " and step_number=? and status is not NULL";

	// STEP 6
	public static final String GET_TOTAL_QUERY_RECORD_COUNT = "select count(*) total_count from " + DB_SCRIPT_FILES
			+ " where UPPER(sqltext) like '%FROM%'";
	
	public static final String GET_PROCESSED_RECORD_COUNT = "select count(*) current_processed_count from "
			+ DB_SCRIPT_FILES + " where status='issue' ";

	// STEP 7-8
	public static final String GET_TOTAL_FILES_COUNT = "select count(distinct filename) from " + DB_SCRIPT_FILES;

	// STEP 7
	public static final String GET_BEFORE_PARSING_FILE_COUNT = "select count(distinct filename) from "
			+ DB_BEFORE_PARSING + " where trim(new_status) is NOT NULL";

	// STEP 8
	public static final String GET_PARSING_COUNT = "select count(distinct filename) from " + DB_PARSING_OUTPUT;

	// STEP 9
	public static final String TOTAL_FOR_STEP9 = "select count(distinct filename) from "+ DB_PARSING_OUTPUT+" where lower(status) like 'success%'";
	public static final String GET_UNNEST_COUNT = "select count(distinct filename) from " + DB_UNNEST_OUTPUT;

	// STEP 10
	public static final String TOTAL_FOR_STEP10 = "select count(distinct filename) from "+ DB_UNNEST_OUTPUT+" where execution_status is null";
	public static final String GET_EXECUTION_SUMMARY_COUNT = "select count( distinct filename) from "+ALIAS_HANDLING_OUTPUT;
			//"select count(distinct file_name) from "+DB_EXECUTION_SUMMARY + " where status = 'Completed'";
					//+ " and execution_id = (select max(execution_id) from "+ DB_EXECUTION_SUMMARY + ")";
	
	//step 11
	public static final String X_VALUE_FOR_STEP11 = "select count(status) as processed_count from "+DB_VOLATILE_OUTPUT+" where status = 'COMPLETED'";
	public static final String Y_VALUE_FOR_STEP11 = "select count(table_type) as total_volatile_count from "+DB_VOLATILE_OUTPUT+" where table_type = 'VOLATILE'";
	
	// STEP 12
	public static final String BTEQ_GREMLIN_STATUS = "select count(*) from "+GREMLIN_STATUS_BTEQ+" where execution_id=?";
	public static final String BTEQ_GREMLIN_STATUS_STEPS = "select steps from "+GREMLIN_STATUS_BTEQ+" where job_id=? limit 1";
			
}
