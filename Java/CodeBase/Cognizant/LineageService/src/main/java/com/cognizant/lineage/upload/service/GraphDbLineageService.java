package com.cognizant.lineage.upload.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.upload.dao.GraphDbLineageDao;
import com.cognizant.lineage.upload.model.DatabaseDetails;
import com.cognizant.lineage.upload.model.DatabaseTableMapResponse;
import com.cognizant.lineage.upload.model.DatabaseWithTableMapping;
import com.cognizant.lineage.upload.model.Lineage;

@Service
public class GraphDbLineageService {

	@Autowired
	private GraphDbLineageDao graphDbLineageDao;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphDbLineageService.class);
	
	
	public List<DatabaseWithTableMapping> getDatabaseWithTable(String projectName) {
		List<DatabaseWithTableMapping> databaseWithTableMapping = new ArrayList<>();
		List<DatabaseWithTableMapping> finalList = new ArrayList<>();
		databaseWithTableMapping = graphDbLineageDao.getDatabaseWithTable(projectName);
		if (databaseWithTableMapping.size() > 0 && databaseWithTableMapping != null) {
			Map<String, List<String>> dataBaseTableRelationshipMap = new HashMap<String, List<String>>();
			for (int i = 0; i < databaseWithTableMapping.size(); i++) {
				String data = databaseWithTableMapping.get(i).getDatabaseName();
				String database = "";
				String tableName = "";
				try {
					database = data.split("\\.")[0];
					tableName = data.split("\\.")[1];
				} catch (Exception e) {
					tableName = data;
					database = "null";

				}
				List<String> tableList = new ArrayList<>();
				if (dataBaseTableRelationshipMap.containsKey(database)) {
					tableList = dataBaseTableRelationshipMap.get(database);
				}
				tableList.add(tableName);
				dataBaseTableRelationshipMap.put(database, tableList);

			}

			for (Map.Entry<String, List<String>> entry : dataBaseTableRelationshipMap.entrySet()) {
				DatabaseWithTableMapping databaseWithTable = new DatabaseWithTableMapping();
				databaseWithTable.setDatabaseName(entry.getKey().replace("null", " "));
				databaseWithTable.setTableName(entry.getValue());
				finalList.add(databaseWithTable);
			}
		}

		return finalList;
	}

	public Set<DatabaseDetails> getDatabase(String projectName) {
		LOGGER.info("calling getDatabse() in service layer");
		List<DatabaseWithTableMapping> databaseWithTableMapping = new ArrayList<>();
		Set<DatabaseDetails> finalList = new HashSet<>();
		databaseWithTableMapping = graphDbLineageDao.getDatabaseWithTable(projectName);
		if (databaseWithTableMapping.size() > 0 && databaseWithTableMapping != null) {
			//Map<String, List<String>> dataBaseTableRelationshipMap = new HashMap<String, List<String>>();
			for (int i = 0; i < databaseWithTableMapping.size(); i++) {
				String data = databaseWithTableMapping.get(i).getDatabaseName();
				String[] database = {};
				//String tableName = "";
				try {
					DatabaseDetails databaseDetails = new DatabaseDetails();
					database = data.split("\\.");
					if(database.length>1) {
						databaseDetails.setLabel(database[0]);
						databaseDetails.setValue(database[0]);
						finalList.add(databaseDetails);
					}
					else {
						//System.out.println("databse not available this table : " + database[0]);
					}

				} catch (Exception e) {
					LOGGER.info("Error in splitting table and database. "+e.getLocalizedMessage());

				}
			}

		}
		//System.out.println(finalList.size());
		return finalList;
	}

	public List<DatabaseTableMapResponse> getTable(Lineage databaseName) {
		LOGGER.info("calling getTable() in service layer");
		List<DatabaseWithTableMapping> databaseWithTableMapping = new ArrayList<>();
		List<DatabaseTableMapResponse> response = new ArrayList<>();
		databaseWithTableMapping = graphDbLineageDao.getDatabaseWithTable(databaseName.getProjectName());
		
		if (databaseWithTableMapping.size() > 0 && databaseWithTableMapping != null) {
			Map<String, List<String>> dataBaseTableRelationshipMap = new HashMap<String, List<String>>();
			for (int i = 0; i < databaseWithTableMapping.size(); i++) {
				String data = databaseWithTableMapping.get(i).getDatabaseName();
				String database = "";
				String tableName = "";
				try {
					String[] parts = data.split("\\.");
					if(parts.length>1) {
						database = parts[0];
						tableName = data;
					}else {
						tableName = data;
						database = "null";
						LOGGER.info("databse not available this table : " + data);
					}
					
				} catch (Exception e) {
//					tableName = data;
//					database = "null";
					LOGGER.error("Error getting table list ",e);

				}
				List<String> tableList = new ArrayList<>();
				if (dataBaseTableRelationshipMap.containsKey(database)) {
					tableList = dataBaseTableRelationshipMap.get(database);
				}
				tableList.add(tableName);
				dataBaseTableRelationshipMap.put(database, tableList);

			}
			LOGGER.info("dataBaseTableRelationshipMap: {}",dataBaseTableRelationshipMap);
			
			if (StringUtils.isNotEmpty(databaseName.getDatabase())) {
				String array = databaseName.getDatabase();
				if(array.contains(",")) {
					String[] databases = array.split(",");
					for(String db: databases) {
						DatabaseTableMapResponse oneRes = new DatabaseTableMapResponse();
						oneRes.setDatabaseName(db);
						oneRes.setTableName(dataBaseTableRelationshipMap.get(db));
						response.add(oneRes);
					}
				}else {
					
					DatabaseTableMapResponse oneRes = new DatabaseTableMapResponse();
					oneRes.setDatabaseName(array);
					oneRes.setTableName(dataBaseTableRelationshipMap.get(array));
					response.add(oneRes);
				}
				
			}else {
				DatabaseTableMapResponse oneRes = new DatabaseTableMapResponse();
				oneRes.setDatabaseName("null");
				oneRes.setTableName(dataBaseTableRelationshipMap.get("null"));
				response.add(oneRes);
			}

		}	
		return response;
	}
	
	public List<String> dbTableJoinedOnDot(Lineage database){
		List<DatabaseTableMapResponse> response = new ArrayList<>();
		response = this.getTable(database);
		
		List<String> returnJoinedList = new ArrayList<>();
		
		for(DatabaseTableMapResponse mapRes: response) {
			
			String databasename = mapRes.getDatabaseName();
			String oneJoined = "";
			
			if(databasename.equalsIgnoreCase("null")) {
				databasename = "";
				for(String str: mapRes.getTableName()) {
					oneJoined = str;
					returnJoinedList.add(oneJoined);
				}
			}
			else {
				for(String str: mapRes.getTableName()) {
					oneJoined = str; //mapRes.getDatabaseName()+"."+str;
					returnJoinedList.add(oneJoined);
				}
			}
			
		}
		LOGGER.info("returnJoinedList: {}", returnJoinedList);
		return returnJoinedList;
	}
	
}
