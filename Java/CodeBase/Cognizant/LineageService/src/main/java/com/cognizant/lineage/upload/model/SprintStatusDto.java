package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class SprintStatusDto {
    private String executionStep;
    private String executionStepName;
    private String status;
}
