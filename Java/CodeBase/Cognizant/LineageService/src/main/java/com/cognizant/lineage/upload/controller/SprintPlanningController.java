package com.cognizant.lineage.upload.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cognizant.lineage.upload.model.*;
import com.cognizant.lineage.util.Sanitization;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.service.SprintPlanningService;

@CrossOrigin(origins = "*")
@RequestMapping(value = "/lineage/planning")
@RestController
public class SprintPlanningController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SprintPlanningController.class);

	@Autowired
	SprintPlanningService sprintPlanningService;

    @PostMapping("/extraction")
    public ResponseEntity<?> invokeExtraction(@RequestBody Extraction connectionDetails) {
        CommonResponse<String> response = new CommonResponse<>();
        connectionDetails.setConnectionName(Sanitization.sanitizeInput(connectionDetails.getConnectionName()));
		connectionDetails.setConnectionType(Sanitization.sanitizeInput(connectionDetails.getConnectionType()));
        try {
            if (StringUtils.isEmpty(connectionDetails.getConnectionName())) {
                response.setMessage("Please input the connection name");
                response.setPayload("Please input the connection name");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            if (StringUtils.isEmpty(connectionDetails.getConnectionType())) {
                response.setMessage("Please input the connection type");
                response.setPayload("Please input the connection type");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            String responseFromScript = sprintPlanningService.volumetricExtraction(connectionDetails);
            response.setMessage(responseFromScript);
            if (GeneralConstants.FAILED.equalsIgnoreCase(responseFromScript)) {
                LOGGER.info("Data extraction failed due to internal error");
                response.setPayload("Data extraction failed due to internal error");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (GeneralConstants.SUCCESS.equalsIgnoreCase(responseFromScript)) {
                sprintPlanningService.calculateComplexity(connectionDetails);
				response.setPayload(GeneralConstants.VOLUMETRIC_ANALYSIS_COMPLETED);
            }
            LOGGER.info("Data extraction success");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred: ", ex);
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal Server Error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/extraction-status")
    public ResponseEntity<?> getExtractionStatus() {
        try {
            List<SprintStatusDto> statusList = sprintPlanningService.getExtractionStatus();
            Set<String> statusStringList = statusList.parallelStream()
                    .map(SprintStatusDto::getStatus).collect(Collectors.toSet());
            CommonResponse<List<SprintStatusDto>> response = new CommonResponse<>();
            response.setPayload(statusList);
            if (statusStringList.contains(GeneralConstants.ERROR)) {
                response.setMessage(GeneralConstants.FAILED);
            } else if (statusStringList.contains(GeneralConstants.NOT_STARTED) ||
                    statusStringList.contains(GeneralConstants.IN_PROCESS)) {
                response.setMessage(GeneralConstants.STATUS_IN_PROGRESS);
            } else if (statusStringList.size() == 1 &&
                    statusStringList.stream().anyMatch(GeneralConstants.COMPLETED::equalsIgnoreCase)) {
                response.setMessage(GeneralConstants.COMPLETED);
            }
            LOGGER.info("Extraction status fetch success");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            CommonResponse<String> response = new CommonResponse<>();
            LOGGER.error("Exception occurred: " + ex);
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal Server Error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @CrossOrigin
	@GetMapping("/executeScriptForSprintPlan/{projectName}")
    public ResponseEntity<?> executeScriptForSprintPlan(@PathVariable String projectName) {
		ResponseForDB response = new ResponseForDB();
        try {
        	sprintPlanningService.executeScriptForSprintPlanObjectApplicationBased(projectName);
            response.setMessage("Sprint generated Successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @GetMapping("/getSprintDetails/{projectName}/{sprintType}")
    public ResponseEntity<?> getSprintDetails(@PathVariable String projectName, @PathVariable String sprintType) {
        CommonResponse<List<WaveDetailUi>> response = new CommonResponse<>();
        try {
            List<WaveDetailUi> waveDetailUiList = sprintPlanningService.getSprintDetails(projectName,sprintType);
            response.setMessage("Sprint details fetched successfully");
            response.setPayload(waveDetailUiList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @GetMapping("/getSprintDetailsCustom/{projectName}/{sprintType}")
    public ResponseEntity<?> getSprintDetailsCustom(@PathVariable String projectName, @PathVariable String sprintType) {
        CommonResponse<List<WaveDetailUi>> response = new CommonResponse<>();
        try {
            List<WaveDetailUi> waveDetailUiList = sprintPlanningService.getSprintDetailsCustom(projectName,sprintType);
            response.setMessage("Custom details fetched successfully");
            response.setPayload(waveDetailUiList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

	//common: submit button
	@CrossOrigin
	@PostMapping("/saveDataOrExecuteScriptForShiftNode/{sprintPlanType}/{projectName}/{nodeName}/{sprintType}")
    public ResponseEntity<?> saveDataOrExecuteScriptForShiftNode(@RequestBody SaveDataOrExecuteScriptForShiftNode saveDataOrExecuteScriptForShiftNode,
                                                                 @PathVariable String sprintPlanType, @PathVariable String projectName,
                                                                 @PathVariable String nodeName,  @PathVariable String sprintType) {
        CommonResponse<List<WaveDetailUi>> response = new CommonResponse<>();
        StringBuilder message = new StringBuilder();
        try {
            List<WaveDetailUi> waveDetailUiList =
                    sprintPlanningService.saveDataOrExecuteScriptForShiftNode(saveDataOrExecuteScriptForShiftNode,
                            sprintPlanType, projectName, nodeName, sprintType, message);
            response.setMessage(message.toString());
            response.setPayload(waveDetailUiList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @GetMapping("/getNodeNames/{projectName}/{sprintType}")
    public ResponseEntity<?> getNodeNames(@PathVariable String projectName, @PathVariable String sprintType) {
        CommonResponse<List<String>> response = new CommonResponse<>();
        try {
            List<String> nodeNamesList = sprintPlanningService.getNodeNames(projectName,sprintType);
            response.setMessage("NodeNames fetched successfully");
            response.setPayload(nodeNamesList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @GetMapping("/executeScriptForNodeBasedSprintPlan/{projectName}/{nodeName}/{sprintType}")
    public ResponseEntity<?> executeScriptForNodeSpecificSprintPlan(@PathVariable String projectName,
                                                                    @PathVariable String nodeName,
                                                                    @PathVariable String sprintType) {
        CommonResponse<List<WaveDetailUi>> response = new CommonResponse<>();
        try {
            List<WaveDetailUi> waveDetailsUiList = sprintPlanningService.executeScriptForNodeBasedSprintPlan(projectName,nodeName,sprintType);
            response.setMessage("Node Specific Sprint Created Successfully");
            response.setPayload(waveDetailsUiList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @GetMapping("/getNodeBasedSprintPlan/{projectName}/{nodeName}/{sprintType}")
    public ResponseEntity<?> getNodeBasedSprintPlan(@PathVariable String projectName, @PathVariable String nodeName,
                                                    @PathVariable String sprintType) {
        CommonResponse<List<WaveDetailUi>> response = new CommonResponse<>();
        try {
            List<WaveDetailUi> waveDetailUiList = sprintPlanningService.getNodeBasedSprintPlan(projectName, nodeName, sprintType);
            response.setMessage("NodeBased Sprint details fetched successfully");
            response.setPayload(waveDetailUiList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @GetMapping("/getNodeBasedSprintPlanCustom/{projectName}/{nodeName}/{sprintType}")
    public ResponseEntity<?> getNodeBasedSprintPlanCustom(@PathVariable String projectName, @PathVariable String nodeName,
                                                          @PathVariable String sprintType) {
        CommonResponse<List<WaveDetailUi>> response = new CommonResponse<>();
        try {
            List<WaveDetailUi> waveDetailUiList = sprintPlanningService.getNodeBasedSprintPlanCustom(projectName, nodeName, sprintType);
            response.setMessage("NodeBased Custom details fetched successfully");
            response.setPayload(waveDetailUiList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @PostMapping("/saveNodeBasedSprintPlan/{projectName}/{nodeName}/{sprintType}")
    public ResponseEntity<?> saveNodeBasedSprintPlan(@RequestBody List<WaveDetailUi> waveDetailUiList,
                                                     @PathVariable String projectName, @PathVariable String nodeName,
                                                     @PathVariable String sprintType) {
        ResponseForDB response = new ResponseForDB();
        try {
            sprintPlanningService.updateNodeBasedSprintPlanCustom(waveDetailUiList, projectName, nodeName, sprintType);
            response.setMessage("NodeBased Custom details saved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
	@CrossOrigin
	@GetMapping("/getSprintPlanStatus/{projectName}")
    public ResponseEntity<?> getSprintPlanStatus(@PathVariable String projectName) {
		ResponseForDB response = new ResponseForDB();
        try {
        	String status = sprintPlanningService.getSprintPlanStatus(projectName);
            response.setMessage(status);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/wave-plan-report/{projectName}")
    public ResponseEntity<?> generateWavePlanReport(@PathVariable String projectName) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            if (projectName.isEmpty()) {
                response.setMessage(GeneralConstants.FAILED);
                response.setPayload("Please input the projectName");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            projectName = Sanitization.sanitizeInput(projectName);
            String fileNameWithPath = sprintPlanningService.generateWavePlanReport(projectName);
            response.setMessage(GeneralConstants.SUCCESS);
            response.setPayload(fileNameWithPath);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage(GeneralConstants.FAILED);
            response.setPayload("Internal server error occurred. Please try again after sometime");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}