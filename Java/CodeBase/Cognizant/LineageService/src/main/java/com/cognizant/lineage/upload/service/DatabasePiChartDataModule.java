package com.cognizant.lineage.upload.service;

import java.util.List;

public class DatabasePiChartDataModule {
	
	private List<PiScriptCountModule> databsePiChartDetails;
	private Object totalDatabaseScriptCount;
	private List<PiScriptCountModule> objectSizePiChartDetails;
	private Object totalObjectSizeSum;
	
	public List<PiScriptCountModule> getObjectSizePiChartDetails() {
		return objectSizePiChartDetails;
	}
	public void setObjectSizePiChartDetails(List<PiScriptCountModule> objectSizePiChartDetails) {
		this.objectSizePiChartDetails = objectSizePiChartDetails;
	}
	public Object getTotalObjectSizeSum() {
		return totalObjectSizeSum;
	}
	public void setTotalObjectSizeSum(Object totalObjectSizeSum) {
		this.totalObjectSizeSum = totalObjectSizeSum;
	}
	public List<PiScriptCountModule> getDatabsePiChartDetails() {
		return databsePiChartDetails;
	}
	public void setDatabsePiChartDetails(List<PiScriptCountModule> databsePiChartDetails) {
		this.databsePiChartDetails = databsePiChartDetails;
	}
	public Object getTotalDatabaseScriptCount() {
		return totalDatabaseScriptCount;
	}
	public void setTotalDatabaseScriptCount(Object totalDatabaseScriptCount) {
		this.totalDatabaseScriptCount = totalDatabaseScriptCount;
	}

}
