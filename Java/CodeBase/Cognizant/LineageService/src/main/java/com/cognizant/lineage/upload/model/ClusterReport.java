package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class ClusterReport {
    private Integer islandId;
    private String sourceObject;
    private String targetObject;
    private String scriptName;
    private String scriptType;
    private String statementType;
}
