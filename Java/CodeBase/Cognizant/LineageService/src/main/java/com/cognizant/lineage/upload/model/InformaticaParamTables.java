package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class InformaticaParamTables {

	private String fileName;
	private String sessionName;
	private String fromTable;
	private String toTable;
	private String paramFilename;
}
