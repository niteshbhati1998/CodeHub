package com.cognizant.lineage.dao.repository;

import com.cognizant.lineage.dao.entity.SprintStatus;
import com.cognizant.lineage.pyspark.dao.repository.CrudRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SprintStatusRepository extends CrudRepository<SprintStatus, Long> {

    List<SprintStatus> findByProjectNameAndFunctionTypeOrderById(String projectName, String functionType);

    Optional<SprintStatus> findFirstByProjectNameAndFunctionTypeOrderByIdDesc(String projectName, String functionType);

    List<SprintStatus> findByFunctionTypeOrderById(String functionType);

    Optional<SprintStatus> findFirstByFunctionTypeOrderByIdDesc(String functionType);

    Optional<SprintStatus> findFirstByFunctionTypeAndExecutionStepOrderByIdDesc(String functionType, String executionStep);

    @Modifying
    @Transactional
    void deleteByFunctionType(String functionType);
}