package com.cognizant.lineage.pyspark.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.pyspark.dao.entity.MethodRecord;
import com.cognizant.lineage.pyspark.dao.repository.MethodRecordRepository;
import com.cognizant.lineage.pyspark.model.Element;
import com.cognizant.lineage.pyspark.model.SourceDestinationType;
import com.cognizant.lineage.pyspark.services.api.SourceDestinationScanner;
import com.cognizant.lineage.pyspark.util.Constants;
import com.cognizant.lineage.pyspark.util.MutableInteger;
import com.cognizant.lineage.pyspark.util.PythonCodeTokenizer;

import jakarta.annotation.PostConstruct;

@Service
public class SourceDestinationScannerImpl implements SourceDestinationScanner {

    private Logger LOGGER = LogManager.getLogger(SourceDestinationScannerImpl.class);

    @Autowired
    Environment environment;
    private List<String> writeMethodNames;

    private List<String> readMethodNames;

    private List<String> specialClassTokens;

    @Value("${skipKeyWordsPattern}")
    private String skipKeyWordsPattern;

    @Autowired
    private PythonCodeTokenizer tokenizer;

    @Autowired
    private MethodRecordRepository methodRecordRepository;

    @Autowired
    private SQLQueryParser sqlQueryParser;

    private Map<String, Map<String, String>> classMethodList = new HashMap<>();

    private Map<String, Map<String, SourceDestinationType>> readMethodMap;
    private Map<String, Map<String, SourceDestinationType>> writeMethodMap;

    private Map<String, Map<String, String>> specialClassTokensMap;

    private Map<String, String> specialObjectClassMap;

    private Map<String, String> objectClassMap;


    public void reset() {
        objectClassMap = new HashMap<>();
    }
    public SourceDestinationScannerImpl(@Value("${writeMethodNames}") List<String> writeMethodNames,
                                        @Value("${readMethodNames}") List<String> readMethodNames,
                                        @Value("${specialClassTokens}") List<String> specialClassTokens) {
        this.writeMethodNames = writeMethodNames;
        this.readMethodNames = readMethodNames;
        this.specialClassTokens = specialClassTokens;

        readMethodMap = readMethodNames.stream().map(readMethodName -> {
            String[] methodNameAndDetails = readMethodName.split("-");

            Map<String, Map<String, SourceDestinationType>> m = new HashMap();
            Map<String, SourceDestinationType> typeParamMap = new HashMap<>();

            for (String methodDetail : methodNameAndDetails[1].split(":")) {

                String[] typeAndParams = methodDetail.split("~");

                if (typeAndParams.length == 2) {
                    typeParamMap.put(typeAndParams[0], getSourceDestinationType(typeAndParams[1]));
                } else {
                    typeParamMap.put(typeAndParams[0], getSourceDestinationType(null));
                }
            }
            m.put(methodNameAndDetails[0], typeParamMap);
            return m;


        }).collect(HashMap::new, Map::putAll, Map::putAll);

        writeMethodMap = writeMethodNames.stream().map(writeMethodName -> {
            String[] methodNameAndDetails = writeMethodName.split("-");

            Map<String, Map<String, SourceDestinationType>> m = new HashMap();
            Map<String, SourceDestinationType> typeParamMap = new HashMap<>();

            for (String methodDetail : methodNameAndDetails[1].split(":")) {

                String[] typeAndParams = methodDetail.split("~");

                if (typeAndParams.length == 2) {
                    typeParamMap.put(typeAndParams[0], getSourceDestinationType(typeAndParams[1]));
                } else {
                    typeParamMap.put(typeAndParams[0], getSourceDestinationType(null));
                }
            }
            m.put(methodNameAndDetails[0], typeParamMap);
            return m;
        }).collect(HashMap::new, Map::putAll, Map::putAll);

        specialClassTokensMap = specialClassTokens.stream().map(specialClassToken -> {
            String[] specialClassDetails = specialClassToken.split("-");
            Map<String, Map<String, String>> m = new HashMap();
            Map<String, String> methodparamMap = new HashMap<>();

            for(String details : specialClassDetails[1].split(":")) {
                String[] methodAndParams = details.split("~");

                if(methodAndParams.length == 2) {
                    methodparamMap.put(methodAndParams[0], methodAndParams[1]);
                } else {
                    methodparamMap.put(methodAndParams[0], null);
                }
            }

            m.put(specialClassDetails[0], methodparamMap);
            return m;
        }).collect(HashMap::new, Map::putAll, Map::putAll);


        objectClassMap = new HashMap<>();

    }

    private SourceDestinationType getSourceDestinationType(String type) {
        if("table".equals(type)) {
            return SourceDestinationType.TABLE;
        } else if( "file".equals(type)) {
            return SourceDestinationType.FILE;
        }
        return SourceDestinationType.TABLE;
    }

    @PostConstruct
    public void init() {
        Iterable<MethodRecord> methodRecords = methodRecordRepository.findAll();

        for(MethodRecord methodRecord : methodRecords) {
            if(!classMethodList.containsKey(methodRecord.getClassName())) {
                Map<String, String> methodQueryMap = new HashMap<>();
                methodQueryMap.put(methodRecord.getMethodName(), methodRecord.getQuery());
                classMethodList.put(methodRecord.getClassName(), methodQueryMap);
            } else {
                classMethodList.get(methodRecord.getClassName()).put(methodRecord.getMethodName(), methodRecord.getQuery());
            }
        }
    }

    @Override
    public void scanLine(String line, String lineNos, Map<String, String> stringVariableMap,
                         Map<String, String> overrideVariableMap, Map<String, Element> variableNameMap,
                         MutableInteger varCount, List<Element> foundElements, MutableInteger elementCount) {

        String eachLineWithoutModification = line.trim();

        if(eachLineWithoutModification.endsWith(":")) {
            return;
        }

        Pattern linePattern = Pattern.compile(skipKeyWordsPattern, Pattern.MULTILINE);
        Matcher matcher = linePattern.matcher(line);
        if (matcher.find()) {
            return;
        }

        String[] leftRightArray = line.split("=");
        String leftLine = "";
        String rightLine = "";
        if (leftRightArray.length > 1) {
            leftLine = leftRightArray[0].trim();
            for (int i = 1; i < leftRightArray.length; i++) {
                rightLine = rightLine + leftRightArray[i].trim();
            }
        } else {
            leftLine = leftRightArray[0].trim();
        }
        //find string variable
        List<String> splittedLine = new ArrayList<>();
        if (!rightLine.equals("")) {

            if (rightLine.startsWith("config.get")) {
                rightLine = rightLine.replace("config.get", "").replace("(", "").replace(")", "").replace("\"", "").trim();
                stringVariableMap.put(leftLine.trim(), rightLine.trim());
                return;
            } else {
                StringBuffer reconstructedLineWithReplacedString = new StringBuffer();
                Map<String, String> stringReplacementMap = new HashMap<>();
                List<String> rightHandTokens = tokenizer.separateCodeAndEnclosedString(rightLine, reconstructedLineWithReplacedString, stringReplacementMap);
                if (StringUtils.isNoneBlank(rightLine) && reconstructedLineWithReplacedString.toString().equals("")) {
                    reconstructedLineWithReplacedString = new StringBuffer(rightLine);
                }
                //String reconstructedLineWithReplacedStringWithOutSpace = reconstructedLineWithReplacedString.toString().replace(" ", "");
                StringBuffer reConstructedString = new StringBuffer();

                String[] rightSideTokensSplitByPlus = reconstructedLineWithReplacedString.toString().split("\\+");
                boolean isString = true;
                if (rightSideTokensSplitByPlus.length == 1 && stringReplacementMap.containsKey(rightSideTokensSplitByPlus[0].trim())) {
                    stringVariableMap.put(leftLine, stringReplacementMap.get(rightSideTokensSplitByPlus[0].trim()).replaceAll("\"",""));
                    reConstructedString.append(stringReplacementMap.get(rightSideTokensSplitByPlus[0].trim()));
                } else {
                    for (String eachToken : rightSideTokensSplitByPlus) {
                        if (eachToken.startsWith(Constants.STRING_REPLACEMENT_VARIABLE) && stringReplacementMap.containsKey(eachToken.trim())) {
                            reConstructedString.append(stringReplacementMap.get(eachToken.trim()));
                        } else if (stringVariableMap.containsKey(eachToken.trim())) {
                            reConstructedString.append(stringVariableMap.get(eachToken.trim()));
                        } else {
                            isString = false;
                            reConstructedString.append(eachToken.trim());
                        }
                    }
                    if (isString) {
                        stringVariableMap.put(leftLine, reConstructedString.toString().trim());
                    }
                }
                splittedLine = tokenizer.convertStringToTokens(reconstructedLineWithReplacedString.toString(), stringReplacementMap, stringVariableMap, variableNameMap);

                if(splittedLine.size() > 0 && classMethodList.containsKey(splittedLine.get(0))) {
                    for(String var: leftLine.split(",")) {
                        objectClassMap.put(var.trim(), splittedLine.get(0));
                    }
                    return;
                }

                List<String> dummyList = new ArrayList<>();
                for(String ln : splittedLine) {
                    String methodQuery = null;
                    if(objectClassMap.containsKey(ln)) {
                        String nextToken = splittedLine.get(splittedLine.indexOf(ln) + 1);
                        Map<String, String> classMethodMap = classMethodList.get(objectClassMap.get(ln));
                        if(null != classMethodMap) {
                            methodQuery = classMethodMap.get(nextToken);
                        }
                    }
                    if(methodQuery != null) {
                        dummyList.add(methodQuery);
                    }
                }
                if(dummyList.size() > 0) {
                    splittedLine = dummyList;
                }
            }
        } else {
            StringBuffer reconstructedLineWithReplacedString = new StringBuffer();
            Map<String, String> stringReplacementMap = new HashMap<>();
            List<String> leftHandTokens = tokenizer.separateCodeAndEnclosedString(leftLine, reconstructedLineWithReplacedString, stringReplacementMap);
            String reconstructedLineWithReplacedStringWithOutSpace = reconstructedLineWithReplacedString.toString().replace(" ", "");


            //List<String> copyOfRightHandTokens = new ArrayList<>(rightHandTokens);

            splittedLine = tokenizer.convertStringToTokens(reconstructedLineWithReplacedStringWithOutSpace, stringReplacementMap, stringVariableMap, variableNameMap);
        }

        StringBuffer concatenatedString = new StringBuffer();

        for (String str : splittedLine) {
            if (stringVariableMap.containsKey(str.trim())) {
                concatenatedString.append(stringVariableMap.get(str.trim())).append(Constants.SPACE);
            }
        }


        List<String> allRightSideTokens = splittedLine;

        specialObjectClassMap = new HashMap<>();

        //StringBuffer variableName;
        if (!rightLine.equals("")) {
            for(String var: leftLine.trim().split(",")) {
                StringBuffer variableName = new StringBuffer(var.trim());
                //allRightSideTokens.remove(0);


                allRightSideTokens = allRightSideTokens.stream().map(token -> {
                    if (overrideVariableMap.containsKey(token.trim())) {
                        return overrideVariableMap.get(token.trim());
                    } else {
                        return token.trim();
                    }
                }).collect(Collectors.toList());

                List<String> copyOfAllRightSideTokens = new ArrayList<>(allRightSideTokens);

                //processSpecialClassTokens(allRightSideTokens, leftLine, varCount);

                if (!allRightSideTokens.isEmpty() && StringUtils.containsAnyIgnoreCase(allRightSideTokens.get(0).toString(),
                        Constants.SQL_KEY_WORDS)) {
                    /*if (variableNameMap.containsKey(variableName.toString())) {
                        variableName.append("_").append(varCount++);
                    }********/
                    String queryString = allRightSideTokens.get(0).toString();
                    //String query = allRightSideTokens.get(0).substring(queryString.indexOf("SELECT "), queryString.contains("\"")? queryString.lastIndexOf("\""): queryString.length());
                    String query = allRightSideTokens.get(0).substring(StringUtils.indexOfAny(queryString.toUpperCase(), Constants.SQL_KEY_WORDS), queryString.contains("\"")? queryString.lastIndexOf("\""): queryString.length());
                    if (variableNameMap.containsKey(variableName.toString())) {
                        overrideVariableMap.put(variableName.toString(), variableName.append("_override_").append(varCount.increase()).toString());
                        //varCount.setValue(varCount.getValue() + 1);
                        //allRightSideTokens.stream().filter(token -> token.equals(oldVariableName)).map(token ->newVariableName);
                    }
                    Map<String, Collection> sourcesAndDestinations = sqlQueryParser.parseBteqScriptIncludingSelectScenrio(query);
                    Collection sources = sourcesAndDestinations.get("sources");
                    Element element = Element.builder().name(variableName.toString()).elementId("Element_"+elementCount.increase())
                            .type(SourceDestinationType.TABLE).level(0).lineNos(lineNos)
                            .actualCode(eachLineWithoutModification)
                            .query(query)
                            .list(new ArrayList<>(sources)).build();

                    allRightSideTokens.stream().forEach(token -> {
                        if (readMethodMap.containsKey(token.trim())) {
                            if (variableNameMap.containsKey(variableName.toString())) {
                                overrideVariableMap.put(variableName.toString(), variableName.append("_override_").append(varCount.increase()).toString());
                                //varCount.setValue(varCount.getValue() + 1);
                                //allRightSideTokens.stream().filter(token -> token.equals(oldVariableName)).map(token ->newVariableName);
                            }
                            Element nextElement = Element.builder().elementId("Element_"+elementCount.increase()).name(variableName.toString()).type(SourceDestinationType.DATAFRAME).level(element.getLevel() + 1).lineNos(lineNos).actualCode(eachLineWithoutModification)
                                    .build();
                            element.getNextElements().add(nextElement);
                            //foundElements.add(nextElement);
                            variableNameMap.put(variableName.toString(), nextElement);


                            element.setName("miscellaneous");
                        }
                    });
                    foundElements.add(element);
                    variableNameMap.put(element.getName(), element);

                } else {
                    allRightSideTokens.forEach(token -> {
                        if (readMethodMap.keySet().contains(token.trim())) {
                            Map<String, SourceDestinationType> methodParamTypeMap = readMethodMap.get(token.trim());
                            Set<String> readMethodParams = methodParamTypeMap.keySet();
                            if (variableNameMap.containsKey(variableName.toString())) {
                                overrideVariableMap.put(variableName.toString(), variableName.append("_override_").append(varCount.increase()).toString());
                                //varCount.setValue(varCount.getValue() + 1);
                                //allRightSideTokens.stream().filter(token -> token.equals(oldVariableName)).map(token ->newVariableName);
                            }
                            Element newElement = Element.builder().elementId("Element_"+elementCount.increase()).name(variableName.toString()).type(SourceDestinationType.DATAFRAME).lineNos(lineNos).actualCode(eachLineWithoutModification)
                                    .build();
                            if (readMethodParams.size() == 0) {
                                List<String> selectQueryList = copyOfAllRightSideTokens.stream().filter(t -> StringUtils.containsAny(t.toUpperCase(), Constants.SQL_KEY_WORDS)).collect(Collectors.toList());
                                if (!selectQueryList.isEmpty()) {
                                    String newTableElementName = "miscellaneous_" + varCount.increase();
                                    Element newTableElement = Element.builder().elementId("Element_"+elementCount.increase()).name(newTableElementName).type(SourceDestinationType.TABLE).lineNos(lineNos).actualCode(eachLineWithoutModification)
                                            .build();
                                    newTableElement.setQuery(selectQueryList.get(0).substring(StringUtils.indexOfAny(selectQueryList.get(0).toUpperCase(),Constants.SQL_KEY_WORDS)));
                                    newTableElement.getNextElements().add(newElement);
                                    variableNameMap.put(newTableElementName, newTableElement);
                                    foundElements.add(newTableElement);
                                } else {
                                    List<Element> elements = copyOfAllRightSideTokens.stream().filter(t -> variableNameMap.containsKey(t.trim())).map(t -> variableNameMap.get(t.trim())).collect(Collectors.toList());
                                    boolean isFound = false;
                                    for (Element e : elements) {
                                        isFound = true;
                                        e.getNextElements().add(newElement);
                                    }
                                    if (!isFound) {
                                        if (StringUtils.isBlank(newElement.getQuery())) {
                                            newElement.getList().add("NOT DETERMINED");
                                        } else {
                                            foundElements.add(newElement);
                                        }
                                    }
                                }
                                //TODO
                            }
                            for (String readMethodParam : readMethodParams) {
                                if (readMethodParam.trim().equals("")) {
                                    List<Element> elements = copyOfAllRightSideTokens.stream().filter(t -> variableNameMap.containsKey(t.trim())).map(t -> variableNameMap.get(t.trim())).collect(Collectors.toList());
                                    if (elements.size() == 0) {
                                        StringBuffer queryVariables = new StringBuffer();
                                        stringVariableMap.forEach((k,v) -> {
                                            for (String copyToken : copyOfAllRightSideTokens) {
                                                if (copyToken.contains("SELECT ") && copyToken.trim().equals(v.trim())) {
                                                    queryVariables.append(k.trim() + ",");
                                                }
                                            }
                                        });

                                        if(!queryVariables.toString().isEmpty()) {
                                            String[] queryVariableArray = queryVariables.toString().split(",");
                                            for(String queryVariable : queryVariableArray) {
                                                if (overrideVariableMap.containsKey(queryVariable.toString())) {
                                                    Element el = variableNameMap.get(overrideVariableMap.get(queryVariable.toString()));
                                                    if (el != null) {
                                                        elements.add(el);
                                                    }
                                                } else {
                                                    Element el = variableNameMap.get(queryVariable.toString());
                                                    elements.add(el);
                                                }
                                            }
                                        }
                                    }
                                    for (Element e : elements) {
                                        e.getNextElements().add(newElement);
                                    }
                                    //TODO
                                } else {
                                    try {
                                        Integer integer = Integer.parseInt(readMethodParam);
                                        String elementName = copyOfAllRightSideTokens.get(integer - 1);
                                        Element foundElement = variableNameMap.get(elementName);
                                        if(methodParamTypeMap.get(readMethodParam).name().equals(SourceDestinationType.FILE.name())) {
                                            newElement.setType(SourceDestinationType.FILE);
                                            if (stringVariableMap.containsKey(elementName.trim())) {
                                                newElement.getList().add(stringVariableMap.get(elementName.trim()));
                                            } else {
                                                newElement.getList().add(elementName.trim());
                                            }
                                            foundElements.add(newElement);
                                        } else {
                                            if (foundElement != null) {
                                                newElement.setLevel(foundElement.getLevel() + 1);
                                                foundElement.getNextElements().add(newElement);
                                            } else {
                                                boolean isSelectQueryFound = false;
                                                String selectQuery = null;
                                                if (stringVariableMap.containsKey(elementName.trim())) {
                                                    String elementValue = stringVariableMap.get(elementName.trim());
                                                    if (StringUtils.containsAny(elementValue.toUpperCase(), Constants.SQL_KEY_WORDS)) {
                                                        //newElement.setQuery(elementValue.substring(elementValue.indexOf("SELECT ")));
                                                        selectQuery = elementValue.substring(StringUtils.indexOfAny(elementValue.toUpperCase(), Constants.SQL_KEY_WORDS));
                                                        isSelectQueryFound = true;
                                                    }
                                                } else if (StringUtils.containsAny(elementName.toUpperCase(), Constants.SQL_KEY_WORDS)) {
                                                    //newElement.setQuery(elementName.substring(elementName.indexOf("SELECT ")));
                                                    selectQuery = elementName.substring(StringUtils.indexOfAny(elementName.toUpperCase(), Constants.SQL_KEY_WORDS));
                                                    isSelectQueryFound = true;
                                                }
                                                if (isSelectQueryFound) {
                                                    String newTableElementName = "miscellaneous_" + varCount;
                                                    varCount.increase();
                                                    Element newTableElement = Element.builder().elementId("Element_" + elementCount.increase()).name(newTableElementName).type(SourceDestinationType.TABLE).lineNos(lineNos).actualCode(eachLineWithoutModification)
                                                            .build();
                                                    newTableElement.setQuery(selectQuery);
                                                    newTableElement.getNextElements().add(newElement);
                                                    variableNameMap.put(newTableElementName, newTableElement);
                                                    foundElements.add(newTableElement);
                                                } else {
                                                    newElement.setType(SourceDestinationType.TABLE);
                                                    newElement.getList().add("NOT DETERMINED");
                                                    foundElements.add(newElement);
                                                }
                                            }
                                        }

                                    } catch (Exception e) {
                                        if (copyOfAllRightSideTokens.indexOf(readMethodParam.trim()) != -1) {
                                            
                                            String elementName = copyOfAllRightSideTokens.get(copyOfAllRightSideTokens.indexOf(readMethodParam.trim()) + 1);
                                            Element foundElement = variableNameMap.get(elementName.trim());

                                            if(methodParamTypeMap.get(readMethodParam).name().equals(SourceDestinationType.FILE.name())) {
                                                newElement.setType(SourceDestinationType.FILE);
                                                if (stringVariableMap.containsKey(elementName.trim())) {
                                                    newElement.getList().add(stringVariableMap.get(elementName.trim()));
                                                } else {
                                                    newElement.getList().add(elementName.trim());
                                                }
                                                foundElements.add(newElement);
                                            } else {

                                                if (foundElement != null) {
                                                    newElement.setLevel(foundElement.getLevel() + 1);
                                                    foundElement.getNextElements().add(newElement);
                                                } else {
                                                    //TODO need to check properly also need to add code for select
                                                    newElement.setType(SourceDestinationType.TABLE);
                                                    newElement.getList().add("NOT DETERMINED");
                                                    foundElements.add(newElement);
                                                    //TODO unknown element
                                                }
                                            }
                                        } else {
                                            if(methodParamTypeMap.get(readMethodParam).name().equals(SourceDestinationType.FILE.name())) {
                                                newElement.setType(SourceDestinationType.FILE);
                                            } else {
                                                newElement.setType(SourceDestinationType.TABLE);
                                            }
                                            newElement.getList().add("NOT DETERMINED");
                                            foundElements.add(newElement);
                                        }
                                    }

                                }
                            }
                            //foundElements.add(newElement);
                            variableNameMap.put(variableName.toString(), newElement);
                        } if (writeMethodMap.keySet().contains(token.trim())) {
                            processTokenForWriteFunction(lineNos, variableNameMap, eachLineWithoutModification, varCount, copyOfAllRightSideTokens, token, elementCount);
                        } else if (variableNameMap.containsKey(token.trim())) {
                            //&& !variableNameMap.containsKey(variableName.toString()

                            Element foundElement = variableNameMap.get(token.trim());
                            //if(!foundElement.getNextElements().stream().anyMatch(ele -> ele.getName().equals(variableName.toString()))) {
                            if (!variableNameMap.containsKey(variableName.toString())) {
                                if (variableNameMap.containsKey(variableName.toString())) {
                                    overrideVariableMap.put(variableName.toString(), variableName.append("_override_").append(varCount.increase()).toString());
                                    //varCount.setValue(varCount.getValue() + 1);
                                    //allRightSideTokens.stream().filter(token -> token.equals(oldVariableName)).map(token ->newVariableName);
                                }
                                Element element = Element.builder().elementId("Element_"+elementCount.increase()).name(variableName.toString()).type(SourceDestinationType.DATAFRAME).level(foundElement.getLevel() + 1).lineNos(lineNos).actualCode(eachLineWithoutModification)
                                        .build();
                                foundElement.getNextElements().add(element);

                                variableNameMap.put(variableName.toString(), element);

                            } else {
                                if (!foundElement.getNextElements().stream().anyMatch(ele -> ele.getName().equals(variableName.toString()))) {
                                    if (!foundElement.getName().trim().equals(variableName.toString().trim())) {
                                        foundElement.getNextElements().add(variableNameMap.get(variableName.toString()));
                                    } else {
                                        System.out.println("selfffffffffff");
                                    }
                                }
                            }
                        }
                    });
                }
            }
        } else {
            processWriteFunction(lineNos, variableNameMap, eachLineWithoutModification, allRightSideTokens, varCount, elementCount);
        }
    }


    private void processSpecialClassTokens(List<String> allRightSideTokens, String leftLine, Map<String, String> overrideVariableMap, Map<String, Element> variableNameMap, MutableInteger varCount) {
        List<String> copyOfaAllRightSideTokens = new ArrayList<>(allRightSideTokens);
        for(String token : allRightSideTokens) {
            if(specialClassTokensMap.containsKey(token)) {
                String[] leftVars = leftLine.split(",");
                for(String leftVar: leftVars) {
                    specialObjectClassMap.put(leftVar.trim(), token);
                }
            } else {
                if(specialObjectClassMap.containsKey(token)) {
                    String specialObjectName = token;
                    int specialObjectIndex = copyOfaAllRightSideTokens.indexOf(token);
                    String specialClassName = specialObjectClassMap.get(token);
                    String nextToken = copyOfaAllRightSideTokens.get(specialObjectIndex + 1);

                    Map<String, String> classMethodDetails = specialClassTokensMap.get(specialClassName);
                    if(classMethodDetails.containsKey(nextToken)) {
                        String methodParamNoOrNameAndType = classMethodDetails.get(nextToken);
                        String[] methodParamNoOrNameAndTypeSplit = methodParamNoOrNameAndType.split("$");
                        String methodParamNoOrName = methodParamNoOrNameAndTypeSplit[0];
                        SourceDestinationType type = getSourceDestinationType(methodParamNoOrNameAndTypeSplit[1]);
                        String param = "NOT FOUND";
                        try {
                            int paramNo = Integer.parseInt(methodParamNoOrName);
                            param = copyOfaAllRightSideTokens.get(specialObjectIndex + 1 + paramNo);
                        } catch (NumberFormatException e) {
                            String paramName = methodParamNoOrName;
                            int paramNameIndex = copyOfaAllRightSideTokens.indexOf(paramName);
                            if(paramNameIndex != -1) {
                                param = copyOfaAllRightSideTokens.get(paramNameIndex + 1);
                            }
                        }
                        String[] variableNames = null;
                        if (StringUtils.isEmpty(leftLine)) {
                            if(copyOfaAllRightSideTokens.contains("if") || copyOfaAllRightSideTokens.contains("for") ||
                                    copyOfaAllRightSideTokens.contains("while")) {
                                variableNames = new String[]{copyOfaAllRightSideTokens.get(1)};
                            } else {
                                variableNames = new String[]{token};
                            }
                        } else {
                            variableNames = leftLine.split(",");
                        }
                        //createSourceDestinationElement(token, param, type, methodParamNoOrNameAndTypeSplit[2], variableNames, overrideVariableMap, variableNameMap, varCount);
                    }
                }
            }
        }
    }

    private void processWriteFunction(String lineNos, Map<String, Element> variableNameMap, String eachLineWithoutModification, List<String> allRightSideTokens, MutableInteger writeCounter, MutableInteger countElement) {
        List<String> tempAllRightSideTokens = new ArrayList<>(allRightSideTokens);
        allRightSideTokens.stream().forEach(token -> {
            if (writeMethodMap.keySet().contains(token.trim())) {
                processTokenForWriteFunction(lineNos, variableNameMap, eachLineWithoutModification, writeCounter, tempAllRightSideTokens, token, countElement);
            }
        });
    }

    private void processTokenForWriteFunction(String lineNos, Map<String, Element> variableNameMap, String eachLineWithoutModification, MutableInteger writeCounter, List<String> tempAllRightSideTokens, String token, MutableInteger countElement) {
        String variableName = "INSERT_" + writeCounter.getValue();
        writeCounter.increase();
        Element element = Element.builder().elementId("Element_"+countElement.increase()).name(variableName).type(SourceDestinationType.TABLE).lineNos(lineNos).actualCode(eachLineWithoutModification)
                .build();
        variableNameMap.put(variableName, element);
        Map<String, SourceDestinationType> writeMethodParamTypeMap = writeMethodMap.get(token.trim());
        for (String writeMethodParam : writeMethodParamTypeMap.keySet()) {
            SourceDestinationType destinationType = writeMethodParamTypeMap.get(writeMethodParam.trim());
            element.setType(destinationType);
            if (writeMethodParam.trim().equals("")) {
                element.getList().add("NOT FOUND");
            } else {
                try {
                    int parameterNo = Integer.parseInt(writeMethodParam);
                    int indexOfToken = tempAllRightSideTokens.indexOf(token.trim());
                    String elementName = tempAllRightSideTokens.get(indexOfToken + parameterNo);
                    element.getList().add(elementName);
                    break;
                } catch (Exception e) {
                    if (tempAllRightSideTokens.indexOf(writeMethodParam) != -1) {
                        String elementName = tempAllRightSideTokens.get(tempAllRightSideTokens.indexOf(writeMethodParam) + 1);
                        element.getList().add(elementName);
                        break;
                    } else {
                        element.getList().add("NOT FOUND");
                    }
                }
            }
        }

        variableNameMap.keySet().stream().forEach(var -> {
            if (tempAllRightSideTokens.contains(var)) {
                Element ele = variableNameMap.get(var);
                ele.getNextElements().add(element);
            }
        });
    }
}