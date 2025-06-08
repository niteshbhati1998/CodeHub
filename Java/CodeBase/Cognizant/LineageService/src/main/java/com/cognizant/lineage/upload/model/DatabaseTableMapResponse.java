package com.cognizant.lineage.upload.model;

import java.util.List;

public class DatabaseTableMapResponse {

	private String databaseName;
	private List<String> tableName;

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public List<String> getTableName() {
		return tableName;
	}

	public void setTableName(List<String> tableName) {
		this.tableName = tableName;
	}

}
