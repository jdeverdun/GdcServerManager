package model;

import java.io.Serializable;
import java.sql.Date;

public class Job implements Serializable{
	private static final long serialVersionUID = 1L;
	private int id;
	private User user_id;
	private String jobid;
	private Date submitDate;
	private String os;
	private String description;
	
	
	public Job() {
		
	}

	public Job(int job_id) {
		this.id = job_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getUserId() {
		return user_id;
	}
	
	public void setUserId(User u) {
		this.user_id = u;
	}
	
	public String getJobId() {
		return jobid;
	}

	public void setJobId(String jobid) {
		this.jobid = jobid;
	}
	
	public Date getSubmitDate() {
		return submitDate;
	}


	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}
	
	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
