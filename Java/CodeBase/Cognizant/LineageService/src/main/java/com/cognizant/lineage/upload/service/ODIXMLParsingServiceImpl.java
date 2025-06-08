package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.exception.LineageRuntimeException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.controller.ODIXMLParsingController;
import com.cognizant.lineage.upload.dao.ODIXMLParsingDao;
import com.cognizant.lineage.upload.model.FieldBean;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.Sanitization;

@Service
public class ODIXMLParsingServiceImpl implements ODIXMLParsingService {

	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${ODIPythonScriptName}")
	private String ODIPythonScriptName;
	
	@Value("${ODIPythonScriptLocation}")
	private String ODIPythonScriptLocation;
	
	@Value("${bteqPythonScriptName}")
	private String bteqPythonScriptName;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Value("${ODIXMLFileLocation}")
	private String ODIXMLFileLocation;
	
	@Value("${logFileLocation}")
	private String logFileName;
	
	@Autowired
	private ODIXMLParsingDao odiXMLParsingDao;
	
	@Autowired
	LineageJobStatusRepository jobStatusRepo;
	
	@Autowired
	LineageJobRepository jobRepo;
	
	@Autowired
	ScriptComplexity scriptComplexity;

	@Autowired
	ScriptLineageService scriptLineageService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ODIXMLParsingServiceImpl.class);

	private String getNextSeqIdBteq() {
		try {
			Long nextId = 0L;
			nextId = odiXMLParsingDao.getNextSequenceIdBteq();
			return nextId.toString();
		} catch (Exception e) {
			return "";
		}
	}
	
	@Async
	@Override
	public void parseODIXML(String tech, File filesArr[], int jobId, String projectName, LineageJobStatus jobStatus, LineageJob lineageJob) {

		int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing((long) jobId,
				ODIXMLFileLocation + File.separator + jobId,
				TechnologyConstants.ORACLE, tech);
		if (exitCodeForScriptLineage != 0) {
			LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
			return;
		}

		for (File f : filesArr) {
			LOGGER.info("processing for filename....." + f.getName());
			try {
				parseXML(f, jobId, projectName);
				jobStatus.setNoOfFileProcessed(jobStatus.getNoOfFileProcessed()+1);
		    	jobStatus = jobStatusRepo.save(jobStatus);
			} catch (Exception ex) {
				LOGGER.error("Exception occurred in parseODIXML Service ", ex);
			}
		}
		
		lineageJob.setEndTime(new Date());
		lineageJob = jobRepo.save(lineageJob);

		jobStatus.setStatus(GeneralConstants.COMPLETED_CAPS);
		jobStatusRepo.save(jobStatus);

		//call cleansing script
		callPythonScript(projectName, tech, String.valueOf(jobId));
		
		LOGGER.info("parsing completed for all files.....");
		LOGGER.info("all queries fetched successfully from xml files");
		
		scriptComplexity.calculateScriptComplexity(projectName, jobId, tech, lineageJob.getJobParams());
	}
	
	private void parseXML(File f, int runId, String projectName) throws XMLStreamException {
		List<Object[]> params = new ArrayList<>();
		List<FieldBean> queryList = new ArrayList<>();
		try {
		  JAXBContext jaxbContext = JAXBContext.newInstance(FieldBean.class);          
		  XMLInputFactory xif = XMLInputFactory.newFactory();
		  xif.setProperty(GeneralConstants.XML_PROPERTY_EXTERNAL_ENTITY, false);
		  StreamSource source = new StreamSource(f.getAbsolutePath());
		  XMLStreamReader xsr = xif.createXMLStreamReader(source); 
		  Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		  
		  //parsing FieldBean object and capturing query
		  int startLineNumber = 0;
		  while (xsr.hasNext()) {
              int eventType = xsr.next();
              if (eventType == XMLStreamReader.START_ELEMENT && 
            		  GeneralConstants.FIELD.equals(xsr.getLocalName())) {
                  FieldBean fb = unmarshaller.unmarshal(xsr, FieldBean.class).getValue();         
                  String name = fb.getName();
				  String textContent = fb.getTextContent();
				  int endLineNumber = xsr.getLocation().getLineNumber()-1;
				  if(GeneralConstants.DEF_TXT.equalsIgnoreCase(name) && !(textContent.equalsIgnoreCase("null"))) {
					  fb.setStartLineNumber(startLineNumber);
					  fb.setEndLineNumber(endLineNumber);
					  queryList.add(fb);	
				  }
              }
              startLineNumber =  xsr.getLocation().getLineNumber();
          }
		  
		  //inserting parsed queries into table
		  for(FieldBean fb: queryList) {
			  
			  //getting updated formatted query
			  StringBuilder queryType = new StringBuilder();
			  long ms1 = System.currentTimeMillis(); 
			  Timestamp startTime = new Timestamp(ms1);
			  String query = fb.getTextContent().toUpperCase().trim();
			  String updatedQuery = formatSqlQuery(f.getName(), query, queryType);
			  long ms2 = System.currentTimeMillis(); 
			  Timestamp endTime = new Timestamp(ms2);
			  
			  Object[] param = {runId, query, updatedQuery, fb.getStartLineNumber(), fb.getEndLineNumber(), queryType, startTime, endTime, f.getName()};
			  System.out.println("runid is.............................................."+runId);
			  params.add(param);
			  ODIXMLParsingController.runId++;
		  }
		  odiXMLParsingDao.insertIntoOdiDetails(params);
		}
		catch (Exception ex) {
			LOGGER.error("Exception occured in parseXML Service ", ex.getMessage());
		}
	}
	
	public String formatSqlQuery(String fileName, String query, StringBuilder queryType) {		
		String updatedQuery = query;
		updatedQuery = updatedQuery.replace("<?=" , "");
		updatedQuery = updatedQuery.replace("?>" , "");
		updatedQuery = updatedQuery.replace("ODIREF.GETOBJECTNAME" , " ODIREF.GETOBJECTNAME ");
		updatedQuery = updatedQuery.replace("(" , " ( ");
		updatedQuery = updatedQuery.replace(")" , " ) ");
		updatedQuery = updatedQuery.replaceAll(" +", " ");
		
		StringBuilder sbInsideBracketData = new StringBuilder();
		StringBuilder sbStringToReplace = new StringBuilder();
		try {
			String[] wordsArr = updatedQuery.split(" ");
			queryType.append(wordsArr[0]);
			
			Boolean functionStart = false;
			Boolean bracketStart = false;
			for (int n = 0; n < wordsArr.length; n++) {
				if (functionStart) {
					if (wordsArr[n].equals("(")) {
						sbStringToReplace.append(wordsArr[n]+" ");
						bracketStart = true;
					} else {
						if (bracketStart) {	
							if(wordsArr[n].equals(")")) {
								sbStringToReplace.append(wordsArr[n]+" ");
								
								//replacing with tableName
								String[] arr = sbInsideBracketData.toString().trim().split(" ");
								if(arr[0].contains("L")) {
									String tableName = getTableNameWhenFirstArgumentIsL(fileName, arr);
									updatedQuery = updatedQuery.replace(sbStringToReplace.toString().trim(), tableName);
								}
								
								sbStringToReplace = new StringBuilder();
								sbInsideBracketData = new StringBuilder();
								bracketStart = false;
								functionStart = false;
							} else {
								sbStringToReplace.append(wordsArr[n]+" ");
								sbInsideBracketData.append(wordsArr[n]+" ");		
							}
						} else {
							functionStart = false;
							sbStringToReplace = new StringBuilder();
						}
					}
				} else {
					if (wordsArr[n].equalsIgnoreCase("ODIREF.GETOBJECTNAME")) {
						sbStringToReplace.append(wordsArr[n]+" ");
						functionStart = true;
					}
				}
			}
		
			//adding semicolon at end of query
			if(!(updatedQuery.equals("")) && !(wordsArr[wordsArr.length-1].contains(";"))) {
				updatedQuery = updatedQuery + " ;";
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in formatSqlQuery Service "+ex.getMessage());
		}
		return updatedQuery;
	}  
		
	private String getTableNameWhenFirstArgumentIsL(String fileName, String[] arr) { //"L", "ECERT_CAMPAIGN_DIM", "DIMENSION_DB", "D"
		String tableName = "";
		try {
			if(arr[1].contains("SNP")) {
				tableName = fileName.replace(".xml", "") + "." + arr[1].replace("\"", "").replace(",","");
			} else {
				if(arr[1].contains("%")) {
					tableName = fileName.replace(".xml", "") + "." + arr[1].replace("%", "").replace("\"", "").replace(",","");
				} else {
					tableName = arr[2].replace("\"", "").replace(",","") + "." + arr[1].replace("\"", "").replace(",","");
				}
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in getTableWhenFirstArgumentIsL Service "+ex.getMessage());
		}
		return tableName;
	}


	private void callPythonScript(String projectName, String tech, String nextSequenceId) {
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			tech = Sanitization.sanitizeInput(tech);
			
			LOGGER.info("Starting bash script for cleansing in ODIXml...");
			ProcessBuilder processBuilder = new ProcessBuilder("bash", ODIPythonScriptLocation + "/" + ODIPythonScriptName, logFileLocation, csrfToken, tech, nextSequenceId);
			LOGGER.info("bash "+ODIPythonScriptLocation + "/" + ODIPythonScriptName+" "+ logFileLocation +" "+ csrfToken +" "+ tech +" "+ nextSequenceId);
			Process process = processBuilder.start();
		
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			LOGGER.info("cleansing python script execution output . . . " + output);
			int exitCode;
			try {
				exitCode = process.waitFor();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new LineageRuntimeException("Waiting time exceeded. Could not process");
			}
			
			if (exitCode == 0) {
				LOGGER.info("cleansing python script executed successfully");
				System.out.println("cleansing python script executed successfully");
				
				//calling last step script
				executePythonScriptForLastScript(projectName, tech, nextSequenceId);
			} else {
				LOGGER.info("cleansing python script failed to execute...with exitCode " + exitCode);
				System.out.println("cleansing python script failed to execute...with exitCode " + exitCode);
			}
		} catch (InterruptedException ex1) {
			Thread.currentThread().interrupt();
			LOGGER.info("InterruptedException occurred in callPythonScript Service " + ex1.getMessage());
		} catch (Exception ex2) {
			LOGGER.info("Exception occurred in callPythonScript Service " + ex2.getMessage());
		}
	}
	
	private void executePythonScriptForLastScript(String projectName, String tech, String bteqExecutionId) throws IOException, InterruptedException {
		
		// process builder to execute script
		LOGGER.info("Executing pythonScriptForLastScript.....");
		
		ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + bteqPythonScriptName, csrfToken, bteqExecutionId, projectName, tech);
		LOGGER.info("python3.9 "+ commonScriptLocation + "/" + bteqPythonScriptName +" "+csrfToken +" "+bteqExecutionId +" "+projectName+" "+tech);
//		System.out.println("python3.9 "+ commonScriptLocation + "/" + bteqPythonScriptName +" "+csrfToken +" "+bteqExecutionId +" "+projectName+" "+tech);
		
		Process process = processBuilder2.start();

		// Read the script output
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		LOGGER.info("last step python script execution output . . . " + output);
		int exitCode;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new LineageRuntimeException("Waiting time exceeded. Could not process");
		}	

		if (exitCode == 0) {
			LOGGER.info("last step python script executed successfully");
			scriptLineageService.invokeScriptLineageIdentification(Long.valueOf(bteqExecutionId), projectName);
		} else {
			LOGGER.info("last step python script failed to execute...with exitCode " + exitCode);
		}
	}
	
	@Override
	public Map<String, Object> parseODIXmlAndSaveLineage(MultipartFile[] files, String tech, String projectName) throws LineageBusinessException {
		
		Map<String, Object> requiredObjects = new HashMap<>();
		try {

			tech = Sanitization.sanitizeInput(tech);
			projectName = Sanitization.sanitizeInput(projectName);

			Long jobId = jobRepo.getJobIdByMax();
			LineageJob lineageJob = new LineageJob();
			lineageJob.setJobId(jobId);
			lineageJob.setTechnology(tech);
			lineageJob.setStartTime(new Date());
			lineageJob.setJobParams(Sanitization.sanitizeInput(TechnologyConstants.ORACLE));
			lineageJob.setUploadType(Sanitization.sanitizeInput(GeneralConstants.UPLOAD_TYPE));
			lineageJob.setProjectName(projectName);

			lineageJob = jobRepo.save(lineageJob);
			Long nextSequenceId = lineageJob.getJobId();
			List<String> techList = List.of("ODI", "Package", "Procedure", "View", "Trigger", "Function");
//			if (TechnologyConstants.ODI.equalsIgnoreCase(tech)) {
			if (techList.contains(tech)) {
				
				// job status id using sequence for job status
				String jobStatusId = getNextSeqIdBteq();
				File newInputDirectory = new File(ODIXMLFileLocation+File.separator+nextSequenceId);
				newInputDirectory.mkdir();
				
				double size = 0;
			
				lineageJob.setUploadDir(newInputDirectory.getAbsolutePath());
				lineageJob = jobRepo.save(lineageJob);
				LineageJobStatus jobStatus = null;
				if (TechnologyConstants.ODI.equalsIgnoreCase(tech)) {
					jobStatus = new LineageJobStatus();
					jobStatus.setId(Long.valueOf(jobStatusId));
					jobStatus.setJobId(nextSequenceId);
					//jobStatus.setNoOfFileReceived(files.length);
					jobStatus.setLogFileLocation(logFileName + "/lineage-service.log");
					jobStatus.setNoOfFileProcessed(0);
					jobStatus.setStepNo(1);
					jobStatus.setStepName(GeneralConstants.ODI_XML_PARSING);
					jobStatus.setStatus(GeneralConstants.STATUS_IN_PROCESS);
					jobStatus = jobStatusRepo.save(jobStatus);
				}
				
				/**
				for (MultipartFile file : files) {
					if (file.getOriginalFilename().toLowerCase().endsWith(GeneralConstants.ZIP_FILE_FORMAT)) {
						CommonUtil.unzipMultipartFileForODIXml(file, newInputDirectory.getAbsolutePath());
						continue;
					} else {
						byte[] bytes = file.getBytes();
						size += file.getSize();
						Path path = Paths.get(newInputDirectory.getAbsolutePath() + File.separator + file.getOriginalFilename());
						Path canonicalPath = path.normalize();
						Files.write(canonicalPath, bytes);
					}
				}**/
				
				CommonUtil.uploadAllScriptsToInputLocation(files,newInputDirectory.getAbsolutePath());
				File file = new File(newInputDirectory.getAbsolutePath());
				File filesArr[] = file.listFiles();
				if (!Objects.isNull(jobStatus) && !Objects.isNull(filesArr) &&
						TechnologyConstants.ODI.equalsIgnoreCase(tech)) {
					jobStatus.setNoOfFileReceived(filesArr.length);
					jobStatus = jobStatusRepo.save(jobStatus);
				}
				LOGGER.info("started fetching queries from xml files: Total upload size = {}", size);
				
				requiredObjects.put(GeneralConstants.TECH, tech);
				requiredObjects.put(GeneralConstants.FILES_ARRAY, filesArr);
				requiredObjects.put(GeneralConstants.PROJECT_NAME, projectName);
				requiredObjects.put(GeneralConstants.JOB_STATUS, jobStatus);
	     		requiredObjects.put(GeneralConstants.LINEAGE_JOB, lineageJob);		
			}
			return requiredObjects;
		} catch (LineageBusinessException e) {
			throw e;
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
			throw new LineageRuntimeException(ex.getMessage());
		}
	}
}
