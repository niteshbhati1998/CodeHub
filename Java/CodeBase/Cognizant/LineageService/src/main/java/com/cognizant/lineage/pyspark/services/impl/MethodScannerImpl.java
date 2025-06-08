package com.cognizant.lineage.pyspark.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cognizant.lineage.pyspark.dao.entity.MethodRecord;
import com.cognizant.lineage.pyspark.services.api.MethodScanner;
import com.cognizant.lineage.pyspark.util.PythonCodeTokenizer;

@Service
public class MethodScannerImpl implements MethodScanner {

    private String className;

    private String methodName;

    @Autowired
    private PythonCodeTokenizer tokenizer;

    @Override
    public MethodRecord scanLine(String line, String lineNos) {
        String trimmedLine = line.trim();

        if(trimmedLine.startsWith("class")) {
            this.className = trimmedLine.replace("class", "").replace(":", "").trim();
            return null;
        }

        boolean isMethodStatement = false;
        if(trimmedLine.startsWith("def")) {
            //remove def from the beginning
            isMethodStatement = true;
            trimmedLine = trimmedLine.replace("def ", "");
        }


        StringBuffer reconstructedLine = new StringBuffer();
        Map<String, String> stringReplacementMap = new HashMap<>();
        List<String> tokens = tokenizer.separateCodeAndEnclosedString(trimmedLine, reconstructedLine, stringReplacementMap);
        //String reconstructedLineWithReplacedStringWithOutSpace = reconstructedLine.toString().replace(" ", "");
        String codeToTokenize = reconstructedLine.toString();
        String leftSideCode = null;
        String rightSideCode = null;
        if(reconstructedLine.toString().indexOf("=") != -1) {
            leftSideCode = reconstructedLine.substring(0, reconstructedLine.toString().indexOf("=")).trim();
            rightSideCode = reconstructedLine.substring(reconstructedLine.toString().indexOf("=")+1).trim();
        }
        if(StringUtils.hasText(rightSideCode)) {
            codeToTokenize = rightSideCode;
        }
        List<String> splittedLine = tokenizer.convertStringToTokens(codeToTokenize, stringReplacementMap, new HashMap<>(), new HashMap<>());

        if(isMethodStatement) {
            this.methodName = splittedLine.get(0);
            return null;
        }

        for(String token : splittedLine) {
                String tokenString = token;
                if(tokenString.contains("SELECT ")) {
                    String selectQuery = StringUtils.trimTrailingCharacter(tokenString.substring(tokenString.indexOf("SELECT ")),'"');
                    return new MethodRecord(this.className, this.methodName,selectQuery, line, lineNos);
                }
        }

        return null;

    }
}
