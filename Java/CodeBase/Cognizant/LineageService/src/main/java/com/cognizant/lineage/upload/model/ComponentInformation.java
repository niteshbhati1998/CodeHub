package com.cognizant.lineage.upload.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ComponentInformation {

	private String mappingName;
    private String componentName;
    private String transformationType;
	private String sqlQuery;
}
