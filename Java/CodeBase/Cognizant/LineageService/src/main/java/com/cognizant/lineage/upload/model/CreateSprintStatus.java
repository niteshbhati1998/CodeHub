package com.cognizant.lineage.upload.model;

import java.util.List;

import lombok.Data;

@Data
public class CreateSprintStatus {
    private String projectname;
    private List<SprintStatusDto> statusList;
}
