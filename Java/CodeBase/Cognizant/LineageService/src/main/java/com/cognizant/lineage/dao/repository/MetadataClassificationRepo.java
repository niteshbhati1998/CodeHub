package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cognizant.lineage.dao.dto.ApplicationNameDetailsDto;
import com.cognizant.lineage.dao.dto.ObjectNameDto;
import com.cognizant.lineage.dao.entity.MetadataClassification;
import com.cognizant.lineage.upload.constants.QueryConstant;

public interface MetadataClassificationRepo extends CrudRepository<MetadataClassification, String> {

    @Query(nativeQuery = true,
            value = "select distinct node_name from presentation.nodes where project_name = :projectName order by 1;")
    List<String> findObjectNamesFromNodes(@Param("projectName") String projectName);

    @Query(nativeQuery = true, value = QueryConstant.METADATA_OBJECT_NAME_DETAILS_QUERY)
    List<ObjectNameDto> getObjectNameDetails(@Param("projectName") String projectName,
                                             @Param("objectName") String objectName);

    @Query(nativeQuery = true,
            value = "select distinct script_name from presentation.edges where project_name = :projectName order by 1;")
    List<String> findApplicationNamesFromEdges(@Param("projectName") String projectName);

    @Query(nativeQuery = true, value = QueryConstant.METADATA_APPLICATION_DETAILS_QUERY)
    List<ApplicationNameDetailsDto> fetchApplicationDetails(@Param("projectName") String projectName,
                                                            @Param("objectOrAppName") String objectOrAppName);

    @Query(nativeQuery = true, value = QueryConstant.INCOMING_NODES_OF_A_NODE)
    String getIncomingNodesOfANode(@Param("projectName") String projectName,
                                   @Param("scriptName") String scriptName,
                                   @Param("scriptType") String scriptType,
                                   @Param("sqlText") String sqlText,
                                   @Param("node") String node);

    @Query(nativeQuery = true, value = QueryConstant.OUTGOING_NODES_OF_A_NODE)
    String getOutgoingNodesOfANode(@Param("projectName") String projectName,
                                   @Param("scriptName") String scriptName,
                                   @Param("scriptType") String scriptType,
                                   @Param("sqlText") String sqlText,
                                   @Param("node") String node);
}
