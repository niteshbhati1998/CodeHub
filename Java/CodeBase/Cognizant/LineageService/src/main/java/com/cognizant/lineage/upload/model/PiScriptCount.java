package com.cognizant.lineage.upload.model;

public class PiScriptCount {
	
	private String technology;
	private Object scriptCount;
	
	public String getTechnology() {
		return technology;
	}
	public void setTechnology(String technology) {
		this.technology = technology;
	}
	public Object getScriptCount() {
		return scriptCount;
	}
	public void setScriptCount(Object scriptCount) {
		this.scriptCount = scriptCount;
	}
	public PiScriptCount() {
		super();
	}
	public PiScriptCount(String technology, Object scriptCount) {
		super();
		this.technology = technology;
		this.scriptCount = scriptCount;
	}
}