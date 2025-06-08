package com.cognizant.lineage.upload.model;

import java.util.List;
import java.util.Map;

public class NestedPiDetails {
	
	private String projectName;
	private Map<String, Map<String, List<NestedPiCount>>> modules;
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Map<String, Map<String, List<NestedPiCount>>> getModules() {
		return modules;
	}
	public void setModules(Map<String, Map<String, List<NestedPiCount>>> modules) {
		this.modules = modules;
	}
}
