package com.cognizant.lineage.upload.model;

import java.util.List;

public class Project {
	
	private String projectName;
	private List<String> modules;
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public List<String> getModules() {
		return modules;
	}
	public void setModules(List<String> modules) {
		this.modules = modules;
	}
	

}
