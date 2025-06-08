package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class BusinessObjectNameDetails {
    private String projectName;
    private String sourceModule;
    private String targetModule;
    private String tableName;
    private String viewName;
    private String materializedViewName;
    private String userDefinedFunctionName;
    private String procedureName;
    private String triggerName;
    private String jobName;
}
