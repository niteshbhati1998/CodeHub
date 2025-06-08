package com.cognizant.lineage.upload.model;

public class ScriptCalculationDetails {

	private int functionCount;
	private int joinCount;
	private int selectCount;
	private int transformationCount;
	public int getFunctionCount() {
		return functionCount;
	}
	public void setFunctionCount(int functionCount) {
		this.functionCount = functionCount;
	}
	public int getJoinCount() {
		return joinCount;
	}
	public void setJoinCount(int joinCount) {
		this.joinCount = joinCount;
	}
	public int getSelectCount() {
		return selectCount;
	}
	public void setSelectCount(int selectCount) {
		this.selectCount = selectCount;
	}
	public int getTransformationCount() {
		return transformationCount;
	}
	public void setTransformationCount(int transformationCount) {
		this.transformationCount = transformationCount;
	}
}
