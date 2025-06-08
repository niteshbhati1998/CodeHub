package com.cognizant.lineage.upload.model;

public class PiTableNSizeCount {
	
	private String technology;
	private long tableCount;
	private long objectSizeCount;
	
	public PiTableNSizeCount() {
		super();
	}
	public PiTableNSizeCount(String technology, long tableCount, long objectSizeCount) {
		super();
		this.technology = technology;
		this.tableCount = tableCount;
		this.objectSizeCount = objectSizeCount;
	}
	public String getTechnology() {
		return technology;
	}
	public void setTechnology(String technology) {
		this.technology = technology;
	}
	public long getTableCount() {
		return tableCount;
	}
	public void setTableCount(long tableCount) {
		this.tableCount = tableCount;
	}
	public long getObjectSizeCount() {
		return objectSizeCount;
	}
	public void setObjectSizeCount(long objectSizeCount) {
		this.objectSizeCount = objectSizeCount;
	}
	@Override
	public String toString() {
		return "PiTableNSizeCount [technology=" + technology + ", tableCount=" + tableCount + ", objectSizeCount="
				+ objectSizeCount + "]";
	}
	
}