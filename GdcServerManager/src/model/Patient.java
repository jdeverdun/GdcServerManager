package model;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Patient implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private Date birthdate;
	private String sex;
	private float weight;
	private float size;
	private String remoteKey;
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
	public String getRemoteKey() {
		return remoteKey;
	}
	public void setRemoteKey(String remoteKey) {
		this.remoteKey = remoteKey;
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


	public Date getBirthdate() {
		return birthdate;
	}


	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}


	public String getSex() {
		return sex;
	}


	public void setSex(String sex) {
		this.sex = sex;
	}


	/**
	 * @return the size
	 */
	public float getSize() {
		return size;
	}


	/**
	 * @param size the size to set
	 */
	public void setSize(float size) {
		this.size = size;
	}


	/**
	 * @return the weight
	 */
	public float getWeight() {
		return weight;
	}


	/**
	 * @param f the weight to set
	 */
	public void setWeight(float f) {
		this.weight = f;
	}

	// Genere la clef de cryptage locale pour le projet
	public static String generateLocalKeyFrom(String seed) {
        List<Character> characters = new ArrayList<Character>();  
        for(char c : seed.toCharArray()) {  
            characters.add(c);  
        }  
        Collections.shuffle(characters,new Random(seed.length()));  
        StringBuilder sb = new StringBuilder();  
        for(char c : characters) {  
            sb.append(c);  
        }  
        return sb.toString();  
	}

}
