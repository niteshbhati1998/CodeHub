package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class WavePlanningObjDetails {
    private String projectName;
    private String functionalModule;
    private String database;
    private String objectName;
    private String objectType;
    private String complexity;
    private String objectSize;
    private Integer wave;
}
