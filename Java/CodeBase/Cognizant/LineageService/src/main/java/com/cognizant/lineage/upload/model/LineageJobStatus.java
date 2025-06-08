package com.cognizant.lineage.upload.model;

public class LineageJobStatus {

	private int totalRecords;
	private int processedRecords;
	private String status;
	private String logFileLocation;
	public int getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
	public int getProcessedRecords() {
		return processedRecords;
	}
	public void setProcessedRecords(int processedRecords) {
		this.processedRecords = processedRecords;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getLogFileLocation() {
		return logFileLocation;
	}
	public void setLogFileLocation(String logFileLocation) {
		this.logFileLocation = logFileLocation;
	}
	
}
