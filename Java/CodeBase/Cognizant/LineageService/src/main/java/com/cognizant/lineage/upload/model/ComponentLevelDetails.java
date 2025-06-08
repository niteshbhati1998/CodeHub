package com.cognizant.lineage.upload.model;

import java.util.ArrayList;
import java.util.List;

public class ComponentLevelDetails {
	private String fileName;
	private String folderName;
	private String mappingName;
	private String fromComponent;
	private String fromComponentType;
	private String toComponent;
	private String toComponentType;
	private String sourceDatabaseType;
	private String sourceDbName;
	private String targetDatabaseType;
	private String targetDbName;
	private String targetTableName;
	private String sourceTableName;
	private String SqlQuery;
	private List<String> preSql = new ArrayList<>();
	private List<String> postSql = new ArrayList<>();
	private Integer executionId;
	private String sessionName;

	public List<String> getPreSql() {
		return preSql;
	}

	public void setPreSql(List<String> preSql) {
		this.preSql = preSql;
	}

	public List<String> getPostSql() {
		return postSql;
	}

	public void setPostSql(List<String> postSql) {
		this.postSql = postSql;
	}

	public Integer getExecutionId() {
		return executionId;
	}

	public void setExecutionId(Integer executionId) {
		this.executionId = executionId;
	}

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

	public String getFromComponent() {
		return fromComponent;
	}

	public void setFromComponent(String fromComponent) {
		this.fromComponent = fromComponent;
	}

	public String getFromComponentType() {
		return fromComponentType;
	}

	public void setFromComponentType(String fromComponentType) {
		this.fromComponentType = fromComponentType;
	}

	public String getToComponent() {
		return toComponent;
	}

	public void setToComponent(String toComponent) {
		this.toComponent = toComponent;
	}

	public String getToComponentType() {
		return toComponentType;
	}

	public void setToComponentType(String toComponentType) {
		this.toComponentType = toComponentType;
	}

	public String getSourceDatabaseType() {
		return sourceDatabaseType;
	}

	public void setSourceDatabaseType(String sourceDatabaseType) {
		this.sourceDatabaseType = sourceDatabaseType;
	}

	public String getSourceDbName() {
		return sourceDbName;
	}

	public void setSourceDbName(String sourceDbName) {
		this.sourceDbName = sourceDbName;
	}

	public String getTargetDatabaseType() {
		return targetDatabaseType;
	}

	public void setTargetDatabaseType(String targetDatabaseType) {
		this.targetDatabaseType = targetDatabaseType;
	}

	public String getTargetDbName() {
		return targetDbName;
	}

	public void setTargetDbName(String targetDbName) {
		this.targetDbName = targetDbName;
	}

	public String getTargetTableName() {
		return targetTableName;
	}

	public void setTargetTableName(String targetTableName) {
		this.targetTableName = targetTableName;
	}

	public String getSourceTableName() {
		return sourceTableName;
	}

	public void setSourceTableName(String sourceTableName) {
		this.sourceTableName = sourceTableName;
	}

	public String getSqlQuery() {
		return SqlQuery;
	}

	public void setSqlQuery(String sqlQuery) {
		SqlQuery = sqlQuery;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((folderName == null) ? 0 : folderName.hashCode());
		result = prime * result + ((fromComponent == null) ? 0 : fromComponent.hashCode());
		result = prime * result + ((mappingName == null) ? 0 : mappingName.hashCode());
		result = prime * result + ((toComponent == null) ? 0 : toComponent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComponentLevelDetails other = (ComponentLevelDetails) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (folderName == null) {
			if (other.folderName != null)
				return false;
		} else if (!folderName.equals(other.folderName))
			return false;
		if (fromComponent == null) {
			if (other.fromComponent != null)
				return false;
		} else if (!fromComponent.equals(other.fromComponent))
			return false;
		if (mappingName == null) {
			if (other.mappingName != null)
				return false;
		} else if (!mappingName.equals(other.mappingName))
			return false;
		if (toComponent == null) {
			if (other.toComponent != null)
				return false;
		} else if (!toComponent.equals(other.toComponent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ComponentLevelDetails [fileName=" + fileName + ", folderName=" + folderName + ", mappingName="
				+ mappingName + ", fromComponent=" + fromComponent + ", fromComponentType=" + fromComponentType
				+ ", toComponent=" + toComponent + ", toComponentType=" + toComponentType + ", sourceDatabaseType="
				+ sourceDatabaseType + ", sourceDbName=" + sourceDbName + ", targetDatabaseType=" + targetDatabaseType
				+ ", targetDbName=" + targetDbName + ", targetTableName=" + targetTableName + ", sourceTableName="
				+ sourceTableName + ", executionId=" + executionId + ", sessionName=" + sessionName + "]";
	}

}
