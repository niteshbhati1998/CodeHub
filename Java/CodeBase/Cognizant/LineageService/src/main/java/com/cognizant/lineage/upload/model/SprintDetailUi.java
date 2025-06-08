package com.cognizant.lineage.upload.model;

import java.util.List;

import lombok.Data;

@Data
public class SprintDetailUi {
	private String sprint;
	private String additionalText;
	private List<SprintDataUi> data;
}