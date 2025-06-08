package com.cognizant.lineage.upload.constants;

public class DataStageQueryConstant {
	private DataStageQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}
	
	//Status Update
	public static final String GET_JOB_ID = "select max(job_id)+1 from lineage_job";
	
	public static final String INSERT_INTO_LINEAGE_JOB = "insert into lineage_job(job_id,project_name,parent_technology,technology,upload_type,uploaded_dir) " + 
			"values(?,?,?,?,?,?)";
	
	public static final String GET_STEP_NO = "select max(step_no)+1 from lineage_job_status where job_id=?";

	public static final String INSERT_INTO_LINEAGE_JOB_STATUS = "insert into lineage_job_status(job_id,step_no,step_name,no_of_file_received,no_of_file_processed,status,log_file_location) " + 
    		"values(?,?,?,?,?,?,?)";

	public static final String UPDATE_LINEAGE_JOB_STATUS = "update lineage_job_status "
			+ "set no_of_file_processed = ?, status = ? where job_id=? and step_no=?";

	public static final String UPDATE_LINEAGE_JOB_STATUS_FOR_ERROR_OR_COMPLETED = "update lineage_job_status "+
    		"set status = ? where job_id=? and step_no=?";
	
	//Parsing: Part-1
	public static final String INSERT_INTO_DATASTAGE_COMPONENT_LEVEL_LINEAGE ="insert into datastage_component_level_lineage(job_id,item,tablename_or_query,identifier,inputpins,outputpins,partner,lookup,jobname) " 
		     + "values (?,?,?,?,?,?,?,?,?)";	
	
	public static final String UPDATE_DATASTAGE_COMPONENT_LEVEL_LINEAGE = "update datastage_component_level_lineage " + 
			"set item = tablename_or_query " + 
			"where (tablename_or_query!=null or tablename_or_query!='') and job_id=?";
	
	//Parsing: Part-2
	public static final String INSERT_DATA_INTO_DATASTAGE_TABLE_LEVEL_LINEAGE = "insert into "
			+ "datastage_table_level_lineage (job_id,from_table,to_table,query,job_name) " + "values(?,?,?,?,?)";

	public static final String GET_DATASTAGE_COMPONENT_LEVEL_LINEAGE_DETAILS_FOR_TARGET = "select item,identifier,inputpins,outputpins, "
			+ "partner,lookup,jobname from ( " + "select item,identifier,inputpins,outputpins, "
			+ "partner,lookup,jobname from datastage_component_level_lineage "
			+ "where  identifier not in( "
			+ "select identifier from (SELECT item,identifier,unnest(string_to_array(inputpins, '|')) as inputpins,outputpins,partner,lookup,jobname "
			+ "FROM datastage_component_level_lineage where job_id=?)a) and job_id=?" + " union "
			+ "SELECT item,identifier,unnest(string_to_array(inputpins, '|')) as inputpins,outputpins,partner,lookup,jobname "
			+ "FROM datastage_component_level_lineage where job_id=?) a where  (inputpins is not null and inputpins !='') and (outputpins is null or outputpins = '') "
			+ "and (partner is null or partner = '')";
	
	public static final String GET_DATASTAGE_COMPONENT_LEVEL_LINEAGE_DETAILS = "select distinct item,identifier,inputpins,outputpins, "
			+ "partner,lookup,jobname from datastage_component_level_lineage "
			+ "where jobname=? and job_id=? and identifier not in( "
			+ "select identifier from (SELECT item,identifier,unnest(string_to_array(inputpins, '|')) as inputpins,outputpins,partner,lookup,jobname "
			+ "FROM datastage_component_level_lineage where jobname = ? and job_id=?)a) " + " union "
			+ "SELECT item,identifier,unnest(string_to_array(inputpins, '|')) as inputpins,outputpins,partner,lookup,jobname "
			+ "FROM datastage_component_level_lineage where jobname = ? and job_id=?";
		
	//Python script
	public static final String GET_COUNT_FROM_DATASTAGE_TABLE_LEVEL_LINEAGE = "select count(query) from datastage_table_level_lineage where query=? and job_id=?";

    //complexity-calculation
	public static final String GET_SCRIPT_CALCULATION_DETAILS = "select complexity,function_used_count,join_count,select_count,other_component_count from script_calculation_details " + 
			"where upper(technology) = 'DATASTAGE' order by id";
	
	public static final String GET_COUNT_FROM_DATASTAGE_TABLE_LEVEL_LINEAGE_JOBNAME_SPECIFIC = "select count(query) from datastage_table_level_lineage where query=? and job_id=? and job_name=?";

	public static final String GET_TRANSFORMATION_COUNT = "select count(item) " + 
			"from datastage_component_level_lineage " + 
			"where trim(partner)='' " + 
			"and (trim(inputpins)!= '' and trim(outputpins) != '') " + 
			"and jobname = ? " + 
			"and job_id = ?";
	
	public static final String GET_QUERY_IF_TYPE_IS_YES = "select tablename_or_query from datastage_component_level_lineage " +
			"where jobname = ? and job_id=? " + 
			"and trim(tablename_or_query)!=''";
	
	public static final String GET_FUNCTION_LIST = "select function_list from function_list";
	
	public static final String INSERT_INTO_SCRIPT_COMPLEXITY = "insert into script_complexity(job_id,project_name,technology,file_name,function_used_count,join_types,select_count,other_component_count,complexity,status) " + 
			"values(?,?,?,?,?,?,?,?,?,?)";
	
	public static final String GET_LINEAGE_JOB_STATUS = "select job_status_details from lineage_job_status where job_id=? and step_no=?";
	
	public static final String UPDATE_LINEAGE_JOB_STATUS_DETAILS = "update lineage_job_status set job_status_details=? where job_id=? and step_no=?";
}