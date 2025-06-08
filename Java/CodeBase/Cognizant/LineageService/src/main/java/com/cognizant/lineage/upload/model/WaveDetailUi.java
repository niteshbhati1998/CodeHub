package com.cognizant.lineage.upload.model;

import java.util.List;

import lombok.Data;

@Data
public class WaveDetailUi {
	private String wave;
	private String additionalText;
	private List<SprintDetailUi> sprints;
}