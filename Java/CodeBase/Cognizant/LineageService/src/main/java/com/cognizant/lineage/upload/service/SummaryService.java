package com.cognizant.lineage.upload.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.upload.dao.SummaryDao;
import com.cognizant.lineage.upload.model.TechnologyDetails;
import com.cognizant.lineage.upload.model.TechnologyInfo;
import com.cognizant.lineage.upload.model.UploadScriptDetails;
import com.cognizant.lineage.upload.model.UploadScriptTypeCount;

@Service
public class SummaryService {

	@Autowired
	private SummaryDao summaryDao;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SummaryService.class);
	
	public List<UploadScriptDetails> getScriptDetails(String projectName) {
		List<UploadScriptDetails> uploadScriptDetailsList = new ArrayList<>();
		try {
			uploadScriptDetailsList = summaryDao.getScriptDetails(projectName);	
		} catch(Exception ex) {
			LOGGER.error("Exception occured in getScriptDetails Service "+ex.getMessage());
		}
		return uploadScriptDetailsList;	
	}
	
	public List<UploadScriptTypeCount> getScriptTypeCount(String projectName) {
		List<UploadScriptTypeCount> uploadScriptTypeCountList = new ArrayList<>();
		try {
			uploadScriptTypeCountList = summaryDao.getScriptTypeCount(projectName);	
		} catch(Exception ex) {
			LOGGER.error("Exception occured in getScriptTypeCountDetails Service "+ex.getMessage());
		}
		return uploadScriptTypeCountList;	
	}
	
	public JSONArray getTechnologyList(String projectName) {
		JSONArray uiJson = new JSONArray();
		try {
			Map<String,String> technologyMapAsPerUi = new HashMap<>();
			technologyMapAsPerUi.put("DATASTAGE", "ETL-DATASTAGE");
			technologyMapAsPerUi.put("INFORMATICA", "ETL-INFORMATICA");
			technologyMapAsPerUi.put("IDMC", "ETL-IDMC");
			technologyMapAsPerUi.put("POWER-BI", "BI-POWERBI");
			technologyMapAsPerUi.put("SHELL", "SCRIPTS-SHELL");
			technologyMapAsPerUi.put("PYTHON-SCRIPT", "SCRIPTS-PYTHON");
			
			Map<String,String> technologySubTechnologyMap = new HashMap<>();
			
			List<String> technologyList = summaryDao.getTechnologyList(projectName);	
			for(String technology: technologyList) {
				if(technologyMapAsPerUi.containsKey(technology)) {
		    		technology =  technologyMapAsPerUi.get(technology);
		    	} 
				if(technologySubTechnologyMap.containsKey(technology.split("-")[0])) {
					String subTechnologies = technologySubTechnologyMap.get(technology.split("-")[0]) + "," +technology.split("-")[1];
					technologySubTechnologyMap.put(technology.split("-")[0], subTechnologies);
				} else {
					technologySubTechnologyMap.put(technology.split("-")[0], technology.split("-")[1]);
				}
			}

			//preparing-json
			for (Map.Entry<String, String> set: technologySubTechnologyMap.entrySet()) {
				JSONObject parent = new JSONObject();
				parent.put("value",set.getKey());
				parent.put("label",set.getKey());

				JSONArray childrenArray = new JSONArray();
				if(set.getValue().contains(",")) {
					String childrenArr[] = set.getValue().split(",");
					for(String child: childrenArr) {
						JSONObject children = new JSONObject();
						children.put("value",getTabName(set.getKey()+"-"+child));
						children.put("label",child);
						childrenArray.put(children);
					}
				} else {
					JSONObject children = new JSONObject();				
					children.put("value",getTabName(set.getKey()+"-"+set.getValue()));
					children.put("label",set.getValue());
					childrenArray.put(children);
				}
				parent.put("children",childrenArray);
				uiJson.put(parent);
			}
		} catch(Exception ex) {
			LOGGER.error("Exception occured in getTechnologyList Service "+ex.getMessage());
		}
		return uiJson;
	}
	
	public String getTabName(String technology) {
		technology = technology.toLowerCase();
		try {
			if(!(technology.contains("procedure") || (!technology.contains("qlikview") && technology.contains("view")) || technology.contains("function") || technology.contains("trigger"))) {
				technology = technology.split("-")[1];
			}
		} catch(Exception ex) {
			LOGGER.error("Exception occured in getTabName Service "+ex.getMessage());
		}
		return technology.toUpperCase();
	}
	
	public JSONArray fetchTechnologyDetails(String projectName, List<TechnologyInfo> technologyList) {
		JSONArray ui = new JSONArray();
		try {
			Map<String,String> technologyMapAsPerUi = new HashMap<>();
			technologyMapAsPerUi.put("ETL-DATASTAGE", "DATASTAGE");
			technologyMapAsPerUi.put("ETL-INFORMATICA", "INFORMATICA");
			technologyMapAsPerUi.put("ETL-IDMC", "IDMC");
			technologyMapAsPerUi.put("BI-POWERBI", "POWER-BI");
			technologyMapAsPerUi.put("SCRIPTS-SHELL", "SHELL");
			technologyMapAsPerUi.put("SCRIPTS-PYTHON", "PYTHON-SCRIPT");
			
			for(TechnologyInfo technology: technologyList) {
				String tech = technology.getParent()+"-"+technology.getChild();
				if(technologyMapAsPerUi.containsKey(tech)) {
					tech =  technologyMapAsPerUi.get(tech);
		    	} 
				JSONObject jsonObjectParent = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				List<TechnologyDetails> technologyDetailsList = summaryDao.fetchTechnologyDetails(projectName,tech);
				for(TechnologyDetails technologyDetails: technologyDetailsList) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("script_name", technologyDetails.getScriptName());
					jsonObject.put("source", technologyDetails.getSource()); 
					jsonObject.put("target", technologyDetails.getTarget());
					jsonObject.put("sql_text", technologyDetails.getSqlText());
					jsonObject.put("statement_type", technologyDetails.getStatementType());
					jsonArray.put(jsonObject);
				}
				jsonObjectParent.put(getTabName(technology.getParent()+"-"+technology.getChild()), jsonArray);
				ui.put(jsonObjectParent);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Exception occured in fetchTechnologyDetails Service "+ex.getMessage());
		}
		return ui;	
	}
}