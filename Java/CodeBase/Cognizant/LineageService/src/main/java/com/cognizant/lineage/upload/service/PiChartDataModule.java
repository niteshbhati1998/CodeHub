package com.cognizant.lineage.upload.service;

import java.util.List;

public class PiChartDataModule {
	
	private List<PiScriptCountModule> etlPiChartDetails;
	private Object totalETLCount;
	private List<PiScriptCountModule> reportPiChartDetails;
	private Object totalReportCount;
	
	public List<PiScriptCountModule> getEtlPiChartDetails() {
		return etlPiChartDetails;
	}
	public void setEtlPiChartDetails(List<PiScriptCountModule> etlPiChartDetails) {
		this.etlPiChartDetails = etlPiChartDetails;
	}
	public Object getTotalETLCount() {
		return totalETLCount;
	}
	public void setTotalETLCount(Object totalETLCount) {
		this.totalETLCount = totalETLCount;
	}
	public List<PiScriptCountModule> getReportPiChartDetails() {
		return reportPiChartDetails;
	}
	public void setReportPiChartDetails(List<PiScriptCountModule> reportPiChartDetails) {
		this.reportPiChartDetails = reportPiChartDetails;
	}
	public Object getTotalReportCount() {
		return totalReportCount;
	}
	public void setTotalReportCount(Object totalReportCount) {
		this.totalReportCount = totalReportCount;
	}
	@Override
	public String toString() {
		return "PiChartDataModule [etlPiChartDetails=" + etlPiChartDetails + ", totalCount=" + totalETLCount
				+ ", reportPiChartDetails=" + reportPiChartDetails + ", totalReportCount=" + totalReportCount + "]";
	}

}
