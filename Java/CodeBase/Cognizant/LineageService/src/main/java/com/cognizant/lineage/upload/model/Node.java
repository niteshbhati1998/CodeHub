package com.cognizant.lineage.upload.model;

import java.util.ArrayList;
import java.util.List;

public class Node {

	String value;
	String type;
	List<Node> neighbors;

	/** others extra properties **/

	String tableName;
	String mappingName;
	String folderName;
	String fileName;
	String sourceDatabasetype;
	String sourceDbdname;
	String targetDatabasetype;
	String targetDbdname;
	String nodeElementType;
	String sqlQuery;
	List<String> preSql;
	List<String> postSql;
	String sessionName;
	

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

	private Integer executionId;

	public Integer getExecutionId() {
		return executionId;
	}

	public void setExecutionId(Integer executionId) {
		this.executionId = executionId;
	}

	public Node(String value, String type) {
		this.value = value;
		this.type = type;
		neighbors = new ArrayList<>();
	}

	public void addEdge(Node to) {
		neighbors.add(to);
	}

	public void addEdges(List<Node> tos) {
		neighbors.addAll(tos);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Node other = (Node) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equalsIgnoreCase(other.type))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equalsIgnoreCase(other.value))
			return false;
		return true;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<Node> getNeighbors() {
		return neighbors;
	}

	/*
	 * public void setNeighbors(List<Node> neighbors) { this.neighbors = neighbors;
	 * }
	 */

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getMappingName() {
		return mappingName;
	}

	public void setMappingName(String mappingName) {
		this.mappingName = mappingName;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSourceDatabasetype() {
		return sourceDatabasetype;
	}

	public void setSourceDatabasetype(String sourceDatabasetype) {
		this.sourceDatabasetype = sourceDatabasetype;
	}

	public String getSourceDbdname() {
		return sourceDbdname;
	}

	public void setSourceDbdname(String sourceDbdname) {
		this.sourceDbdname = sourceDbdname;
	}

	public String getTargetDatabasetype() {
		return targetDatabasetype;
	}

	public void setTargetDatabasetype(String targetDatabasetype) {
		this.targetDatabasetype = targetDatabasetype;
	}

	public String getTargetDbdname() {
		return targetDbdname;
	}

	public void setTargetDbdname(String targetDbdname) {
		this.targetDbdname = targetDbdname;
	}

	public String getNodeElementType() {
		return nodeElementType;
	}

	public void setNodeElementType(String sourceElementType) {
		this.nodeElementType = sourceElementType;
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
}