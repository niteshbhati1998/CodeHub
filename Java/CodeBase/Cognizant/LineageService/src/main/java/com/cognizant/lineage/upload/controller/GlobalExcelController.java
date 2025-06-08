package com.cognizant.lineage.upload.controller;

import static com.cognizant.lineage.upload.constants.GeneralConstants.CSL_GLOBAL_EXCEL_REPORT_PREFIX;
import static com.cognizant.lineage.upload.constants.GeneralConstants.XLSX_EXTENSION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.model.CommonResponse;
import com.cognizant.lineage.upload.model.GlobalExcelRequest;
import com.cognizant.lineage.upload.service.GlobalExcelService;
import com.cognizant.lineage.util.Sanitization;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lineage")
public class GlobalExcelController {

    @Value("${globalExcelFilePathForLineage}")
    private String globalExcelFilePathForLineage;

    @Autowired
    GlobalExcelService globalExcelService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExcelController.class);

    @PostMapping("/csl-global-excel")
    public ResponseEntity<?> downloadGlobalExcel(@RequestBody GlobalExcelRequest globalExcelRequest) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            String projectName = Sanitization.sanitizeInput(globalExcelRequest.getProjectName());
            String fileName = CSL_GLOBAL_EXCEL_REPORT_PREFIX.concat(projectName).concat(XLSX_EXTENSION);
            String result = globalExcelService.deleteGlobalExcelExistingFile(projectName, fileName);
            if (GeneralConstants.FAILED.equalsIgnoreCase(result)) {
                response.setMessage(result);
                response.setPayload("Internal error occurred. Please try after some time.");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            globalExcelService.createGlobalExcel(projectName, fileName);
            response.setMessage(result);
            response.setPayload(globalExcelFilePathForLineage.concat(fileName));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred in global excel controller downloadGlobalExcel: {}", ex.getMessage());
            response.setMessage(GeneralConstants.ERROR);
            response.setPayload("Internal Server error occurred. Please try after some time.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/csl-global-excel/status")
    public ResponseEntity<?> getStatusOfGlobalExcel(@RequestBody GlobalExcelRequest globalExcelRequest) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            String projectName = Sanitization.sanitizeInput(globalExcelRequest.getProjectName());
            String status = globalExcelService.getStatusOfGlobalExcel(projectName);
            if (GeneralConstants.ERROR.equalsIgnoreCase(status)) {
                response.setMessage(GeneralConstants.ERROR);
                response.setPayload("Internal Server error occurred. Please try after some time.");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            response.setMessage(status);
            response.setPayload(status);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred in global excel controller getStatusOfGlobalExcel: {}", ex.getMessage());
            response.setMessage(GeneralConstants.ERROR);
            response.setPayload("Internal Server error occurred. Please try after some time.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
