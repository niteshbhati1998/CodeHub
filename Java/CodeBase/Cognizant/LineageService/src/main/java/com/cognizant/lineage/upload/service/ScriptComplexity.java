package com.cognizant.lineage.upload.service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.dao.entity.ComplexityDetails;
import com.cognizant.lineage.dao.entity.QueryComplexityDetails;
import com.cognizant.lineage.dao.repository.ComplexityDetailsRepository;
import com.cognizant.lineage.dao.repository.InformaticaSourceRepo;
import com.cognizant.lineage.dao.repository.ODIComplexityRepository;
import com.cognizant.lineage.dao.repository.QueryComplexityDetailsRepository;
import com.cognizant.lineage.dao.repository.ScriptFilesInfoRepo;
import com.cognizant.lineage.dao.repository.ScriptModuleMappingRepository;
import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.constants.TechnologyConstants;
import com.cognizant.lineage.upload.model.ComplexityQueryDto;
import com.cognizant.lineage.util.Sanitization;

@Service
public class ScriptComplexity {

	@Autowired
	@Qualifier("lineageJdbcTemplate")
	JdbcTemplate lineageJdbcTemplate;

	@Autowired
    ODIComplexityRepository odiComplexityRepository;

	@Autowired
	QueryComplexityDetailsRepository queryComplexityDetailsRepository;

	@Autowired
	InformaticaSourceRepo informaticaSourceRepo;
	
	@Autowired
	ScriptFilesInfoRepo scriptFilesInfoRepo;

	@Autowired
	ComplexityDetailsRepository complexityDetailsRepo;

	@Autowired
	ScriptModuleMappingRepository scriptModuleMappingRepo;

	@Autowired
	JobStatusService jobStatusService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptComplexity.class);

	public void calculateScriptComplexity(String projectName, int jobId, String tech, String parentTech) {
		LOGGER.info("Script Complexity start with jobId: {} :: projectName: {} :: parentTech: {} :: tech: {}",
				jobId, projectName, parentTech, tech);
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			tech = Sanitization.sanitizeInput(tech);

			HashSet<String> joinList = new HashSet<>();
			HashSet<String> functionList = new HashSet<>();
			HashSet<Boolean> complexityBoolSet = new HashSet<>();
			List<String> scriptFileList;
			if (TechnologyConstants.ODI.equalsIgnoreCase(tech)) {
				scriptFileList = odiComplexityRepository.findFileNamesByJobId(jobId);
			} else {
				scriptFileList = scriptFilesInfoRepo.findFileNamesByJobId(jobId);
			}
			
			LOGGER.info("scriptFileList :: {}", scriptFileList);

			String joinTypes = "FULL OUTER JOIN,RIGHT OUTER JOIN,RIGHT JOIN"
					+ ",LEFT OUTER JOIN,LEFT JOIN,INNER JOIN,CROSS JOIN";

			for (String scriptFile : scriptFileList) {
				scriptFile = Sanitization.sanitizeInput(scriptFile);

				List<String> sqlTextList;
				if (TechnologyConstants.ODI.equalsIgnoreCase(tech)) {
					sqlTextList = odiComplexityRepository.findNewSQLTextByJobId(jobId, scriptFile);
				} else {
					sqlTextList = scriptFilesInfoRepo.findSqlTextByJobIdAndFileName(jobId, scriptFile);
				}

				int queryCount = sqlTextList.size();
				String sql1SequenceId = md5AndHashCodeAlgo(scriptFile.concat(String.valueOf(jobId)));
                Map<String, Integer> countMap = patternsAndMatchers(sqlTextList, joinTypes, joinList, functionList);
				countMap.put(GeneralConstants.QUERY_COUNT, queryCount);
				LOGGER.info("countMap :: {}", countMap);
				QueryComplexityDetails complexityDetails;

				// if condition is common for TERADATA, ORACLE, MS_SQL_SERVER - (VIEW, TPT, MLOAD)
                if (TechnologyConstants.VIEW.equalsIgnoreCase(tech) ||
						TechnologyConstants.TPT.equalsIgnoreCase(tech) ||
						TechnologyConstants.MLOAD.equalsIgnoreCase(tech)) {
					complexityDetails = buildComplexityDetailsForViewTptMLoad(sql1SequenceId,
							jobId, projectName, scriptFile.toUpperCase(), tech, parentTech, countMap);
                } else if (TechnologyConstants.SSRS.equalsIgnoreCase(tech) ||
						TechnologyConstants.SSIS.equalsIgnoreCase(tech)) {
					complexityDetails = buildComplexityDetailsForSSISandSSRS(sql1SequenceId,
							jobId, projectName, scriptFile.toUpperCase(), tech, parentTech, countMap);
				} else {
					complexityDetails = buildComplexityDetails(sql1SequenceId,
							jobId, projectName, scriptFile.toUpperCase(), tech, parentTech, countMap);
				}

				boolean uploadFiles = calculateComplexityAndSaveDetails(projectName, parentTech, tech,
						scriptFile.toUpperCase(), complexityDetails);
				complexityBoolSet.add(uploadFiles);
            }

			if (scriptFileList.isEmpty() ||
					(complexityBoolSet.size() == 1 && complexityBoolSet.contains(Boolean.TRUE))) {
				jobStatusService.updateJobStatusDetailsWithComplexityString((long) jobId);
			} else {
				LOGGER.info("Some error has occurred while calculating complexity for 1 or more files.");
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred in ScriptComplexity class :: ", e);
		} finally {
			LOGGER.info("================== Script complexity end ==========================");
		}
	}

	public static String md5AndHashCodeAlgo(String inputStr) throws NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance("SHA-256");
		m.reset();
		m.update(inputStr.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1, digest);
		String hashText = bigInt.toString(16);
		while (hashText.length() < 32) {
			hashText = "0" + hashText;
		}
		LOGGER.info("md5 algo. output: " + hashText);
		String uniqueAlphabets = removeDuplicates(hashText.replaceAll("[0-9]", ""));

		String input = hashText;
		String uniqueVal = String.valueOf(Math.abs(input.hashCode()));
		LOGGER.info("hashCode output: " + uniqueVal);

		StringBuilder str = new StringBuilder(uniqueVal);
		if (uniqueAlphabets.length() >= 3) {
			str.insert(0, uniqueAlphabets.charAt(0));
			str.insert(2, uniqueAlphabets.charAt(1));
			str.insert(6, uniqueAlphabets.charAt(2));
		}
		return str.toString();
	}

	public static String removeDuplicates(String str) {
		String newStr = "";
		int length = str.length();
		for (int i = 0; i < length; i++) {
			char charAtPosition = str.charAt(i);
			if (newStr.indexOf(charAtPosition) < 0) {
				newStr += charAtPosition;
			}
		}
		LOGGER.info("unique alphabets from md5: " + newStr);
		return newStr;
	}

	private Map<String, Integer> patternsAndMatchers(List<String> sqlTextList, String joinTypes,
													 HashSet<String> joinList, HashSet<String> functionList) {
		// insert, update, delete, create, merge, join, function_used, condition, select
		Map<String, Integer> countMap = new HashMap<>(); 
		for (String sqlQuery : sqlTextList) {

			if (sqlQuery.toLowerCase().trim().startsWith("insert") && (sqlQuery.length() == "insert".length()
					|| Character.isWhitespace(sqlQuery.charAt("insert".length())))) {
				String patternStrForInsert = ".*?\\binsert\\b.*?";
				Pattern patternInsert = Pattern.compile(patternStrForInsert);
				Matcher matcherInsert = patternInsert.matcher(sqlQuery.toLowerCase());
				int insertCount = countMap.getOrDefault(GeneralConstants.INSERT_COUNT, 0);
				while (matcherInsert.find()) {
					++insertCount;
				}
				countMap.put(GeneralConstants.INSERT_COUNT, insertCount);
				LOGGER.info("insertCount " + insertCount);
			}

			else if (sqlQuery.toLowerCase().trim().startsWith("ins") && (sqlQuery.length() == "ins".length()
					|| Character.isWhitespace(sqlQuery.charAt("ins".length())))) {
				String patternStrForIns = ".*?\\bins\\b.*?";
				Pattern patternIns = Pattern.compile(patternStrForIns);
				Matcher matcherIns = patternIns.matcher(sqlQuery.toLowerCase());
				int insertCount = countMap.getOrDefault(GeneralConstants.INSERT_COUNT, 0);
				while (matcherIns.find()) {
					++insertCount;
				}
				countMap.put(GeneralConstants.INSERT_COUNT, insertCount);
				LOGGER.info("insertCount " + insertCount);
			}

			else if (sqlQuery.toLowerCase().trim().startsWith("update") && (sqlQuery.length() == "update".length()
					|| Character.isWhitespace(sqlQuery.charAt("update".length())))) {
				String patternStrForUpdate = ".*?\\bupdate\\b.*?";
				Pattern patternUpdate = Pattern.compile(patternStrForUpdate);
				Matcher matcherUpdate = patternUpdate.matcher(sqlQuery.toLowerCase());
				int updateCount = countMap.getOrDefault(GeneralConstants.UPDATE_COUNT, 0);
				while (matcherUpdate.find()) {
					++updateCount;
				}
				countMap.put(GeneralConstants.UPDATE_COUNT, updateCount);
				LOGGER.info("updateCount " + updateCount);
			}

			else if (sqlQuery.toLowerCase().trim().startsWith("upd") && (sqlQuery.length() == "upd".length()
					|| Character.isWhitespace(sqlQuery.charAt("upd".length())))) {
				String patternStrForUpd = ".*?\\bupd\\b.*?";
				Pattern patternUpd = Pattern.compile(patternStrForUpd);
				Matcher matcherUpd = patternUpd.matcher(sqlQuery.toLowerCase());
				int updateCount = countMap.getOrDefault(GeneralConstants.UPDATE_COUNT, 0);
				while (matcherUpd.find()) {
					++updateCount;
				}
				countMap.put(GeneralConstants.UPDATE_COUNT, updateCount);
				LOGGER.info("updateCount " + updateCount);
			}

			else if (sqlQuery.toLowerCase().trim().startsWith("delete") && (sqlQuery.length() == "delete".length()
					|| Character.isWhitespace(sqlQuery.charAt("delete".length())))) {
				String patternStrForDelete = ".*?\\bdelete\\b.*?";
				Pattern patternDelete = Pattern.compile(patternStrForDelete);
				Matcher matcherDelete = patternDelete.matcher(sqlQuery.toLowerCase());
				int deleteCount = countMap.getOrDefault(GeneralConstants.DELETE_COUNT, 0);
				while (matcherDelete.find()) {
					++deleteCount;
				}
				countMap.put(GeneralConstants.DELETE_COUNT, deleteCount);
				LOGGER.info("deleteCount " + deleteCount);
			}

			else if (sqlQuery.toLowerCase().trim().startsWith("del") && (sqlQuery.length() == "del".length()
					|| Character.isWhitespace(sqlQuery.charAt("del".length())))) {
				String patternStrForDel = ".*?\\bdel\\b.*?";
				Pattern patternDel = Pattern.compile(patternStrForDel);
				Matcher matcherDel = patternDel.matcher(sqlQuery.toLowerCase());
				int deleteCount = countMap.getOrDefault(GeneralConstants.DELETE_COUNT, 0);
				while (matcherDel.find()) {
					++deleteCount;
				}
				countMap.put(GeneralConstants.DELETE_COUNT, deleteCount);
				LOGGER.info("deleteCount " + deleteCount);
			}

			else if (sqlQuery.toLowerCase().trim().startsWith("create") && (sqlQuery.length() == "create".length()
					|| Character.isWhitespace(sqlQuery.charAt("create".length())))) {
				String patternStrForCreate = ".*?\\bcreate\\b.*?";
				Pattern patternCreate = Pattern.compile(patternStrForCreate);
				Matcher matcherCreate = patternCreate.matcher(sqlQuery.toLowerCase());
				int createCount = countMap.getOrDefault(GeneralConstants.CREATE_COUNT, 0);
				while (matcherCreate.find()) {
					++createCount;
				}
				countMap.put(GeneralConstants.CREATE_COUNT, createCount);
				LOGGER.info("createCount " + createCount);
			}

			else if (sqlQuery.toLowerCase().trim().startsWith("merge") && (sqlQuery.length() == "merge".length()
					|| Character.isWhitespace(sqlQuery.charAt("merge".length())))) {
				String patternStrForMerge = ".*?\\bmerge\\b.*?";
				Pattern patternMerge = Pattern.compile(patternStrForMerge);
				Matcher matcherMerge = patternMerge.matcher(sqlQuery.toLowerCase());
				int mergeCount = countMap.getOrDefault(GeneralConstants.MERGE_COUNT, 0);
				while (matcherMerge.find()) {
					++mergeCount;
				}
				countMap.put(GeneralConstants.MERGE_COUNT, ++mergeCount);
				LOGGER.info("mergeCount " + mergeCount);
			}

			else if (sqlQuery.toLowerCase().trim().startsWith("select") && (sqlQuery.length() == "select".length()
					|| Character.isWhitespace(sqlQuery.charAt("select".length())))) {
				String patternStrForMerge = ".*?\\bselect\\b.*?";
				Pattern patternMerge = Pattern.compile(patternStrForMerge);
				Matcher matcherSelect = patternMerge.matcher(sqlQuery.toLowerCase());
				int selectCount = countMap.getOrDefault(GeneralConstants.SELECT_COUNT, 0);
				while (matcherSelect.find()) {
					++selectCount;
				}
				countMap.put(GeneralConstants.SELECT_COUNT, ++selectCount);
				LOGGER.info("selectCount " + selectCount);
			}

			List<String> funList = odiComplexityRepository.getFunList();
			for (String arrSplit : funList) {
				String patternStr = "(\\(|,\\s*)" + arrSplit.toLowerCase().trim() + "\\s*\\(.*?\\)"
						+ "(\\s*\\))?" + "|" + arrSplit.trim().toLowerCase() + "\\s*" + "\\(.*?\\)";
				Pattern p = Pattern.compile(patternStr);
				Matcher m = p.matcher(sqlQuery.toLowerCase());
				int functionUsedCount = countMap.getOrDefault(GeneralConstants.FUNCTION_USED_COUNT, 0);
				while (m.find()) {
					if (m.start() - 1 >= 0) {
						char values = sqlQuery.charAt(m.start() - 1);
						if (sqlQuery.charAt(m.start()) == '(' || sqlQuery.charAt(m.start()) == ','
								|| sqlQuery.charAt(m.start()) == ' ' || values == ' ' || values == '\n'
								|| values == '=' && values != '_') {
							functionList.add(arrSplit);
							++functionUsedCount;
						}
					} else if (m.start() == 0) {
						functionList.add(arrSplit);
						++functionUsedCount;
					}
				}
				countMap.put(GeneralConstants.FUNCTION_USED_COUNT, functionUsedCount);
			}

			int joinsCount = countMap.getOrDefault(GeneralConstants.JOIN_COUNT, 0);
			String[] arrJoinSplit = joinTypes.split(",");
			for (int j = 0; j < arrJoinSplit.length; j++) {
				Pattern p = Pattern.compile(arrJoinSplit[j].trim().toLowerCase());
				Matcher m = p.matcher(sqlQuery.toLowerCase().replaceAll("\\s{2,}", " ").trim());
				int joinSplitCount = 0;
				while (m.find()) {
					joinList.add(arrJoinSplit[j]);
					++joinSplitCount;
					joinsCount += joinSplitCount;
				}
				LOGGER.info("joinSplitCount - " + arrJoinSplit[j] + ": " + joinSplitCount);
			}

			int countJoins =0;
			Pattern patternForCountingJoins = Pattern.compile(
					"\\b(?<!left\\s)(?<!right\\s)(?<!full\\souter\\s)"
							+ "(?<!left\\souter\\s)(?<!right\\souter\\s)(?<!inner\\s)(?<!cross\\s)join\\b",
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = patternForCountingJoins.matcher(sqlQuery);
			while (matcher.find()) {
				countJoins++;
			}
			LOGGER.info("countJoins " + countJoins);
			countMap.put(GeneralConstants.JOIN_COUNT, (joinsCount + countJoins));
			LOGGER.info("count split " + joinsCount);

			/* Below code is for calculating condition count in the sql query
			 * sum of where, on, or, and conditions
			 */

			String conditionCountTypes = "WHERE,ON,OR,AND";
			int conditionCount = 0;
			int countSplitForCondition = 0;
			String[] arrSplitConditionCount = conditionCountTypes.split(",");
			for (int j = 0; j < arrSplitConditionCount.length; j++) {
				Pattern p = Pattern.compile("\\b" + arrSplitConditionCount[j].trim().toLowerCase() + "\\b");
				Matcher m = p.matcher(sqlQuery.toLowerCase());
				while (m.find()) {
					countSplitForCondition++;
				}
			}
			LOGGER.info("countSplitForCondition :" + countSplitForCondition);
			conditionCount += countSplitForCondition;

			/* Below code is to remove the usages of 'and' keyword between 'When' and 'Then' keywords in queries
			 * and count is removed from condition count is it as not considered for condition count
			 */

			String regexForAndBetweenWhenThen = "(?<=when).*?\\b(and)\\b.*?(?=then)";
			Pattern patternForAndBetweenWhenThen = Pattern.compile(regexForAndBetweenWhenThen, Pattern.CASE_INSENSITIVE);
			Matcher matcherForAndBetweenWhenThen = patternForAndBetweenWhenThen.matcher(sqlQuery);

			int andCountBetweenWhenThen = 0;
			while (matcherForAndBetweenWhenThen.find()) {
				andCountBetweenWhenThen++;
			}
			conditionCount = conditionCount - andCountBetweenWhenThen;
			LOGGER.info("conditionCount :" + conditionCount);
			countMap.put(GeneralConstants.CONDITION_COUNT, conditionCount);
		}
		return countMap;
	}

	public boolean calculateScriptComplexityForInformaticaUsingSqlQueryAndTags(String projectName, Long jobId, String tech) {
		try {
			projectName = Sanitization.sanitizeInput(projectName);
			tech = Sanitization.sanitizeInput(tech);

			LOGGER.info("ScriptComplexityForInformatica start");
			Set<String> scriptFileSet = informaticaSourceRepo.getDistinctFilename(jobId);

			HashSet<String> joinList = new HashSet<>();
			HashSet<String> functionList = new HashSet<>();
			HashSet<Boolean> complexityBoolSet = new HashSet<>();
			String joinTypes = "FULL OUTER JOIN,RIGHT OUTER JOIN,RIGHT JOIN"
					+ ",LEFT OUTER JOIN,LEFT JOIN,INNER JOIN,CROSS JOIN";
			for (String scriptFile : scriptFileSet) {
				List<String> sqlTextList = informaticaSourceRepo.findSqlTextByJobIdAndFileName(jobId, scriptFile);
				Map<String, Integer> countMap = patternsAndMatchers(sqlTextList, joinTypes, joinList, functionList);
				getComponentCount(jobId, scriptFile, countMap);
				String sql1SequenceId = md5AndHashCodeAlgo(scriptFile.concat(String.valueOf(jobId)));
				QueryComplexityDetails complexityDetails = buildComplexityDetailsForInformatica(sql1SequenceId, jobId,
						projectName, scriptFile.toUpperCase(), countMap, tech);
				Boolean uploadFiles = calculateComplexityAndSaveDetails(projectName, TechnologyConstants.INFORMATICA,
						tech, scriptFile.toUpperCase(), complexityDetails);
				complexityBoolSet.add(uploadFiles);
			}
			LOGGER.info("ScriptComplexityForInformatica end");
			return scriptFileSet.isEmpty() || (complexityBoolSet.size() == 1 && complexityBoolSet.contains(Boolean.TRUE));
		} catch (Exception e) {
			LOGGER.error("Exception occurred in ScriptComplexity class getScriptComplexityForInformatica:: ", e);
			return false;
		}
	}

	private String calculateComplexityUsingQueryForInformatica(String id) throws LineageBusinessException {
		try {
			int simpleSelectCount = 0; int simpleFunctionUsedCount = 0;
			int simpleJoinCount = 0; int simpleOtherComponentCount = 0;

			int mediumSelectCount = 0; int mediumFunctionUsedCount = 0;
			int mediumJoinCount = 0; int mediumOtherComponentCount = 0;

			int complexSelectCount = 0; int complexFunctionUsedCount = 0; int complexJoinCount = 0;
			int complexAggregatorCount = 0; int complexNormalizerCount = 0; int complexProcedureCount = 0;
			int complexOtherComponentCount = 0;

			int veryComplexSelectCount = 0; int veryComplexFunctionUsedCount = 0; int veryComplexJoinCount = 0;
			int veryComplexCustomCount = 0; int veryComplexOtherComponentCount = 0; int veryComplexAggregatorCount = 0;
			int veryComplexNormalizerCount = 0; int veryComplexProcedureCount = 0;

			StringBuilder simpleQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");
			StringBuilder mediumQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");
			StringBuilder complexQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");
			StringBuilder veryComplexQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");

			List<ComplexityDetails> complexityList = complexityDetailsRepo.findByTechnologyOrderByIdDesc(TechnologyConstants.INFORMATICA_CAPS);

			for (ComplexityDetails details : complexityList) {
				if (GeneralConstants.VERY_COMPLEX.equalsIgnoreCase(details.getComplexity())) {

					veryComplexJoinCount = details.getJoinCount();
					veryComplexSelectCount = details.getSelectCount();
					veryComplexFunctionUsedCount = details.getFunctionUsedCount();
					veryComplexCustomCount = details.getCustomComponentCount();
					veryComplexAggregatorCount = details.getAggregatorComponentCount();
					veryComplexNormalizerCount = details.getNormalizerComponentCount();
					veryComplexProcedureCount = details.getProcedureComponentCount();
					veryComplexOtherComponentCount = details.getOtherComponentCount();

					veryComplexQuery.append(" join_count >= ").append(veryComplexJoinCount);
					veryComplexQuery.append(" or select_count >= ").append(veryComplexSelectCount);
					veryComplexQuery.append(" or function_used_count >= ").append(veryComplexFunctionUsedCount);
					veryComplexQuery.append(" or custom_component_count >= ").append(veryComplexCustomCount);
					veryComplexQuery.append(" or aggregator_component_count >= ").append(veryComplexAggregatorCount);
					veryComplexQuery.append(" or normalizer_component_count >= ").append(veryComplexNormalizerCount);
					veryComplexQuery.append(" or procedure_component_count >= ").append(veryComplexProcedureCount);
					veryComplexQuery.append(" or other_component_count >= ").append(veryComplexOtherComponentCount);
					veryComplexQuery.append(" )");

				} else if (GeneralConstants.COMPLEX.equalsIgnoreCase(details.getComplexity())) {

					complexJoinCount = details.getJoinCount();
					complexSelectCount = details.getSelectCount();
					complexFunctionUsedCount = details.getFunctionUsedCount();
					complexAggregatorCount = details.getAggregatorComponentCount();
					complexNormalizerCount = details.getNormalizerComponentCount();
					complexProcedureCount = details.getProcedureComponentCount();
					complexOtherComponentCount = details.getOtherComponentCount();

					complexQuery.append(" ( join_count >= ").append(complexJoinCount);
					complexQuery.append(" and join_count < ").append(veryComplexJoinCount);
					complexQuery.append(" ) or ( select_count >= ").append(complexSelectCount);
					complexQuery.append(" and select_count < ").append(veryComplexSelectCount);
					complexQuery.append(" ) or ( function_used_count >= ").append(complexFunctionUsedCount);
					complexQuery.append(" and function_used_count < ").append(veryComplexFunctionUsedCount);
					complexQuery.append(" ) or ( other_component_count >= ").append(complexOtherComponentCount);
					complexQuery.append(" and other_component_count < ").append(veryComplexOtherComponentCount);
					complexQuery.append(" ) or aggregator_component_count >= ").append(complexAggregatorCount);
					complexQuery.append(" or normalizer_component_count >= ").append(complexNormalizerCount);
					complexQuery.append(" or procedure_component_count >= ").append(complexProcedureCount);
					complexQuery.append(" ) ");

				} else if (GeneralConstants.MEDIUM.equalsIgnoreCase(details.getComplexity())) {

					mediumJoinCount = details.getJoinCount();
					mediumSelectCount = details.getSelectCount();
					mediumFunctionUsedCount = details.getFunctionUsedCount();
					mediumOtherComponentCount = details.getOtherComponentCount();

					mediumQuery.append(" ( join_count >= ").append(mediumJoinCount);
					mediumQuery.append(" and join_count < ").append(complexJoinCount);
					mediumQuery.append(" ) or ( select_count >= ").append(mediumSelectCount);
					mediumQuery.append(" and select_count < ").append(complexSelectCount);
					mediumQuery.append(" ) or ( function_used_count >= ").append(mediumFunctionUsedCount);
					mediumQuery.append(" and function_used_count < ").append(complexFunctionUsedCount);
					mediumQuery.append(" ) or ( other_component_count >= ").append(mediumOtherComponentCount);
					mediumQuery.append(" and other_component_count < ").append(complexOtherComponentCount);
					mediumQuery.append(" ))");

				} else if (GeneralConstants.SIMPLE.equalsIgnoreCase(details.getComplexity())) {

					simpleJoinCount = details.getJoinCount();
					simpleSelectCount = details.getSelectCount();
					simpleFunctionUsedCount = details.getFunctionUsedCount();
					simpleOtherComponentCount = details.getOtherComponentCount();

					simpleQuery.append(" ( join_count >= ").append(simpleJoinCount);
					simpleQuery.append(" and join_count < ").append(mediumJoinCount);
					simpleQuery.append(" ) or ( select_count >= ").append(simpleSelectCount);
					simpleQuery.append(" and select_count < ").append(mediumSelectCount);
					simpleQuery.append(" ) or ( function_used_count >= ").append(simpleFunctionUsedCount);
					simpleQuery.append(" and function_used_count < ").append(mediumFunctionUsedCount);
					simpleQuery.append(" ) or ( other_component_count >= ").append(simpleOtherComponentCount);
					simpleQuery.append(" and other_component_count < ").append(mediumOtherComponentCount);
					simpleQuery.append(" ))");
				}
			}

			LOGGER.info("veryComplexQuery: {}", veryComplexQuery);
			LOGGER.info("complexQuery: {}", complexQuery);
			LOGGER.info("mediumQuery: {}", mediumQuery);
			LOGGER.info("simpleQuery: {}", simpleQuery);

			Integer simpleCount = lineageJdbcTemplate.queryForObject(simpleQuery.toString(), Integer.class);
			Integer mediumCount = lineageJdbcTemplate.queryForObject(mediumQuery.toString(), Integer.class);
			Integer complexCount = lineageJdbcTemplate.queryForObject(complexQuery.toString(), Integer.class);
			Integer veryComplexCount = lineageJdbcTemplate.queryForObject(veryComplexQuery.toString(), Integer.class);

			if (veryComplexCount > 0) {
				return GeneralConstants.VERY_COMPLEX;
			}
			if (complexCount > 0) {
				return GeneralConstants.COMPLEX;
			}
			if (mediumCount > 0) {
				return GeneralConstants.MEDIUM;
			}
			return GeneralConstants.SIMPLE;
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in calculateComplexityUsingQuery: ", ex);
			throw new LineageBusinessException("Exception occurred in complexity calculation: " + ex.getMessage());
		}
	}

	public boolean calculateScriptComplexityForTableau(Long jobId, String projectName, String fileName, String technology) {
		try {
			technology = Sanitization.sanitizeInput(technology);
			projectName = Sanitization.sanitizeInput(projectName);

			Integer tableCount = scriptModuleMappingRepo.getTableCountForComplexityForTableauAndCognos(projectName, fileName, technology);
			String sql1SequenceId = md5AndHashCodeAlgo(fileName.concat(String.valueOf(jobId)));

			QueryComplexityDetails queryComplexityDetails = QueryComplexityDetails.builder()
					.jobId(jobId)
					.id(sql1SequenceId)
					.fileName(fileName)
					.projectName(projectName)
					.technology(technology)
					.tableCount(tableCount)
					.status(GeneralConstants.SUCCESS)
					.build();
			return calculateComplexityAndSaveDetails(projectName, TechnologyConstants.BI, technology,
					fileName.toUpperCase(), queryComplexityDetails);
		} catch (Exception e) {
			LOGGER.error("Unexpected error occurred in calculating complexity in tableau:: ", e);
			return false;
		}
	}

	private String getComplexityUsingTableCount(Integer tableCount) {
		List<ComplexityDetails> detailsList =
				complexityDetailsRepo.findByTechnologyOrderByIdDesc(TechnologyConstants.BI_TABLEAU);

		int simpleTableCount = 0;
		int mediumTableCount = 0;
		int complexTableCount = 0;
		int veryComplexTableCount = 0;

		for (ComplexityDetails details : detailsList) {
			if (GeneralConstants.SIMPLE.equalsIgnoreCase(details.getComplexity())) {
				simpleTableCount = details.getTableCount();
			} else if (GeneralConstants.MEDIUM.equalsIgnoreCase(details.getComplexity())) {
				mediumTableCount = details.getTableCount();
			} else if (GeneralConstants.COMPLEX.equalsIgnoreCase(details.getComplexity())) {
				complexTableCount = details.getTableCount();
			} else if (GeneralConstants.VERY_COMPLEX.equalsIgnoreCase(details.getComplexity())) {
				veryComplexTableCount = details.getTableCount();
			}
		}

		String complexity;
		if (tableCount >= veryComplexTableCount) {
			complexity = GeneralConstants.VERY_COMPLEX;
		} else if (tableCount >= complexTableCount) {
			complexity = GeneralConstants.COMPLEX;
		} else if (tableCount >= mediumTableCount) {
			complexity = GeneralConstants.MEDIUM;
		} else {
			complexity = GeneralConstants.SIMPLE;
		}
		return complexity;
	}

	private String calculateComplexityForViewTptMLoad(String id, String tech) throws LineageBusinessException {
		try {
			tech = Sanitization.sanitizeInput(tech);

			int simpleSelectCount = 0, simpleFunctionUsedCount = 0, simpleJoinCount = 0, simpleConditionCount = 0;
			int mediumSelectCount = 0, mediumFunctionUsedCount = 0, mediumJoinCount = 0, mediumConditionCount = 0;
			int complexSelectCount = 0, complexFunctionUsedCount = 0, complexJoinCount = 0, complexConditionCount = 0;
			int veryComplexSelectCount = 0, veryComplexFunctionUsedCount = 0, veryComplexJoinCount = 0, veryComplexConditionCount = 0;

			StringBuilder simpleQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");
			StringBuilder mediumQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");
			StringBuilder complexQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");
			StringBuilder veryComplexQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");

			List<ComplexityDetails> complexityList = complexityDetailsRepo.findByTechnologyOrderByIdDesc(tech);

			for (ComplexityDetails details : complexityList) {
				if (GeneralConstants.VERY_COMPLEX.equalsIgnoreCase(details.getComplexity())) {

					veryComplexSelectCount = details.getSelectCount();
					veryComplexFunctionUsedCount = details.getFunctionUsedCount();
					veryComplexJoinCount = details.getJoinCount();
					veryComplexConditionCount = details.getConditionCount();

					veryComplexQuery.append(" select_count >= ").append(veryComplexSelectCount);
					veryComplexQuery.append(" or function_used_count >= ").append(veryComplexFunctionUsedCount);
					veryComplexQuery.append(" or join_count >= ").append(veryComplexJoinCount);
					veryComplexQuery.append(" or condition_count >= ").append(veryComplexConditionCount);
					veryComplexQuery.append(" )");

				} else if (GeneralConstants.COMPLEX.equalsIgnoreCase(details.getComplexity())) {

					complexSelectCount = details.getSelectCount();
					complexFunctionUsedCount = details.getFunctionUsedCount();
					complexJoinCount = details.getJoinCount();
					complexConditionCount = details.getConditionCount();

					complexQuery.append(" ( select_count >= ").append(complexSelectCount);
					complexQuery.append(" and select_count < ").append(veryComplexSelectCount);
					complexQuery.append(" ) or ( function_used_count >= ").append(complexFunctionUsedCount);
					complexQuery.append(" and function_used_count < ").append(veryComplexFunctionUsedCount);
					complexQuery.append(" ) or ( join_count >= ").append(complexJoinCount);
					complexQuery.append(" and join_count < ").append(veryComplexJoinCount);
					complexQuery.append(" ) or ( condition_count >= ").append(complexConditionCount);
					complexQuery.append(" and condition_count < ").append(veryComplexConditionCount);
					complexQuery.append(" ))");

				} else if (GeneralConstants.MEDIUM.equalsIgnoreCase(details.getComplexity())) {

					mediumSelectCount = details.getSelectCount();
					mediumFunctionUsedCount = details.getFunctionUsedCount();
					mediumJoinCount = details.getJoinCount();
					mediumConditionCount = details.getConditionCount();

					mediumQuery.append(" ( select_count >= ").append(mediumSelectCount);
					mediumQuery.append(" and select_count < ").append(complexSelectCount);
					mediumQuery.append(" ) or ( function_used_count >= ").append(mediumFunctionUsedCount);
					mediumQuery.append(" and function_used_count < ").append(complexFunctionUsedCount);
					mediumQuery.append(" ) or ( join_count >= ").append(mediumJoinCount);
					mediumQuery.append(" and join_count < ").append(complexJoinCount);
					mediumQuery.append(" ) or ( condition_count >= ").append(mediumConditionCount);
					mediumQuery.append(" and condition_count < ").append(complexConditionCount);
					mediumQuery.append(" ))");

				} else if (GeneralConstants.SIMPLE.equalsIgnoreCase(details.getComplexity())) {

					simpleSelectCount = details.getSelectCount();
					simpleFunctionUsedCount = details.getFunctionUsedCount();
					simpleJoinCount = details.getJoinCount();
					simpleConditionCount = details.getConditionCount();

					simpleQuery.append(" ( select_count >= ").append(simpleSelectCount);
					simpleQuery.append(" and select_count < ").append(mediumSelectCount);
					simpleQuery.append(" ) or ( function_used_count >= ").append(simpleFunctionUsedCount);
					simpleQuery.append(" and function_used_count < ").append(mediumFunctionUsedCount);
					simpleQuery.append(" ) or ( join_count >= ").append(simpleJoinCount);
					simpleQuery.append(" and join_count < ").append(mediumJoinCount);
					simpleQuery.append(" ) or ( condition_count >= ").append(simpleConditionCount);
					simpleQuery.append(" and condition_count < ").append(mediumConditionCount);
					simpleQuery.append(" ))");
				}
			}

			LOGGER.info("veryComplexQuery: {}", veryComplexQuery);
			LOGGER.info("complexQuery: {}", complexQuery);
			LOGGER.info("mediumQuery: {}", mediumQuery);
			LOGGER.info("simpleQuery: {}", simpleQuery);

			Integer simpleCount = lineageJdbcTemplate.queryForObject(Sanitization.sanitizeQuery(simpleQuery.toString()), Integer.class);
			Integer mediumCount = lineageJdbcTemplate.queryForObject(Sanitization.sanitizeQuery(mediumQuery.toString()), Integer.class);
			Integer complexCount = lineageJdbcTemplate.queryForObject(Sanitization.sanitizeQuery(complexQuery.toString()), Integer.class);
			Integer veryComplexCount = lineageJdbcTemplate.queryForObject(Sanitization.sanitizeQuery(veryComplexQuery.toString()), Integer.class);

			if (veryComplexCount > 0) {
				return GeneralConstants.VERY_COMPLEX;
			}
			if (complexCount > 0) {
				return GeneralConstants.COMPLEX;
			}
			if (mediumCount > 0) {
				return GeneralConstants.MEDIUM;
			}
			return GeneralConstants.SIMPLE;
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in calculateComplexityUsingQuery: ", ex);
			throw new LineageBusinessException("Exception occurred in complexity calculation: " + ex.getMessage());
		}
	}

	public void calculateScriptComplexityForInformatica(String projectName, Long jobId, String tech) {
		try {
			tech = Sanitization.sanitizeInput(tech);
			projectName = Sanitization.sanitizeInput(projectName);

			HashSet<Boolean> complexityBoolSet = new HashSet<>();

			Set<String> fileNameSetFromInfoSql = informaticaSourceRepo.getDistinctFilename(jobId);
			Set<String> fileNameListFromInfoComponent = informaticaSourceRepo.getDistinctFilenameFromComponentTable(jobId);
			LOGGER.info("fileNameSetFromInfoSql: {}", fileNameSetFromInfoSql);
			LOGGER.info("fileNameListFromInfoComponent: {}", fileNameListFromInfoComponent);
			fileNameListFromInfoComponent.removeAll(fileNameSetFromInfoSql);
			LOGGER.info("fileNameListFromInfoComponent after removing filename which has both queries and components: {}", fileNameListFromInfoComponent);

			for (String fileName : fileNameListFromInfoComponent) {
				Map<String, Integer> countMap = new HashMap<>();
				getComponentCount(jobId, fileName, countMap);
				String sql1SequenceId = md5AndHashCodeAlgo(fileName.concat(String.valueOf(jobId)));
				QueryComplexityDetails complexityDetails = buildComplexityDetailsForInformatica(sql1SequenceId, jobId,
						projectName, fileName.toUpperCase(), countMap, tech);
				boolean uploadFiles = calculateComplexityAndSaveDetails(projectName, TechnologyConstants.INFORMATICA,
						tech, fileName.toUpperCase(), complexityDetails);
				complexityBoolSet.add(uploadFiles);
			}
			boolean uploadFilesForQuery = calculateScriptComplexityForInformaticaUsingSqlQueryAndTags(projectName, jobId, tech);
			complexityBoolSet.add(uploadFilesForQuery);
			if (fileNameListFromInfoComponent.isEmpty() ||
					(complexityBoolSet.size() == 1 && complexityBoolSet.contains(Boolean.TRUE))) {
				jobStatusService.updateJobStatusDetailsWithComplexityString(jobId);
			} else {
				LOGGER.info("Some error has occurred while calculating complexity for 1 or more files.");
			}
		} catch (Exception ex) {
			LOGGER.error("Exception occurred : ", ex);
		}
	}

	private void getComponentCount(Long jobId, String scriptFile, Map<String, Integer> countMap) {
		List<ComplexityQueryDto> componentTypeCountList =
				informaticaSourceRepo.executeComplexitySuperQuery(jobId, scriptFile);

		int otherComponentCount = 0;

		for (ComplexityQueryDto complexityDto : componentTypeCountList) {
			switch (complexityDto.getComponentType()) {
				case GeneralConstants.CUSTOM_TRANSFORMATION:
					countMap.put(GeneralConstants.CUSTOM_COMPONENT_COUNT, complexityDto.getCount());
					break;
				case GeneralConstants.AGGREGATOR:
					countMap.put(GeneralConstants.AGGREGATOR_COMPONENT_COUNT, complexityDto.getCount());
					break;
				case GeneralConstants.NORMALIZER:
					countMap.put(GeneralConstants.NORMALIZER_COMPONENT_COUNT, complexityDto.getCount());
					break;
				case GeneralConstants.STORED_PROCEDURE:
					countMap.put(GeneralConstants.PROCEDURE_COMPONENT_COUNT, complexityDto.getCount());
					break;
				default:
					otherComponentCount += complexityDto.getCount();
					break;
			}
		}
		countMap.put(GeneralConstants.OTHER_COMPONENT_COUNT, otherComponentCount);
	}

	private QueryComplexityDetails buildComplexityDetailsForInformatica(String sqlSequenceId, Long jobId,
				String projectName, String scriptFile, Map<String, Integer> countMap, String tech ) {

		projectName = Sanitization.sanitizeInput(projectName);
		tech = Sanitization.sanitizeInput(tech);
		scriptFile = Sanitization.sanitizeInput(scriptFile);

		return QueryComplexityDetails.builder()
				.jobId(jobId)
				.id(sqlSequenceId)
				.fileName(scriptFile)
				.projectName(projectName)
				.technology(tech)
				.joinCount(countMap.getOrDefault(GeneralConstants.JOIN_COUNT, 0))
				.selectCount(countMap.getOrDefault(GeneralConstants.SELECT_COUNT, 0))
				.functionUsedCount(countMap.getOrDefault(GeneralConstants.FUNCTION_USED_COUNT, 0))
				.customComponentCount(countMap.getOrDefault(GeneralConstants.CUSTOM_COMPONENT_COUNT, 0))
				.aggregatorComponentCount(countMap.getOrDefault(GeneralConstants.AGGREGATOR_COMPONENT_COUNT, 0))
				.normalizerComponentCount(countMap.getOrDefault(GeneralConstants.NORMALIZER_COMPONENT_COUNT, 0))
				.procedureComponentCount(countMap.getOrDefault(GeneralConstants.PROCEDURE_COMPONENT_COUNT, 0))
				.otherComponentCount(countMap.getOrDefault(GeneralConstants.OTHER_COMPONENT_COUNT, 0))
				.status(GeneralConstants.SUCCESS)
				.build();
	}

	private boolean calculateComplexityAndSaveDetails(String projectName, String parentTech, String tech, String scriptFile,
												   QueryComplexityDetails queryComplexityDetails) {
		try {
			String complexity = "";
			List<QueryComplexityDetails> data =
					queryComplexityDetailsRepository.findRecordsByFileNameAndProjectName(scriptFile, projectName);
			LOGGER.info("Finding if there are records present with fileName: {} and projectName: {}", scriptFile, projectName);

			if (!data.isEmpty()) {
				LOGGER.info("There are existing records with fileName: {} and projectName: {} and no of records: {}",
						scriptFile, projectName, data.size());

				queryComplexityDetailsRepository.deleteRecordsByFileNameAndProjectName(scriptFile, projectName);
				LOGGER.info("Existing records deleted with fileName: {} and projectName: {}", scriptFile, projectName);
			}
			queryComplexityDetails = queryComplexityDetailsRepository.save(queryComplexityDetails);
			LOGGER.info("Record saved: {}", queryComplexityDetails);

			complexity = getComplexity(queryComplexityDetails);
			if (complexity.isEmpty()) {
				return false;
			}
			LOGGER.info("Complexity for fileName: {} and projectName: {}: {}", scriptFile, projectName, complexity);

			Integer noOfRecordsUpdated =
					queryComplexityDetailsRepository.updateComplexityById(queryComplexityDetails.getId(), complexity);
			LOGGER.info("Complexity updated in the database");
			return noOfRecordsUpdated > 0;
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in saving complexity saveComplexityDetails: ", ex);
			return false;
		}
	}

	private String getComplexity(QueryComplexityDetails queryComplexityDetails) throws LineageBusinessException {
		String tech = queryComplexityDetails.getTechnology();
		String id = queryComplexityDetails.getId();
		String complexity;
		List<String> viewTptMLoadList = Arrays.asList(TechnologyConstants.TERADATA_TPT, TechnologyConstants.TERADATA_MLOAD,
				TechnologyConstants.TERADATA_VIEW, TechnologyConstants.ORACLE_VIEW, TechnologyConstants.MS_SQL_SERVER_VIEW);

		if (viewTptMLoadList.stream().anyMatch(tech::equalsIgnoreCase)) {
			complexity = calculateComplexityForViewTptMLoad(id, tech);
		} else if (TechnologyConstants.SSRS.equalsIgnoreCase(tech) ||
				TechnologyConstants.SSIS.equalsIgnoreCase(tech)) {
			complexity = calculateComplexityForSSISandSSRS(id, tech);
		} else if (TechnologyConstants.BI_TABLEAU.equalsIgnoreCase(tech)) {
			complexity = getComplexityUsingTableCount(queryComplexityDetails.getTableCount());
		} else if (TechnologyConstants.INFORMATICA.equalsIgnoreCase(tech)) {
			complexity = calculateComplexityUsingQueryForInformatica(queryComplexityDetails.getId());
		} else {
			complexity = calculateComplexity(id, tech);
		}
		return complexity;
	}

	private String calculateComplexityForSSISandSSRS(String id, String tech) throws LineageBusinessException {
		try {
			tech = Sanitization.sanitizeInput(tech);

			int simpleCreateCount = 0, simpleJoinCount = 0, simpleConditionCount = 0, simpleFunctionUsedCount = 0;
			int mediumCreateCount = 0, mediumJoinCount = 0, mediumConditionCount = 0, mediumFunctionUsedCount = 0;
			int complexCreateCount = 0, complexJoinCount = 0, complexConditionCount = 0, complexFunctionUsedCount = 0;
			int veryComplexCreateCount = 0, veryComplexJoinCount = 0, veryComplexConditionCount = 0, veryComplexFunctionUsedCount = 0;

			StringBuilder simpleQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");
			StringBuilder mediumQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");
			StringBuilder complexQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");
			StringBuilder veryComplexQuery = new StringBuilder("select count(*) from semantic.script_complexity where id = '").append(id).append("' and (");

			List<ComplexityDetails> complexityList = complexityDetailsRepo.findByTechnologyOrderByIdDesc(tech);

			for (ComplexityDetails details : complexityList) {
				if (GeneralConstants.VERY_COMPLEX.equalsIgnoreCase(details.getComplexity())) {

					veryComplexCreateCount = details.getCreateQueryCount();
					veryComplexFunctionUsedCount = details.getFunctionUsedCount();
					veryComplexJoinCount = details.getJoinCount();
					veryComplexConditionCount = details.getConditionCount();

					veryComplexQuery.append(" create_count >= ").append(veryComplexCreateCount);
					veryComplexQuery.append(" or function_used_count >= ").append(veryComplexFunctionUsedCount);
					veryComplexQuery.append(" or join_count >= ").append(veryComplexJoinCount);
					veryComplexQuery.append(" or condition_count >= ").append(veryComplexConditionCount);
					veryComplexQuery.append(" )");

				} else if (GeneralConstants.COMPLEX.equalsIgnoreCase(details.getComplexity())) {

					complexCreateCount = details.getCreateQueryCount();
					complexFunctionUsedCount = details.getFunctionUsedCount();
					complexJoinCount = details.getJoinCount();
					complexConditionCount = details.getConditionCount();

					complexQuery.append(" ( create_count >= ").append(complexCreateCount);
					complexQuery.append(" and create_count < ").append(veryComplexCreateCount);
					complexQuery.append(" ) or ( function_used_count >= ").append(complexFunctionUsedCount);
					complexQuery.append(" and function_used_count < ").append(veryComplexFunctionUsedCount);
					complexQuery.append(" ) or ( join_count >= ").append(complexJoinCount);
					complexQuery.append(" and join_count < ").append(veryComplexJoinCount);
					complexQuery.append(" ) or ( condition_count >= ").append(complexConditionCount);
					complexQuery.append(" and condition_count < ").append(veryComplexConditionCount);
					complexQuery.append(" ))");

				} else if (GeneralConstants.MEDIUM.equalsIgnoreCase(details.getComplexity())) {

					mediumCreateCount = details.getCreateQueryCount();
					mediumFunctionUsedCount = details.getFunctionUsedCount();
					mediumJoinCount = details.getJoinCount();
					mediumConditionCount = details.getConditionCount();

					mediumQuery.append(" ( create_count >= ").append(mediumCreateCount);
					mediumQuery.append(" and create_count < ").append(complexCreateCount);
					mediumQuery.append(" ) or ( function_used_count >= ").append(mediumFunctionUsedCount);
					mediumQuery.append(" and function_used_count < ").append(complexFunctionUsedCount);
					mediumQuery.append(" ) or ( join_count >= ").append(mediumJoinCount);
					mediumQuery.append(" and join_count < ").append(complexJoinCount);
					mediumQuery.append(" ) or ( condition_count >= ").append(mediumConditionCount);
					mediumQuery.append(" and condition_count < ").append(complexConditionCount);
					mediumQuery.append(" ))");

				} else if (GeneralConstants.SIMPLE.equalsIgnoreCase(details.getComplexity())) {

					simpleCreateCount = details.getCreateQueryCount();
					simpleFunctionUsedCount = details.getFunctionUsedCount();
					simpleJoinCount = details.getJoinCount();
					simpleConditionCount = details.getConditionCount();

					simpleQuery.append(" ( create_count >= ").append(simpleCreateCount);
					simpleQuery.append(" and create_count < ").append(mediumCreateCount);
					simpleQuery.append(" ) or ( function_used_count >= ").append(simpleFunctionUsedCount);
					simpleQuery.append(" and function_used_count < ").append(mediumFunctionUsedCount);
					simpleQuery.append(" ) or ( join_count >= ").append(simpleJoinCount);
					simpleQuery.append(" and join_count < ").append(mediumJoinCount);
					simpleQuery.append(" ) or ( condition_count >= ").append(simpleConditionCount);
					simpleQuery.append(" and condition_count < ").append(mediumConditionCount);
					simpleQuery.append(" ))");
				}
			}

			LOGGER.info("veryComplexQuery: {}", veryComplexQuery);
			LOGGER.info("complexQuery: {}", complexQuery);
			LOGGER.info("mediumQuery: {}", mediumQuery);
			LOGGER.info("simpleQuery: {}", simpleQuery);

			Integer simpleCount = lineageJdbcTemplate.queryForObject(Sanitization.sanitizeQuery(simpleQuery.toString()), Integer.class);
			Integer mediumCount = lineageJdbcTemplate.queryForObject(Sanitization.sanitizeQuery(mediumQuery.toString()), Integer.class);
			Integer complexCount = lineageJdbcTemplate.queryForObject(Sanitization.sanitizeQuery(complexQuery.toString()), Integer.class);
			Integer veryComplexCount = lineageJdbcTemplate.queryForObject(Sanitization.sanitizeQuery(veryComplexQuery.toString()), Integer.class);

			if (veryComplexCount > 0) {
				return GeneralConstants.VERY_COMPLEX;
			}
			if (complexCount > 0) {
				return GeneralConstants.COMPLEX;
			}
			if (mediumCount > 0) {
				return GeneralConstants.MEDIUM;
			}
			return GeneralConstants.SIMPLE;
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in calculateComplexityUsingQuery: ", ex);
			throw new LineageBusinessException("Exception occurred in complexity calculation: " + ex.getMessage());
		}
	}

	private QueryComplexityDetails buildComplexityDetailsForViewTptMLoad(String sql1SequenceId, int jobId,
																		 String projectName, String scriptFile,
																		 String tech, String parentTech,
																		 Map<String, Integer> countMap) {
		if (TechnologyConstants.VIEW.equalsIgnoreCase(tech)) {
			if (TechnologyConstants.MS_SQL_SERVER.equalsIgnoreCase(parentTech)) {
				tech = TechnologyConstants.MS_SQL_SERVER_VIEW;
			} else if (TechnologyConstants.TERADATA.equalsIgnoreCase(parentTech)) {
				tech = TechnologyConstants.TERADATA_VIEW;
			} else if (TechnologyConstants.ORACLE.equalsIgnoreCase(parentTech)) {
				tech = TechnologyConstants.ORACLE_VIEW;
			}
		} else if (TechnologyConstants.MLOAD.equalsIgnoreCase(tech)) {
			tech = TechnologyConstants.TERADATA_MLOAD;
		} else {
			tech = TechnologyConstants.TERADATA_TPT;
		}
		return QueryComplexityDetails.builder()
				.jobId((long) jobId)
				.id(sql1SequenceId)
				.fileName(scriptFile)
				.projectName(projectName)
				.technology(tech)
				.selectCount(countMap.getOrDefault(GeneralConstants.SELECT_COUNT, 0))
				.functionUsedCount(countMap.getOrDefault(GeneralConstants.FUNCTION_USED_COUNT, 0))
				.joinCount(countMap.getOrDefault(GeneralConstants.JOIN_COUNT, 0))
				.conditionCount(countMap.getOrDefault(GeneralConstants.CONDITION_COUNT, 0))
				.status(GeneralConstants.SUCCESS)
				.build();
	}

	private QueryComplexityDetails buildComplexityDetailsForSSISandSSRS(String sql1SequenceId, int jobId,
																		String projectName, String scriptFile,
																		String tech, String parentTech,
																		Map<String, Integer> countMap) {
		return QueryComplexityDetails.builder()
				.jobId((long) jobId)
				.id(sql1SequenceId)
				.fileName(scriptFile)
				.projectName(projectName)
				.technology(tech)
				.createCount(countMap.getOrDefault(GeneralConstants.CREATE_COUNT, 0))
				.functionUsedCount(countMap.getOrDefault(GeneralConstants.FUNCTION_USED_COUNT, 0))
				.joinCount(countMap.getOrDefault(GeneralConstants.JOIN_COUNT, 0))
				.conditionCount(countMap.getOrDefault(GeneralConstants.CONDITION_COUNT, 0))
				.status(GeneralConstants.SUCCESS)
				.build();
	}


	private QueryComplexityDetails buildComplexityDetails(String sql1SequenceId, int jobId, String projectName,
														  String scriptFile, String technology, String parentTech,
														  Map<String, Integer> countMap) {
		LOGGER.info("buildComplexityDetails - parentTech: {}, tech: {}", parentTech, technology);
		if (TechnologyConstants.TERADATA.equalsIgnoreCase(parentTech)) {
			if (TechnologyConstants.BTEQ.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.TERADATA_BTEQ;
			} else if (TechnologyConstants.FUNCTION.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.TERADATA_FUNCTION;
			} else if (TechnologyConstants.PROCEDURE.equalsIgnoreCase(technology) ||
					TechnologyConstants.ACTION_STORED_PROCEDURE.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.TERADATA_PROCEDURE;
			} else if (TechnologyConstants.MACRO.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.TERADATA_MACRO;
			}
		} else if (TechnologyConstants.ORACLE.equalsIgnoreCase(parentTech)) {
			if (TechnologyConstants.FUNCTION.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.ORACLE_FUNCTION;
			} else if (TechnologyConstants.PROCEDURE.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.ORACLE_PROCEDURE;
			} else if (TechnologyConstants.PACKAGE.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.ORACLE_PACKAGE;
			} else if (TechnologyConstants.TRIGGER.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.ORACLE_TRIGGER;
			} else {
				technology = TechnologyConstants.ORACLE_ODI;
			}
		} else if (TechnologyConstants.MS_SQL_SERVER.equalsIgnoreCase(parentTech)) {
			if (TechnologyConstants.FUNCTION.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.MS_SQL_SERVER_FUNCTION;
			} else if (TechnologyConstants.PROCEDURE.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.MS_SQL_SERVER_PROCEDURE;
			} else if (TechnologyConstants.TRIGGER.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.MS_SQL_SERVER_TRIGGER;
			}
		} else if (TechnologyConstants.BI.equalsIgnoreCase(parentTech)) {
			if (TechnologyConstants.POWERBI.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.POWER_BI;
			} else if (TechnologyConstants.QLIKVIEW.equalsIgnoreCase(technology)) {
				technology = TechnologyConstants.BI_QLIKVIEW;
			}
		}
		LOGGER.info("Modified technology: {}", technology);

		return QueryComplexityDetails.builder()
				.id(sql1SequenceId)
				.jobId((long) jobId)
				.fileName(scriptFile)
				.projectName(projectName)
				.technology(technology)
				.queryCount(countMap.getOrDefault(GeneralConstants.QUERY_COUNT, 0))
				.functionUsedCount(countMap.getOrDefault(GeneralConstants.FUNCTION_USED_COUNT, 0))
				.insertCount(countMap.getOrDefault(GeneralConstants.INSERT_COUNT, 0))
				.updateCount(-countMap.getOrDefault(GeneralConstants.UPDATE_COUNT, 0))
				.deleteCount(countMap.getOrDefault(GeneralConstants.DELETE_COUNT, 0))
				.createCount(countMap.getOrDefault(GeneralConstants.CREATE_COUNT, 0))
				.mergeCount(countMap.getOrDefault(GeneralConstants.MERGE_COUNT, 0))
				.joinCount(countMap.getOrDefault(GeneralConstants.JOIN_COUNT, 0))
				.conditionCount(countMap.getOrDefault(GeneralConstants.CONDITION_COUNT, 0))
				.status(GeneralConstants.SUCCESS)
				.build();
	}

	public String calculateComplexity(String id, String tech) {
		int lowerCount=0;
		int mediumCount=0;
		int veryComplexCount=0;
		int highComplexCount=0;
		String complexity="";
		try {
			LOGGER.info("getComplexity id: {}, tech: {}", id, tech);
			List<ComplexityDetails> complexityLevelList = complexityDetailsRepo.findByTechnologyOrderByIdDesc(tech);
			LOGGER.info("Complexity levels: {}", complexityLevelList);

			long lowerCountForQueries=0;
			long lowerCountForFunctionUsedCount=0;
			long lowerCountForInsertCount=0;
			long lowerCountForUpdateCount=0;
			long lowerCountForDeleteCount=0;
			long lowerCountForCreateCount=0;
			long lowerCountForMergeCount=0;
			long lowerCountForJoin=0;
			long lowerCountForCondition=0;

			long mediumCountForQueries=0;
			long mediumCountForFunctionUsedCount=0;
			long mediumCountForInsertCount=0;
			long mediumCountForUpdateCount=0;
			long mediumCountForDeleteCount=0;
			long mediumCountForCreateCount=0;
			long mediumCountForMergeCount=0;
			long mediumCountForJoin=0;
			long mediumCountForCondition=0;

			long veryComplexCountForQueries=0;
			long veryComplexCountForFunctionUsedCount=0;
			long veryComplexCountForInsertCount=0;
			long veryComplexCountForUpdateCount=0;
			long veryComplexCountForDeleteCount=0;
			long veryComplexCountForCreateCount=0;
			long veryComplexCountForMergeCount=0;
			long veryComplexCountForJoin=0;
			long veryComplexCountForCondition=0;

			long complexCountForQueries=0;
			long complexCountForFunctionUsedCount=0;
			long complexCountForInsertCount=0;
			long complexCountForUpdateCount=0;
			long complexCountForDeleteCount=0;
			long complexCountForCreateCount=0;
			long complexCountForMergeCount=0;
			long complexCountForJoin=0;
			long complexCountForCondition=0;

			StringBuilder lowerWhereCondition = new StringBuilder();
			StringBuilder mediumWhereCondition = new StringBuilder();
			StringBuilder veryComplexWhereCondition = new StringBuilder();
			StringBuilder highComplexWhereCondition = new StringBuilder();

			for (ComplexityDetails complexityLevel : complexityLevelList) {
				if (GeneralConstants.SIMPLE.equalsIgnoreCase(complexityLevel.getComplexity())) {

					lowerCountForQueries = complexityLevel.getQueryCount();
					lowerCountForFunctionUsedCount = complexityLevel.getFunctionUsedCount();
					lowerCountForInsertCount = complexityLevel.getInsertQueryCount();
					lowerCountForUpdateCount = complexityLevel.getUpdateQueryCount();
					lowerCountForDeleteCount = complexityLevel.getDeleteQueryCount();
					lowerCountForCreateCount = complexityLevel.getCreateQueryCount();
					lowerCountForMergeCount = complexityLevel.getMergeQueryCount();
					lowerCountForJoin = complexityLevel.getJoinCount();
					lowerCountForCondition = complexityLevel.getConditionCount();
//					lowerCountForTotalNoOfLines = complexityLevel.getTotalNoOfLines();

					lowerWhereCondition.append(" query_count >= ").append(lowerCountForQueries);
					lowerWhereCondition.append(" or function_used_count >= ").append(lowerCountForFunctionUsedCount);
					lowerWhereCondition.append(" or insert_count >= ").append(lowerCountForInsertCount);
					lowerWhereCondition.append(" or update_count >= ").append(lowerCountForUpdateCount);
					lowerWhereCondition.append(" or delete_count >= ").append(lowerCountForDeleteCount);
					lowerWhereCondition.append(" or create_count >= ").append(lowerCountForCreateCount);
					lowerWhereCondition.append(" or merge_count >= ").append(lowerCountForMergeCount);
					lowerWhereCondition.append(" or join_count >= ").append(lowerCountForJoin);
					lowerWhereCondition.append(" or condition_count >= ").append(lowerCountForCondition)
//					lowerWhereCondition.append(" or total_no_of_lines >= ").append(lowerCountForTotalNoOfLines)
							.append(")");

				} else if (GeneralConstants.MEDIUM.equalsIgnoreCase(complexityLevel.getComplexity())) {
					mediumCountForQueries = complexityLevel.getQueryCount();
					mediumCountForFunctionUsedCount = complexityLevel.getFunctionUsedCount();
					mediumCountForInsertCount = complexityLevel.getInsertQueryCount();
					mediumCountForUpdateCount = complexityLevel.getUpdateQueryCount();
					mediumCountForDeleteCount = complexityLevel.getDeleteQueryCount();
					mediumCountForCreateCount = complexityLevel.getCreateQueryCount();
					mediumCountForMergeCount = complexityLevel.getMergeQueryCount();
					mediumCountForJoin = complexityLevel.getJoinCount();
					mediumCountForCondition = complexityLevel.getConditionCount();
//					mediumCountForTotalNoOfLines = complexityLevel.getTotalNoOfLines();

					mediumWhereCondition.append(" ( query_count >= ").append(mediumCountForQueries)
							.append(" and query_count < ").append(complexCountForQueries);
					mediumWhereCondition.append(" ) or ( function_used_count >= ").append(mediumCountForFunctionUsedCount)
							.append(" and function_used_count < ").append(complexCountForFunctionUsedCount);
					mediumWhereCondition.append(" ) or ( insert_count >= ").append(mediumCountForInsertCount)
							.append(" and insert_count < ").append(complexCountForInsertCount);
					mediumWhereCondition.append(" ) or ( update_count >= ").append(mediumCountForUpdateCount)
							.append(" and update_count < ").append(complexCountForUpdateCount);
					mediumWhereCondition.append(" ) or ( delete_count >= ").append(mediumCountForDeleteCount)
							.append(" and delete_count < ").append(complexCountForDeleteCount);
					mediumWhereCondition.append(" ) or ( create_count >= ").append(mediumCountForCreateCount)
							.append(" and create_count < ").append(complexCountForCreateCount);
					mediumWhereCondition.append(" ) or ( merge_count >= ").append(mediumCountForMergeCount)
							.append(" and merge_count < ").append(complexCountForMergeCount);
					mediumWhereCondition.append(" ) or ( join_count >= ").append(mediumCountForJoin)
							.append(" and join_count < ").append(complexCountForJoin);
					mediumWhereCondition.append(" ) or ( condition_count >= ").append(mediumCountForCondition)
							.append(" and condition_count < ").append(complexCountForCondition)
//					mediumWhereCondition.append(" ) or ( total_no_of_lines >= ").append(mediumCountForTotalNoOfLines)
//					.append(" and total_no_of_lines < ").append(complexCountForTotalNoOfLines)
							.append("))");

				} else if (GeneralConstants.COMPLEX.equalsIgnoreCase(complexityLevel.getComplexity().trim())) {

					complexCountForQueries = complexityLevel.getQueryCount();
					complexCountForFunctionUsedCount = complexityLevel.getFunctionUsedCount();
					complexCountForInsertCount = complexityLevel.getInsertQueryCount();
					complexCountForUpdateCount = complexityLevel.getUpdateQueryCount();
					complexCountForDeleteCount = complexityLevel.getDeleteQueryCount();
					complexCountForCreateCount = complexityLevel.getCreateQueryCount();
					complexCountForMergeCount = complexityLevel.getMergeQueryCount();
					complexCountForJoin = complexityLevel.getJoinCount();
					complexCountForCondition  = complexityLevel.getConditionCount();
//					complexCountForTotalNoOfLines = complexityLevel.getTotalNoOfLines();

					highComplexWhereCondition.append(" ( query_count  >= ").append(complexCountForQueries)
							.append(" and query_count  < ").append(veryComplexCountForQueries);
					highComplexWhereCondition.append(" ) or ( function_used_count >= ").append(complexCountForFunctionUsedCount)
							.append(" and function_used_count < ").append(veryComplexCountForFunctionUsedCount);
					highComplexWhereCondition.append(" ) or ( insert_count >= ").append(complexCountForInsertCount)
							.append(" and insert_count < ").append(veryComplexCountForInsertCount);
					highComplexWhereCondition.append(" ) or ( update_count >= ").append(complexCountForUpdateCount)
							.append(" and update_count < ").append(veryComplexCountForUpdateCount);
					highComplexWhereCondition.append(" ) or ( delete_count >= ").append(complexCountForDeleteCount)
							.append(" and delete_count < ").append(veryComplexCountForDeleteCount);
					highComplexWhereCondition.append(" ) or ( create_count >= ").append(complexCountForCreateCount)
							.append(" and create_count < ").append(veryComplexCountForCreateCount);
					highComplexWhereCondition.append(" ) or ( merge_count >= ").append(complexCountForMergeCount)
							.append(" and merge_count < ").append(veryComplexCountForMergeCount);
					highComplexWhereCondition.append(" ) or ( join_count >= ").append(complexCountForJoin)
							.append(" and join_count < ").append(veryComplexCountForJoin);
					highComplexWhereCondition.append(" ) or ( condition_count >= ").append(complexCountForCondition)
							.append(" and condition_count < ").append(veryComplexCountForCondition)
//					highComplexWhereCondition.append(" ) or ( total_no_of_lines >= ").append(complexCountForTotalNoOfLines)
//					.append(" and total_no_of_lines < ").append(veryComplexCountForTotalNoOfLines)
							.append("))");

				} else if (GeneralConstants.VERY_COMPLEX.equalsIgnoreCase(complexityLevel.getComplexity())) {

					veryComplexCountForQueries = complexityLevel.getQueryCount();
					veryComplexCountForFunctionUsedCount = complexityLevel.getFunctionUsedCount();
					veryComplexCountForInsertCount = complexityLevel.getInsertQueryCount();
					veryComplexCountForUpdateCount = complexityLevel.getUpdateQueryCount();
					veryComplexCountForDeleteCount = complexityLevel.getDeleteQueryCount();
					veryComplexCountForCreateCount = complexityLevel.getCreateQueryCount();
					veryComplexCountForMergeCount = complexityLevel.getMergeQueryCount();
					veryComplexCountForJoin = complexityLevel.getJoinCount();
					veryComplexCountForCondition = complexityLevel.getConditionCount();
//					veryComplexCountForTotalNoOfLines = complexityLevel.getTotalNoOfLines();

					veryComplexWhereCondition.append(" query_count >=").append(veryComplexCountForQueries);
					veryComplexWhereCondition.append(" or function_used_count >= ").append(veryComplexCountForFunctionUsedCount);
					veryComplexWhereCondition.append(" or insert_count >= ").append(veryComplexCountForInsertCount);
					veryComplexWhereCondition.append(" or update_count >= ").append(veryComplexCountForUpdateCount);
					veryComplexWhereCondition.append(" or delete_count >= ").append(veryComplexCountForDeleteCount);
					veryComplexWhereCondition.append(" or create_count >= ").append(veryComplexCountForCreateCount);
					veryComplexWhereCondition.append(" or merge_count >= ").append(veryComplexCountForMergeCount);
					veryComplexWhereCondition.append(" or join_count >= ").append(veryComplexCountForJoin);
					veryComplexWhereCondition.append(" or condition_count >= ").append(veryComplexCountForCondition)
//					veryComplexWhereCondition.append(" or total_no_of_lines >= ").append(complexCountForTotalNoOfLines)
							.append(")");
				}
			}

			LOGGER.info("lowerWhereCondition "+lowerWhereCondition);
			LOGGER.info("mediumWhereCondition "+mediumWhereCondition);
			LOGGER.info("veryComplexWhereCondition "+veryComplexWhereCondition);
			LOGGER.info("highComplexWhereCondition "+highComplexWhereCondition);

			String lowerCountQuery="select count(*) from semantic.script_complexity where ("+lowerWhereCondition +" and id='"+id+"'";
			lowerCountQuery = Sanitization.sanitizeQuery(lowerCountQuery);

			LOGGER.info("lowerCountQuery "+lowerCountQuery);
			lowerCount = lineageJdbcTemplate.queryForObject(lowerCountQuery, Integer.class);
			LOGGER.info("lowerCount "+lowerCount);

			String mediumCountQuery="select count(*) from semantic.script_complexity where ("+mediumWhereCondition +" and id='"+id+"'";
			mediumCountQuery = Sanitization.sanitizeQuery(mediumCountQuery);

			LOGGER.info("mediumCountQuery "+mediumCountQuery);
			mediumCount = lineageJdbcTemplate.queryForObject(mediumCountQuery, Integer.class);
			LOGGER.info("mediumCount "+mediumCount);

			String highComplexCountQuery="select count(*) from semantic.script_complexity where ("+highComplexWhereCondition +" and id='"+id+"'";
			highComplexCountQuery = Sanitization.sanitizeQuery(highComplexCountQuery);

			LOGGER.info("highComplexCountQuery "+highComplexCountQuery);
			highComplexCount = lineageJdbcTemplate.queryForObject(highComplexCountQuery, Integer.class);
			LOGGER.info("highComplexCount "+highComplexCount);

			String veryComplexCountQuery="select count(*) from semantic.script_complexity where ("+veryComplexWhereCondition +" and id='"+id+"'";
			veryComplexCountQuery = Sanitization.sanitizeQuery(veryComplexCountQuery);

			LOGGER.info("veryComplexCountQuery "+veryComplexCountQuery);
			veryComplexCount = lineageJdbcTemplate.queryForObject(veryComplexCountQuery, Integer.class);
			//System.out.println("veryComplexCount after " + veryComplexCount);
			LOGGER.info("veryComplexCount "+veryComplexCount);

			if (veryComplexCount > 0) {
				complexity = "Very Complex";
			} else if (highComplexCount > 0) {
				complexity = "Complex";
			} else if (mediumCount > 0) {
				complexity = "Medium";
			} else {
				complexity = "Simple";
			}
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in getComplexity Dao: ",ex);
		}
		return complexity;
	}
}