package com.cognizant.lineage.upload.controller;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.service.SsrsService;
import com.cognizant.lineage.util.LoggerUtil;
import com.cognizant.lineage.util.Sanitization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

@RestController
@RequestMapping("/lineage")
public class SsrsController {

    @Value("${logFileLocation}")
    private String logFileDirectory;

    @Autowired
    LoggerUtil loggerUtil;

    @Autowired
    SsrsService ssrsService;

    @PostMapping(value = "/parseSsrs/{tech}/{projectName}")
    public ResponseEntity<?> parseSsrsScript(@RequestParam("file") MultipartFile[] files, @PathVariable String tech,
                                             @PathVariable String projectName) throws Exception {
        CommonResponse<String> response = new CommonResponse<>();
        String sanitizedTech = Sanitization.sanitizeInput(tech);
        String sanitizedProjectName = Sanitization.sanitizeInput(projectName);
        Logger LOGGER;
        FileHandler handler = null;
        String logFileName = null;

        synchronized (this) {
            logFileName = logFileDirectory + "SsrsUpload.log";
            try {
                handler = new FileHandler(logFileName, true);
            } catch (SecurityException | IOException e) {
                throw new Exception(e);
            }
            LOGGER = loggerUtil.getLogger(handler, logFileName);
        }
        try {
            if (StringUtils.isEmpty(sanitizedTech) ||
                    StringUtils.isEmpty(sanitizedProjectName) ||
                    Objects.isNull(files) || files.length == 0) {
                response.setMessage("Please input the technology | project name | upload files");
                response.setPayload("Please input the technology | project name | upload files");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            LineageJob lineageJob = ssrsService.uploadAndParseSsrsScripts(files, sanitizedProjectName, sanitizedTech, LOGGER);
            System.out.println("SSRS files uploaded under job id: " + lineageJob.getJobId());
            ssrsService.parseSsrsScripts(lineageJob, LOGGER);
            response.setPayload(String.valueOf(lineageJob.getJobId()));
            response.setMessage(GeneralConstants.PARSING_DONE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            System.out.println("Exception occurred into MsSqlParsingController " + ex.getMessage());
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal server error occurred. Please try again after some time");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
