package com.cognizant.lineage.dao.repository;

import com.cognizant.lineage.dao.entity.ObjectCalculationDetails;

import java.util.List;

public interface ObjectCalculationDetailsRepo extends CrudRepository<ObjectCalculationDetails, Integer> {
    List<ObjectCalculationDetails> findByDbType(String dbType);
}