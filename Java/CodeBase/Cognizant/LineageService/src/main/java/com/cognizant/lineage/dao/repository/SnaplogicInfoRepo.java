package com.cognizant.lineage.dao.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.cognizant.lineage.dao.entity.SnaplogicInfo;

public interface SnaplogicInfoRepo extends CrudRepository<SnaplogicInfo, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE SnaplogicInfo SET parameters = ?1, sqlStatement = ?2 WHERE jobId = ?3 AND source = ?4")
    void updateParametersAndSqlStatementForSource(String parameters, String sqlStatement, Long jobId, String source);
}
