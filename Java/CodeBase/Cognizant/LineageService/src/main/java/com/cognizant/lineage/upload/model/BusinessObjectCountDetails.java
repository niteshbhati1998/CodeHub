package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class BusinessObjectCountDetails {
    private String projectName;
    private String sourceModule;
    private String targetModule;
    private Integer tableCount;
    private Integer viewCount;
    private Integer materializedViewCount;
    private Integer userDefinedFunctionCount;
    private Integer procedureCount;
    private Integer triggerCount;
    private Integer jobCount;
}
