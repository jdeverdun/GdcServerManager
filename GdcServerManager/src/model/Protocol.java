package model;

import java.util.Set;

public class Protocol {

	private int id;
	private String name;
	private AcquisitionDate acquisitionDate;
	private Project projet;
	private Patient patient;
	private Set<Serie> series;
	
	public Protocol() {
		// TODO Auto-generated constructor stub
	}

	public Protocol(int protocol_id) {
		this.id = protocol_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String string) {
		this.name = string;
	}

	public AcquisitionDate getAcquisitionDate() {
		return acquisitionDate;
	}

	public void setAcquisitionDate(AcquisitionDate acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
	}

	public Set<Serie> getSeries() {
		return series;
	}

	public void setSeries(Set<Serie> series) {
		this.series = series;
	}

	public Project getProjet() {
		return projet;
	}

	public void setProjet(Project projet) {
		this.projet = projet;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public boolean equals(Protocol p){
		return this.id==p.id;
	}
}
