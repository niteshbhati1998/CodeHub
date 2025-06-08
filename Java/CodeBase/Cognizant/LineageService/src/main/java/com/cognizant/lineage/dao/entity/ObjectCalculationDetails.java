package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "object_calculation_details", schema = "semantic")
public class ObjectCalculationDetails {
    @Id
    @Column(name="id")
    private Integer id;
    @Column(name="db_type")
    private String dbType;
    @Column(name="complexity")
    private String complexity;
    @Column(name="partition_count")
    private Integer partitionCount;
    @Column(name="index_count")
    private Integer indexCount;
    @Column(name="row_count")
    private Integer rowCount;
    @Column(name = "spl_datatype_count")
    private Integer splDatatypeCount;
    @Column(name = "compression_count")
    private Integer compressionCount;
    @Column(name = "column_nullable_count")
    private Integer columnNullableCount;
}
