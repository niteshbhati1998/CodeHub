package com.cognizant.lineage.pyspark.services.api;

import com.cognizant.lineage.pyspark.dao.entity.MethodRecord;

public interface MethodScanner {
    MethodRecord scanLine(String line, String lineNos);
}
