package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class SprintSummaryApplication {
	
	private int sprint;
	private String technology;
	private String complexity;
	private Integer count;
	private String module;

}
