package model;

import java.util.Set;

public class User {

	private int id;
      private String nom;
      private String prenom;
      private String email;
      private String login;
      private String password;
      private Set<Project> projects;

      public User(String nom, String prenom, String email, int poids,
  			int age, String login, String password) {
  		super();
  		this.nom = nom;
  		this.prenom = prenom;
  		this.email = email;
  		this.login = login;
  		this.password = password;
  	}
	  public User(){
		  
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
	  public String getPrenom() {
	        return prenom;
	  }
	  public void setPrenom(String prenom) {
	        this.prenom = prenom;
	  }
	  public String getEmail() {
	        return email;
	  }
	  public void setEmail(String email) {
	        this.email = email;
	  }
	  public String getLogin() {
	        return login;
	  }
	  public void setLogin(String login) {
	        this.login = login;
	  }
	  public String getPassword() {
	        return password;
	  }
	  public void setPassword(String password) {
	        this.password = password;
	  }
	   public void setAscensions(Set<Project> projets) {
			this.projects = projets;
		}
		public Set<Project> getAscensions() {
			return projects;
		}
}

