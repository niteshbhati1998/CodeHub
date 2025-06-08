package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tivoli_job", schema = "semantic")
public class TivoliJob {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "job_id")
	private Long jobId;
	
	@Column(name = "project_name")
	private String projectName;
	
	@Column(name = "filename")
	private String fileName;
	
	@Column(name = "task")
	private String task;
	
	@Column(name = "follow_up_task")
	private String followUpTask;
	
	@Column(name = "job_name")
	private String jobName;
	
	@Column(name = "workflow_name")
	private String workflowName;
	
	@Column(name = "description")
	private String description;
}
