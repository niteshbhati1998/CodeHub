package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class ShiftNodeDetails {

	private String nodeName;
	private String sourceWaveNo;
	private String targetWaveNo;
	private String sourceSprintNo;
	private String targetSprintNo;
}
