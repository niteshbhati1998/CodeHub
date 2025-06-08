package com.cognizant.lineage.upload.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.service.TableauService;
import com.cognizant.lineage.util.LoggerUtil;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class TableauController {
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${tableauInputFiles}")
	private String tableauUploadFileLocation;
	
	@Autowired
	LoggerUtil loggerUtil;
	
	@Autowired
	private TableauService tableauService;
	
	@CrossOrigin
	@RequestMapping(value = "/tableauFileParsing/{tech}/{projectName}", method = RequestMethod.POST)
	public ResponseEntity<?> parseTableauXML(@RequestParam("file") MultipartFile[] files,@PathVariable String tech ,@PathVariable String projectName) throws Exception {
		CommonResponse<String> responseBody = new CommonResponse<>();
		try {		
			Map<String, Object> hm = tableauService.uploadFilesToServer(files, projectName, tech);
			tableauService.readFilesAndParse(projectName, tech, hm);
			responseBody.setMessage("Tableau files parsed successfully ");
			responseBody.setPayload(String.valueOf(hm.get("JobId")));
			return new ResponseEntity<>(responseBody, HttpStatus.OK);
		} catch (LineageBusinessException e) {
			responseBody.setMessage(GeneralConstants.FAILED);
			responseBody.setPayload(e.getMessage());
			return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
		} catch (Exception ex) {
			responseBody.setPayload("Failed to parse Tableau files ");
			return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
		}
	}	
}
