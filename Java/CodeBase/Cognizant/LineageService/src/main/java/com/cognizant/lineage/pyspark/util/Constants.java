package com.cognizant.lineage.pyspark.util;

public class Constants {
    public static final String STRING_REPLACEMENT_VARIABLE = "stringReplacementVariable__";
    public static final String EOF = "EOF";
    public static final String SPL_CHAR_REPLACE_PATTERN = "[^a-zA-Z0-9_=+.\\s]";
    public static final String SPL_CHAR_REPLACE_PATTERN_1 = "[^a-zA-Z0-9_=+\\s]";
    public static final String ENCLOSED_STRING_PATTERN = "(\"[^\"]*\")";
    public static final String SPACE = " ";
    public static final String NO_SPACE = "";

    public static final String[] SQL_KEY_WORDS = {"SELECT ", " MERGE ", "UPDATE ", "INSERT INTO "};
}
