package com.cognizant.lineage.upload.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.HotspotLineageQueryConstant;
import com.cognizant.lineage.upload.model.NodeDataModel;
import com.cognizant.lineage.upload.rowMapper.NodeDataRowMapper;

@Repository
public class HotspotDao {
	
	@Autowired
	@Qualifier("presentationJdbcTemplate")
	private JdbcTemplate presentationJdbcTemplate;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HotspotDao.class);
	
    public List<String> getNodeNames(String projectName){	
		List<String> nodeNamesList = new ArrayList<>();
		try {
			Object[] param = {projectName};
			nodeNamesList = presentationJdbcTemplate.queryForList(HotspotLineageQueryConstant.GET_NODE_NAMES, String.class, param);
		}catch(Exception e) {
			LOGGER.error("Error in getNodeNames", e);
		}		
		return nodeNamesList;
	}
    
    public List<String> getNodeNamesApplicationBased(String projectName){	
		List<String> nodeNamesList = new ArrayList<>();
		try {
			Object[] param = {projectName};
			nodeNamesList = presentationJdbcTemplate.queryForList(HotspotLineageQueryConstant.GET_NODE_NAMES_APPLICATION_BASED, String.class, param);
		}catch(Exception e) {
			LOGGER.error("Error in getNodeNames", e);
		}		
		return nodeNamesList;
	}
	
	public List<NodeDataModel> getNodeData(String projectName){
		
		List<NodeDataModel>  nodeData = new ArrayList<>();
		
		try {
			Object[] param = {projectName};
			nodeData = presentationJdbcTemplate.query(HotspotLineageQueryConstant.GET_NODE_DATA, new NodeDataRowMapper(), param);
		}catch(Exception e) {
			LOGGER.error("Error in getNodeData", e);
		}		
		return nodeData;
	}

	public List<String> getProjectNames() {
		List<String>  projList = new ArrayList<>();
		
		try {
			projList = presentationJdbcTemplate.queryForList(HotspotLineageQueryConstant.GET_PROJECT_NAMES, String.class);
		}catch(Exception e) {
			LOGGER.error("Error in getNodeData", e);
		}		
		return projList;
	}

}
