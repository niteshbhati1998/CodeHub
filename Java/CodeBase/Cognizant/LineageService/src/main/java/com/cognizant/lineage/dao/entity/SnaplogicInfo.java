package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "snaplogic_info", schema = "semantic")
public class SnaplogicInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "source")
    private String source;
    @Column(name = "target")
    private String target;
    @Column(name = "snaplex_path")
    private String snaplexPath;
    @Column(name = "parameters")
    private String parameters;
    @Column(name = "execution_mode")
    private String executionMode;
    @Column(name = "class_id")
    private String classId;
    @Column(name = "table_name")
    private String tableName;
    @Column(name = "sql_statement")
    private String sqlStatement;
}
