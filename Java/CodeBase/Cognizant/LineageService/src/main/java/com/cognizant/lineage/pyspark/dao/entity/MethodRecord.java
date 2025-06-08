package com.cognizant.lineage.pyspark.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@Table(name = "python_lineage_method")
public class MethodRecord extends AbstractTimestampEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_name")
    private String folderName;

    @Column(name = "file_Name")
    private String fileName;

    @Column(name = "source_code")
    private String sourceCode;

    @Column(name = "class_name")
    private String className;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "return_value")
    private String returnValue;

    @Column(name = "query")
    private String query;

    @Column(name="line_nos")
    private String lineNos;

    public MethodRecord(String className, String methodName, String returnValue, String query, String lineNos) {
        this.className = className;
        this.methodName = methodName;
        this.returnValue = returnValue;
        this.query = query;
        this.lineNos = lineNos;
    }
}
