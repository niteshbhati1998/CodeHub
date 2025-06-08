package com.cognizant.lineage.dao.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cognizant.lineage.dao.entity.InformaticaSource;
import com.cognizant.lineage.upload.constants.QueryConstant;
import com.cognizant.lineage.upload.model.ComplexityQueryDto;

public interface InformaticaSourceRepo extends CrudRepository<InformaticaSource, Long>{

    @Query("SELECT DISTINCT i.fileName FROM InformaticaSource i WHERE i.jobId= :jobId")
    Set<String> getDistinctFilename(@Param("jobId") Long jobId);

    @Query("SELECT sqlText FROM InformaticaSource WHERE jobId= :jobId AND fileName= :fileName")
    List<String> findSqlTextByJobIdAndFileName(Long jobId, String fileName);

    @Query(value = QueryConstant.INFORMATICA_COMPLEXITY_QUERY, nativeQuery = true)
    List<ComplexityQueryDto> executeComplexitySuperQuery(@Param("jobId") Long jobId, @Param("fileName") String fileName);

    @Query(value = "SELECT DISTINCT file_name FROM infa_component_level_lineage WHERE job_id= :jobId", nativeQuery = true)
    Set<String> getDistinctFilenameFromComponentTable(@Param("jobId") Long jobId);
}
