package com.cognizant.lineage.upload.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UpdateModule {
	@JsonProperty("scriptName")
	private String scriptName;
	@JsonProperty("objectName")
	private String objectName;
	@JsonProperty("projectName")
	private String projectName;
	@JsonProperty("updatedModule")
	private String updatedModule;
}
