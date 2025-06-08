package com.cognizant.lineage.upload.model;

import java.util.List;

public class TableDetails {
	
	private String name;
	private String complexity;
	private String table;
	private String technology;
	private String sprint;
	private List<String> script;
	private Object dbSize = 0;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getComplexity() {
		return complexity;
	}
	public void setComplexity(String complexity) {
		this.complexity = complexity;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getSprint() {
		return sprint;
	}
	public void setSprint(String sprint) {
		this.sprint = sprint;
	}
	public List<String> getScript() {
		return script;
	}
	public void setScript(List<String> script) {
		this.script = script;
	}
	public Object getDbSize() {
		return dbSize;
	}
	public void setDbSize(Object dbSize) {
		this.dbSize = dbSize;
	}
	public String getTechnology() {
		return technology;
	}
	public void setTechnology(String technology) {
		this.technology = technology;
	}
	

}
