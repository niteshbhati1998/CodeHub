package com.cognizant.lineage.database.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cognizant.lineage.database.constants.DatabaseConstants;
import com.cognizant.lineage.database.dao.ImportScriptsDAO;
import com.cognizant.lineage.database.model.DBObject;
import com.cognizant.lineage.database.model.ScriptConfigurationDetails;
import com.cognizant.lineage.database.model.VaultDataWithAllFields;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.model.FieldBean;
import com.cognizant.lineage.upload.service.ScriptComplexity;
import com.cognizant.lineage.upload.service.TableauService;
import com.cognizant.lineage.util.Sanitization;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ImportScriptsService {
	
	@Value("${vault.url}")
	private String vaultURL;
 
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${vault.dbconnectiondetails.path}")
	private String vaultDbConnectionDetailsPath;
	
	@Value("${importScriptName}")
	private String importScriptName;
	
	@Value("${importScriptLocation}")
	private String importScriptLocation;
	
	@Value("${ODIPythonScriptName}")
	private String lineageScriptName;
	
	@Value("${ODIPythonScriptLocation}")
	private String lineageScriptLocation;
	
	@Value("${bteqPythonScriptName}")
	private String lineageScriptLastStepName;
	
	@Value("${commonScriptLocation}")
	private String lineageScriptLastStepLocation;
	
    @Autowired
    ImportScriptsDAO importScriptsDAO;
    
    @Autowired
    TableauService tableauService;

	@Autowired
	ScriptComplexity scriptComplexity;
		
    public Map<String, String> fetchServerHosts(String scriptType, Logger LOGGER) throws Exception {
		Map<String, String> hostIpDetails = new HashMap<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");

			String requestBody = "{\"path\": \"" + vaultDbConnectionDetailsPath + "/" + scriptType+"\"}";
			LOGGER.info("requestBody..............."+requestBody);
			HttpEntity<String> requestEntity = new HttpEntity<String>(requestBody, headers);
	
			URL url = new URL(vaultURL);
			if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
			    throw new Exception("Forbidden remote source");
		    }
			
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.exchange(vaultURL, HttpMethod.POST, requestEntity, String.class);
			ObjectMapper objectMapper = new ObjectMapper();
			List<VaultDataWithAllFields> list = objectMapper.readValue(response.getBody(), new TypeReference<List<VaultDataWithAllFields>>(){});
			for(VaultDataWithAllFields entry : list) {
				DBObject dbObject = entry.getValue();
				hostIpDetails.put(entry.getKey(), dbObject.getHost());
			}
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in fetchHostsFromVault Service " + ex.getMessage());
		}
		return hostIpDetails;
	}
    
	@Async
	public void executePythonImportScript(ScriptConfigurationDetails scriptConfigurationDetails, String jobId, String logFileName, Logger LOGGER) {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		try {
			String projectName = Sanitization.sanitizeInput(scriptConfigurationDetails.getProjectName());
		    String scriptName = Sanitization.sanitizeInput(scriptConfigurationDetails.getScriptName().trim());
		    String key = Sanitization.sanitizeInput(scriptConfigurationDetails.getServerAlias());
		    String serverAlias = Sanitization.sanitizeInput(vaultDbConnectionDetailsPath + "/" + scriptName) ;
		    String inputDirectory = Sanitization.sanitizeInput(scriptConfigurationDetails.getInputDirectory());
			String outputDirectory = Sanitization.sanitizeInput(scriptConfigurationDetails.getOutputDirectory() + File.separator + jobId);
		    String fileExtension = Sanitization.sanitizeInput(scriptConfigurationDetails.getFileExtension());
		    String parentTechnology = "";
		    String technology = "";
		    
		    //updating lineage_job table
			if (scriptName.contains("-")) {
				String[] tech = scriptName.split("-");
				parentTechnology = tech[0];
				technology = tech[1];
			} else if(scriptName.contains("Tableau")) {
				parentTechnology = "BI";
				technology = "Tableau";
			}
			importScriptsDAO.insertIntoLineageJob(jobId, projectName, parentTechnology, technology, "D",
					outputDirectory, LOGGER);
		    //importScriptsDAO.insertIntoLineageJob(jobId, projectName, scriptName, "", "D", outputDirectory, LOGGER);
		   
			ProcessBuilder processBuilder = new ProcessBuilder("python3.9", importScriptLocation + "/" + importScriptName, "-T", scriptName, "-r", inputDirectory, "-d", outputDirectory, "-e", fileExtension, "-j", jobId, "-k", key, "-a", serverAlias);
			Process process = processBuilder.start();
			LOGGER.info(".....command used: python3.9 " + importScriptLocation + "/" + importScriptName + " -T " + scriptName + " -r " + inputDirectory + " -d " + outputDirectory + " -e " + fileExtension + " -j " + jobId + " -k " + key+ " -a " + serverAlias);
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
		    int exitCode = process.waitFor();
		    if(exitCode==0) {
		    	LOGGER.info("Step1: python import script executed successfully");
				if (scriptName.equalsIgnoreCase("Oracle-ODI")) {
					LOGGER.info("Step2: starting parsing xml's files");
					parseOdiXml(outputDirectory, Integer.parseInt(jobId), logFileName, LOGGER);
					LOGGER.info("Step2: parsing completed for all xml files");
					
					LOGGER.info("Step3: start calling lineage script for all steps starting from cleansing");
					callPythonLineageScriptForAllSteps(jobId,  scriptConfigurationDetails, LOGGER);
					scriptComplexity.calculateScriptComplexity(projectName, Integer.parseInt(jobId), TechnologyConstants.ODI, TechnologyConstants.ORACLE);
				} else if (scriptName.equalsIgnoreCase("Tableau")) {
					LOGGER.info("Step2: calling tableau jar to start tableau file parsing and generate lineage");
					parseTableauFilesAndGenerateLineage(jobId, outputDirectory, scriptName, projectName, logFileName, LOGGER);
					LOGGER.info("Step2: tableau file parsing and generate lineage executed successfully");
				} else {
					LOGGER.info("Step2: Can't start parsing xml files as scriptName: "+scriptName+" doesn't match with the requirements");
				}
			} else {
				LOGGER.info("Step1: python import script failed to execute with exitCode "+exitCode);
			}
		    
		   //updating lineage_job table
		   Timestamp endTime = new Timestamp(System.currentTimeMillis());
		   importScriptsDAO.updateLineageJob(jobId, startTime, endTime, LOGGER);
		} catch (IOException ex1) {
			LOGGER.info("IOException occurred in executePythonImportScript Service "+ ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("InterruptedException occurred in executePythonImportScript Service "+ ex2.getMessage());
			Thread.currentThread().interrupt();
		}
	}

	private void parseTableauFilesAndGenerateLineage(String jobId, String outputDirectory, String scriptName, String projectName, String logFileName, Logger LOGGER) {
		Map<String, Object> hm = new HashMap<>();
		try {
			//updating lineage_job table
		    importScriptsDAO.insertIntoLineageJob(jobId, projectName, scriptName, "", "D", outputDirectory, LOGGER);
		    
		    hm.put("Logger", LOGGER);
			hm.put("JobId", Long.parseLong(jobId));
			hm.put("UploadedPath", outputDirectory);
			tableauService.readFilesAndParse(projectName, scriptName, hm);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in parseTableFilesAndGenerateLineage Service " + ex.getMessage());
		}
	}

	public void parseOdiXml(String outputDirectory, int jobId, String logFileName, Logger LOGGER) {
		int stepNo = importScriptsDAO.getStepNo(jobId, LOGGER);
		try {
			outputDirectory = Sanitization.sanitizeInput(outputDirectory);

			File file = new File(outputDirectory);
			File filesArr[] = file.listFiles();
			int counter=0;
			importScriptsDAO.insertIntoLineageJobStatus(jobId, stepNo, "ODI Parsing", filesArr.length, 0, "Started", logFileName, LOGGER);
			for (File f : filesArr) { 
				counter++;
				parseXML(f, jobId, LOGGER);
				LOGGER.info(".....processing for file : " +counter+" of "+filesArr.length);
				importScriptsDAO.updateLineageJobStatus(counter, "In Progress", jobId, stepNo, LOGGER);
			}
			importScriptsDAO.updateLineageJobStatusForErrorOrCompleted("Completed", jobId, stepNo, LOGGER);
		} catch(Exception ex) {
			importScriptsDAO.updateLineageJobStatusForErrorOrCompleted("Error: "+ex.getMessage(), jobId, stepNo, LOGGER);
			LOGGER.info("Exception occurred in parseOdiXml Service "+ex.getMessage());
		}
	}
	
	private void parseXML(File f, int jobId, Logger LOGGER) throws XMLStreamException {
		List<Object[]> params = new ArrayList<>();
		List<FieldBean> queryList = new ArrayList<>();
		try {
		  JAXBContext jaxbContext = JAXBContext.newInstance(FieldBean.class);          
		  XMLInputFactory xif = XMLInputFactory.newFactory();
		  xif.setProperty(GeneralConstants.XML_PROPERTY_EXTERNAL_ENTITY, false);
		  xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		  StreamSource source = new StreamSource(f.getAbsolutePath());
		  XMLStreamReader xsr = xif.createXMLStreamReader(source); 
		  Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		  
		  //parsing FieldBean object and capturing query
		  int startLineNumber = 0;
		  while (xsr.hasNext()) {
              int eventType = xsr.next();
              if (eventType == XMLStreamReader.START_ELEMENT && 
            		  DatabaseConstants.FIELD.equals(xsr.getLocalName())) {
                  FieldBean fb = unmarshaller.unmarshal(xsr, FieldBean.class).getValue();         
                  String name = fb.getName();
				  String textContent = fb.getTextContent();
				  int endLineNumber = xsr.getLocation().getLineNumber()-1;
				  if(DatabaseConstants.DEF_TXT.equalsIgnoreCase(name) && !(textContent.equalsIgnoreCase("null"))) {
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
			  String updatedQuery = formatSqlQuery(f.getName(), query, queryType, LOGGER);
			  long ms2 = System.currentTimeMillis(); 
			  Timestamp endTime = new Timestamp(ms2);
			  
			  Object[] param = {jobId, query, updatedQuery, fb.getStartLineNumber(), fb.getEndLineNumber(), queryType, startTime, endTime, f.getName()};
			  params.add(param);
		  }
		  importScriptsDAO.insertIntoOdiDetails(params, LOGGER);
		}
		catch (Exception ex) {
			LOGGER.info("Exception occurred in parseXML Service "+ ex.getMessage());
		}
	}
	
	public String formatSqlQuery(String fileName, String query, StringBuilder queryType, Logger LOGGER) {		
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
									String tableName = getTableNameWhenFirstArgumentIsL(fileName, arr, LOGGER);
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
			LOGGER.info("Exception occurred in formatSqlQuery Service "+ex.getMessage());
		}
		return updatedQuery;
	}  
		
	private String getTableNameWhenFirstArgumentIsL(String fileName, String[] arr, Logger LOGGER) {
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
			LOGGER.info("Exception occurred in getTableWhenFirstArgumentIsL Service "+ex.getMessage());
		}
		return tableName;
	}
	
	public void callPythonLineageScriptForAllSteps(String jobId, ScriptConfigurationDetails scriptConfigurationDetails, Logger LOGGER) {
		try {
			String projectName = Sanitization.sanitizeInput(scriptConfigurationDetails.getProjectName());
			String scriptName = Sanitization.sanitizeInput(scriptConfigurationDetails.getScriptName());
			
			ProcessBuilder processBuilder = new ProcessBuilder("bash", lineageScriptLocation + "/" + lineageScriptName, logFileLocation, csrfToken, scriptName, jobId);
			LOGGER.info(".....command used: bash "+ lineageScriptLocation + "/" + lineageScriptName+" "+ logFileLocation+" "+ csrfToken+" "+scriptName+" "+jobId);
			Process process = processBuilder.start();
		
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			int exitCode = process.waitFor();	
			if (exitCode == 0) {
				LOGGER.info("Step 3: python lineage script for all steps starting from cleansing executed successfully");
				LOGGER.info("Step 4: start calling python lineage script for last step ");
				callPythonLineageScriptForLastStep(jobId, projectName, scriptName, LOGGER);
			} else {
				LOGGER.info("Step 3: python lineage script for all steps starting from cleansing failed to execute...with exitCode " + exitCode);
			}
		} catch (IOException ex1) {
			LOGGER.info("IOException occurred in callPythonLineageScriptForAllSteps Service "+ ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("InterruptedException occurred in callPythonLineageScriptForAllSteps Service "+ ex2.getMessage());
			Thread.currentThread().interrupt();
		}
	}
	
	public void callPythonLineageScriptForLastStep(String jobId, String projectName, String scriptName, Logger LOGGER) {
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			scriptName = Sanitization.sanitizeInput(scriptName);
			
			ProcessBuilder processBuilder2 = new ProcessBuilder("python3.9", lineageScriptLastStepLocation + "/" + lineageScriptLastStepName, csrfToken, jobId, projectName, scriptName);
			LOGGER.info(".....command used: python3.9 " + lineageScriptLastStepLocation + "/" + lineageScriptLastStepName + " "+ csrfToken + " " + jobId + " " + projectName + " " + scriptName);
			Process process = processBuilder2.start();

			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("Step 4: python lineage script for last step executed successfully");
			} else {
				LOGGER.info("Step 4: python lineage script for last step failed with exitcode " + exitCode);
			}
		} catch (IOException ex1) {
			LOGGER.info("IOException occurred in callPythonLineageScriptForLastStep Service "+ ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("InterruptedException occurred in callPythonLineageScriptForLastStep Service "+ ex2.getMessage());
			Thread.currentThread().interrupt();
		}
	}
}
