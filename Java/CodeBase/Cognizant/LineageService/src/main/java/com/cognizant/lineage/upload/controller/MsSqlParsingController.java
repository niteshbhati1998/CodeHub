package com.cognizant.lineage.upload.controller;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.service.MsSqlParsingService;
import com.cognizant.lineage.util.LoggerUtil;
import com.cognizant.lineage.util.Sanitization;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage")
public class MsSqlParsingController {

    @Value("${logFileLocation}")
    private String logFileLocation;

    @Autowired
    LoggerUtil loggerUtil;

    @Autowired
    MsSqlParsingService msSqlParsingService;

    @CrossOrigin
    @PostMapping("/parseSql/{tech}/{projectName}")
    public ResponseEntity<?> parseSQLServerFiles(@RequestParam("file") MultipartFile[] files,
                                                 @PathVariable("tech") String tech,
                                                 @PathVariable("projectName") String projectName) throws Exception {
        CommonResponse<String> response = new CommonResponse<>();
        String sanitizedTech = Sanitization.sanitizeInput(tech);
        String sanitizedProjectName = Sanitization.sanitizeInput(projectName);
        Logger LOGGER;
        FileHandler handler = null;
        String logFileName = null;
        synchronized (this) {
            logFileName = logFileLocation + "MsSqlServerUpload.log";
            try {
                handler = new FileHandler(logFileName, true);
            } catch (SecurityException | IOException e) {
                throw new Exception(e);
            }
            LOGGER = loggerUtil.getLogger(handler, logFileName);
        }
        try {
            LineageJob lineageJob = msSqlParsingService.uploadFilesAndSaveLineage(files, sanitizedTech, sanitizedProjectName, LOGGER);
            msSqlParsingService.executePythonScriptsForSqlServer(sanitizedProjectName, sanitizedTech, lineageJob.getJobId(), LOGGER);
            response.setPayload(String.valueOf(lineageJob.getJobId()));
            response.setMessage(GeneralConstants.PARSING_DONE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (LineageBusinessException e) {
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred into MsSqlParsingController " + ex.getMessage());
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal server error occurred. Please try again after some time");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
