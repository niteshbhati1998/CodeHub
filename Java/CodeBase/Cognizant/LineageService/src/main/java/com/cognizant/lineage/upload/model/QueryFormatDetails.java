package com.cognizant.lineage.upload.model;

public class QueryFormatDetails {

	private String functionName;
	private int startArgument;
	private int endArgument;
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	public int getStartArgument() {
		return startArgument;
	}
	public void setStartArgument(int startArgument) {
		this.startArgument = startArgument;
	}
	public int getEndArgument() {
		return endArgument;
	}
	public void setEndArgument(int endArgument) {
		this.endArgument = endArgument;
	}
	
}
