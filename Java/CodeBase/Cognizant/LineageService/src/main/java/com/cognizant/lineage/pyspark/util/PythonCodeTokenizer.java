package com.cognizant.lineage.pyspark.util;

import static com.cognizant.lineage.pyspark.util.Constants.SPL_CHAR_REPLACE_PATTERN_1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import com.cognizant.lineage.pyspark.model.Element;


@Controller
public class PythonCodeTokenizer {

    public List<String> separateCodeAndEnclosedString(String eachLine, StringBuffer reconstructedString, Map<String, String> stringReplacementMap) {
        String actualLine = new String(eachLine);
        Matcher matcher;
        Pattern enclosedStringPattern = Pattern.compile(Constants.ENCLOSED_STRING_PATTERN);
        int stringReplacementSeq = 1;
        List<String> tokenList = new ArrayList<>(100);
        matcher = enclosedStringPattern.matcher(eachLine);
        StringBuffer restOfTheLine = new StringBuffer(eachLine);
        while (matcher.find()) {
            String foundString = matcher.group();
            String[] lineSplitedByFoundString = actualLine.split(Pattern.quote(foundString));
            String stringReplacementVariable = Constants.STRING_REPLACEMENT_VARIABLE + stringReplacementSeq;
            restOfTheLine = new StringBuffer();
            if (actualLine.startsWith(foundString)) {
                tokenList.add(stringReplacementVariable);
                String replaceRegex = Pattern.quote(foundString);
                stringReplacementMap.put(stringReplacementVariable, foundString.replaceAll("\"", ""));
                reconstructedString.append(" ").append(stringReplacementVariable);

                restOfTheLine.append(actualLine.replaceFirst(replaceRegex, ""));
            } else {
                //test i am his you find next you i am subhra
                stringReplacementMap.put(stringReplacementVariable, foundString.replaceAll("\"", ""));

                reconstructedString.append(" ").append(lineSplitedByFoundString[0]);
                reconstructedString.append(" ").append(stringReplacementVariable);

                tokenList.add(lineSplitedByFoundString[0]);
                tokenList.add(stringReplacementVariable);

                String replaceRegex = Pattern.quote(lineSplitedByFoundString[0] + foundString);
                restOfTheLine.append(actualLine.replaceFirst(replaceRegex, ""));

            }
            if (StringUtils.hasText(restOfTheLine.toString())) {
                actualLine = restOfTheLine.toString();
                matcher = enclosedStringPattern.matcher(restOfTheLine.toString());
            }
            stringReplacementSeq++;
        }
        reconstructedString.append(" ").append(restOfTheLine);
        tokenList.add(restOfTheLine.toString());
        return tokenList;
    }

    public List<String> convertStringToTokens(String lineWithoutSpaces, Map<String, String> stringReplacementMap,
                                              Map<String, String > stringVariableMap, Map<String, Element> variableMap) {
        List<String> splittedLine = new ArrayList<>(100);
        String[] allTokens = lineWithoutSpaces.replaceAll(SPL_CHAR_REPLACE_PATTERN_1, ",").split(",");
        for (String token : allTokens) {
            if (!StringUtils.hasText(token)) {
                continue;
            }

            String[] splitedString = token.split("\\+");


            StringBuffer calculatedToken = new StringBuffer();
            if (splitedString.length > 1) {
                for (String str : splitedString) {
                    replaceWithStringValue(stringReplacementMap, stringVariableMap, new HashMap<>(), calculatedToken, str);
                }
            } else {
                replaceWithStringValue(stringReplacementMap, stringVariableMap, variableMap, calculatedToken, token);

            }
            splittedLine.add(calculatedToken.toString().trim());

        }
        return splittedLine;
    }

    private void replaceWithStringValue(Map<String, String> stringReplacementMap, Map<String, String> stringVariableMap, Map<String, Element> variableMap, StringBuffer calculatedToken, String str) {
        if (stringReplacementMap.containsKey(str)) {
            calculatedToken.append(stringReplacementMap.get(str));
        } else {
            for (String str2 : str.split(" ")) {
                if(stringReplacementMap.containsKey(str2.trim())) {
                    calculatedToken.append(stringReplacementMap.get(str2.trim())).append(" ");
                } else if (stringVariableMap.containsKey(str2.trim())) {
                    calculatedToken.append(stringVariableMap.get(str2.trim())).append(" ");
                } else {
                    calculatedToken.append(str2).append(" ");
                }
            }
        }
    }
}
