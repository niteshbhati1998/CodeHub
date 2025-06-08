package com.cognizant.lineage.upload.model;

import java.util.List;

public class BarChartData {
	private String projectName;
	private List<BarChartDetails> details;
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public List<BarChartDetails> getDetails() {
		return details;
	}
	public void setDetails(List<BarChartDetails> details) {
		this.details = details;
	}
	

}
