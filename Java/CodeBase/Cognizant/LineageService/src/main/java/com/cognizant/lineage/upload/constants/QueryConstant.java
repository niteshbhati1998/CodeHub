package com.cognizant.lineage.upload.constants;

/**
 * The QueryConstant
 *
 * @author
 *
 */
public class QueryConstant {

	private QueryConstant() {
		throw new IllegalAccessError("Utility class");
	}

	//execution_id is job_id

	public static final String COMPONENT_LEVEL_LINEAGE = "INFA_COMPONENT_LEVEL_LINEAGE";
	public static final String TABLE_LEVEL_LINEAGE = "INFA_TABLE_LEVEL_LINEAGE";
	public static final String SUMMARY_LEVEL_LINEAGE = "INFA_EXECUTION_SUMMERY";
	public static final String LINEAGE_EXECUTION_ID_SEQ = "lineage_job_status_seq";
	public static final String infa_gremlin_loading_status = "presentation.delta_data_loading_status";
	public static final String COMMON_SCRIPT_STATUS_TABLE = "semantic.delta_data_loading_status";
	public static final String INFORMATICA_QUERY_TABLE = "informatica_source_sql";

	public static final String INSERT_INTO_INFORMATICA_COMPONENT_LINEAGE = "insert into " + COMPONENT_LEVEL_LINEAGE
			+ " (file_name,folder_name,mapping_name,from_component,from_component_type,to_component,to_component_type,source_databasetype,source_db_name,target_databasetype,target_db_name,status,error_msg,source_table_name,target_table_name,job_id,session_name)\r\n"
			+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String INSERT_INTO_INFORMATICA_COMPONENT_LINEAGE_FAILURE = "insert into " + COMPONENT_LEVEL_LINEAGE
			+ " (file_name,folder_name,mapping_name,from_component,from_component_type,to_component,to_component_type,source_databasetype,source_db_name,target_databasetype,target_db_name,status,error_msg,source_table_name,target_table_name,job_id,session_name)\r\n"
			+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String INFORMATICA_COMPONENT_LENEAGE_EXIST = "select count(*) from " + COMPONENT_LEVEL_LINEAGE
			+ " where file_name = ? and status = 'Success'";

	public static final String FAILURE_INFORMATICA_COMPONENT_LENEAGE_DELETE = "DELETE FROM " + COMPONENT_LEVEL_LINEAGE
			+ " WHERE file_name= ? ";
	public static final String INSERT_INTO_INFORMATICA_TABLE_LEVEL_LINEAGE = "insert into " + TABLE_LEVEL_LINEAGE
			+ " (FILE_NAME,FOLDER_NAME,MAPPING_NAME,FROM_TABLE,TO_TABLE,SOURCE_DATABASETYPE,source_db_name,TARGET_DATABASETYPE,target_db_name,job_id, sourcetype, targettype, session_name, param_filename)"
			+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static final String INFA_LENEAGE_EXECUTION_SEQUENCE_ID = "select nextval('" + LINEAGE_EXECUTION_ID_SEQ + "')";
	public static final String INFA_LENEAGE_SEQUENCE_ID = "select last_value from "+ LINEAGE_EXECUTION_ID_SEQ +"";
	public static final String INFA_LENEAGE_GREMLIN_STATUS = "select count(*) from "+ infa_gremlin_loading_status +" where execution_id=?";
	public static final String INFRA_GREMLIN_STATUS_STEPS = "select steps from "+infa_gremlin_loading_status+" where execution_id=? limit 1";

	public static final String INSERT_INTO_INFA_EXECUTION_SUMMERY = "insert into " + SUMMARY_LEVEL_LINEAGE
			+ " (job_id,batch_size,file_name,start_time,end_time,status,log_file) "
			+ "values(?,?,?,to_timestamp(?::numeric/1000.0) AT TIME ZONE 'UTC',to_timestamp(?::numeric/1000.0) AT TIME ZONE 'UTC',?,?)";

	public static final String GET_RUN_STATS = "select job_id, batch_size, " +
			" (select count(*) from " + SUMMARY_LEVEL_LINEAGE + " where job_id ="
			+ "(select max(job_id) from " + SUMMARY_LEVEL_LINEAGE + " ) and status = 'Completed') as success_count, " +
			" (select count(*) from " + SUMMARY_LEVEL_LINEAGE + " where job_id =(select max(job_id) from " + SUMMARY_LEVEL_LINEAGE + " ) "
			+ " and status = 'Failed') as fail_count" +
			" from " + SUMMARY_LEVEL_LINEAGE +
			" where job_id = (select max(job_id) from " + SUMMARY_LEVEL_LINEAGE + " ) limit 1";

	public static final String GET_SUMMARY = "select job_id, batch_size, file_name, to_char(start_time, 'MM/DD/YYYY HH24:MI') start_time, to_char(end_time, 'MM/DD/YYYY HH24:MI') end_time, status, log_file "
			+ "from " + SUMMARY_LEVEL_LINEAGE + " where job_id = (select max(job_id) from " + SUMMARY_LEVEL_LINEAGE +" ) order by status";

	//common python script status query for step 12
	public static final String GET_COMMON_SCRIPT_EXECUTION_STATUS = "select ui_log from "+ COMMON_SCRIPT_STATUS_TABLE +" limit 1";



	public static final String INSERT_PROJECT_NAMES = "insert into project_names (project_name) values(?)";
	public static final String GET_PROJECT_NAMES = "select project_name from project_names";

	//insert informatic sql query
	public static final String INSERT_INTO_INFA_QUERY_TABLE =
			"insert into "+INFORMATICA_QUERY_TABLE+" (job_id,file_name,folder_name,mapping_name,sql,source_type,target, from_component, to_component, session_name, param_filename) \n" +
					"values(?,?,?,?,?,?,?,?,?,?,?);";

	public static final String INFA_GET_ROW_COUNT_FOR_SQL_TABLE =
			"select COUNT(*) from "+INFORMATICA_QUERY_TABLE +" where job_id=?";

	public static final String DATASTAGE_GET_ROW_COUNT_FOR_SQL_TABLE =
			"select count(query) from datastage_table_level_lineage where query=? and job_id=?";

	//odi
	public static final String BTEQ_LENEAGE_EXECUTION_NEXT_SEQUENCE_ID = "select nextval('"+ LINEAGE_EXECUTION_ID_SEQ +"');";

	public static final String GET_QUERY_FORMAT_DETAILS = "select function_name,start_argument,end_argument from semantic.odi_format_details ";

	public static final String INSERT_INTO_ODI_DETAILS = "insert into odi_details(job_id,sqltext,new_sqltext,start_line,end_line,query_type,creation_time,updation_time,filename) " +
			"values(?,?,?,?,?,?,?,?,?)";

	public static final String ODI_LENEAGE_EXECUTION_SEQUENCE_ID = "select last_value from DB_SCRIPT_EXECUTION_ID";

	public static final String GET_TOTAL_RECORD_COUNT = "select count(*) from semantic.odi_details";

	public static final String GET_TOTAL_PROCESSED_COUNT = "select count(*) from semantic.script_files_info";

	public static final String INFORMATICA_COMPLEXITY_QUERY = "select component_type as componentType, count(*) from ( \n" +
			"select distinct from_component as component, from_component_type as component_type from semantic.INFA_COMPONENT_LEVEL_LINEAGE \n" +
			"where file_name = :fileName and job_id = :jobId \n" +
			"union \n" +
			"select distinct to_component as component,to_component_type as component_type from semantic.INFA_COMPONENT_LEVEL_LINEAGE \n" +
			"where file_name = :fileName and job_id = :jobId ) a group by 1;";
	public static final String MAPPING_AND_FILENAME_INFIX = "__";

	public static final String METADATA_OBJECT_NAME_DETAILS_QUERY = "SELECT node_name as nodeName,\n" +
			"       node_type as nodeType,\n" +
			"       script_name as scriptName,\n" +
			"       script_type as scriptType,\n" +
			"       CASE\n" +
			"         WHEN sql_text is NULL or sql_text = 'NULL' THEN 'N/A' \n" +
			"         ELSE sql_text \n" +
			"       END AS sqltext \n" +
			"FROM (SELECT DISTINCT node_name,\n" +
			"             node_type,\n" +
			"             script_name,\n" +
			"             script_type,\n" +
			"             sql_text\n" +
			"      FROM (SELECT n.project_name,\n" +
			"                   n.node_name,\n" +
			"                   n.node_type,\n" +
			"                   e.script_name,\n" +
			"                   e.script_type,\n" +
			"                   e.sql_text\n" +
			"            FROM presentation.nodes n\n" +
			"              INNER JOIN presentation.edges e\n" +
			"                      ON n.project_name = e.project_name\n" +
			"                     AND (n.node_name = e.from_node\n" +
			"                      OR n.node_name = e.to_node)\n" +
			"            WHERE n.project_name = :projectName \n" +
			"            AND   e.project_name = :projectName ) abc ) a\n" +
			"WHERE node_name = :objectName ";

	public static final String METADATA_APPLICATION_DETAILS_QUERY =
			"SELECT script_name AS scriptName,\n" +
					"       script_type AS scriptType,\n" +
					"       sql_type AS sqlType,\n" +
					"       source AS sourceNode " +
					"FROM (SELECT DISTINCT script_name,\n" +
					"             script_type,\n" +
					"             statement_type AS sql_type,\n" +
					"             from_node AS source,\n" +
					"             to_node AS target\n" +
					"      FROM presentation.edges\n" +
					"      WHERE project_name = :projectName\n" +
					"      AND   script_name = :objectOrAppName ) a;";

	public static final String INCOMING_NODES_OF_A_NODE = "select string_agg(from_node, ',') as nodes from presentation.edges where \n" +
			"project_name = :projectName and \n" +
			"script_name= :scriptName and \n" +
			"script_type= :scriptType and \n" +
			"sql_text= :sqlText\n" +
			"and to_node= :node ";

	public static final String OUTGOING_NODES_OF_A_NODE = "select string_agg(to_node, ',') as nodes from presentation.edges where \n" +
			"project_name = :projectName and \n" +
			"script_name= :scriptName and \n" +
			"script_type= :scriptType and \n" +
			"sql_text= :sqlText\n" +
			"and from_node= :node ";
	
	public static final String GET_INFORMATICA_SOURCE_TARGET_TO_BE_REPLACED = "select file_name, session_name, from_table, to_table, param_filename from infa_table_level_lineage "
			+ "where (from_table like '$%' or to_table like '$%') and job_id=?";
	
	public static final String UPDATE_INFORMATICA_PARAM_SOURCE_TARGET = "update infa_table_level_lineage "
			+ "set from_table=?, to_table=? where file_name=? and job_id=?";

	public static final String NO_OF_RECORDS_SCRIPT_FILES_INFO = "select count(*) from semantic.script_files_info where job_id = ?";
}
