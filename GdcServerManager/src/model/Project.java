package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * @version 1.0
 * @created 03-Nov-2009 16:58:21
 */

public class Project implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String nom;
	private String remoteKey;
	private Set<User> users = new HashSet<User>();
	private Set<Patient> patients = new HashSet<Patient>();

	public Project() {

	}
	public Project(String n, String rkey) {
		setNom(n);
		setRemoteKey(rkey);
	}

	public Project(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Set<Patient> getPatients() {
		return patients;
	}

	public void setPatients(Set<Patient> p) {
		this.patients = p;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}
	
	public String getRemoteKey() {
		return remoteKey;
	}
	public void setRemoteKey(String remoteKey) {
		this.remoteKey = remoteKey;
	}
	public void addUser(User u){
		this.users.add(u);
	}

	@Override
	public String toString() {
		return "project(nom=" + nom + ")";
	}
	public boolean equals(Project p){
		return this.id==p.id;
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