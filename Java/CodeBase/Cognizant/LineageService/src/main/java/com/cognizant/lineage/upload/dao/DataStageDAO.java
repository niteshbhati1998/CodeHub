package com.cognizant.lineage.upload.dao;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.constants.DataStageQueryConstant;
import com.cognizant.lineage.upload.model.Datastaging;
import com.cognizant.lineage.upload.model.ScriptCalculationDetails;
import com.cognizant.lineage.upload.model.SourceAndTarget;
import com.cognizant.lineage.upload.rowMapper.DataStageRowMapper;
import com.cognizant.lineage.upload.rowMapper.ScriptCalculationDetailsMapper;

@Repository
public class DataStageDAO {

	//private static Logger LOGGER = LoggerFactory.getLogger(DataStageDAO.class);
	
	@Autowired
	@Qualifier("lineageJdbcTemplate")
    JdbcTemplate jdbcTemplate;

	//Status Update
	public int getJobId() {
		int jobId = 1;
		try {
			jobId = jdbcTemplate.queryForObject(DataStageQueryConstant.GET_JOB_ID, Integer.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return jobId;
	}
	
	public void insertIntoLineageJob(int jobId, String projectName, String parentTechnology, String technology, String uploadType, String uploadDirectory, Logger LOGGER) {
		try {
			Object[] params = {jobId, projectName, parentTechnology, technology, uploadType, uploadDirectory};
			jdbcTemplate.update(DataStageQueryConstant.INSERT_INTO_LINEAGE_JOB, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in insertIntoLineageJob DAO " + ex.getMessage());	
		}
	}
	
	public int getStepNo(int jobId, Logger LOGGER) {
		int stepNo = 0;
		try {
			Object[] param = {jobId};
			stepNo = jdbcTemplate.queryForObject(DataStageQueryConstant.GET_STEP_NO, Integer.class, param);
		} catch (Exception ex) {
			stepNo=1;
			LOGGER.info("Exception occurred in getStepNo DAO " + ex.getMessage());
		}
		return stepNo;
	}
	
	public void insertIntoLineageJobStatus(int jobId, int stepNo, String stepName, int noOfFileReceived, int noOfFileProcessed, String status, String logFileLocation, Logger LOGGER) {
		try {
			Object[] params = {jobId,stepNo,stepName,noOfFileReceived,noOfFileProcessed,status,logFileLocation};
			jdbcTemplate.update(DataStageQueryConstant.INSERT_INTO_LINEAGE_JOB_STATUS, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in insertIntoLineageJobStatus DAO " + ex.getMessage());
		}
	}
	
	public void updateLineageJobStatus(int noOfFileProcessed, String status, int jobId, int stepNo, Logger LOGGER) {
		try {
			Object[] params = {noOfFileProcessed,status,jobId,stepNo};
			jdbcTemplate.update(DataStageQueryConstant.UPDATE_LINEAGE_JOB_STATUS, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occurred in updateLineageJobStatus DAO " + ex.getMessage());
		}
	}
	
	public void updateLineageJobStatusForErrorOrCompleted(String status, int jobId, int stepNo, Logger LOGGER) {
		try {
			Object[] params = {status,jobId,stepNo};
			jdbcTemplate.update(DataStageQueryConstant.UPDATE_LINEAGE_JOB_STATUS_FOR_ERROR_OR_COMPLETED, params);
		} catch (Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occurred in updateLineageJobStatusForErrorOrCompleted DAO " + ex.getMessage());
		}
	}
	
	//Parsing: Part1
	public void insertIntoDataStageComponentLevelLineage(int jobId, String item, String tableNameOrQuery, String identifier, String inputpins, String outputpins, String partner, String lookup, String jobName, Logger LOGGER) {
		try {
			Object[] params = {jobId, item, tableNameOrQuery, identifier, inputpins, outputpins, partner, lookup, jobName};
			jdbcTemplate.update(DataStageQueryConstant.INSERT_INTO_DATASTAGE_COMPONENT_LEVEL_LINEAGE, params);
		} catch (Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occured in insertIntoDataStageComponentLevelLineage DAO " + ex.getMessage());
		}
	}
	
	public void updateDataStage(int jobId, Logger LOGGER) {
		try {
			Object[] param = {jobId};
			jdbcTemplate.update(DataStageQueryConstant.UPDATE_DATASTAGE_COMPONENT_LEVEL_LINEAGE, param);
		} catch (Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occured in insertIntoDataStage DAO " + ex.getMessage());	
		}
	}
	
	//Parsing: Part2
	public boolean insertIntoDataStageTableLevelLineage(int jobId, List<SourceAndTarget> sourceTargetlist, Logger LOGGER) {
		boolean inserted = false;
		try {
			jdbcTemplate.batchUpdate(DataStageQueryConstant.INSERT_DATA_INTO_DATASTAGE_TABLE_LEVEL_LINEAGE, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setInt(1, jobId);
					ps.setString(2, sourceTargetlist.get(i).getSource());
					ps.setString(3, sourceTargetlist.get(i).getTarget());
					if (sourceTargetlist.get(i).getSource().toUpperCase().startsWith("SELECT") || (sourceTargetlist.get(i).getSource().toUpperCase().startsWith("LOCKING") && sourceTargetlist.get(i).getSource().toUpperCase().contains("SELECT"))) {
						ps.setString(4, "Y");
					} else {
						ps.setString(4, "N");
					}
					ps.setString(5, sourceTargetlist.get(i).getJobName());
				}

				@Override
				public int getBatchSize() {
					return sourceTargetlist.size();
				}
			});
			inserted = true;
		} catch (Exception e) {
			LOGGER.info("Exception occured in insertIntoDataStageTableLevelLineage DAO "+e.getMessage());
		}
		return inserted;
	}

	public List<Datastaging> getDataStageComponentLevelLineageDetailsForTarget(int jobId, Logger LOGGER) {
		List<Datastaging> dataStagingList = new ArrayList<>();
		try {
			Object[] params = {jobId,jobId,jobId};
		    dataStagingList = jdbcTemplate.query(DataStageQueryConstant.GET_DATASTAGE_COMPONENT_LEVEL_LINEAGE_DETAILS_FOR_TARGET, new DataStageRowMapper(), params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getDataStageComponentLevelLineageDetailsForTarget DAO "+ex.getMessage());
		}
		return dataStagingList;
	}
	
	public List<Datastaging> getDataStageComponentLevelLineageDetails(int jobId, String jobname, Logger LOGGER) {		
		List<Datastaging> dataStagingList = new ArrayList<Datastaging>();
		try {
			Object[] params = {jobname,jobId,jobname,jobId,jobname,jobId};
		    dataStagingList = jdbcTemplate.query(DataStageQueryConstant.GET_DATASTAGE_COMPONENT_LEVEL_LINEAGE_DETAILS, new DataStageRowMapper(), params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getDataStageComponentLevelLineageDetails DAO "+ex.getMessage());
		}
		return dataStagingList;
	}
	
	//Python
	public int getCountFromDataStageTableLevelLineage(String isQuery, int jobId, Logger LOGGER) {
		int count = 0;
		try {
			Object[] params = {isQuery,jobId};
			count = jdbcTemplate.queryForObject(DataStageQueryConstant.GET_COUNT_FROM_DATASTAGE_TABLE_LEVEL_LINEAGE, Integer.class, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getCountFromDataStageTableLevelLineage DAO "+ex.getMessage());
		}
		return count;
	}
	
	//complexity-calculation
	public List<ScriptCalculationDetails> getScriptCalculationDetails(Logger LOGGER) {
		List<ScriptCalculationDetails> scriptCalculationDetailsList = new ArrayList<>();
		try {
			scriptCalculationDetailsList = jdbcTemplate.query(DataStageQueryConstant.GET_SCRIPT_CALCULATION_DETAILS, new ScriptCalculationDetailsMapper());
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getScriptCalculationDetails DAO "+ex.getMessage());
		}
		return scriptCalculationDetailsList;
	}
	
	public int getCountFromDataStageTableLevelLineageJobNameSpecific(String isQuery, int jobId, String jobName, Logger LOGGER) {
		int count = 0;
		try {
			Object[] params = {isQuery,jobId,jobName};
			count = jdbcTemplate.queryForObject(DataStageQueryConstant.GET_COUNT_FROM_DATASTAGE_TABLE_LEVEL_LINEAGE_JOBNAME_SPECIFIC, Integer.class, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getCountFromDataStageTableLevelLineageJobNameSpecific DAO "+ex.getMessage());
		}
		return count;
	}
	
	public int getTransformationCount(int jobId, String jobName, Logger LOGGER) {
		int count = 0;
		try {
			Object[] params = {jobName,jobId};
			count = jdbcTemplate.queryForObject(DataStageQueryConstant.GET_TRANSFORMATION_COUNT, Integer.class, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getTransformationCount DAO "+ex.getMessage());
		}
		return count;
	}
	
	public List<String> getQueryIfTypeIsYes(int jobId, String jobName, Logger LOGGER) {
		List<String> queryList = new ArrayList<>();
		try {
			Object[] params = {jobName,jobId};
			queryList = jdbcTemplate.queryForList(DataStageQueryConstant.GET_QUERY_IF_TYPE_IS_YES, String.class, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getQueryIfTypeIsYes DAO "+ex.getMessage());
		}
		return queryList;
	}
	
	public List<String> getFunctionList(Logger LOGGER) {
		List<String> functionList = new ArrayList<>();
		try {
			functionList = jdbcTemplate.queryForList(DataStageQueryConstant.GET_FUNCTION_LIST, String.class);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in functionList DAO "+ex.getMessage());
		}
		return functionList;
	}
	
	public void insertIntoScriptComplexity(int jobId, String projectName, String technology, String fileName, 
			int functionUsedCount, int joinCount, int selectCount, int transformationCount, String complexity, String status, Logger LOGGER) {
		try {
			Object[] params = {jobId, projectName, technology, fileName.toUpperCase(), functionUsedCount, joinCount, selectCount, transformationCount, complexity, status};
			jdbcTemplate.update(DataStageQueryConstant.INSERT_INTO_SCRIPT_COMPLEXITY, params);
		} catch (Exception ex) {
			ex.printStackTrace();
			LOGGER.info("Exception occured in insertIntoScriptComplexity DAO " + ex.getMessage());
		}
	}
	
	public String getLineageJobStatus(int jobId, int stepNo, Logger LOGGER) {
		String status = "";
		try {
			Object[] params = {jobId, stepNo};
			status = jdbcTemplate.queryForObject(DataStageQueryConstant.GET_LINEAGE_JOB_STATUS, String.class, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in getLineageJobStatus DAO "+ex.getMessage());
		}
		return status;
	}
	
	public void updateLineageJobStatus(String status, int jobId, int stepNo, Logger LOGGER) {
		try {
			Object[] params = {status, jobId, stepNo};
			jdbcTemplate.update(DataStageQueryConstant.UPDATE_LINEAGE_JOB_STATUS_DETAILS, params);
		} catch (Exception ex) {
			LOGGER.info("Exception occured in updateLineageJobStatus DAO "+ex.getMessage());
		}
	}
}
