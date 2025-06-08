package com.cognizant.lineage.upload.model;

import com.cognizant.lineage.util.Sanitization;

public class TableRequestModel {

	private String tableName;
	private String projectName;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = Sanitization.sanitizeInput(tableName);
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = Sanitization.sanitizeInput(projectName);
	}

}
