package com.cognizant.lineage.upload.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeDataModel {

	private String tableName;
	private int Serial;
	private String domainName;
	private String layerName;
	
//	@JsonSerialize(using=DoubleSerializer.class)
//	private double pagerank;
	private int inEdges;
	private int outEdges;
	private int degree;
	private String projectName;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@JsonProperty(value="serial_no")
	public int getSerial() {
		return Serial;
	}

	@JsonProperty(value="serial_no")
	public void setSerial(int serial) {
		Serial = serial;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	@JsonIgnore
	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layer) {
		this.layerName = layer;
	}

//	public double getPagerank() {
//		return pagerank;
//	}
//
//	public void setPagerank(double pagerank) {
//		this.pagerank = pagerank;
//	}

	public int getInEdges() {
		return inEdges;
	}

	public void setInEdges(int inEdges) {
		this.inEdges = inEdges;
	}

	public int getOutEdges() {
		return outEdges;
	}

	public void setOutEdges(int outEdges) {
		this.outEdges = outEdges;
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	@JsonIgnore
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
