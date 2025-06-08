package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class HotSpotDetail {
    private String projectName;
    private String nodeName;
    private Integer incomingEdges;
    private Integer outgoingEdges;
    private Integer degree;
}
