package com.cognizant.lineage.database.constants;

public class ImportScriptsQueryConstant {
	
	private ImportScriptsQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}

	public static final String GET_JOB_ID = "select coalesce(max(job_id)+1, 1) from lineage_job";
	
	public static final String INSERT_INTO_ODI_DETAILS = "insert into odi_details(job_id,sqltext,new_sqltext,start_line,end_line,query_type,creation_time,updation_time,filename) " + 
			"values(?,?,?,?,?,?,?,?,?)";
	
	public static final String GET_STEP_NO = "select max(step_no)+1 from lineage_job_status where job_id=?";
	
	public static final String INSERT_INTO_LINEAGE_JOB = "insert into lineage_job(job_id,project_name,parent_technology,technology,upload_type,uploaded_dir) " + 
			"values(?,?,?,?,?,?)";
	
	public static final String UPDATE_LINEAGE_JOB = "update lineage_job set start_time = ?, end_time=? where job_id=?";
	
    public static final String INSERT_INTO_LINEAGE_JOB_STATUS = "insert into lineage_job_status(job_id,step_no,step_name,no_of_file_received,no_of_file_processed,status,log_file_location) " + 
    		"values(?,?,?,?,?,?,?)";

	public static final String UPDATE_LINEAGE_JOB_STATUS = "update lineage_job_status "
			+ "set no_of_file_processed = ?, status = ? where job_id=? and step_no=?";

	public static final String UPDATE_LINEAGE_JOB_STATUS_FOR_ERROR_OR_COMPLETED = "update lineage_job_status "+
    		"set status = ? where job_id=? and step_no=?";
}