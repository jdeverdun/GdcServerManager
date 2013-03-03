package model;

import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;

public class User {
	public static enum Acclvl{SIMPLE,ADMIN}
	private static int RANDOM_NUM = 16;
	private int id;
	private String nom;
	private String prenom;
	private String email;
	private String login;
	private String password;
	private int level;
	private Set<Project> projects;
	private int firstConnect;

      public User(String nom, String prenom, String email,String login, String password, int level) {
  		super();
  		this.nom = nom;
  		this.prenom = prenom;
  		this.email = email;
  		this.login = login;
  		this.password = password;
  		this.level = level;
  		}
      public User(String nom, String prenom, String email, String login, int level) {
    		super();
    		this.nom = nom;
    		this.prenom = prenom;
    		this.email = email;
    		this.login = login;
    		this.password = randomPass(RANDOM_NUM);
    		this.level = level;
    	}

	public User(){
		this.nom = "";
		this.prenom = "";
		this.email = "";
		this.login = "";
		this.password = "";
		this.level = -1; 
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
	   public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getFirstConnect() {
		return firstConnect;
	}
	public void setFirstConnect(int firstConnect) {
		this.firstConnect = firstConnect;
	}
	public void setProjects(Set<Project> projets) {
		this.projects = projets;
	}
	public Set<Project> getProjects() {
		return projects;
	}
	public boolean equals(User p){
		return this.id==p.id;
	}
	public void addProject(Project proj) {
		this.projects.add(proj);
	}
	public static String randomPass(int count) {
		return RandomStringUtils.randomAlphanumeric(count);
	}
	public boolean isReadyForInsert() {
		if(!this.getEmail().equals("") && !this.getNom().equals("") && !this.getPrenom().equals("")
				&& !this.getLogin().equals("") && this.getLevel()!=-1 && !this.getPassword().equals(""))
			return true;
		return false;
	}
	public int firstConnect() {
		return firstConnect;
	}
}

