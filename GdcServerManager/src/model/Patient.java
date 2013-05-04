package model;

import java.io.Serializable;
import java.util.Set;

public class Patient implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String birthdate;
	private String sex;
	private Project project;
	private String nom;
	private Set<AcquisitionDate> acquisitionsDates;
	
	



	public Patient() {
		
	}


	public Patient(int patient_id) {
		this.id = patient_id;
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


	public void setProject(Project p) {
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
	public boolean equals(Patient p){
		return this.id==p.id;
	}


	public String getBirthdate() {
		return birthdate;
	}


	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}


	public String getSex() {
		return sex;
	}


	public void setSex(String sex) {
		this.sex = sex;
	}



}
