package com.cognizant.lineage.upload.service;

import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.exception.LineageBusinessException;

public interface BteqService {
	
	public LineageJob uploadFilesAndSaveLineage(MultipartFile[] files, String tech, String projectName) throws LineageBusinessException;

}
