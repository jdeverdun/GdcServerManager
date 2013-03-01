package model;

import java.util.HashSet;
import java.util.Set;


/**
 * @version 1.0
 * @created 03-Nov-2009 16:58:21
 */

public class Project {

	private int id;
	private String nom;
	private Set<User> users = new HashSet<User>();
	private Set<Patient> patients = new HashSet<Patient>();

	public Project() {

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



}