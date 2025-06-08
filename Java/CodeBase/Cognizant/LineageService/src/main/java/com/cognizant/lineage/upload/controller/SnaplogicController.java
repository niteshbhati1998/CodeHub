package com.cognizant.lineage.upload.controller;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.model.SnaplogicUploadRequest;
import com.cognizant.lineage.upload.service.SnaplogicService;
import com.cognizant.lineage.util.Sanitization;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class SnaplogicController {

    @Autowired
    SnaplogicService snaplogicService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SnaplogicController.class);

    @PostMapping(value = "/snaplogic/upload")
    public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile[] files,
                                        @RequestParam("tech") String tech,
                                        @RequestParam("projectName") String projectName) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            if (Objects.isNull(files) || files.length == 0 ||
                    Objects.isNull(tech) || Objects.isNull(projectName)) {
                LOGGER.info("Snaplogic Upload Request Invalid");
                response.setMessage(GeneralConstants.FAILED);
                response.setPayload(GeneralConstants.INVALID_INPUT_REQUEST);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Sanitization
            SnaplogicUploadRequest uploadRequest = new SnaplogicUploadRequest();
            uploadRequest.setFile(files);
            uploadRequest.setTech(Sanitization.sanitizeInput(tech));
            uploadRequest.setProjectName(Sanitization.sanitizeInput(projectName));

            LineageJob lineageJob = snaplogicService.uploadSnaplogicFiles(uploadRequest);
            snaplogicService.parseSnaplogicFiles(lineageJob);

            response.setMessage(GeneralConstants.PARSING_DONE);
            response.setPayload(String.valueOf(lineageJob.getJobId()));

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.info("Exception in file Snaplogic upload: {}", ex.getMessage());
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal Server error occurred.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
