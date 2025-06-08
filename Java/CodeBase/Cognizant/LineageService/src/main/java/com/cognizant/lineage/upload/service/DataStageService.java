package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.dao.DataStageDAO;
import com.cognizant.lineage.upload.model.Datastaging;
import com.cognizant.lineage.upload.model.ScriptCalculationDetails;
import com.cognizant.lineage.upload.model.SourceAndTarget;
import com.cognizant.lineage.util.CommonUtil;
import com.cognizant.lineage.util.Sanitization;

@Service
public class DataStageService {

	//private static Logger LOGGER = LoggerFactory.getLogger(DataStageService.class);
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${dataStageScriptLocation}")
	private String dataStageScriptLocation;
	
	@Value("${dataStageScriptName}")
	private String dataStageScriptName;
	
	@Value("${commonScriptLocation}")
	private String networkXScriptLocation;
	
	@Value("${bteqPythonScriptName}")
	private String networkXScriptName;
	
	@Value("${datastageFileUploadLocation}")
	private String datastageFileUploadLocation;
	
	@Autowired
	DataStageDAO dataStageDAO;

	@Autowired
	ScriptLineageService scriptLineageService;

	static int functionUsedCountVal = 0;static int joinCountVal = 0;static int selectCountVal = 0;
	
	public void insertIntoLineageJob(int jobId, String projectName, Logger LOGGER) {
		try {
			LOGGER.info("Step1: Updating lineage_job with ui provided inputs");
			LOGGER.info("..........JobId: "+jobId);
			dataStageDAO.insertIntoLineageJob(jobId, projectName, "datastage", "datastage", "U", datastageFileUploadLocation+ File.separator + jobId, LOGGER);
			LOGGER.info("Step1: lineage_job updated successfully");
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in insertIntoLineageJob Service "+ex.getMessage());
		}
	}
	
	public void uploadFilesToServer(MultipartFile[] files, int jobId, Logger LOGGER) {
		try {
			LOGGER.info("Step2: Uploading dataStage Xml Files to Server");
			LOGGER.info("..........FileUploadLocation: "+datastageFileUploadLocation+ File.separator + jobId);
			File fileUploadDirectory = new File(datastageFileUploadLocation + File.separator + jobId);
			CommonUtil.uploadAllScriptsToInputLocation(files, fileUploadDirectory.getAbsolutePath());
			LOGGER.info("Step2: dataStage Xml Files uploaded to Server Successfully");
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in uploadFilesToServer Service "+ex.getMessage());
		}
	}

	//Parsing: Part1
	@Async
	public void parseDataStageXmls(int jobId, String projectName, String logFileName, Logger LOGGER) {
		HashSet<String> jobNameList = new HashSet<>();
		int stepNo = 1;
		try {

			int exitCodeForScriptLineage = scriptLineageService.invokeScriptLineageCleansing((long) jobId,
					datastageFileUploadLocation + File.separator + jobId,
					TechnologyConstants.DATASTAGE, TechnologyConstants.DATASTAGE);
			if (exitCodeForScriptLineage != 0) {
				LOGGER.info("Script Lineage exited with exit code: " + exitCodeForScriptLineage);
				return;
			}

			LOGGER.info("Step3: Parsing DataStageXmls one by one");
			File location = new File(datastageFileUploadLocation+ File.separator + jobId);
			File filesArr[] = location.listFiles();
			dataStageDAO.insertIntoLineageJobStatus(jobId, stepNo, "DataStageXmlParsing", filesArr.length, 0, "Processing", logFileName, LOGGER);
			
			Map<String,Map<String, String>> realFileNamesMap = new HashMap<>();
			Map<String,Map<String, String>> tableNamesForHashReplacementMap = new HashMap<>();
			for (File f : filesArr) {	
				captureRealFileNamesAndHashReplacementTableNames(f, realFileNamesMap, tableNamesForHashReplacementMap, LOGGER);
			}
				
			int counter = 1;
		    for (File f : filesArr) {	
				LOGGER.info("..........parsing file "+counter+" of "+filesArr.length);
				dataStageDAO.updateLineageJobStatus(counter, "Processing", jobId, stepNo, LOGGER);
				captureRawDataFromDataStageXmls(f, jobId, realFileNamesMap, jobNameList, tableNamesForHashReplacementMap, LOGGER);
				counter++;
			}
		    dataStageDAO.updateDataStage(jobId, LOGGER);
		    dataStageDAO.updateLineageJobStatusForErrorOrCompleted("Completed", jobId, stepNo, LOGGER);
		    LOGGER.info("Step3: DataStage all Xml's parsed successfully");
		    
		    backtrackAndCaptureSourceTarget(jobId, logFileName, LOGGER);
			callingPythonScriptForLineageSteps(jobId, projectName, LOGGER);
			calculateComplexity(jobId, projectName, jobNameList, logFileName, LOGGER);
			LOGGER.info("end....................................................................");		
		} catch(Exception ex) {
			dataStageDAO.updateLineageJobStatusForErrorOrCompleted("Exception: "+ex.getMessage(), jobId, stepNo, LOGGER);
			LOGGER.info("Exception occurred in parseDataStageXmls Service "+ex.getMessage());
		}
	} 
	
	public void captureRealFileNamesAndHashReplacementTableNames(File f, Map<String,Map<String, String>> realFileNamesMap, Map<String,Map<String, String>> tableNamesForHashReplacementMap, Logger LOGGER) throws IOException {	
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setValidating(false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder db = dbFactory.newDocumentBuilder();
			Document doc = db.parse(f);
            doc.getDocumentElement().normalize();	
            
			NodeList dsExportNodeList = doc.getElementsByTagName("DSExport");
			System.out.println("Inside DSExport.......");
			
			for (int i = 0; i < dsExportNodeList.getLength(); i++) {
				System.out.println("Inside " + (i + 1) + " dsExport.......");
				Element dsExportElement = (Element) dsExportNodeList.item(i);
				NodeList jobNodeList = dsExportElement.getElementsByTagName("Job");
				
				for (int j = 0; j < jobNodeList.getLength(); j++) {
					System.out.println("Inside " + (j + 1) + " Job.......");
					Element jobElement = (Element) jobNodeList.item(j);
					String jobName = jobElement.getAttribute("Identifier");
					Map<String, String> tableNamesForHashReplacement = new HashMap<>();
					Map<String, String> realFileNames = new HashMap<>();

					NodeList recordNodeList = jobElement.getElementsByTagName("Record");
					for (int k = 0; k < recordNodeList.getLength(); k++) {		
						System.out.println("Inside " + (k + 1) + " Record.......");
						Element recordElement = (Element) recordNodeList.item(k);
						String recordType = recordElement.getAttribute("Type");
						
						//for capturing real name of table that are starting with #
						if (recordType.equalsIgnoreCase("JobDefn")) {
							String identifier = recordElement.getAttribute("Identifier");
							NodeList collectionNodeList = recordElement.getElementsByTagName("Collection");
							int counter = 0;
							for (int m = 0; m < collectionNodeList.getLength(); m++) {
								System.out.println("Inside " + (counter + 1) + " Collection.......");

								Element collectionElement = (Element) collectionNodeList.item(m);
								NodeList subRecordNodeList = collectionElement.getElementsByTagName("SubRecord");
								for (int n = 0; n < subRecordNodeList.getLength(); n++) {
									System.out.println("Inside " + (n + 1) + " SubRecord.......");
									String nameTag="";
									String defaultTag="";

									Element subRecordElement = (Element) subRecordNodeList.item(n);
									NodeList propertyNodeList = subRecordElement.getElementsByTagName("Property");
									for (int o = 0; o < propertyNodeList.getLength(); o++) {
										System.out.println("Inside " + (o + 1) + " Property.......");

										Element propertyElement = (Element) propertyNodeList.item(o);
										String type = propertyElement.getAttribute("Name");
										String value = propertyElement.getTextContent();
										if (type.equalsIgnoreCase("Name")) {
											nameTag = value;
										} else if (type.equalsIgnoreCase("Default")) {
											defaultTag = value;
										}
									}
									if(!nameTag.isEmpty() && !defaultTag.isEmpty()) {
										tableNamesForHashReplacement.put(nameTag, defaultTag);
									}
								}
							}
						//for capturing real name of file
						} else if(!(recordType.equalsIgnoreCase("JobDefn")) && !(recordType.equalsIgnoreCase("ContainerView"))) {
							String identifier = recordElement.getAttribute("Identifier");
							NodeList collectionNodeList = recordElement.getElementsByTagName("Collection");
							int counter = 0;
							for (int m = 0; m < collectionNodeList.getLength(); m++) {
								System.out.println("Inside " + (counter + 1) + " Collection.......");

								Element collectionElement = (Element) collectionNodeList.item(m);
								NodeList subRecordNodeList = collectionElement.getElementsByTagName("SubRecord");
								for (int n = 0; n < subRecordNodeList.getLength(); n++) {
									System.out.println("Inside " + (n + 1) + " SubRecord.......");
									boolean isValueOfFileOrDataSet = false;
									String valueTagForFileOrDataSet = "";

									Element subRecordElement = (Element) subRecordNodeList.item(n);
									NodeList propertyNodeList = subRecordElement.getElementsByTagName("Property");
									for (int o = 0; o < propertyNodeList.getLength(); o++) {
										System.out.println("Inside " + (o + 1) + " Property.......");

										Element propertyElement = (Element) propertyNodeList.item(o);
										String type = propertyElement.getAttribute("Name");
										String value = propertyElement.getTextContent();
										if (type.equalsIgnoreCase("Name") && (value.startsWith("file") || value.startsWith("File") || value.startsWith("dataset") || value.startsWith("DataSet"))) {
											isValueOfFileOrDataSet = true;
										} else if (type.equalsIgnoreCase("Value") && isValueOfFileOrDataSet == true && value.contains(".")) {
											String filename[] = value.replace("\\", "/").split("[.]");
											String filenameArr1[] = filename[0].split("/");
											String filenameArr2[] = filename[1].split("/");
											valueTagForFileOrDataSet = filenameArr1[filenameArr1.length - 1] + "." + filenameArr2[0];
											isValueOfFileOrDataSet = false;
										}
									}
									if (!valueTagForFileOrDataSet.isEmpty()) {
										realFileNames.put(identifier, valueTagForFileOrDataSet);
									}
								}
							}
						}
					}	
					tableNamesForHashReplacementMap.put(jobName, tableNamesForHashReplacement);
					realFileNamesMap.put(jobName, realFileNames);
				}				
			}
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in captureRealFileNamesAndHashReplacementTableNames Service "+ex.getMessage());
		}
	}
	
	public void captureRawDataFromDataStageXmls(File f, int jobId, Map<String,Map<String, String>> realFileNamesMap, HashSet<String> jobNameList, Map<String,Map<String, String>> tableNamesForHashReplacementMap, Logger LOGGER) throws IOException {		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setValidating(false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder db = dbFactory.newDocumentBuilder();
			Document doc = db.parse(f);
            doc.getDocumentElement().normalize();	
            
			NodeList dsExportNodeList = doc.getElementsByTagName("DSExport");
			System.out.println("Inside DSExport.......");
			
			for (int i = 0; i < dsExportNodeList.getLength(); i++) {
				System.out.println("Inside " + (i + 1) + " dsExport.......");
				Element dsExportElement = (Element) dsExportNodeList.item(i);
				NodeList jobNodeList = dsExportElement.getElementsByTagName("Job");
				
				for (int j = 0; j < jobNodeList.getLength(); j++) {
					System.out.println("Inside " + (j + 1) + " Job.......");
					Element jobElement = (Element) jobNodeList.item(j);
					String jobName = jobElement.getAttribute("Identifier");
					Map<String, String> realFileNames = realFileNamesMap.get(jobName);
					Map<String, String> tableNamesForHashReplacement = tableNamesForHashReplacementMap.get(jobName);
					
					NodeList recordNodeList = jobElement.getElementsByTagName("Record");
					for (int k = 0; k < recordNodeList.getLength(); k++) {		
						System.out.println("Inside " + (k + 1) + " Record.......");
						Element recordElement = (Element) recordNodeList.item(k);										
						String recordType = recordElement.getAttribute("Type");
						
						if(!(recordType.equalsIgnoreCase("JobDefn")) && !(recordType.equalsIgnoreCase("ContainerView"))) {
							
							String identifier = recordElement.getAttribute("Identifier");
							NodeList propertyNodeList = recordElement.getElementsByTagName("Property");
							String item = "", inputPin = "", outputPin = "", partner = "", tablenameOrQuery = "", lookup = "";

							//for capturing all details of source/target tablename
							int counter=0;
							for (int m = 0; m < propertyNodeList.getLength(); m++) {
								Element propertyElement = (Element) propertyNodeList.item(m);
								String parentNodeName = propertyElement.getParentNode().getNodeName();
								if(parentNodeName.equals("Record")) {
									System.out.println("Inside " + (counter + 1) + " Property of Record.......");
									counter++;

									String name = propertyElement.getAttribute("Name");
									if (name.equalsIgnoreCase("Name")) {
										item = propertyElement.getTextContent();
									} else if (name.equalsIgnoreCase("InputPins")) {
										inputPin = propertyElement.getTextContent();
									} else if (name.equalsIgnoreCase("OutputPins")) {
										outputPin = propertyElement.getTextContent();
									} else if (name.equalsIgnoreCase("Partner")) {
										partner = propertyElement.getTextContent();
									} else if (name.equalsIgnoreCase("StageType")) {
										if(propertyElement.getTextContent().contains("Lookup")) {
											lookup = "Y";
										}
									}
								}
							};

							//for capturing real (source/target tablename or query)........not applicable for intermediate tables
							NodeList collectionNodeList = recordElement.getElementsByTagName("Collection");
							for (int m = 0; m < collectionNodeList.getLength(); m++) {
								System.out.println("Inside " + (counter + 1) + " Collection.......");

								Element collectionElement = (Element) collectionNodeList.item(m);
								NodeList subRecordNodeList = collectionElement.getElementsByTagName("SubRecord");
								for (int n = 0; n < subRecordNodeList.getLength(); n++) {
									System.out.println("Inside " + (n + 1) + " SubRecord.......");

									Element subRecordElement = (Element) subRecordNodeList.item(n);
									NodeList propertyNodeListt= subRecordElement.getElementsByTagName("Property");
									boolean isValueOfXMLProperty = false;
									for (int o = 0; o < propertyNodeListt.getLength(); o++) {
										System.out.println("Inside " + (o + 1) + " Property.......");

										Element propertyElement = (Element) propertyNodeListt.item(o);
										String type = propertyElement.getAttribute("Name");
										String value = propertyElement.getTextContent();
										if(type.equalsIgnoreCase("Name") && value.equalsIgnoreCase("XMLProperties")) {
											isValueOfXMLProperty = true;
										} else if(type.equalsIgnoreCase("Value") && isValueOfXMLProperty==true) {
											tablenameOrQuery = parseString(value, LOGGER);
										}
									}
								}
							}	
							if(((inputPin.equals("") && !(outputPin.equals(""))) || !(inputPin.equals("")) && outputPin.equals("")) && tablenameOrQuery.equals("")) {
								if(inputPin.equals("")) {
									if(outputPin.contains("|")) {
										tablenameOrQuery = realFileNames.get(outputPin.split("|")[0]);
										if(tablenameOrQuery == null) {
											tablenameOrQuery = realFileNames.get(outputPin.split("|")[1]);
										}
									} else {
										tablenameOrQuery = realFileNames.get(outputPin);
									}
								} else {
									if(inputPin.contains("|")) {
										tablenameOrQuery = realFileNames.get(inputPin.split("|")[0]);
										if(tablenameOrQuery == null) {
											tablenameOrQuery = realFileNames.get(inputPin.split("|")[1]);
										}
									} else {
										tablenameOrQuery = realFileNames.get(inputPin);
									}
								}
							}
							jobNameList.add(jobName+"__"+f.getName().replace(".xml", "").replace(" ", "_"));
							replaceHashWithCorrectTableName(jobId, item, tablenameOrQuery, identifier, inputPin, outputPin, partner, lookup, (jobName+"__"+f.getName().replace(".xml", "").replace(" ", "_")), tableNamesForHashReplacement, LOGGER);	
						}
					}					
				}				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occurred in captureRawDataFromDataStageXmls Service "+ex.getMessage());
		}
	}
	
	public void replaceHashWithCorrectTableName(int jobId, String item, String tablenameOrQuery, String identifier, String inputPin, String outputPin, String partner, String lookup, String jobName, Map<String,String> tableNamesForHashReplacement, Logger LOGGER) {
		List<String> replaceWordsList = new ArrayList<>();
		boolean isQueryEmpty = false;
		try {
			StringBuilder tableName = new StringBuilder();
			boolean hashStart = false;
			if(tablenameOrQuery == null || tablenameOrQuery.equals("")) {
				isQueryEmpty = true;
				for(int i=0;i<item.length();i++) {
					if(hashStart) {
						tableName.append(item.charAt(i));
					}
					if(item.charAt(i) == '#' && hashStart == false) {
						hashStart = true;
					} else if(item.charAt(i) == '#' && hashStart == true) {
						tableName.deleteCharAt(tableName.length()-1);
						replaceWordsList.add(tableName.toString());
						tableName = new StringBuilder();
					}
				}
			} else {
				for(int i=0;i<tablenameOrQuery.length();i++) {
					if(hashStart) {
						tableName.append(tablenameOrQuery.charAt(i));
					}
					if(tablenameOrQuery.charAt(i) == '#' && hashStart == false) {
						hashStart = true;
					} else if(tablenameOrQuery.charAt(i) == '#' && hashStart == true) {
						tableName.deleteCharAt(tableName.length()-1);
						replaceWordsList.add(tableName.toString());
						tableName = new StringBuilder();
					}
				}
			}
			for(String word: replaceWordsList) {
				if(tableNamesForHashReplacement.containsKey(word)) {
					String table =  tableNamesForHashReplacement.get(word);
					if(isQueryEmpty) {
						item = item.replace("#"+word+"#", table);
					} else {
						tablenameOrQuery = tablenameOrQuery.replace("#"+word+"#", table);
					}
				} 
			}
			dataStageDAO.insertIntoDataStageComponentLevelLineage(jobId, item, tablenameOrQuery, identifier, inputPin, outputPin, partner, lookup, jobName, LOGGER);						
		} catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occurred in replaceHashWithCorrectTableName Service "+ex.getMessage());
		}
	}

	public String parseString(String str, Logger LOGGER) {
		String tableNameOrQuery = "";
		try {
			str = str.replace("TableName", " TableName ")
					.replace("Filename", " Filename ")
					.replace("Select", " Select ")
					.replace("CDATA", " CDATA ")
			        .replace("[", " [ ")
			        .replace("]", " ] ");
			String strArr[] = str.split(" ");
			
			boolean foundKeyWords =false;
			boolean cdataFound = false;
			boolean cdataOpenBracket = false;
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<strArr.length;i++) {
				if(foundKeyWords) {
					if(strArr[i].equalsIgnoreCase("CDATA")) {
						cdataFound = true;	
					}
					if(cdataFound && cdataOpenBracket == true) {
						if(strArr[i].equals("]")) {
							tableNameOrQuery = sb.toString().trim();
							break;
						} else {
							sb.append(strArr[i] +" ");
						}
					}
					if(cdataFound && strArr[i].equals("[")) {
						cdataOpenBracket = true;
					}
				} else {
				    if(strArr[i].equalsIgnoreCase("TableName") || strArr[i].equalsIgnoreCase("Filename") || strArr[i].equalsIgnoreCase("Select")) {
				    	foundKeyWords = true;
				    }
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occurred in parseString Service "+ex.getMessage());
		}
		return tableNameOrQuery;
	}
	
	//Parsing: Part2
	public void backtrackAndCaptureSourceTarget(int jobId, String logFileName, Logger LOGGER) {
		HashSet<SourceAndTarget>  listObj =  new LinkedHashSet<SourceAndTarget>();
		List<Datastaging> datastageTargetDetailsList = new ArrayList<Datastaging>();
		int stepNo = dataStageDAO.getStepNo(jobId, LOGGER);
		try {
			LOGGER.info("Step4: Capturing source and target tablenames from dataStage raw data");
			datastageTargetDetailsList = dataStageDAO.getDataStageComponentLevelLineageDetailsForTarget(jobId, LOGGER);
			dataStageDAO.insertIntoLineageJobStatus(jobId, stepNo, "SourceTargetIdentification", datastageTargetDetailsList.size(), 0, "Processing", logFileName, LOGGER);

			int counter = 1;
			for (Datastaging targetObj : datastageTargetDetailsList) {
				dataStageDAO.updateLineageJobStatus(counter, "Processing", jobId, stepNo, LOGGER);counter++;

				List<Datastaging> datastagingDetailsList = new ArrayList<Datastaging>();
				datastagingDetailsList = dataStageDAO.getDataStageComponentLevelLineageDetails(jobId, targetObj.getJobName(), LOGGER);
				String inputPin = targetObj.getInputPin();
				recursiveMethodDatastaging(inputPin, datastagingDetailsList, targetObj.getItem(), targetObj.getJobName(), listObj, LOGGER);
			}
			List<SourceAndTarget> sourceTargetlist = new ArrayList<SourceAndTarget>(listObj);
			if (sourceTargetlist.size() > 0) {
				dataStageDAO.insertIntoDataStageTableLevelLineage(jobId,sourceTargetlist, LOGGER);
			}
			dataStageDAO.updateLineageJobStatusForErrorOrCompleted("Completed", jobId, stepNo, LOGGER);
			LOGGER.info("Step4: source and target tablenames captured successfully");
		} catch (Exception ex) {
			dataStageDAO.updateLineageJobStatusForErrorOrCompleted("Exception: "+ex.getMessage(), jobId, stepNo, LOGGER);
			LOGGER.info("Exception occurred in recursive method outside :" + ex.getMessage());
		}
	}

	private void recursiveMethodDatastaging(String inputpin, List<Datastaging> datastagingDetailsList, String target, String jobName, HashSet<SourceAndTarget>  listObj, Logger LOGGER) {
		try {
			for (Datastaging datastageObj : datastagingDetailsList) {
				if (datastageObj.getIdentifier().trim().equalsIgnoreCase(inputpin.trim())) {
					if(datastageObj.getLookup().equalsIgnoreCase("Y")) {
						SourceAndTarget sourceAndTarget = new SourceAndTarget();
						String source = datastageObj.getItem();
						String finalTarget = target;
						sourceAndTarget.setSource(source);
						sourceAndTarget.setTarget(finalTarget);
						sourceAndTarget.setJobName(jobName);
						listObj.add(sourceAndTarget);
					}
					if (datastageObj.getParnter() != null && datastageObj.getParnter() != "" && datastageObj.getParnter().isBlank() == false) {
						String[] identifiersplit = datastageObj.getParnter().split("\\|");
						String newInputPin = identifiersplit[0];
						recursiveMethodDatastaging(newInputPin, datastagingDetailsList, target, jobName, listObj, LOGGER);
					}
					if (datastageObj.getInputPin().isBlank() == false && datastageObj.getInputPin() != null && datastageObj.getOutputPin() != null && datastageObj.getOutputPin().isBlank() == false) {
						recursiveMethodDatastaging(datastageObj.getInputPin(), datastagingDetailsList, target, jobName, listObj, LOGGER);
					} else if (datastageObj.getInputPin().isBlank() == true && datastageObj.getOutputPin() != null && datastageObj.getOutputPin().isBlank() == false) {
						SourceAndTarget sourceAndTarget = new SourceAndTarget();
						String source = datastageObj.getItem();
						String finalTarget = target;
						sourceAndTarget.setSource(source);
						sourceAndTarget.setTarget(finalTarget);
						sourceAndTarget.setJobName(jobName);
						listObj.add(sourceAndTarget);
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in recursiveMethodDatastaging Service " + ex.getMessage());
		}
	}

	//Python script calling
	public void callingPythonScriptForLineageSteps(int jobId, String projectName, Logger LOGGER) {
		try {
			int yCount = dataStageDAO.getCountFromDataStageTableLevelLineage("Y", jobId, LOGGER);
			int nCount = dataStageDAO.getCountFromDataStageTableLevelLineage("N", jobId, LOGGER);
			LOGGER.info("Step5: Calling python script based on 'Y' count in datastage_table_level_lineage.....Y count: "
					+ yCount + ".....N count: " + nCount);
			if (yCount == 0 && nCount > 0) {
				LOGGER.info("..........Step 5.1: executing only NetworkXPythonScript using below command");
				executeNetworkXPythonScript(String.valueOf(jobId), projectName, "dstage", LOGGER);
			} else if (yCount > 0 && nCount == 0) {
				LOGGER.info("..........Step 5.1: executing newly developed DataStagePythonScript using below command");
				executeNewlyDevelopedDataStagePythonScript(String.valueOf(jobId), projectName, "datastage", LOGGER);
			} else {
				LOGGER.info("..........Step 5.1: executing newly developed DataStagePythonScript using below command");
				executeNewlyDevelopedDataStagePythonScript(String.valueOf(jobId), projectName, "datastage", LOGGER);
				LOGGER.info("..........Step 5.3: executing NetworkXPythonScript using below command");
				executeNetworkXPythonScript(String.valueOf(jobId), projectName, "dstage", LOGGER);
			}
			LOGGER.info("Step5: python scripts executed successfully");
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in callingPythonScriptForLineageSteps Service " + ex.getMessage());
		}
	}
	
	public void executeNetworkXPythonScript(String jobId, String projectName, String technology, Logger LOGGER) {
		try {		
			projectName = Sanitization.sanitizeInput(projectName);
			technology = Sanitization.sanitizeInput(technology);
			
			ProcessBuilder processBuilder = new ProcessBuilder("python3.9", networkXScriptLocation + "/" + networkXScriptName, csrfToken, jobId, projectName, technology);
			LOGGER.info("..........Command: python3.9 "+ networkXScriptLocation + "/" + networkXScriptName +" "+csrfToken +" "+jobId +" "+projectName+" "+technology);

			Process process = processBuilder.start();
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("..........NetworkXPython script executed successfully");
			} else {
				LOGGER.info("..........NetworkXPython script failed to execute with exitCode " + exitCode);
			}
		} catch (IOException ex1) {
			LOGGER.info("Exception occurred in executeNetworkXPythonScript Service " + ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("Exception occurred in executeNetworkXPythonScript Service: "+ ex2.getMessage());
			Thread.currentThread().interrupt();
		}
	}
	
	public void executeNewlyDevelopedDataStagePythonScript(String jobId, String projectName, String technology, Logger LOGGER) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("bash", dataStageScriptLocation + "/" + dataStageScriptName, logFileLocation, csrfToken, technology, jobId);
			LOGGER.info("..........Command: bash "+ dataStageScriptLocation + "/" + dataStageScriptName +" "+logFileLocation +" "+csrfToken +" "+technology+" "+jobId);

			Process process = processBuilder.start();
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("..........DataStagePython script executed successfully");
				LOGGER.info("..........Step 5.2: executing NetworkXPythonScript script using below command");
				executeNetworkXPythonScript(jobId, projectName, technology, LOGGER);
			} else {
				LOGGER.info("..........DataStagePython script failed to execute with exitCode " + exitCode);
			}
		} catch (IOException ex1) {
			LOGGER.info("Exception occurred in executeNewlyDevelopedDataStagePythonScript Service " + ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("Exception occurred in executeNewlyDevelopedDataStagePythonScript Service: "+ ex2.getMessage());
			Thread.currentThread().interrupt();
		}
	}
	
	//Complexity calculation
	public void calculateComplexity(int jobId, String projectName, HashSet<String> jobNamesList, String logFileName, Logger LOGGER) {
		int stepNo = dataStageDAO.getStepNo(jobId, LOGGER);
		try {
			LOGGER.info("Step 6: Started calculating complexity for all jobNames");
			String status = dataStageDAO.getLineageJobStatus(jobId, stepNo-1, LOGGER);
			
			List<ScriptCalculationDetails> scriptCalculationDetailsList = dataStageDAO.getScriptCalculationDetails(LOGGER);
			for(String jobName: jobNamesList) {
				functionUsedCountVal = 0; joinCountVal = 0; selectCountVal = 0;
				
				List<String> queryList = new ArrayList<>();
				String complexity = "";
				
				int transformationCount = 0;
				
				int yCount = dataStageDAO.getCountFromDataStageTableLevelLineageJobNameSpecific("Y", jobId, jobName, LOGGER);
				int nCount = dataStageDAO.getCountFromDataStageTableLevelLineageJobNameSpecific("N", jobId, jobName, LOGGER);
				if(yCount==0 && nCount>0) {                         //only tags
					transformationCount = dataStageDAO.getTransformationCount(jobId, jobName, LOGGER);
					complexity = calculateComplexityForTags(scriptCalculationDetailsList, transformationCount, LOGGER);
				} else if(yCount>0 && nCount==0) {                  //only query
					queryList = dataStageDAO.getQueryIfTypeIsYes(jobId, jobName, LOGGER);
					complexity = calculateComplexityForQuery(scriptCalculationDetailsList, queryList, LOGGER);
				} else if(yCount>0 && nCount>0) {                   //both tags and query
					queryList = dataStageDAO.getQueryIfTypeIsYes(jobId, jobName, LOGGER);
					transformationCount = dataStageDAO.getTransformationCount(jobId, jobName, LOGGER);
					String queryComplexity = calculateComplexityForQuery(scriptCalculationDetailsList, queryList, LOGGER);
					String tagComplexity = calculateComplexityForTags(scriptCalculationDetailsList, transformationCount, LOGGER);
					complexity = calculateComplexityForBothQueryAndTags(queryComplexity, tagComplexity, LOGGER);
				} 
				//insert into table
				dataStageDAO.insertIntoScriptComplexity(jobId, projectName, "DATASTAGE", jobName, functionUsedCountVal, joinCountVal, selectCountVal, transformationCount, complexity, "Success", LOGGER);
			}
			
			//status update
			String[] valArr = status.split(",");
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<valArr.length;i++) {
				if(i==valArr.length-1) {
					sb.append("Complexity calculation completed"+",");  
					sb.append(valArr[i]);
					break;
				}
				sb.append(valArr[i]+",");
			}
			dataStageDAO.updateLineageJobStatus(sb.toString(), jobId, stepNo-1, LOGGER);
			LOGGER.info("Step 6: Complexity for all jobNames calculated successfully");
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in calculateComplexity Service " + ex.getMessage());
		}
	}
	
	public String calculateComplexityForTags(List<ScriptCalculationDetails> scriptCalculationDetailsList, int transformationCount, Logger LOGGER) {
		String complexity = "";
		try {
			int simpleTransformationCount = 0;
			int mediumTransformationCount = 0;
			int complexTransformationCount = 0;
			int veryComplexTransformationCount = 0;
			for(int i=0;i<scriptCalculationDetailsList.size();i++) {
				if(i==0) {
					simpleTransformationCount = scriptCalculationDetailsList.get(i).getTransformationCount();
				} else if(i==1) {
					mediumTransformationCount = scriptCalculationDetailsList.get(i).getTransformationCount();
				} else if(i==2) {
					complexTransformationCount = scriptCalculationDetailsList.get(i).getTransformationCount();
				} else if(i==3) {
					veryComplexTransformationCount = scriptCalculationDetailsList.get(i).getTransformationCount();
				}
			}
			if(transformationCount>=veryComplexTransformationCount) {
				complexity = "Very Complex";
			} else if(transformationCount>=complexTransformationCount) {
				complexity = "Complex";
			} else if(transformationCount>=mediumTransformationCount) {
				complexity = "Medium";
			} else if(transformationCount>=simpleTransformationCount) {
				complexity = "Simple";
			}
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in calculateComplexityForTags Service " + ex.getMessage());
		}
		return complexity;
	}
	
	public String calculateComplexityForQuery(List<ScriptCalculationDetails> scriptCalculationDetailsList, List<String> queryList, Logger LOGGER) {
		String complexity = "";
		try {
			List<String> funList = dataStageDAO.getFunctionList(LOGGER);
			
			int simpleFunctionCount = 0, simpleJoinCount=0, simpleSelectCount=0;
			int mediumFunctionCount = 0, mediumJoinCount=0, mediumSelectCount=0;
			int complexFunctionCount = 0, complexJoinCount=0, complexSelectCount=0;
			int veryComplexFunctionCount = 0, veryComplexJoinCount=0, veryComplexSelectCount=0;
			for(int i=0;i<scriptCalculationDetailsList.size();i++) {
				if(i==0) {
					simpleFunctionCount = scriptCalculationDetailsList.get(i).getFunctionCount();
					simpleJoinCount = scriptCalculationDetailsList.get(i).getJoinCount();
					simpleSelectCount = scriptCalculationDetailsList.get(i).getSelectCount();
				} else if(i==1) {
					mediumFunctionCount = scriptCalculationDetailsList.get(i).getFunctionCount();
					mediumJoinCount = scriptCalculationDetailsList.get(i).getJoinCount();
					mediumSelectCount = scriptCalculationDetailsList.get(i).getSelectCount();
				} else if(i==2) {
					complexFunctionCount = scriptCalculationDetailsList.get(i).getFunctionCount();
					complexJoinCount = scriptCalculationDetailsList.get(i).getJoinCount();
					complexSelectCount = scriptCalculationDetailsList.get(i).getSelectCount();
				} else if(i==3) {
					veryComplexFunctionCount = scriptCalculationDetailsList.get(i).getFunctionCount();
					veryComplexJoinCount = scriptCalculationDetailsList.get(i).getJoinCount();
					veryComplexSelectCount = scriptCalculationDetailsList.get(i).getSelectCount();
				}
			}
			
			//calculating different counts from query
			Map<String, Integer> countMap = new HashMap<>(); 
	    	countMap.put("FUNCTION_USED_COUNT", 0);
	    	countMap.put("SELECT_COUNT", 0);
	    	countMap.put("JOIN_COUNT", 0);
			String joinCount = "FULL OUTER JOIN,RIGHT OUTER JOIN,RIGHT JOIN,LEFT OUTER JOIN,LEFT JOIN,INNER JOIN,CROSS JOIN";
			for (String sqlQuery : queryList) {
				
				//function-count
				for (String arrSplit : funList) {
					String patternStr = "(\\(|,\\s*)" + arrSplit.toLowerCase().trim() + "\\s*\\(.*?\\)"
							+ "(\\s*\\))?" + "|" + arrSplit.trim().toLowerCase() + "\\s*" + "\\(.*?\\)";
					Pattern p = Pattern.compile(patternStr);
					Matcher m = p.matcher(sqlQuery.toLowerCase());
					while (m.find()) {
						if (m.start() - 1 >= 0) {
							char values = sqlQuery.charAt(m.start() - 1);
							if (sqlQuery.charAt(m.start()) == '(' || sqlQuery.charAt(m.start()) == ','
									|| sqlQuery.charAt(m.start()) == ' ' || values == ' ' || values == '\n'
									|| values == '=' && values != '_') {
								int functionUsedCount = countMap.getOrDefault("FUNCTION_USED_COUNT", 0);
								countMap.put("FUNCTION_USED_COUNT", ++functionUsedCount);
							}
						} else if (m.start() == 0) {
							int functionUsedCount = countMap.getOrDefault("FUNCTION_USED_COUNT", 0);
							countMap.put("FUNCTION_USED_COUNT", ++functionUsedCount);
						}
					}
				}
				
				//select count
				if (sqlQuery.toLowerCase().trim().startsWith("select") && (sqlQuery.length() == "select".length() || Character.isWhitespace(sqlQuery.charAt("select".length())))) {
					String patternStrForMerge = ".*?\\bselect\\b.*?";
					Pattern patternMerge = Pattern.compile(patternStrForMerge);
					Matcher matcherMerge = patternMerge.matcher(sqlQuery.toLowerCase());
					while (matcherMerge.find()) {
						int selectCount = countMap.getOrDefault("SELECT_COUNT", 0);
						countMap.put("SELECT_COUNT", ++selectCount);
					}
				} 
				
				//join count
				String[] arrJoinSplit = joinCount.split(",");
				int joinSplitCount = 0;
				for (int j = 0; j < arrJoinSplit.length; j++) {
					Pattern p = Pattern.compile(arrJoinSplit[j].trim().toLowerCase());
					Matcher m = p.matcher(sqlQuery.toLowerCase().replaceAll("\\s{2,}", " ").trim());
					while (m.find()) {
						joinSplitCount++;
					}
				}
				int joinsCount = countMap.getOrDefault("JOIN_COUNT", 0);
				joinsCount += joinSplitCount;

				int countJoins =0;
				Pattern patternForCountingJoins = Pattern.compile(
						"\\b(?<!left\\s)(?<!right\\s)(?<!full\\souter\\s)"
								+ "(?<!left\\souter\\s)(?<!right\\souter\\s)(?<!inner\\s)(?<!cross\\s)join\\b",
								Pattern.CASE_INSENSITIVE);
				Matcher matcher = patternForCountingJoins.matcher(sqlQuery);
				while (matcher.find()) {
					countJoins++;
				}
				countMap.put("JOIN_COUNT", (joinsCount + countJoins));
			}
			
			//calculating complexity
		    functionUsedCountVal = countMap.get("FUNCTION_USED_COUNT");
		    joinCountVal = countMap.get("JOIN_COUNT");
		    selectCountVal = countMap.get("SELECT_COUNT");
		    
		    if(functionUsedCountVal >= veryComplexFunctionCount && selectCountVal >= veryComplexSelectCount && joinCountVal >= veryComplexJoinCount) {
				 complexity = "Very Complex";
			} else if(functionUsedCountVal >= complexFunctionCount && selectCountVal >= complexSelectCount && joinCountVal >= complexJoinCount) {
				 complexity = "Complex";
			} else if(functionUsedCountVal >= mediumFunctionCount && selectCountVal >= mediumSelectCount && joinCountVal >= mediumJoinCount) {
				 complexity = "Medium";
			} else if(functionUsedCountVal >= simpleFunctionCount && selectCountVal >= simpleSelectCount && joinCountVal >= simpleJoinCount) {
				 complexity = "Simple";
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in calculateComplexityForQuery Service " + ex.getMessage());
		}
		return complexity;
	}
	
	public String calculateComplexityForBothQueryAndTags(String queryComplexity, String tagComplexity, Logger LOGGER) {
		String complexity = "";
		try {
			Map<String, Integer> complexityRank = new HashMap<>();
			complexityRank.put("Simple", 1);
			complexityRank.put("Medium", 2);
			complexityRank.put("Complex", 3);
			complexityRank.put("Very Complex", 4);
			
			int queryValue = complexityRank.get(queryComplexity);
			int tagValue = complexityRank.get(tagComplexity);
			
			if(queryValue>tagValue) {
				complexity = queryComplexity;
			} else {
				complexity = tagComplexity;
			}
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in calculateComplexityForBothQueryAndTags Service " + ex.getMessage());
		}
		return complexity;
	}
}

