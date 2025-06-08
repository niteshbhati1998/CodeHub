package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "volumetric_info", schema = "semantic")
public class VolumetricInfo {
    @Id
    @Column(name = "object_name")
    private String objectName;
    @Column(name = "db_type")
    private String dbType;
    @Column(name = "table_create_time")
    private String tableCreateTime;
    @Column(name = "space_used_by_table_mb")
    private Long spaceUsedByTableMB;
    @Column(name = "currentsize_gb")
    private Long currentSIzeInGB;
    @Column(name = "table_rowcount")
    private Integer rowCount;
    @Column(name = "partition_column_count")
    private Integer partitionCount;
    @Column(name = "index_count")
    private Integer indexCount;
    @Column(name = "column_name")
    private String columnName;
    @Column(name = "column_datatype")
    private String columnDataType;
    @Column(name = "column_compressible")
    private String compressionValue;
    @Column(name = "column_nullable")
    private String columnNullable;
    @Column(name = "complexity")
    private String complexity;
}
