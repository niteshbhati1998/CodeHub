package com.cognizant.lineage.upload.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.ModuleLineageQueryConstant;
import com.cognizant.lineage.upload.model.ModuleLineageStatus;
import com.cognizant.lineage.upload.model.ModuleSchema;
import com.cognizant.lineage.upload.model.NodeInfo;
import com.cognizant.lineage.upload.rowMapper.ModuleLineageStatusMapper;
import com.cognizant.lineage.upload.rowMapper.ModuleSchemaMapper;
import com.cognizant.lineage.upload.rowMapper.NodeInfoMapper;

@Repository
public class ModuleLineageDAO {

	@Autowired
	@Qualifier("presentationJdbcTemplate")
    JdbcTemplate jdbcTemplate;

	public List<NodeInfo> getNodeInfo(String projectName, Logger LOGGER) {
		List<NodeInfo> nodeInfoList = new ArrayList<>();
		try {
			Object param = projectName;
			nodeInfoList = jdbcTemplate.query(ModuleLineageQueryConstant.GET_NODE_INFO, new NodeInfoMapper(), param);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getNodeInfo DAO "+ex.getMessage());
		}
		return nodeInfoList;
	}
	
	public List<ModuleSchema> getModuleSchemaList(String projectName, Logger LOGGER) {
		List<ModuleSchema> moduleSchemaList = new ArrayList<>();
		try {
			Object[] params = {projectName,projectName,projectName};
			moduleSchemaList = jdbcTemplate.query(ModuleLineageQueryConstant.GET_MODULE_SCHEMA_LIST, new ModuleSchemaMapper(), params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getModuleList DAO "+ex.getMessage());
		}
		return moduleSchemaList;
	}

	public void saveModuleInfo(String projectName, String moduleName, Logger LOGGER) {
		try {
			Object[] params = {projectName, moduleName};
			jdbcTemplate.update(ModuleLineageQueryConstant.SAVE_MODULE_INFO, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in saveModuleInfo DAO "+ex.getMessage());
		}
	}
	
	public ModuleSchema getModuleSchemaForExistingModule(String projectName, String module, Logger LOGGER) {
		ModuleSchema moduleSchema = new ModuleSchema();
		try {
			String query = "select module,schemas from semantic.module_schema_mapper where project_name = '"+projectName+"' "
					+ "and module = '"+module+"'";
			moduleSchema = jdbcTemplate.queryForObject(query, new ModuleSchemaMapper());
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getModuleSchemaForExistingModule DAO "+ex.getMessage());
		}
		return moduleSchema;
	}
	
	public void insertIntoModuleSchemaMapper(String projectName, String moduleName, String schemaName, Logger LOGGER) {
		try {
			Object[] params = {projectName, moduleName, schemaName};
			jdbcTemplate.update(ModuleLineageQueryConstant.INSERT_INTO_MODULE_SCHEMA_MAPPER, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in insertIntoModuleSchemaMapper DAO "+ex.getMessage());
		}
	}
	
	public List<ModuleSchema> getModuleSchemaMapper(String projectName, String schema, Logger LOGGER) {
		List<ModuleSchema> moduleSchemaList = new ArrayList<>();
		try {
			String query = "select module,schemas from semantic.module_schema_mapper where project_name = '"+projectName+"' "
					+ "and schemas like '%,"+schema+",%' or schemas like '"+schema+",%' or schemas like '%,"+schema+"' or schemas='"+schema+"'";
			moduleSchemaList = jdbcTemplate.query(query, new ModuleSchemaMapper());
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getModuleSchemaMapper DAO "+ex.getMessage());
		}
		return moduleSchemaList;
	}
	
	public void updateModuleSchemaMapper(String projectName, String module, String schemas, Logger LOGGER) {
		try {
			Object[] params = {schemas, projectName, module};
			jdbcTemplate.update(ModuleLineageQueryConstant.UPDATE_MODULE_SCHEMA_MAPPER, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in updateModuleSchemaMapper DAO "+ex.getMessage());
		}
	}
	
	public void deleteFromModuleSchemaMapperAccordingToProjectModules(String projectName, Logger LOGGER) {
		try {
			Object[] params = {projectName,projectName};
			jdbcTemplate.update(ModuleLineageQueryConstant.DELETE_FROM_MODULE_SCHEMA_MAPPER_ACC_TO_PROJECT_MODULES, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in deleteFromModuleSchemaMapperAccordingToProjectModules DAO "+ex.getMessage());
		}
	}
	
	public void deleteExistingProjectDetailsFromModuleLineageStatus(String projectName, Logger LOGGER) {
		try {
			Object param = projectName;
			jdbcTemplate.update(ModuleLineageQueryConstant.DELETE_EXISTING_PROJECT_DETAILS_FROM_MODULE_LINEAGE_STATUS, param);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in deleteExistingProjectDetailsFromModuleLineageStatus DAO "+ex.getMessage());
		}
	}
	
	public void insertIntoModuleLineageStatus(String projectName, int stepNo, String stepName, String status, Logger LOGGER) {
		try {
			Object[] params = {projectName, stepNo, stepName, status};
			jdbcTemplate.update(ModuleLineageQueryConstant.INSERT_INTO_MODULE_LINEAGE_STATUS, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in insertIntoModuleLineageStatus DAO "+ex.getMessage());
		}
	}
	
	public void deleteModuleInfo(String projectName, String moduleName, Logger LOGGER) {
		try {
			Object[] params = {projectName, moduleName};
			jdbcTemplate.update(ModuleLineageQueryConstant.DELETE_MODULE_INFO, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in deleteModuleInfo DAO "+ex.getMessage());
		}
	}
	
	public ModuleLineageStatus getModuleLineageStatus(String projectName) {
		ModuleLineageStatus moduleLineageStatus = new ModuleLineageStatus();
		try {
			Object param = projectName;
			moduleLineageStatus = jdbcTemplate.queryForObject(ModuleLineageQueryConstant.GET_MODULE_LINEAGE_STATUS, new ModuleLineageStatusMapper(), param);
		} catch (Exception ex) {
		}
		return moduleLineageStatus;
	}
}