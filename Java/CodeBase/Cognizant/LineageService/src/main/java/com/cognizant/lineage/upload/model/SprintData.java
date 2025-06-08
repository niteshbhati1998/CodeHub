package com.cognizant.lineage.upload.model;

import java.util.List;

public class SprintData {
	
	private String module;
	private String projectName;
	private List<String> technology;
	
	public List<String> getTechnology() {
		return technology;
	}
	public void setTechnology(List<String> technology) {
		this.technology = technology;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}	

}