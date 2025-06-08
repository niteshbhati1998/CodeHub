package com.cognizant.lineage.dao.dto;

import java.sql.Timestamp;

public interface WavePlanDto {
    String getProjectName();
    Integer getWave();
    String getWaveSprint();
    Integer getSprint();
    String getMigrationType();
    String getScriptType();
    String getNodeType();
    Timestamp getStartDate();
    Timestamp getEndDate();
    String getDuration();
    String getSize();
    String getComplexity();
    String getStatus();
}
