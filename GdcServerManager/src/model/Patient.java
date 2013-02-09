package model;

import java.util.Set;

public class Patient {

	private int id;
	private Project project;
	private String nom;
	private Set<AcquisitionDate> acquisitionsDates;
	
	



	public Patient() {
		
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public Project getProject() {
		return project;
	}


	public void setProject(int p) {
		this.project = p;
	}

	public Set<AcquisitionDate> getAcquisitionsDates() {
		return acquisitionsDates;
	}


	public void setAcquisitionsDates(Set<AcquisitionDate> acquisitionsDates) {
		this.acquisitionsDates = acquisitionsDates;
	}
	public String getNom() {
		return nom;
	}


	public void setNom(String nom) {
		this.nom = nom;
	}

}
