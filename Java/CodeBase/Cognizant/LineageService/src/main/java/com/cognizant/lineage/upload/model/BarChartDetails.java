package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class BarChartDetails {
	
	private Object category;
	private Object etlAndEltCount = 0;
	private Object analyticsCount = 0;
	private Object databaseObjectsCount = 0;	
	private Object dbSizeSum = 0;
}
