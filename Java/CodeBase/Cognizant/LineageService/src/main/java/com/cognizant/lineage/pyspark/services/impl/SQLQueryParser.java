package com.cognizant.lineage.pyspark.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Service;

@Service
public class SQLQueryParser {

    private org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SQLQueryParser.class);
    public Map<String,Collection> parseBteqScriptIncludingSelectScenrio(String sqlText) {
        Map<String, Collection> hm  = new HashMap();
        try {
            List<String> unwantedKeyWordsListForSymbol = new ArrayList();
            List<String> unwantedKeyWordsListForWords = new ArrayList();
            List<String> anotherList1 = Arrays.asList("<",">","(",")",",","=","*");
            List<String> anotherList2 = Arrays.asList("'Q'","THE","END","AND","IN","ON","BUT","SET","FOR","NOT","WITH","CASE","THEN","WHEN","LEFT","FROM","ORDER","GROUP","SELECT","WHERE","SELECT","COLLECT","TIMESTAMP","QUALIFY","HAVING","MINUS","INNER","TABLE","INSERT","CREATE","MISSING");
            unwantedKeyWordsListForSymbol.addAll(anotherList1);
            unwantedKeyWordsListForWords.addAll(anotherList2);

            HashSet<String> sourceList = new HashSet();
            HashSet<String> targetList = new LinkedHashSet();

            sqlText = sqlText.replaceAll(","," , ");
            sqlText = sqlText.replaceAll(";"," ;");
            sqlText = sqlText.replaceAll("[\\t\\n\\r]+"," ");
            sqlText = sqlText.replaceAll("\\(select", "( select");
            sqlText = sqlText.replaceAll("\\(SELECT", "( SELECT");
            sqlText = sqlText.replaceAll(" as ", " ");
            sqlText = sqlText.replaceAll(" AS ", " ");
            sqlText = sqlText.replaceAll(" +", " ");
            sqlText = sqlText.trim();
            String[] wordsArr = sqlText.split(" ");

            int wordCount =0;
            Boolean isMerge = false;
            Boolean isTargetForInsMerDel = false;
            Boolean isTargetForUpdate = false;
            Boolean isTargetForCreate = false;
            Boolean findCreateTable = false;
            Boolean isTargetForCreateSetMultiGlobalVol = false;
            Boolean isTargetForCreateSetMultiGlobalVolFinal = false;

            if(wordsArr[0].trim().equalsIgnoreCase("INSERT") || wordsArr[0].trim().equalsIgnoreCase("INS") ||
                    wordsArr[0].trim().equalsIgnoreCase("MERGE") || wordsArr[0].trim().equalsIgnoreCase("DELETE") ||
                    wordsArr[0].trim().equalsIgnoreCase("DEL") || wordsArr[0].trim().equalsIgnoreCase("CREATE") ||
                    wordsArr[0].trim().equalsIgnoreCase("UPDATE") ||wordsArr[0].trim().equalsIgnoreCase("UPD") ||
                    wordsArr[0].trim().equalsIgnoreCase("SELECT") || wordsArr[0].trim().equalsIgnoreCase("SEL")) {
                try {
                    for(int n=0;n<wordsArr.length;n++) {
                        String word = wordsArr[n];

                        //checking for target-->ins/mer/del
                        if(word.equalsIgnoreCase("INSERT") || word.equalsIgnoreCase("INS") || word.equalsIgnoreCase("MERGE") || word.equalsIgnoreCase("DELETE") || word.equalsIgnoreCase("DEL")) {
                            wordCount=0;
                            isTargetForInsMerDel=true;
                            if(word.equalsIgnoreCase("MERGE")) {
                                isMerge = true;
                            }
                        }
                        if(wordCount==2 && isTargetForInsMerDel==true) {
                            int index = n;
                            if(((n-2)>=0) && wordsArr[n-2].trim().equalsIgnoreCase("DELETE") && word.trim().equalsIgnoreCase("FROM")) {
                                targetList.add(wordsArr[n-1].replace("(", "").replace(")", "").replace(";", ""));
                            } else if (((index-2)>=0) && ((index-1)>=0) && wordsArr[index-2].equalsIgnoreCase("INSERT") && wordsArr[index-1].equalsIgnoreCase("(")) {

                            } else {
                                targetList.add(word.replace("(", "").replace(")", "").replace(";", ""));
                            }
                            isTargetForInsMerDel = false;
                        }

                        //checking for target-->cre
                        if(word.equalsIgnoreCase("CREATE")) {
                            wordCount=0;
                            isTargetForCreate = true;
                        }
                        if(wordCount==1 && isTargetForCreate==true && (word.equalsIgnoreCase("TABLE") || word.equalsIgnoreCase("VIEW") || word.equalsIgnoreCase("PROCEDURE") || word.equalsIgnoreCase("FUNCTION") || word.equalsIgnoreCase("MACRO"))) {
                            isTargetForCreate = false;
                            findCreateTable = true;
                        } else if(wordCount==1 && isTargetForCreate==true && !(word.equalsIgnoreCase("TABLE")) && !(word.equalsIgnoreCase("VIEW")) && !(word.equalsIgnoreCase("PROCEDURE")) && !(word.equalsIgnoreCase("FUNCTION")) && !(word.equalsIgnoreCase("MACRO"))) {
                            isTargetForCreate = false;
                            isTargetForCreateSetMultiGlobalVol = true;
                        }
                        if(wordCount==2 && findCreateTable ==true) {
                            //System.out.println("adding in target list..." + word);
                            targetList.add(word.replace("(", "").replace(")", "").replace(";", ""));
                            findCreateTable = false;
                            wordCount = 0;
                        }
                        if(isTargetForCreateSetMultiGlobalVol==true && (word.equalsIgnoreCase("TABLE") || word.equalsIgnoreCase("VIEW") || word.equalsIgnoreCase("PROCEDURE") || word.equalsIgnoreCase("FUNCTION") || word.equalsIgnoreCase("MACRO"))) {
                            isTargetForCreateSetMultiGlobalVol = false;
                            isTargetForCreateSetMultiGlobalVolFinal = true;
                            wordCount = 0;
                        }
                        if(wordCount==1 && isTargetForCreateSetMultiGlobalVolFinal==true) {
                            //System.out.println("adding in target list 164.." + word);
                            targetList.add(word.replace("(", "").replace(")", "").replace(";", ""));
                            isTargetForCreateSetMultiGlobalVolFinal = false;
                            wordCount = 0;
                        }

                        //checking for target-->upd
                        if(word.equalsIgnoreCase("UPDATE") || word.equalsIgnoreCase("UPD")) {
                            wordCount=0;
                            isTargetForUpdate=true;
                        }
                        if(wordCount==1 && isTargetForUpdate==true) {
                            targetList.add(word.replace("(", "").replace(")", "").replace(";", ""));
                            isTargetForUpdate= false;
                        }
                        wordCount++;

                        //checking for source->from/join/using
                        if (word.trim().equalsIgnoreCase("FROM") || word.trim().equalsIgnoreCase("JOIN") || (word.trim().equalsIgnoreCase("USING") && isMerge==true)) {
                            if (!(wordsArr[n - 1].trim().toUpperCase().equals("DELETE"))) {
                                traversingWholeText(word.trim(), n, wordsArr, sourceList, targetList, unwantedKeyWordsListForSymbol, unwantedKeyWordsListForWords);
                            }
                        }

                        //ignoring aliases which are coming like WITH CTE AS
                        if((word.trim().equalsIgnoreCase("WITH") || word.trim().equalsIgnoreCase(",")) && (n+2)<wordsArr.length && wordsArr[n+2].equals("(")) {
                            unwantedKeyWordsListForWords.add(wordsArr[n+1]);
                        }
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (sourceList.size() == 0 && targetList.size() == 0) {
                LOGGER.info("Failed ->query doesn't starts with ins/upd/del/cre/merg/sel");
            } else {
                HashSet<String> sourceListFiltered = new HashSet();
                HashSet<String> targetListFiltered = new LinkedHashSet();
                for (String source : sourceList) {
                    Boolean sourceStatus = true;
                    for (String keyword : unwantedKeyWordsListForWords) {
                        if (source.trim().toUpperCase().equals(keyword)) {
                            // System.out.println("this is source..."+source);
                            // System.out.println("source contains keyword..."+keyword);
                            sourceStatus = false;
                        }
                    }
                    for (String keyword : unwantedKeyWordsListForSymbol) {
                        if (source.trim().toUpperCase().contains(keyword)) {
                            // System.out.println("this is source..."+source);
                            // System.out.println("source contains keyword..."+keyword);
                            sourceStatus = false;
                        }
                    }
                    try {
                        int val = Integer.parseInt(source);
                        sourceStatus = false;
                    } catch (Exception ex) {
                    }
                    if (sourceStatus) {
                        if (!(source.trim().equals(""))) {
                            sourceListFiltered.add(source.trim().toUpperCase());
                        }
                    }
                }

                for (String target : targetList) {
                    Boolean targetStatus = true;
                    for (String keyword : unwantedKeyWordsListForWords) {
                        if (target.trim().toUpperCase().equals(keyword)) {
                            // System.out.println("this is target..."+target);
                            // System.out.println("target contains keyword..."+keyword);
                            targetStatus = false;
                        }
                    }
                    for (String keyword : unwantedKeyWordsListForSymbol) {
                        if (target.trim().toUpperCase().contains(keyword)) {
                            // System.out.println("this is target..."+target);
                            // System.out.println("target contains keyword..."+keyword);
                            targetStatus = false;
                        }
                    }
                    if (targetStatus) {
                        if (!(target.trim().equals(""))) {
                            if (isMerge) {
                                targetListFiltered.add(target.trim().toUpperCase());
                                break;
                            } else {
                                targetListFiltered.add(target.trim().toUpperCase());
                            }
                        }
                    }
                }

                if (sourceListFiltered.size() == 0 && targetListFiltered.size() == 0) {
                    LOGGER.info("Failed ->query doesn't starts with ins/upd/del/cre/merg/sel");
                } else {
                    hm.put("sources", sourceListFiltered);
                    hm.put("targets", targetListFiltered);
                }
            }
        } catch (Exception ex) {
            LOGGER.info("Exception occured in parseBteqScriptIncludingSelectScenrio " + ex.getMessage());
        }
        //System.out.println("Exit parseBteqScript BO ");
        return hm;
    }

    public void traversingWholeText(String word, int index, String[] wordsArr, HashSet<String> sourceList, HashSet<String> targetList, List<String> unwantedKeyWordsListForSymbol, List<String> unwantedKeyWordsListForWords) {
        try {
            int counter=0;
            Boolean source = false;
            HashMap<String,String> hm = new HashMap();
            for(int i=index;i<wordsArr.length;i++) {
                counter = 0;
                if (wordsArr[i].equalsIgnoreCase("from") || wordsArr[i].equalsIgnoreCase("join") || wordsArr[i].equalsIgnoreCase("using")) {
                    counter = 1;
                    source = true;
                }
                if(counter==0 && source==true) {
                    //System.out.println("wordsArr i is..."+wordsArr[i]);
                    //System.out.println("wordsArr i+1 is..."+wordsArr[i+1]);
                    if(wordsArr[i].contains("(") || wordsArr[i].contains(")")) {
                        continue;                   //added recently for from ( ( ( ( ( tablename scenerio but issue may come with from ( (( select
                    } else {
                        //checking for alias            a , b , c , d ,   a aa , b b , c ccn , add statically for five comma
                        if((i+1)<wordsArr.length && wordsArr[i+1].equals(",")) {
                            sourceList.add(wordsArr[i]);

                            if((i+3)<wordsArr.length && wordsArr[i+3].equals(",")) {
                                //System.out.println("this is source1: "+wordsArr[i+2]);
                                sourceList.add(wordsArr[i+2]);

                                if((i+5)<wordsArr.length && wordsArr[i+5].equals(",")) {
                                    //System.out.println("this is source2: "+wordsArr[i+4]);
                                    sourceList.add(wordsArr[i+4]);

                                    if((i+7)<wordsArr.length && wordsArr[i+7].equals(",")) {
                                        //System.out.println("this is source3: "+wordsArr[i+6]);
                                        sourceList.add(wordsArr[i+6]);
                                    } else {
                                        //fetching elements of target hashset
                                        String firstElement = "";
                                        String secondElement = "";
                                        int counthm = 0;
                                        for(String targethm: targetList) {
                                            if(counthm==0) {
                                                firstElement = targethm;
                                            } else {
                                                secondElement = targethm;
                                                break;
                                            }
                                            counthm++;
                                        }
                                        if((i+7)<wordsArr.length && (wordsArr[i+7].equals(firstElement) || wordsArr[i+7].equals(secondElement))) {
                                            hm.put(wordsArr[i+7], wordsArr[i+6]);
                                        }
                                        //System.out.println("this is source4: "+wordsArr[i+6]);
                                        sourceList.add(wordsArr[i+6]);
                                    }
                                } else {
                                    String firstElement = "";
                                    String secondElement = "";
                                    int counthm = 0;
                                    for(String targethm: targetList) {
                                        if(counthm==0) {
                                            firstElement = targethm;
                                        } else {
                                            secondElement = targethm;
                                            break;
                                        }
                                        counthm++;
                                    }
                                    if((i+5)<wordsArr.length && (wordsArr[i+5].equals(firstElement) || wordsArr[i+5].equals(secondElement))) {
                                        hm.put(wordsArr[i+5], wordsArr[i+4]);
                                    }
                                    //System.out.println("this is source5: "+wordsArr[i+4]);
                                    sourceList.add(wordsArr[i+4]);
                                }
                            } else {
                                String firstElement = "";
                                String secondElement = "";
                                int counthm = 0;
                                for(String targethm: targetList) {
                                    if(counthm==0) {
                                        firstElement = targethm;
                                    } else {
                                        secondElement = targethm;
                                        break;
                                    }
                                    counthm++;
                                }
                                //System.out.println("this is source6: "+wordsArr[i+2]);
                                if((i+3)<wordsArr.length && (wordsArr[i+3].equals(firstElement) || wordsArr[i+3].equals(secondElement))) {
                                    hm.put(wordsArr[i+3], wordsArr[i+2]);
                                }
                                sourceList.add(wordsArr[i+2]);
                            }
                        } else if((i+2)<wordsArr.length && wordsArr[i+2].equals(",") && !(wordsArr[i+1].equals(")"))) {
                            //System.out.println("this is source7: "+wordsArr[i]);
                            sourceList.add(wordsArr[i]);
                            hm.put(wordsArr[i+1], wordsArr[i]);
                            if((i+5)<wordsArr.length && wordsArr[i+5].equals(",") && !(wordsArr[i+4].equals(")"))) {
                                //System.out.println("this is source8: "+wordsArr[i+3]);
                                sourceList.add(wordsArr[i+3]);
                                hm.put(wordsArr[i+4], wordsArr[i+3]);

                                if((i+8)<wordsArr.length && wordsArr[i+8].equals(",") && !(wordsArr[i+7].equals(")"))) {
                                    //System.out.println("this is source9: "+wordsArr[i+7]);
                                    sourceList.add(wordsArr[i+6]);
                                    hm.put(wordsArr[i+7], wordsArr[i+6]);

                                    if((i+11)<wordsArr.length && wordsArr[i+11].equals(",") && !(wordsArr[i+10].equals(")"))) {
                                        //System.out.println("this is source10: "+wordsArr[i+9]);
                                        sourceList.add(wordsArr[i+9]);
                                        hm.put(wordsArr[i+10], wordsArr[i+9]);
                                    } else {
                                        String firstElement = "";
                                        String secondElement = "";
                                        int counthm = 0;
                                        for(String targethm: targetList) {
                                            if(counthm==0) {
                                                firstElement = targethm;
                                            } else {
                                                secondElement = targethm;
                                                break;
                                            }
                                            counthm++;
                                        }
                                        //System.out.println("this is source11: "+wordsArr[i+9]);
                                        if((i+10)<wordsArr.length && (wordsArr[i+10].equals(firstElement) || wordsArr[i+10].equals(secondElement))) {
                                            hm.put(wordsArr[i+10], wordsArr[i+9]);
                                        }
                                        sourceList.add(wordsArr[i+9]);
                                    }
                                } else {
                                    String firstElement = "";
                                    String secondElement = "";
                                    int counthm = 0;
                                    for(String targethm: targetList) {
                                        if(counthm==0) {
                                            firstElement = targethm;
                                        } else {
                                            secondElement = targethm;
                                            break;
                                        }
                                        counthm++;
                                    }
                                    //System.out.println("this is source12: "+wordsArr[i+6]);
                                    if((i+7)<wordsArr.length && (wordsArr[i+7].equals(firstElement) || wordsArr[i+7].equals(secondElement))) {
                                        hm.put(wordsArr[i+7], wordsArr[i+6]);
                                    }
                                    sourceList.add(wordsArr[i+6]);
                                }
                            } else {
                                String firstElement = "";
                                String secondElement = "";
                                int counthm = 0;
                                for(String targethm: targetList) {
                                    if(counthm==0) {
                                        firstElement = targethm;
                                    } else {
                                        secondElement = targethm;
                                        break;
                                    }
                                    counthm++;
                                }
                                //System.out.println("this is source13: "+wordsArr[i+3]);
                                if((i+4)<wordsArr.length && (wordsArr[i+4].equals(firstElement) || wordsArr[i+4].equals(secondElement))) {
                                    hm.put(wordsArr[i+4], wordsArr[i+3]);
                                }
                                sourceList.add(wordsArr[i+3]);
                            }
                        } else {
                            String firstElement = "";
                            String secondElement = "";
                            int counthm = 0;
                            for(String targethm: targetList) {
                                if(counthm==0) {
                                    firstElement = targethm;
                                } else {
                                    secondElement = targethm;
                                    break;
                                }
                                counthm++;
                            }
                            //System.out.println("this is source14: "+wordsArr[i]);
                            if((i+1)<wordsArr.length && (wordsArr[i+1].equals(firstElement) || wordsArr[i+1].equals(secondElement))) {
                                hm.put(wordsArr[i+1], wordsArr[i]);
                            }
                            sourceList.add(wordsArr[i]);
                        }
                    }
                    source = false;
                    break;          //added recently so that we don't have to traverse again in main method
                }
            }
            //updating target tablename with real name if coming
            //System.out.println("this is hashmap...."+hm.toString());
            for(String targetVal: targetList) {
                if(hm.containsKey(targetVal.trim())) {
                    String val = hm.get(targetVal.trim());

                    Boolean status = true;
                    for(String keyword: unwantedKeyWordsListForSymbol) {
                        if(targetVal.trim().toUpperCase().contains(keyword) || val.trim().toUpperCase().contains(keyword)) {
                            status = false;
                        }
                    }
                    for(String keyword: unwantedKeyWordsListForWords) {
                        if(targetVal.trim().toUpperCase().equals(keyword) || val.trim().toUpperCase().equals(keyword)) {
                            status = false;
                        }
                    }
                    if(status) {
                        targetList.remove(targetVal);
                        targetList.add(val);
                        //System.out.println("this is new target..."+val);
                    }
                }
            }

        } catch(Exception ex) {
            LOGGER.info("Exception occured in traversingWholeText "+ex.getMessage());
        }
    }
}

