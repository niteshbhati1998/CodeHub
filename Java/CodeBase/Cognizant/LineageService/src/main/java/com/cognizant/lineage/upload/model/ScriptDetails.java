package com.cognizant.lineage.upload.model;

import java.util.List;

public class ScriptDetails {
	
	private String name;
	private String complexity;
	private List<String> table;
	private String sprint;
	private String script;
	private Object noOfLines = 0;
	
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
	public List<String> getTable() {
		return table;
	}
	public void setTable(List<String> table) {
		this.table = table;
	}
	public String getSprint() {
		return sprint;
	}
	public void setSprint(String sprint) {
		this.sprint = sprint;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	public Object getNoOfLines() {
		return noOfLines;
	}
	public void setNoOfLines(Object noOfLines) {
		this.noOfLines = noOfLines;
	}

}
