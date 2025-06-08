package com.cognizant.lineage.upload.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SprintDetails {
	
	@JsonProperty("Sprint")
	private String sprint;
	@JsonProperty("Simple")
	private Object simple = 0;
	@JsonProperty("Medium")
	private Object medium = 0;
	@JsonProperty("Complex")
	private Object complex = 0;
	@JsonProperty("VeryComplex")
	private Object veryComplex = 0;
	@JsonProperty("Total")
	private Object total = 0;
}
