package com.cognizant.lineage.upload.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.exception.LineageBusinessException;

public interface InformaticaLineageService {
	
	public void parseXmlFile(int batchSize, double size, String projectName, 
			Integer sequenceId) throws Exception;
	
	public Map<String, Object> uploadFilesForInformatica(MultipartFile[] files, String projectName) throws LineageBusinessException;
	
	public void parseInformaticaParameterFiles();
}
