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

@Table(name = "lineage_job_status")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode
public class LineageJobStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "job_id")
	private Long jobId;

	@Column(name = "step_no")
	private Integer stepNo;

	@Column(name = "step_name")
	private String stepName;

	@Column(name = "no_of_file_received")
	private Integer noOfFileReceived;

	@Column(name = "no_of_file_processed")
	private Integer noOfFileProcessed;
	
	@Column(name = "no_of_query_parsing_issue")
	private Integer noOfQueryParsingIssue;
	
	@Column(name = "no_of_query_service_issue")
	private Integer noOfQueryServiceIssue;
	
	@Column(name = "no_of_query_skipped")
	private Integer noOfQuerySkipped;
	
	@Column(name = "log_file_location")
	private String logFileLocation;

	@Column(name = "status")
	private String status;
	
	@Column(name = "job_status_details")
	private String jobStatusDetails;
	
	public String getJobStatusDetails() {
		return jobStatusDetails;
	}

	public void setJobStatusDetails(String jobStatusDetails) {
		this.jobStatusDetails = jobStatusDetails;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public Integer getStepNo() {
		return stepNo;
	}

	public void setStepNo(Integer stepNo) {
		this.stepNo = stepNo;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	public Integer getNoOfFileReceived() {
		return noOfFileReceived;
	}

	public void setNoOfFileReceived(Integer noOfFileReceived) {
		this.noOfFileReceived = noOfFileReceived;
	}

	public Integer getNoOfFileProcessed() {
		return noOfFileProcessed;
	}

	public void setNoOfFileProcessed(Integer noOfFileProcessed) {
		this.noOfFileProcessed = noOfFileProcessed;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLogFileLocation() {
		return logFileLocation;
	}

	public void setLogFileLocation(String logFileLocation) {
		this.logFileLocation = logFileLocation;
	}


	

}
