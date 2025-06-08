package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
@Table(name = "informatica_param_details", schema = "semantic")
public class InformaticaParamDetails {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "folder_name")
	private String folderName;
	
	@Column(name = "workflow_name")
	private String worflowName;
	
	@Column(name = "session_name")
	private String sessionName;
	
	@Column(name = "param_name")
	private String paramName;
	
	@Column(name = "param_value")
	private String paramValue;
	
	@Column(name = "type")
	private String type;
}
