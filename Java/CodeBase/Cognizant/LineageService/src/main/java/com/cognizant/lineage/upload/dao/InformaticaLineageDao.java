package com.cognizant.lineage.upload.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.QueryConstant;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.model.InformaticaParamTables;
import com.cognizant.lineage.upload.rowMapper.InformaticaParamTablesMapper;

@Repository
public class InformaticaLineageDao {

	@Autowired
	@Qualifier("lineageJdbcTemplate")
	private JdbcTemplate lineageJdbcTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(InformaticaLineageDao.class);

	public int[] insertIntoInfraComponentLineage(List<Object[]> batchUpdateParams) {
		try {

			int[] results = lineageJdbcTemplate.batchUpdate(QueryConstant.INSERT_INTO_INFORMATICA_COMPONENT_LINEAGE,
					batchUpdateParams);
			LOGGER.info("result of INSERT_INTO_INFORMATICA_COMPONENT_LINEAGE batch update " + Arrays.toString(results));
			return results;
		} catch (Exception e) {
			LOGGER.error("Exception occurred in insertIntoInfraComponentLineage ", e);
			LOGGER.info("Exception occurred in insertIntoInfraComponentLineage " + e.getMessage());
			throw new RuntimeException("Exception occurred in insertIntoInfraComponentLineage ", e);
		}

	}

	public void insertIntoFailureInfraComponentLineage(String fileName, String errMsg, Integer sequenceId) {

		try {

			Object[] param = { fileName, "", "", "", "", "", "", "", "", "", "", "Failure", errMsg, "", "" ,sequenceId, ""};
			lineageJdbcTemplate.update(QueryConstant.INSERT_INTO_INFORMATICA_COMPONENT_LINEAGE_FAILURE, param);
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in checkLogFileStatus " + ex.getMessage());
		}

	}

	public Boolean failureInfraComponentDelete(String fileName) {
		boolean fileStatus = false;
		try {
			Object[] param = { fileName };
			int count = lineageJdbcTemplate.update(QueryConstant.FAILURE_INFORMATICA_COMPONENT_LENEAGE_DELETE, param);
			if (count < 0) {
				fileStatus = true;
			}
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in checkLogFileStatus " + ex.getMessage());
		}

		return fileStatus;
	}

	public int[] insertIntoInfaTableLevelLineage(List<Object[]> batchUpdateParams) {
		try {

			int[] results = lineageJdbcTemplate.batchUpdate(QueryConstant.INSERT_INTO_INFORMATICA_TABLE_LEVEL_LINEAGE,
					batchUpdateParams);
			LOGGER.info(
					"result of INSERT_INTO_INFORMATICA_TABLE_LEVEL_LINEAGE batch update " + Arrays.toString(results));
			return results;
		} catch (Exception e) {
			LOGGER.error("Exception occurred in insertIntoInfaTableLevelLineage ", e);
			throw new RuntimeException("Exception occurred in insertIntoInfaTableLevelLineage ", e);
		}

	}
	
	public Integer getSequenceId() {
		Integer sequenceId =0;
		try {
			
			 sequenceId = lineageJdbcTemplate.queryForObject(QueryConstant.INFA_LENEAGE_EXECUTION_SEQUENCE_ID, Integer.class);
			
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getSequenceId " + ex.getMessage());
		}

		return sequenceId;
	}
	
	public void insertIntoInfaExecutionSummery(Integer execution_id, int batch_size, String file_name, long start_time,
			long end_time, String status,String log_file) {
		
		try {
			Object[] param = {execution_id,batch_size,file_name,start_time,end_time,status,log_file};
			lineageJdbcTemplate.update(QueryConstant.INSERT_INTO_INFA_EXECUTION_SUMMERY, param);
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in insertIntoInfaExecutionSummery " + ex.getMessage());
		}

	}	

	public Long getSequenceIdValue() {
		Long sequenceId =0L;
		try {
			
			 sequenceId = lineageJdbcTemplate.queryForObject(QueryConstant.INFA_LENEAGE_SEQUENCE_ID, Long.class);
			
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getSequenceId " + ex.getMessage());
		}
		return sequenceId;
	}
		
	//informatica sql info db store
	public void sqlInsertForInformatica(Object[] param) {
		try {
			lineageJdbcTemplate.update(QueryConstant.INSERT_INTO_INFA_QUERY_TABLE, param);
		}catch(Exception e) {
			LOGGER.error("Exception in storing sql details to database. . . "+e.getMessage());
		}
	}
	
	public int infaSqlTableRowCountStatus(int job_id) {  //int job_id parameter needed to get correct status.
		int count=0;
		try {
			 Object[] param = {job_id};
			 count = lineageJdbcTemplate.queryForObject(QueryConstant.INFA_GET_ROW_COUNT_FOR_SQL_TABLE, Integer.class, param);
			 LOGGER.info("SQlQuery count for informatica =  "+count);

		} catch (Exception ex) {
			LOGGER.error("Exception occurred in infaSqlTableStatus " + ex.getMessage());
		}
		
		return count;
	}
	
	public String getTechnologyBasedOnYesNoQueryCountInDatastage(int job_id) {  //int job_id parameter needed to get correct status.
		int steps = 0;
		String technology = "";
		try {
			Object[] paramsYes = {"Y", job_id};
			int yCount = lineageJdbcTemplate.queryForObject(QueryConstant.DATASTAGE_GET_ROW_COUNT_FOR_SQL_TABLE, Integer.class, paramsYes);
			
			Object[] paramsNo = {"N", job_id};
			int nCount = lineageJdbcTemplate.queryForObject(QueryConstant.DATASTAGE_GET_ROW_COUNT_FOR_SQL_TABLE, Integer.class, paramsNo);
			
			if (yCount == 0 && nCount > 0) {
				technology = TechnologyConstants.DATASTAGE_SCENARIO1;
			} else if (yCount > 0 && nCount == 0) {
				technology = TechnologyConstants.DATASTAGE_SCENARIO2;
			} else {
				technology = TechnologyConstants.DATASTAGE_SCENARIO3;
			}
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getTechnologyBasedOnQueryCountInDatastage " + ex.getMessage());
		}
		return technology;
	}
	
	public List<InformaticaParamTables> getInformaticaParamTables(int jobId) {
		List<InformaticaParamTables> informaticaParamTablesList = new ArrayList<>();
		try {
			Object param = jobId;
			informaticaParamTablesList = lineageJdbcTemplate.query(QueryConstant.GET_INFORMATICA_SOURCE_TARGET_TO_BE_REPLACED, new InformaticaParamTablesMapper(), param);
		} catch(Exception ex) {
			LOGGER.error("Exception occured in getInformaticaParamTables "+ex.getMessage());
		}
		return informaticaParamTablesList;
	}
	
	public void updateInformaticaParamTables(String fromTable, String toTable, String fileName, int jobId) {
		try {
			Object[] params = {fromTable,toTable,fileName,jobId};
			lineageJdbcTemplate.update(QueryConstant.UPDATE_INFORMATICA_PARAM_SOURCE_TARGET, params);
		} catch(Exception ex) {
			LOGGER.error("Exception occured in updateInformaticaParamTables "+ex.getMessage());
		}
	}
	
	public String getParamValue(String paramFileName, String folderName, String workflowName, String sessionName, int priority, String paramName) {
		String paramValue = "";
		try {
			if(priority==1) {
				String query = "select param_value from semantic.informatica_param_details "
						+ "where file_name in "+paramFileName+" and folder_name = '"+folderName+"' and workflow_name = '"+workflowName+"' and session_name = '"+sessionName+"' and param_name = '"+paramName+"' and type = 'SPECIFIC' limit 1";
				LOGGER.info("query...Priority1..."+query);
				paramValue = lineageJdbcTemplate.queryForObject(query, String.class);
				LOGGER.info("paramValue...Priority1..."+paramValue);
			} else if(priority==2) {
				String query = "select param_value from semantic.informatica_param_details "
						+ "where file_name in "+paramFileName+" and folder_name = '"+folderName+"' and session_name = '"+sessionName+"' and param_name = '"+paramName+"' and type = 'SPECIFIC' limit 1";
				LOGGER.info("query...Priority2..."+query);
				paramValue = lineageJdbcTemplate.queryForObject(query, String.class);
				LOGGER.info("paramValue...Priority2..."+paramValue);
			} else if(priority==3) {
				String query = "select param_value from semantic.informatica_param_details "
						+ "where file_name in "+paramFileName+" and folder_name = '"+folderName+"' and workflow_name = '"+workflowName+"' and param_name = '"+paramName+"' and type = 'SPECIFIC' limit 1";
				LOGGER.info("query...Priority3..."+query);
				paramValue = lineageJdbcTemplate.queryForObject(query, String.class);
				LOGGER.info("paramValue...Priority3..."+paramValue);
			} else if(priority==4) {
				String query = "select param_value from semantic.informatica_param_details "
						+ "where file_name in "+paramFileName+" and (folder_name like '"+folderName+",%' or folder_name like '%,"+folderName+",%' or folder_name like '%,"+folderName+"') and param_name='"+paramName+"' and type = 'GLOBAL' limit 1";
				LOGGER.info("query...Priority4..."+query);
				paramValue = lineageJdbcTemplate.queryForObject(query, String.class);
				LOGGER.info("paramValue...Priority4..."+paramValue);
			} else if(priority==5) {
				String query = "select param_value from semantic.informatica_param_details "
						+ "where file_name in "+paramFileName+" and session_name = '"+sessionName+"' and param_name = '"+paramName+"' and type = 'SPECIFIC' limit 1";
				LOGGER.info("query...Priority5..."+query);
				paramValue = lineageJdbcTemplate.queryForObject(query, String.class);
				LOGGER.info("paramValue...Priority5..."+paramValue);
			} else if(priority==6) {
				String query = "select param_value from semantic.informatica_param_details "
						+ "where file_name in "+paramFileName+" and param_name = '"+paramName+"' and type = 'SPECIFIC' limit 1";
				LOGGER.info("query...Priority6..."+query);
				paramValue = lineageJdbcTemplate.queryForObject(query, String.class);
				LOGGER.info("paramValue...Priority6..."+paramValue);
			}
		} catch(Exception ex) {
			LOGGER.error("Exception occured in getParamValue "+ex.getMessage());
		}
		return paramValue;
	}

	public int infaSqlTableRowCountStatusFromScriptFilesInfo(int job_id) {  //int job_id parameter needed to get correct status.
		int count=0;
		try {
			Object[] param = {job_id};
			count = lineageJdbcTemplate.queryForObject(QueryConstant.NO_OF_RECORDS_SCRIPT_FILES_INFO, Integer.class, param);
			LOGGER.info("SQlQuery count for informatica =  "+count);

		} catch (Exception ex) {
			LOGGER.error("Exception occurred in infaSqlTableStatus " + ex.getMessage());
		}

		return count;
	}
}
