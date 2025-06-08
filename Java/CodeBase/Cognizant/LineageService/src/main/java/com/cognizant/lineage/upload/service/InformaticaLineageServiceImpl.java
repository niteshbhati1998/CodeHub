package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cognizant.lineage.dao.entity.InformaticaParamDetails;
import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.dao.repository.InformaticaParamDetailsRepository;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.exception.LineageRuntimeException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.QueryConstant;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.dao.InformaticaLineageDao;
import com.cognizant.lineage.upload.model.ComponentLevelDetails;
import com.cognizant.lineage.upload.model.InformaticaParamTables;
import com.cognizant.lineage.upload.model.InstanceDetails;
import com.cognizant.lineage.upload.model.Node;
import com.cognizant.lineage.upload.model.SingleComponentDetails;
import com.cognizant.lineage.upload.model.SourceTargetMapping;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.LoggerUtil;

@Service
@Transactional(propagation = Propagation.SUPPORTS,  rollbackFor = Exception.class)
public class InformaticaLineageServiceImpl implements InformaticaLineageService {

	@Value("${informaticaInputFiles}")
	private String inputFiles;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${informaticaInputFiles}")
	private String uploadFileLocation;
	
	@Value("${informaticaParameterFileLocation}")
	private String informaticaParameterFileLocation;

	@Autowired
	LineageJobRepository jobRepo;
	
	@Autowired
	LineageJobStatusRepository jobStatusRepo;

	@Autowired
	private InformaticaLineageDao informaticaLineageDao;
	
	@Autowired
	private ExecuteScript executeScript;	
	
	//private NodeList workflowTag;
	//private NodeList sessionExtensionList;
	
	@Autowired
	private InformaticaParamDetailsRepository informaticaParamDetailsRepository;

	@Autowired
	private InformaticaScriptExecution infaScriptService;

	@Autowired
	private ScriptComplexity scriptComplexity;

	@Autowired
	ScriptLineageService scriptLineageService;

	private static final Logger LOGGER = LoggerFactory.getLogger(InformaticaLineageServiceImpl.class);
	
	@Async
	@Override
	public void parseXmlFile(int batchSize, double size, String projectName, Integer sequenceId) throws Exception {
		String executionDirectory = inputFiles + File.separator + sequenceId;

		long startTime = Instant.now().toEpochMilli();
		int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(Long.valueOf(sequenceId),
				executionDirectory, TechnologyConstants.INFORMATICA, TechnologyConstants.INFORMATICA);
		if (exitCodeForScriptLineage != 0) {
			LOGGER.info("Script Lineage exited with exit code: {}", exitCodeForScriptLineage);
			return;
		}

		LOGGER.info("Parsing started for job id: "+sequenceId);
		
		Thread.sleep(2000);
		
		//job status id 
		int statusId = informaticaLineageDao.getSequenceId();
		
		LineageJobStatus jobStatus = new LineageJobStatus();
		jobStatus.setId((long) statusId);
		jobStatus.setJobId(Long.valueOf(sequenceId));
		jobStatus.setNoOfFileReceived(batchSize);
		jobStatus.setStatus("In Process");
		jobStatus.setStepNo(2);
		jobStatus.setStepName("XML Parsing");
		jobStatus.setNoOfFileProcessed(0);
		jobStatus.setLogFileLocation(logFileLocation + File.separator + sequenceId + ".log");
		jobStatus = jobStatusRepo.saveAndFlush(jobStatus);
		
		statusId = jobStatus.getId().intValue();

		String logFileName = "";
		java.util.logging.Logger CUSTOM_LOGGER = null;
		FileHandler handler = null;
		synchronized (this) {
			/** Creating Log File Name (It's Should be unique) **/
			logFileName = new String(logFileLocation + File.separator + sequenceId + ".log");
			handler = new FileHandler(logFileName, true);
			LoggerUtil loggerUtil = new LoggerUtil();
			CUSTOM_LOGGER = loggerUtil.getLogger(handler, logFileName);
		}

		File input = new File(executionDirectory);
		if (input.isDirectory()) {
			traverseDirectory(input, sequenceId, CUSTOM_LOGGER, logFileName, batchSize, jobStatus, statusId);
		}
		//replacing parameter with real names
		List<InformaticaParamTables> informaticaParamTablesList = informaticaLineageDao.getInformaticaParamTables(sequenceId);
		updateInformaticaParamTables(informaticaParamTablesList,sequenceId);

		long endTime = Instant.now().toEpochMilli();
		long diff = (endTime - startTime) / 1000;

		LOGGER.info("Input files location " + executionDirectory);
		LOGGER.info("Processing time taken in seconds = " + diff);
		//LOGGER.info("Total file size in MB = " + size / (1024 * 1024));
		LOGGER.info("Total file count <> " + batchSize);
		CUSTOM_LOGGER.info("Input files location " + executionDirectory);
		//CUSTOM_LOGGER.info("Total file size in MB = " + size / (1024 * 1024));
		CUSTOM_LOGGER.info("Total file count = " + batchSize);
		CUSTOM_LOGGER.info("Processing time taken in seconds = " + diff);
		
		jobStatus.setStatus("Completed");
		jobStatusRepo.saveAndFlush(jobStatus);
		
		try {
            if (infaScriptService.infaSqlTableStatus((int) sequenceId)) {
				try {
					LOGGER.info("Found sql as source for informatica, starting process. . . : "+sequenceId);
					infaScriptService.executeFileCreationScript("informatica", projectName, sequenceId);
				} catch (Exception e) {
					LOGGER.error("Exception in executing python script for informatica. (TECH -> informatica)");
				}
			}
			
			LOGGER.info("Executing python script for informatica. <<"+sequenceId+">>");
			executeScript.executePythonInformaticaScript(sequenceId.toString(), projectName);
			LOGGER.info("Python script executed successfully");
			CUSTOM_LOGGER.info("Python script executed successfully");
			
			LineageJob lineageJob = jobRepo.findById(Long.valueOf(sequenceId)).get();
			lineageJob.setEndTime(new Date());
			lineageJob.setUploadDir(inputFiles);
			jobRepo.save(lineageJob);

			LOGGER.info("Process completed for job id: "+sequenceId);

			scriptComplexity.calculateScriptComplexityForInformatica(lineageJob.getProjectName(), lineageJob.getJobId(), lineageJob.getTechnology());		
		} catch (Exception e) {
			LOGGER.info("Exception occurred in executing informatica python script.", e);
			e.printStackTrace();
			LineageJob lineageJob = jobRepo.findById(Long.valueOf(sequenceId)).get();
			lineageJob.setEndTime(new Date());
			lineageJob.setUploadDir(inputFiles);
			jobRepo.save(lineageJob);
			CUSTOM_LOGGER.info("Exception occurred in executing informatica python script."+ e.getCause());
		}
	}

	private void traverseDirectory(File directory, Integer sequenceId, java.util.logging.Logger CUSTOM_LOGGER,
			String logFileName, int batchSize, LineageJobStatus jobStatus, Integer statusId) {
		File[] files = directory.listFiles();
		LOGGER.info("batchsize: " + batchSize);
		CUSTOM_LOGGER.info("batchsize: " + batchSize);

		for (File file : files) {
			if (file.isDirectory()) {
				LOGGER.info("directory Name " + file.getName());
				CUSTOM_LOGGER.info("directory Name " + file.getName());
				traverseDirectory(file, sequenceId, CUSTOM_LOGGER, logFileName, batchSize, jobStatus, statusId);
			} else {
				parseXmlFile(file, sequenceId, batchSize, CUSTOM_LOGGER, logFileName, jobStatus, statusId);
			}
		}
	}

	private void parseXmlFile(File file, Integer sequenceId, Integer batchSize, java.util.logging.Logger CUSTOM_LOGGER,
			String logFileName, LineageJobStatus jobStatus, Integer statusId) {
		    String fileName = file.getName();

		LOGGER.info("parsing for file Name ..." + fileName);
		CUSTOM_LOGGER.info("parsing for file Name ..." + fileName);
		if (file.isDirectory()) {
			return;
		}
		if (fileName == null || !fileName.toUpperCase().endsWith(GeneralConstants.FileTypeConstraint)) {
			LOGGER.info("File doesn't meet the requirement constraints " + fileName);
			CUSTOM_LOGGER.info("File doesn't meet the requirement constraints " + fileName);
			return;
		}
		try {
			parseInformaticaXmlFiles(file, sequenceId, batchSize, CUSTOM_LOGGER, logFileName);
			//LineageJobStatus incStatus = jobStatusRepo.findById(Long.valueOf(statusId)).get();
			jobStatus.setNoOfFileProcessed(jobStatus.getNoOfFileProcessed()+1);
			jobStatus = jobStatusRepo.save(jobStatus);
		} catch (Exception e) {
			e.printStackTrace();
			CUSTOM_LOGGER.info(e.getMessage());
			LOGGER.error("Exception in transactional ", e);
		}
	}

//	@Transactional(rollbackFor = Exception.class)
	private void parseInformaticaXmlFiles(File file, Integer sequenceId, Integer batchSize,
			java.util.logging.Logger CUSTOM_LOGGER, String logFileName) throws Exception {

		Document document = null;
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setValidating(false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			DocumentBuilder db = dbFactory.newDocumentBuilder();
			document = db.parse(file);
			document.getDocumentElement().normalize();
			LOGGER.info("Root element: " + document.getDocumentElement().getNodeName());
			CUSTOM_LOGGER.info("Root element: " + document.getDocumentElement().getNodeName());
		} catch (SAXException e) {
			e.printStackTrace();
			LOGGER.error("Exception in transactional ", e);
			CUSTOM_LOGGER.info("Exception occurred while reading document : SAXException " + file.getName());
			return;
		}
		try {
			boolean isInfraComponentExist = false;// informaticaLineageDao.checkInfraComponentExist(file.getName());
			LOGGER.info("isInfraComponentExist  : " + file.getName() + ": " + isInfraComponentExist);
			CUSTOM_LOGGER.info("isInfraComponentExist  : " + file.getName() + ": " + isInfraComponentExist);
			
			parseInformaticaDetailsFromXML(document, file.getName(), sequenceId, batchSize, CUSTOM_LOGGER,
						logFileName);
			
		} catch (Exception e) {
			LOGGER.error("Exception in transactional ", e);
			CUSTOM_LOGGER.info("Exception in transactional " + e.getMessage());
			throw new RuntimeException("Error ", e);
		}
	}

	private void parseInformaticaDetailsFromXML(Document document, String fileName, Integer sequenceId,
			Integer batchSize, java.util.logging.Logger CUSTOM_LOGGER, String logFileName) {
		String errmsg = null;
		boolean xmlTagNotAvailable = false;
		long startTime = Instant.now().toEpochMilli();
		String paramFileNames = "";
	    Set<String> paramFileNamesSet = new HashSet<>();//.replaceAll("^\\[|\\]$", "").replaceAll(",\\s+", ",").trim()
		try {
			List<SourceTargetMapping> sourceTargetMappingList = new ArrayList<>(); // used for table level lineage
			Set<Map<SingleComponentDetails, SingleComponentDetails>> disconnectedComponents = new HashSet<>(); //for disconnected procs
			NodeList nodeList = document.getElementsByTagName(GeneralConstants.POWERMART);
			errmsg = GeneralConstants.POWERMART + GeneralConstants.MESSAGE_VALUE;
			LOGGER.info("Inside POWERMART.......");
			CUSTOM_LOGGER.info("Inside POWERMART.......");
			
			//this.workflowTag = document.getElementsByTagName(GeneralConstants.WORKFLOW_TAG);
			NodeList sessionExtensionList = document.getElementsByTagName(GeneralConstants.SESSION_EXTENSION_TAG);
			Map<String, String> mappingNameSessionNameMap = getSessionNamesMap(document);
			for (int i = 0; i < 1; i++) {
				List<Object[]> batchUpdateParams = new ArrayList<Object[]>();
				Set<ComponentLevelDetails> componentLevelDetailsList = new HashSet<>();
				
				Element rootElement = (Element) nodeList.item(i);
				NodeList rootCategories = rootElement.getElementsByTagName(GeneralConstants.REPOSITORY_TAG);

				errmsg = GeneralConstants.REPOSITORY_TAG + GeneralConstants.MESSAGE_VALUE;

				LOGGER.info("Inside REPOSITORY.......");
				CUSTOM_LOGGER.info("Inside REPOSITORY.......");
				for (int j = 0; j < 1; j++) {
					Element repositoryElement = (Element) rootCategories.item(j);
					NodeList repositoryList = repositoryElement.getElementsByTagName(GeneralConstants.FOLDER_TAG);
					errmsg = GeneralConstants.FOLDER_TAG + GeneralConstants.MESSAGE_VALUE;
					xmlTagNotAvailable = rootCategories.getLength() <= 0;

					for (int k = 0; k < repositoryList.getLength(); k++) {
						LOGGER.info("Inside  FOLDER.......");
						CUSTOM_LOGGER.info("Inside  FOLDER.......");
						Element folderElement = (Element) repositoryList.item(k);
						String folderName = folderElement.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
						NodeList sourceNodeList = folderElement.getElementsByTagName(GeneralConstants.SOURCE_TAG);
						NodeList targetNodeList = folderElement.getElementsByTagName(GeneralConstants.TARGET_TAG);
						NodeList folderNodeList = folderElement.getElementsByTagName(GeneralConstants.MAPPING_TAG);
						NodeList sessionNodeList = folderElement.getElementsByTagName(GeneralConstants.SESSION_TAG);
						NodeList workflowNodeList = folderElement.getElementsByTagName(GeneralConstants.WORKFLOW_TAG);
						
						getParameterFileName(sessionNodeList,workflowNodeList,paramFileNamesSet);
						paramFileNames = paramFileNamesSet.toString().replaceAll("^\\[|\\]$", "").replaceAll(",\\s+", ",").trim();

						errmsg = GeneralConstants.MAPPING_TAG + GeneralConstants.MESSAGE_VALUE;
						xmlTagNotAvailable = folderNodeList.getLength() <= 0;

						Set<ComponentLevelDetails> mappingSpecificComponent = null; // used for table level lineage

						for (int l = 0; l < folderNodeList.getLength(); l++) {

							mappingSpecificComponent = new HashSet<>(); // used for table level lineage
							Element mappingElement = (Element) folderNodeList.item(l);
							String mappingName = mappingElement.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
							LOGGER.info("Inside  MAPPING : " + mappingName);
							CUSTOM_LOGGER.info("Inside  MAPPING : " + mappingName);

							NodeList instanceNodeList = mappingElement.getElementsByTagName(GeneralConstants.INSTANCE_TAG);
							NodeList transformationList = mappingElement.getElementsByTagName(GeneralConstants.TRANSFORMATION);

							List<InstanceDetails> sourceComponentList = new ArrayList<>();
							List<InstanceDetails> targetComponentList = new ArrayList<>();

							errmsg = GeneralConstants.INSTANCE_TAG + GeneralConstants.MESSAGE_VALUE;
							xmlTagNotAvailable = instanceNodeList.getLength() <= 0;

							for (int s = 0; s < instanceNodeList.getLength(); s++) {
								InstanceDetails instanceDetails = new InstanceDetails();
								Element category3 = (Element) instanceNodeList.item(s);
								String tfType = category3.getAttribute(GeneralConstants.TRANSFORMATION_TYPE);
								String tfName = category3.getAttribute(GeneralConstants.TRANSFORMATION_NAME);
								String tableName = category3.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
								if (StringUtils.equalsIgnoreCase(tfType, GeneralConstants.SOURCE_TAG)
										|| StringUtils.equalsIgnoreCase(tfType, GeneralConstants.SOURCE_DEFINITION)
										|| StringUtils.equalsIgnoreCase(tfType, GeneralConstants.LOOKUP_PROCEDURE)) {
									instanceDetails.setTableName(tableName);
									instanceDetails.setTransformationName(tfName);
									sourceComponentList.add(instanceDetails);
								}
								if (StringUtils.equalsIgnoreCase(tfType, GeneralConstants.TARGET_TAG)
										|| StringUtils.equalsIgnoreCase(tfType, GeneralConstants.TARGET_DEFINITION)) {
									instanceDetails.setTableName(tableName);
									instanceDetails.setTransformationName(tfName);
									targetComponentList.add(instanceDetails);
								}
							}

							LOGGER.info("Inside CONNECTOR_TAG.......");
							CUSTOM_LOGGER.info("Inside CONNECTOR_TAG.......");
							NodeList connectorNodeList = mappingElement.getElementsByTagName(GeneralConstants.CONNECTOR_TAG);

							LOGGER.info("Inside CONNECTOR_TAG SIZE......."+connectorNodeList.getLength());
							CUSTOM_LOGGER.info("Inside CONNECTOR_SIZE......."+connectorNodeList.getLength());
							errmsg = GeneralConstants.CONNECTOR_TAG + GeneralConstants.MESSAGE_VALUE;
							xmlTagNotAvailable = connectorNodeList.getLength() <= 0;
							
							//look for disconnected lookup and stored procedures
							for (int v = 0; v < connectorNodeList.getLength(); v++) {
								Element connectorElement = (Element) connectorNodeList.item(v);
								String fromInstanceType = connectorElement.getAttribute(GeneralConstants.FROMINSTANCETYPE);
								String toInstanceType = connectorElement.getAttribute(GeneralConstants.TOINSTANCETYPE);
								String fromInstance = connectorElement.getAttribute(GeneralConstants.FROMINSTANCE);
								String toInstance = connectorElement.getAttribute(GeneralConstants.TOINSTANCE);
								ComponentLevelDetails connectorDetails = new ComponentLevelDetails();
								connectorDetails.setFileName(fileName);
								connectorDetails.setFolderName(folderName);
								connectorDetails.setMappingName(mappingName);
								connectorDetails.setFromComponent(fromInstance);
								connectorDetails.setFromComponentType(fromInstanceType);
								connectorDetails.setToComponent(toInstance);
								connectorDetails.setToComponentType(toInstanceType);
								connectorDetails.setExecutionId(sequenceId);
								connectorDetails.setSessionName(mappingNameSessionNameMap.getOrDefault(mappingName, ""));
								//String sinstanceName = fromInstance;
								//String dinstanceName = toInstance;

								if (StringUtils.equalsIgnoreCase(fromInstanceType, GeneralConstants.SOURCE_TAG)
										|| StringUtils.equalsIgnoreCase(fromInstanceType, GeneralConstants.SOURCE_DEFINITION)
										|| StringUtils.equalsIgnoreCase(fromInstanceType, GeneralConstants.LOOKUP_PROCEDURE)) {

									if (!StringUtils.equalsIgnoreCase(fromInstanceType, GeneralConstants.LOOKUP_PROCEDURE)) {
										connectorDetails.setFromComponentType(GeneralConstants.SOURCE_TAG);
										for (int c = 0; c < sourceComponentList.size(); c++) {
											if (fromInstance
													.equalsIgnoreCase(sourceComponentList.get(c).getTableName())) {
												String tfName = sourceComponentList.get(c).getTransformationName();
												connectorDetails.setSourceTableName(tfName);
												for (int a = 0; a < sourceNodeList.getLength(); a++) {
													Element sourceElementName = (Element) sourceNodeList.item(a);
													if (tfName.equalsIgnoreCase(sourceElementName
															.getAttribute(GeneralConstants.NAME_ATTRIBUTE))) {
														connectorDetails.setSourceDatabaseType(sourceElementName.getAttribute(GeneralConstants.DATABASETYPE));
														connectorDetails.setSourceDbName(sourceElementName.getAttribute(GeneralConstants.DBDNAME));
														connectorDetails.setSourceTableName(sourceElementName.getAttribute(GeneralConstants.NAME_ATTRIBUTE));
														
														//if databasetype contains file then get 
														//dir and filename from sess-ext based on instance name
														if(StringUtils.containsIgnoreCase(sourceElementName.getAttribute(GeneralConstants.DATABASETYPE), "file")) {
															String tableName = getInfoFromSessionExtension(fromInstance, tfName, "SOURCE", sessionExtensionList);
															if(tableName!=null && !tableName.trim().isEmpty()) {
																connectorDetails.setSourceTableName(tableName);
															}	
														}
													}
												}
											}
										}
									}
									else {
										connectorDetails.setFromComponentType(GeneralConstants.LOOKUP_PROCEDURE);
										for (int c = 0; c < sourceComponentList.size(); c++) {
											if (fromInstance.equalsIgnoreCase(sourceComponentList.get(c).getTableName())) {
												String tfName = sourceComponentList.get(c).getTransformationName();

												for (int d = 0; d < transformationList.getLength(); d++) {

													Element transformationNodeList = (Element) transformationList.item(d);
													String lookUpTableName = transformationNodeList.getAttribute(GeneralConstants.NAME_ATTRIBUTE);

													NodeList lookupTableAttributeList = transformationNodeList.getElementsByTagName(GeneralConstants.TABLEATTRIBUTE);
													
													//check flatfile based on taking value containing file
													//<TABLEATTRIBUTE NAME ="Source Type" VALUE ="Database"/>
													if (tfName.equalsIgnoreCase(lookUpTableName)
															&& (transformationNodeList.getAttribute(
																	GeneralConstants.TYPE)).equalsIgnoreCase(
																			GeneralConstants.LOOKUP_PROCEDURE)) {
														for (int n = 0; n < lookupTableAttributeList.getLength(); n++) {

															Element lookupElement = (Element) lookupTableAttributeList.item(n);
															String tname = lookupElement.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
															
															if ((lookupElement.getAttribute(
																	GeneralConstants.NAME_ATTRIBUTE)).equalsIgnoreCase(
																			GeneralConstants.LOOKUP_TABLE_NAME)) {
																Element tableAttributeList = (Element) lookupTableAttributeList
																		.item(1);

																if (StringUtils.isBlank(tableAttributeList
																		.getAttribute(GeneralConstants.VALUE))) {
																	connectorDetails.setSourceTableName(lookUpTableName);
																} else {connectorDetails.setSourceTableName(
																			tableAttributeList.getAttribute(
																					GeneralConstants.VALUE));
																}
															}
															//checking if database is of file type
															
															if(tname.equalsIgnoreCase(GeneralConstants.LOOKUP_SOURCE_TYPE)) {
																String value = lookupElement.getAttribute(GeneralConstants.VALUE);
																if(StringUtils.containsIgnoreCase(value, "file")) {
																	String tName = getInfoFromSessionExtension(fromInstance, tfName, "LOOKUP", sessionExtensionList);
																	if(tName!=null && !tName.trim().isEmpty()) {
																		connectorDetails.setSourceTableName(tName);
																	}
																}
															}
															//check for sql if not file 
															else if ((lookupElement.getAttribute(
																	GeneralConstants.NAME_ATTRIBUTE)).equalsIgnoreCase(
																			GeneralConstants.Lookup_Sql_Override)) {
																String lookupSql = lookupElement.getAttribute(GeneralConstants.VALUE);
																connectorDetails.setSqlQuery(lookupSql);
															}
														}
													}
												}
											}
										}
									}
								}
								
								//SETTING LOOKUP VALUES IF FOUND IN TO INSTANCE
								if (StringUtils.equalsIgnoreCase(toInstanceType, GeneralConstants.LOOKUP_PROCEDURE)){
									for (int c = 0; c < sourceComponentList.size(); c++) {
										if (toInstance.equalsIgnoreCase(sourceComponentList.get(c).getTableName())) {
											String tfName = sourceComponentList.get(c).getTransformationName();

											for (int d = 0; d < transformationList.getLength(); d++) {

												Element transformationNodeList = (Element) transformationList
														.item(d);
												String lockUptableName = transformationNodeList
														.getAttribute(GeneralConstants.NAME_ATTRIBUTE);

												NodeList lookupTableAttributeList = transformationNodeList
														.getElementsByTagName(GeneralConstants.TABLEATTRIBUTE);
												
												
												//check flatfile based on taking value containing file
												//<TABLEATTRIBUTE NAME ="Source Type" VALUE ="Database"/>
												if (tfName.equalsIgnoreCase(lockUptableName)
														&& (transformationNodeList.getAttribute(
																GeneralConstants.TYPE)).equalsIgnoreCase(
																		GeneralConstants.LOOKUP_PROCEDURE)) {
													for (int n = 0; n < lookupTableAttributeList.getLength(); n++) {

														Element lookupElement = (Element) lookupTableAttributeList.item(n);
														String tname = lookupElement.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
														
														if ((lookupElement.getAttribute(
																GeneralConstants.NAME_ATTRIBUTE)).equalsIgnoreCase(
																		GeneralConstants.LOOKUP_TABLE_NAME)) {
															Element tableAttributeList = (Element) lookupTableAttributeList
																	.item(1);

															if (StringUtils.isBlank(tableAttributeList.getAttribute(GeneralConstants.VALUE))) {
																connectorDetails.setTargetTableName(lockUptableName);
															} else {
																connectorDetails.setTargetTableName(tableAttributeList.getAttribute(GeneralConstants.VALUE));
															}
														}
														
														//checking if database is of file type
														if(tname.equalsIgnoreCase(GeneralConstants.LOOKUP_SOURCE_TYPE)) {
															String value = lookupElement.getAttribute(GeneralConstants.VALUE);
															if(StringUtils.containsIgnoreCase(value, "file")) {
																String tName = getInfoFromSessionExtension(fromInstance, tfName, "LOOKUP", sessionExtensionList);
																if(tName!=null && !tName.trim().isEmpty()) {
																	connectorDetails.setTargetTableName(tName);
																}
															}
														}
														//check for sql if not file
														else if ((lookupElement.getAttribute(
																GeneralConstants.NAME_ATTRIBUTE)).equalsIgnoreCase(
																		GeneralConstants.Lookup_Sql_Override)) {
															String lookupSql = lookupElement.getAttribute(GeneralConstants.VALUE);
															connectorDetails.setSqlQuery(lookupSql);
														}
													}
												}
											}
										}
									}
								}
								
								//source qualifier sql override
								if (StringUtils.equalsIgnoreCase(GeneralConstants.SOURCE_QUALIFIER, toInstanceType)) {
									getSqlForSourceQualifier(document, toInstance, transformationList, instanceNodeList,
											connectorDetails);
								}
								
								if (StringUtils.equalsIgnoreCase(toInstanceType, GeneralConstants.TARGET_TAG)
										|| StringUtils.equalsIgnoreCase(toInstanceType, GeneralConstants.TARGET_DEFINITION)) {
									connectorDetails.setToComponentType(GeneralConstants.TARGET_TAG);
									String tfName = "";
									for (int c = 0; c < targetComponentList.size(); c++) {
										if (toInstance.equalsIgnoreCase(targetComponentList.get(c).getTableName())) {
											tfName = targetComponentList.get(c).getTransformationName();
											connectorDetails.setTargetTableName(tfName);
											for (int b = 0; b < targetNodeList.getLength(); b++) {
												Element targetElementName = (Element) targetNodeList.item(b);
												if (tfName.equalsIgnoreCase(targetElementName.getAttribute(GeneralConstants.NAME_ATTRIBUTE))) {
													connectorDetails.setTargetDatabaseType(targetElementName.getAttribute(GeneralConstants.DATABASETYPE));
													connectorDetails.setTargetDbName(targetElementName.getAttribute(GeneralConstants.DBDNAME));
												}
											}
										}
									}
									
//									if(StringUtils.equalsIgnoreCase(toInstance, "Shortcut_to_TGEDW_COMBINED_ERRORS")) {
//										System.out.println();
//									}
									String targetTable = getTargetTableInfo(mappingName, toInstance, tfName, instanceNodeList, document, sessionExtensionList);
									targetTable = targetTable.replaceAll("\\s+", "_");
									connectorDetails.setTargetTableName(targetTable);
								}
								
								//INFORMATION PARSING FOR STORED PROCEDURE -- SIMILAR TO LOOKUP PROCEDURE
								if(StringUtils.equalsIgnoreCase(toInstanceType, GeneralConstants.STORED_PROCEDURE)) {
									String procName = getInfoForStoredProcedure(toInstance, transformationList, instanceNodeList);
									connectorDetails.setTargetTableName(procName);
								}
								
								if(StringUtils.equalsIgnoreCase(fromInstanceType, GeneralConstants.STORED_PROCEDURE)) {
									String procName = getInfoForStoredProcedure(fromInstance, transformationList, instanceNodeList);
									connectorDetails.setSourceTableName(procName);
								}

								//removing shortcut prefix
								String tableName = connectorDetails.getSourceTableName();
								if(StringUtils.startsWith(tableName, GeneralConstants.Shortcut_to_)) {
									tableName = tableName.substring(GeneralConstants.Shortcut_to_.length());
								}
								connectorDetails.setSourceTableName(tableName);
								
								tableName = connectorDetails.getTargetTableName();
								if(StringUtils.startsWith(tableName, GeneralConstants.Shortcut_to_)) {
									tableName = tableName.substring(GeneralConstants.Shortcut_to_.length());
								}
								connectorDetails.setTargetTableName(tableName);
								//shortcut removal done

								if (Stream.of(GeneralConstants.TARGET_TAG, GeneralConstants.TARGET_DEFINITION,
										GeneralConstants.SOURCE_QUALIFIER).anyMatch(toInstanceType::equalsIgnoreCase) ||
										StringUtils.equalsIgnoreCase(fromInstanceType, GeneralConstants.LOOKUP_PROCEDURE)) {
									getSqlQueriesIfExistsFromInstanceTag(connectorDetails, instanceNodeList);
								}
								getAdditionalSqlQueriesForTargetAndLookUp(document, connectorDetails,
										sourceComponentList, targetComponentList);

								//collect connectors here
								componentLevelDetailsList.add(connectorDetails);
								// used for table level lineage
								mappingSpecificComponent.add(connectorDetails);
							}
							
							//lineage for disconnected procedures
							disconnectedComponents.addAll(disconnectedLookupAndStoredProcedures(document, componentLevelDetailsList,
									transformationList, instanceNodeList, targetNodeList, fileName, folderName, mappingName,
									sessionExtensionList, mappingNameSessionNameMap));
							
							LOGGER.info("Connector mapping done mapping name: " + mappingName);
							CUSTOM_LOGGER.info(" Connector mapping done mapping name: " + mappingName);		
							
							tableLevelLineage(mappingSpecificComponent, sourceTargetMappingList); // used for table level lineage
						}
					}
				}
					
				for (ComponentLevelDetails prop : componentLevelDetailsList) {
                    if (StringUtils.isNotEmpty(prop.getTargetTableName())) {
                        prop.setTargetTableName(prop.getTargetTableName()
                                .replace(GeneralConstants.Shortcut_to_, "")
                                .replace(GeneralConstants.Shortcut_to_1, ""));
                    }
                    String updatedFileName = prop.getFolderName()
							.concat(QueryConstant.MAPPING_AND_FILENAME_INFIX)
							.concat(prop.getFileName().replaceAll("(?i)\\.xml", ""))
					        .concat(QueryConstant.MAPPING_AND_FILENAME_INFIX)
					        .concat(prop.getMappingName());
					batchUpdateParams.add(convertToObjectArray(updatedFileName, prop.getFolderName(),
							prop.getMappingName(), prop.getFromComponent(),
							prop.getFromComponentType().replaceAll(GeneralConstants.LOOKUP_PROCEDURE1, GeneralConstants.LOOKUP_PROCEDURE),
							prop.getToComponent(), prop.getToComponentType(), prop.getSourceDatabaseType(),
							prop.getSourceDbName(), prop.getTargetDatabaseType(), prop.getTargetDbName(),
							prop.getSourceTableName(), prop.getTargetTableName(), prop.getExecutionId(), prop.getSessionName()));
				}

				if (!batchUpdateParams.isEmpty()) {
					LOGGER.info("No of batch properties to be updated " + batchUpdateParams.size());
					CUSTOM_LOGGER.info("No of batch properties to be updated " + batchUpdateParams.size());

					int[] noOfRowsUpdated = informaticaLineageDao.insertIntoInfraComponentLineage(batchUpdateParams);
					int successCount = 0;
					for (int res : noOfRowsUpdated) {
						if (res == 1) {
							successCount++;
						}
					}

					LOGGER.info("No of rows affected " + successCount);
					CUSTOM_LOGGER.info("No of rows affected " + successCount);
					insertSummaryTableDetails(fileName, GeneralConstants.COMPLETED, startTime, sequenceId, batchSize,
							logFileName);
				}
				else {
					CUSTOM_LOGGER.info("No information to insert in infaComponentLevelLineage.\n\n\n");
				}
			}
			if (xmlTagNotAvailable && sourceTargetMappingList.isEmpty()) {
				informaticaLineageDao.insertIntoFailureInfraComponentLineage(fileName, errmsg, sequenceId);
				insertSummaryTableDetails(fileName, GeneralConstants.FAILED, startTime, sequenceId, batchSize,
						logFileName);
				LOGGER.info("failure file name insert in databse fileName: " + fileName + " ::: couse :" + errmsg);
				CUSTOM_LOGGER
						.info("failure file name insert in databse fileName: " + fileName + " ::: couse :" + errmsg);
			}

			/************************
			 * Insert to table level lineage 
			 ****************************/
			List<Object[]> batchUpdateParamsTableLevel = new ArrayList<Object[]>();
			int counter = 0;
			int sqlCounter = 0;
			for (SourceTargetMapping st : sourceTargetMappingList) {
				if (st.getSourceNode() != null && st.getTargetNode() != null
						&& st.getSourceNode().getTableName() != null && st.getTargetNode().getTableName() != null) {
					LOGGER.info(++counter + ">>Source table name : " + st.getSourceNode().getTableName() + " Target table name : "
							+ st.getTargetNode().getTableName());
					CUSTOM_LOGGER.info(counter + ">>Source table name : " + st.getSourceNode().getTableName()
							+ " Target table name : " + st.getTargetNode().getTableName());
					
					//insert for sql query
					String updatedFileName = st.getSourceNode().getFolderName()
							.concat(QueryConstant.MAPPING_AND_FILENAME_INFIX)
							.concat(st.getSourceNode().getFileName().replaceAll("(?i)\\.xml", ""))
					        .concat(QueryConstant.MAPPING_AND_FILENAME_INFIX)
					   		.concat(st.getSourceNode().getMappingName());
					if(!StringUtils.isBlank(st.getSourceNode().getSqlQuery())) {
						sqlCounter ++ ;
						
						String sqlQuery = updateSqlQueryParamTables(st.getSourceNode().getSqlQuery(), modifyParameterFileNameForInClause(paramFileNames), st.getSourceNode().getFolderName(), st.getSourceNode().getFileName().replaceAll("(?i)\\.xml", ""), st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());	
						String updatedTargetTable = getParamValue(st.getTargetNode().getTableName(), modifyParameterFileNameForInClause(paramFileNames), updatedFileName.split("__")[0], updatedFileName.split("__")[1], st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());				
						Object[] param1 = {st.getSourceNode().getExecutionId(), updatedFileName,
										  st.getSourceNode().getFolderName(), st.getSourceNode().getMappingName(),
										  //st.getSourceNode().getSqlQuery(), st.getSourceNode().getNodeElementType(),
										  sqlQuery, st.getSourceNode().getNodeElementType(),
										  updatedTargetTable, st.getSourceNode().getValue(),
										  st.getTargetNode().getValue(), st.getSourceNode().getSessionName(), paramFileNames};
						informaticaLineageDao.sqlInsertForInformatica(param1);

						savePreSqlQueries(st, updatedFileName, paramFileNames);
						savePostSqlQueries(st, updatedFileName, paramFileNames);
						continue;
					} else if(StringUtils.isBlank(st.getSourceNode().getSqlQuery()) &&
							(!st.getSourceNode().getPreSql().isEmpty()) ||
									!st.getSourceNode().getPostSql().isEmpty()) {
						sqlCounter ++ ;

						savePreSqlQueries(st, updatedFileName, paramFileNames);
						savePostSqlQueries(st, updatedFileName, paramFileNames);
					}
					if (StringUtils.isNotEmpty(st.getSourceNode().getSourceDbdname()) &&
							!st.getSourceNode().getTableName().contains("/")) {
						st.getSourceNode().setTableName(
								st.getSourceNode().getSourceDbdname() + "." + st.getSourceNode().getTableName());
					}
					batchUpdateParamsTableLevel
							.add(convertToObjectArrayTableLavel(updatedFileName, st.getSourceNode().getFolderName(),
									st.getSourceNode().getMappingName(), st.getSourceNode().getTableName(),
									st.getTargetNode().getTableName(), st.getSourceNode().getSourceDatabasetype(),
									st.getSourceNode().getSourceDbdname(), st.getTargetNode().getTargetDatabasetype(),
									st.getTargetNode().getTargetDbdname(), st.getSourceNode().getExecutionId(),
									st.getSourceNode().getNodeElementType(), st.getTargetNode().getNodeElementType(),
									st.getSourceNode().getSessionName(), paramFileNames));
					
				} else {
					LOGGER.info("?Source node : " + st.getSourceNode().getValue() + "  Target Node : " + st.getTargetNode().getValue());
					CUSTOM_LOGGER.info("?Source node : " + st.getSourceNode() + "  Target Node : " + st.getTargetNode());
				}
			}
			if (!batchUpdateParamsTableLevel.isEmpty()) {
				LOGGER.info("Total params to be inserted: "+batchUpdateParamsTableLevel.size()+" <<>> and (sql) : "+sqlCounter);
				int[] noOfRowsUpdatedTableLevel = informaticaLineageDao
						.insertIntoInfaTableLevelLineage(batchUpdateParamsTableLevel);
				int successCountTableLevel = 0;
				for (int res : noOfRowsUpdatedTableLevel) {
					if (res == 1) {
						successCountTableLevel++;
					}
				}

				LOGGER.info("Record added to table level lineage table " + successCountTableLevel);
				CUSTOM_LOGGER.info("Record added to table level lineage table " + successCountTableLevel);
				/********************************************************/
			}
			
			//Update table level lineage with disconnected procedures
			LOGGER.info("Inserting record in table level lineage for disconnected procedures. . .");
			CUSTOM_LOGGER.info("Inserting record in table level lineage for disconnected procedures. . .");
			List<Object[]> batchParamsForDisconnected = new ArrayList<Object[]>();
			int dcount = 0;
			for(Map<SingleComponentDetails, SingleComponentDetails> sourceToTarget: disconnectedComponents) {
				
				for(Map.Entry<SingleComponentDetails,SingleComponentDetails> entry: sourceToTarget.entrySet()) {
					String sql = entry.getKey().getSqlQuery();
					SingleComponentDetails source = entry.getKey();
					SingleComponentDetails target = entry.getValue();
					String updatedFileName = source.getFolderName()
							.concat(QueryConstant.MAPPING_AND_FILENAME_INFIX)
							.concat(source.getFileName().replaceAll("(?i)\\.xml", ""))
							.concat(QueryConstant.MAPPING_AND_FILENAME_INFIX)
							.concat(source.getMappingName());
					if(StringUtils.isBlank(sql)) {
						if(source.getTableName()!=null) {
							Object[] params = { updatedFileName, source.getFolderName(), source.getMappingName(),
									source.getTableName(), target.getTableName(), source.getDatabasetype(),
									source.getDbdname(), target.getDatabasetype(), target.getDbdname(), sequenceId,
									source.getComponentType(), target.getComponentType(), source.getSessionName(),paramFileNames};
							batchParamsForDisconnected.add(params);
						}
					}else {
						dcount ++;
						String sqlQuery = updateSqlQueryParamTables(source.getSqlQuery(), modifyParameterFileNameForInClause(paramFileNames), source.getFolderName(), source.getFileName().replaceAll("(?i)\\.xml", ""), source.getSessionName(), sequenceId);	
						String updatedTargetTable = getParamValue(target.getTableName(), modifyParameterFileNameForInClause(paramFileNames), updatedFileName.split("__")[0], updatedFileName.split("__")[1], source.getSessionName(), sequenceId);				
						Object[] params = {sequenceId, updatedFileName, source.getFolderName(), source.getMappingName(),
								   //source.getSqlQuery(), source.getComponentType(), target.getTableName(), source.getInstanceName(),
								   sqlQuery, source.getComponentType(), updatedTargetTable, source.getInstanceName(),
								   target.getInstanceName(), source.getSessionName(), paramFileNames};
						informaticaLineageDao.sqlInsertForInformatica(params);
					}
				}	
			}
			LOGGER.info("Disconnected procedures count : "+batchParamsForDisconnected.size()+" and ( with sql) : "+dcount);
			if (!batchParamsForDisconnected.isEmpty()) {
				
				int[] noOfRowsUpdatedTableLevel = informaticaLineageDao
						.insertIntoInfaTableLevelLineage(batchParamsForDisconnected);
				int successCountTableLevel = 0;
				for (int res : noOfRowsUpdatedTableLevel) {
					if (res == 1) {
						successCountTableLevel++;
					}
				}
				LOGGER.info("Record added to table level lineage table for disconnected procedures " + successCountTableLevel);
				CUSTOM_LOGGER.info("Record added to table level lineage table for disconnected procedures " + successCountTableLevel);
			}

		} catch (Exception e) {
			LOGGER.info("Error while parsing XML details because here xml file extension error:  " + errmsg);
			CUSTOM_LOGGER.info("Error while parsing XML details because here xml file extension error:  " + errmsg);
			informaticaLineageDao.insertIntoFailureInfraComponentLineage(fileName, errmsg+" "+e.getLocalizedMessage()+" >>"+sequenceId, sequenceId);
			insertSummaryTableDetails(fileName, GeneralConstants.FAILED, startTime, sequenceId, batchSize, logFileName);
			LOGGER.info("failure file name insert in database  table INFA_COMPONENT_LEVEL_LINEAGE fileName: " + fileName
					+ " ::: cause :" + errmsg);
			CUSTOM_LOGGER.info("failure file name insert in database  table INFA_COMPONENT_LEVEL_LINEAGE fileName: "
					+ fileName + " ::: cause :" + errmsg);
			e.printStackTrace();
		}
	}

	private void savePreSqlQueries(SourceTargetMapping st, String updatedFileName, String paramFileNames) {
		for (String preSql : st.getSourceNode().getPreSql()) {
			if (!StringUtils.isBlank(preSql)) {
				String preSqlQuery = updateSqlQueryParamTables(preSql, modifyParameterFileNameForInClause(paramFileNames), st.getSourceNode().getFolderName(),
						st.getSourceNode().getFileName().replaceAll("(?i)\\.xml", ""),
						st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());
				String updatedTargetTable = getParamValue(st.getTargetNode().getTableName(), modifyParameterFileNameForInClause(paramFileNames), updatedFileName.split("__")[0], updatedFileName.split("__")[1], st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());				
				
				Object[] param2 = {st.getSourceNode().getExecutionId(), updatedFileName,
						st.getSourceNode().getFolderName(), st.getSourceNode().getMappingName(),
						preSqlQuery, st.getSourceNode().getNodeElementType(),
						updatedTargetTable, st.getSourceNode().getValue(),
						st.getTargetNode().getValue(), st.getSourceNode().getSessionName(), paramFileNames};
				informaticaLineageDao.sqlInsertForInformatica(param2);
			}
		}
		for (String preSql : st.getTargetNode().getPreSql()) {
			if (!StringUtils.isBlank(preSql)) {
				String preSqlQuery = updateSqlQueryParamTables(preSql, modifyParameterFileNameForInClause(paramFileNames), st.getSourceNode().getFolderName(),
						st.getSourceNode().getFileName().replaceAll("(?i)\\.xml", ""),
						st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());
				String updatedTargetTable = getParamValue(st.getTargetNode().getTableName(), modifyParameterFileNameForInClause(paramFileNames), updatedFileName.split("__")[0], updatedFileName.split("__")[1], st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());				
				
				Object[] param2 = {st.getSourceNode().getExecutionId(), updatedFileName,
						st.getSourceNode().getFolderName(), st.getSourceNode().getMappingName(),
						preSqlQuery, st.getTargetNode().getNodeElementType(),
						updatedTargetTable, st.getSourceNode().getValue(),
						st.getTargetNode().getValue(), st.getSourceNode().getSessionName(), paramFileNames};
				informaticaLineageDao.sqlInsertForInformatica(param2);
			}
		}
	}

	private void savePostSqlQueries(SourceTargetMapping st, String updatedFileName, String paramFileNames) {
		for (String postSql : st.getSourceNode().getPostSql()) {
			if (!StringUtils.isBlank(postSql)) {
				String postSqlQuery = updateSqlQueryParamTables(postSql, modifyParameterFileNameForInClause(paramFileNames), st.getSourceNode().getFolderName(),
						st.getSourceNode().getFileName().replaceAll("(?i)\\.xml", ""),
						st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());
				String updatedTargetTable = getParamValue(st.getTargetNode().getTableName(), modifyParameterFileNameForInClause(paramFileNames), updatedFileName.split("__")[0], updatedFileName.split("__")[1], st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());				
				
				Object[] param3 = {st.getSourceNode().getExecutionId(), updatedFileName,
						st.getSourceNode().getFolderName(), st.getSourceNode().getMappingName(),
						postSqlQuery, st.getSourceNode().getNodeElementType(),
						updatedTargetTable, st.getSourceNode().getValue(),
						st.getTargetNode().getValue(), st.getSourceNode().getSessionName(), paramFileNames};
				informaticaLineageDao.sqlInsertForInformatica(param3);
			}
		}
		for (String postSql : st.getTargetNode().getPostSql()) {
			if (!StringUtils.isBlank(postSql)) {
				String postSqlQuery = updateSqlQueryParamTables(postSql, modifyParameterFileNameForInClause(paramFileNames), st.getSourceNode().getFolderName(),
						st.getSourceNode().getFileName().replaceAll("(?i)\\.xml", ""),
						st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());
				String updatedTargetTable = getParamValue(st.getTargetNode().getTableName(), modifyParameterFileNameForInClause(paramFileNames), updatedFileName.split("__")[0], updatedFileName.split("__")[1], st.getSourceNode().getSessionName(), st.getSourceNode().getExecutionId());				
				
				Object[] param3 = {st.getSourceNode().getExecutionId(), updatedFileName,
						st.getSourceNode().getFolderName(), st.getSourceNode().getMappingName(),
						postSqlQuery, st.getTargetNode().getNodeElementType(),
						updatedTargetTable, st.getSourceNode().getValue(),
						st.getTargetNode().getValue(), st.getSourceNode().getSessionName(), paramFileNames};
				informaticaLineageDao.sqlInsertForInformatica(param3);
			}
		}
	}

	//updated implementation for target
	private String getTargetTableInfo(String mappingName, String instanceName, String transformationName,
			NodeList instanceNodeList, Document document, NodeList sessionExtensionList) {
		
		NodeList sessionList = document.getElementsByTagName(GeneralConstants.SESSION_TAG);
		String returnInfo = transformationName;
		String dbName = "";
		String tableName = "";
		Map<String, String> returnMap = new HashMap<>();

		for(int i=0; i<instanceNodeList.getLength(); i++) {
			Element instance = (Element) instanceNodeList.item(i);
			String name = instance.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
			String tfType = instance.getAttribute(GeneralConstants.TRANSFORMATION_TYPE);
			if(StringUtils.equalsIgnoreCase(name, instanceName) &&
					StringUtils.equalsIgnoreCase(tfType, GeneralConstants.TARGET_DEFINITION)) {
				NodeList tableAttributes = instance.getElementsByTagName(GeneralConstants.TABLEATTRIBUTE);{
					if(tableAttributes.getLength()<=0) {
						break;
					}
					else {
						for(int j=0; j<tableAttributes.getLength(); j++) {
							Element tableAttribute = (Element) tableAttributes.item(j);
							String attName = tableAttribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
							if(StringUtils.equalsIgnoreCase(attName, GeneralConstants.Table_Name_Prefix)) {
								dbName = tableAttribute.getAttribute(GeneralConstants.VALUE);
							}
							if(StringUtils.equalsIgnoreCase(attName, GeneralConstants.Target_Table_Name)) {
								tableName = tableAttribute.getAttribute(GeneralConstants.VALUE);;
							}
						}
					}
				}
			}
		}
		
		//if got values then return
		if(!StringUtils.isBlank(tableName)) {
			if(StringUtils.isBlank(dbName))
				 returnInfo = tableName;
			else
				 returnInfo = dbName+"."+tableName;
		}

		for(int i=0; i<sessionList.getLength(); i++) {
			Element session = (Element) sessionList.item(i);
			String mapName = session.getAttribute(GeneralConstants.MAPPINGNAME);
			if(StringUtils.equalsIgnoreCase(mapName, mappingName)) {
				NodeList sessTransInstList = session.getElementsByTagName(GeneralConstants.SESSTRANSFORMATIONINST);
				
				for(int j=0; j<sessTransInstList.getLength(); j++) {
					
					Element sessTrans = (Element) sessTransInstList.item(j);
					String sInstanceName = sessTrans.getAttribute(GeneralConstants.SINSTANCENAME);
					String tfName = sessTrans.getAttribute(GeneralConstants.TRANSFORMATIONNAME);
					String tfType = sessTrans.getAttribute(GeneralConstants.TRANSFORMATIONTYPE);

					if((StringUtils.equalsIgnoreCase(sInstanceName, instanceName) ||
							StringUtils.equalsIgnoreCase(tfName, transformationName)) &&
							StringUtils.equalsIgnoreCase(tfType, GeneralConstants.TARGET_DEFINITION)) {
						NodeList attributeList = sessTrans.getElementsByTagName(GeneralConstants.ATTRIBUTE_TAG);
						LOGGER.info("sInstanceName size: {}", attributeList.getLength());
						for(int k=0; k<attributeList.getLength(); k++) {
							 Element attribute = (Element) attributeList.item(k);
							 String name = attribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
							 if(StringUtils.equalsIgnoreCase(name, GeneralConstants.Table_Name_Prefix)) {
								 dbName = attribute.getAttribute(GeneralConstants.VALUE);
							 }
							 if(StringUtils.equalsIgnoreCase(name, GeneralConstants.Target_Table_Name)) {
								 tableName = attribute.getAttribute(GeneralConstants.VALUE);
							 }
						}
					}
				}
			}
		}
		
		//get file if blank table name
		if(StringUtils.isBlank(tableName)) {
            String fileNameWithPathOrTfName =
					getInfoFromSessionExtension(instanceName, transformationName, "TARGET", sessionExtensionList);
			if (!dbName.trim().isEmpty() && StringUtils.isNotEmpty(fileNameWithPathOrTfName)) {
				if (!fileNameWithPathOrTfName.contains("/")) {
					returnInfo = dbName.concat(".").concat(fileNameWithPathOrTfName);
				} else {
					returnInfo = fileNameWithPathOrTfName;
				}
			} else {
				returnInfo = fileNameWithPathOrTfName;
			}
		} else {
			 if(StringUtils.isBlank(dbName))
				 returnInfo = tableName;
			 else
				 returnInfo = dbName+"."+tableName;
		 }
		
		return returnInfo;
	}//
	
	//extracting file location from session extension   
	//PARAMETERS ARE instanceName, transformationName, transformationType
	private String getInfoFromSessionExtension(String instanceName, String tfName, String type, NodeList sessionExtensionList) {
		
		//Map<String, String> infoMap = new HashMap<>();
		String directory = "";
		String file = "";
		
		boolean directoryIsSet = false;
		boolean fileIsSet = false;
		
		if(StringUtils.equalsIgnoreCase(type, "SOURCE")) {
			//FOR SOURCE
			for(int i=0; i<sessionExtensionList.getLength(); i++) {
					
				Element sessExt = (Element) sessionExtensionList.item(i);
				String matchKey = sessExt.getAttribute(GeneralConstants.SINSTANCENAME);
				//System.out.println(key+"------->"+matchKey+"----->W: "+x+" ::------>SE: "+i+" ::");
				if(matchKey.equalsIgnoreCase(instanceName) || matchKey.equalsIgnoreCase(tfName)) {
					NodeList sessAttributes = sessExt.getElementsByTagName(GeneralConstants.ATTRIBUTE_TAG);
					
					for(int j=0; j<sessAttributes.getLength(); j++) {
						
						Element sessAttribute = (Element) sessAttributes.item(j);
						
						String srcDir = sessAttribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
						
						//either name is equals to or contains (for source, source qualifier, lookup )
						if(srcDir.equalsIgnoreCase(GeneralConstants.SOURCE_DIR_NAME) ||
								srcDir.toLowerCase().contains(GeneralConstants.SOURCE_DIR_NAME.toLowerCase())) {
							directory = sessAttribute.getAttribute("VALUE");
							directoryIsSet = true;
						}
						if(srcDir.equalsIgnoreCase(GeneralConstants.FILE_NAME) ||
								srcDir.toLowerCase().contains(GeneralConstants.FILE_NAME.toLowerCase())){
							file = "/"+sessAttribute.getAttribute("VALUE");
							fileIsSet=true;
						}
						if(directoryIsSet && fileIsSet) return (directory+file).replaceAll("\\\\", "");
					}
				}
			}
		
		}//IF ENDS
		else if (StringUtils.equalsIgnoreCase(type, "TARGET")) {
			//FOR TARGET
			
			for(int i=0; i<sessionExtensionList.getLength(); i++) {
				
				Element sessExt = (Element) sessionExtensionList.item(i);
				//String sessionExtName = sessExt.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
				String matchKey = sessExt.getAttribute(GeneralConstants.SINSTANCENAME);
				String transformationType = sessExt.getAttribute(GeneralConstants.TRANSFORMATIONTYPE);
				//System.out.println("Target---->"+key+"||"+key2+"---->"+matchKey);
				if((matchKey.equalsIgnoreCase(instanceName)|| matchKey.equalsIgnoreCase(tfName))
						&& StringUtils.equalsIgnoreCase(transformationType, GeneralConstants.TARGET_DEFINITION)) {
					
					NodeList sessAttributes = sessExt.getElementsByTagName(GeneralConstants.ATTRIBUTE_TAG);
					
					for(int j=0; j<sessAttributes.getLength(); j++) {
						
						Element sessAttribute = (Element) sessAttributes.item(j);
						String targetDir = sessAttribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
						if(targetDir.equalsIgnoreCase(GeneralConstants.OUTPUT_DIR) ||
								targetDir.toLowerCase().contains(GeneralConstants.OUTPUT_DIR.toLowerCase())) {
							directory = sessAttribute.getAttribute("VALUE");
							directoryIsSet = true;
						}
						if(targetDir.equalsIgnoreCase(GeneralConstants.OUTPUT_FILE) ||
								targetDir.toLowerCase().contains(GeneralConstants.OUTPUT_FILE.toLowerCase())){
							file = sessAttribute.getAttribute("VALUE");
							fileIsSet=true;
						}
						if(directoryIsSet && fileIsSet) {
							if(!StringUtils.isBlank(file)) {
								break;
							}
						}
					}
				}
			}
			if (directoryIsSet && fileIsSet) {
				if (StringUtils.isAllBlank(directory, file)) {
					return instanceName;
				} else {
					file = "/"+file;
					return (directory+file).replaceAll("\\\\", "");
				}
			} else {
				return instanceName;
			}
		}
		else if(StringUtils.equalsIgnoreCase(type, "LOOKUP")) {
			// for lookup
			for (int i = 0; i < sessionExtensionList.getLength(); i++) {

				Element sessExt = (Element) sessionExtensionList.item(i);
				String matchKey = sessExt.getAttribute(GeneralConstants.SINSTANCENAME);

				if (matchKey.equalsIgnoreCase(instanceName)|| matchKey.equalsIgnoreCase(tfName)) {

					NodeList sessAttributes = sessExt.getElementsByTagName(GeneralConstants.ATTRIBUTE_TAG);
					for (int j = 0; j < sessAttributes.getLength(); j++) {
						Element sessAttribute = (Element) sessAttributes.item(j);
						String targetDir = sessAttribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);

						if (StringUtils.equalsIgnoreCase(targetDir, GeneralConstants.LOOKUP_SOURCE_DIR)) {
							directory = sessAttribute.getAttribute("VALUE");
							directoryIsSet = true;
						}
						if (StringUtils.equalsIgnoreCase(targetDir, GeneralConstants.LOOKUP_SOURCE_FILE)) {
							file += "/" + sessAttribute.getAttribute("VALUE");
							fileIsSet = true;
						}
						if (directoryIsSet && fileIsSet) return (directory+file).replaceAll("\\\\", "");
					}
				}
			}
		}
		return directory+file;
	}

	//extracting sql for source qualifier
	private void getSqlForSourceQualifier(Document document, String instanceName, NodeList transformationList,
										  NodeList instanceNodeList, ComponentLevelDetails connectorDetails) {
		String sqlQuery = "";
		String preSql = "";
		String postSql = "";
		String tName = instanceName;
		//get transformation tag from instance tags
		for(int i=0; i<instanceNodeList.getLength(); i++) {	
			Element instance = (Element) instanceNodeList.item(i);
			String iName = instance.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
			
			if(StringUtils.equalsIgnoreCase(iName, instanceName)) {
                tName = instance.getAttribute(GeneralConstants.TRANSFORMATION_NAME);
			}
		}
		
		//get transformation tag from transformation tag list
		for(int j=0; j<transformationList.getLength(); j++) {
			Element transformation = (Element) transformationList.item(j);
			String tfName = transformation.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
			
			if(StringUtils.equalsIgnoreCase(tfName, tName)) {
				NodeList tableAttributes = transformation.getElementsByTagName(GeneralConstants.TABLEATTRIBUTE);
				for(int k=0; k<tableAttributes.getLength(); k++) {
					Element tableattribute = (Element) tableAttributes.item(k);
					String name = tableattribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
					if (StringUtils.equalsIgnoreCase(name, GeneralConstants.Sql_Query) ||
							StringUtils.equalsIgnoreCase(name, GeneralConstants.Sql_Override_Query)) {
						sqlQuery = tableattribute.getAttribute(GeneralConstants.VALUE);
					} else if(StringUtils.equalsIgnoreCase(name, GeneralConstants.Pre_SQL)) {
						preSql = tableattribute.getAttribute(GeneralConstants.VALUE);
					} else if(StringUtils.equalsIgnoreCase(name, GeneralConstants.Post_SQL)) {
						postSql = tableattribute.getAttribute(GeneralConstants.VALUE);
					}
				}
			}
		}
		connectorDetails.setSqlQuery(sqlQuery);
		connectorDetails.getPreSql().add(preSql);
		connectorDetails.getPostSql().add(postSql);
		getSqlQueryFromSessTransInstAttributeList(document, connectorDetails, connectorDetails.getToComponent(),
				tName, GeneralConstants.SOURCE_QUALIFIER);
	}
	
	
	//extracting stored procedure info
	private String getInfoForStoredProcedure(String instanceName, NodeList transformationList, NodeList instanceNodeList) {
		
		String tName = instanceName;
		//get transformation tag from instance tags
		for(int i=0; i<instanceNodeList.getLength(); i++) {	
			Element instance = (Element) instanceNodeList.item(i);
			String iName = instance.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
			
			if(StringUtils.equalsIgnoreCase(iName, instanceName)) {
				String tfName = instance.getAttribute(GeneralConstants.TRANSFORMATION_NAME);
				tName = tfName;
			}
		}
		
		
		//get transformation tag from transformation tag list
		for(int j=0; j<transformationList.getLength(); j++) {
			Element transformation = (Element) transformationList.item(j);
			String tfName = transformation.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
			
			if(StringUtils.equalsIgnoreCase(tfName, tName)) {
				NodeList tableAttributes = transformation.getElementsByTagName(GeneralConstants.TABLEATTRIBUTE);
				for(int k=0; k<tableAttributes.getLength(); k++) {
					Element tableattribute = (Element) tableAttributes.item(k);
					String name = tableattribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
					if(StringUtils.equalsIgnoreCase(name, GeneralConstants.STORED_PROCEDURE_NAME)) {
						tName = tableattribute.getAttribute(GeneralConstants.VALUE);
						return tName+"__sp";
					}
				}
			}
		}
		
		return tName+"__sp";
		
	}
	
	
	//extracting disconnected lookup and stored procedures
	private Set<Map<SingleComponentDetails, SingleComponentDetails>> disconnectedLookupAndStoredProcedures(
			Document document, Set<ComponentLevelDetails> componentList, NodeList transformationList,
			NodeList instanceNodeList, NodeList targetNodeList, String fileName, String folderName, String mappingName,
			NodeList sessionExtensionList, Map<String, String> mappingNameSessionNameMap) {
		
		Map<String, String> tProcedures = new HashMap<>();
		Map<String, List<String>> iProcedures = new HashMap<>();
		Map<String, List<String>> disconnetedProcs = new HashMap<>();
		Map<String, List<String>> targetInfo = new HashMap<>();
		List<SingleComponentDetails> targetList = new ArrayList<>();
		Set<Map<SingleComponentDetails, SingleComponentDetails>> sourceTarget = new HashSet<>();
		
		for(int i=0; i<transformationList.getLength(); i++) {
		   Element transformation = (Element) transformationList.item(i);
		   String type = transformation.getAttribute(GeneralConstants.TYPE);
		   if(StringUtils.equalsIgnoreCase(type, GeneralConstants.LOOKUP_PROCEDURE)||
				   StringUtils.equalsIgnoreCase(type, GeneralConstants.STORED_PROCEDURE)) {
			   String name = transformation.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
			   tProcedures.put(name, type);
		   }
		}
		
		for(int j=0; j<instanceNodeList.getLength(); j++) {
			Element instance = (Element) instanceNodeList.item(j);
			String tType = instance.getAttribute(GeneralConstants.TRANSFORMATION_TYPE);
			if(StringUtils.equalsIgnoreCase(tType, GeneralConstants.LOOKUP_PROCEDURE)||
					   StringUtils.equalsIgnoreCase(tType, GeneralConstants.STORED_PROCEDURE)) {
				String tName = instance.getAttribute(GeneralConstants.TRANSFORMATION_NAME);
				String iName = instance.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
				List<String> instanceInfo = new ArrayList<>();
				instanceInfo.add(tName);
				instanceInfo.add(tType);
				iProcedures.put(iName, instanceInfo);
			}
			if(StringUtils.equalsIgnoreCase(tType, GeneralConstants.TARGET_DEFINITION)) {
				String tName = instance.getAttribute(GeneralConstants.TRANSFORMATION_NAME);
				String iName = instance.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
				List<String> instanceInfo = new ArrayList<>();
				instanceInfo.add(tName);
				instanceInfo.add(tType);
				targetInfo.put(iName, instanceInfo);
			}
		}
		
		for(String name: iProcedures.keySet()) {
			boolean present = false;
			
			for(ComponentLevelDetails comp: componentList ) {
				
				String fromComponent = comp.getFromComponent();
				String toComponent = comp.getToComponent();
				
				if(StringUtils.equals(name, fromComponent) || StringUtils.equals(name, toComponent)) {
					present = true;
				}
			}
			if(!present) {
				disconnetedProcs.put(name, iProcedures.get(name));
			}
		}
		
		System.out.println("Elements in disconnected map: "+disconnetedProcs.size() +"\n"+ disconnetedProcs.keySet());
		System.out.println("No of targets = "+targetInfo.size());
		
		if(targetInfo.isEmpty() || disconnetedProcs.isEmpty()) {
			LOGGER.info("No disconnected procedures found.");
			return new HashSet<>();
		}else {
			
			for(Map.Entry<String, List<String>> entry:targetInfo.entrySet()) {
				
				SingleComponentDetails targetElement = new SingleComponentDetails();
				targetElement.setInstanceName(entry.getKey());
				targetElement.setComponentType(GeneralConstants.TARGET_DEFINITION);
				targetElement.setFileName(fileName);
				targetElement.setFolderName(folderName);
				targetElement.setMappingName(mappingName);
				targetElement.setSessionName(mappingNameSessionNameMap.getOrDefault(mappingName, ""));
				String tableName = getTargetTableInfo(mappingName, entry.getKey(),
						entry.getValue().get(0), instanceNodeList, document, sessionExtensionList);
				tableName = tableName.replaceAll("\\s+", "_");
				targetElement.setTableName(tableName);
				targetList.add(targetElement);
			}
			
		}
		
		
		for(Map.Entry<String, List<String>> entry: disconnetedProcs.entrySet()) {
			
			String instName = entry.getKey();
			List<String> tfInfo = entry.getValue();
			
			if(StringUtils.equals(tfInfo.get(1), GeneralConstants.LOOKUP_PROCEDURE)) {
				
				SingleComponentDetails lookupElement = new SingleComponentDetails();
				lookupElement.setComponentType("Disconnected "+GeneralConstants.LOOKUP_PROCEDURE);
				lookupElement.setFileName(fileName);
				lookupElement.setFolderName(folderName);
				lookupElement.setMappingName(mappingName);
				lookupElement.setInstanceName(entry.getKey());
				lookupElement.setTransformationName(entry.getValue().get(0));
				lookupElement.setSessionName(mappingNameSessionNameMap.getOrDefault(mappingName, ""));
				Map<String, String> values = lookupTransformationDetails(instanceNodeList, instName, transformationList, sessionExtensionList);
				String tableName = values.get("source");
				if(tableName!=null) {
					tableName = tableName.replaceAll("\\s+", "_");
				}
			
				if(tableName!=null && StringUtils.startsWith(tableName, GeneralConstants.Shortcut_to_)) {
					tableName = tableName.substring(GeneralConstants.Shortcut_to_.length());
				}
				
				lookupElement.setTableName(tableName);
				lookupElement.setDatabasetype(values.get("dbType"));
				lookupElement.setSqlQuery(values.get("sql"));
				
				for(int i=0; i<targetList.size(); i++) {
					Map<SingleComponentDetails,SingleComponentDetails> oneToOne = new HashMap<>();
					oneToOne.put(lookupElement, targetList.get(i));
					sourceTarget.add(oneToOne);
				}
				
			}else {//for stored procedure
				
				SingleComponentDetails spElement = new SingleComponentDetails();
				spElement.setComponentType("Disconnected "+GeneralConstants.STORED_PROCEDURE);
				spElement.setFileName(fileName);
				spElement.setFolderName(folderName);
				spElement.setMappingName(mappingName);
				spElement.setSessionName(mappingNameSessionNameMap.getOrDefault(mappingName, ""));
				String tableName = getInfoForStoredProcedure(instName, transformationList, instanceNodeList);
				if(tableName!=null) {
					tableName = tableName.replaceAll("\\s+", "_");
				}
				if(tableName!=null && StringUtils.startsWith(tableName, GeneralConstants.Shortcut_to_)) {
					tableName = tableName.substring(GeneralConstants.Shortcut_to_.length());
				}
				
				spElement.setTableName(tableName);					
				for(int i=0; i<targetList.size(); i++) {
					Map<SingleComponentDetails,SingleComponentDetails> oneToOne = new HashMap<>();
					oneToOne.put(spElement, targetList.get(i));
					sourceTarget.add(oneToOne);
				}
			}
	
		}//end for
		
		return sourceTarget;
		
	}
	
	
	//lookup copied - - - 
	//data extraction for lookup procedure
	private Map<String, String> lookupTransformationDetails(NodeList instanceNodeList, String name, NodeList transformationList, NodeList sessionExtensionList) {
		
		Map<String, String> expressions  = new HashMap<>();
		
		String nameInInstance = name;
		String SINSTANCENAME = name;
		//get TRANSFORMATION_NAME from instance by matching NAME with TOINSTANCE of connector tag
		for(int i=0; i<instanceNodeList.getLength(); i++) {
			Element instanceElement = (Element) instanceNodeList.item(i);
			String instanceName = instanceElement.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
			if(StringUtils.equalsIgnoreCase(instanceName, nameInInstance)) {
				String instanceTransformationName = instanceElement.getAttribute(GeneralConstants.TRANSFORMATION_NAME);
				nameInInstance=instanceTransformationName;
			}
		}
		
		for(int j=0; j<transformationList.getLength(); j++) {
			Element transformation = (Element) transformationList.item(j);
			String tName = transformation.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
			
			if(StringUtils.equalsIgnoreCase(nameInInstance, tName)) {
				
				NodeList tableAttributes = transformation.getElementsByTagName(GeneralConstants.TABLEATTRIBUTE);
				
				for(int x=0; x<tableAttributes.getLength(); x++) {
					
					Element tableAttribute = (Element) tableAttributes.item(x);
					String lookupTableNameConst = tableAttribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
					//String lookupSourceTypeConst = tableAttribute.getAttribute(GeneralConstants.LOOKUP_SOURCE_TYPE);
					
					if(StringUtils.equalsIgnoreCase(lookupTableNameConst, GeneralConstants.LOOKUP_TABLE_NAME)) {
						String lookupTableName = tableAttribute.getAttribute(GeneralConstants.VALUE);
						if(StringUtils.isBlank(lookupTableName)) {
							lookupTableName = nameInInstance;
						}
						expressions.put("source", lookupTableName);
						//lookup table name
					}
					if(StringUtils.equalsIgnoreCase(lookupTableNameConst, GeneralConstants.LOOKUP_SOURCE_TYPE)) {
						//lookup database/source type
						String databaseType = tableAttribute.getAttribute(GeneralConstants.VALUE);
						expressions.put("dbType",databaseType);
						if(StringUtils.equalsIgnoreCase(databaseType, GeneralConstants.FLAT_FILE)) {
							String fileDir = getInfoFromSessionExtension(nameInInstance, SINSTANCENAME, "LOOKUP", sessionExtensionList);
							expressions.put("source",fileDir);
						}
						
					}
					if (lookupTableNameConst.equalsIgnoreCase(GeneralConstants.Lookup_Sql_Override)) {
						String sql = tableAttribute.getAttribute(GeneralConstants.VALUE);
						expressions.put("sql", sql);
					}
				}
			}
		}
		return expressions;
	}
		
	//	
	
	
	
	
	//------------------------------------------------------------------------------------------------
	//object to array
	private Object[] convertToObjectArray(String file_name, String folder_name, String mapping_name,
			String from_component, String from_component_type, String to_component, String to_component_type,
			String source_databasetype, String source_dbdname, String target_databasetype, String target_dbdname,
			String source_table_name, String target_table_name, Integer execution_id, String sessionName) {
		String error_msg = "";
		String status = GeneralConstants.SUCCESS;
		return new Object[] { file_name, folder_name, mapping_name, from_component, from_component_type, to_component,
				to_component_type, source_databasetype, source_dbdname, target_databasetype, target_dbdname, status,
				error_msg, source_table_name, target_table_name, execution_id, sessionName};
	}

	private Object[] convertToObjectArrayTableLavel(String fileName, String folderName, String mappingName,
			String fromTable, String toTable, String sourceDatabasetype, String sourceDbdname,
			String targetDatabasetype, String targetDbdname, Integer executionId, String sourceType, String targetType, String sessionName, String paramFileNames) {
		
		return new Object[] { fileName, folderName, mappingName, fromTable, toTable, sourceDatabasetype, sourceDbdname,
				targetDatabasetype, targetDbdname, executionId, sourceType, targetType, sessionName, paramFileNames};
	}

	/**
	 * The tableLevelLineage
	 * 
	 * @param connectorSet : This is connector object list for a mapping
	 */
	private void tableLevelLineage(Set<ComponentLevelDetails> connectorSet,
								   List<SourceTargetMapping> sourceTargetMappingList) {

		List<ComponentLevelDetails> connectorList = new ArrayList<ComponentLevelDetails>(connectorSet);
		LOGGER.info("Graph creation started...");
		List<Node> graph = createGraphObject(connectorList);
		LOGGER.info("Graph creation completed...");
		List<Node> sourceNodes = graph.stream().filter(c -> {
			return (GeneralConstants.SOURCE_TAG.equals(c.getType())
					|| GeneralConstants.LOOKUP_PROCEDURE.equals(c.getType())
					|| GeneralConstants.STORED_PROCEDURE.equals(c.getType()));
		}).collect(Collectors.toList());
		
		
		System.out.println("Total no of sources: "+sourceNodes.size());
		
		for (Node sourceNode : sourceNodes) {
			LOGGER.info("BFS Iterative Start...for the source node... " + sourceNode.getValue());
			bfs(sourceNode, sourceTargetMappingList);
			LOGGER.info("BFS Iterative End...for the source node... " + sourceNode.getValue());
		}
	}

	/**
	 * Graph Traversal methods : Breadth First Search
	 * 
	 * @param startNode
	 * @param sourceTargetMappingList
	 */
	private void bfs(Node startNode, List<SourceTargetMapping> sourceTargetMappingList) {
		
		Queue<Node> queue = new LinkedList<>();
		Set<String> visited = new HashSet<>();

		queue.add(startNode);
		visited.add(startNode.getValue() + startNode.getType());
		
		while (!queue.isEmpty()) {
			LOGGER.info("startNode....................................."+startNode);
			LOGGER.info("startNodeValue....................................."+startNode.getValue());


			Node currentNode = queue.remove();
			LOGGER.info(currentNode.getValue());
			if (GeneralConstants.TARGET_TAG.equals(currentNode.getType())) {
				LOGGER.info("Target Node found..." + currentNode.getValue());
				SourceTargetMapping sourceTargetMapping = new SourceTargetMapping();
				LOGGER.info("source...."+startNode);
				LOGGER.info("target...."+currentNode);
				sourceTargetMapping.setSourceNode(startNode);
				sourceTargetMapping.setTargetNode(currentNode);
				sourceTargetMappingList.add(sourceTargetMapping);
				
			}

			for (Node n : currentNode.getNeighbors()) { // Check each neighbor node
				if (!visited.contains(n.getValue() + n.getType())) { // If neighbor node's value is not in visited set
					queue.add(n);
					visited.add(n.getValue() + n.getType());
				}
			}
		}
	}

	/**
	 * Constructing the graph
	 * 
	 * @param connectorList
	 * @return
	 */
	private List<Node> createGraphObject(List<ComponentLevelDetails> connectorList) {

		List<Node> nodes = new ArrayList<Node>();
		for (ComponentLevelDetails c : connectorList) {
			
			Node node = new Node(c.getFromComponent(), c.getFromComponentType());
			int index = nodes.indexOf(node);
			if (index == -1) {
				nodes.add(node);
			} else {
				node = nodes.get(index);
			}
			addNeighbors(node, nodes, connectorList);

			// adding additional information to the Node
			node.setTableName(c.getSourceTableName());
			node.setMappingName(c.getMappingName());
			node.setFolderName(c.getFolderName());
			node.setFileName(c.getFileName());
			node.setSourceDatabasetype(c.getSourceDatabaseType());
			node.setSourceDbdname(c.getSourceDbName());
			node.setExecutionId(c.getExecutionId());
			node.setNodeElementType(c.getFromComponentType());
			node.setSqlQuery(c.getSqlQuery());
			node.setPreSql(c.getPreSql());
			node.setPostSql(c.getPostSql());
			node.setSessionName(c.getSessionName());
		}
		return nodes;
	}

	/**
	 * Adding neighbors for a node
	 * 
	 * @param node
	 * @param nodes
	 * @param connectorList
	 */
	private void addNeighbors(Node node, List<Node> nodes, List<ComponentLevelDetails> connectorList) {

		List<ComponentLevelDetails> connectorListFilter = connectorList.stream().filter(c -> {
			return node.getValue().equals(c.getFromComponent());
		}).collect(Collectors.toList());

		for (ComponentLevelDetails c : connectorListFilter) {
			Node n = new Node(c.getToComponent(), c.getToComponentType());

			int index = nodes.indexOf(n);
			if (index == -1) {
				nodes.add(n);
			} else {
				n = nodes.get(index);
			}

			node.addEdge(n);

			// adding additional information to the Node
			n.setTableName(c.getTargetTableName());
			n.setMappingName(c.getMappingName());
			n.setFolderName(c.getFolderName());
			n.setFileName(c.getFileName());
			n.setTargetDatabasetype(c.getTargetDatabaseType());
			n.setTargetDbdname(c.getTargetDbName());
			n.setExecutionId(c.getExecutionId());
			n.setNodeElementType(c.getToComponentType());
			n.setSqlQuery(c.getSqlQuery());
            n.setPreSql(c.getPreSql());
            n.setPostSql(c.getPostSql());
			n.setSessionName(c.getSessionName());
		}
	}

	private void insertSummaryTableDetails(String fileName, String success, long startTime, Integer sequenceId,
			Integer batchSize, String logFileName) {
		LOGGER.info("insertSummaryTableDetails :" + fileName + " sequence::::::" + sequenceId+"   Status:: "+success);
		long endTime = Instant.now().toEpochMilli();
		informaticaLineageDao.insertIntoInfaExecutionSummery(sequenceId, batchSize, fileName, startTime, endTime,
				success, logFileName);

	}	
	
	@Override
	public Map<String, Object> uploadFilesForInformatica(MultipartFile[] files, String projectName) throws LineageBusinessException {
		Map<String, Object> requiredObjects = new HashMap<>();
		try {

			Long jobId = jobRepo.getJobIdByMax();
			LineageJob lineageJob = new LineageJob();
			lineageJob.setJobId(jobId);
			lineageJob.setProjectName(projectName);
			lineageJob.setTechnology(TechnologyConstants.INFORMATICA);
			lineageJob.setStartTime(new Date());
			lineageJob.setJobParams(TechnologyConstants.INFORMATICA);
			lineageJob.setUploadType(GeneralConstants.UPLOAD_TYPE);
		
			lineageJob = jobRepo.save(lineageJob);
			jobId = lineageJob.getJobId();
		
			LOGGER.info("Job Id assigned as: {}", jobId);
			
			File newInputDirectory = new File(uploadFileLocation + File.separator + jobId);
			newInputDirectory.mkdir();

			LOGGER.info("Save location = {}", newInputDirectory);
			
			double size = 0;
			long startTime = Instant.now().toEpochMilli();
			
			/**
			for (MultipartFile file : files) {
				
				if(file.getOriginalFilename().toLowerCase().endsWith(GeneralConstants.ZIP_FILE_FORMAT)) {
					CommonUtil.unzipMultipartFileForInformatica(file, newInputDirectory.getAbsolutePath());
					continue;
				}				
				byte[] bytes = file.getBytes();
				size += file.getSize();
				Path path = Paths.get(newInputDirectory + File.separator + file.getOriginalFilename());
				Path canonicalPath = path.normalize();
				LOGGER.info(newInputDirectory+"/"+file.getOriginalFilename());
				Files.write(canonicalPath, bytes);
			}**/
			
			CommonUtil.uploadAllScriptsToInputLocation(files,newInputDirectory.getAbsolutePath());
			File f = new File(newInputDirectory.getAbsolutePath());
			File[] arr = f.listFiles();
			
			LOGGER.info("No of files = {}", arr.length);
			
			//calling parsing service
			long endTime = Instant.now().toEpochMilli();
			long diff = (endTime - startTime)/1000;
			LOGGER.info("Upload time taken in seconds = {}", diff);
			LOGGER.info("Total size of files in MB = {}", (size/(1024*1024)));
			LOGGER.info("Uploaded files successfully and calling parsing methods.");
			
			requiredObjects.put(GeneralConstants.FILE_LENGTH, arr.length);
			requiredObjects.put(GeneralConstants.SIZE, size);
			requiredObjects.put(GeneralConstants.PROJECT_NAME, projectName);
			requiredObjects.put(GeneralConstants.LINEAGE_JOB, lineageJob);
			
			return requiredObjects;
		
		} catch (LineageBusinessException e) {
			throw e;
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
			throw new LineageRuntimeException(ex.getMessage());
		}
	}

	public void parseInformaticaParameterFiles() {
		informaticaParamDetailsRepository.truncateInformaticaParamDetails();
		LOGGER.info("Start Parsing Parameter files one by one.....................................................................................");
		
		File file = new File(informaticaParameterFileLocation);
		File[] filesArr = file.listFiles();
		int counter = 1;
		for(File f: filesArr) {
			try {
			LOGGER.info("Parameter file "+counter+" of "+filesArr.length +" : "+f.getName());
			counter++;;
			
			Map<String,Map<String,String>> paramMap = new HashMap<>();
			Map<String,String> paramInternalMap = new HashMap<>();
	        Map<String,String> globalParamMap = new HashMap<>();
	        Set<String> folderSet = new HashSet<>();
	        
	        String folderName = "";
			String workflowName = "";
			String sessionName = " ";
			boolean isGlobal = false;
			boolean isParam = false;

			int lineNo = 0;
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String line;
				while ((line = br.readLine()) != null) {

					lineNo++;
					line = line.trim();
					if(line.toUpperCase().equals("[GLOBAL]")) {
						isGlobal = true;
					}
					if(isGlobal && line.startsWith("$") && line.contains("=")) {
						globalParamMap.put(getKey(line.split("=",2)[0].trim()), line.split("=",2)[1].trim());
					}

					if(line.startsWith("[") && line.endsWith("]") && line.contains(".")) {
						isGlobal = false;

						if(folderName!="" || (folderName.isEmpty() && !sessionName.trim().isEmpty())) {
							if(folderName!="") {
								folderSet.add(folderName);
							}
							paramMap.put(folderName+":"+workflowName+":"+sessionName, paramInternalMap);
						    folderName = ""; workflowName = ""; sessionName = " ";
							paramInternalMap = new HashMap<>();
						}

						String[] arr = line.split("[.]");
						folderName = arr[0].replace("[", "").trim();
						
						//scenarios: f.w.s, f.s, f.w, .s
						if(arr.length==3) {
							workflowName = arr[1];
							if(workflowName.contains(":")) {
								workflowName = workflowName.split(":")[1].trim();
							}
							sessionName = arr[2];
							if(sessionName.contains(":")) {
								sessionName = sessionName.split(":")[1].replace("]", "").trim();
							}
						} else if(arr.length==2) {
							String workflowOrSession = arr[1].replace("]", "").trim();
							if(workflowOrSession.contains(":")) {
								workflowOrSession = workflowOrSession.split(":")[1];
							}
							if(workflowOrSession.startsWith("wf_")) {
								workflowName = workflowOrSession;
							} else if(workflowOrSession.startsWith("s_")){
								sessionName = workflowOrSession;
							}
						}
						isParam = true;
					}

					if(isParam && line.contains("$") && line.contains("=")) {
						paramInternalMap.put(getKey(line.split("=",2)[0].trim()), line.split("=",2)[1].trim());
					}
				}
				if(folderName!="") {
					folderSet.add(folderName);
				}
				paramMap.put(folderName+":"+workflowName+":"+sessionName, paramInternalMap);	
			} catch (IOException ex) {
				LOGGER.error("Exception occured for filename: "+f.getName());
				LOGGER.error("Exception message: "+ex.getMessage() +" lineNo: "+lineNo);
			}

			//table insertion
			globalParamMap.forEach((paramName,paramValue)-> {
				InformaticaParamDetails informaticaParamDetails = InformaticaParamDetails.builder()
						.fileName(f.getName())
						.folderName(folderSet.toString().replaceAll("^\\[|\\]$", "").replaceAll(",\\s+", ",").trim())
						.paramName(paramName)
						.paramValue(paramValue)
						.type("GLOBAL")
						.build();
				informaticaParamDetailsRepository.save(informaticaParamDetails);
			});

			paramMap.forEach((folderWorflowSession,map)-> {

				String[] arr = folderWorflowSession.split(":");
				map.forEach((paramName,paramValue)-> {
					InformaticaParamDetails informaticaParamDetails = InformaticaParamDetails.builder()
							.fileName(f.getName())
							.folderName(arr[0])
							.worflowName(arr[1])
							.sessionName(arr[2].trim())
							.paramName(paramName)
							.paramValue(paramValue)
							.type("SPECIFIC")
							.build();
					informaticaParamDetailsRepository.save(informaticaParamDetails);
				});
			});
			} catch (Exception ex) {
				LOGGER.error("Exception occured for filename: "+f.getName());
				LOGGER.error("Exception message: "+ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
	
	public String getKey(String line) {
        String key = null;
        String prefix = null;

        // Regular expression to match keys starting with $ or $$
        Pattern pattern = Pattern.compile("(\\$\\$?)(\\w+)");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            prefix = matcher.group(1);
            key = matcher.group(2);
        }
        String result = (prefix != null ? prefix : "") + key;
        return result;
	} 

	public void updateInformaticaParamTables(List<InformaticaParamTables> informaticaParamTablesList, int jobId) {
		try {
			for(InformaticaParamTables informaticaParamTables: informaticaParamTablesList) {
				String fromTable = informaticaParamTables.getFromTable();
				String toTable = informaticaParamTables.getToTable();
				String paramFileName = modifyParameterFileNameForInClause(informaticaParamTables.getParamFilename());
				
				if(paramFileName!="") {
					String updatedFromTable = getParamValue(fromTable, paramFileName, informaticaParamTables.getFileName().split("__")[0], informaticaParamTables.getFileName().split("__")[1], informaticaParamTables.getSessionName(), jobId);
					String updatedToTable = getParamValue(toTable, paramFileName, informaticaParamTables.getFileName().split("__")[0], informaticaParamTables.getFileName().split("__")[1], informaticaParamTables.getSessionName(), jobId);
					if(fromTable!=updatedFromTable || toTable!=updatedToTable) {
						informaticaLineageDao.updateInformaticaParamTables(updatedFromTable, updatedToTable, informaticaParamTables.getFileName(), jobId);	
					}
				}
			}
		} catch(Exception ex) {
			LOGGER.error("Exception occurred in updateInformaticaParamTables "+ex.getMessage());
		}
	}
	
	public String updateSqlQueryParamTables(String sqlQuery, String paramFileName, String folderName, String workflowName, String sessionName, int jobId) {
		try {
			if(paramFileName!="" && sqlQuery.contains("$")) {
				String[] wordArr = sqlQuery.split(" ");
				for(String word: wordArr) {
					if(word.replace("'", "").startsWith("$")) {
						String updatedWord = getParamValue(word, paramFileName, folderName, workflowName, sessionName, jobId);
					    if(word!=updatedWord) {
					    	sqlQuery = sqlQuery.replace(word, updatedWord);
						}
					}
				}
			}
		} catch(Exception ex) {
			LOGGER.error("Exception occurred in updateSqlQueryParamTables "+ex.getMessage());
		}
		return sqlQuery;
	}
    
    public String getParamValue(String paramName, String paramFileName, String folderName, String workflowName, String sessionName, int jobId) {
    	StringBuilder sb = new StringBuilder();
    	try {
    		String[] paramArr = paramName.split("(?=[./])|(?<=[./])");
    		for(String param: paramArr) {
    			if(param.startsWith("$")) {
    				String updatedWord = "";
    				for (int i = 1; i <= 6; i++) {
    				    updatedWord = informaticaLineageDao.getParamValue(paramFileName, folderName, workflowName, sessionName, i, param);
    				    if (!updatedWord.isEmpty()) {
    				        break;
    				    }
    				}
				    if(updatedWord.isEmpty()) {
				    	sb.append(param);
				    } else {
				    	sb.append(updatedWord);
				    }
    			} else {
    				sb.append(param);
    			}
    		}
    	} catch(Exception ex) {
			LOGGER.error("Exception occurred in getParamValue "+ex.getMessage());
		}
		return sb.toString();
    }
    
    private Map<String, String> getSessionNamesMap(Document document) {
        Map<String, String> sessionNameMap = new HashMap<>();
        NodeList sessionList = document.getElementsByTagName(GeneralConstants.SESSION_TAG);
        for(int i=0; i<sessionList.getLength(); i++) {
            Element session = (Element) sessionList.item(i);
            String mapName = session.getAttribute(GeneralConstants.MAPPINGNAME);
            String sessionName = session.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
            sessionNameMap.put(mapName, sessionName);
        }
        return sessionNameMap;
    }
    
    public void getParameterFileName(NodeList sessionNodeList, NodeList workflowNodeList, Set<String> paramFileNames) {
    	try {
    		for (int i=0;i<sessionNodeList.getLength();i++) {
				Element sessionElement = (Element) sessionNodeList.item(i);
				NodeList attributeNodeList = sessionElement.getElementsByTagName(GeneralConstants.ATTRIBUTE_TAG);
				for (int j=0;j<attributeNodeList.getLength();j++) {
					Element attributeElement = (Element) attributeNodeList.item(j);
					String name = attributeElement.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
					if(name.equalsIgnoreCase("Parameter Filename")) {
						String value = attributeElement.getAttribute(GeneralConstants.VALUE);
						String[] valueArr = value.split("[/\\\\]");
				    	paramFileNames.add(valueArr[valueArr.length-1]);
					}
				}
    		}
    		for (int i=0;i<workflowNodeList.getLength();i++) {
				Element workflowElement = (Element) workflowNodeList.item(i);
				NodeList attributeNodeList = workflowElement.getElementsByTagName(GeneralConstants.ATTRIBUTE_TAG);
				for (int j=0;j<attributeNodeList.getLength();j++) {
					Element attributeElement = (Element) attributeNodeList.item(j);
					String name = attributeElement.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
					if(name.equalsIgnoreCase("Parameter Filename")) {
						String value = attributeElement.getAttribute(GeneralConstants.VALUE);
						String[] valueArr = value.split("[/\\\\]");
				    	paramFileNames.add(valueArr[valueArr.length-1]);
					}
				}
    		}
    	} catch(Exception ex) {
    		LOGGER.error("Exception occurred in getParameterFileName "+ex.getMessage());
    	}
    }
    
    public String modifyParameterFileNameForInClause(String paramFileName) {	
        String[] parts = paramFileName.split(",", -1); 
        StringBuilder result = new StringBuilder("(");

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append("'").append(parts[i]).append("'");
        }
        result.append(")");
        return result.toString();
    }

	private void getSqlQueriesIfExistsFromInstanceTag(ComponentLevelDetails connectorDetails, NodeList instanceNodeList) {
		// below integer is added to check if it has gone through both instances of fromComponentType and toComponentType(if both are in 1 connector)
		// when a connector has fromComponentType as lookUp and toComponentType as target.
		// if we don't add this check, for loop will be broken after only one componentType is evaluated for one instance node,
		// and it won't evaluate for the other componentType instance node
		int tableAttributeInstanceCheckForFromToComponent = 0;
		List<String> sqlKeywordList =
				Arrays.asList("INSERT", "UPDATE", "DELETE", "TRUNCATE", "MERGE", "CREATE", "SELECT",
						"exec", "call", "execute", "ins", "upd", "del", "sel");
		for (int i = 0; i < instanceNodeList.getLength(); i++) {
			Element instance = (Element) instanceNodeList.item(i);
			String name = instance.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
			String tfType = instance.getAttribute(GeneralConstants.TRANSFORMATION_TYPE);
			boolean sourceQualifierAndTargetInstanceCheck =
					StringUtils.equalsIgnoreCase(name, connectorDetails.getToComponent()) &&
					(StringUtils.equalsIgnoreCase(tfType, GeneralConstants.SOURCE_QUALIFIER) ||
							StringUtils.equalsIgnoreCase(tfType, GeneralConstants.TARGET_DEFINITION));
			boolean lookupProcedureInstanceCheck =
					StringUtils.equalsIgnoreCase(name, connectorDetails.getFromComponent()) &&
					StringUtils.equalsIgnoreCase(tfType, GeneralConstants.LOOKUP_PROCEDURE);
			if (sourceQualifierAndTargetInstanceCheck || lookupProcedureInstanceCheck) {
				tableAttributeInstanceCheckForFromToComponent++;
				NodeList tableAttributes = instance.getElementsByTagName(GeneralConstants.TABLEATTRIBUTE);
				if (tableAttributes.getLength() <= 0 && tableAttributeInstanceCheckForFromToComponent == 2) {
					break;
				}
				for (int j = 0; j < tableAttributes.getLength(); j++) {
					Element tableAttribute = (Element) tableAttributes.item(j);
					String attName = tableAttribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
					if (StringUtils.equalsIgnoreCase(attName, GeneralConstants.Sql_Query) ||
							StringUtils.equalsIgnoreCase(attName, GeneralConstants.Pre_SQL) ||
							StringUtils.equalsIgnoreCase(attName, GeneralConstants.Post_SQL)) {
						connectorDetails.getPreSql().add(tableAttribute.getAttribute(GeneralConstants.VALUE));
					} else {
						String attributeValue = tableAttribute.getAttribute(GeneralConstants.VALUE);
						if (sqlKeywordList.stream().anyMatch(
								keyword -> attributeValue.toLowerCase().startsWith(keyword.toLowerCase()))) {
							connectorDetails.getPostSql().add(tableAttribute.getAttribute(GeneralConstants.VALUE));
						}
					}
				}
			}
		}
	}

	private void getAdditionalSqlQueriesForTargetAndLookUp(Document document, ComponentLevelDetails connectorDetails,
														   List<InstanceDetails> sourceComponentList,
														   List<InstanceDetails> targetComponentList) {
		InstanceDetails instanceDetailsForTarget = null;
		InstanceDetails instanceDetailsForLookUp = null;
		if (GeneralConstants.TARGET_TAG.equalsIgnoreCase(connectorDetails.getToComponentType())) {
			Optional<InstanceDetails> optInstanceDetailsForTarget = targetComponentList.stream().filter(component ->
					component.getTableName().equalsIgnoreCase(connectorDetails.getToComponent())).findAny();
			if (optInstanceDetailsForTarget.isEmpty()) {
				return;
			}
			instanceDetailsForTarget = optInstanceDetailsForTarget.get();
			getSqlQueryFromSessTransInstAttributeList(document, connectorDetails, connectorDetails.getToComponent(),
					instanceDetailsForTarget.getTransformationName(), GeneralConstants.TARGET_DEFINITION);
		} else if (GeneralConstants.LOOKUP_PROCEDURE.equalsIgnoreCase(connectorDetails.getFromComponentType())) {
			Optional<InstanceDetails> optInstanceDetailsForLookUp = sourceComponentList.stream().filter(component ->
					component.getTableName().equalsIgnoreCase(connectorDetails.getFromComponent())).findAny();
			if (optInstanceDetailsForLookUp.isEmpty()) {
				return;
			}
			instanceDetailsForLookUp = optInstanceDetailsForLookUp.get();
			getSqlQueryFromSessTransInstAttributeList(document, connectorDetails, connectorDetails.getFromComponent(),
					instanceDetailsForLookUp.getTransformationName(), GeneralConstants.LOOKUP_PROCEDURE);
		}
	}

	private void getSqlQueryFromSessTransInstAttributeList(Document document, ComponentLevelDetails connectorDetails,
														   String instanceName, String transformationName, String transFormationType) {
		String sqlQuery = "", preSql = "", postSql = "";
		NodeList sessionList = document.getElementsByTagName(GeneralConstants.SESSION_TAG);
		for(int i=0; i<sessionList.getLength(); i++) {
			Element session = (Element) sessionList.item(i);
			String mapName = session.getAttribute(GeneralConstants.MAPPINGNAME);
			if(StringUtils.equalsIgnoreCase(mapName, connectorDetails.getMappingName())) {
				NodeList sessTransInstList = session.getElementsByTagName(GeneralConstants.SESSTRANSFORMATIONINST);

				for(int j=0; j<sessTransInstList.getLength(); j++) {
					Element sessTrans = (Element) sessTransInstList.item(j);
					String sInstanceName = sessTrans.getAttribute(GeneralConstants.SINSTANCENAME);
					String tfName = sessTrans.getAttribute(GeneralConstants.TRANSFORMATIONNAME);
					String tfType = sessTrans.getAttribute(GeneralConstants.TRANSFORMATIONTYPE);

					if (((StringUtils.equalsIgnoreCase(sInstanceName, instanceName) ||
							StringUtils.equalsIgnoreCase(tfName, transformationName)) &&
							StringUtils.equalsIgnoreCase(tfType, transFormationType))) {
						NodeList attributeList = sessTrans.getElementsByTagName(GeneralConstants.ATTRIBUTE_TAG);
						LOGGER.info("sInstanceName size: {}", attributeList.getLength());
						for (int k=0; k < attributeList.getLength(); k++) {
							Element attribute = (Element) attributeList.item(k);
							String name = attribute.getAttribute(GeneralConstants.NAME_ATTRIBUTE);
							if (StringUtils.equalsIgnoreCase(name, GeneralConstants.Sql_Query)) {
								connectorDetails.getPreSql().add(attribute.getAttribute(GeneralConstants.VALUE));
							}
							if (StringUtils.equalsIgnoreCase(name, GeneralConstants.Pre_SQL)) {
								connectorDetails.getPreSql().add(attribute.getAttribute(GeneralConstants.VALUE));
							}
							if (StringUtils.equalsIgnoreCase(name, GeneralConstants.Post_SQL)) {
								connectorDetails.getPostSql().add(attribute.getAttribute(GeneralConstants.VALUE));
							}
						}
					}
				}
			}
		}
	}
}