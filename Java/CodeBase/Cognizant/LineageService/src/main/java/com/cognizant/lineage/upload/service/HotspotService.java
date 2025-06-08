package com.cognizant.lineage.upload.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.upload.dao.HotspotDao;
import com.cognizant.lineage.upload.model.NodeDataModel;
import com.cognizant.lineage.upload.model.TableHeadingResponseModel;


@Service
public class HotspotService {
	
	@Autowired
	HotspotDao hotDao;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HotspotService.class);
	
	public List<NodeDataModel> getNodeData(String projectName){
		
		List<NodeDataModel> nodeData = new ArrayList<>();
		
		try {
			nodeData = hotDao.getNodeData(projectName);
		} catch (Exception e) {
			LOGGER.error("Error in getNodeData service:  ", e);
		}	
		
		return nodeData;
	}
	
	
	public TableHeadingResponseModel getTableHeading(){
		
		//List<AttributeMapModel> mapData = hotDao.getAttributeMap();
		TableHeadingResponseModel response = new TableHeadingResponseModel();
		try {
			//mapData = hotDao.getAttributeMap();
			List<String> headings= new ArrayList<>();
			headings.add("Table Name");
			headings.add("Domain");
			headings.add("PageRank");
			headings.add("InEdges#");
			headings.add("OutEdges#");
			headings.add("Degree");
			response.setTableHeading(headings);
		}catch (Exception e) {
			LOGGER.error("Error in getAttributeMapData service:  ", e);
		}	
		
		return response;
	}


	public List<String> getProjects() {
		List<String> projectList = new ArrayList<String>();
		
		try {
			 projectList = hotDao.getProjectNames();
			
		}catch(Exception e) {
			 LOGGER.info("Error in getting project names for hotspot");
		}
		return projectList;
	}

}
