package com.cognizant.lineage.upload.model;

public class PiChartDetailsForTableFileView {
	
	private String database; 
	private String type;
	private Object count;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Object getCount() {
		return count;
	}
	public void setCount(Object count) {
		this.count = count;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}	
	
}
