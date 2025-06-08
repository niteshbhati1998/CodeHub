package com.cognizant.lineage.database.model;

public class DbScriptDetails {

	private int jobId;
	private String projectName;
	private String databaseType;
	private String databaseObjectType;
	private String location;
	private String uploadType;
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
	public String getDatabaseType() {
		return databaseType;
	}
	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
	public String getDatabaseObjectType() {
		return databaseObjectType;
	}
	public void setDatabaseObjectType(String databaseObjectType) {
		this.databaseObjectType = databaseObjectType;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getUploadType() {
		return uploadType;
	}
	public void setUploadType(String uploadType) {
		this.uploadType = uploadType;
	}
}
