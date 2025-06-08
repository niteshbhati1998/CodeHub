package com.cognizant.lineage.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.exception.LineageBusinessException;
import com.cognizant.lineage.upload.constants.GeneralConstants;

@Component
public class Sanitization {

	public static String sanitizeInput(String input) {
		String sanitizedString = "";
		try {
			if (input == null || input.isEmpty())
			    return input;
			else 
				sanitizedString = input.replaceAll("[^A-Za-z0-9(),!/^[-@.\\/#&+'\\w]*$/]", "");
		} catch(Exception ex) {
			System.out.println("Exception occurred in sanitizeInput "+ex.getMessage());
		}
		return sanitizedString;	
	}
	
	public static String sanitizeQuery(String input) {
		String sanitizedString = "";
		try {
			if (input == null || input.isEmpty())
			    return input;
			else 
				sanitizedString = input.replaceAll("[^\\n\\r\\t\\p{Print}]", "");
		} catch(Exception ex) {
			System.out.println("Exception occurred in sanitizeQuery "+ex.getMessage());
		}
		return sanitizedString;	
	}
	
	public static String validateIntegerInput(String input) throws Exception {   
		try {
			Integer.parseInt(input);
			return input;
		} catch (NumberFormatException e) {
          throw new LineageBusinessException("Input is not an Integer");
		}
	}

	public static void sanitizeFileName(MultipartFile file) throws LineageBusinessException {
		if (GeneralConstants.SQL_KEY_WORDS_LIST_FOR_FILE.stream().anyMatch(file.getName().toLowerCase()::contains)) {
			throw new LineageBusinessException("File name should not contain select/create/insert/update/delete/drop/truncate words. Please try with other name");
		}
	}

	public static void sanitizeDirectory(String path) throws LineageBusinessException {
		if (GeneralConstants.SQL_KEY_WORDS_LIST_FOR_PATH.stream().anyMatch(path.toLowerCase()::contains)) {
			throw new LineageBusinessException("Directory path should not contain select/create/insert/update/delete/drop/truncate words. Please try with other name");
		}
	}

	public static String sanitizeInputForWhiteSpace(String input) throws LineageBusinessException {
		if (input.contains(" ")) {
			throw new LineageBusinessException("Input should not contain whitespace character. Please try without whitespace");
		}
		return input;
	}
}
