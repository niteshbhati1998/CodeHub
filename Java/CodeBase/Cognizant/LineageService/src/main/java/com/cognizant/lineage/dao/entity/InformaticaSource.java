package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(name = "informatica_source_sql", schema = "semantic")
public class InformaticaSource {

    @Id
    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "folder_name")
    private String folderName;
    @Column(name = "mapping_name")
    private String mappingName;
    @Column(name = "sql")
    private String sqlText;
    @Column(name = "source_type")
    private String sourceType;
    @Column(name = "target")
    private String target;
    @Column(name = "from_component")
    private String fromComponent;
    @Column(name = "to_component")
    private String toComponent;
}
