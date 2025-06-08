package com.cognizant.lineage.dao.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "odi_details", schema = "semantic")
@Entity
//public class ScriptFilesInfo {
public class OdiDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sqlid")
	private Integer sqlId;

	@Column(name = "job_id")
	private Integer jobId;

	@Column(name = "object_type")
	private String objectType;

	@Column(name = "sqltext")
	private String sqlText;
	
	@Column(name = "new_sqltext")
	private String newSqltext;
	
	@Column(name = "cleansed_sqltext")
	private String cleansedSqltext;

	@Column(name = "filename")
	private String fileName;

	@Column(name = "start_line")
	private Integer startLine;
	
	@Column(name = "end_line")
	private Integer endLine;
	
	@Column(name = "query_type")
	private String queryType;
	
	@Column(name = "creation_time")
	private Date creationTime;
	
	@Column(name = "updation_time")
	private Date updation_time;

	public Integer getSqlId() {
		return sqlId;
	}

	public void setSqlId(Integer sqlId) {
		this.sqlId = sqlId;
	}

	public Integer getJobId() {
		return jobId;
	}

	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

	public String getNewSqltext() {
		return newSqltext;
	}

	public void setNewSqltext(String newSqltext) {
		this.newSqltext = newSqltext;
	}

	public String getCleansedSqltext() {
		return cleansedSqltext;
	}

	public void setCleansedSqltext(String cleansedSqltext) {
		this.cleansedSqltext = cleansedSqltext;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getStartLine() {
		return startLine;
	}

	public void setStartLine(Integer startLine) {
		this.startLine = startLine;
	}

	public Integer getEndLine() {
		return endLine;
	}

	public void setEndLine(Integer endLine) {
		this.endLine = endLine;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getUpdation_time() {
		return updation_time;
	}

	public void setUpdation_time(Date updation_time) {
		this.updation_time = updation_time;
	}
		
}
