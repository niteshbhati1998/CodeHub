package com.cognizant.lineage.upload.model;

public class TableauParsing {

	//id,project_name,report_name,column_name,not_real_table_name,real_table_name,filename,tech
	private int jobId;
	private String projectName;
	private String reportName;
	private String columnName;
	private String notRealName;
	private String realName;
	private String jobName;
	private String jobType;
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getNotRealName() {
		return notRealName;
	}
	public void setNotRealName(String notRealName) {
		this.notRealName = notRealName;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	
}
