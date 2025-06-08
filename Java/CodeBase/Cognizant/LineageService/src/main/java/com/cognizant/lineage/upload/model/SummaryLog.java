package com.cognizant.lineage.upload.model;

public class SummaryLog {

	private String databaseObjectType;
	private int filesExtracted;
	private int totalFiles;
	private String logFileLocation;
	private String scriptStatus;
	public String getDatabaseObjectType() {
		return databaseObjectType;
	}
	public void setDatabaseObjectType(String databaseObjectType) {
		this.databaseObjectType = databaseObjectType;
	}
	public int getFilesExtracted() {
		return filesExtracted;
	}
	public void setFilesExtracted(int filesExtracted) {
		this.filesExtracted = filesExtracted;
	}
	public int getTotalFiles() {
		return totalFiles;
	}
	public void setTotalFiles(int totalFiles) {
		this.totalFiles = totalFiles;
	}
	public String getLogFileLocation() {
		return logFileLocation;
	}
	public void setLogFileLocation(String logFileLocation) {
		this.logFileLocation = logFileLocation;
	}
	public String getScriptStatus() {
		return scriptStatus;
	}
	public void setScriptStatus(String scriptStatus) {
		this.scriptStatus = scriptStatus;
	}
	
}
