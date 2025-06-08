package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.database.model.CommonResponse;
import com.cognizant.lineage.upload.dao.ModuleLineageDAO;
import com.cognizant.lineage.upload.model.ModuleInfo;
import com.cognizant.lineage.upload.model.ModuleLineageStatus;
import com.cognizant.lineage.upload.model.ModuleLineageUi;
import com.cognizant.lineage.upload.model.ModuleSchema;
import com.cognizant.lineage.upload.model.NodeInfo;
import com.cognizant.lineage.util.Sanitization;

@Service
public class ModuleLineageService {
	
	@Value("${csrfToken}")
	private String csrfToken;
	
	@Value("${logFileLocation}")
	private String logFileLocation;
	
	@Value("${moduleLineageScriptLocation}")
	private String moduleLineageScriptLocation;
	
	@Autowired
	ModuleLineageDAO moduleLineageDAO;
	
	public ModuleLineageUi getSchemaAndModuleList(String projectName, Logger LOGGER) {
		ModuleLineageUi moduleLineageUi = new ModuleLineageUi();
		List<String> schemaList = new ArrayList<>();
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			
			LOGGER.info("Fetching nodeInfo from presentation.nodes and generating schemaList");
			List<NodeInfo> nodeInfoList = moduleLineageDAO.getNodeInfo(projectName, LOGGER);
			for(NodeInfo nodeInfo: nodeInfoList) {
				String nodeType = nodeInfo.getNodeType();
				String node = nodeInfo.getNodeName();
				if(nodeType.equalsIgnoreCase("File")) {
					if(!(schemaList.contains(node))) {
						schemaList.add(node);
					}
				} else {
					if(!(schemaList.contains(node.split("[.]")[0]))) {
						schemaList.add(node.split("[.]")[0]);
					}
				}
			}
			moduleLineageUi.setSchemaList(schemaList);
			LOGGER.info("schemaList generated successfully");
			
			LOGGER.info("Fetching moduleList from semantic.project_modules");
			List<ModuleSchema> moduleSchemaList = moduleLineageDAO.getModuleSchemaList(projectName, LOGGER);
			
			int counter = 0;
			List<ModuleInfo> moduleInfoList = new ArrayList<>();
			for(ModuleSchema moduleSchema: moduleSchemaList) {
				ModuleInfo moduleInfo = new ModuleInfo();
				moduleInfo.setTitle(moduleSchema.getModule());
				moduleInfo.setId(String.valueOf(counter++));

				List<ModuleInfo> moduleInfoListChild = new ArrayList<>();
				if(moduleSchema.getSchemas().trim().length()>0) {
					String[] schemaArr = moduleSchema.getSchemas().split(",");
					for(String schema: schemaArr) {
						ModuleInfo moduleInfoChild = new ModuleInfo();
						moduleInfoChild.setTitle(schema);
						moduleInfoChild.setId(String.valueOf(counter++));
						moduleInfoChild.setChildNodes(new ArrayList<>());
						moduleInfoListChild.add(moduleInfoChild);
					}
				}
				moduleInfo.setChildNodes(moduleInfoListChild);
				moduleInfoList.add(moduleInfo);
			}
			moduleLineageUi.setModuleList(moduleInfoList);
			LOGGER.info("moduleList fetched successfully");
		} catch(Exception ex) {
			LOGGER.info("Exception occured in getSchemaAndModuleList Service "+ex.getMessage());
		}
		return moduleLineageUi;
	}
	
	public void addModuleInfo(String projectName, String moduleName, Logger LOGGER) {
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			moduleName = Sanitization.sanitizeInput(moduleName);
			
			LOGGER.info("adding moduleInfo in semantic.project_modules");
			moduleLineageDAO.saveModuleInfo(projectName, moduleName, LOGGER);
			LOGGER.info("moduleInfo added successfully");
		} catch(Exception ex) {
			LOGGER.info("Exception occured in addModuleInfo Service "+ex.getMessage());
		}
	}

	public void executePythonScript(String projectName, Map<String,List<String>> map, Logger LOGGER) {
		try {		
			//deleting records from module schema mapper according to project modules
			moduleLineageDAO.deleteFromModuleSchemaMapperAccordingToProjectModules(projectName, LOGGER);
			
			//inserting new module details
			moduleLineageDAO.deleteExistingProjectDetailsFromModuleLineageStatus(projectName, LOGGER);
			map.forEach((k,v)-> {
				HashSet<String> hs = new HashSet<>();
				boolean updateRequired = false;
				
				//adding existing data available for a particular module
				ModuleSchema moduleSchemaForExistingModule = moduleLineageDAO.getModuleSchemaForExistingModule(projectName, Sanitization.sanitizeInput(k), LOGGER);
				try {
					if(moduleSchemaForExistingModule.getSchemas().trim().length()>0) {
						String[] schemasArr = moduleSchemaForExistingModule.getSchemas().split(",");
					    for(String schema : schemasArr) {
						    hs.add(schema);
					    }
					} 
					updateRequired = true;
				} catch(Exception ex) {
				}
				
				for(String schema : v) {
					hs.add(schema);
					
					//for removing schema from previous modules if its present
					List<ModuleSchema> moduleSchemaList = moduleLineageDAO.getModuleSchemaMapper(projectName, Sanitization.sanitizeInput(schema), LOGGER);
					if(moduleSchemaList.size()>0) {
						for(ModuleSchema moduleSchema: moduleSchemaList) {
							String[] arr = moduleSchema.getSchemas().split(",");
							HashSet<String> hsNewSchemas = new HashSet<>();
							for(String str : arr) {
								if(!str.equalsIgnoreCase(schema)) {
									hsNewSchemas.add(str);
								}
							}
							moduleLineageDAO.updateModuleSchemaMapper(projectName, moduleSchema.getModule(), hsNewSchemas.toString().replace("[","").replace("]", "").replaceAll("\\s",""), LOGGER);
						}
					}
				}
				if(updateRequired) {
					moduleLineageDAO.updateModuleSchemaMapper(projectName, k, hs.toString().replace("[","").replace("]", "").replaceAll("\\s",""), LOGGER);
				} else {
				    moduleLineageDAO.insertIntoModuleSchemaMapper(projectName, k, hs.toString().replace("[","").replace("]", "").replaceAll("\\s",""), LOGGER);
				}
			});
			moduleLineageDAO.insertIntoModuleLineageStatus(projectName, 1, "module_schema_mapper", "completed", LOGGER);
			
			ProcessBuilder processBuilder = new ProcessBuilder("python3.9",moduleLineageScriptLocation, csrfToken, projectName);
			LOGGER.info("..........Command: python3.9 "+ moduleLineageScriptLocation+" "+csrfToken +" "+projectName);

			Process process = processBuilder.start();
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				LOGGER.info("..........pythonScript executed successfully");
			} else {
				LOGGER.info("..........pythonScript failed to execute with exitCode " + exitCode);
			}
		} catch (IOException ex1) {
			LOGGER.info("IOException occurred in executePythonScript Service " + ex1.getMessage());
		} catch (InterruptedException ex2) {
			LOGGER.info("InterruptedException occurred in executePythonScript Service " + ex2.getMessage());
			Thread.currentThread().interrupt();
		}
	}
	
	public void deleteModuleInfo(String projectName, String moduleName, Logger LOGGER) {
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			moduleName = Sanitization.sanitizeInput(moduleName);
			
			LOGGER.info("deleting moduleInfo from semantic.project_modules");
			moduleLineageDAO.deleteModuleInfo(projectName, moduleName, LOGGER);
			LOGGER.info("moduleInfo deleted successfully");
		} catch(Exception ex) {
			LOGGER.info("Exception occured in deleteModuleInfo Service "+ex.getMessage());
		}
	}
	
	public void getModuleLineageStatus(String projectName, CommonResponse<String> response) {
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			
			ModuleLineageStatus moduleLineageStatus = moduleLineageDAO.getModuleLineageStatus(projectName);
			String stepName = moduleLineageStatus.getStepName();
			String status = moduleLineageStatus.getStatus();
			if(status.equalsIgnoreCase("failed")) {
				response.setMessage(status);
				response.setPayload("stepname: "+stepName);
			} else if(status.equalsIgnoreCase("completed") && stepName.equalsIgnoreCase("module_lineage_complexity_calculation")) {
				response.setMessage(status);
				response.setPayload("stepname: "+stepName);
			} else {
				response.setMessage("inprogress");
				response.setPayload("stepname: "+stepName);
			}
		} catch(Exception ex) {
		}
	}
}
