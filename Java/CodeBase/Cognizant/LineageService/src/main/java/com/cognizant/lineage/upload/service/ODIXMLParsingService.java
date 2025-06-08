package com.cognizant.lineage.upload.service;

import java.io.File;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.exception.LineageBusinessException;

public interface ODIXMLParsingService {
	
	public void parseODIXML(String tech, File filesArr[], int jobId, String projectName,
			LineageJobStatus jobStatus, LineageJob lineageJob);
	
	public Map<String, Object> parseODIXmlAndSaveLineage(MultipartFile[] files, String tech, 
			String projectName) throws LineageBusinessException;

}
