package com.cognizant.lineage.upload.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Sprint {
	
	@JsonProperty("Sprint")
	private String sprint;
	@JsonProperty("Simple")
	private long simple;
	@JsonProperty("SimpleSize")
	private long simpleSize;
	@JsonProperty("Medium")
	private long medium;
	@JsonProperty("MediumSize")
	private long mediumSize;
	@JsonProperty("Complex")
	private long complex;
	@JsonProperty("ComplexSize")
	private long complexSize;
	@JsonProperty("VeryComplex")
	private long veryComplex;
	@JsonProperty("VeryComplexSize")
	private long veryComplexSize;
	@JsonProperty("TotalCount")
	private long totalCount;
	@JsonProperty("TotalSize")
	private long totalSize;	
}
