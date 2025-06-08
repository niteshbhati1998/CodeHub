package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.cognizant.lineage.dao.entity.LineageJob;
import com.cognizant.lineage.dao.entity.LineageJobStatus;
import com.cognizant.lineage.dao.repository.LineageJobRepository;
import com.cognizant.lineage.dao.repository.LineageJobStatusRepository;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.exception.LineageRuntimeException;
import com.cognizant.lineage.parser.ParserManager;
import com.cognizant.lineage.parser.QueryParser;
import com.cognizant.lineage.parser.model.Query;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TableauQueryConstant;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.dao.TableauDAO;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.LoggerUtil;
import com.cognizant.lineage.util.Sanitization;

@Service
public class TableauService {

	@Value("${commonScriptName}")
	private String commonScriptName;
	
	@Value("${commonScriptLocation}")
	private String commonScriptLocation;
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${tableauInputFiles}")
	private String tableauUploadFileLocation;
	
	@Autowired
	private TableauDAO tableauDAO;
	
	@Autowired
	LineageJobRepository jobRepo;
	
	@Autowired
	LineageJobStatusRepository jobStatusRepo;
	
	@Autowired
	LoggerUtil loggerUtil;

	@Autowired
	ScriptComplexity scriptComplexity;

	@Autowired
	JobStatusService jobStatusService;

	@Autowired
	ScriptLineageService scriptLineageService;

	public Map<String, Object> uploadFilesToServer(MultipartFile[] files, String projectName, String tech) throws LineageBusinessException {
		Logger LOGGER;
		String logFileName = "TableauParsing";
		Map<String, Object> hm = new HashMap<>();
		try {	
			LineageJob lineageJob = saveLineage(tech, projectName);
			Long jobId = lineageJob.getJobId();
			
			String uploadedPath = tableauUploadFileLocation+"/"+String.valueOf(jobId);
		    LOGGER = loggerUtil.getUtilLogObject(logFileLocation, logFileName, String.valueOf(jobId));
			
			lineageJob.setUploadDir(uploadedPath);
			lineageJob = jobRepo.save(lineageJob);	
			CommonUtil.uploadAllScriptsToInputLocation(files, uploadedPath);

			lineageJob.setEndTime(new Date());
			lineageJob = jobRepo.save(lineageJob);
			
			hm.put("Logger", LOGGER);
			hm.put("JobId", jobId);
			hm.put("UploadedPath", uploadedPath);
		} catch (LineageBusinessException e) {
			throw e;
		} catch (Exception ex) {
			throw new LineageRuntimeException(ex.getMessage());
		}
		return hm;
	}
	
	public Long getNextSeqIdBteq(Logger LOGGER) {
		try {
			Long nextId = 0L;
			nextId = tableauDAO.getNextSequenceIdBteq(LOGGER);
			return nextId;
		}catch(Exception e) {
			return 0L;
		}
	}
	
	public LineageJob saveLineage(String tech, String projectName) {
		try {
			Long jobId = jobRepo.getJobIdByMax();
			LineageJob lineageJob = new LineageJob();
			lineageJob.setJobId(jobId);
			lineageJob.setProjectName(projectName);
			lineageJob.setTechnology(tech);
			lineageJob.setStartTime(new Date());
			lineageJob.setJobParams("BI");
			lineageJob.setUploadType("U");
			lineageJob = jobRepo.save(lineageJob);
			return lineageJob;
		} catch (Exception ex) {
			throw new LineageRuntimeException(ex.getLocalizedMessage());
		}
	}
	
	@Async
	public void readFilesAndParse(String projectName, String tech, Map<String, Object> hm) {
		Logger LOGGER = (Logger) hm.get("Logger");
		Long jobId = (Long) hm.get("JobId");
		String uploadedPath = (String) hm.get("UploadedPath");

		int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing(jobId, uploadedPath, TechnologyConstants.BI, tech);
		if (exitCodeForScriptLineage != 0) {
			LOGGER.info("Script Lineage exited with exit code: " + exitCodeForScriptLineage);
			return;
		}

		int stepNo = tableauDAO.getStepNo(jobId.intValue(), LOGGER);
		try {
			Path p = Paths.get(uploadedPath);
			String normalizedPath = Sanitization.sanitizeInput(p.normalize().toString());	
			File file = new File(normalizedPath);
			File filesArr[] = file.listFiles();
			LineageJobStatus jobStatus = new LineageJobStatus();
			Long jobStatusId = getNextSeqIdBteq(LOGGER);
			jobStatus.setId(jobStatusId);
			jobStatus.setJobId(jobId);
			jobStatus.setNoOfFileReceived(filesArr.length);
			jobStatus.setLogFileLocation(LOGGER.getName());
			jobStatus.setNoOfFileProcessed(0);
			jobStatus.setStepNo(stepNo);
			jobStatus.setStepName("Tableau XML Parsing");
			jobStatus.setStatus("In Process");
			jobStatus = jobStatusRepo.save(jobStatus);
			
			LOGGER.info("Tableau files Uploaded successfully");
			LOGGER.info("Job Id: "+String.valueOf(jobId)); 
			LOGGER.info("parsing all files one by one....");
			for (File f : filesArr) {
			    String fileName=(f.getName()).replace(".twb", "");
			    String fileNameWithExctension=f.getName();
			    if(!(fileName.contains("Model"))) {		    	
			    	parsingXMLFile(Integer.valueOf(jobId.intValue()), f, fileName, LOGGER,tech,fileNameWithExctension,projectName);
			    	jobStatus.setNoOfFileProcessed(jobStatus.getNoOfFileProcessed()+1);
			    	jobStatus = jobStatusRepo.save(jobStatus);
			    }
			}
			
			//executing PythonScript
			executePythonScript(String.valueOf(jobId), tech, projectName, LOGGER);

			//calculating script complexity
			if (tech.toLowerCase().contains(TechnologyConstants.TABLEAU.toLowerCase())) {
				tech = TechnologyConstants.BI_TABLEAU;
			}
			HashSet<Boolean> complexityBoolSet = new HashSet<>();
			for (File f : filesArr) {
				String fileName = (f.getName()).replace(".twb", "");
				if (!(GeneralConstants.MODEL.equalsIgnoreCase(fileName))) {
					boolean uploadFiles = scriptComplexity.calculateScriptComplexityForTableau(jobId, projectName,
							f.getName().toUpperCase(), tech);
					complexityBoolSet.add(uploadFiles);
				}
			}
			if (complexityBoolSet.size() == 1 && complexityBoolSet.contains(Boolean.TRUE)) {
				jobStatusService.updateJobStatusDetailsWithComplexityString(jobId);
			} else {
				LOGGER.info("Some error has occurred while calculating complexity for 1 or more files.");
			}

			jobStatus.setStatus("COMPLETED");
			jobStatusRepo.save(jobStatus);
			LOGGER.info("Tableau files parsed successfully ");
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in readFilesAndParse "+ex.getMessage());
		}
	}

	public void parsingXMLFile(int jobId, File f, String fileName, Logger LOGGER, String tech, String fileNameWithExtension, String projectName) throws IOException {	
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setValidating(false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder db = dbFactory.newDocumentBuilder();
			Document doc = db.parse(f);

			doc.getDocumentElement().normalize();	
			NodeList nodeList1 = doc.getElementsByTagName("workbook");			
			for (int i = 0; i < nodeList1.getLength(); i++) {
				//System.out.println("Inside " + (i + 1) + " workbook.......");	
				Element element1 = (Element) nodeList1.item(i);
				
				//pre-requisite for storing info
				HashMap<String,String> reportNameWithColumnNameMap = new LinkedHashMap<>();
				Set<String> columnList = new HashSet<>();
				HashMap<String,String> columnNameWithNotRealTableNameMap = new LinkedHashMap<>();
				HashMap<String,String> connectionNameWithSqlQueryMap = new LinkedHashMap<>();
				HashMap<String,String> columnNameWithConnectionNameMapIfSqlQueryPresent = new LinkedHashMap<>();
				HashMap<String,String> notRealTableNameWithRealTableNameMap = new LinkedHashMap<>();
				HashMap<String,String> realTableNameWithConnectionNameMap = new LinkedHashMap<>();
				HashMap<String,String> columnNameWithConnectionNameMapIfNotRealTableNameNotPresent = new LinkedHashMap<>();
				HashMap<String,String> connectionNameWithDbNameSchemaNameFileNameMap = new LinkedHashMap<>();
				
				//Step-1
				captureReportNameWithColumnName(element1, reportNameWithColumnNameMap, columnNameWithNotRealTableNameMap, notRealTableNameWithRealTableNameMap,  columnList, fileName, LOGGER);

				NodeList nodeList4 = element1.getElementsByTagName("datasources");	
				for (int k = 0; k < nodeList4.getLength(); k++) {		
					//System.out.println("Inside " + (k + 1) + " datasources.......");
					
					Element element4 = (Element) nodeList4.item(k);
					NodeList nodeList5 = element4.getElementsByTagName("datasource");	
					for (int l = 0; l < nodeList5.getLength(); l++) {
						//System.out.println("Inside " + (l + 1) + " datasource.......");
						
						Element element5 = (Element) nodeList5.item(l);
						NodeList nodeList6 = element5.getElementsByTagName("connection");	
						for (int m = 0; m < nodeList6.getLength(); m++) {
							//System.out.println("Inside " + (m + 1) + " connection.......");
							Element element6 = (Element) nodeList6.item(m);		
							
							//Step-2
							captureColumnNameWithNotRealTableName(element6, columnNameWithNotRealTableNameMap, columnNameWithConnectionNameMapIfNotRealTableNameNotPresent, columnList, LOGGER);
							
							//Step-3
							captureNotRealTableNameWithRealTableName(element6, notRealTableNameWithRealTableNameMap, columnNameWithNotRealTableNameMap, realTableNameWithConnectionNameMap, columnList, LOGGER); 
				
							//Step-4
							captureSqlQuery(element6, connectionNameWithSqlQueryMap, columnNameWithConnectionNameMapIfSqlQueryPresent, columnList, LOGGER);
							
							//Step-5
							captureConnectionNameWithDbNameSchemaNameFileNameMap(element6, connectionNameWithDbNameSchemaNameFileNameMap, LOGGER);
						}					   
					}
				}
				
				LOGGER.info("reportNameWithColumnNameMap..."+reportNameWithColumnNameMap.toString());
				LOGGER.info("columnNameWithNotRealTableNameMap..."+columnNameWithNotRealTableNameMap.toString());
				LOGGER.info("connectionNameWithSqlQueryMap..."+connectionNameWithSqlQueryMap.toString());
				LOGGER.info("columnNameWithConnectionNameMapIfSqlQueryPresent..."+columnNameWithConnectionNameMapIfSqlQueryPresent.toString());
				LOGGER.info("notRealTableNameWithRealTableNameMap..."+notRealTableNameWithRealTableNameMap.toString());	
				LOGGER.info("realTableNameWithConnectionNameMap..."+realTableNameWithConnectionNameMap.toString());
				LOGGER.info("columnNameWithConnectionNameMapIfNotRealTableNameNotPresent..."+columnNameWithConnectionNameMapIfNotRealTableNameNotPresent.toString());
				LOGGER.info("connectionNameWithDbNameSchemaNameFileNameMap..."+connectionNameWithDbNameSchemaNameFileNameMap.toString());
				
				//processing each sql query if present using columnlineage codebase
				Map<String,Set<String>> columnNameWithSourceTableNameMap = new HashMap<>();
				for (Map.Entry<String, String> entry : connectionNameWithSqlQueryMap.entrySet()) {	
					String[] sqlQueryArr = entry.getValue().split(";");
					for(String sqlQuery: sqlQueryArr) {			
						QueryParser queryParser = ParserManager.getParser(sqlQuery);
						queryParser.parse();
						Query query = queryParser.getQuery();

						query.getFieldsAliasMap().forEach((fieldName,selectField) -> {	
							Set<String> srcTableName = new HashSet<>();
							srcTableName.add(selectField.getActualFieldName().split("~")[0]);
							String columnName = selectField.getActualFieldName().split("~")[1];

							columnList.forEach((column)-> {
								if(column.equalsIgnoreCase(columnName)) {  
									if(columnNameWithSourceTableNameMap.containsKey(columnName)) {
										Set<String> prevSrcTableName = columnNameWithSourceTableNameMap.get(columnName);
										prevSrcTableName.addAll(srcTableName);
										columnNameWithSourceTableNameMap.put(columnName,prevSrcTableName);
									} else {
										columnNameWithSourceTableNameMap.put(columnName,srcTableName);
									}
								}
							});
						});
					}
				}	
				
				//inserting details in postgres
				reportNameWithColumnNameMap.forEach((reportName,columnNames)-> {
					reportName = TableauQueryConstant.REPORT__.concat(reportName);
					String workbookName = TableauQueryConstant.REPORT__.concat(reportName.split("__")[1]);
							
					LOGGER.info("Processing for ReportName.................................................."+reportName);
					String[] columnsArr = columnNames.split(",");
					for(String columnName: columnsArr) {
						LOGGER.info("columnName: "+columnName);
						if(columnNameWithNotRealTableNameMap.get(columnName)!=null) {
							String[] notRealTableNamesArr = columnNameWithNotRealTableNameMap.get(columnName).split(",");
							for(String notRealTableName: notRealTableNamesArr) {
								String realTableName = "";
								if(notRealTableNameWithRealTableNameMap.get(notRealTableName)!=null) {
									realTableName = notRealTableNameWithRealTableNameMap.get(notRealTableName);
							        String tableName = "", connectionName="", schemaName="";
							        if(realTableNameWithConnectionNameMap.get(realTableName)!=null) {
							        	connectionName = realTableNameWithConnectionNameMap.get(realTableName);
							        	if(connectionNameWithDbNameSchemaNameFileNameMap.get(connectionName)!=null) {
							        		try {
							        		    schemaName = connectionNameWithDbNameSchemaNameFileNameMap.get(connectionName).split(",")[1];
							        		} catch(Exception ex) {
							        			schemaName = connectionNameWithDbNameSchemaNameFileNameMap.get(connectionName).split(",")[0];
							        		}
							        	}
							        }
							        if(!schemaName.isEmpty()) {
							        	if(realTableName.split("[.]").length==3) {
							        		tableName = realTableName.split("[.]")[2];
							        	} else if(realTableName.split("[.]").length==2) {
							        		tableName = realTableName.split("[.]")[1];
							        	} else {
							        		tableName = realTableName.split("[.]")[0];
							        	}
							        	realTableName = schemaName +"."+ tableName;
							        }
								}
								tableauDAO.insertIntoTableauParsing(jobId, projectName, reportName, workbookName, columnName, notRealTableName, realTableName, fileNameWithExtension, tech, "COMPLETED", LOGGER);
							}
						} else {
							boolean isSourceFile = false;
							if(columnNameWithConnectionNameMapIfNotRealTableNameNotPresent.get(columnName)!=null) {
								String connectionName = columnNameWithConnectionNameMapIfNotRealTableNameNotPresent.get(columnName);
								if(connectionNameWithDbNameSchemaNameFileNameMap.get(connectionName)!=null) {
									String fileNameVal = connectionNameWithDbNameSchemaNameFileNameMap.get(connectionName).split(",")[2];
									tableauDAO.insertIntoTableauParsing(jobId, projectName, reportName, workbookName, columnName, "", fileNameVal, fileNameWithExtension, tech, "COMPLETED", LOGGER);
									isSourceFile = true;
								} 
							} 
							if(isSourceFile==false) {
								boolean isColumnAvailableInSqlQuery = false;
								
								Set<String> sourceTableList = columnNameWithSourceTableNameMap.get(columnName.toUpperCase());
								if(sourceTableList!=null) {
									for(String sourceTable: sourceTableList) {
										LOGGER.info("sourceTable.."+sourceTable+"....columnName..."+columnName);
										String tableName = "";
										if(columnNameWithConnectionNameMapIfSqlQueryPresent.get(columnName)!=null) {
											String connectionName = columnNameWithConnectionNameMapIfSqlQueryPresent.get(columnName);
											if(sourceTable.contains(".")) {
												String schemaName = sourceTable.split("[.]")[0];
												if(connectionNameWithDbNameSchemaNameFileNameMap.get(connectionName)!=null) {
													try {
														schemaName = connectionNameWithDbNameSchemaNameFileNameMap.get(connectionName).split(",")[1];
													} catch(Exception ex) {
														schemaName = connectionNameWithDbNameSchemaNameFileNameMap.get(connectionName).split(",")[0];
													}
												}
												tableName = schemaName + "." + sourceTable.split("[.]")[1];
												tableauDAO.insertIntoTableauParsing(jobId, projectName, reportName, workbookName, columnName, "", tableName, fileNameWithExtension, tech, "COMPLETED", LOGGER);
											} else {
												tableauDAO.insertIntoTableauParsing(jobId, projectName, reportName, workbookName, columnName, "", sourceTable, fileNameWithExtension, tech, "COMPLETED", LOGGER);
											}
										} else {
											tableauDAO.insertIntoTableauParsing(jobId, projectName, reportName, workbookName, columnName, "", sourceTable, fileNameWithExtension, tech, "COMPLETED", LOGGER);
										}
									}
									isColumnAvailableInSqlQuery = true;
								}
								
			                    if(isColumnAvailableInSqlQuery==false) {
									tableauDAO.insertIntoTableauParsing(jobId, projectName, reportName, workbookName, columnName, "", "", fileNameWithExtension, tech, "COMPLETED", LOGGER);
								}
							}
						}			
					}
				});
			 }
		} catch (Exception ex) {
			LOGGER.info("Exception occured in parsingXMLFile "+ex);
			ex.printStackTrace();
		}
	}
	
	public void captureConnectionNameWithDbNameSchemaNameFileNameMap(Element element6, HashMap<String,String> connectionNameWithDbNameSchemaNameFileNameMap, Logger LOGGER) {
		try {
			NodeList nodeList8 = element6.getElementsByTagName("named-connections");	
			for (int p = 0; p < nodeList8.getLength(); p++) {
				Element element7 = (Element) nodeList8.item(p);
				NodeList nodeList9 = element7.getElementsByTagName("named-connection");	
				for (int q = 0; q < nodeList9.getLength(); q++) {
					Element element8 = (Element) nodeList9.item(q);

					if(element8.hasAttribute("name")) {
						String connectionName = element8.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");

						NodeList nodeList10 = element8.getElementsByTagName("connection");
						for (int r = 0; r < nodeList10.getLength(); r++) {
							Element element9 = (Element) nodeList10.item(r);
							String dbName = "", schemaName = "", fileNameVal = "";

							if(element9.hasAttribute("dbname")) {
								dbName = element9.getAttribute("dbname").replaceAll("\'","").replaceAll("\\[|\\]", "");
							}
							if(element9.hasAttribute("schema")) {
								schemaName = element9.getAttribute("schema").replaceAll("\'","").replaceAll("\\[|\\]", "");
							}
							if(element9.hasAttribute("filename")) {
								fileNameVal = element9.getAttribute("filename").replaceAll("\'","").replaceAll("\\[|\\]", "");
							}
							connectionNameWithDbNameSchemaNameFileNameMap.put(connectionName, dbName+","+schemaName+","+fileNameVal);
						}
					}
				}
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in captureConnectionNameWithDbNameSchemaNameFileNameMap "+ex.getMessage());
		}
	}
	
	public void captureNotRealTableNameWithRealTableName(Element element6, HashMap<String,String> notRealTableNameWithRealTableNameMap, HashMap<String,String> columnNameWithNotRealTableNameMap, HashMap<String,String> realTableNameWithConnectionNameMap,Set<String> columnList, Logger LOGGER) {
		try {
			NodeList nodeList7 = element6.getElementsByTagName("_.fcp.ObjectModelEncapsulateLegacy.false...relation");
			for (int o = 0; o < nodeList7.getLength(); o++) {
				Element element7 = (Element) nodeList7.item(o);

				NodeList nodeList8 = element7.getElementsByTagName("relation");
				for (int p = 0; p < nodeList8.getLength(); p++) {
					Element element8 = (Element) nodeList8.item(p);

					if(element8.hasAttribute("connection") && element8.hasAttribute("name") && element8.hasAttribute("table")) {
						String connectionName = element8.getAttribute("connection").replaceAll("\'","").replaceAll("\\[|\\]", "");
						String name = element8.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");
						String tableName = element8.getAttribute("table").replaceAll("\'","").replaceAll("\\[|\\]", "");

						columnNameWithNotRealTableNameMap.forEach((columnName,notRealTableNames)-> {
							String[] notRealTableNameArr =  notRealTableNames.split(",");
							for(String notRealTableName: notRealTableNameArr) {
								if(notRealTableName.equalsIgnoreCase(name)) {  
									notRealTableNameWithRealTableNameMap.put(notRealTableName, tableName);	
									
									//real tablename with connectionname...we can use this for finding dbname/schemaname
									realTableNameWithConnectionNameMap.put(tableName, connectionName);
								}
							}
						});
					}
				}
			}
			
			NodeList nodeList77 = element6.getElementsByTagName("_.fcp.ObjectModelEncapsulateLegacy.true...relation");
			for (int o = 0; o < nodeList77.getLength(); o++) {
				Element element7 = (Element) nodeList77.item(o);

				NodeList nodeList8 = element7.getElementsByTagName("relation");
				for (int p = 0; p < nodeList8.getLength(); p++) {
					Element element8 = (Element) nodeList8.item(p);

					if(element8.hasAttribute("connection") && element8.hasAttribute("name") && element8.hasAttribute("table")) {
						String connectionName = element8.getAttribute("connection").replaceAll("\'","").replaceAll("\\[|\\]", "");
						String name = element8.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");
						String tableName = element8.getAttribute("table").replaceAll("\'","").replaceAll("\\[|\\]", "");

						columnNameWithNotRealTableNameMap.forEach((columnName,notRealTableNames)-> {
							String[] notRealTableNameArr =  notRealTableNames.split(",");
							for(String notRealTableName: notRealTableNameArr) {
								if(notRealTableName.equalsIgnoreCase(name)) {  
									notRealTableNameWithRealTableNameMap.put(notRealTableName, tableName);	
									
									//real tablename with connectionname...we can use this for finding dbname/schemaname
									realTableNameWithConnectionNameMap.put(tableName, connectionName);
								}
							}
						});
					}
				}
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in captureNotRealTableNameWithRealTableName "+ex.getMessage());
		}
	}
	
	public void captureSqlQuery(Element element6, HashMap<String,String> connectionNameWithSqlQueryMap, HashMap<String,String> columnNameWithConnectionNameMapIfSqlQueryPresent, Set<String> columnList, Logger LOGGER) {
		try {
			NodeList nodeList7 = element6.getElementsByTagName("_.fcp.ObjectModelEncapsulateLegacy.false...relation");
			for (int o = 0; o < nodeList7.getLength(); o++) {
				Element element7 = (Element) nodeList7.item(o);

				if(element7.hasAttribute("connection") && element7.hasAttribute("name")) {
					String connectionName = element7.getAttribute("connection").replaceAll("\'","").replaceAll("\\[|\\]", "");
					String name = element7.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");
					if(name.contains("Custom SQL Query")) {
						String query = element7.getTextContent().replaceAll("\'","").replaceAll("\\[|\\]", "").replaceAll("\\;", "");
						if(connectionNameWithSqlQueryMap.containsKey(connectionName)) {
							String prevSqlQuery = connectionNameWithSqlQueryMap.get(connectionName);
							connectionNameWithSqlQueryMap.put(connectionName, prevSqlQuery +";"+ query);
						} else {
							connectionNameWithSqlQueryMap.put(connectionName, query);
						}	
						
						//column-name with connectionname...once we get source tablename from query...we can use this for finding dbname/schemaname
						columnList.forEach((column)-> {
							if(query.contains(column)) {  
								if(!(columnNameWithConnectionNameMapIfSqlQueryPresent.containsKey(column))) {
									columnNameWithConnectionNameMapIfSqlQueryPresent.put(column, connectionName);
								}
							}
						});
					}
				}
				
				NodeList nodeList8 = element7.getElementsByTagName("relation");
				for (int p = 0; p < nodeList8.getLength(); p++) {
					Element element8 = (Element) nodeList8.item(p);
					
					if(element8.hasAttribute("connection") && element8.hasAttribute("name") && !(element8.hasAttribute("table"))) {
						String connectionName = element8.getAttribute("connection").replaceAll("\'","").replaceAll("\\[|\\]", "");
						String name = element8.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");
						if(name.contains("Custom SQL Query")) {
							String query = element8.getTextContent().replaceAll("\'","").replaceAll("\\[|\\]", "").replaceAll("\\;", "");
							if(connectionNameWithSqlQueryMap.containsKey(connectionName)) {
								String prevSqlQuery = connectionNameWithSqlQueryMap.get(connectionName);
								connectionNameWithSqlQueryMap.put(connectionName, prevSqlQuery +";"+ query);
							} else {
								connectionNameWithSqlQueryMap.put(connectionName, query);
							}
							
							//column-name with connectionname...once we get source tablename from query...we can use this for finding dbname/schemaname
							columnList.forEach((column)-> {
								if(query.contains(column)) {  
									if(!(columnNameWithConnectionNameMapIfSqlQueryPresent.containsKey(column))) {
										columnNameWithConnectionNameMapIfSqlQueryPresent.put(column, connectionName);
									}
								}
							});
						}
					}
				}
			}
			
			NodeList nodeList77 = element6.getElementsByTagName("_.fcp.ObjectModelEncapsulateLegacy.true...relation");
			for (int o = 0; o < nodeList77.getLength(); o++) {
				Element element7 = (Element) nodeList77.item(o);

				if(element7.hasAttribute("connection") && element7.hasAttribute("name")) {
					String connectionName = element7.getAttribute("connection").replaceAll("\'","").replaceAll("\\[|\\]", "");
					String name = element7.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");
					if(name.contains("Custom SQL Query")) {
						String query = element7.getTextContent().replaceAll("\'","").replaceAll("\\[|\\]", "").replaceAll("\\;", "");
						if(connectionNameWithSqlQueryMap.containsKey(connectionName)) {
							String prevSqlQuery = connectionNameWithSqlQueryMap.get(connectionName);
							connectionNameWithSqlQueryMap.put(connectionName, prevSqlQuery +";"+ query);
						} else {
							connectionNameWithSqlQueryMap.put(connectionName, query);
						}	
						
						//column-name with connectionname...once we get source tablename from query...we can use this for finding dbname/schemaname
						columnList.forEach((column)-> {
							if(query.contains(column)) {  
								if(!(columnNameWithConnectionNameMapIfSqlQueryPresent.containsKey(column))) {
									columnNameWithConnectionNameMapIfSqlQueryPresent.put(column, connectionName);
								}
							}
						});
					}
				}
				
				NodeList nodeList8 = element7.getElementsByTagName("relation");
				for (int p = 0; p < nodeList8.getLength(); p++) {
					Element element8 = (Element) nodeList8.item(p);
					
					if(element8.hasAttribute("connection") && element8.hasAttribute("name") && !(element8.hasAttribute("table"))) {
						String connectionName = element8.getAttribute("connection").replaceAll("\'","").replaceAll("\\[|\\]", "");
						String name = element8.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");
						if(name.contains("Custom SQL Query")) {
							String query = element8.getTextContent().replaceAll("\'","").replaceAll("\\[|\\]", "").replaceAll("\\;", "");
							if(connectionNameWithSqlQueryMap.containsKey(connectionName)) {
								String prevSqlQuery = connectionNameWithSqlQueryMap.get(connectionName);
								connectionNameWithSqlQueryMap.put(connectionName, prevSqlQuery +";"+ query);
							} else {
								connectionNameWithSqlQueryMap.put(connectionName, query);
							}
							
							//column-name with connectionname...once we get source tablename from query...we can use this for finding dbname/schemaname
							columnList.forEach((column)-> {
								if(query.contains(column)) {  
									if(!(columnNameWithConnectionNameMapIfSqlQueryPresent.containsKey(column))) {
										columnNameWithConnectionNameMapIfSqlQueryPresent.put(column, connectionName);
									}
								}
							});
						}
					}
				}
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in captureSqlQuery "+ex.getMessage());
		}
	}
	
	public void captureColumnNameWithNotRealTableName(Element element6, HashMap<String,String> columnNameWithNotRealTableNameMap, HashMap<String,String> columnNameWithConnectionNameMapIfNotRealTableNameNotPresent, Set<String> columnList, Logger LOGGER) {
		try {
			NodeList nodeList7 = element6.getElementsByTagName("cols");	
			for (int n = 0; n < nodeList7.getLength(); n++) {
				//System.out.println("Inside " + (n + 1) + " cols.......");
				
				Element element7 = (Element) nodeList7.item(n);
				NodeList nodeList8 = element7.getElementsByTagName("map");	
				for (int o = 0; o < nodeList8.getLength(); o++) {
					//System.out.println("Inside " + (o + 1) + " map.......");
					
					Element element8 = (Element) nodeList8.item(o);
					if(element8.hasAttribute("key") && element8.hasAttribute("value")) {
						String columnName = element8.getAttribute("key").replaceAll("\'","").replaceAll("\\[|\\]", "");
						String notRealTableName =  element8.getAttribute("value").replaceAll("\'","").replaceAll("\\[|\\]", "").split("[.]")[0];
						
						columnList.forEach((column)-> {
							if(column.equalsIgnoreCase(columnName)) {  
								if(columnNameWithNotRealTableNameMap.containsKey(columnName)) {
									String prevNotRealTableName = columnNameWithNotRealTableNameMap.get(columnName);
									columnNameWithNotRealTableNameMap.put(columnName, prevNotRealTableName+","+notRealTableName);
								} else {
									columnNameWithNotRealTableNameMap.put(columnName, notRealTableName);
								}
							}
						});
					}
				}
			}
			
			NodeList nodeList8 = element6.getElementsByTagName("_.fcp.ObjectModelEncapsulateLegacy.false...relation");
			for (int o = 0; o < nodeList8.getLength(); o++) {
				Element element7 = (Element) nodeList8.item(o);
				
				NodeList nodeList9 = element7.getElementsByTagName("relation");
				for (int p = 0; p < nodeList9.getLength(); p++) {
					Element element8 = (Element) nodeList9.item(p);

					if(element8.hasAttribute("connection")) {
						String connectionName = element8.getAttribute("connection").replaceAll("\'","").replaceAll("\\[|\\]", "");

						NodeList nodeList10 = element8.getElementsByTagName("columns");
						for (int q = 0; q < nodeList10.getLength(); q++) {
							Element element9 = (Element) nodeList10.item(q);
							
							NodeList nodeList11 = element9.getElementsByTagName("column");
							for (int r = 0; r < nodeList11.getLength(); r++) {
								Element element10 = (Element) nodeList11.item(r);
								
								if(element10.hasAttribute("name")) {
									String columnName = element10.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");
									columnList.forEach((column)-> {
										if(column.equalsIgnoreCase(columnName)) {
											if(!(columnNameWithConnectionNameMapIfNotRealTableNameNotPresent.containsKey(columnName))) {
												columnNameWithConnectionNameMapIfNotRealTableNameNotPresent.put(columnName, connectionName);	
											}
										}
									});
								}
							}
						}
					}
				} 
			}
			
			NodeList nodeList88 = element6.getElementsByTagName("_.fcp.ObjectModelEncapsulateLegacy.true...relation");
			for (int o = 0; o < nodeList88.getLength(); o++) {
				Element element7 = (Element) nodeList88.item(o);
				
				NodeList nodeList9 = element7.getElementsByTagName("relation");
				for (int p = 0; p < nodeList9.getLength(); p++) {
					Element element8 = (Element) nodeList9.item(p);

					if(element8.hasAttribute("connection")) {
						String connectionName = element8.getAttribute("connection").replaceAll("\'","").replaceAll("\\[|\\]", "");

						NodeList nodeList10 = element8.getElementsByTagName("columns");
						for (int q = 0; q < nodeList10.getLength(); q++) {
							Element element9 = (Element) nodeList10.item(q);
							
							NodeList nodeList11 = element9.getElementsByTagName("column");
							for (int r = 0; r < nodeList11.getLength(); r++) {
								Element element10 = (Element) nodeList11.item(r);
								
								if(element10.hasAttribute("name")) {
									String columnName = element10.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");
									columnList.forEach((column)-> {
										if(column.equalsIgnoreCase(columnName)) {
											if(!(columnNameWithConnectionNameMapIfNotRealTableNameNotPresent.containsKey(columnName))) {
												columnNameWithConnectionNameMapIfNotRealTableNameNotPresent.put(columnName, connectionName);	
											}
										}
									});
								}
							}
						}
					}
				} 
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occured in captureColumnNameWithNotRealTableName "+ex.getMessage());
		}
	}
	
    public void captureReportNameWithColumnName(Element element1, HashMap<String,String> reportNameWithColumnNameMap, HashMap<String,String> columnNameWithNotRealTableNameMap, HashMap<String,String> notRealTableNameWithRealTableNameMap, Set<String> columnList, String fileName, Logger LOGGER) {
		try {
			NodeList nodeList2 = element1.getElementsByTagName("worksheets");
			for (int j = 0; j < nodeList2.getLength(); j++) {	
				//System.out.println("Inside " + (j + 1) + " worksheets.......");
				
				Element element3 = (Element) nodeList2.item(j);
				NodeList nodeList4 = element3.getElementsByTagName("worksheet");
				String worksheetName, runTagContent = "";
				
				for (int k = 0; k < nodeList4.getLength(); k++) {		
					//System.out.println("Inside " + (k + 1) + " worksheet.......");	
					
					Element element4 = (Element) nodeList4.item(k);
					worksheetName = ""; runTagContent = ""; 

					//worksheetName
					worksheetName = element4.getAttribute("name");

					//runtag info 
					NodeList nodeList5 = element4.getElementsByTagName("layout-options");	
					for (int l = 0; l < nodeList5.getLength(); l++) {
						//System.out.println("Inside " + (l + 1) + " layout-options.......");
						
						Element element5 = (Element) nodeList5.item(l);
						NodeList nodeList6 = element5.getElementsByTagName("title");	
						for (int m = 0; m < nodeList6.getLength(); m++) {
							//System.out.println("Inside " + (m + 1) + " title.......");
							
							Element element6 = (Element) nodeList6.item(m);
							NodeList nodeList7 = element6.getElementsByTagName("formatted-text");	
							for (int n = 0; n < nodeList7.getLength(); n++) {
								//System.out.println("Inside " + (n + 1) + " formatted-text.......");
								
								Element element7 = (Element) nodeList7.item(n);
								NodeList nodeList8 = element7.getElementsByTagName("run");	
								for (int o = 0; o < nodeList8.getLength(); o++) {
									//System.out.println("Inside " + (o + 1) + " run.......");
									
									Element element8 = (Element) nodeList8.item(o);
									runTagContent = element8.getTextContent();								
								}   
							}
						}
					}
					
					//reportName
					String reportName = "";
					if(runTagContent.equals("")) {
						reportName = (fileName+"__"+worksheetName)
								.replaceAll(" ","_")
								.replaceAll("\'","")
								.replaceAll("\\[|\\]", "");
					} else {
						reportName = (fileName+"__"+worksheetName+"__"+runTagContent)
								.replaceAll(" ","_")
								.replaceAll("\'","")
								.replaceAll("\\[|\\]", "");
					}

					//column info
					NodeList nodeList6 = element4.getElementsByTagName("table");	
					for (int l = 0; l < nodeList6.getLength(); l++) {
						//System.out.println("Inside " + (l + 1) + " table.......");
						
						Element element5 = (Element) nodeList6.item(l);
						NodeList nodeList7 = element5.getElementsByTagName("view");	
						for (int m = 0; m < nodeList7.getLength(); m++) {
							//System.out.println("Inside " + (m + 1) + " view.......");
							
							Element element6 = (Element) nodeList7.item(m);
							NodeList nodeList8 = element6.getElementsByTagName("datasource-dependencies");	
							for (int n = 0; n < nodeList8.getLength(); n++) {
								//System.out.println("Inside " + (n + 1) + " datasource-dependencies.......");
								
								Element element7 = (Element) nodeList8.item(n);
								NodeList nodeList9 = element7.getElementsByTagName("column");	
								for (int o = 0; o < nodeList9.getLength(); o++) {
									
									Element element8 = (Element) nodeList9.item(o);
									if(element8.hasAttribute("name")) {                                    //element8.hasAttribute("caption")
										//System.out.println("Inside " + (o + 1) + " column.......");
										
										String columnName = element8.getAttribute("name").replaceAll("\'","").replaceAll("\\[|\\]", "");	
										if(reportNameWithColumnNameMap.containsKey(reportName)) {
											String prevColumns = reportNameWithColumnNameMap.get(reportName);
											reportNameWithColumnNameMap.put(reportName, prevColumns+","+columnName);
											columnList.add(columnName);
										} else {
											reportNameWithColumnNameMap.put(reportName, columnName);
											columnList.add(columnName);
										}		    
									} 
								}
									
								NodeList nodeList99 = element7.getElementsByTagName("column-instance");	
								for (int o = 0; o < nodeList99.getLength(); o++) {
									
									Element element8 = (Element) nodeList99.item(o);
									if(element8.hasAttribute("name") && element8.hasAttribute("derivation") && element8.hasAttribute("column")) {                               
										//System.out.println("Inside " + (o + 1) + " column-instance.......");
										
										String derivation = element8.getAttribute("derivation").replaceAll("\'","").replaceAll("\\[|\\]", "");
										if(derivation.equals("Count")) {   
											
											//parsing the text to capture columnName/tableName
											String columnName = "";
											String text = element8.getAttribute("name");
											String[] textArr = text.split(":");
											for(int i=0;i<textArr.length;i++) {
												if(textArr[i].equalsIgnoreCase("[cnt")) {
													columnName = textArr[i+1].trim();
													break;
												}
											}
											
											String columnAttribute = element8.getAttribute("column").replaceAll("\'","").replaceAll("\\[|\\]", "");												       
											if(reportNameWithColumnNameMap.containsKey(reportName)) {
												String prevColumns = reportNameWithColumnNameMap.get(reportName);
												reportNameWithColumnNameMap.put(reportName, prevColumns+","+columnName);
												columnList.add(columnName);
											} else {
												reportNameWithColumnNameMap.put(reportName, columnName);
												columnList.add(columnName);
											}	
											
											if(!(columnName.equalsIgnoreCase(columnAttribute))) {
												columnNameWithNotRealTableNameMap.put(columnName, columnName);
												notRealTableNameWithRealTableNameMap.put(columnName, columnName);
											}
										}
									} 
								}
							}
						}
					}								
				}				
			}
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in captureReportNameWithColumnName " + ex);
		}
	}

	public void executePythonScript(String bteqExecutionId, String technology, String projectName, Logger LOGGER) {
		try {
			technology = Sanitization.sanitizeInput(technology);
			projectName = Sanitization.sanitizeInput(projectName);

			LOGGER.info("Tableu python processing start . . .");
			ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", commonScriptLocation + "/" + commonScriptName,
					csrfToken, bteqExecutionId, projectName, technology);
			LOGGER.info("python3.9 " + commonScriptLocation + "/" + commonScriptName + " " +
					csrfToken + " " + bteqExecutionId + " " + projectName + " " + technology);
			Process process = processBuilder2.start();

			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			LOGGER.info("Tableau python script execution output . . . " + output.toString());

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("Tableau python script executed successfully");
				scriptLineageService.invokeScriptLineageIdentification(Long.valueOf(bteqExecutionId), projectName);
			} else {
				LOGGER.info("Error in executing Tableau python script with exit code: " + exitCode);
			}
			LOGGER.info("Tableau python script processing end . . .");
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in executePythonScript " + ex.getMessage());
		}
	}
}
