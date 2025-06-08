package com.cognizant.lineage.upload.service;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.exception.LineageRuntimeException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.dao.InformaticaLineageDao;
import com.cognizant.lineage.upload.model.JobStatus;
import com.cognizant.lineage.upload.model.JobStatusList;
import com.cognizant.lineage.upload.model.LineageJobStatusUI;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JobStatusServiceImpl implements JobStatusService {
	
	@Autowired
	private LineageJobStatusRepository lineageJobStatusRepository;

	@Autowired
	private LineageJobRepository lineageJobRepository;
	
	@Autowired
	private InformaticaLineageDao informaticaLineageDao;

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JobStatusServiceImpl.class);
	
	@Override
	public String getJobStatus(Long jobId) throws LineageBusinessException {
		String status = "";
		List<LineageJobStatus> stepStatus;
		if (jobId==null){
			stepStatus = lineageJobStatusRepository.findAllByMaxJobId();
		}
		else {
			Optional<LineageJob> lineageJob = lineageJobRepository.findById(jobId);
			if (lineageJob.isEmpty()) {
				throw new LineageBusinessException("No jobId found");
			}
			stepStatus = lineageJobStatusRepository.findAllByJobId(lineageJob.get().getJobId());
		}
		
		for(LineageJobStatus jobStatus: stepStatus) {	
			if (StringUtils.containsAnyIgnoreCase(jobStatus.getStepName(),"Lineage Identification")){
				String extra = jobStatus.getStepNo()+": "+jobStatus.getJobStatusDetails() +",";
				extra = extra.replaceAll("," , ", \u00A0 \u00A0 \u00A0 \u00A0 \u00A0 \u00A0 \u00A0 \u00A0 \u00A0 \u00A0 \u00A0");
				status += extra;
				continue;			  
			}
			status += jobStatus.getStepNo()+": "+ 
					  jobStatus.getNoOfFileProcessed()
					  +"/"+jobStatus.getNoOfFileReceived()+" "+
					  jobStatus.getStatus()+",";

		}
		
		return status;
	}
	
	@Override
	public List<LineageJobStatusUI> getAllLineageJobs() throws LineageBusinessException {
	
		List<LineageJob> lineageJobsList = lineageJobRepository.findAllByOrderByJobIdDesc();
		if(CollectionUtils.isEmpty(lineageJobsList)) {
			throw new LineageBusinessException("No jobs found");
		}
		String lineageJobString = "";
		List<LineageJobStatusUI> jobStatusUiList = new ArrayList<>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			for(LineageJob lineageJob: lineageJobsList) {
				Optional<LineageJobStatus> jobStatus = 
						lineageJobStatusRepository.findJobIdOrderByDescLimitTo1(lineageJob.getJobId());
				if (jobStatus.isPresent()) {
					LineageJobStatus jobStatus1 = jobStatus.get();
					lineageJobString = mapper.writeValueAsString(lineageJob);
					LineageJobStatusUI jobStatusUi = 
							mapper.readValue(lineageJobString, LineageJobStatusUI.class);
					String statusMessage = jobStatus1.getStepName() + " " + 
							jobStatus1.getStatus();
					jobStatusUi.setStatus(statusMessage);
					jobStatusUiList.add(jobStatusUi);
				}
			}
		} catch (JsonProcessingException e) {
			throw new LineageRuntimeException("Parsing Error");
		}
		return jobStatusUiList;
	}
	
	@Override
	public JobStatusList getExecutionStatus(Long jobId) throws LineageBusinessException {
		List<JobStatus> jobStatusList = new ArrayList<>();
		String status = "";
		List<LineageJobStatus> stepStatus;
		stepStatus = lineageJobStatusRepository.findAllByMaxJobId();

		Optional<LineageJob> lineageJob = lineageJobRepository.findById(jobId);
		if (lineageJob.isEmpty()) {
			throw new LineageBusinessException("No jobId found");
		}
		stepStatus = lineageJobStatusRepository.findAllByJobId(lineageJob.get().getJobId());

		if (stepStatus.isEmpty()) {
			return null;
		}

		//String jobStatusDetailsForColumnLineage = "";
		String jobStatusDetailsForLineageIdentification = "";
		String jobStatusDetailsString = "";
		List<String> stepListForProcessedFiles = Arrays.asList(TechnologyConstants.IDMC_FILE_PARSING,
				TechnologyConstants.TIVOLI_FILE_PARSING, TechnologyConstants.SSIS_SCRIPT_PARSING,
				TechnologyConstants.MLOAD_FILES_CLEANSING);

		for(LineageJobStatus lineageJobStatus: stepStatus) {
			if (StringUtils.equalsAnyIgnoreCase(lineageJobStatus.getStepName(), TechnologyConstants.LINEAGE_IDENTIFICATION)){
				jobStatusDetailsForLineageIdentification = lineageJobStatus.getJobStatusDetails();
                status = lineageJobStatus.getJobStatusDetails().replaceAll("\\P{Print}", "").replaceAll(",", "\n");
				JobStatus idJobStatus = prepareJobStatus(lineageJobStatus.getStepNo(), 
						lineageJobStatus.getStepName().toUpperCase(), lineageJobStatus.getStatus(), status);
				jobStatusList.add(idJobStatus);
				continue;
			}
			
			String statusMessage = lineageJobStatus.getNoOfFileProcessed() + "/" + 
					lineageJobStatus.getNoOfFileReceived()+" "+lineageJobStatus.getStatus();
			/**if(StringUtils.containsAnyIgnoreCase(lineageJobStatus.getStepName(),"ColumnLevelLineage Identification")){
				jobStatusDetailsForColumnLineage = lineageJobStatus.getJobStatusDetails();
			    statusMessage = lineageJobStatus.getJobStatusDetails().replaceAll(",", "\n");
			}**/

			if (StringUtils.containsAnyIgnoreCase(lineageJobStatus.getStepName(),TechnologyConstants.MLOAD_FILES_CLEANSING)){
				String jobStatusDetails = "";
				if (Objects.equals(lineageJobStatus.getNoOfFileReceived(), lineageJobStatus.getNoOfFileProcessed())) {
					jobStatusDetails = "No. of Files Received: " + lineageJobStatus.getNoOfFileReceived()
							+ ",No. of Files Processed: "+lineageJobStatus.getNoOfFileProcessed()
							+ ",LogFileLocation: "+lineageJobStatus.getLogFileLocation();
				} else {
					jobStatusDetails = "No. of Files Received: " + lineageJobStatus.getNoOfFileReceived()
							+ ",No. of Files Processed: "+lineageJobStatus.getNoOfFileProcessed();
				}
				jobStatusDetailsString = jobStatusDetails;
				statusMessage = lineageJobStatus.getJobStatusDetails().replaceAll(",", "\n");
			} else if (stepListForProcessedFiles.stream().anyMatch(step -> step.equalsIgnoreCase(lineageJobStatus.getStepName()))) {
				jobStatusDetailsString = lineageJobStatus.getJobStatusDetails();
				statusMessage = lineageJobStatus.getJobStatusDetails().replaceAll(",", "\n");
			}

			JobStatus jobStatus = prepareJobStatus(lineageJobStatus.getStepNo(), 
					lineageJobStatus.getStepName().toUpperCase(), lineageJobStatus.getStatus(), statusMessage);
			jobStatusList.add(jobStatus);
		}

		String technology = populateIncompleteSteps(jobStatusList, lineageJob.get());
		List<JobStatus> modifiedJobStatusList = getReducedStatusSteps(jobStatusList, technology);

		String statusSummary = prepareStatusSummary(modifiedJobStatusList, jobStatusDetailsForLineageIdentification,
				stepListForProcessedFiles, jobStatusDetailsString);
		
		JobStatusList statusList = new JobStatusList();
		statusList.setJobId(jobId);
		statusList.setJobStatusList(modifiedJobStatusList);
		statusList.setStatusSummary(statusSummary);
		
		return statusList;
	}

	private JobStatus prepareJobStatus(Integer stepNo, String stepName, 
			String status, String statusMessage) {
		JobStatus jobStatus = new JobStatus();
		jobStatus.setStep(stepNo);
		jobStatus.setStepName(stepName);
		jobStatus.setMessage(statusMessage);
		if (status.toLowerCase().contains(GeneralConstants.COMPLETED.toLowerCase())) {
			jobStatus.setStatus(GeneralConstants.COMPLETED);
		} else if (status.toLowerCase().contains(GeneralConstants.STATUS_IN_PROCESS.toLowerCase()) || 
				status.toLowerCase().contains(GeneralConstants.STATUS_IN_PROGRESS.toLowerCase())) {
			jobStatus.setStatus(GeneralConstants.IN_PROCESS);
		} else if (status.toLowerCase().contains(GeneralConstants.ERROR)) {
			jobStatus.setStatus(GeneralConstants.ERROR);
		} else {
			jobStatus.setStatus(GeneralConstants.NOT_STARTED);
		}
		return jobStatus;
	}
	
	private String populateIncompleteSteps(List<JobStatus> jobStatusList, LineageJob lineageJob) {
		try {
			String technology = getRequiredTechnology(lineageJob);
			Integer requiredSteps = TechnologyConstants.NO_OF_STEPS_FOR_TECH.getOrDefault(technology, 8);
			if (requiredSteps.equals(jobStatusList.size())) {
				return technology;
			}
			for (int stepNo = jobStatusList.size() + 1; stepNo <= requiredSteps; stepNo++) {
				JobStatus jobStatus = prepareJobStatus(stepNo,
						TechnologyConstants.STEPS_FOR_TECH.get(technology)[stepNo - 1],
						GeneralConstants.NOT_STARTED, "-");
				jobStatusList.add(jobStatus);
			}
			return technology;
		} catch (NullPointerException e) {
			LOGGER.info("Null pointer Exception occurred: {}", e.getStackTrace()[1]);
		}
		return "";
	}

	private String getRequiredTechnology(LineageJob lineageJob) {
		String parentTech = lineageJob.getJobParams();
		String technology = lineageJob.getTechnology();
		if(TechnologyConstants.DATASTAGE.equalsIgnoreCase(technology.trim())) {
			return informaticaLineageDao.getTechnologyBasedOnYesNoQueryCountInDatastage(Integer.parseInt(lineageJob.getJobId().toString()));
		}
		if (technology.toLowerCase().contains(TechnologyConstants.INFORMATICA.toLowerCase())) {
			int noOfSteps = informaticaLineageDao
					.infaSqlTableRowCountStatusFromScriptFilesInfo(Integer.parseInt(lineageJob.getJobId().toString()));
			if (noOfSteps != 0) {
				return TechnologyConstants.INFORMATICA_DYNAMIC;
			}
			return TechnologyConstants.INFORMATICA_DEFAULT;
		}
		if (TechnologyConstants.ORACLE.equalsIgnoreCase(parentTech) ||
				TechnologyConstants.TERADATA.equalsIgnoreCase(parentTech) ||
				TechnologyConstants.MS_SQL_SERVER.equalsIgnoreCase(parentTech)) {
			return parentTech.concat("-").concat(technology).toUpperCase();
		}
		if (TechnologyConstants.IDMC.contains(parentTech)) {
			return TechnologyConstants.IDMC;
		}
		return technology;
	}

	@Override
	public void updateJobStatusDetailsWithComplexityString(Long jobId) {
		List<LineageJobStatus> statusList = lineageJobStatusRepository.getJobStatusWithLikeString(jobId);
		String complexityString = GeneralConstants.COMPLEXITY_CALCULATION_COMPLETED_STATUS_STRING.concat(",");
		for (LineageJobStatus jobStatus : statusList){
            String jobDetails = jobStatus.getJobStatusDetails();
			int index = jobDetails.lastIndexOf(GeneralConstants.PROCESSED_FOR_PROJECT_STRING);
			StringBuilder jobDetailsBuilder = new StringBuilder(jobDetails);
			jobDetailsBuilder.insert(index, complexityString);
			jobStatus.setJobStatusDetails(jobDetailsBuilder.toString());
			lineageJobStatusRepository.save(jobStatus);
		}
	}

	@Override
	public String getDbUploadStatus(HashMap<String, String> jobIdWithTechMap) throws LineageBusinessException {
		Map<Long, String> sortedJobIdMap = new TreeMap<>();
		for (Map.Entry<String, String> entry : jobIdWithTechMap.entrySet()) {
			sortedJobIdMap.put(Long.valueOf(entry.getKey()), entry.getValue());
		}
		StringBuilder status = new StringBuilder();
		for (Map.Entry<Long, String> entry : sortedJobIdMap.entrySet()) {
			JobStatusList statusList = getExecutionStatus(entry.getKey());
			String statusForJobID = Objects.isNull(statusList) ? "" : statusList.getStatusSummary();
			//getJobStatus(entry.getKey());
			status.append("**************** ").append(entry.getValue().toUpperCase())
					.append(" Status Log ****************\n");
			status.append(!statusForJobID.isEmpty() ? statusForJobID : GeneralConstants.PROCESS_NOT_STARTED);
			status.append("\n\n");
		}
		return status.toString();
	}

	@Override
	public int getNoOfOccurrences(String dbUploadStatus) {
		boolean loopRun = true;
		int noOfOccurrences = 0;
		int lastFoundIndex = dbUploadStatus.indexOf(GeneralConstants.PROCESSED_FOR_PROJECT_STRING);
		if (lastFoundIndex > 0) {
			++noOfOccurrences;
		}
		do {
			lastFoundIndex = dbUploadStatus.indexOf(GeneralConstants.PROCESSED_FOR_PROJECT_STRING,
					lastFoundIndex+1);
			if (lastFoundIndex > 0) {
				++noOfOccurrences;
			}
			if (lastFoundIndex == -1) {
				loopRun = false;
			}
		} while (loopRun);
		return noOfOccurrences;
	}

	/**
	 *	This method is used to reduce the no of steps which needs to be displayed in UI screen.
	 *	Database steps won't be disturbed. This is purely for displaying in UI.
	 *	If you want to display all the steps, then you don't call this function and proceed with existing status list.
	 */
	private List<JobStatus> getReducedStatusSteps(List<JobStatus> jobStatusList, String technology) {
		List<JobStatus> modifiedJobStatusList = new ArrayList<>();
		try {
//			if (TechnologyConstants.REDUCED_STEPS_NOT_REQUIRED_TECHS.contains(technology)) {
//				return jobStatusList;
//			}
			List<JobStatus> scriptPreProcessingList = new ArrayList<>();
			List<JobStatus> scriptParsingList = new ArrayList<>();
			List<JobStatus> lineageIdentificationList = new ArrayList<>();
			List<JobStatus> columnLevelLineageList = new ArrayList<>();
//			boolean includeCleansingPreviousStep =
//					TechnologyConstants.INCLUDE_CLEANSING_PREVIOUS_STEP_REQUIRED_TECHS.contains(technology);
//			boolean includeParsingPreviousStep =
//					TechnologyConstants.INCLUDE_PARSING_PREVIOUS_STEP_REQUIRED_TECHS.contains(technology);
			for (JobStatus jobStatus : jobStatusList) {
				String reducedStepValue = TechnologyConstants.STEPS_REDUCTION_MAP
						.getOrDefault(jobStatus.getStepName().toUpperCase(), "default");
				switch (reducedStepValue) {
					case TechnologyConstants.SCRIPT_PREPROCESSING:
						scriptPreProcessingList.add(jobStatus);
						break;
					case TechnologyConstants.SCRIPT_PARSING:
						scriptParsingList.add(jobStatus);
						break;
					case TechnologyConstants.SCRIPT_LINEAGE_IDENTIFICATION:
					case TechnologyConstants.LINEAGE_IDENTIFICATION:
						lineageIdentificationList.add(jobStatus);
						break;
					/**case TechnologyConstants.COLUMN_LEVEL_LINEAGE_IDENTIFICATION:
						columnLevelLineageList.add(jobStatus);
						break;**/
					default:
//						if (!scriptPreProcessingList.isEmpty()) {
//							modifiedJobStatusList.add(getReducedJobStatus(scriptPreProcessingList, modifiedJobStatusList,
//									TechnologyConstants.SCRIPT_PREPROCESSING));
//							scriptPreProcessingList.clear();
//						}
						jobStatus.setStep(modifiedJobStatusList.size() + 1);
						modifiedJobStatusList.add(jobStatus);
						break;
				}
			}
			if (!scriptPreProcessingList.isEmpty()) {
				modifiedJobStatusList.add(getReducedJobStatus(scriptPreProcessingList, modifiedJobStatusList,
						TechnologyConstants.SCRIPT_PREPROCESSING));
				scriptPreProcessingList.clear();
			}
			if (!scriptParsingList.isEmpty()) {
				modifiedJobStatusList.add(getReducedJobStatus(scriptParsingList, modifiedJobStatusList,
						TechnologyConstants.SCRIPT_PARSING));
			}
			if (!lineageIdentificationList.isEmpty()) {
				modifiedJobStatusList.add(getReducedJobStatus(lineageIdentificationList, modifiedJobStatusList,
						TechnologyConstants.LINEAGE_IDENTIFICATION));
			}
			/**if (!columnLevelLineageList.isEmpty()) {
				modifiedJobStatusList.add(getReducedJobStatus(columnLevelLineageList, modifiedJobStatusList,
						false, TechnologyConstants.COLUMN_LEVEL_LINEAGE_IDENTIFICATION));
			}**/
		} catch (Exception e) {
			LOGGER.info("Exception occurred: {}", e.getMessage());
		}
		return modifiedJobStatusList;
	}

	private JobStatus getReducedJobStatus(List<JobStatus> statusList, List<JobStatus> modifiedJobStatusList,
										  String stepName) {
		boolean isError = false;
		boolean isNotStarted = false;
		boolean isInProcess = false;
		boolean isCompleted = false;
		boolean isPreviousStatusError = false;

		StringBuilder statusMessage = new StringBuilder();
		JobStatus reducedStatus = new JobStatus();

		// checking if previous status of step is error or not
		if (!modifiedJobStatusList.isEmpty()) {
			JobStatus previousModifiedJobStatus = modifiedJobStatusList.get(modifiedJobStatusList.size() - 1);
			isPreviousStatusError = GeneralConstants.ERROR.equalsIgnoreCase(previousModifiedJobStatus.getStatus());
		}
		if (statusList.size() == 1) {
			JobStatus jobStatus = statusList.get(0);
			jobStatus.setStep(modifiedJobStatusList.size() + 1);
			jobStatus.setStatus(isPreviousStatusError ? GeneralConstants.ERROR : jobStatus.getStatus());
			return jobStatus;
		}
		// includes previous step in the modified step
//		if (includePreviousStep) {
//			statusList.add(0, modifiedJobStatusList.get(modifiedJobStatusList.size() - 1));
//			modifiedJobStatusList.remove(modifiedJobStatusList.size() - 1);
//		}
		for (JobStatus jobStatus : statusList) {
			String status = jobStatus.getStatus();
			if (GeneralConstants.ERROR.equalsIgnoreCase(status)) {
				isError = true;
			} else if (GeneralConstants.STARTED.equalsIgnoreCase(status) ||
					GeneralConstants.IN_PROCESS.equalsIgnoreCase(status)) {
				isInProcess = true;
			} else if (GeneralConstants.NOT_STARTED.equalsIgnoreCase(status)) {
				isNotStarted = true;
			} else if (GeneralConstants.COMPLETED.equalsIgnoreCase(status)) {
				isCompleted = true;
			}
			statusMessage.append(jobStatus.getStepName().toUpperCase()).append(" - ")
					.append(status.toUpperCase()).append("\n");
//			if (TechnologyConstants.LINEAGE_IDENTIFICATION.equalsIgnoreCase(jobStatus.getStepName())) {
//				statusMessage.append(jobStatus.getMessage()).append("\n");
//			}
		}
		reducedStatus.setStepName(stepName);
		reducedStatus.setStep(modifiedJobStatusList.size() + 1);
		reducedStatus.setMessage(statusMessage.toString());
		if (isPreviousStatusError) {
			reducedStatus.setStatus(GeneralConstants.ERROR);
			return reducedStatus;
		}
		if (!isError && !isInProcess && !isCompleted) {
			reducedStatus.setStatus(GeneralConstants.NOT_STARTED);
		} else if (isInProcess || (isCompleted && isNotStarted && !isError)) {
			reducedStatus.setStatus(GeneralConstants.IN_PROCESS);
		} else if (!isNotStarted && !isError) {
			reducedStatus.setStatus(GeneralConstants.COMPLETED);
		} else if (isError) {
			reducedStatus.setStatus(GeneralConstants.ERROR);
		}
		return reducedStatus;
	}

	private String prepareStatusSummary(List<JobStatus> modifiedJobStatusList, String jobStatusDetailsForLineageIdentification,
										List<String> stepListForProcessedFiles, String jobStatusDetailsString) {
		StringBuilder statusSummary = new StringBuilder();
		String unicodeSpace = " ".replaceAll(" ", "\u00A0 \u00A0 \u00A0 \u00A0 \u00A0 \u00A0");
		int i=0;
		for (JobStatus jobStatus : modifiedJobStatusList) {
			statusSummary.append(++i).append(". ").append(jobStatus.getStepName()).append(" - ")
					.append(jobStatus.getStatus()).append("\n");
			if (TechnologyConstants.LINEAGE_IDENTIFICATION.equalsIgnoreCase(jobStatus.getStepName()) &&
					GeneralConstants.COMPLETED.equalsIgnoreCase(jobStatus.getStatus())) {
				String[] jobStatusDetailsArr = jobStatusDetailsForLineageIdentification.replaceAll("\\P{Print}", "").split(",");
				for(String status: jobStatusDetailsArr) {
//					status = unicodeSpace + status;
					statusSummary.append(unicodeSpace).append(status).append("\n");
				}
			}
			/**if (TechnologyConstants.COLUMN_LEVEL_LINEAGE_IDENTIFICATION.equalsIgnoreCase(jobStatus.getStepName())) {
				String[] jobStatusDetailsArr = jobStatusDetailsForColumnLineage.split(",");
				for(String status: jobStatusDetailsArr) {
					status = unicodeSpace +status;
					statusSummary.append(status+"\n");
				}
			}**/

			if (stepListForProcessedFiles.stream().anyMatch(step -> step.equalsIgnoreCase(jobStatus.getStepName()))) {
				String[] jobStatusDetailsArr = jobStatusDetailsString.split(",");
				for(String status: jobStatusDetailsArr) {
					status = unicodeSpace + status + "\n";
					statusSummary.append(status);
				}
			}
		}
		return statusSummary.toString();
	}

	@Override
	public void updateJobStatusDetailsWithScriptLineageString(Long jobId) {
		List<LineageJobStatus> statusList = lineageJobStatusRepository.getJobStatusWithLikeString(jobId);
		String scriptString = GeneralConstants.SCRIPT_LINEAGE_COMPLETED_STATUS_STRING.concat(",");
		for (LineageJobStatus jobStatus : statusList) {
			String jobDetails = jobStatus.getJobStatusDetails();
			int index = jobDetails.lastIndexOf(GeneralConstants.PROCESSED_FOR_PROJECT_STRING);
			StringBuilder jobDetailsBuilder = new StringBuilder(jobDetails);
			jobDetailsBuilder.insert(index, scriptString);
			jobStatus.setJobStatusDetails(jobDetailsBuilder.toString());
			lineageJobStatusRepository.save(jobStatus);
		}
	}
}