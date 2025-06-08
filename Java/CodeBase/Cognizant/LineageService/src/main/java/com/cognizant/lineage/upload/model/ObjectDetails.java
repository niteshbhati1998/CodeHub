package com.cognizant.lineage.upload.model;

public class ObjectDetails {
	
	private Object tables = 0;
	private Object scripts = 0;
	private Object dbSize = 0;
	public Object getTables() {
		return tables;
	}
	public void setTables(Object tables) {
		this.tables = tables;
	}
	public Object getScripts() {
		return scripts;
	}
	public void setScripts(Object scripts) {
		this.scripts = scripts;
	}
	public Object getDbSize() {
		return dbSize;
	}
	public void setDbSize(Object dbSize) {
		this.dbSize = dbSize;
	}
}
