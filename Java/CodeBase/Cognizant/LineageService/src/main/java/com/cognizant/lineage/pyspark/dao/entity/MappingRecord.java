package com.cognizant.lineage.pyspark.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "python_lineage_table")
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
public class MappingRecord extends AbstractTimestampEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name="folder_name")
    private String folderName;

    @Column(name="file_name")
    private String fileName;

    @Column(name="source_line_nos")
    private String sourceLineNos;

    @Column(name="source_line_content")
    private String sourceLineContent;

    @Column(name="source_query")
    private String sourceQuery;

    @Column(name="source")
    private String source;

    @Column(name="source_tables")
    private String sourceTables;

    @Column(name="source_type")
    private String sourceType;

    @Column(name="destination_line_nos")
    private String destinationLineNo;

    @Column(name="destination_line_content")
    private String destinationLineContent;

    @Column(name="destination_query")
    private String destinationQuery;

    @Column(name="destination")
    private String destination;

    @Column(name="destination_tables")
    private String destinationTables;

    @Column(name="destination_type")
    private String destinationType;



    public MappingRecord(Long jobId, String folderName, String fileName, String sourceLineNos, String sourceLineContent,
                         String sourceQuery, String source, String sourceTables, String sourceType, String destinationLineNo,
                         String destinationLineContent, String destinationQuery, String destination, String destinationTables, String destinationType) {
        this.jobId = jobId;
        this.folderName = folderName;
        this.fileName = fileName;
        this.sourceLineNos = sourceLineNos;
        this.sourceLineContent = sourceLineContent;
        this.sourceQuery = sourceQuery;
        this.source = source;
        this.sourceTables = sourceTables;
        this.sourceType = sourceType;
        this.destinationLineNo = destinationLineNo;
        this.destinationLineContent = destinationLineContent;
        this.destinationQuery = destinationQuery;
        this.destination = destination;
        this.destinationTables = destinationTables;
        this.destinationType = destinationType;
    }
}
