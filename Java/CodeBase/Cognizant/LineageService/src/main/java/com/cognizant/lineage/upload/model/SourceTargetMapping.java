package com.cognizant.lineage.upload.model;

public class SourceTargetMapping {

	private Node sourceNode;
	private Node targetNode;
	private String sourceType;
	private String targetType;

	public SourceTargetMapping(Node sourceNode, Node targetNode) {
		super();
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
	}

	public SourceTargetMapping() {
		super();
	}

	public Node getSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(Node sourceNode) {
		this.sourceNode = sourceNode;
	}

	public Node getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(Node targetNode) {
		this.targetNode = targetNode;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

}
