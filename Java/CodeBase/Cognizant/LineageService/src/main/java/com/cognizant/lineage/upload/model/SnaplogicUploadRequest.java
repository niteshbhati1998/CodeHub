package com.cognizant.lineage.upload.model;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SnaplogicUploadRequest {
    private MultipartFile[] file;
    private String projectName;
    private String tech;
}
