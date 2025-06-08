package com.cognizant.lineage.pyspark.services.api;

import java.util.List;
import java.util.Map;

import com.cognizant.lineage.pyspark.model.Element;
import com.cognizant.lineage.pyspark.util.MutableInteger;

public interface SourceDestinationScanner {
    void scanLine(String line, String lineNos, Map<String, String> stringVariableMap,
                  Map<String, String> overrideVariableMap, Map<String, Element> variableNameMap,
                  MutableInteger varCount, List<Element> foundElements, MutableInteger elementCount);

    void reset();

}
