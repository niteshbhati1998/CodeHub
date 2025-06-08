package com.cognizant.lineage.upload.constants;

import java.util.Arrays;
import java.util.List;

public class GeneralConstants {
		
	public static final String FileTypeConstraint = ".XML";
	public static final String POWERMART = "POWERMART";
	public static final String REPOSITORY_TAG = "REPOSITORY";
	public static final String FOLDER_TAG = "FOLDER";
	public static final String SOURCE_TAG = "SOURCE";
	public static final String TARGET_TAG = "TARGET";
	public static final String MAPPING_TAG = "MAPPING";
	public static final String TRANSFORMATION = "TRANSFORMATION";
	public static final String INSTANCE_TAG = "INSTANCE";
	public static final String CONNECTOR_TAG = "CONNECTOR";
	public static final String NAME_ATTRIBUTE = "NAME";
	public static final String TRANSFORMATION_TYPE = "TRANSFORMATION_TYPE";
	public static final String TRANSFORMATION_NAME = "TRANSFORMATION_NAME";
	public static final String SOURCE_DEFINITION = "Source Definition";
	public static final String TARGET_DEFINITION = "Target Definition";
	public static final String DATABASETYPE = "DATABASETYPE";
	public static final String DBDNAME = "DBDNAME";
	public static final String FROMINSTANCETYPE = "FROMINSTANCETYPE";
	public static final String TOINSTANCETYPE = "TOINSTANCETYPE";
	public static final String FROMINSTANCE = "FROMINSTANCE";
	public static final String TOINSTANCE = "TOINSTANCE";
	public static final String MESSAGE_VALUE =" Tag <> nodeList not available";
	public static final String SUCCESS = "Success";
	public static final String FAILURE = "Failure";
	public static final String FAILED = "Failed";
	public static final String COMPLETED = "Completed";
	public static final String LOOKUP_PROCEDURE = "Lookup Procedure";
	public static final String LOOKUP_PROCEDURE1 = "LookupProcedure";
	public static final String LOOKUP_TABLE_NAME = "Lookup table name";
	public static final String Lookup_Sql_Override = "Lookup Sql Override";
	public static final String Update_Override = "Update Override";
	public static final String TABLEATTRIBUTE = "TABLEATTRIBUTE";
	public static final String TYPE = "TYPE";
	public static final String VALUE = "VALUE";
	
	public static final String LOG_FILE_NAME_STEP0 = "pre_processing_script";
	//public static final String LOG_FILE_NAME_STEP0PB = "power_bi_cleansing";
	public static final String LOG_FILE_NAME_STEP1To4 = "cleansing";
	public static final String LOG_FILE_NAME_STEP5 = "summary";
	public static final String LOG_FILE_NAME_STEP6 = "UpdateSqltextInScriptInfo";
	public static final String LOG_FILE_NAME_STEP7 = "BeforeParsingCode";
	public static final String LOG_FILE_NAME_STEP8 = "ParsingCode";
	public static final String LOG_FILE_NAME_STEP9 = "UnnestCode";
	public static final String LOG_FILE_NAME_STEP10 = "AliasHandlingCode";
	public static final String LOG_FILE_NAME_STEP11 = "AliasHandlingOutputWithVolatileTables";
	public static final String LOG_FILE_NAME_STEP12 = "graphdbPGSQLloading";
	//public static final String LOG_FILE_NAME_STEP13 = "graphdbPGSQLloading.log";
	public static final String LOG_FILE_NAME_INFORMATICA_GREMLIN = "graphdbPGSQLloading";
	public static final String LOG_FILE_NAME_QLIK_VIEW = "queries_from_log_files";
	
	public static final String KSH_LOG_FILE_NAME_1="ksh_cleansing_script";
	public static final String WORKFLOW_TAG = "WORKFLOW";
	public static final String SESSION_EXTENSION_TAG = "SESSIONEXTENSION";
	
	
	public static final String SINSTANCENAME = "SINSTANCENAME";
	public static final String ATTRIBUTE_TAG = "ATTRIBUTE";
	public static final String SOURCE_DIR_NAME  = "Source file directory";
	public static final String FILE_NAME = "Source filename";
	public static final String FROMFIELD = "FROMFIELD";
	public static final String TOFIELD = "TOFIELD";
	public static final String OWNERNAME = "OWNERNAME";
	public static final String EXPRESSION = "Expression";
	public static final String EXPRESSION_VALUE = "EXPRESSION";
	public static final String TRANSFORMFIELD = "TRANSFORMFIELD";
	public static final String PORTTYPE = "PORTTYPE";
	public static final String PORTTYPE_VALUE = "OUTPUT";
	
	public static final String OUTPUT_DIR = "Output file directory";
	public static final String OUTPUT_FILE = "Output filename";
	public static final String SOURCE_QUALIFIER = "Source Qualifier";
	
	public static final String AGGREGATOR = "Aggregator";
	public static final String EXPRESSION_TYPE = "EXPRESSIONTYPE";
	public static final String GROUP = "GROUP";
	public static final String GROUP_TYPE = "TYPE";
	public static final String GROUP_TYPE_INPUT = "INPUT";
	public static final String GROUP_TYPE_OUTPUT = "OUTPUT";
	public static final String CUSTOM_TRANSFORMATION = "Custom Transformation";
	public static final String FILTER = "Filter";
	public static final String FILTER_CONDITION = "Filter Condition";
	
	public static final String JOINER = "Joiner";
	public static final String FCONDITION = "Condition";
	public static final String JOINER_TYPE = "Join Type";
	public static final String JOINER_CONDITION = "Join Condition";
	
	public static final String LOOKUP_SOURCE_TYPE = "Source Type";
	public static final String Sql_Query = "Sql Query";
	public static final String Sql_Override_Query = "Sql Override Query";
	public static final String Pre_SQL = "Pre SQL";
	public static final String Post_SQL = "Post SQL";
	public static final String FLAT_FILE = "Flat File";
	public static final String LOOKUP_SOURCE_DIR = "Lookup source file directory";
	public static final String LOOKUP_SOURCE_FILE = "Lookup source filename";
	public static final String LOOKUP_CONDITION = "Lookup condition";
	
	public static final String PORTTYPE_OUTPUT = "OUTPUT";
	public static final String PORTTYPE_INPUT = "INPUT";
	public static final String NORMALIZER = "Normalizer";
	public static final String EXPRESSIONTYPE = "EXPRESSIONTYPE";
	public static final String GROUPBY = "GROUPBY";
	public static final String RANKPORT = "RANKPORT";
	public static final String TOP_BOTTOM = "Top/Bottom";
	public static final String NO_OF_RANKS = "Number of Ranks";
	public static final String RANK = "Rank";
	public static final String ROUTER = "Router";
	public static final String SEQUENCE = "Sequence";
	public static final String START_VALUE = "Start Value";
	public static final String INCREMENT_BY = "Increment By";
	public static final String END_VALUE = "End Value";
	public static final String CURRENT_VALUE = "Current Value";
	
	public static final String SORTER = "Sorter";
	public static final String ISSORTKEY = "ISSORTKEY";
	public static final String ISSORTKEY_VALUE = "YES";
	public static final String SORTDIRECTION = "SORTDIRECTION";
	
	public static final String UPDATE_STRATEGY = "Update Strategy";
	public static final String UPDATE_STRATEGY_EXP = "Update Strategy Expression";
	public static final String UPDATE_STRATEGY_FWD = "Forward Rejected Rows";
	
	public static final String STORED_PROCEDURE_NAME = "Stored Procedure Name";
	public static final String CONNECTION_INFORMATION = "Connection Information";
	public static final String STORED_PROCEDURE_TYPE = "Stored Procedure Type";
	public static final String STORED_PROCEDURE = "Stored Procedure";
	
	public static final String SESSION_TAG = "SESSION";
	public static final String MAPPINGNAME = "MAPPINGNAME";
	public static final String SESSTRANSFORMATIONINST = "SESSTRANSFORMATIONINST";
	public static final String TRANSFORMATIONNAME = "TRANSFORMATIONNAME";
	public static final String TRANSFORMATIONTYPE = "TRANSFORMATIONTYPE";
	public static final String Table_Name_Prefix = "Table Name Prefix";
	public static final String Target_Table_Name = "Target Table Name";
	public static final String Shortcut_to_ = "Shortcut_to_";
	public static final String Shortcut_to_1 = "shortcut_to_";
	
	public static final String UPLOAD_TYPE = "U";
	public static final String ZIP_FILE_FORMAT = "zip";
	public static final String TABLE_NAME = "TABLE_NAME";
	public static final String SESSION_NAME = "SESSION_NAME";
	public static final String ODI_XML_PARSING= "ODI XML Parsing";
	public static final String STATUS_IN_PROCESS = "In Process";
	public static final String STATUS_IN_PROGRESS = "In Progress";
	public static final String PARSING_DONE = "Parsing done.";
	public static final String NO_FILES_TO_UPLOAD= "No files to upload";
	public static final String UNSUPPORTED_TECH = "Unsupported technology.";
	
	public static final String EXCEPTION_IN_GETTING_SUMMARY_TABLE = "Exception in getting summary table";
	public static final String EXCEPTION_IN_GETTING_TABLE_DETAILS = "Exception in getting table details";
	public static final String EXCEPTION_WHILE_ODI_XML_PARSING = "Failed to parse ODIXml Files";
	public static final String EXCEPTION_INSERTING_PROJECT_NAME = "Exception in inserting project name";
	public static final String EXCEPTION_GETTING_PROJECT_NAMES = "Exception in getting project names";
	
	public static final String ERROR_GETTING_NODE_DATA = "Error in getting node data.";
	
	public static final String FILES_UPLOAD_SUCCESS = "Files uploaded successfully";
	public static final String PROJECT_NAME_INSERTED = "ProjectName inserted";
	public static final String JOB_EXECUTION_STATUS_FOUND = "Job execution Status found";
	public static final String FILE_LENGTH = "fileLength";
	public static final String LINEAGE_JOB = "lineageJob";
	public static final String JOB_ID = "jobId";
	public static final String PROJECT_NAME = "projectName";
	public static final String SIZE = "size";
	public static final String JOB_STATUS = "jobStatus";
	public static final String FILES_ARRAY = "filesArr";
	public static final String TECH = "tech";
	public static final String COMPLETED_CAPS = "COMPLETED";
	public static final String COMPLETED_SMALL = "completed";
	public static final String FIELD = "Field";
	public static final String DEF_TXT = "DefTxt";
	public static final String IN_PROCESS = "InProcess";
	public static final String NOT_STARTED = "NotStarted";
	public static final String TABLE = "table";
	public static final String SCRIPT = "script";
	public static final String XLSX_EXTENSION = ".xlsx";
	public static final String RECREATE_EXCEL_HEADER_4 = "Functional Module";
	public static final String RECREATE_EXCEL_HEADER_2 = "Scripts / Objects(should be Unique)";
	public static final String RECREATE_EXCEL_HEADER_3 = "Type";
	public static final String RECREATE_EXCEL_HEADER_1 = "Project Name";
	public static final String STARTED = "started";
	public static final String ERROR = "error";
	public static final String STATUS = "status";
	public static final String FUNCTION_TYPE_CREATE = "create";
	public static final String FUNCTION_TYPE_UPLOAD = "upload";
	public static final String[] UPLOAD_STATUS_LIST = {"Deleting Existing Modules Mapping Info",
			"Inserting Nodes/Scripts Info"};
	public static final String[] CREATE_STATUS_LIST = {"Assigning Modules to Nodes/Scripts",
			"Re-calculating Sprint Info"};
	public static final String VOL_IDENTIFICATION = "vol_identification";
	public static final String[] VOL_IDENTIFICATION_STATUS_LIST = {"Data Extraction", "Data Loading"};
	public static final String SIMPLE = "Simple";
	public static final String MEDIUM = "Medium";
	public static final String COMPLEX = "Complex";
	public static final String VERY_COMPLEX = "Very Complex";
	public static final String FILE = "File";
	/**
	 * 	Below arrays are used for complexity calculation in case of no data is present in database
	 *	array[0] - partitionCount; array[1] - indexCount; array[2] - rowCount;
	 *	array[3] - splDataTypeCount - count( 'CLOB' and 'BLOB' );
	 *	array[4] - compressionCount - count( 'Y' ); array[4] - columnNullableCount - count( 'Y' )
	 */
	public static final Integer[] TERADATA_SIMPLE_ARRAY = new Integer[]{5, 5, 100, 0, 1, 1};
	public static final Integer[] TERADATA_MEDIUM_ARRAY = new Integer[]{10, 10, 500, 1, 2, 2};
	public static final Integer[] TERADATA_COMPLEX_ARRAY = new Integer[]{15, 15, 5000, 2, 3, 3};
	public static final Integer[] TERADATA_VERY_COMPLEX_ARRAY = new Integer[]{20, 20, 10001, 3, 4, 4};
	public static final String SUCCESSFULLY = "successfully";
	public static final String CLOB = "CLOB";
	public static final String BLOB = "BLOB";
	public static final String YES = "Y";
	public static final String NO = "N";
	public static final String ONE = "1";
	public static final String TWO = "2";
	public static final String VOL_COMPLEXITY = "vol_complexity";
	public static final String CUSTOM_COMPONENT_COUNT = "custom_count";
	public static final String AGGREGATOR_COMPONENT_COUNT = "aggregate_count";
	public static final String NORMALIZER_COMPONENT_COUNT = "normalizer_count";
	public static final String PROCEDURE_COMPONENT_COUNT = "procedure_count";
	public static final String OTHER_COMPONENT_COUNT = "other_count";
	public static final String SELECT_COUNT = "select_count";
	public static final String INSERT_COUNT = "insert_count";
	public static final String DELETE_COUNT = "delete_count";
	public static final String UPDATE_COUNT = "update_count";
	public static final String CREATE_COUNT = "create_count";
	public static final String MERGE_COUNT = "merge_count";
	public static final String JOIN_COUNT = "join_count";
	public static final String FUNCTION_USED_COUNT = "function_used_count";
	public static final String CONDITION_COUNT = "condition_count";
	public static final String QUERY_COUNT = "query_count";
	public static final String[] VOLUMETRIC_STATUS_LIST = {"Identifying Volumetric Info", "Identifying Complexity"};
	public static final String FUNCTION_TYPE_CALCULATING_SCRIPT = "calculating_sprint_for_objects";
    public static final String MODEL = "model";
	public static final String PROCESSED_FOR_PROJECT_STRING = "Processed data for project";
	public static final String COMPLEXITY_CALCULATION_COMPLETED_STATUS_STRING = "Complexity calculation completed";
    public static final String INVALID_INPUT = "Invalid input. Please input the project name";
	public static final String VOLUMETRIC_ANALYSIS_COMPLETED = "Volumetric analysis completed";
	public static final String XML_PROPERTY_EXTERNAL_ENTITY = "javax.xml.stream.isSupportingExternalEntities";
	public static final List<String> SQL_KEY_WORDS_LIST_FOR_FILE =
			Arrays.asList("';", "select ", "create ", "insert ", "update ", "delete ", "drop ", "truncate ", "merge ");
	public static final List<String> SQL_KEY_WORDS_LIST_FOR_PATH =
			Arrays.asList("select", "create", "insert", "update", "delete", "drop", "truncate", "merge");
	public static final String PROCESS_NOT_STARTED = "Process not started";
	public static final String INTERNAL_ERROR_OCCURRED = "Internal error occurred. Please try after some time";
	public static final String DASHBOARD_WARNING_WITH_ETL_BI_PAGE =
			"There are no records present for this project. Please upload scripts in either ETL/BI Script or Database Scripts page under Configuration";
	public static final String DASHBOARD_WARNING_WITH_APPLICATION_MAPPING_PAGE =
			"There are no records present for this project. Please perform the operations in Application Mapping page under Configuration";
	public static final String DASHBOARD_WARNING_WITH_COLUMN_LINEAGE_PAGE =
			"There are no records present for this project. Please perform the operations in Column Lineage page under Configuration";
	public static final String DASHBOARD_WARNING_WITH_WAVE_PLAN_PAGE =
			"There are no records present for this project. Please perform the operations in Wave Plan page under Configuration";
	public static final String DASHBOARD_WARNING_WITH_DATA_ASSET_MAPPING_PAGE =
			"There are no records present for this project. Please perform the mapping in Data Asset Mapping page under Configuration";
	public static final String DASHBOARD_WARNING_WITH_SPRINT_DETAILS_PAGE =
			"There are no records present for this project. Please generate the sprint for this project under Configuration->Sprint Plan";
	// CSL - Cross System Lineage
	public static final String CSL_GLOBAL_EXCEL_REPORT_PREFIX = "Cross_System_Lineage_Report_";
	public static final String INVALID_INPUT_REQUEST = "Invalid input. Please check the request and try again with valid input";
	public static final String SCRIPT_LINEAGE_COMPLETED_STATUS_STRING = "Script Lineage Completed";
	public static final String TECHNICAL_LINEAGE_REPORT_FULL_PATH_FUNCTION_TYPE = "technical_lineage_report_full_path";
}