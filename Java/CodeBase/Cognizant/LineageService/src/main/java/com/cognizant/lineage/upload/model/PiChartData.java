package com.cognizant.lineage.upload.model;

import java.util.List;

public class PiChartData {

	private List<PiScriptCount> techPiChartDetails;
	private Object totalCount;
	private List<PiScriptCount> reportPiChartDetails;
	private Object totalReportCount;

	public List<PiScriptCount> getTechPiChartDetails() {
		return techPiChartDetails;
	}

	public void setTechPiChartDetails(List<PiScriptCount> techPiChartDetails) {
		this.techPiChartDetails = techPiChartDetails;
	}

	public List<PiScriptCount> getReportPiChartDetails() {
		return reportPiChartDetails;
	}

	public void setReportPiChartDetails(List<PiScriptCount> reportPiChartDetails) {
		this.reportPiChartDetails = reportPiChartDetails;
	}

	public Object getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Object totalCount) {
		this.totalCount = totalCount;
	}

	public Object getTotalReportCount() {
		return totalReportCount;
	}

	public void setTotalReportCount(Object totalReportCount) {
		this.totalReportCount = totalReportCount;
	}

	@Override
	public String toString() {
		return "PiChartData [techPiChartDetails=" + techPiChartDetails + ", totalCount=" + totalCount
				+ ", reportPiChartDetails=" + reportPiChartDetails + ", totalReportCount=" + totalReportCount + "]";
	}

}
