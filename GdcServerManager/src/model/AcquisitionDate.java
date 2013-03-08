package model;

import java.util.Date;
import java.util.Set;

public class AcquisitionDate {

	private int id;
	private String date; // format comme dans les dicom AAAAJJMM
	private Patient patient;
	private Project projet;
	private Set<Protocol> protocoles;
	
	public AcquisitionDate() {
		
	}

	public AcquisitionDate(int acqDate_id) {
		this.id = acqDate_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
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

	public Project getProjet() {
		return projet;
	}

	public void setProjet(Project projet) {
		this.projet = projet;
	}

}
