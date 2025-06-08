package com.cognizant.lineage.upload.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.upload.model.BusinessObjectCountDetails;
import com.cognizant.lineage.upload.model.BusinessObjectDetails;
import com.cognizant.lineage.upload.model.BusinessObjectNameDetails;
import com.cognizant.lineage.upload.model.ClusterReport;
import com.cognizant.lineage.upload.model.HotSpotDetail;
import com.cognizant.lineage.upload.model.ObjectInventory;
import com.cognizant.lineage.upload.model.SpaceAndComplexity;
import com.cognizant.lineage.upload.model.WavePlanningAppDetails;
import com.cognizant.lineage.upload.model.WavePlanningObjDetails;
import com.cognizant.lineage.upload.rowMapper.BusinessObjectCountDetailsMapper;
import com.cognizant.lineage.upload.rowMapper.BusinessObjectDetailsMapper;
import com.cognizant.lineage.upload.rowMapper.BusinessObjectNameDetailsMapper;
import com.cognizant.lineage.upload.rowMapper.ClusterReportMapper;
import com.cognizant.lineage.upload.rowMapper.HotSpotDetailMapper;
import com.cognizant.lineage.upload.rowMapper.ObjectInventoryMapper;
import com.cognizant.lineage.upload.rowMapper.SpaceAndComplexityMapper;
import com.cognizant.lineage.upload.rowMapper.WavePlanningAppDetailsMapper;
import com.cognizant.lineage.upload.rowMapper.WavePlanningObjDetailsMapper;

@Repository
public class GlobalExcelDAO {

    @Autowired
    @Qualifier("lineageJdbcTemplate")
    JdbcTemplate jdbcTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExcelDAO.class);

    public List<ObjectInventory> getObjectsInventoryDetails(String projectName) {
        List<ObjectInventory> inventoryList = new ArrayList<>();
        Object[] param = {projectName};
        String query = "select node_type, count(*) from presentation.nodes where project_name = ? group by 1;";
        try {
            inventoryList = jdbcTemplate.query(query, new ObjectInventoryMapper(), param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching object inventory details: {}", ex.getMessage());
        }
        return inventoryList;
    }

    public List<ClusterReport> getClusterReportDetails(String projectName) {
        List<ClusterReport> clusterReportList = new ArrayList<>();
        Object[] param = {projectName};
        String query = "select island_id, from_node, to_node, script_name, script_type, statement_type from presentation.edges where project_name = ?;";
        try {
            clusterReportList = jdbcTemplate.query(query, new ClusterReportMapper(), param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching cluster report details: {}", ex.getMessage());
        }
        return clusterReportList;
    }

    public List<HotSpotDetail> getHotSpotDetails(String projectName) {
        List<HotSpotDetail> details = new ArrayList<>();
        Object[] param = {projectName};
        String query = "select project_name,node_name,incoming_edges,outgoing_edges,degree from presentation.nodes where project_name = ? order by 2;";
        try {
            details = jdbcTemplate.query(query, new HotSpotDetailMapper(), param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching cluster report details: {}", ex.getMessage());
        }
        return details;
    }

    public List<WavePlanningAppDetails> getWavePlanningAppDetails(String projectname) {
        List <WavePlanningAppDetails> planningAppDetails = new ArrayList<>();
        Object[] param = {projectname};
        String query = "SELECT project_name,\n" +
                "   module,\n" +
                "   CASE\n" +
                "     WHEN UPPER(technology) LIKE '%%BI%%' THEN 'Analytics Applications'\n" +
                "     ELSE 'ETL & ELT Applications'\n" +
                "   END AS Application_type,\n" +
                "   technology,\n" +
                "   script_name,\n" +
                "   complexity,\n" +
                "   sprint\n" +
                "    FROM presentation.sprint_scripts\n" +
                "    WHERE project_name = ?\n" +
                "    ORDER BY module,\n" +
                "     Application_type,\n" +
                "     technology,\n" +
                "     script_name,\n" +
                "     complexity;";
        try {
            planningAppDetails = jdbcTemplate.query(query, new WavePlanningAppDetailsMapper(), param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching wave planning application details: {}", ex.getMessage());
        }
        return planningAppDetails;
    }

    public List<WavePlanningObjDetails> getWavePlanningObjDetails(String projectname) {
        List<WavePlanningObjDetails> details = new ArrayList<>();
        Object[] param = {projectname};
        String query = "SELECT project_name,\n" +
                "        MODULE as functional_module,\n" +
                "               technology AS DATABASE,\n" +
                "               object_name,\n" +
                "               object_type,\n" +
                "               complexity,\n" +
                "               CASE\n" +
                "                 WHEN UPPER(TRIM(object_type)) IN ('VIEW','FILE') THEN 'NA'\n" +
                "                 WHEN UPPER(TRIM(technology)) IN ('TEMPORARY_TABLE') THEN 'NA'\n" +
                "                 ELSE CAST(object_size AS VARCHAR(200))\n" +
                "               END AS \"object_size(MB)\",\n" +
                "               sprint\n" +
                "        FROM (SELECT DISTINCT project_name,module,\n" +
                "                     technology,\n" +
                "                     object_name,\n" +
                "                     object_type,\n" +
                "                     complexity,\n" +
                "                     script_name,\n" +
                "                     object_size,\n" +
                "                     sprint,\n" +
                "                     script_name,\n" +
                "                     ROW_NUMBER() OVER (PARTITION BY object_name ORDER BY script_name) AS rnk\n" +
                "              FROM presentation.sprint_objects o\n" +
                "              WHERE project_name = ?\n" +
                "              AND   o.script_name IN (SELECT DISTINCT script_name\n" +
                "                                      FROM presentation.sprint_scripts ss\n" +
                "                                      WHERE ss.sprint = o.sprint)) a\n" +
                "        WHERE rnk = 1\n" +
                "        ORDER BY 1,2,3,4,5,6,7;";
        try {
            details = jdbcTemplate.query(query, new WavePlanningObjDetailsMapper(), param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching wave planning object details: {}", ex.getMessage());
        }
        return details;
    }

    public Integer getMaxNoOfBusinessLevels(String projectName) {
        Integer result = null;
        Object[] param = {projectName};
        String query = "select max(maxNode) from (\n" +
                "select full_path, max((CHAR_LENGTH(full_path) - CHAR_LENGTH(REPLACE(full_path, ',', '')))/ CHAR_LENGTH(',')) \n" +
                "as maxNode from semantic.module_wise_fullpath where project_name = ? group by 1 order by 2 desc) a;\n";
        try {
            result = jdbcTemplate.queryForObject(query, Integer.class, param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching e2e business lineage max number details: {}", ex.getMessage());
        }
        return result;
    }

    public List<String> getE2EBusinessLineageDetails(String projectName) {
        List<String> details = new ArrayList<>();
        Object[] param = {projectName};
        String query = "select full_path from module_wise_fullpath where project_name = ? order by full_path;";
        try {
            details = jdbcTemplate.queryForList(query, String.class, param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching e2e business lineage details: {}", ex.getMessage());
        }
        return details;
    }

    public List<BusinessObjectDetails> getBusinessObjectDetails(String projectName) {
        List<BusinessObjectDetails> details = new ArrayList<>();
        Object[] param = {projectName};
        String query = "SELECT DISTINCT node_name, node_type, module_name FROM semantic.nodes_module_mapper " +
                "WHERE project_name = ? ORDER BY module_name;";
        try {
            details = jdbcTemplate.query(query, new BusinessObjectDetailsMapper(), param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching business lineage object details: {}", ex.getMessage());
        }
        return details;
    }

    public String getObjectTypeForObjectDetails(String objectName) {
        String object = null;
        Object[] param = {objectName};
        String query = "SELECT distinct object_type FROM semantic.db_objects WHERE object_name = ? limit 1";
        try {
            object = jdbcTemplate.queryForObject(query, String.class, param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching business lineage object type for object: {}", ex.getMessage());
        }
        return object;
    }

    public SpaceAndComplexity getSizeAndComplexityForObjectDetails(String objectName) {
        List<SpaceAndComplexity> list = new ArrayList<>();
        Object[] param = {objectName};
        String query = "SELECT distinct cast (round(space_used_by_table_mb, 2) as varchar ) as space, complexity FROM semantic.volumetric_info WHERE object_name = ? limit 1";
        try {
            list = jdbcTemplate.query(query, new SpaceAndComplexityMapper(), param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching business lineage size and complexity for object: {}", ex.getMessage());
        }
        return list.isEmpty() ? null : list.get(0);
    }

    public List<BusinessObjectNameDetails> getBusinessObjectNameDetails(String projectName) {
        List<BusinessObjectNameDetails> details = new ArrayList<>();
        Object[] param = {projectName};
        String query = "SELECT project_name, source_module, target_module,table_name,view_name,materialized_view_name,\n" +
                "user_defined_function_name,procedure_name,trigger_name,job_name\n" +
                "FROM semantic.module_wise_objects_name\n" +
                "WHERE project_name = ?\n" +
                "ORDER BY source_module";
        try {
            details = jdbcTemplate.query(query, new BusinessObjectNameDetailsMapper(), param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching business lineage object name details: {}", ex.getMessage());
        }
        return details;
    }

    public List<BusinessObjectCountDetails> getBusinessObjectCountDetails(String projectName) {
        List<BusinessObjectCountDetails> details = new ArrayList<>();
        Object[] param = {projectName};
        String query = "SELECT project_name,source_module,target_module,table_count,view_count,materialized_view_count, \n" +
                "user_defined_function_count,procedure_count,trigger_count,job_count \n" +
                "FROM semantic.module_wise_objects_count \n" +
                "WHERE project_name = ? \n" +
                "ORDER BY source_module";
        try {
            details = jdbcTemplate.query(query, new BusinessObjectCountDetailsMapper(), param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching business lineage object count details: {}", ex.getMessage());
        }
        return details;
    }

    public String getStatusOfGlobalExcel(String projectName) {
        String status = "";
        Object[] param = {projectName};
        String query = "select status from presentation.sprint_status where project_name = ? and function_type = 'global_excel_download'";
        try {
            status = jdbcTemplate.queryForObject(query, String.class, param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while fetching status of global excel dao: {}", ex.getMessage());
        }
        return status;
    }

    public Integer deleteGlobalExcelStatusForProject(String projectName) {
        String query = "delete from presentation.sprint_status where project_name = ? and function_type = 'global_excel_download'";
        Object[] param = {projectName};
        try {
            return jdbcTemplate.update(query, param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while deleting status of global excel dao: {}", ex.getMessage());
        }
        return Integer.MIN_VALUE;
    }

    public void insertGlobalExcelStatusIntoDB(String projectName, String status) {
        String query = "insert into presentation.sprint_status (project_name,function_type,status) values (?, 'global_excel_download', ?)";
        Object[] param = {projectName, status};
        try {
            jdbcTemplate.update(query, param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while inserting status of global excel dao: {}", ex.getMessage());
        }
    }

    public void updateGlobalExcelStatusIntoDB(String projectName, String status) {
        String query = "UPDATE presentation.sprint_status SET status = ? WHERE project_name = ? AND function_type = 'global_excel_download'";
        Object[] param = {status, projectName};
        try {
            jdbcTemplate.update(query, param);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while inserting status of global excel dao: {}", ex.getMessage());
        }
    }
}
