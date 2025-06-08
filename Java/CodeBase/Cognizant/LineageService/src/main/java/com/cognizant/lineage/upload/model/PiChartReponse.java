package com.cognizant.lineage.upload.model;

import java.util.List;

public class PiChartReponse {
	
	private List<PiChartDetailsForTableFileView> objectPiChartDetails;
	private Object totalCount;
	
	public List<PiChartDetailsForTableFileView> getObjectPiChartDetails() {
		return objectPiChartDetails;
	}
	public void setObjectPiChartDetails(List<PiChartDetailsForTableFileView> objectPiChartDetails) {
		this.objectPiChartDetails = objectPiChartDetails;
	}
	public Object getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(Object totalCount) {
		this.totalCount = totalCount;
	}
	
}
