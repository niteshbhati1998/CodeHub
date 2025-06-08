package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "script_files_info", schema = "semantic")
public class ScriptFilesInfo {
	@Id
    @Column(name = "sqlid")
    private Integer sqlId;
    @Column(name = "job_id")
    private Integer jobId;
    @Column(name = "object_type")
    private String objectType;
    @Column(name = "sqlid_file_specific")
    private Integer sqlidFileSpecific;  
    @Column(name = "sqltext")
    private String sqlText;
    @Column(name = "filename")
    private String fileName;  
    @Column(name = "line_number")
    private Integer lineNumber;
    @Column(name = "status")
    private String status;    
    @Column(name = "new_sqltext")
    private String newSqltext;
}
