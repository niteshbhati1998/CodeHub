package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.dao.dto.WavePlanDto;
import com.cognizant.lineage.dao.entity.NodeBasedSprintPlanApplicationBased;
import com.cognizant.lineage.dao.entity.NodeBasedSprintPlanCustomApplicationBased;
import com.cognizant.lineage.dao.entity.NodeBasedSprintPlanCustomObjectBased;
import com.cognizant.lineage.dao.entity.NodeBasedSprintPlanObjectBased;
import com.cognizant.lineage.dao.entity.ObjectCalculationDetails;
import com.cognizant.lineage.dao.entity.SprintDetailsApplicationBased;
import com.cognizant.lineage.dao.entity.SprintDetailsCustomApplicationBased;
import com.cognizant.lineage.dao.entity.SprintDetailsCustomObjectBased;
import com.cognizant.lineage.dao.entity.SprintDetailsObjectBased;
import com.cognizant.lineage.dao.entity.SprintLogStatus;
import com.cognizant.lineage.dao.entity.SprintStatus;
import com.cognizant.lineage.dao.entity.VolumetricInfo;
import com.cognizant.lineage.dao.repository.NodeBasedSprintPlanApplicationBasedRepository;
import com.cognizant.lineage.dao.repository.NodeBasedSprintPlanCustomApplicationBasedRepository;
import com.cognizant.lineage.dao.repository.NodeBasedSprintPlanCustomObjectBasedRepository;
import com.cognizant.lineage.dao.repository.NodeBasedSprintPlanObjectBasedRepository;
import com.cognizant.lineage.dao.repository.ObjectCalculationDetailsRepo;
import com.cognizant.lineage.dao.repository.SprintDetailsApplicationBasedRepository;
import com.cognizant.lineage.dao.repository.SprintDetailsCustomApplicationBasedRepository;
import com.cognizant.lineage.dao.repository.SprintDetailsCustomObjectBasedRepository;
import com.cognizant.lineage.dao.repository.SprintDetailsObjectBasedRepository;
import com.cognizant.lineage.dao.repository.SprintLogStatusRepository;
import com.cognizant.lineage.dao.repository.SprintScriptRepository;
import com.cognizant.lineage.dao.repository.SprintStatusRepository;
import com.cognizant.lineage.dao.repository.VolumetricInfoRepo;
import com.cognizant.lineage.exception.LineageRuntimeException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.dao.HotspotDao;
import com.cognizant.lineage.upload.model.Extraction;
import com.cognizant.lineage.upload.model.PiChartData;
import com.cognizant.lineage.upload.model.PiScriptCount;
import com.cognizant.lineage.upload.model.SaveDataOrExecuteScriptForShiftNode;
import com.cognizant.lineage.upload.model.ShiftNodeDetails;
import com.cognizant.lineage.upload.model.SprintData;
import com.cognizant.lineage.upload.model.SprintDataUi;
import com.cognizant.lineage.upload.model.SprintDetailUi;
import com.cognizant.lineage.upload.model.SprintStatusDto;
import com.cognizant.lineage.upload.model.WaveDetailUi;
import com.cognizant.lineage.util.Sanitization;

import jakarta.transaction.Transactional;

@Service
public class SprintPlanningService {

	@Value("${csrfToken}")
	private String csrfToken;

	@Value("${base.path.location}")
	private String basePathLocation;

	@Value("${extractionPythonFile}")
	private String extractionPythonFile;

	@Value("${OracleScriptLocation}")
	private String oracleScriptLocation;

	@Value("${TeradataScriptLocation}")
	private String teradataScriptLocation;

	@Value("${SqlServerScriptLocation}")
	private String sqlServerScriptLocation;

	@Value("${SprintPlanScriptForObjectBased}")
	private String sprintPlanScriptForObjectBased;
	
	@Value("${SprintPlanScriptForApplicationBased}")
	private String sprintPlanScriptForApplicationBased;

	@Value("${ScriptForShiftNodesObjectBased}")
	private String scriptForShiftNodesObjectBased;
			
	@Value("${ScriptForShiftNodesApplicationBased}")
	private String scriptForShiftNodesApplicationBased;

	@Value("${NodeBasedSprintPlanScriptForObjectBased}")
	private String nodeBasedSprintPlanScriptForObjectBased;
			
	@Value("${NodeBasedSprintPlanScriptForApplicationBased}")
	private String nodeBasedSprintPlanScriptForApplicationBased;

	@Value("${wavePlanReportFileLocation}")
	private String wavePlanReportFileLocation;

	@Autowired
	SprintScriptRepository sprintScriptRepository;

	@Autowired
	private SprintStatusRepository sprintStatusRepository;

	@Autowired
	private VolumetricInfoRepo volumetricInfoRepo;

	@Autowired
	private ObjectCalculationDetailsRepo objectCalculationDetailsRepo;

	@Autowired
	private SprintLogStatusRepository sprintLogStatusRepository;

	@Autowired
	private SprintDetailsObjectBasedRepository sprintDetailsObjectBasedRepository;

	@Autowired
	private SprintDetailsApplicationBasedRepository sprintDetailsApplicationBasedRepository;

	@Autowired
	private SprintDetailsCustomObjectBasedRepository sprintDetailsCustomObjectBasedRepository;
	
	@Autowired
	private SprintDetailsCustomApplicationBasedRepository sprintDetailsCustomApplicationBasedRepository;

	@Autowired
	private NodeBasedSprintPlanObjectBasedRepository nodeBasedSprintPlanObjectBasedRepository;
	
	@Autowired
	private NodeBasedSprintPlanApplicationBasedRepository nodeBasedSprintPlanApplicationBasedRepository;

	@Autowired
	private NodeBasedSprintPlanCustomObjectBasedRepository nodeBasedSprintPlanCustomObjectBasedRepository;
	
	@Autowired
	private NodeBasedSprintPlanCustomApplicationBasedRepository nodeBasedSprintPlanCustomApplicationBasedRepository;

	@Autowired
	private HotspotDao hotspotDao;

	private static final Logger LOGGER = LoggerFactory.getLogger(SprintPlanningService.class);
	NumberFormat numberFormat = NumberFormat.getInstance();

	public PiChartData getPieChartDetailsByProjectNameAndModule(SprintData sprintData) {
		PiChartData piChartData = new PiChartData();
		List<PiScriptCount> scriptCountDetails = new ArrayList<>();
		List<PiScriptCount> scriptCountDetailsForReport = new ArrayList<>();
		long totalCount = 0;
		long totalCountReport = 0;
		try {
			if (StringUtils.isEmpty(sprintData.getModule())) {
				scriptCountDetails = sprintScriptRepository.getScriptCountByProjectName(sprintData.getProjectName());
				changeTechnologyForETL(scriptCountDetails);
				totalCount = getTotalCount(scriptCountDetails, totalCount);
				changeCountWithCommaSerprated(scriptCountDetails);
				LOGGER.info("script count details: " + scriptCountDetails);

				scriptCountDetailsForReport = sprintScriptRepository
						.getScriptCountByProjectNameForReport(sprintData.getProjectName());
				changeTechnologyNameForReport(scriptCountDetailsForReport);
				totalCountReport = getTotalCount(scriptCountDetailsForReport, totalCountReport);
				changeCountWithCommaSerprated(scriptCountDetailsForReport);
				LOGGER.info("script count details for report: " + scriptCountDetailsForReport);

			} else {
				scriptCountDetails = sprintScriptRepository
						.getScriptCountByProjectNameAndModule(sprintData.getProjectName(), sprintData.getModule());
				changeTechnologyForETL(scriptCountDetails);
				totalCount = getTotalCount(scriptCountDetails, totalCount);
				changeCountWithCommaSerprated(scriptCountDetails);
				LOGGER.info("script count details: " + scriptCountDetails);

				scriptCountDetailsForReport = sprintScriptRepository.getScriptCountByProjectNameAndModuleForReport(
						sprintData.getProjectName(), sprintData.getModule());
				changeTechnologyNameForReport(scriptCountDetailsForReport);
				totalCountReport = getTotalCount(scriptCountDetailsForReport, totalCountReport);
				changeCountWithCommaSerprated(scriptCountDetailsForReport);
				LOGGER.info("script count details for report: " + scriptCountDetailsForReport);

			}
			piChartData.setTechPiChartDetails(scriptCountDetails);
			piChartData.setReportPiChartDetails(scriptCountDetailsForReport);
			piChartData.setTotalCount(numberFormat.format(totalCount));
			piChartData.setTotalReportCount(numberFormat.format(totalCountReport));

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new LineageRuntimeException("Processing error occurred. Try after some time");
		}
		return piChartData;
	}

	private long getTotalCount(List<PiScriptCount> scriptCountDetails, long totalCount) {
		for (PiScriptCount piScriptCount : scriptCountDetails) {
			totalCount = totalCount + Long.parseLong(piScriptCount.getScriptCount().toString());
		}
		return totalCount;
	}

	private void changeCountWithCommaSerprated(List<PiScriptCount> scriptCountDetails) {
		for (PiScriptCount piScriptCount : scriptCountDetails) {
			piScriptCount.setScriptCount(numberFormat.format(piScriptCount.getScriptCount()));
		}
	}

	private void changeTechnologyForETL(List<PiScriptCount> scriptCountDetails) {
		for (PiScriptCount piScriptCount : scriptCountDetails) {
			if (piScriptCount.getTechnology().equalsIgnoreCase(TechnologyConstants.TERADATA_TPT)) {
				piScriptCount.setTechnology("TPT");
			} else if (piScriptCount.getTechnology().equalsIgnoreCase(TechnologyConstants.TERADATA_BTEQ)) {
				piScriptCount.setTechnology("BTEQ");
			} else if (piScriptCount.getTechnology().equalsIgnoreCase(TechnologyConstants.TERADATA_PROCEDURE)) {
				piScriptCount.setTechnology("Teradata-Procedure");
			} else if (piScriptCount.getTechnology().equalsIgnoreCase(TechnologyConstants.INFORMATICA_CAPS)) {
				piScriptCount.setTechnology("Informatica");
			} else if (piScriptCount.getTechnology().equalsIgnoreCase(TechnologyConstants.ORACLE_ODI)) {
				piScriptCount.setTechnology("ODI");
			}
		}

	}

	private void changeTechnologyNameForReport(List<PiScriptCount> scriptCountDetailsForReport) {
		for(PiScriptCount piScriptCount : scriptCountDetailsForReport) {
			if(piScriptCount.getTechnology().equalsIgnoreCase(TechnologyConstants.BI_COGNOS)) {
				piScriptCount.setTechnology("Cognos");
			} else if(piScriptCount.getTechnology().equalsIgnoreCase(TechnologyConstants.BI_QLIKVIEW)) {
				piScriptCount.setTechnology("Qlikview");
			} else if(piScriptCount.getTechnology().equalsIgnoreCase(TechnologyConstants.BI_TABLEAU)) {
				piScriptCount.setTechnology("Tableau");
			} else if(piScriptCount.getTechnology().equalsIgnoreCase(TechnologyConstants.POWER_BI)) {
				piScriptCount.setTechnology("Power-Bi");
			}
		}
	}

	public void executeScriptForSprintPlanObjectApplicationBased(String projectName) throws Exception {
		projectName = Sanitization.sanitizeInput(projectName);

		ProcessBuilder processBuilder =
				new ProcessBuilder("python3.9", sprintPlanScriptForObjectBased, csrfToken, projectName);
		LOGGER.info("python3.9 " + sprintPlanScriptForObjectBased + " " + csrfToken + " " + projectName);
		Process process = processBuilder.start();

		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		int exitCode = process.waitFor();
		if (exitCode == 0) {
			LOGGER.info("ObjectAndApplicationBased Script executed successfully");
			executeScriptForSprintPlanApplicationBased(projectName);
			
			sprintLogStatusRepository.deleteByProjectName(projectName);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
			ZoneId z = ZoneId.of("Asia/Kolkata") ;
			LocalDateTime now = LocalDateTime.now(z);  
			SprintLogStatus sprintLogStatus = SprintLogStatus.builder()
					.projectName(projectName)
					.status("Sprint Plan last generated on "+ dtf.format(now))
					.build();
			sprintLogStatusRepository.save(sprintLogStatus);			
		} else {
			throw new Exception("Exception occurred while executing ObjectBased Script: exitCode "+exitCode);
		}
	}

	private String invokeConnection(String scriptLocation, Extraction connectionDetails) throws IOException {

		scriptLocation = Sanitization.sanitizeInput(scriptLocation);
		String connectionType =
				Sanitization.sanitizeInput(connectionDetails.getConnectionType().replaceAll(" ",""));
		String connectionName = Sanitization.sanitizeInput(connectionDetails.getConnectionName());

		ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", scriptLocation, connectionType, connectionName);
		LOGGER.info("python3.9 " + scriptLocation + " " + connectionType + " " +connectionName);

//		processBuilder2.directory(new File(basePathLocation));
		processBuilder2.redirectErrorStream(true);
		Process process = processBuilder2.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line).append(System.lineSeparator());
		}
		LOGGER.info("python script execution output . . . {}", output);
		int exitCode;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new LineageRuntimeException("Waiting time exceeded. Could not process");
		}
		if (exitCode == 0 && StringUtils.isNotBlank(output) &&
				String.valueOf(output).contains(GeneralConstants.VOLUMETRIC_ANALYSIS_COMPLETED)) {
			LOGGER.info("volumetric analysis python script executed successfully");
			return GeneralConstants.SUCCESS;
		} else {
			SprintStatus status = getSprintStatus(output);
			sprintStatusRepository.save(status);

//			Volumetric analysis script execution has stopped due to some error
//			so deleting the existing vol_complexity record to avoid confusion about execution status

			sprintStatusRepository.deleteByFunctionType(GeneralConstants.VOL_COMPLEXITY);
			LOGGER.info("python script failed to execute...with exitCode: {} and error: {}", exitCode, output);
			return GeneralConstants.FAILED;
		}
	}

	private SprintStatus getSprintStatus(StringBuilder errorOutput) {
		Optional<SprintStatus> optObj = sprintStatusRepository
				.findFirstByFunctionTypeOrderByIdDesc(GeneralConstants.VOL_IDENTIFICATION);
		SprintStatus status;
		if (optObj.isPresent()) {
			status = optObj.get();
			if (GeneralConstants.COMPLETED.equalsIgnoreCase(status.getStatus()) &&
					!GeneralConstants.TWO.equalsIgnoreCase(status.getExecutionStep())) {
				status = SprintStatus.builder().executionStep(GeneralConstants.TWO)
						.functionType(GeneralConstants.VOL_IDENTIFICATION)
						.status(GeneralConstants.ERROR)
						.remarks(String.valueOf(errorOutput))
						.build();
				return status;
			} else if (GeneralConstants.STARTED.equalsIgnoreCase(status.getStatus()) &&
					GeneralConstants.ONE.equalsIgnoreCase(status.getExecutionStep())) {
				status.setRemarks(errorOutput.toString());
				status.setStatus(GeneralConstants.ERROR);
				return status;
			}
		}
		status = SprintStatus.builder().executionStep(GeneralConstants.ONE)
				.status(GeneralConstants.ERROR)
				.functionType(GeneralConstants.VOL_IDENTIFICATION)
				.remarks(String.valueOf(errorOutput))
				.build();
		return status;
	}

	public List<SprintStatusDto> getExtractionStatus() {
		List<SprintStatusDto> responseStatusList = new ArrayList<>();
		try {
			List<SprintStatus> identificationStatusList =
					sprintStatusRepository.findByFunctionTypeOrderById(GeneralConstants.VOL_IDENTIFICATION);
			int idListSize = identificationStatusList.size();
			if (idListSize == 0) {
				prepareSprintStatus(GeneralConstants.ONE, GeneralConstants.NOT_STARTED,
						GeneralConstants.VOLUMETRIC_STATUS_LIST[0], responseStatusList);
				prepareSprintStatus(GeneralConstants.TWO, GeneralConstants.NOT_STARTED,
						GeneralConstants.VOLUMETRIC_STATUS_LIST[1], responseStatusList);
				return responseStatusList;
			}
			if (idListSize == 1) {
				prepareSprintStatus(GeneralConstants.ONE, GeneralConstants.IN_PROCESS,
						GeneralConstants.VOLUMETRIC_STATUS_LIST[0], responseStatusList);
				prepareSprintStatus(GeneralConstants.TWO, GeneralConstants.NOT_STARTED,
						GeneralConstants.VOLUMETRIC_STATUS_LIST[1], responseStatusList);
				return responseStatusList;
			}
			if (idListSize == 2) {
				List<SprintStatus> complexityStatusList =
						sprintStatusRepository.findByFunctionTypeOrderById(GeneralConstants.VOL_COMPLEXITY);
				Set<String> statusSet = identificationStatusList.stream()
						.map(SprintStatus::getStatus).collect(Collectors.toSet());
				if (statusSet.size() == 1 && statusSet.contains(GeneralConstants.COMPLETED_SMALL)) {
					prepareSprintStatus(GeneralConstants.ONE, GeneralConstants.COMPLETED,
							GeneralConstants.VOLUMETRIC_STATUS_LIST[0], responseStatusList);
					if (complexityStatusList.isEmpty()) {
						prepareSprintStatus(GeneralConstants.TWO, GeneralConstants.NOT_STARTED,
								GeneralConstants.VOLUMETRIC_STATUS_LIST[1], responseStatusList);
						return responseStatusList;
					}
					if (GeneralConstants.COMPLETED_SMALL.equalsIgnoreCase(complexityStatusList.get(0).getStatus())) {
						prepareSprintStatus(GeneralConstants.TWO, GeneralConstants.COMPLETED,
								GeneralConstants.VOLUMETRIC_STATUS_LIST[1], responseStatusList);
					} else if (GeneralConstants.ERROR.equalsIgnoreCase(complexityStatusList.get(0).getStatus())) {
						prepareSprintStatus(GeneralConstants.TWO, GeneralConstants.ERROR,
								GeneralConstants.VOLUMETRIC_STATUS_LIST[1], responseStatusList);
					} else {
						prepareSprintStatus(GeneralConstants.TWO, GeneralConstants.IN_PROCESS,
								GeneralConstants.VOLUMETRIC_STATUS_LIST[1], responseStatusList);
					}
					return responseStatusList;
				} else if (statusSet.contains(GeneralConstants.STARTED)) {
					prepareSprintStatus(GeneralConstants.ONE, GeneralConstants.IN_PROCESS,
							GeneralConstants.VOLUMETRIC_STATUS_LIST[0], responseStatusList);
					prepareSprintStatus(GeneralConstants.TWO, GeneralConstants.NOT_STARTED,
							GeneralConstants.VOLUMETRIC_STATUS_LIST[1], responseStatusList);
					return responseStatusList;
				} else if (statusSet.contains(GeneralConstants.ERROR)) {
					prepareSprintStatus(GeneralConstants.ONE, GeneralConstants.ERROR,
							GeneralConstants.VOLUMETRIC_STATUS_LIST[0], responseStatusList);
					prepareSprintStatus(GeneralConstants.TWO, GeneralConstants.ERROR,
							GeneralConstants.VOLUMETRIC_STATUS_LIST[1], responseStatusList);
					return responseStatusList;
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getting volumetric extraction status: ", ex);
		}
		return responseStatusList;
	}

	private void prepareSprintStatus(String stepNo, String status, String stepName, List<SprintStatusDto> responseStatusList) {
		SprintStatusDto statusDto = new SprintStatusDto();
		statusDto.setStatus(status);
		statusDto.setExecutionStep(stepNo);
		statusDto.setExecutionStepName(stepName);
		responseStatusList.add(statusDto);
	}

	@Async
	@Transactional
	public void calculateComplexity(Extraction connectionDetails) {
		try {
			List<ObjectCalculationDetails> complexData =
					objectCalculationDetailsRepo.findByDbType(TechnologyConstants.TERADATA);
			Integer[] simpleArray = null;
			Integer[] mediumArray = null;
			Integer[] complexArray = null;
			Integer[] veryComplexArray = null;
			for (ObjectCalculationDetails object : complexData) {
				if (GeneralConstants.SIMPLE.equalsIgnoreCase(object.getComplexity())) {
					simpleArray = new Integer[]{object.getPartitionCount(), object.getIndexCount(), object.getRowCount(),
							object.getSplDatatypeCount(), object.getCompressionCount(), object.getColumnNullableCount()};
				} else if (GeneralConstants.MEDIUM.equalsIgnoreCase(object.getComplexity())) {
					mediumArray = new Integer[]{object.getPartitionCount(), object.getIndexCount(), object.getRowCount(),
							object.getSplDatatypeCount(), object.getCompressionCount(), object.getColumnNullableCount()};
				} else if (GeneralConstants.COMPLEX.equalsIgnoreCase(object.getComplexity())) {
					complexArray = new Integer[]{object.getPartitionCount(), object.getIndexCount(), object.getRowCount(),
							object.getSplDatatypeCount(), object.getCompressionCount(), object.getColumnNullableCount()};
				} else if (GeneralConstants.VERY_COMPLEX.equalsIgnoreCase(object.getComplexity())) {
					veryComplexArray = new Integer[]{object.getPartitionCount(), object.getIndexCount(), object.getRowCount(),
							object.getSplDatatypeCount(), object.getCompressionCount(), object.getColumnNullableCount()};
				}
			}
			simpleArray = Objects.nonNull(simpleArray) ? simpleArray : GeneralConstants.TERADATA_SIMPLE_ARRAY;
			mediumArray = Objects.nonNull(mediumArray) ? mediumArray : GeneralConstants.TERADATA_MEDIUM_ARRAY;
			complexArray = Objects.nonNull(complexArray) ? complexArray : GeneralConstants.TERADATA_COMPLEX_ARRAY;
			veryComplexArray = Objects.nonNull(veryComplexArray) ? veryComplexArray : GeneralConstants.TERADATA_VERY_COMPLEX_ARRAY;

			SprintStatus complexityStatus = SprintStatus.builder()
					.functionType(GeneralConstants.VOL_COMPLEXITY)
					.executionStep(GeneralConstants.ONE)
					.status(GeneralConstants.STARTED)
					.build();
			complexityStatus = sprintStatusRepository.save(complexityStatus);

			List<String> distinctObjectList =
					volumetricInfoRepo.findDistinctObjectNameByDatabaseType(connectionDetails.getConnectionType());
            for (String objectName : distinctObjectList) {
                List<VolumetricInfo> infolist = volumetricInfoRepo.findByObjectName(objectName);
                int clobAndBlobCount = 0;
                int compressionCount = 0;
                for (VolumetricInfo info : infolist) {
                    if (info.getColumnDataType().startsWith(GeneralConstants.CLOB) ||
                            info.getColumnDataType().startsWith(GeneralConstants.BLOB)) {
                        clobAndBlobCount++;
                    }
                    if (GeneralConstants.YES.equalsIgnoreCase(info.getCompressionValue())) {
                        compressionCount++;
                    }
                }
                VolumetricInfo info = infolist.get(0);
                String complexity = complexityCalculation(simpleArray, mediumArray, complexArray, veryComplexArray,
                        info.getPartitionCount(), info.getIndexCount(), info.getRowCount(), clobAndBlobCount,
						compressionCount);
				volumetricInfoRepo.saveComplexity(objectName, complexity);
            }
			complexityStatus.setStatus(GeneralConstants.COMPLETED);
			complexityStatus = sprintStatusRepository.save(complexityStatus);
			LOGGER.info("Teradata Volumetric Extraction Complexity Calculation {}", complexityStatus.getStatus());
		} catch (Exception ex) {
			SprintStatus complexityStatus = SprintStatus.builder()
					.functionType(GeneralConstants.VOL_COMPLEXITY)
					.executionStep(GeneralConstants.ONE)
					.status(GeneralConstants.FAILED)
					.remarks(ex.getMessage())
					.build();
			sprintStatusRepository.save(complexityStatus);
			LOGGER.error("Exception occurred in Teradata Volumetric Complexity Calculation: ", ex);
		}
	}

	/**
	 *	array[0] - partitionCount; array[1] - indexCount; array[2] - rowCount;
	 *	array[3] - splDataTypeCount - count( 'CLOB' and 'BLOB' ); array[4] - compressionCount - count( 'Y' );
	 */
	private String complexityCalculation(Integer[] simpleArray, Integer[] mediumArray, Integer[] complexArray,
										 Integer[] veryComplexArray, Integer partitionCount, Integer indexCount, Integer rowCount,
										 Integer splDataTypeCount, Integer compressionCount) {
		if (partitionCount >= veryComplexArray[0] || indexCount >= veryComplexArray[1] || rowCount >= veryComplexArray[2]
				|| splDataTypeCount >= veryComplexArray[3] || compressionCount >= veryComplexArray[4]){
			return GeneralConstants.VERY_COMPLEX;
		} else if ((partitionCount >= complexArray[0]) || (indexCount >= complexArray[1]) || (rowCount >= complexArray[2])
				|| Objects.equals(splDataTypeCount, complexArray[3]) || Objects.equals(compressionCount, complexArray[4])){
			return GeneralConstants.COMPLEX;
		} else if ((partitionCount >= mediumArray[0]) || (indexCount >= mediumArray[1]) || (rowCount >= mediumArray[2])
				|| Objects.equals(splDataTypeCount, mediumArray[3]) || Objects.equals(compressionCount, mediumArray[4])){
			return GeneralConstants.MEDIUM;
		} else if ((partitionCount >= simpleArray[0]) || (indexCount >= simpleArray[1]) || (rowCount >= simpleArray[2])
				|| Objects.equals(splDataTypeCount, simpleArray[3]) || Objects.equals(compressionCount, simpleArray[4])){
			return GeneralConstants.SIMPLE;
		}
		return GeneralConstants.SIMPLE;
	}

	public String volumetricExtraction(Extraction connectionDetails) throws IOException {

		// Deleting the existing status record for volumetric complexity calculation
		Optional<SprintStatus> volComplexityStatusOpt =
				sprintStatusRepository.findFirstByFunctionTypeAndExecutionStepOrderByIdDesc(
						GeneralConstants.VOL_COMPLEXITY, GeneralConstants.ONE);
		if (volComplexityStatusOpt.isPresent()) {
			sprintStatusRepository.deleteByFunctionType(GeneralConstants.VOL_COMPLEXITY);
		}

		String response = "";
		response = invokeConnection(extractionPythonFile, connectionDetails);
		if (GeneralConstants.SUCCESS.equalsIgnoreCase(response)) {
			String status = extractDataFromMetadataTable(connectionDetails.getConnectionType(), connectionDetails.getConnectionName());
			if(GeneralConstants.SUCCESS.equalsIgnoreCase(status)) {
				return GeneralConstants.SUCCESS;
			} else {
				return GeneralConstants.FAILED;
			}
		} else {
			return GeneralConstants.FAILED;
		}
	}

	public String extractDataFromMetadataTable(String connectionType, String connectionName) {
		try {
			connectionType = Sanitization.sanitizeInput(connectionType);
			connectionName = Sanitization.sanitizeInput(connectionName);

			ProcessBuilder processBuilder = null;
			if(connectionType.equalsIgnoreCase("oracle")) {
			    processBuilder = new ProcessBuilder("python3.9", oracleScriptLocation, csrfToken, connectionName);
				LOGGER.info("python3.9 " + oracleScriptLocation + " " + csrfToken + " " +connectionName);
			} else if(connectionType.equalsIgnoreCase("teradata")) {
				processBuilder = new ProcessBuilder("python3.9", teradataScriptLocation, csrfToken, connectionName);
				LOGGER.info("python3.9 " + teradataScriptLocation + " " + csrfToken + " " +connectionName);
			} else {
				processBuilder = new ProcessBuilder("python3.9", sqlServerScriptLocation, csrfToken, connectionName);
				LOGGER.info("python3.9 " + sqlServerScriptLocation + " " + csrfToken + " " +connectionName);
			}

			Process process = processBuilder.start();
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
		    int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("python script for extracting data from "+connectionType+" metadata table executed successfully");
				return GeneralConstants.SUCCESS;
			} else {
				LOGGER.info("python script for extracting data from "+connectionType+" metadata table failed to execute with exitCode " + exitCode);
				return GeneralConstants.FAILED;
			}
		} catch (InterruptedException ex1) {
			Thread.currentThread().interrupt();
		    LOGGER.error("InterruptedException occurred in extractDataFromMetadataTable " + ex1.getMessage());
		} catch (IOException ex2) {
			LOGGER.error("IOException occurred in extractDataFromMetadataTable " + ex2.getMessage());
		}
		return GeneralConstants.FAILED;
	}


	public void executeScriptForSprintPlanApplicationBased(String projectName) throws Exception {
		projectName = Sanitization.sanitizeInput(projectName);

		ProcessBuilder processBuilder =
				new ProcessBuilder("python3.9", sprintPlanScriptForApplicationBased, csrfToken, projectName);
		LOGGER.info("python3.9 " + sprintPlanScriptForApplicationBased + " " + csrfToken + " " + projectName);
		Process process = processBuilder.start();

		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		int exitCode = process.waitFor();
		if (exitCode == 0) {
			LOGGER.info("ApplicationBased Script executed successfully");
		} else {
			throw new Exception("Exception occurred while executing ApplicationBased Script: exitCode "+exitCode);
		}
	}

	public List<WaveDetailUi> getSprintDetails(String projectName, String sprintType) {	
		List<WaveDetailUi> waveDetailUiList = new ArrayList<>();
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			sprintType = Sanitization.sanitizeInput(sprintType);
			
			if(sprintType.equalsIgnoreCase("ObjectApplicationBased")) {
				List<Integer> waveList = sprintDetailsObjectBasedRepository.getWaveList(projectName);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = sprintDetailsObjectBasedRepository.getSprintList(projectName, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<SprintDetailsObjectBased> sprintDataList =
								sprintDetailsObjectBasedRepository.getDataForEachSprint(projectName, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} else if(sprintType.equalsIgnoreCase("ApplicationBased")) {
				List<Integer> waveList = sprintDetailsApplicationBasedRepository.getWaveList(projectName);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = sprintDetailsApplicationBasedRepository.getSprintList(projectName, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<SprintDetailsApplicationBased> sprintDataList =
								sprintDetailsApplicationBasedRepository.getDataForEachSprint(projectName, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							if(sprintData.getWaveName()!=null) {
								sprintDataUi.setWaveName(sprintData.getWaveName());
							}
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							if(sprintData.getSprintName()!=null) {
								sprintDataUi.setSprintName(sprintData.getSprintName());
							}
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							if(sprintData.getStartDate()!=null) {
								sprintDataUi.setStartDate(sprintData.getStartDate());
							}
							if(sprintData.getEndDate()!=null) {
								sprintDataUi.setEndDate(sprintData.getEndDate());
							}
							if(sprintData.getDuration()!=null) {
								sprintDataUi.setDuration(sprintData.getDuration());
							}
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							if(sprintData.getStatus()!=null) {
								sprintDataUi.setStatus(sprintData.getStatus());
							}
							if(sprintData.getUnit()!=null) {
								sprintDataUi.setUnit(sprintData.getUnit());
							}
							if(sprintData.getValue()!=null) {
								sprintDataUi.setValue(sprintData.getValue());
							}
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} else if(sprintType.equalsIgnoreCase("OnlyObject")) {
				List<Integer> waveList = sprintDetailsObjectBasedRepository.getWaveListOnlyObject(projectName);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = sprintDetailsObjectBasedRepository.getSprintListOnlyObject(projectName, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<SprintDetailsObjectBased> sprintDataList =
								sprintDetailsObjectBasedRepository.getDataForEachSprintOnlyObject(projectName, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getSprintDetails Service "+ex.getMessage());
			ex.printStackTrace();
		}
		return waveDetailUiList;
	}

	public List<WaveDetailUi> getSprintDetailsCustom(String projectName, String sprintType) {
		List<WaveDetailUi> waveDetailUiList = new ArrayList<>();
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			sprintType = Sanitization.sanitizeInput(sprintType);
			
			if(sprintType.equalsIgnoreCase("ObjectApplicationBased")) {
				List<Integer> waveList = sprintDetailsCustomObjectBasedRepository.getWaveList(projectName);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = sprintDetailsCustomObjectBasedRepository.getSprintList(projectName, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<SprintDetailsCustomObjectBased> sprintDataList =
								sprintDetailsCustomObjectBasedRepository.getDataForEachSprint(projectName, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							waveDetailUi.setAdditionalText(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDetailUi.setAdditionalText(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} else if(sprintType.equalsIgnoreCase("ApplicationBased")) {
				List<Integer> waveList = sprintDetailsCustomApplicationBasedRepository.getWaveList(projectName);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = sprintDetailsCustomApplicationBasedRepository.getSprintList(projectName, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<SprintDetailsCustomApplicationBased> sprintDataList =
								sprintDetailsCustomApplicationBasedRepository.getDataForEachSprint(projectName, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							waveDetailUi.setAdditionalText(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDetailUi.setAdditionalText(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} else if(sprintType.equalsIgnoreCase("OnlyObject")) {
				List<Integer> waveList = sprintDetailsCustomObjectBasedRepository.getWaveListOnlyObject(projectName);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = sprintDetailsCustomObjectBasedRepository.getSprintListOnlyObject(projectName, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<SprintDetailsCustomObjectBased> sprintDataList =
								sprintDetailsCustomObjectBasedRepository.getDataForEachSprintOnlyObject(projectName, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							waveDetailUi.setAdditionalText(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDetailUi.setAdditionalText(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} 
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getSprintDetailsCustom Service "+ex.getMessage());
		}
		return waveDetailUiList;
	}

	public void updateSprintDetailsCustom(List<WaveDetailUi> waveDetailUiList, String projectName, String sprintType) {
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			sprintType = Sanitization.sanitizeInput(sprintType);
			
			if(sprintType.equalsIgnoreCase("ObjectApplicationBased")) {
				for(WaveDetailUi waveDetailUi: waveDetailUiList) {
					Integer Wave = Integer.valueOf(waveDetailUi.getWave().replace("Wave", "").trim());
					LOGGER.info("deleting existing data for wave: "+Wave);
					sprintDetailsCustomObjectBasedRepository.deleteByProjectNameAndWave(projectName, Wave);

					List<SprintDetailUi> sprintDetailUiList = waveDetailUi.getSprints();
					for(SprintDetailUi sprintDetailUi: sprintDetailUiList) {
						Integer Sprint = Integer.valueOf(sprintDetailUi.getSprint().replace("Sprint", "").trim());
						List<SprintDataUi> sprintDataUiList = sprintDetailUi.getData();
						for(SprintDataUi sprintDataUi: sprintDataUiList) {
							SprintDetailsCustomObjectBased sprintDetailsCustomObjectBased = new SprintDetailsCustomObjectBased();
							sprintDetailsCustomObjectBased.setProjectName(projectName);
							sprintDetailsCustomObjectBased.setWave(Wave);
							sprintDetailsCustomObjectBased.setWaveName(sprintDataUi.getWaveName());
							sprintDetailsCustomObjectBased.setWaveSprint(sprintDataUi.getWaveSprint());
							sprintDetailsCustomObjectBased.setSprint(Sprint);
							sprintDetailsCustomObjectBased.setSprintName(sprintDataUi.getSprintName());
							sprintDetailsCustomObjectBased.setMigrationType(sprintDataUi.getMigrationType());
							sprintDetailsCustomObjectBased.setStartDate(sprintDataUi.getStartDate());
							sprintDetailsCustomObjectBased.setEndDate(sprintDataUi.getEndDate());
							sprintDetailsCustomObjectBased.setDuration(sprintDataUi.getDuration());
							sprintDetailsCustomObjectBased.setSize(sprintDataUi.getSize());
							sprintDetailsCustomObjectBased.setComplexity(sprintDataUi.getComplexity());
							sprintDetailsCustomObjectBased.setStatus(sprintDataUi.getStatus());
							sprintDetailsCustomObjectBased.setUnit(sprintDataUi.getUnit());
							sprintDetailsCustomObjectBased.setValue(sprintDataUi.getValue());
							sprintDetailsCustomObjectBasedRepository.saveAndFlush(sprintDetailsCustomObjectBased);
						}
					}
				}
			} else if(sprintType.equalsIgnoreCase("ApplicationBased")) {
				for(WaveDetailUi waveDetailUi: waveDetailUiList) {
					Integer Wave = Integer.valueOf(waveDetailUi.getWave().replace("Wave", "").trim());
					LOGGER.info("deleting existing data for wave: "+Wave);
					sprintDetailsCustomApplicationBasedRepository.deleteByProjectNameAndWave(projectName, Wave);

					List<SprintDetailUi> sprintDetailUiList = waveDetailUi.getSprints();
					for(SprintDetailUi sprintDetailUi: sprintDetailUiList) {
						Integer Sprint = Integer.valueOf(sprintDetailUi.getSprint().replace("Sprint", "").trim());
						List<SprintDataUi> sprintDataUiList = sprintDetailUi.getData();
						for(SprintDataUi sprintDataUi: sprintDataUiList) {
							SprintDetailsCustomApplicationBased sprintDetailsCustomApplicationBased = new SprintDetailsCustomApplicationBased();
							sprintDetailsCustomApplicationBased.setProjectName(projectName);
							sprintDetailsCustomApplicationBased.setWave(Wave);
							sprintDetailsCustomApplicationBased.setWaveName(sprintDataUi.getWaveName());
							sprintDetailsCustomApplicationBased.setWaveSprint(sprintDataUi.getWaveSprint());
							sprintDetailsCustomApplicationBased.setSprint(Sprint);
							sprintDetailsCustomApplicationBased.setSprintName(sprintDataUi.getSprintName());
							sprintDetailsCustomApplicationBased.setMigrationType(sprintDataUi.getMigrationType());
							sprintDetailsCustomApplicationBased.setStartDate(sprintDataUi.getStartDate());
							sprintDetailsCustomApplicationBased.setEndDate(sprintDataUi.getEndDate());
							sprintDetailsCustomApplicationBased.setDuration(sprintDataUi.getDuration());
							sprintDetailsCustomApplicationBased.setSize(sprintDataUi.getSize());
							sprintDetailsCustomApplicationBased.setComplexity(sprintDataUi.getComplexity());
							sprintDetailsCustomApplicationBased.setStatus(sprintDataUi.getStatus());
							sprintDetailsCustomApplicationBased.setUnit(sprintDataUi.getUnit());
							sprintDetailsCustomApplicationBased.setValue(sprintDataUi.getValue());
							sprintDetailsCustomApplicationBasedRepository.saveAndFlush(sprintDetailsCustomApplicationBased);
						}
					}
				}
			} else if(sprintType.equalsIgnoreCase("OnlyObject")) {
				for(WaveDetailUi waveDetailUi: waveDetailUiList) {
					Integer Wave = Integer.valueOf(waveDetailUi.getWave().replace("Wave", "").trim());
					LOGGER.info("deleting existing data for wave: "+Wave);
					sprintDetailsCustomObjectBasedRepository.deleteByProjectNameAndWaveOnlyObject(projectName, Wave);

					List<SprintDetailUi> sprintDetailUiList = waveDetailUi.getSprints();
					for(SprintDetailUi sprintDetailUi: sprintDetailUiList) {
						Integer Sprint = Integer.valueOf(sprintDetailUi.getSprint().replace("Sprint", "").trim());
						List<SprintDataUi> sprintDataUiList = sprintDetailUi.getData();
						for(SprintDataUi sprintDataUi: sprintDataUiList) {
							SprintDetailsCustomObjectBased sprintDetailsCustomObjectBased = new SprintDetailsCustomObjectBased();
							sprintDetailsCustomObjectBased.setProjectName(projectName);
							sprintDetailsCustomObjectBased.setWave(Wave);
							sprintDetailsCustomObjectBased.setWaveName(sprintDataUi.getWaveName());
							sprintDetailsCustomObjectBased.setWaveSprint(sprintDataUi.getWaveSprint());
							sprintDetailsCustomObjectBased.setSprint(Sprint);
							sprintDetailsCustomObjectBased.setSprintName(sprintDataUi.getSprintName());
							sprintDetailsCustomObjectBased.setMigrationType(sprintDataUi.getMigrationType());
							sprintDetailsCustomObjectBased.setStartDate(sprintDataUi.getStartDate());
							sprintDetailsCustomObjectBased.setEndDate(sprintDataUi.getEndDate());
							sprintDetailsCustomObjectBased.setDuration(sprintDataUi.getDuration());
							sprintDetailsCustomObjectBased.setSize(sprintDataUi.getSize());
							sprintDetailsCustomObjectBased.setComplexity(sprintDataUi.getComplexity());
							sprintDetailsCustomObjectBased.setStatus(sprintDataUi.getStatus());
							sprintDetailsCustomObjectBased.setUnit(sprintDataUi.getUnit());
							sprintDetailsCustomObjectBased.setValue(sprintDataUi.getValue());
							sprintDetailsCustomObjectBasedRepository.saveAndFlush(sprintDetailsCustomObjectBased);
						}
					}
				}
			} 
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in updateSprintDetailsCustom Service "+ex.getMessage());
		}
	}

	public List<WaveDetailUi> saveDataOrExecuteScriptForShiftNode(SaveDataOrExecuteScriptForShiftNode saveDataOrExecuteScriptForShiftNode,
																  String sprintPlanType, String projectName, String nodeName, String sprintType,
																  StringBuilder message) {
		List<WaveDetailUi> waveDetailUi = new ArrayList<>();
		try {
			sprintPlanType = Sanitization.sanitizeInput(sprintPlanType);
			projectName = Sanitization.sanitizeInput(projectName);
			//nodeName = Sanitization.sanitizeInput(nodeName);
			sprintType = Sanitization.sanitizeInput(sprintType);			

			List<WaveDetailUi> waveDetailUiList = saveDataOrExecuteScriptForShiftNode.getWaveDetailUiList();
			ShiftNodeDetails shiftNodeDetails = saveDataOrExecuteScriptForShiftNode.getShiftNodeDetails();

			if(sprintPlanType.equalsIgnoreCase("ProjectBasedSprintPlan")) {
				if(waveDetailUiList!=null) {
					LOGGER.info("saving data in sprint_details_custom..."+waveDetailUiList.toString());
					updateSprintDetailsCustom(waveDetailUiList, projectName, sprintType);
				}

				if(shiftNodeDetails!=null) {
					if(sprintType.equalsIgnoreCase("ObjectApplicationBased") || sprintType.equalsIgnoreCase("OnlyObject")) {
						if(shiftNodeDetails.getNodeName()!=null) {
							LOGGER.info("calling python script to regenerate sprint plan for Shift Node");
							ProcessBuilder processBuilder =
									new ProcessBuilder("python3.9", scriptForShiftNodesObjectBased, "--csrf", csrfToken,
											"--project_name", projectName, "--shift_node", Sanitization.sanitizeInput(shiftNodeDetails.getNodeName()),
											"--source_wave", shiftNodeDetails.getSourceWaveNo(), "--target_wave", shiftNodeDetails.getTargetWaveNo(),
											"--source_sprint", shiftNodeDetails.getSourceSprintNo(), "--target_sprint", shiftNodeDetails.getTargetSprintNo());

							LOGGER.info("python3.9" +" "+ scriptForShiftNodesObjectBased +" "+ "--csrf" +" "+ csrfToken
									+" "+ "--project_name" +" "+ projectName +" "+ "--shift_node" +" "+ Sanitization.sanitizeInput(shiftNodeDetails.getNodeName())
									+" "+ "--source_wave" +" "+ shiftNodeDetails.getSourceWaveNo() +" "+ "--target_wave" +" "+ shiftNodeDetails.getTargetWaveNo()
									+" "+ "--source_sprint" +" "+ shiftNodeDetails.getSourceSprintNo() +" "+ "--target_sprint" +" "+ shiftNodeDetails.getTargetSprintNo());

							Process process = processBuilder.start();
							StringBuilder output = new StringBuilder();
							BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							int exitCode = process.waitFor();
							if (exitCode == 0) {
								LOGGER.info("Script for Shift Node executed successfully");
							    message.append("Script for Shift Node executed successfully");
							} else {
								message.append("Exception occurred while executing Shift Node Script: ExitCode "+exitCode);
								throw new Exception("Exception occurred while executing Script for Shift Node: ExitCode "+exitCode);
							}
						} else {
							LOGGER.info("calling python script to regenerate sprint plan for Shift Sprint");
							ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptForShiftNodesObjectBased, "--csrf", csrfToken, "--project_name", projectName,
									"--source_wave", shiftNodeDetails.getSourceWaveNo(), "--target_wave", shiftNodeDetails.getTargetWaveNo(),
									"--source_sprint", shiftNodeDetails.getSourceSprintNo(), "--target_sprint", shiftNodeDetails.getTargetSprintNo(), "--sprint_shifting");

							LOGGER.info("python3.9" +" "+ scriptForShiftNodesObjectBased +" "+ "--csrf" +" "+ csrfToken +" "+ "--project_name" +" "+ projectName +" "+
									"--source_wave" +" "+ shiftNodeDetails.getSourceWaveNo() +" "+ "--target_wave" +" "+ shiftNodeDetails.getTargetWaveNo() +" "+
									"--source_sprint" +" "+ shiftNodeDetails.getSourceSprintNo() +" "+ "--target_sprint" +" "+ shiftNodeDetails.getTargetSprintNo() +" "+ "--sprint_shifting");

							Process process = processBuilder.start();
							StringBuilder output = new StringBuilder();
							BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							int exitCode = process.waitFor();
							if (exitCode == 0) {
								LOGGER.info("Script for Shift Sprint executed successfully");
							    message.append("Script for Shift Sprint executed successfully");
							} else {
								message.append("Exception occurred while executing Shift Sprint Script: ExitCode "+exitCode);
								throw new Exception("Exception occurred while executing Script for Shift Sprint: ExitCode "+exitCode);
							}
						}
					} else if(sprintType.equalsIgnoreCase("ApplicationBased")) {
						if(shiftNodeDetails.getNodeName()!=null) {
							LOGGER.info("calling python script to regenerate sprint plan for Shift Node");
							ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptForShiftNodesApplicationBased, "--csrf", csrfToken, "--project_name", projectName,
									"--shift_node", Sanitization.sanitizeInput(shiftNodeDetails.getNodeName()), "--source_wave", shiftNodeDetails.getSourceWaveNo(), "--target_wave", shiftNodeDetails.getTargetWaveNo(),
									"--source_sprint", shiftNodeDetails.getSourceSprintNo(), "--target_sprint", shiftNodeDetails.getTargetSprintNo());

							LOGGER.info("python3.9" +" "+ scriptForShiftNodesApplicationBased +" "+ "--csrf" +" "+ csrfToken +" "+ "--project_name" +" "+ projectName +" "+
									"--shift_node" +" "+ Sanitization.sanitizeInput(shiftNodeDetails.getNodeName()) +" "+ "--source_wave" +" "+ shiftNodeDetails.getSourceWaveNo() +" "+ "--target_wave" +" "+ shiftNodeDetails.getTargetWaveNo() +" "+
									"--source_sprint" +" "+ shiftNodeDetails.getSourceSprintNo() +" "+ "--target_sprint" +" "+ shiftNodeDetails.getTargetSprintNo());

							Process process = processBuilder.start();
							StringBuilder output = new StringBuilder();
							BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							int exitCode = process.waitFor();
							if (exitCode == 0) {
								LOGGER.info("Script for Shift Node executed successfully");
							    message.append("Script for Shift Node executed successfully");
							} else {
								message.append("Exception occurred while executing Shift Node Script: ExitCode "+exitCode);
								throw new Exception("Exception occurred while executing Script for Shift Node: ExitCode "+exitCode);
							}
						} else {
							LOGGER.info("calling python script to regenerate sprint plan for Shift Sprint");
							ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptForShiftNodesApplicationBased, "--csrf", csrfToken, "--project_name", projectName,
									"--source_wave", shiftNodeDetails.getSourceWaveNo(), "--target_wave", shiftNodeDetails.getTargetWaveNo(),
									"--source_sprint", shiftNodeDetails.getSourceSprintNo(), "--target_sprint", shiftNodeDetails.getTargetSprintNo(), "--sprint_shifting");

							LOGGER.info("python3.9" +" "+ scriptForShiftNodesApplicationBased +" "+ "--csrf" +" "+ csrfToken +" "+ "--project_name" +" "+ projectName +" "+
									"--source_wave" +" "+ shiftNodeDetails.getSourceWaveNo() +" "+ "--target_wave" +" "+ shiftNodeDetails.getTargetWaveNo() +" "+
									"--source_sprint" +" "+ shiftNodeDetails.getSourceSprintNo() +" "+ "--target_sprint" +" "+ shiftNodeDetails.getTargetSprintNo() +" "+ "--sprint_shifting");

							Process process = processBuilder.start();
							StringBuilder output = new StringBuilder();
							BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							int exitCode = process.waitFor();
							if (exitCode == 0) {
								LOGGER.info("Script for Shift Sprint executed successfully");
							    message.append("Script for Shift Sprint executed successfully");
							} else {
								message.append("Exception occurred while executing Shift Sprint Script: ExitCode "+exitCode);
								throw new Exception("Exception occurred while executing Script for Shift Sprint: ExitCode "+exitCode);
							}
						}
					}	
				} else {
					message.append("Data saved successfully");
				}
				waveDetailUi = getSprintDetailsCustom(projectName,sprintType);	
			} else {
				if(waveDetailUiList!=null) {
					LOGGER.info("saving data in node based sprint_details_custom..."+waveDetailUiList.toString());
					updateNodeBasedSprintPlanCustom(waveDetailUiList, projectName, nodeName, sprintType);
				}

				if(shiftNodeDetails!=null) {
					if(sprintType.equalsIgnoreCase("ObjectApplicationBased") || sprintType.equalsIgnoreCase("OnlyObject")) {
						if(shiftNodeDetails.getNodeName()!=null) {
							LOGGER.info("calling python script to regenerate node based sprint plan for Shift Node");
							ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptForShiftNodesObjectBased, "--csrf", csrfToken, "--project_name", projectName,
									"--shift_node", Sanitization.sanitizeInput(shiftNodeDetails.getNodeName()), "--source_wave", shiftNodeDetails.getSourceWaveNo(), "--target_wave", shiftNodeDetails.getTargetWaveNo(),
									"--source_sprint", shiftNodeDetails.getSourceSprintNo(), "--target_sprint", shiftNodeDetails.getTargetSprintNo(), "--node_based", "--selected_node", nodeName);

							LOGGER.info("python3.9" +" "+ scriptForShiftNodesObjectBased +" "+ "--csrf" +" "+ csrfToken +" "+ "--project_name" +" "+ projectName +" "+
									"--shift_node" +" "+ Sanitization.sanitizeInput(shiftNodeDetails.getNodeName()) +" "+ "--source_wave" +" "+ shiftNodeDetails.getSourceWaveNo() +" "+ "--target_wave" +" "+ shiftNodeDetails.getTargetWaveNo() +" "+
									"--source_sprint" +" "+ shiftNodeDetails.getSourceSprintNo() +" "+ "--target_sprint" +" "+ shiftNodeDetails.getTargetSprintNo() +" "+ "--node_based" +" "+ "--selected_node" +" "+ nodeName);

							Process process = processBuilder.start();
							StringBuilder output = new StringBuilder();
							BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							int exitCode = process.waitFor();
							if (exitCode == 0) {
								LOGGER.info("Node Based Script for Shift Node executed successfully");
								message.append("Script for Shift Node executed successfully");
							} else {
								message.append("Exception occurred while executing Shift Node Script: ExitCode "+exitCode);
								throw new Exception("Exception occurred while executing Node Based script for Shift Nodes: ExitCode "+exitCode);
							}
						} else {
							LOGGER.info("calling python script to regenerate node based sprint plan for Shift Sprint");
							ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptForShiftNodesObjectBased, "--csrf", csrfToken, "--project_name", projectName,
									"--source_wave", shiftNodeDetails.getSourceWaveNo(), "--target_wave", shiftNodeDetails.getTargetWaveNo(),
									"--source_sprint", shiftNodeDetails.getSourceSprintNo(), "--target_sprint", shiftNodeDetails.getTargetSprintNo(), "--node_based", "--selected_node", nodeName, "--sprint_shifting");

							LOGGER.info("python3.9" +" "+ scriptForShiftNodesObjectBased +" "+ "--csrf" +" "+ csrfToken +" "+ "--project_name" +" "+ projectName +" "+
									"--source_wave" +" "+ shiftNodeDetails.getSourceWaveNo() +" "+ "--target_wave" +" "+ shiftNodeDetails.getTargetWaveNo() +" "+
									"--source_sprint" +" "+ shiftNodeDetails.getSourceSprintNo() +" "+ "--target_sprint" +" "+ shiftNodeDetails.getTargetSprintNo() +" "+ "--node_based" +" "+ "--selected_node" +" "+ nodeName +" "+ "--sprint_shifting");

							Process process = processBuilder.start();
							StringBuilder output = new StringBuilder();
							BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							int exitCode = process.waitFor();
							if (exitCode == 0) {
								LOGGER.info("Node Based Script for Shift Sprint executed successfully");
								message.append("Script for Shift Sprint executed successfully");
							} else {
								message.append("Exception occurred while executing Shift Sprint Script: ExitCode "+exitCode);
								throw new Exception("Exception occurred while executing Node Based script for Shift Sprint: ExitCode "+exitCode);
							}
						}
					} else if(sprintType.equalsIgnoreCase("ApplicationBased")) {
						if(shiftNodeDetails.getNodeName()!=null) {
							LOGGER.info("calling python script to regenerate node based sprint plan for Shift Node");
							ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptForShiftNodesApplicationBased, "--csrf", csrfToken, "--project_name", projectName,
									"--shift_node", Sanitization.sanitizeInput(shiftNodeDetails.getNodeName()), "--source_wave", shiftNodeDetails.getSourceWaveNo(), "--target_wave", shiftNodeDetails.getTargetWaveNo(),
									"--source_sprint", shiftNodeDetails.getSourceSprintNo(), "--target_sprint", shiftNodeDetails.getTargetSprintNo(), "--node_based", "--selected_node", nodeName);

							LOGGER.info("python3.9" +" "+ scriptForShiftNodesApplicationBased +" "+ "--csrf" +" "+ csrfToken +" "+ "--project_name" +" "+ projectName +" "+
									"--shift_node" +" "+ Sanitization.sanitizeInput(shiftNodeDetails.getNodeName()) +" "+ "--source_wave" +" "+ shiftNodeDetails.getSourceWaveNo() +" "+ "--target_wave" +" "+ shiftNodeDetails.getTargetWaveNo() +" "+
									"--source_sprint" +" "+ shiftNodeDetails.getSourceSprintNo() +" "+ "--target_sprint" +" "+ shiftNodeDetails.getTargetSprintNo() +" "+ "--node_based" +" "+ "--selected_node" +" "+ nodeName);

							Process process = processBuilder.start();
							StringBuilder output = new StringBuilder();
							BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							int exitCode = process.waitFor();
							if (exitCode == 0) {
								LOGGER.info("Node Based Script for Shift Node executed successfully");
								message.append("Script for Shift Node executed successfully");
							} else {
								message.append("Exception occurred while executing Shift Node Script: ExitCode "+exitCode);
								throw new Exception("Exception occurred while executing Node Based script for Shift Nodes: ExitCode "+exitCode);
							}
						} else {
							LOGGER.info("calling python script to regenerate node based sprint plan for Shift Sprint");
							ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptForShiftNodesApplicationBased, "--csrf", csrfToken, "--project_name", projectName,
									"--source_wave", shiftNodeDetails.getSourceWaveNo(), "--target_wave", shiftNodeDetails.getTargetWaveNo(),
									"--source_sprint", shiftNodeDetails.getSourceSprintNo(), "--target_sprint", shiftNodeDetails.getTargetSprintNo(), "--node_based", "--selected_node", nodeName, "--sprint_shifting");

							LOGGER.info("python3.9" +" "+ scriptForShiftNodesApplicationBased +" "+ "--csrf" +" "+ csrfToken +" "+ "--project_name" +" "+ projectName +" "+
									"--source_wave" +" "+ shiftNodeDetails.getSourceWaveNo() +" "+ "--target_wave" +" "+ shiftNodeDetails.getTargetWaveNo() +" "+
									"--source_sprint" +" "+ shiftNodeDetails.getSourceSprintNo() +" "+ "--target_sprint" +" "+ shiftNodeDetails.getTargetSprintNo() +" "+ "--node_based" +" "+ "--selected_node" +" "+ nodeName +" "+ "--sprint_shifting");

							Process process = processBuilder.start();
							StringBuilder output = new StringBuilder();
							BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
							String line;
							while ((line = reader.readLine()) != null) {
								output.append(line).append("\n");
							}
							int exitCode = process.waitFor();
							if (exitCode == 0) {
								LOGGER.info("Node Based Script for Shift Sprint executed successfully");
								message.append("Script for Shift Sprint executed successfully");
							} else {
								message.append("Exception occurred while executing Shift Sprint Script: ExitCode "+exitCode);
								throw new Exception("Exception occurred while executing Node Based script for Shift Sprint: ExitCode "+exitCode);
							}
						}
					}
				} else {
					message.append("Data saved successfully");
				}
				waveDetailUi = getNodeBasedSprintPlanCustom(projectName, nodeName, sprintType);
			}
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in saveDataOrExecuteScriptForShiftNode Service "+ex.getMessage());
		}
		return waveDetailUi;
	}

	public List<String> getNodeNames(String projectName, String sprintType) {
		projectName = Sanitization.sanitizeInput(projectName);
		sprintType = Sanitization.sanitizeInput(sprintType);
		
		List<String> nodeNamesList = new ArrayList<>();
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			if(sprintType.equalsIgnoreCase("ObjectApplicationBased")) {
				nodeNamesList = hotspotDao.getNodeNames(projectName);
			} else {
				nodeNamesList = hotspotDao.getNodeNamesApplicationBased(projectName);
			}
		} catch(Exception ex) {
			LOGGER.error("Exception occurred in getNodeNames Service "+ex.getMessage());
		}
		return nodeNamesList;
	}

	public List<WaveDetailUi> executeScriptForNodeBasedSprintPlan(String projectName, String nodeName, String sprintType) throws Exception {
		projectName = Sanitization.sanitizeInput(projectName);
		//nodeName = Sanitization.sanitizeInput(nodeName);
		sprintType = Sanitization.sanitizeInput(sprintType);
		
		List<WaveDetailUi> waveDetailUiList = new ArrayList<>();
		if(sprintType.equalsIgnoreCase("ObjectApplicationBased") || sprintType.equalsIgnoreCase("OnlyObject")) {
			int count = nodeBasedSprintPlanCustomObjectBasedRepository.getCount(projectName, nodeName);
			if(count==0) {
				ProcessBuilder processBuilder = new ProcessBuilder("python3.9", nodeBasedSprintPlanScriptForObjectBased, csrfToken, projectName, nodeName);
				LOGGER.info("python3.9 " + nodeBasedSprintPlanScriptForObjectBased + " " + csrfToken + " " + projectName + " " + nodeName);
				Process process = processBuilder.start();

				StringBuilder output = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
				int exitCode = process.waitFor();
				if (exitCode == 0) {
					LOGGER.info("NodeBased Script executed successfully");
				} else {
					throw new Exception("Exception occurred while executing NodeBased Script: exitCode "+exitCode);
				}
			} else {
				LOGGER.info("Data already present in presentation.node_based_sprint_plan_custom for project: "+projectName+" and nodeName: "+nodeName);
			}
		} else if(sprintType.equalsIgnoreCase("ApplicationBased")) {
			int count = nodeBasedSprintPlanCustomApplicationBasedRepository.getCount(projectName, nodeName);
			if(count==0) {
				ProcessBuilder processBuilder = new ProcessBuilder("python3.9", nodeBasedSprintPlanScriptForApplicationBased, csrfToken, projectName, nodeName);
				LOGGER.info("python3.9 " + nodeBasedSprintPlanScriptForApplicationBased + " " + csrfToken + " " + projectName + " " + nodeName);
				Process process = processBuilder.start();

				StringBuilder output = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
				int exitCode = process.waitFor();
				if (exitCode == 0) {
					LOGGER.info("NodeBased Script executed successfully");
				} else {
					throw new Exception("Exception occurred while executing NodeBased Script: exitCode "+exitCode);
				}
			} else {
				LOGGER.info("Data already present in presentation.node_based_sprint_plan_custom for project: "+projectName+" and nodeName: "+nodeName);
			}
		}
		waveDetailUiList = getNodeBasedSprintPlanCustom(projectName, nodeName, sprintType);
		return waveDetailUiList;
	}

	public List<WaveDetailUi> getNodeBasedSprintPlan(String projectName, String selectedNode, String sprintType) {
		List<WaveDetailUi> waveDetailUiList = new ArrayList<>();
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			//selectedNode = Sanitization.sanitizeInput(selectedNode);
			sprintType = Sanitization.sanitizeInput(sprintType);
			
			if(sprintType.equalsIgnoreCase("ObjectApplicationBased")) {
				List<Integer> waveList = nodeBasedSprintPlanObjectBasedRepository.getWaveList(projectName, selectedNode);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = nodeBasedSprintPlanObjectBasedRepository.getSprintList(projectName, selectedNode, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<NodeBasedSprintPlanObjectBased> sprintDataList = nodeBasedSprintPlanObjectBasedRepository.getDataForEachSprint(projectName, selectedNode, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} else if(sprintType.equalsIgnoreCase("ApplicationBased")) {
				List<Integer> waveList = nodeBasedSprintPlanApplicationBasedRepository.getWaveList(projectName, selectedNode);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = nodeBasedSprintPlanApplicationBasedRepository.getSprintList(projectName, selectedNode, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<NodeBasedSprintPlanApplicationBased> sprintDataList = nodeBasedSprintPlanApplicationBasedRepository.getDataForEachSprint(projectName, selectedNode, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} else if(sprintType.equalsIgnoreCase("OnlyObject")) {
				List<Integer> waveList = nodeBasedSprintPlanObjectBasedRepository.getWaveListOnlyObject(projectName, selectedNode);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = nodeBasedSprintPlanObjectBasedRepository.getSprintListOnlyObject(projectName, selectedNode, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<NodeBasedSprintPlanObjectBased> sprintDataList = nodeBasedSprintPlanObjectBasedRepository.getDataForEachSprintOnlyObject(projectName, selectedNode, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} 
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getNodeBasedSprintPlan Service "+ex.getMessage());
		}
		return waveDetailUiList;
	}

	public List<WaveDetailUi> getNodeBasedSprintPlanCustom(String projectName, String selectedNode, String sprintType) {
		List<WaveDetailUi> waveDetailUiList = new ArrayList<>();
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			//selectedNode = Sanitization.sanitizeInput(selectedNode);
			sprintType = Sanitization.sanitizeInput(sprintType);
			
			if(sprintType.equalsIgnoreCase("ObjectApplicationBased")) {
				List<Integer> waveList = nodeBasedSprintPlanCustomObjectBasedRepository.getWaveList(projectName, selectedNode);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = nodeBasedSprintPlanCustomObjectBasedRepository.getSprintList(projectName, selectedNode, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<NodeBasedSprintPlanCustomObjectBased> sprintDataList = nodeBasedSprintPlanCustomObjectBasedRepository.getDataForEachSprint(projectName, selectedNode, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							waveDetailUi.setAdditionalText(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDetailUi.setAdditionalText(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} else if(sprintType.equalsIgnoreCase("ApplicationBased")) {
				List<Integer> waveList = nodeBasedSprintPlanCustomApplicationBasedRepository.getWaveList(projectName, selectedNode);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = nodeBasedSprintPlanCustomApplicationBasedRepository.getSprintList(projectName, selectedNode, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<NodeBasedSprintPlanCustomApplicationBased> sprintDataList = nodeBasedSprintPlanCustomApplicationBasedRepository.getDataForEachSprint(projectName, selectedNode, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							waveDetailUi.setAdditionalText(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDetailUi.setAdditionalText(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} else if(sprintType.equalsIgnoreCase("OnlyObject")) {
				List<Integer> waveList = nodeBasedSprintPlanCustomObjectBasedRepository.getWaveListOnlyObject(projectName, selectedNode);
				for(Integer wave: waveList) {
					WaveDetailUi waveDetailUi = new WaveDetailUi();
					waveDetailUi.setWave("Wave "+wave);
					waveDetailUi.setAdditionalText("");

					List<Integer> sprintList = nodeBasedSprintPlanCustomObjectBasedRepository.getSprintListOnlyObject(projectName, selectedNode, wave);
					List<SprintDetailUi> sprintDetailList = new ArrayList<>();
					for(Integer sprint: sprintList) {
						SprintDetailUi sprintDetailUi = new SprintDetailUi();
						sprintDetailUi.setSprint("Sprint "+sprint);
						sprintDetailUi.setAdditionalText("");

						List<SprintDataUi> sprintDataUiList = new ArrayList<>();
						List<NodeBasedSprintPlanCustomObjectBased> sprintDataList = nodeBasedSprintPlanCustomObjectBasedRepository.getDataForEachSprintOnlyObject(projectName, selectedNode, wave, sprint);
						sprintDataList.forEach(sprintData-> {
							SprintDataUi sprintDataUi = new SprintDataUi();
							sprintDataUi.setWaveName(sprintData.getWaveName());
							waveDetailUi.setAdditionalText(sprintData.getWaveName());
							sprintDataUi.setWaveSprint(sprintData.getWaveSprint());
							sprintDataUi.setSprintName(sprintData.getSprintName());
							sprintDetailUi.setAdditionalText(sprintData.getSprintName());
							sprintDataUi.setMigrationType(sprintData.getMigrationType());
							sprintDataUi.setStartDate(sprintData.getStartDate());
							sprintDataUi.setEndDate(sprintData.getEndDate());
							sprintDataUi.setDuration(sprintData.getDuration());
							sprintDataUi.setSize(sprintData.getSize());
							sprintDataUi.setComplexity(sprintData.getComplexity());
							sprintDataUi.setStatus(sprintData.getStatus());
							sprintDataUi.setUnit(sprintData.getUnit());
							sprintDataUi.setValue(sprintData.getValue());
							sprintDataUiList.add(sprintDataUi);
						});
						sprintDetailUi.setData(sprintDataUiList);
						sprintDetailList.add(sprintDetailUi);
					}
					waveDetailUi.setSprints(sprintDetailList);
					waveDetailUiList.add(waveDetailUi);
				}
			} 
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getNodeBasedSprintPlanCustom Service "+ex.getMessage());
		}
		return waveDetailUiList;
	}

	public void updateNodeBasedSprintPlanCustom(List<WaveDetailUi> waveDetailUiList, String projectName, String selectedNode, String sprintType) {
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			//selectedNode = Sanitization.sanitizeInput(selectedNode);
			sprintType = Sanitization.sanitizeInput(sprintType);
			
			if(sprintType.equalsIgnoreCase("ObjectApplicationBased")) {
				for(WaveDetailUi waveDetailUi: waveDetailUiList) {
					Integer Wave = Integer.valueOf(waveDetailUi.getWave().replace("Wave", "").trim());
					LOGGER.info("deleting existing data for wave: "+Wave);
					nodeBasedSprintPlanCustomObjectBasedRepository.deleteByProjectNameAndSelectedNodeAndWave(projectName,selectedNode,Wave);

					List<SprintDetailUi> sprintDetailUiList = waveDetailUi.getSprints();
					for(SprintDetailUi sprintDetailUi: sprintDetailUiList) {
						Integer Sprint = Integer.valueOf(sprintDetailUi.getSprint().replace("Sprint", "").trim());
						List<SprintDataUi> sprintDataUiList = sprintDetailUi.getData();
						for(SprintDataUi sprintDataUi: sprintDataUiList) {
							NodeBasedSprintPlanCustomObjectBased nodeBasedSprintPlanCustomObjectBased = new NodeBasedSprintPlanCustomObjectBased();
							nodeBasedSprintPlanCustomObjectBased.setProjectName(projectName);
							nodeBasedSprintPlanCustomObjectBased.setSelectedNode(selectedNode);
							nodeBasedSprintPlanCustomObjectBased.setWave(Wave);
							nodeBasedSprintPlanCustomObjectBased.setWaveName(sprintDataUi.getWaveName());
							nodeBasedSprintPlanCustomObjectBased.setWaveSprint(sprintDataUi.getWaveSprint());
							nodeBasedSprintPlanCustomObjectBased.setSprint(Sprint);
							nodeBasedSprintPlanCustomObjectBased.setSprintName(sprintDataUi.getSprintName());
							nodeBasedSprintPlanCustomObjectBased.setMigrationType(sprintDataUi.getMigrationType());
							nodeBasedSprintPlanCustomObjectBased.setStartDate(sprintDataUi.getStartDate());
							nodeBasedSprintPlanCustomObjectBased.setEndDate(sprintDataUi.getEndDate());
							nodeBasedSprintPlanCustomObjectBased.setDuration(sprintDataUi.getDuration());
							nodeBasedSprintPlanCustomObjectBased.setSize(sprintDataUi.getSize());
							nodeBasedSprintPlanCustomObjectBased.setComplexity(sprintDataUi.getComplexity());
							nodeBasedSprintPlanCustomObjectBased.setStatus(sprintDataUi.getStatus());
							nodeBasedSprintPlanCustomObjectBased.setUnit(sprintDataUi.getUnit());
							nodeBasedSprintPlanCustomObjectBased.setValue(sprintDataUi.getValue());
							nodeBasedSprintPlanCustomObjectBasedRepository.saveAndFlush(nodeBasedSprintPlanCustomObjectBased);
						}
					}
				}
			} else if(sprintType.equalsIgnoreCase("ApplicationBased")) {
				for(WaveDetailUi waveDetailUi: waveDetailUiList) {
					Integer Wave = Integer.valueOf(waveDetailUi.getWave().replace("Wave", "").trim());
					LOGGER.info("deleting existing data for wave: "+Wave);
					nodeBasedSprintPlanCustomApplicationBasedRepository.deleteByProjectNameAndSelectedNodeAndWave(projectName,selectedNode,Wave);

					List<SprintDetailUi> sprintDetailUiList = waveDetailUi.getSprints();
					for(SprintDetailUi sprintDetailUi: sprintDetailUiList) {
						Integer Sprint = Integer.valueOf(sprintDetailUi.getSprint().replace("Sprint", "").trim());
						List<SprintDataUi> sprintDataUiList = sprintDetailUi.getData();
						for(SprintDataUi sprintDataUi: sprintDataUiList) {
							NodeBasedSprintPlanCustomApplicationBased nodeBasedSprintPlanCustomApplicationBased = new NodeBasedSprintPlanCustomApplicationBased();
							nodeBasedSprintPlanCustomApplicationBased.setProjectName(projectName);
							nodeBasedSprintPlanCustomApplicationBased.setSelectedNode(selectedNode);
							nodeBasedSprintPlanCustomApplicationBased.setWave(Wave);
							nodeBasedSprintPlanCustomApplicationBased.setWaveName(sprintDataUi.getWaveName());
							nodeBasedSprintPlanCustomApplicationBased.setWaveSprint(sprintDataUi.getWaveSprint());
							nodeBasedSprintPlanCustomApplicationBased.setSprint(Sprint);
							nodeBasedSprintPlanCustomApplicationBased.setSprintName(sprintDataUi.getSprintName());
							nodeBasedSprintPlanCustomApplicationBased.setMigrationType(sprintDataUi.getMigrationType());
							nodeBasedSprintPlanCustomApplicationBased.setStartDate(sprintDataUi.getStartDate());
							nodeBasedSprintPlanCustomApplicationBased.setEndDate(sprintDataUi.getEndDate());
							nodeBasedSprintPlanCustomApplicationBased.setDuration(sprintDataUi.getDuration());
							nodeBasedSprintPlanCustomApplicationBased.setSize(sprintDataUi.getSize());
							nodeBasedSprintPlanCustomApplicationBased.setComplexity(sprintDataUi.getComplexity());
							nodeBasedSprintPlanCustomApplicationBased.setStatus(sprintDataUi.getStatus());
							nodeBasedSprintPlanCustomApplicationBased.setUnit(sprintDataUi.getUnit());
							nodeBasedSprintPlanCustomApplicationBased.setValue(sprintDataUi.getValue());
							nodeBasedSprintPlanCustomApplicationBasedRepository.saveAndFlush(nodeBasedSprintPlanCustomApplicationBased);
						}
					}
				}
			} else if(sprintType.equalsIgnoreCase("OnlyObject")) {
				for(WaveDetailUi waveDetailUi: waveDetailUiList) {
					Integer Wave = Integer.valueOf(waveDetailUi.getWave().replace("Wave", "").trim());
					LOGGER.info("deleting existing data for wave: "+Wave);
					nodeBasedSprintPlanCustomObjectBasedRepository.deleteByProjectNameAndSelectedNodeAndWaveOnlyObject(projectName,selectedNode,Wave);

					List<SprintDetailUi> sprintDetailUiList = waveDetailUi.getSprints();
					for(SprintDetailUi sprintDetailUi: sprintDetailUiList) {
						Integer Sprint = Integer.valueOf(sprintDetailUi.getSprint().replace("Sprint", "").trim());
						List<SprintDataUi> sprintDataUiList = sprintDetailUi.getData();
						for(SprintDataUi sprintDataUi: sprintDataUiList) {
							NodeBasedSprintPlanCustomObjectBased nodeBasedSprintPlanCustomObjectBased = new NodeBasedSprintPlanCustomObjectBased();
							nodeBasedSprintPlanCustomObjectBased.setProjectName(projectName);
							nodeBasedSprintPlanCustomObjectBased.setSelectedNode(selectedNode);
							nodeBasedSprintPlanCustomObjectBased.setWave(Wave);
							nodeBasedSprintPlanCustomObjectBased.setWaveName(sprintDataUi.getWaveName());
							nodeBasedSprintPlanCustomObjectBased.setWaveSprint(sprintDataUi.getWaveSprint());
							nodeBasedSprintPlanCustomObjectBased.setSprint(Sprint);
							nodeBasedSprintPlanCustomObjectBased.setSprintName(sprintDataUi.getSprintName());
							nodeBasedSprintPlanCustomObjectBased.setMigrationType(sprintDataUi.getMigrationType());
							nodeBasedSprintPlanCustomObjectBased.setStartDate(sprintDataUi.getStartDate());
							nodeBasedSprintPlanCustomObjectBased.setEndDate(sprintDataUi.getEndDate());
							nodeBasedSprintPlanCustomObjectBased.setDuration(sprintDataUi.getDuration());
							nodeBasedSprintPlanCustomObjectBased.setSize(sprintDataUi.getSize());
							nodeBasedSprintPlanCustomObjectBased.setComplexity(sprintDataUi.getComplexity());
							nodeBasedSprintPlanCustomObjectBased.setStatus(sprintDataUi.getStatus());
							nodeBasedSprintPlanCustomObjectBased.setUnit(sprintDataUi.getUnit());
							nodeBasedSprintPlanCustomObjectBased.setValue(sprintDataUi.getValue());
							nodeBasedSprintPlanCustomObjectBasedRepository.saveAndFlush(nodeBasedSprintPlanCustomObjectBased);
						}
					}
				}
			} 
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in updateNodeBasedSprintPlanCustom Service "+ex.getMessage());
		}
	}
	
	public String getSprintPlanStatus(String projectName) {
		String status = "";
		try {
			projectName = Sanitization.sanitizeInput(projectName);	
	
			SprintLogStatus sprintLogStatus = sprintLogStatusRepository.findByProjectName(projectName);
			status = sprintLogStatus.getStatus();
		} catch(Exception ex) {
			status = "Sprint Plan never generated for Project "+projectName;
		}
		return status;
	}

	public List<WavePlanDto> getToolRecommendedWavePlan(String projectName) {
		return sprintDetailsObjectBasedRepository.getToolRecommendedWavePlanDetails(projectName);
	}

	public List<WavePlanDto> getCustomWavePlan(String projectName) {
		return sprintDetailsCustomObjectBasedRepository.getCustomWavePlanDetails(projectName);
	}

	public String generateWavePlanReport(String projectName) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		String fileNameWithPath = wavePlanReportFileLocation.concat("Wave_Plan_Report_").concat(projectName)
						.concat(GeneralConstants.XLSX_EXTENSION);
		prepareToolRecommendedWavePlan(workbook, fileNameWithPath, projectName);
		prepareCustomWavePlan(workbook, fileNameWithPath, projectName);
		return fileNameWithPath;
	}

	private void prepareToolRecommendedWavePlan(XSSFWorkbook workbook, String fileName, String projectName) {
		try {
			LOGGER.info("Tool Recommended Wave Plan Report start for projectName: " + projectName);
			// Tool Recommended Wave Plan constants
			String wavePlan = "Tool Recommended Wave Plan";
			String projectNameHeading = "Project Name";
			String wave = "Wave";
			String waveSprint = "Objects";
			String sprint = "Sprint";
			String migrationType = "Migration Type";
			String scriptType = "Script Type";
			String nodeType = "Node Type";
			String startDate = "Start Date";
			String endDate = "End Date";
			String duration = "Duration";
			String size = "Size (MB)";
			String complexity = "Complexity";
			String status = "Status";

			List<WavePlanDto> details = getToolRecommendedWavePlan(projectName);

			XSSFSheet sheet = workbook.createSheet(wavePlan);
			XSSFCellStyle titleStyle = getTitleStyle(workbook);
			XSSFCellStyle valueStyle = getValueStyle(workbook);

			if (details.isEmpty()) {
				XSSFRow xSSFRow1 = sheet.createRow(0);
				Cell cell_1_1 = xSSFRow1.createCell(0);
				cell_1_1.setCellValue("No data available for the specified project");
				cell_1_1.setCellStyle(valueStyle);
				sheet.autoSizeColumn(0);
				saveWorkbookToExcel(workbook, fileName);
				LOGGER.info("Tool Recommended Wave Plan Report end as there is no data present for this project");
				return;
			}

			// Below parenthesis added to keep a block of code at one place for better readability
			{
				XSSFRow xSSFRow1 = sheet.createRow(0);
				Cell cell_1_1 = xSSFRow1.createCell(0);
				cell_1_1.setCellValue(projectNameHeading);
				cell_1_1.setCellStyle(titleStyle);

				Cell cell_1_2 = xSSFRow1.createCell(1);
				cell_1_2.setCellValue(wave);
				cell_1_2.setCellStyle(titleStyle);

				Cell cell_1_3 = xSSFRow1.createCell(2);
				cell_1_3.setCellValue(waveSprint);
				cell_1_3.setCellStyle(titleStyle);

				Cell cell_1_4 = xSSFRow1.createCell(3);
				cell_1_4.setCellValue(sprint);
				cell_1_4.setCellStyle(titleStyle);

				Cell cell_1_5 = xSSFRow1.createCell(4);
				cell_1_5.setCellValue(migrationType);
				cell_1_5.setCellStyle(titleStyle);

				Cell cell_1_6 = xSSFRow1.createCell(5);
				cell_1_6.setCellValue(scriptType);
				cell_1_6.setCellStyle(titleStyle);

				Cell cell_1_7 = xSSFRow1.createCell(6);
				cell_1_7.setCellValue(nodeType);
				cell_1_7.setCellStyle(titleStyle);

				Cell cell_1_8 = xSSFRow1.createCell(7);
				cell_1_8.setCellValue(startDate);
				cell_1_8.setCellStyle(titleStyle);

				Cell cell_1_9 = xSSFRow1.createCell(8);
				cell_1_9.setCellValue(endDate);
				cell_1_9.setCellStyle(titleStyle);

				Cell cell_1_10 = xSSFRow1.createCell(9);
				cell_1_10.setCellValue(duration);
				cell_1_10.setCellStyle(titleStyle);

				Cell cell_1_11 = xSSFRow1.createCell(10);
				cell_1_11.setCellValue(size);
				cell_1_11.setCellStyle(titleStyle);

				Cell cell_1_12 = xSSFRow1.createCell(11);
				cell_1_12.setCellValue(complexity);
				cell_1_12.setCellStyle(titleStyle);

				Cell cell_1_13 = xSSFRow1.createCell(12);
				cell_1_13.setCellValue(status);
				cell_1_13.setCellStyle(titleStyle);
			}

			int rowCounter = 1;
			for (WavePlanDto detail: details) {
				XSSFRow xSSFRow = sheet.createRow(rowCounter++);

				Cell cell_1 = xSSFRow.createCell(0);
				cell_1.setCellValue(detail.getProjectName());
				cell_1.setCellStyle(valueStyle);

				Cell cell_2 = xSSFRow.createCell(1);
				cell_2.setCellValue(detail.getWave());
				cell_2.setCellStyle(valueStyle);

				Cell cell_3 = xSSFRow.createCell(2);
				cell_3.setCellValue(detail.getWaveSprint());
				cell_3.setCellStyle(valueStyle);

				Cell cell_4 = xSSFRow.createCell(3);
				cell_4.setCellValue(detail.getSprint());
				cell_4.setCellStyle(valueStyle);

				Cell cell_5 = xSSFRow.createCell(4);
				cell_5.setCellValue(detail.getMigrationType());
				cell_5.setCellStyle(valueStyle);

				Cell cell_6 = xSSFRow.createCell(5);
				cell_6.setCellValue(detail.getScriptType());
				cell_6.setCellStyle(valueStyle);

				Cell cell_7 = xSSFRow.createCell(6);
				cell_7.setCellValue(detail.getNodeType());
				cell_7.setCellStyle(valueStyle);

				Cell cell_8 = xSSFRow.createCell(7);
				cell_8.setCellValue(
						Objects.isNull(detail.getStartDate()) ? "" : detail.getStartDate().toString());
				cell_8.setCellStyle(valueStyle);

				Cell cell_9 = xSSFRow.createCell(8);
				cell_9.setCellValue(
						Objects.isNull(detail.getEndDate()) ? "" : detail.getEndDate().toString());
				cell_9.setCellStyle(valueStyle);

				Cell cell_10 = xSSFRow.createCell(9);
				cell_10.setCellValue(detail.getDuration());
				cell_10.setCellStyle(valueStyle);

				Cell cell_11 = xSSFRow.createCell(10);
				cell_11.setCellValue(detail.getSize());
				cell_11.setCellStyle(valueStyle);

				Cell cell_12 = xSSFRow.createCell(11);
				cell_12.setCellValue(detail.getComplexity());
				cell_12.setCellStyle(valueStyle);

				Cell cell_13 = xSSFRow.createCell(12);
				cell_13.setCellValue(detail.getStatus());
				cell_13.setCellStyle(valueStyle);
			}

			for (int i = 0; i < 13; i++) {
				sheet.autoSizeColumn(i);
			}

			CellRangeAddress rangeAddress = new CellRangeAddress(1, sheet.getLastRowNum(), 0, 0);
			sheet.addMergedRegion(rangeAddress);

			saveWorkbookToExcel(workbook, fileName);
			LOGGER.info("Tool Recommended Wave Plan Report end");
		} catch (Exception ex) {
			LOGGER.error("Exception occurred while preparing Tool Recommended Wave Plan sheet: ", ex);
		}
	}

	private void prepareCustomWavePlan(XSSFWorkbook workbook, String fileName, String projectName) {
		try {
			LOGGER.info("Custom Wave Plan Report start for project: " + projectName);
			// Custom Wave Plan constants
			String wavePlan = "Custom Wave Plan";
			String projectNameHeading = "Project Name";
			String wave = "Wave";
			String waveSprint = "Objects";
			String sprint = "Sprint";
			String migrationType = "Migration Type";
			String scriptType = "Script Type";
			String nodeType = "Node Type";
			String startDate = "Start Date";
			String endDate = "End Date";
			String duration = "Duration";
			String size = "Size (MB)";
			String complexity = "Complexity";
			String status = "Status";

			List<WavePlanDto> details = getCustomWavePlan(projectName);

			XSSFSheet sheet = workbook.createSheet(wavePlan);
			XSSFCellStyle titleStyle = getTitleStyle(workbook);
			XSSFCellStyle valueStyle = getValueStyle(workbook);

			if (details.isEmpty()) {
				XSSFRow xSSFRow1 = sheet.createRow(0);
				Cell cell_1 = xSSFRow1.createCell(0);
				cell_1.setCellValue("No data available for the specified project");
				cell_1.setCellStyle(valueStyle);
				sheet.autoSizeColumn(0);
				saveWorkbookToExcel(workbook, fileName);
				LOGGER.info("Lineage Global Excel Custom Wave Plan Report end as there is no data present for this project");
				return;
			}
			// Below parenthesis added to keep a block of code at one place for better readability
			{
				XSSFRow xSSFRow1 = sheet.createRow(0);
				Cell cell_1_1 = xSSFRow1.createCell(0);
				cell_1_1.setCellValue(projectNameHeading);
				cell_1_1.setCellStyle(titleStyle);

				Cell cell_1_2 = xSSFRow1.createCell(1);
				cell_1_2.setCellValue(wave);
				cell_1_2.setCellStyle(titleStyle);

				Cell cell_1_3 = xSSFRow1.createCell(2);
				cell_1_3.setCellValue(waveSprint);
				cell_1_3.setCellStyle(titleStyle);

				Cell cell_1_4 = xSSFRow1.createCell(3);
				cell_1_4.setCellValue(sprint);
				cell_1_4.setCellStyle(titleStyle);

				Cell cell_1_5 = xSSFRow1.createCell(4);
				cell_1_5.setCellValue(migrationType);
				cell_1_5.setCellStyle(titleStyle);

				Cell cell_1_6 = xSSFRow1.createCell(5);
				cell_1_6.setCellValue(scriptType);
				cell_1_6.setCellStyle(titleStyle);

				Cell cell_1_7 = xSSFRow1.createCell(6);
				cell_1_7.setCellValue(nodeType);
				cell_1_7.setCellStyle(titleStyle);

				Cell cell_1_8 = xSSFRow1.createCell(7);
				cell_1_8.setCellValue(startDate);
				cell_1_8.setCellStyle(titleStyle);

				Cell cell_1_9 = xSSFRow1.createCell(8);
				cell_1_9.setCellValue(endDate);
				cell_1_9.setCellStyle(titleStyle);

				Cell cell_1_10 = xSSFRow1.createCell(9);
				cell_1_10.setCellValue(duration);
				cell_1_10.setCellStyle(titleStyle);

				Cell cell_1_11 = xSSFRow1.createCell(10);
				cell_1_11.setCellValue(size);
				cell_1_11.setCellStyle(titleStyle);

				Cell cell_1_12 = xSSFRow1.createCell(11);
				cell_1_12.setCellValue(complexity);
				cell_1_12.setCellStyle(titleStyle);

				Cell cell_1_13 = xSSFRow1.createCell(12);
				cell_1_13.setCellValue(status);
				cell_1_13.setCellStyle(titleStyle);
			}

			int rowCounter = 1;
			for (WavePlanDto detail: details) {
				XSSFRow xSSFRow = sheet.createRow(rowCounter++);

				Cell cell_1 = xSSFRow.createCell(0);
				cell_1.setCellValue(detail.getProjectName());
				cell_1.setCellStyle(valueStyle);

				Cell cell_2 = xSSFRow.createCell(1);
				cell_2.setCellValue(detail.getWave());
				cell_2.setCellStyle(valueStyle);

				Cell cell_3 = xSSFRow.createCell(2);
				cell_3.setCellValue(detail.getWaveSprint());
				cell_3.setCellStyle(valueStyle);

				Cell cell_4 = xSSFRow.createCell(3);
				cell_4.setCellValue(detail.getSprint());
				cell_4.setCellStyle(valueStyle);

				Cell cell_5 = xSSFRow.createCell(4);
				cell_5.setCellValue(detail.getMigrationType());
				cell_5.setCellStyle(valueStyle);

				Cell cell_6 = xSSFRow.createCell(5);
				cell_6.setCellValue(detail.getScriptType());
				cell_6.setCellStyle(valueStyle);

				Cell cell_7 = xSSFRow.createCell(6);
				cell_7.setCellValue(detail.getNodeType());
				cell_7.setCellStyle(valueStyle);

				Cell cell_8 = xSSFRow.createCell(7);
				cell_8.setCellValue(
						Objects.isNull(detail.getStartDate()) ? "" : detail.getStartDate().toString());
				cell_8.setCellStyle(valueStyle);

				Cell cell_9 = xSSFRow.createCell(8);
				cell_9.setCellValue(
						Objects.isNull(detail.getEndDate()) ? "" : detail.getEndDate().toString());
				cell_9.setCellStyle(valueStyle);

				Cell cell_10 = xSSFRow.createCell(9);
				cell_10.setCellValue(detail.getDuration());
				cell_10.setCellStyle(valueStyle);

				Cell cell_11 = xSSFRow.createCell(10);
				cell_11.setCellValue(detail.getSize());
				cell_11.setCellStyle(valueStyle);

				Cell cell_12 = xSSFRow.createCell(11);
				cell_12.setCellValue(detail.getComplexity());
				cell_12.setCellStyle(valueStyle);

				Cell cell_13 = xSSFRow.createCell(12);
				cell_13.setCellValue(detail.getStatus());
				cell_13.setCellStyle(valueStyle);
			}

			for (int i = 0; i < 13; i++) {
				sheet.autoSizeColumn(i);
			}

			CellRangeAddress rangeAddress = new CellRangeAddress(1, sheet.getLastRowNum(), 0, 0);
			sheet.addMergedRegion(rangeAddress);

			saveWorkbookToExcel(workbook, fileName);
			LOGGER.info("Lineage Global Excel Custom Wave Plan Report end");
		} catch (Exception ex) {
			LOGGER.error("Exception occurred while preparing Custom Wave Plan sheet: ", ex);
		}
	}

	public XSSFCellStyle getTitleStyle(XSSFWorkbook workbook) {

		XSSFColor myColor = new XSSFColor(new java.awt.Color(49, 134, 155),
				new DefaultIndexedColorMap());

		XSSFFont xSSFFont1 = workbook.createFont();
		xSSFFont1.setColor(IndexedColors.WHITE.getIndex());
		xSSFFont1.setUnderline((byte) 1);

		XSSFCellStyle titleStyle = workbook.createCellStyle();
		titleStyle.setFillForegroundColor(myColor);
		titleStyle.setFillPattern(FillPatternType.forInt((short) 1));
		titleStyle.setFont(xSSFFont1);
		titleStyle.setAlignment(HorizontalAlignment.forInt((short) 2));
		titleStyle.setBorderLeft(BorderStyle.THIN);
		titleStyle.setBorderRight(BorderStyle.THIN);
		titleStyle.setBorderTop(BorderStyle.THIN);
		titleStyle.setBorderBottom(BorderStyle.THIN);

		return titleStyle;
	}

	public XSSFCellStyle getValueStyle(XSSFWorkbook workbook) {
		XSSFFont xSSFFont3 = workbook.createFont();
		xSSFFont3.setColor(IndexedColors.BLACK.getIndex());

		XSSFCellStyle valueStyle = workbook.createCellStyle();
		valueStyle.setWrapText(true);
		valueStyle.setFont(xSSFFont3);
		valueStyle.setAlignment(HorizontalAlignment.forInt((short) 1));
		valueStyle.setBorderLeft(BorderStyle.THIN);
		valueStyle.setBorderRight(BorderStyle.THIN);
		valueStyle.setBorderTop(BorderStyle.THIN);
		valueStyle.setBorderBottom(BorderStyle.THIN);
		valueStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		return valueStyle;
	}

	private void saveWorkbookToExcel(XSSFWorkbook workbook, String fileName) throws IOException {
		File f = new File(fileName);
		f.setExecutable(false);
		f.setReadable(true);
		f.setWritable(true);
		if (!f.delete()) {
			LOGGER.info("Existing File is not able to delete. Please try again after some time.");
		}
		FileOutputStream outputStream = new FileOutputStream(f.getAbsolutePath());
		try {
			workbook.write(outputStream);
			outputStream.close();
		} catch (Throwable throwable) {
			try {
				outputStream.close();
			} catch (Throwable throwable1) {
				throwable.addSuppressed(throwable1);
			}
			throw throwable;
		}
	}
}