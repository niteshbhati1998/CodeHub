package com.cognizant.lineage.upload.constants;

/**
 * The QueryConstant
 * 
 * @author
 *
 */
public class ColumnLineageQueryConstant {

	private ColumnLineageQueryConstant() {
		throw new IllegalAccessError("Utility class");
	}
	public static final String SEQUENCE_ID = "execution_id_seq";
		
	public static final String SQL_SEQUENCE_ID = "select nextval('" + SEQUENCE_ID + "')";
	public static final String GET_ALL_TABLE_NAMES ="select distinct TABLE_NAME from "
			+ "COLUMN_LINEAGE.ALL_COLUMN_MASTER where PROJECT_NAME =? order by TABLE_NAME asc";
	public static final String GET_COLUMN_NAMES_BY_TABLE_NAME ="select distinct COLUMN_NAME from "
			+ "COLUMN_LINEAGE.ALL_COLUMN_MASTER where PROJECT_NAME =? and TABLE_NAME =? order by COLUMN_NAME asc";

}
