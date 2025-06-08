package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "metadata_security_classification", schema = "semantic")
public class MetadataClassification {

    @Column(name = "objectname")
    private String objectName;
    @Id
    @Column(name = "attributename")
    private String attributeName;
    @Column(name = "classification")
    private String classification;
    @Column(name = "business_description")
    private String businessDescription;
    @Column(name = "related_terms")
    private String relatedTerms;
    @Column(name = "source_system")
    private String sourceSystem;
}
