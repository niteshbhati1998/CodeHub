package com.cognizant.lineage.database.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.database.constants.ImportDBObjectsQueryConstant;
import com.cognizant.lineage.database.model.DbScriptDetails;
import com.cognizant.lineage.database.rowMapper.DbScriptDetailsMapper;
import com.cognizant.lineage.util.Sanitization;

@Repository
public class ImportDBObjectsDAO {
	  
	@Autowired
	@Qualifier("lineageJdbcTemplate")
	private JdbcTemplate lineageJdbcTemplate;
	
	//Endpoint: ExtractButton
	public int getJobId(Logger LOGGER) {
		int jobId = 0;
		try {
			jobId = lineageJdbcTemplate.queryForObject(ImportDBObjectsQueryConstant.GET_JOB_ID, Integer.class);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in getJobId DAO " + ex.getMessage());
		}
		return jobId;
	}
	
	public void saveDatabaseObjectTypeDetails(int jobId, String projectName, String databaseType, String databaseObject, String uploadType, String databaseObjectPath, String connectionName, Logger LOGGER) {
		try {
			Object[] params = {jobId, projectName, databaseType, databaseObject, uploadType, databaseObjectPath, connectionName};
			LOGGER.info(".........saving JobId: " + jobId + ".....DatabaseType: " + databaseType + ".....ObjectType: " + databaseObject + ".....Location: " + databaseObjectPath);
			lineageJdbcTemplate.update(ImportDBObjectsQueryConstant.SAVE_DATABASE_OBJECT_TYPE_DETAILS, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in saveDatabaseObjectTypeDetails DAO " + ex.getMessage());
		}
	}
	
	public List<DbScriptDetails> getDbScriptDetails(List<Integer> jobIdList, Logger LOGGER) {
		List<DbScriptDetails> dbScriptDetailsList = new ArrayList<>();
		try {
			String jobIds = jobIdList.toString().replace("[", "").replace("]", "");
			jobIds = Sanitization.sanitizeInput(jobIds);
			String query = "select job_id, project_name, parent_technology, technology, uploaded_dir from lineage_job where job_id in ("+jobIds+") order by job_id asc";
			dbScriptDetailsList = lineageJdbcTemplate.query(query, new DbScriptDetailsMapper());
		} catch(Exception ex) {
			LOGGER.info("Exception occurred in getDbScriptDetails DAO "+ex.getMessage());
		}
		return dbScriptDetailsList;
	}
	
	//Endpoint: StatusButton
	public int getDbObjectStatus(int jobId, String status) {
		int count=0;
		String query = "";
		try {
			if(status.equals("")) {
				  query = "select count(*) from lineage_job_status where job_id="+jobId;
			} else {
			      query = "select count(*) from lineage_job_status where job_id="+jobId + " and upper(trim(status)) like '%"+status+"%'";
			}
			System.out.println("query used: "+query);
			count = lineageJdbcTemplate.queryForObject(query,Integer.class);
		} catch (Exception ex) {
			System.out.println("Exception occurred in getDbObjectStatus DAO" + ex.getMessage());
		}
		return count;
	}
}
