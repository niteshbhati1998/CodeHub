package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.dao.entity.SSISParsing;
import com.cognizant.lineage.dao.entity.SSISParsingFinal;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.dao.repository.SSISParsingFinalRepository;
import com.cognizant.lineage.dao.repository.SSISParsingRepository;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.model.ComponentInformation;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.Sanitization;

@Service
public class SSISService {
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${commonScriptName}")
	private String commonScriptName;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
 
	@Value("${bteqScriptName}")
    private String cleansingScriptName;
	
    @Value("${bteqScriptLocation}")
    private String cleansingScriptLocation;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${SSISRawScriptLocation}")
	private String ssisRawScriptLocation;
	
	@Value("${SSISParsedScriptLocation}")
	private String ssisParsedScriptLocation;
	
	@Value("${SSISCleansedScriptLocation}")
	private String ssisCleansedScriptLocation;

	@Autowired
	public LineageJobRepository lineageJobRepository;
	
	@Autowired
	public LineageJobStatusRepository lineageJobStatusRepository;
	
	@Autowired
	public SSISParsingRepository ssisParsingRepository;
	
	@Autowired
	public SSISParsingFinalRepository ssisParsingFinalRepository;

	@Autowired
	ScriptLineageService scriptLineageService;

	@Autowired
	ScriptComplexity scriptComplexity;

	private static final Logger LOGGER = LoggerFactory.getLogger(SSISService.class);
	
	public Long uploadSSISScriptsToServer(MultipartFile[] files, String projectName, String parentTechnology, String technology) {
		Long jobId = lineageJobRepository.getJobIdByMax();
		try {
			projectName = Sanitization.sanitizeInput(projectName);	
			parentTechnology = Sanitization.sanitizeInput(parentTechnology);
			technology = Sanitization.sanitizeInput(technology);	
			
			String fileUploadLocation = ssisRawScriptLocation +"/"+ jobId;
			
			//saving details in lineage_job
			LineageJob lineageJob = LineageJob.builder()
					.jobId(jobId)
					.projectName(projectName)
					.jobParams(parentTechnology)
					.technology(technology)
					.uploadType(GeneralConstants.UPLOAD_TYPE)
					.uploadDir(fileUploadLocation)
					.startTime(new Date())
					.build();
			lineageJobRepository.saveAndFlush(lineageJob);

			LOGGER.info("Uploading SSIS scripts to server");
			CommonUtil.uploadAllScriptsToInputLocation(files, fileUploadLocation);
			LOGGER.info("SSIS scripts uploaded successfully");

			//saving details in lineage_job_status
			File file = new File(fileUploadLocation);
			File[] filesArr = file.listFiles();
			
			LineageJobStatus lineageJobStatus = LineageJobStatus.builder()
					.jobId(jobId)
					.stepNo(1)
					.stepName("SSIS Script Parsing")
					.noOfFileReceived(filesArr.length)
					.noOfFileProcessed(0)
					.status("Processing")
					.logFileLocation(logFileLocation + "LineageService.log")
					.build();
			lineageJobStatusRepository.saveAndFlush(lineageJobStatus);
		} catch(Exception ex) {
			LOGGER.error("Exception occured in uploadTivoliFilesToServer Service "+ex.getMessage());
		}
		return jobId;
	}
	
	@Async
	public void parseSSISScripts(Long jobId, String projectName, String technology) throws IOException {
		try {
			File file = new File(ssisRawScriptLocation +"/"+ jobId);
			File[] fileArr = file.listFiles();
			for(int z=0;z<fileArr.length;z++) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				dbFactory.setValidating(false);
				dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				DocumentBuilder db = dbFactory.newDocumentBuilder();
				Document doc = db.parse(fileArr[z]);
				doc.getDocumentElement().normalize();
			
				Map<String, ComponentInformation> componentInformationMap = new LinkedHashMap<>();
				Map<String, String> pathInformationMap = new LinkedHashMap<>();
				
				Node parentExecutableNode = doc.getElementsByTagName("DTS:Executable").item(0);
				Element parentExecutableElement = (Element) parentExecutableNode;
				String workflowName = parentExecutableElement.getAttribute("DTS:ObjectName");
				
				NodeList executableList = doc.getElementsByTagName("DTS:Executable");
				for (int i = 0; i < executableList.getLength(); i++) {
					Node executableNode = executableList.item(i);
	                Element executableElement = (Element) executableNode;
					
	                String description = executableElement.getAttribute("DTS:Description");
	                if(description.equalsIgnoreCase("Data Flow Task")) {
	                	String mappingName = executableElement.getAttribute("DTS:ObjectName");
	                	
	                	//fetching component information
	                	NodeList componentsList = executableElement.getElementsByTagName("components");
	                	for (int j = 0; j < componentsList.getLength(); j++) {
	                		Node componentsNode = componentsList.item(j);
	                        Element componentsElement = (Element) componentsNode;
	                        
	                        NodeList componentList = componentsElement.getElementsByTagName("component");
	                        for (int k = 0; k < componentList.getLength(); k++) {
	                        	Node componentNode = componentList.item(k);
	                        	Element componentElement = (Element) componentNode;

	                        	String refId = componentElement.getAttribute("refId");
	                        	String componentName = componentElement.getAttribute("name");
	                        	String transformationType = componentElement.getAttribute("componentClassID");
	                        	String componentDescription = componentElement.getAttribute("description");
	                        	
	                        	String sqlQuery = "";
	                        	if(componentDescription.contains("Source") || componentDescription.contains("Lookup") || componentDescription.contains("Destination")) {
	                        		
	                        		NodeList propertyList = componentElement.getElementsByTagName("property");
	                        		for (int l = 0; l < propertyList.getLength(); l++) {
	                        			Node propertyNode = propertyList.item(l);
	                        			Element propertyElement = (Element) propertyNode;

	                        			String name = propertyElement.getAttribute("name");
	                        			if((componentDescription.contains("Source") || componentDescription.contains("Lookup")) && name.equalsIgnoreCase("SqlCommand")) {
	                        				 sqlQuery = propertyElement.getTextContent();
	                        			} else if(componentDescription.contains("Destination") && name.equalsIgnoreCase("OpenRowset")) {
	                        				if(!propertyElement.getTextContent().trim().equals("")) {
	                        					componentName = propertyElement.getTextContent().replaceAll("\\[|\\]", "");
	                        				}
	                        			}
	                        		} 
	                        	} 
	                        	ComponentInformation componentInformation = ComponentInformation.builder()
                						.mappingName(mappingName)
                						.componentName(componentName)
                						.transformationType(transformationType)
                						.sqlQuery(sqlQuery)
                						.build();
                				componentInformationMap.put(refId, componentInformation);
	                        }
	                	}	
	                	
	                	//fetching path information
	                	NodeList pathsList = executableElement.getElementsByTagName("paths");
	                	for (int j = 0; j < pathsList.getLength(); j++) {
	                		Node pathsNode = pathsList.item(j);
	                        Element pathsElement = (Element) pathsNode;
	                        
	                        NodeList pathList = pathsElement.getElementsByTagName("path");
	                        for (int k = 0; k < pathList.getLength(); k++) {
	                        	Node pathNode = pathList.item(k);
		                        Element pathElement = (Element) pathNode;
		                        
	                        	String startId = pathElement.getAttribute("startId").split("[.]")[0];
	                        	String endId = pathElement.getAttribute("endId").split("[.]")[0];
	                        	if(pathInformationMap.containsKey(startId)) {
	                        		String prevEndId = pathInformationMap.get(startId);
	                        		pathInformationMap.put(startId, prevEndId +":"+ endId);
	                        	} else {
	                        		pathInformationMap.put(startId, endId);
	                        	}
	                        }
	                	}
	                }
				}
				
				//inserting source/target/sql information
				pathInformationMap.forEach((startId,endIds)-> {
					ComponentInformation source = componentInformationMap.get(startId);
					String endIdArr[] = endIds.split(":");

					for(String endId: endIdArr) {
						ComponentInformation target = componentInformationMap.get(endId);

						SSISParsing ssisParsing = SSISParsing.builder()
								.jobId(jobId)
								.projectName(projectName)
								.workflowName(workflowName)
								.mappingName(source.getMappingName())
								.source(source.getComponentName())
								.sourceTransformationType(source.getTransformationType())
								.target(target.getComponentName())
								.targetTransformationType(target.getTransformationType())
								.sqlQuery(source.getSqlQuery())
								.build();
						ssisParsingRepository.save(ssisParsing);
					}
				});	
				
				//updating lineage_job_status
				StringBuilder jobStatusDetails = new StringBuilder();
				if((z+1) == fileArr.length) {
					jobStatusDetails.append("No of Files Received: "+fileArr.length +","+ "No of Files Processed: "+(z+1) +","+ "LogFileLocation: "+logFileLocation + "LineageService.log");
					lineageJobStatusRepository.updateNoOfFilesProcessedAndJobStatusDetails((z+1), jobStatusDetails.toString(), "Completed", jobId, 1);
				} else {
					jobStatusDetails.append("No of Files Received: "+fileArr.length +","+ "No of Files Processed: "+(z+1));
					lineageJobStatusRepository.updateNoOfFilesProcessedAndJobStatusDetails((z+1), jobStatusDetails.toString(), "Processing", jobId, 1);
				} 
			}
			
			//capturing source target after parsing
			LOGGER.info("Start capturing source target from different transformations");
			captureSourceTarget(jobId, projectName);
			LOGGER.info("source target captured successfully");
			
			//creating separate files and storing in server
			LOGGER.info("Start uploading parsed files over server location: "+ssisParsedScriptLocation+"/"+jobId+"/");
			List<String> fileNamesList = ssisParsingFinalRepository.getFilenamesList(jobId, projectName);
			for(String fileName: fileNamesList) {
				List<String> sqlQueryList = ssisParsingFinalRepository.getSqlQueryBasedOnFilename(jobId, projectName, fileName);

				String path = ssisParsedScriptLocation + File.separator + jobId + File.separator + fileName+".txt";
				File f = new File(path);

				// Ensure the parent directory exist
				File parentDir = f.getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdirs();
				}
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
					for (String line : sqlQueryList) {
						writer.write(line + ";");
						writer.newLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
			LOGGER.info("Parsed files uploaded successfully");

			int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(jobId, ssisRawScriptLocation.concat("/").concat(String.valueOf(jobId)),TechnologyConstants.SSIS, TechnologyConstants.SSIS);
			if (exitCodeForScriptLineage != 0) {
				LOGGER.info("Script Lineage Cleansing exited with exit code: " + exitCodeForScriptLineage);
				return;
			}
			
			//calling python cleansing and graph data load script
			executeCleansingScript(String.valueOf(jobId),projectName,technology);
			
			//update endtime in lineage_job
			lineageJobRepository.updateEndTime(new Date(), jobId);
		} catch(Exception ex) {
			LOGGER.error("Exception occured in parseSSISScripts Service "+ex.getMessage());
		} 
	}
	
	public void captureSourceTarget(Long jobId, String projectName) {
		try {			
			List<String> workflowNameList = ssisParsingRepository.getWorkflowNameList(jobId, projectName);
		    for(String workflowName: workflowNameList) {
		    	System.out.println("workflowName: "+workflowName);
		    	List<String> mappingNameList = ssisParsingRepository.getMappingNameList(jobId, projectName, workflowName);
		    	for(String mappingName: mappingNameList) {
		    		System.out.println("mappingName: "+mappingName);
		    		List<String> sourceList = ssisParsingRepository.getSourceList(jobId, projectName, workflowName, mappingName);
		    		iterateRecursively(jobId, projectName, workflowName, mappingName, sourceList);
		    	}
		    }
		} catch(Exception ex) {
			LOGGER.info("Exception occured in captureSourceTarget Service "+ex.getMessage());
		}
	}
	
	public void iterateRecursively(Long jobId, String projectName, String workflowName, String mappingName, List<String> sourceList) {
		try {			
			for(String source: sourceList) {
				System.out.println("Source: "+source);
				List<SSISParsing> ssisParsingList1 = ssisParsingRepository.findDistinctByJobIdAndProjectNameAndWorkflowNameAndMappingNameAndSource(jobId, projectName, workflowName, mappingName, source);
			    for(SSISParsing ssisParsing1: ssisParsingList1) {
			    	List<String> visitedList = new ArrayList<>();
			    	
			    	Stack<String> stack = new Stack<>();
			    	stack.push(ssisParsing1.getTarget()+":"+ssisParsing1.getTargetTransformationType());
			    	visitedList.add(ssisParsing1.getTarget());
			    	
			    	while(!(stack.isEmpty())) {
			    		String element = stack.pop();
			    		String newSource = element.split(":")[0];
			    		
			    		List<SSISParsing> ssisParsingList2 = ssisParsingRepository.findDistinctByJobIdAndProjectNameAndWorkflowNameAndMappingNameAndSource(jobId, projectName, workflowName, mappingName, newSource);
			    	    if(ssisParsingList2.isEmpty()) {
			    	    	System.out.println("No further targets, so final Target is: "+newSource);
			    	    	
			    	    	SSISParsingFinal ssisParsingFinal = SSISParsingFinal.builder()
			    	    			.jobId(jobId)
			    	    			.projectName(projectName)
			    	    			.fileName(mappingName+"__"+workflowName)
			    	    			.source(source)
			    	    			.sourceTransformationType(ssisParsing1.getSourceTransformationType())
			    	    			.target(newSource)
			    	    			.targetTransformationType(element.split(":")[1])
			    	    			//sql query may not be present if its a real source, in that case prepare sql query
			    	    			.sqlQuery(getUpdatedSqlQuery(ssisParsing1.getSqlQuery().replace(";", ""), source, newSource))
			    	    			.build();
			    	    	ssisParsingFinalRepository.save(ssisParsingFinal);		
			    	    } else {
			    	    	for(SSISParsing ssisParsing2: ssisParsingList2) {
			    	    		if(!(visitedList.contains(ssisParsing2.getTarget()))) {
			    	    		    stack.push(ssisParsing2.getTarget()+":"+ssisParsing2.getTargetTransformationType());
			    	    		    visitedList.add(ssisParsing2.getTarget());
			    	    		}
			    	    	}
			    	    }
			    	}
			    }
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in iterateRecursively Service "+ex.getMessage());
		}
	}
	
	public String getUpdatedSqlQuery(String sqlQuery, String source, String target) {
		try {
			if(sqlQuery.trim().equals("")) {
				sqlQuery = "CREATE TABLE "+target+ " SELECT * FROM "+source;
			} else {
				sqlQuery = "CREATE TABLE "+target+" "+sqlQuery;
			}	
		} catch(Exception ex) {
			LOGGER.info("Exception occured in getUpdatedSqlQuery Service "+ex.getMessage());
		}
		return sqlQuery;
	}
	
	private void executeCleansingScript(String jobId, String projectName, String technology) {
		try {
            LOGGER.info("Start executing cleansing script for SSIS");   		
			ProcessBuilder processBuilder = new ProcessBuilder("bash", cleansingScriptLocation + "/" + cleansingScriptName, ssisParsedScriptLocation + "/" + jobId + "/", ssisCleansedScriptLocation + "/" + jobId + "/", logFileLocation + "/", csrfToken, technology, jobId);
			LOGGER.info("bash" +" "+ cleansingScriptLocation + "/" + cleansingScriptName +" "+ ssisParsedScriptLocation + "/" + jobId + "/" +" "+ ssisCleansedScriptLocation + "/" + jobId + "/" +" "+ logFileLocation + "/" +" "+ csrfToken +" "+ technology +" "+ jobId);
			Process process = processBuilder.start();
		
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("cleansing python script executed successfully");
				executeGraphDataLoadScript(jobId, projectName, technology);
			} else {
				LOGGER.info("cleansing python script failed to execute, with exitCode " + exitCode);
			}
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in executeCleansingScript Service " + ex.getMessage());
		}
	}
	
	public void executeGraphDataLoadScript(String jobId, String projectName, String technology) {
		try {		
            ProcessBuilder processBuilder = new ProcessBuilder("python3.9", commonScriptLocation + "/" + commonScriptName, csrfToken, jobId,  projectName, technology.toLowerCase());
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
				LOGGER.info("Graph data load python script executed successfully");
				scriptLineageService.invokeScriptLineageIdentification(Long.valueOf(jobId), projectName);
				scriptComplexity.calculateScriptComplexity(projectName, Integer.parseInt(jobId), technology,
						technology);
			}
		} catch (Exception ex) {
			LOGGER.info("Exception occured in executeGraphDataLoadScript Service "+ex.getMessage());
		}
	}
}

