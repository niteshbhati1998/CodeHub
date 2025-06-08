package com.cognizant.lineage.upload.model;

public class SingleComponentDetails {

	private String fileName;
	private String folderName;
	private String mappingName;
	private String instanceName;
	private String transformationName;
	private String tableName;
	private String Databasetype;
	private String Dbdname;
	private String componentType;
	private String sqlQuery;
	private String sessionName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getMappingName() {
		return mappingName;
	}

	public void setMappingName(String mappingName) {
		this.mappingName = mappingName;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getTransformationName() {
		return transformationName;
	}

	public void setTransformationName(String transformationName) {
		this.transformationName = transformationName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDatabasetype() {
		return Databasetype;
	}

	public void setDatabasetype(String databasetype) {
		Databasetype = databasetype;
	}

	public String getDbdname() {
		return Dbdname;
	}

	public void setDbdname(String dbdname) {
		Dbdname = dbdname;
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	@Override
	public String toString() {
		return "SingleComponentDetails [fileName=" + fileName + ", folderName=" + folderName + ", mappingName="
				+ mappingName + ", instanceName=" + instanceName + ", transformationName=" + transformationName
				+ ", tableName=" + tableName + ", Databasetype=" + Databasetype + ", Dbdname=" + Dbdname
				+ ", componentType=" + componentType + ", sqlQuery=" + sqlQuery + ", sessionName=" + sessionName + "]";
	}

}
