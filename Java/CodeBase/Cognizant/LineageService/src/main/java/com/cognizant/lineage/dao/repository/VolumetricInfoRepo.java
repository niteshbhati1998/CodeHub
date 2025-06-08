package com.cognizant.lineage.dao.repository;

import com.cognizant.lineage.dao.entity.VolumetricInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VolumetricInfoRepo extends CrudRepository<VolumetricInfo, String>{

    @Query("SELECT DISTINCT v.objectName from VolumetricInfo v WHERE UPPER(TRIM(v.dbType))= UPPER(TRIM(:databaseType))")
    List<String> findDistinctObjectNameByDatabaseType(@Param("databaseType") String databaseType);

    List<VolumetricInfo> findByObjectName(String objectName);

    @Modifying
    @Query("UPDATE VolumetricInfo v SET v.complexity= :complexity WHERE objectName= :objectName")
    void saveComplexity(@Param("objectName") String objectName, @Param("complexity") String complexity);
}