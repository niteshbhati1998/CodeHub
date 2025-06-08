package com.cognizant.lineage.upload.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.dao.dto.ApplicationNameDetailsDto;
import com.cognizant.lineage.dao.dto.ObjectNameDto;
import com.cognizant.lineage.dao.repository.MetadataClassificationRepo;
import com.cognizant.lineage.upload.model.ObjectNameDetails;
import com.cognizant.lineage.upload.model.ObjectNameWithNodes;

@Service
public class MetadataService {

    @Autowired
    MetadataClassificationRepo metadataClassificationRepo;

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataService.class);

    public List<String> getObjectNameFromNodes(String projectName) {
        try {
            return metadataClassificationRepo.findObjectNamesFromNodes(projectName);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred in getObjectNameDetails service: ", ex);
            throw ex;
        }
    }

    public List<String> getApplicationNameFromEdges(String projectName) {
        try {
            return metadataClassificationRepo.findApplicationNamesFromEdges(projectName);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred in getObjectNameDetails service: ", ex);
            throw ex;
        }
    }

    public ObjectNameDetails getObjectNameDetails(String projectName, String objectOrAppName) {
        ObjectNameDetails objectNameDetails = new ObjectNameDetails();
        try {
            List<ObjectNameDto> dtoList =
                    metadataClassificationRepo.getObjectNameDetails(projectName, objectOrAppName);
            List<ObjectNameWithNodes> objectNameWithNodesList = new ArrayList<>();
            for (ObjectNameDto dto : dtoList) {
                ObjectNameWithNodes incomingNodeObject = new ObjectNameWithNodes();
                incomingNodeObject.setNodeType(dto.getNodeType());
                incomingNodeObject.setScriptName(dto.getScriptName());
                incomingNodeObject.setScriptType(dto.getScriptType());
                incomingNodeObject.setSqlText(dto.getSqlText());

                incomingNodeObject.setNodeName(dto.getNodeName());
                String incomingNode = metadataClassificationRepo.getIncomingNodesOfANode(projectName,
                        dto.getScriptName(), dto.getScriptType(), dto.getSqlText(), dto.getNodeName());
                incomingNode = Objects.isNull(incomingNode) ? "N/A" :
                        (",".equals(incomingNode.charAt(incomingNode.length() - 1)) ?
                                incomingNode.substring(0, incomingNode.length() - 1) : incomingNode);
                incomingNodeObject.setIncomingNodes(incomingNode);

                objectNameWithNodesList.add(incomingNodeObject);

                ObjectNameWithNodes outgoingNodeObject = new ObjectNameWithNodes();
                outgoingNodeObject.setNodeType(dto.getNodeType());
                outgoingNodeObject.setScriptName(dto.getScriptName());
                outgoingNodeObject.setScriptType(dto.getScriptType());
                outgoingNodeObject.setSqlText(dto.getSqlText());

                String outgoingNode = metadataClassificationRepo.getOutgoingNodesOfANode(projectName,
                        dto.getScriptName(), dto.getScriptType(), dto.getSqlText(), dto.getNodeName());
                outgoingNode = Objects.isNull(outgoingNode) ? "N/A" :
                        (",".equals(outgoingNode.charAt(outgoingNode.length() - 1)) ?
                                outgoingNode.substring(0, outgoingNode.length() - 1) : outgoingNode);
                outgoingNodeObject.setNodeName(outgoingNode);
                outgoingNodeObject.setIncomingNodes(dto.getNodeName());

                objectNameWithNodesList.add(outgoingNodeObject);
            }
            objectNameDetails.setObjectNameList(objectNameWithNodesList);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred in getObjectNameDetails service: ", ex);
            throw ex;
        }
        return objectNameDetails;
    }

    public List<ApplicationNameDetailsDto> getApplicationNameDetails(String projectName, String objectOrAppName) {
        try {
            return metadataClassificationRepo.fetchApplicationDetails(projectName, objectOrAppName);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred in getObjectNameDetails service: ", ex);
            throw ex;
        }
    }
}
