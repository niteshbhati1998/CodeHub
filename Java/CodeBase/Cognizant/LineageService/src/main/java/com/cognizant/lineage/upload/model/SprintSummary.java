package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class SprintSummary {

	private Integer sprint;
	private String module;
	private String technology;
	private String objectName;
	private String complexity;
	private Integer totalCount;
	private Integer totalSize;

}
