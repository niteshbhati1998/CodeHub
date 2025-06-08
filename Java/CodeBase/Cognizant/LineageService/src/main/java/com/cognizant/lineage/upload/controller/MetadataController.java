package com.cognizant.lineage.upload.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.dao.dto.ApplicationNameDetailsDto;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.model.MetadataInput;
import com.cognizant.lineage.upload.model.ObjectNameDetails;
import com.cognizant.lineage.upload.service.MetadataService;
import com.cognizant.lineage.util.Sanitization;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/lineage/metadata")
public class MetadataController {

    @Autowired
    MetadataService metadataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataController.class);

    @PostMapping("/object-names")
    public ResponseEntity<?> getObjectNames(@RequestBody MetadataInput metadataInput) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            String projectName = metadataInput.getProjectName();
            if (StringUtils.isEmpty(projectName)) {
                response.setMessage(GeneralConstants.FAILED);
                response.setPayload("Please input the project name");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            projectName = Sanitization.sanitizeInputForWhiteSpace(projectName);
            LOGGER.info("Fetch object names project name: {}", projectName);
            List<String> objectNameList = metadataService.getObjectNameFromNodes(projectName);
            CommonResponse<List<String>> response1 = new CommonResponse<>();
            if (objectNameList.isEmpty()) {
                response.setMessage(GeneralConstants.FAILED);
                response.setPayload(String.format(
                        "No objects present for this project: %s. Please try with different project.", projectName));
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response1.setMessage(GeneralConstants.SUCCESS);
            response1.setPayload(objectNameList);
            return new ResponseEntity<>(response1, HttpStatus.OK);
        } catch (LineageBusinessException ex) {
            response.setMessage(ex.getMessage());
            response.setPayload(ex.getMessage());
            LOGGER.error("Lineage Business Exception occurred: ", ex);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred: ", ex);
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal server error occurred. Please try again");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/application-names")
    public ResponseEntity<?> getApplicationNames(@RequestBody MetadataInput metadataInput) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            String projectName = metadataInput.getProjectName();
            if (StringUtils.isEmpty(projectName)) {
                response.setMessage(GeneralConstants.FAILED);
                response.setPayload("Please input the project name");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            projectName = Sanitization.sanitizeInputForWhiteSpace(projectName);
            LOGGER.info("Fetch application names project name: {}", projectName);
            List<String> objectNameList = metadataService.getApplicationNameFromEdges(projectName);
            CommonResponse<List<String>> response1 = new CommonResponse<>();
            if (objectNameList.isEmpty()) {
                response.setMessage(GeneralConstants.FAILED);
                response.setPayload(String.format(
                        "No data present for this project: %s. Please try with different project.", projectName));
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response1.setMessage(GeneralConstants.SUCCESS);
            response1.setPayload(objectNameList);
            return new ResponseEntity<>(response1, HttpStatus.OK);
        } catch (LineageBusinessException ex) {
            response.setMessage(ex.getMessage());
            response.setPayload(ex.getMessage());
            LOGGER.error("Lineage Business Exception occurred: ", ex);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred: ", ex);
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal server error occurred. Please try again");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/object-name-details")
    public ResponseEntity<?> getObjectNameDetails(@RequestBody MetadataInput metadataInput) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            String projectName = metadataInput.getProjectName();
            String objectName = metadataInput.getObjectOrAppName();
            if (StringUtils.isEmpty(projectName) || StringUtils.isEmpty(objectName)) {
                response.setMessage(GeneralConstants.FAILED);
                response.setPayload("Please input the project name and object name");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            projectName = Sanitization.sanitizeInputForWhiteSpace(projectName);
            objectName = Sanitization.sanitizeInputForWhiteSpace(objectName);
            LOGGER.info("Fetch object name details for project name: {}, object name: {}", projectName, objectName);
            ObjectNameDetails objectDetailsWithClassification =
                    metadataService.getObjectNameDetails(projectName, objectName);
            CommonResponse<ObjectNameDetails> response1 = new CommonResponse<>();
            response1.setMessage(GeneralConstants.SUCCESS);
            response1.setPayload(objectDetailsWithClassification);
            return new ResponseEntity<>(response1, HttpStatus.OK);
        } catch (LineageBusinessException ex) {
            response.setMessage(ex.getMessage());
            response.setPayload(ex.getMessage());
            LOGGER.error("Lineage Business Exception occurred: ", ex);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred: ", ex);
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal server error occurred. Please try again");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/application-name-details")
    public ResponseEntity<?> getApplicationNameDetails(@RequestBody MetadataInput metadataInput) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            String projectName = metadataInput.getProjectName();
            String applicationName = metadataInput.getObjectOrAppName();
            if (StringUtils.isEmpty(projectName) || StringUtils.isEmpty(applicationName)) {
                response.setMessage(GeneralConstants.FAILED);
                response.setPayload("Please input the project name and application name");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            projectName = Sanitization.sanitizeInputForWhiteSpace(projectName);
            applicationName = Sanitization.sanitizeInputForWhiteSpace(applicationName);
            LOGGER.info("Fetch application name details for project name: {}, application name: {}",
                    projectName, applicationName);
            List<ApplicationNameDetailsDto> objectNameDetailsList =
                    metadataService.getApplicationNameDetails(projectName, applicationName);
            CommonResponse<List<ApplicationNameDetailsDto>> response1 = new CommonResponse<>();
            response1.setMessage(GeneralConstants.SUCCESS);
            response1.setPayload(objectNameDetailsList);
            return new ResponseEntity<>(response1, HttpStatus.OK);
        } catch (LineageBusinessException ex) {
            response.setMessage(ex.getMessage());
            response.setPayload(ex.getMessage());
            LOGGER.error("Lineage Business Exception occurred: ", ex);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred: ", ex);
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal server error occurred. Please try again");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
