package com.cognizant.lineage.upload.model;

public class FinalStatus {

	private int recordsCount;
	private String logLocation;
	
	public FinalStatus(){
		this.recordsCount=0;
		this.logLocation="";
	}
	
	public int getRecordsCount() {
		return recordsCount;
	}

	public void setRecordsCount(int recordsCount) {
		this.recordsCount = recordsCount;
	}

	public String getLogLocation() {
		return logLocation;
	}

	public void setLogLocation(String logLocation) {
		this.logLocation = logLocation;
	}
	
	@Override
	public String toString() {
		return recordsCount+", "+logLocation;
	}

}
