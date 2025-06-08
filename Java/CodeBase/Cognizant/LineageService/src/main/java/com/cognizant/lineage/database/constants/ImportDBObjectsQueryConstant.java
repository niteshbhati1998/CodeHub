package com.cognizant.lineage.database.constants;

public class ImportDBObjectsQueryConstant {
	
	private ImportDBObjectsQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}
	//Endpoint: ExtractButton
	public static final String GET_JOB_ID = "select coalesce(max(job_id)+1, 1) from lineage_job";
	
	public static final String SAVE_DATABASE_OBJECT_TYPE_DETAILS = "insert into lineage_job(job_id,project_name,parent_technology,technology,upload_type,uploaded_dir,connection_name) " + 
			"values(?,?,?,?,?,?,?)";
}
