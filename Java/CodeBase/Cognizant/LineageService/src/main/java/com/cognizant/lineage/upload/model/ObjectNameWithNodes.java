package com.cognizant.lineage.upload.model;

import lombok.Data;

@Data
public class ObjectNameWithNodes {
    private String nodeName;
    private String nodeType;
    private String scriptName;
    private String scriptType;
    private String sqlText;
    private String incomingNodes;
}
