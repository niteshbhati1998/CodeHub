package com.cognizant.lineage.upload.model;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class SprintDataUi {

	private String waveName;
	private String waveSprint;
	private String sprintName;
	private String migrationType;
	private Timestamp startDate;
	private Timestamp endDate;
	private String duration;
	private String size;
	private String complexity;
	private String status;
	private String unit;
	private String value;
}
