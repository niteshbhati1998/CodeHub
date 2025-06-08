package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class TechnologyDetails {

	private String scriptName;
	private String source;
	private String target;
	private String sqlText;
	private String statementType;
}
