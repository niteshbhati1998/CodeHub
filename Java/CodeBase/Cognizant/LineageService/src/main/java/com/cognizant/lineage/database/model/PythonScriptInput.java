package com.cognizant.lineage.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PythonScriptInput {
	private String projectName;
	private String nodeName;
	private String direction;
	private int pathLimit;
	@JsonProperty("report_type")
	private String reportType;
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public int getPathLimit() {
		return pathLimit;
	}
	public void setPathLimit(int pathLimit) {
		this.pathLimit = pathLimit;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
}
