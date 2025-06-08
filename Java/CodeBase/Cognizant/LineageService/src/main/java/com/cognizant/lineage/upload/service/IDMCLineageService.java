package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.dao.entity.IdmcLinks;
import com.cognizant.lineage.dao.entity.IdmcSourceSql;
import com.cognizant.lineage.dao.entity.IdmcSourceTarget;
import com.cognizant.lineage.dao.entity.IdmcTransformations;
import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.dao.repository.IdmcLinksRepository;
import com.cognizant.lineage.dao.repository.IdmcSourceSqlRepository;
import com.cognizant.lineage.dao.repository.IdmcSourceTargetRepository;
import com.cognizant.lineage.dao.repository.IdmcTransformationsRepository;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.Sanitization;

@Service
public class IDMCLineageService {
	
	@Value("${IDMCFileUploadLocation}")
	private String idmcFileUploadLocation;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${commonScriptName}")
	private String commonScriptName;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Autowired
	public LineageJobRepository lineageJobRepository;
	
	@Autowired
	public LineageJobStatusRepository lineageJobStatusRepository;
	
	@Autowired
	public IdmcLinksRepository idmcLinksRepository;
	
	@Autowired
	public IdmcTransformationsRepository idmcTransformationsRepository;
	
	@Autowired
	public IdmcSourceTargetRepository idmcSourceTargetRepository;
	
	@Autowired
	public IdmcSourceSqlRepository idmcSourceSqlRepository;

	@Autowired
	ScriptLineageService scriptLineageService;

	private static final Logger LOGGER = LoggerFactory.getLogger(IDMCLineageService.class);
	
	static List<String> fileNamesList = new ArrayList<>(); 
	
	static String fileUploadLocation = ""; 
	
	static int stepNo = 1;
	
	public Long uploadFilesToServer(MultipartFile[] files, String projectName, String technology) {
		Long jobId = lineageJobRepository.getJobIdByMax();
		try {
			projectName = Sanitization.sanitizeInput(projectName);	
			technology = Sanitization.sanitizeInput(technology);	
			
			fileUploadLocation = idmcFileUploadLocation +"/"+ jobId;
			
			//saving details in lineage_job
			LineageJob lineageJob = LineageJob.builder()
					.jobId(jobId)
					.projectName(projectName)
					.jobParams(technology)
					.technology(technology)
					.uploadType(GeneralConstants.UPLOAD_TYPE)
					.uploadDir(fileUploadLocation)
					.startTime(new Date())
					.build();
			lineageJobRepository.saveAndFlush(lineageJob);

			LOGGER.info("Uploading IDMC files to server");
			CommonUtil.uploadAllScriptsToInputLocationForIdmc(files, fileUploadLocation);
			CommonUtil.searchZipFileRecursivelyAndUnzip(fileUploadLocation);
			LOGGER.info("IDMC files uploaded successfully");

			//saving details in lineage_job_status
			File uplLocation = new File(fileUploadLocation);
			fileNamesList = Arrays.stream(uplLocation.listFiles())
					.filter(file->!(file.getName().endsWith(".zip")))
					.map(file->file.getName())
					.collect(Collectors.toList());
			LineageJobStatus lineageJobStatus = LineageJobStatus.builder()
					.jobId(jobId)
					.stepNo(stepNo)
					.stepName("IDMC File Parsing")
					.noOfFileReceived(fileNamesList.size())
					.noOfFileProcessed(0)
					.status("Processing")
					.logFileLocation(logFileLocation + "LineageService.log")
					.build();
			lineageJobStatusRepository.saveAndFlush(lineageJobStatus);
		} catch(Exception ex) {
			LOGGER.error("Exception occured in uploadFilesToServer Service "+ex.getMessage());
		}
		return jobId;
	}
	
	@Async
	public void startIDMCProcess(Long jobId, String projectName, String technology) {
		try {
			projectName = Sanitization.sanitizeInput(projectName);	
			technology = Sanitization.sanitizeInput(technology);	
			
			int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(jobId, fileUploadLocation, technology, technology);
			if (exitCodeForScriptLineage != 0) {
				LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
				return;
			}
			//parsing each file one by one
			findIdmcFilesRecursivelyInFolder(jobId, fileUploadLocation, projectName, technology, new StringBuilder(), 0);
			executePythonScript(jobId, projectName, technology);
			
			//update endtime in lineage_job
			lineageJobRepository.updateEndTime(new Date(), jobId);
		} catch(Exception ex) {
			LOGGER.info("Exception occured in startIDMCProcess Service "+ex.getMessage());
		}
	}
	
	public void findIdmcFilesRecursivelyInFolder(Long jobId, String path, String projectName, String technology, StringBuilder jobStatusDetails, int noOfFilesProcessed) {
		try {
			File f = new File(path);
			File[] filesArr = f.listFiles();
			for(int i=0; i<filesArr.length; i++) {	
				File file = filesArr[i];
				if(fileNamesList.contains(file.getName())) {
					noOfFilesProcessed++;
				}
				
				if(file.isDirectory()) {
					findIdmcFilesRecursivelyInFolder(jobId, file.getAbsolutePath(), projectName, technology, jobStatusDetails, noOfFilesProcessed);
				} else {    
					if (file.getAbsolutePath().toUpperCase().contains(".DTEMPLATE") && file.getName().equalsIgnoreCase("@3.bin")) {
						jobStatusDetails = new StringBuilder();		
				        executeParsingSteps(jobId, file, projectName, technology, jobStatusDetails, fileNamesList.size(), noOfFilesProcessed, getFileNameFromPath(file.getAbsolutePath()));
					}
				}
			}	
		} catch(Exception ex) {
			LOGGER.info("Exception occured in findIdmcFilesRecursivelyInFolder Service "+ex.getMessage());
		}
	}
	
	public void executeParsingSteps(Long jobId, File f, String projectName, String technology, StringBuilder jobStatusDetails, int noOfFilesReceived, int noOfFilesProcessed, String fileName) {
		try {
			String json = FileUtils.readFileToString(f, StandardCharsets.UTF_8);

			List<Integer> sourceIdList = new ArrayList<>(); 
			parseJsonAndCaptureLinkInformation(jobId.intValue(), json, projectName, fileName);
			parseJsonAndCaptureTransformationInformation(jobId.intValue(), json, projectName, fileName);
			parseJsonAndCaptureAllSourceIds(json, sourceIdList);
			captureSourceTargetInfo(jobId.intValue(), sourceIdList, projectName, fileName);
			
			//updating lineage_job_status
			if(noOfFilesProcessed == noOfFilesReceived) {
				jobStatusDetails.append("No of Files Received: "+noOfFilesReceived +","+ "No of Files Processed: "+noOfFilesProcessed +","+ "LogFileLocation: "+logFileLocation + "LineageService.log");
				lineageJobStatusRepository.updateNoOfFilesProcessedAndJobStatusDetails(noOfFilesProcessed, jobStatusDetails.toString(), "Completed", jobId, stepNo);
			} else {
				jobStatusDetails.append("No of Files Received: "+noOfFilesReceived +","+ "No of Files Processed: "+noOfFilesProcessed);
				lineageJobStatusRepository.updateNoOfFilesProcessedAndJobStatusDetails(noOfFilesProcessed, jobStatusDetails.toString(), "Processing", jobId, stepNo);
			} 
		} catch(Exception ex) {
			LOGGER.info("Exception occured in executeParsingSteps Service "+ex.getMessage());
		}
	}

	public void parseJsonAndCaptureLinkInformation(int jobId, String json, String projectName, String fileName) {
		try {		
			JSONObject jsonObjParent = new JSONObject(json);
			JSONObject jsonObj1 = jsonObjParent.getJSONObject("content");
			String mappingName = jsonObj1.get("name").toString();
			JSONArray jsonArr1 = jsonObj1.getJSONArray("links");
			
			for(int j=0;j<jsonArr1.length();j++) {
				//System.out.println("Inside "+(j+1)+" link object................................");
				JSONObject jsonObj2 = jsonArr1.getJSONObject(j);

				int fromClass = Integer.parseInt(jsonObj2.getJSONObject("fromTransformation").get("$$class").toString());
				int fromId = Integer.parseInt(jsonObj2.getJSONObject("fromTransformation").get("##ID").toString());

				int toClass = Integer.parseInt(jsonObj2.getJSONObject("toTransformation").get("$$class").toString());
				int toId = Integer.parseInt(jsonObj2.getJSONObject("toTransformation").get("##ID").toString());

				IdmcLinks idmcLinks = IdmcLinks.builder()
						.projectName(projectName)
						.jobId(jobId)
						.fromTransformationClass(fromClass)
						.fromTransformationId(fromId)
						.toTransformationClass(toClass)
						.toTransformationId(toId)
						.mappingName(mappingName)
						.fileName(fileName)
						.build();
				idmcLinksRepository.save(idmcLinks);
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in parseJsonAndCaptureLinkInformation Service "+ex.getMessage());
		}
	}
	
	public void parseJsonAndCaptureTransformationInformation(int jobId, String json, String projectName, String fileName) {
		try {		
			JSONObject jsonObjParent = new JSONObject(json);
			JSONObject jsonObj1 = jsonObjParent.getJSONObject("content");
			String mappingName = jsonObj1.get("name").toString();
			JSONArray jsonArr1 = jsonObj1.getJSONArray("transformations");

			for(int j=0;j<jsonArr1.length();j++) {
				//System.out.println("Inside "+(j+1)+" transformation object................................");
				JSONObject jsonObj2 = jsonArr1.getJSONObject(j);
				
				int classVal = Integer.parseInt(jsonObj2.get("$$class").toString());
				int idVal = Integer.parseInt(jsonObj2.get("$$ID").toString());
				String transformationName = jsonObj2.get("name").toString();
				String tableName = "", customQuery = "", preSql = "", postSql = "", sqlOverride = "";
				
				//preSql, postSql, sqlOverride
				if(jsonObj2.has("advancedProperties")) {
					JSONArray jsonArr2 = jsonObj2.getJSONArray("advancedProperties");
					for(int k=0;k<jsonArr2.length();k++) {
						//System.out.println("Inside "+(k+1)+" advancedProperties object................................");
						JSONObject jsonObj3 = jsonArr2.getJSONObject(k);

						String name = jsonObj3.get("name").toString();
						String value = jsonObj3.get("value").toString();
						if(name.equalsIgnoreCase("Pre SQL")) {
							preSql = value;
						} else if(name.equalsIgnoreCase("Post SQL")) {
							postSql = value;
						} else if(name.equalsIgnoreCase("Sql Override")) {
							sqlOverride = value;
						}
					}
				}
				
				//tableName
				if(jsonObj2.has("dataAdapter")) {
				    JSONObject jsonObj3 = jsonObj2.getJSONObject("dataAdapter");
				    
				    if(jsonObj3.has("objectType") && jsonObj3.has("object")) {
				    	String objectType = jsonObj3.get("objectType").toString();
				    	if(objectType.equalsIgnoreCase("SINGLE")) {
					    	tableName = jsonObj3.getJSONObject("object").get("name").toString();
					    } else if(objectType.equalsIgnoreCase("QUERY")) {
					    	customQuery = jsonObj3.getJSONObject("object").get("customQuery").toString();
					    }
				    }
				}
				
				IdmcTransformations idmcTransformations = IdmcTransformations.builder()
						.projectName(projectName)
						.jobId(jobId)
						.classVal(classVal)
						.idVal(idVal)
						.tableName(tableName)
						.transformationName(transformationName)
						.customQuery(customQuery)
						.preSql(preSql)
						.postSql(postSql)
						.sqlOverride(sqlOverride)
						.mappingName(mappingName)
						.fileName(fileName)
						.build();
				idmcTransformationsRepository.save(idmcTransformations);
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in parseJsonAndCaptureTransformationInformation Service "+ex.getMessage());
		}
	}
	
	public void parseJsonAndCaptureAllSourceIds(String json, List<Integer> sourceIdList) {
		try {
			JSONObject jsonObjParent = new JSONObject(json);
			JSONObject jsonObj1 = jsonObjParent.getJSONObject("metadata");
			JSONObject jsonObj2 = jsonObj1.getJSONObject("$$classInfo");
			
			Iterator<?> keys = jsonObj2.keys();
			while(keys.hasNext() ) {
			   String id = keys.next().toString();
			   String value = jsonObj2.get(id).toString();
			   if(value.endsWith("TmplSource") || value.endsWith("TmplLookup") || value.endsWith("TmplStoredProcedure")) {
				   sourceIdList.add(Integer.parseInt(id));
			   } 
			}
			//System.out.println("sourceIdList..."+sourceIdList);
		} catch(Exception ex) {
			LOGGER.info("Exception occured in parseJsonAndCaptureAllSourceIds Service "+ex.getMessage());
		}
	}
	
	public void captureSourceTargetInfo(int jobId, List<Integer> sourceIdList, String projectName, String fileName) {
		try {
			for(int classId: sourceIdList) {
				//System.out.println("classId.........."+classId);
				List<IdmcLinks> idmcLinksList = idmcLinksRepository.getLinksInfoBasedOnClassId(classId, jobId, fileName);
				//System.out.println("Targets for classId: "+idmcLinksList.toString());
				
				for(IdmcLinks idmcLinks: idmcLinksList) {
					int parentSourceClass = idmcLinks.getFromTransformationClass();
					int parentSourceId = idmcLinks.getFromTransformationId();
					//System.out.println("ParentSource...."+parentSourceClass+":"+parentSourceId);
					IdmcTransformations idmcTransformations = idmcTransformationsRepository.getTransformationsInfoBasedOnClassIdAndId(parentSourceClass, parentSourceId, jobId, fileName);
					
					Stack<String> s = new Stack<>();
					s.push(idmcLinks.getToTransformationClass()+":"+idmcLinks.getToTransformationId());
					//System.out.println("Updated Stack: "+s.toString());
					List<String> visitedList = new ArrayList<>();
					visitedList.add(parentSourceClass+":"+parentSourceId);
					visitedList.add(idmcLinks.getToTransformationClass()+":"+idmcLinks.getToTransformationId());
					
					while(!(s.isEmpty())) {
						String top = s.pop();
						int classsId = Integer.parseInt(top.split(":")[0]);
						int id = Integer.parseInt(top.split(":")[1]);
						//System.out.println("Pop element from Stack: "+top);

						List<IdmcLinks> idmcLinksList1 = idmcLinksRepository.getLinksInfoBasedOnClassIdAndId(classsId, id, jobId, fileName);
						//System.out.println("Target for Top element: "+idmcLinksList1.toString());
						if(idmcLinksList1.isEmpty()) {
							//System.out.println("No further targets, so final Target "+top);
							
							IdmcTransformations idmcTransformations1 = idmcTransformationsRepository.getTransformationsInfoBasedOnClassIdAndId(classsId, id, jobId, fileName);
							
							IdmcSourceTarget idmcSourceTarget = IdmcSourceTarget.builder()
									.projectName(projectName)
									.jobId(jobId)
									.source(idmcTransformations.getTableName())
									.target(idmcTransformations1.getTableName())
									.sourceTransformationName(idmcTransformations.getTransformationName())
									.targetTransformationName(idmcTransformations1.getTransformationName())
									.mappingName(idmcTransformations1.getMappingName())
									.fileName(fileName)
									.build();  
							idmcSourceTargetRepository.saveAndFlush(idmcSourceTarget);
							updateQueryDetailsInIdmcSourceSql(idmcTransformations, idmcTransformations1);
							
						} else {
							for(IdmcLinks idmcLinks1: idmcLinksList1) {
								if(!(visitedList.contains(idmcLinks1.getToTransformationClass()+":"+idmcLinks1.getToTransformationId()))) {
									s.push(idmcLinks1.getToTransformationClass()+":"+idmcLinks1.getToTransformationId());
									visitedList.add(idmcLinks1.getToTransformationClass()+":"+idmcLinks1.getToTransformationId());
								} 
							}
						}
						//System.out.println("Updated Stack: "+s.toString());
					}
				}
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in captureSourceTargetInfo Service "+ex.getMessage());
		}
	}
	
	public void updateQueryDetailsInIdmcSourceSql(IdmcTransformations idmcTransformationsSource, IdmcTransformations idmcTransformationsTarget) {
		try {
			if(!idmcTransformationsSource.getCustomQuery().isEmpty()) {
				IdmcSourceSql idmcSourceSql = IdmcSourceSql.builder()
						.jobId(idmcTransformationsSource.getJobId())
						.fileName(idmcTransformationsSource.getFileName())
						.mappingName(idmcTransformationsSource.getMappingName())
						.queryType("custom_query")
						.sql(idmcTransformationsSource.getCustomQuery())
						.sourceType(idmcTransformationsSource.getTransformationName())
						.target(idmcTransformationsTarget.getTableName())
						.build();
				idmcSourceSqlRepository.saveAndFlush(idmcSourceSql);			
			}
			if(!idmcTransformationsSource.getPreSql().isEmpty()) {
				IdmcSourceSql idmcSourceSql = IdmcSourceSql.builder()
						.jobId(idmcTransformationsSource.getJobId())
						.fileName(idmcTransformationsSource.getFileName())
						.mappingName(idmcTransformationsSource.getMappingName())
						.queryType("pre_sql")
						.sql(idmcTransformationsSource.getPreSql())
						.sourceType(idmcTransformationsSource.getTransformationName())
						.target(idmcTransformationsTarget.getTableName())
						.build();
				idmcSourceSqlRepository.saveAndFlush(idmcSourceSql);
			}
			if(!idmcTransformationsSource.getPostSql().isEmpty()) {
				IdmcSourceSql idmcSourceSql = IdmcSourceSql.builder()
						.jobId(idmcTransformationsSource.getJobId())
						.fileName(idmcTransformationsSource.getFileName())
						.mappingName(idmcTransformationsSource.getMappingName())
						.queryType("post_sql")
						.sql(idmcTransformationsSource.getPostSql())
						.sourceType(idmcTransformationsSource.getTransformationName())
						.target(idmcTransformationsTarget.getTableName())
						.build();
				idmcSourceSqlRepository.saveAndFlush(idmcSourceSql);
			}
			if(!idmcTransformationsSource.getSqlOverride().isEmpty()) {
				IdmcSourceSql idmcSourceSql = IdmcSourceSql.builder()
						.jobId(idmcTransformationsSource.getJobId())
						.fileName(idmcTransformationsSource.getFileName())
						.mappingName(idmcTransformationsSource.getMappingName())
						.queryType("sql_override")
						.sql(idmcTransformationsSource.getSqlOverride())
						.sourceType(idmcTransformationsSource.getTransformationName())
						.target(idmcTransformationsTarget.getTableName())
						.build();
				idmcSourceSqlRepository.saveAndFlush(idmcSourceSql);
			} 
		} catch(Exception ex) {
			LOGGER.info("Exception occured in updateQueryDetailsInIdmcSourceSql Service "+ex.getMessage());
		}
	}
	
	public void executePythonScript(Long jobId, String projectName, String technology) {
		try {		
			projectName = Sanitization.sanitizeInput(projectName);
			technology = Sanitization.sanitizeInput(technology);
			
			ProcessBuilder processBuilder = new ProcessBuilder("python3.9", commonScriptLocation + "/" + commonScriptName, csrfToken, String.valueOf(jobId),  projectName, technology.toLowerCase());
			LOGGER.info("python3.9" +" "+ commonScriptLocation +"/"+ commonScriptName +" "+ csrfToken +" "+ jobId +" "+ projectName +" "+ technology.toLowerCase());
			Process process = processBuilder.start();

			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("Python Script executed successfully");
				scriptLineageService.invokeScriptLineageIdentification(jobId, projectName);
			} else {
				LOGGER.info("Exception occured while executing Python Script: ExitCode "+exitCode);
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in executePythonScript Service "+ex.getMessage());
		}
	}
	
	public String getFileNameFromPath(String path) {
		StringBuilder fileName = new StringBuilder();
		try {
			String[] pathArr = path.replace("\\", "/").split("/");
			
			boolean isDTemplate = false;
			for(String word: pathArr) {
				if(word.toUpperCase().endsWith(".DTEMPLATE")) {
					isDTemplate = true;
				}
				if(isDTemplate) {
					fileName.append(word.split("[.]")[0]+"/");
				}
			}
			fileName = fileName.deleteCharAt(fileName.length()-1);
		} catch(Exception ex) {
			LOGGER.info("Exception occured in getFileNameFromPath Service "+ex.getMessage());
		}
		return fileName.toString();
	}
}

