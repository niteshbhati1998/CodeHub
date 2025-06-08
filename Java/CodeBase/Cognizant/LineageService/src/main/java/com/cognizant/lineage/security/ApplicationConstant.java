package com.cognizant.lineage.security;

public class ApplicationConstant {
    public static final int MAXUSER = 100;
    public static final String MAXUSER_MSG = "Maximum user quota reached. Please contact support team to increase user quota.";
    public static final String APPLICATION_LICENSE_EXPIRY_DATE = "12/31/2024 11:55 PM";
    public static final String APPLICATION_LICENSE_EXPIRY_DATE_FORMAT = "MM/dd/yyyy hh:mm a";
    public static final String APPLICATION_LICENSE_EXPIRY_MSG = "Application license expired. Please contact support team.";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String CRLF = "CRLF";
    public static final String LF = "LF";
    public static final String PASS = "PASS";
    public static final String FAIL = "FAIL";
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final String WIP = "WIP";
    public static final String ROW_ID = "ROW_ID";

    public static final String DISCOVERY = "discovery";
    public static final String ADVANCED_DOMAIN_NAME = "ADVANCED";
    public static final String ADVANCED = "advanced";

    public static final String CUSTOMIZED_RULE = "Customized Rule";
    public static final String CUSTOMIZED = "customized";
    public static final String REGEX_VALIDATION_RULE = "RegEx Validation Rule";
    public static final String REGEX = "regex";
    public static final String REFERENCE_DATA_VALIDATION = "Reference Data Validation";
    public static final String REFERENCE = "reference";
    public static final String RULE_NAME_EXIST = "Rule Name is already used. Please provide different rule name.";
    public static final String INTERNAL_SERVER_ERROR = "Internal server Error. Please check the log for more.";
    public static final String NO_DATA_FOUND = "No data found.";
    public static final String INVALID_CUSTOM_RULE_TYPE = "Invalid custom rule type.";
    public static final String FILE_READING_START = "Source file reading started";
    public static final String FILE_READING_COMPLETED = "Source file reading completed";
    public static final String FILE_READING_ERROR = "Source file reading getting error";
    public static final String FILE_READING_STEP = "Step 1";
    public static final String INPROCESS = "INPROCESS";
    public static final String DRILLDOWNSTEP = "Step 2";
    public static final String DRILLDOWNSTEP_START = "DrillDown process started";
    public static final String DRILLDOWNSTEP_COMPLETED = "DrillDown process completed";
    public static final String DRILLDOWNSTEP_ERROR = "Error in DrillDown process";
    public static final String PROCESS_DISCOVERY_STEP = "Step 3";
    public static final String PROCESS_DISCOVERY_START = "Discovery process started";
    public static final String PROCESS_DISCOVERY_COMPLETED = "Discovery process completed";
    public static final String PROCESS_DISCOVERY_ERROR = "Error in Discovery process";
    public static final String PROCESS_DISCOVERY_MAPPING_NOTFOUND = "Discovery mapping not found";
    public static final String ADVANCED_RULE_MAPPING_STEP = "Step 4";
    public static final String ADVANCED_RULE_MAPPING_START = "Advanced rule process started";
    public static final String ADVANCED_RULE_MAPPING_COMPLETED = "Advanced rule process completed";
    public static final String ADVANCED_RULE_PASS_FAIL_DATA = "Rules validation completed, pass and fail data loading started";
    public static final String ADVANCED_RULE_MAPPING_ERROR = "Error in Advanced rule process";
    public static final String ADVANCED_RULE_MAPPING_NOT_FOUND = "Advanced mapping not found";
    public static final String EXCEL_GENERATE_STEP = "Step 6";
    public static final String EXCEL_GENERATE_START = "Excel creation process started";
    public static final String EXCEL_GENERATE_COMPLETED = "Excel creation process completed";
    public static final String EXCEL_GENERATE_ERROR = "Error in Excel creation process";
    public static final String PROCESSED = "PROCESSED";
    public static final String NOT_PROCESSED = "NOTPROCESSED";
    public static final String UPDATEFILE_STEP = "Step 5";
    public static final String UPDATEFILE_START = "File processing status updatation started";
    public static final String UPDATEFILE_COMPLETED = "File processing status updated";
    public static final String UPDATEFILE_ERROR = "Error in file processing status update";
    public static final String DUPLICATE_PYTHON_SCRIPT_STEP = "Step 7";
    public static final String DUPLICATE_PYTHON_SCRIPT_START = "Duplicate Record identification started";
    public static final String DUPLICATE_PYTHON_SCRIPT_COMPLETED = "Duplicate Record identification completed";
    public static final String DUPLICATE_PYTHON_SCRIPT_ERROR = "Error in duplicate Record identification";

    public static final String HIERARCHICAL_RELATIONSHIP_ERROR = "Error in hierarchical relationship";

    public static final String ATTRIBUTE_PROFILE_PYTHON_SCRIPT_CLASSIFICATION_STEP = "Step 8";
    public static final String ATTRIBUTE_PROFILE_PYTHON_SCRIPT_CLASSIFICATION_STEP_START = "Attribute classification started";

    public static final String ATTRIBUTE_PROFILE_PYTHON_SCRIPT_GLOSSARY_STEP = "Step 9";
    public static final String ATTRIBUTE_PROFILE_PYTHON_SCRIPT_GLOSSARY_START = "Attribute business glossary started";

    public static final String ATTRIBUTE_PROFILE_PYTHON_SCRIPT_COMPLETED = "Attribute profile  python script completed";
    public static final String ATTRIBUTE_PROFILE_PYTHON_SCRIPT_ERROR = "Error in attribute profile python script status update";

    public static final String NOT_STARTED = "NOT STARTED";
    public static final String ATTRIBUTE_WISE_ANALYSIS_STEP = "Step 10";
    public static final String ATTRIBUTE_WISE_ANALYSIS_AND_LOAD_START = "Attribute Wise analysis and data load started";
    public static final String ATTRIBUTE_WISE_ANALYSIS_AND_LOAD_COMPLETED = "Attribute Wise analysis and data load completed";
    public static final String ATTRIBUTE_WISE_ANALYSIS_AND_LOAD_ERROR = "Error in Attribute Wise analysis and data load";

    public static final String ATTRIBUTE_WISE_ANOMALY_STEP = "Step 11";
    public static final String ATTRIBUTE_WISE_ANOMALY_START = "Attribute Wise anomaly detection number and date started";
    public static final String ATTRIBUTE_WISE_ANOMALY_COMPLETED = "Attribute Wise anomaly detection number and date completed";
    public static final String ATTRIBUTE_WISE_ANOMALY_ERROR = "Error in Attribute Wise anomaly detection number and date";


    public static final String EXCEL_ATTRIBUTE_WISE_ANOMALY_STEP = "Step 12";
    public static final String EXCEL_ATTRIBUTE_WISE_ANOMALY_START = "Attribute Wise anomaly detection Excel tab update";
    public static final String EXCEL_ATTRIBUTE_WISE_ANOMALY_COMPLETED = "Attribute Wise anomaly detection Excel tab update completed";
    public static final String EXCEL_ATTRIBUTE_WISE_ANOMALY_ERROR = "Error in Attribute Wise anomaly detection Excel tab ";

    public static final String BASIC_PROFILING_START = "Basic profiling started";
    public static final String BASIC_PROFILING_COMPLETED = "Basic profiling completed";
    public static final String TABLE_READING_START = "Source table reading started";
    public static final String TABLE_READING_COMPLETED = "Source table reading completed";
    public static final String TABLE_READING_ERROR = "Source table reading getting error";


    public static final String TREND_EDA_ANALYSIS_ERROR = "Error in Trend And EDA Analysis Python Script ";
    public static final String NUMBER = "NUMBER";
    public static final String DATE = "DATE";
    public static final String STRING = "STRING";
    public static final String STRONG = "strong";
    public static final String WEAK = "weak";
    public static final String INCOMPLETE = "incomplete";
    public static final String NO_ANOMALY = "no-anomaly";
    public static final String NO_VALUE = "no-value";

    public static final String PATTERN_LEVEL = "patternLevel";
    public static final String MASK_LEVEL = "maskLevel";
    public static final String VALUE_LEVEL = "valueLevel";
    public static final String FORMAT = "CSV";
    public static final String ENTITY_TYPE_DELIMITED = "Delimited";
    public static final String ENTITY_TYPE_SERVER = "Server";
    public static final String LOCAL_FILE = "localFile";
    public static final String SERVER_FILE = "serverFile";
    public static final String ENTITY_TYPE_DB_TABLE = "DB Table";
    public static final String ENTITY_AND_ATTRIBUTE_PROFILING_EXECUTION_ID = "ENTITY_AND_ATTRIBUTE_PROFILING_EXECUTION_ID";
    public static final String DRILL_DOWN_EXECUTION_ID = "DRILL_DOWN_EXECUTION_ID";
    public static final String DATA_DISCOVERY_EXECUTION_ID = "DATA_DISCOVERY_EXECUTION_ID";
    public static final String ADVANCED_RULE_EXECUTION_ID = "ADVANCED_RULE_EXECUTION_ID";
    public static final String DUPLICATE_EXECUTION_ID = "DUPLICATE_EXECUTION_ID";
    public static final String ANOMALY_EXECUTION_ID = "ANOMALY_EXECUTION_ID";
    public static final String DATA_CLASSIFICATION_EXECUTION_ID = "DATA_CLASSIFICATION_EXECUTION_ID";
    public static final String DATA_DOMAIN_EXECUTION_ID = "DATA_DOMAIN_EXECUTION_ID";
    public static final String BUSINESS_GLOSSARY_EXECUTION_ID = "BUSINESS_GLOSSARY_EXECUTION_ID";
    public static final String SQL_RULE_EXECUTION_ID = "SQL_RULE_EXECUTION_ID";
    public static final String SQL_ANOMALY_RULE_EXECUTION_ID = "SQL_ANOMALY_RULE_EXECUTION_ID";
    public static final String DATA_CLASSIFICATION_LOGICAL_EXECUTION_ID = "DATA_CLASSIFICATION_LOGICAL_EXECUTION_ID";

    public static final String TASK_DATA_LOAD = "DATA LOAD";
    public static final String TASK_ENTITY_AND_ATTRIBUTE_PROFILING = "ENTITY AND ATTRIBUTE_PROFILING";
    public static final String TASK_DRILL_DOWN = "PATTERN, MASK AND VALUE LEVEL DRILL DOWN";
    public static final String TASK_DATA_DISCOVERY = "DATA DISCOVERY";
    public static final String TASK_ADVANCED_RULE = "ADVANCED RULES";
    public static final String TASK_DUPLICATE = "DUPLICATE_DETECTION";
    public static final String TASK_ANOMALY = "ANOMALY DETECTION";
    public static final String TASK_ANOMALY_GEN_AI = "GEN AI ANOMALY DETECTION";
    public static final String TASK_DATA_CLASSIFICATION = "DATA CLASSIFICATION";
    public static final String TASK_DATA_DOMAIN = "DATA DOMAIN";
    public static final String TASK_RULE_RECOMMADATION = "RULE RECOMMADATION";
    public static final String TASK_BUSINESS_GLOSSARY = "BUSINESS GLOSSARY";
    public static final String TASK_SQL_RULE = "SQL RULES";
    public static final String TASK_NO_VALUE_ANOMALY_PROFILING = "NO VALUE ANOMALY DETECTION";
    public static final String TASK_HIERARCHICAL_RELATIONSHIP= "HIERARCHICAL_RELATIONSHIP";
    public static final String TASK_TREND_EDA_ANALYSIS = "TREND_EDA_ANALYSIS";
    public static final String TASK_DATA_COMPARISON = "DATA_COMPARISON";
    public static final String TASK_SQL_BASED_ANOMALY_RULE = "SQL BASED ANOMALY RULES";
    public static final String TASK_DATA_CLASSIFICATION_LOGICAL = "DATA CLASSIFICATION LOGICAL";
    public static final String TASK_STATISTICS_INSIGHTS = "STATISTICS INSIGHTS";

    public static final String RULE_NAME_EXIST_BATCH = "The rule name is already in use. To ensure uniqueness,"
            + " a current timestamp has been appended to the rule name";
    public static final String TASK_ANOMALY_GENERATE_EXCEL = "GENERATE_EXCEL";

    public static final String NOT_CONFIGURED = "NOT CONFIGURED";

    public static final String TABLE_PK = "PK";
    public static final String TABLE_FK = "FK";
    public static final String NO_NULL_VALUES = "No Null Values";
    public static final String NO_ANOMALY_VALUES = "No Anomalies";
    public static final String SQL_RULE_WITH_NO_RECORD = "Sql rule does not return any record";
    public static final String EXACT = "EXACT";
    public static final String SOUNDEX = "SOUNDEX";
    public static final String FUZZY = "FUZZY";
    public static final String NOT_AVAILABLE = "N/A";

    public static final String USER_ROLE_ADMIN = "ADMIN";
    public static final String USER_ROLE_USER = "USER";
    public static final String USER_ROLE_PSEUDO_ADMIN = "PSEUDO-ADMIN";
    public static final String USER_ROLE_READ_ONLY_USER = "READ-ONLY-USER";
    public static final String USER_STATUS_ACTIVE = "ACTIVE";
    public static final String USER_STATUS_INACTIVE = "INACTIVE";
    public static final String LICENSE_VALIDATOR_YES = "YES";
    public static final String USER_CREATED_BY_SYSTEM = "SYSTEM";
    public static final String ADVANCED_ATTRIBUTES_RULE_PASS_FAIL_DRILLDOWN = "ADVANCED_ATTRIBUTES_RULE_PASS_FAIL_DRILLDOWN";
    public static final String DISCOVERY_ATTRIBUTES_RULE_PASS_FAIL_DRILLDOWN = "DISCOVERY_ATTRIBUTES_RULE_PASS_FAIL_DRILLDOWN";
    public static final String ADVANCED_SQL_RULE_PASS_FAIL_DRILLDOWN = "ADVANCED_SQL_RULE_PASS_FAIL_DRILLDOWN";
    public static final String ADVANCED_SQL_RULE_FAIL_START_QUERY = "SELECT * FROM ";
    public static final String ATTRIBUTE_NO_VALUE_ANOMALY = "ATTRIBUTE_NO_VALUE_ANOMALY";
    public static final String ADVANCED_ANOMALY_SQL_RULE_FAIL_START_QUERY = "SELECT *, CASE <WRITE WHEN CLAUSE> END AS ";
    public static final String ADVANCED_ANOMALY_SQL_RULE_TABLE_WHERE_CLAUSE = " <WRITE WHERE CLAUSE IF REQUIRE> ";
    public static final String SQL_RULE_BASED_ANOMALY = "SQL_RULE_BASED_ANOMALY";
    public static final String SQL_RULE_BASED_ANOMALY_FLAG = "anomaly_flag_";
    public static final String ACCURACY_WITH_REFERENCE = "accuracy_with_reference";
    public static final String INTEGRITY_WITH_REFERENCE = "integrity_with_reference";
    public static final String DIMENSION_ACCURACY = "accuracy";
    public static final String DIMENSION_INTEGRITY = "integrity";
    public static final String DIMENSION_CONFORMITY = "conformity";
    public static final String DIMENSION_CONSISTENCY = "consistency";
    public static final String CONFORMITY_WITH_REFERENCE = "conformity_with_reference";
    public static final String COMPLETED = "COMPLETED";
    public static final String DIMENSION_COMPLETENESS = "completeness";
    public static final String DIMENSION_UNIQUENESS = "uniqueness";

}