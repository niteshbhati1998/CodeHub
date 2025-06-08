package com.cognizant.lineage.upload.model;

public class ImportSummary {

	private String dbObjectType;
	private int totalFiles;
	private int processedFiles;
	private String status;
	private String logInfo;
	private String outputDirectory;
	public String getDbObjectType() {
		return dbObjectType;
	}
	public void setDbObjectType(String dbObjectType) {
		this.dbObjectType = dbObjectType;
	}
	public int getTotalFiles() {
		return totalFiles;
	}
	public void setTotalFiles(int totalFiles) {
		this.totalFiles = totalFiles;
	}
	public int getProcessedFiles() {
		return processedFiles;
	}
	public void setProcessedFiles(int processedFiles) {
		this.processedFiles = processedFiles;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getLogInfo() {
		return logInfo;
	}
	public void setLogInfo(String logInfo) {
		this.logInfo = logInfo;
	}
	public String getOutputDirectory() {
		return outputDirectory;
	}
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
}
