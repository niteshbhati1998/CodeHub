package com.cognizant.lineage.upload.constants;

public class TableauQueryConstant {
	private TableauQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}	
	public static final String LINEAGE_EXECUTION_ID_SEQ = "lineage_job_status_seq";
	
	public static final String INSERT_INTO_TABLEAU_PARSING = "insert into tableau_parsing_component(job_id,project_name,"
			+ "report_name, workbook_name, column_name, not_real_table_name, real_table_name, filename, tech,status) "
			+ "values(?,?,?,?,?,?,?,?,?,?)";
	
    public static final String BTEQ_LENEAGE_EXECUTION_NEXT_SEQUENCE_ID = "select nextval('"+LINEAGE_EXECUTION_ID_SEQ+"');";
	
	public static final String GET_STEP_NO = "select max(step_no)+1 from lineage_job_status where job_id=?";

	public static final String REPORT__ = "REPORT__";
    
}