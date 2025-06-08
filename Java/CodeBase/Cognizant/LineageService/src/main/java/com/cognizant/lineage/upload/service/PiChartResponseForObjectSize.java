package com.cognizant.lineage.upload.service;

import java.util.List;

public class PiChartResponseForObjectSize {
	
	private List<PiChartDetailsForObjectSize> piChartDetailsForObjectSizeList;
	private Object totalSum;
	
	public List<PiChartDetailsForObjectSize> getPiChartDetailsForObjectSizeList() {
		return piChartDetailsForObjectSizeList;
	}
	public void setPiChartDetailsForObjectSizeList(List<PiChartDetailsForObjectSize> piChartDetailsForObjectSizeList) {
		this.piChartDetailsForObjectSizeList = piChartDetailsForObjectSizeList;
	}
	public Object getTotalSum() {
		return totalSum;
	}
	public void setTotalSum(Object totalSum) {
		this.totalSum = totalSum;
	}

}
