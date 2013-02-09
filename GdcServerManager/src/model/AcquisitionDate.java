package model;

import java.util.Date;
import java.util.Set;

public class AcquisitionDate {

	private int id;
	private Date date;
	private Patient patient;
	private Set<Protocol> protocoles;
	
	public AcquisitionDate() {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Set<Protocol> getProtocoles() {
		return protocoles;
	}

	public void setProtocoles(Set<Protocol> protocoles) {
		this.protocoles = protocoles;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

}
