package settings.sql.tables;

/**
 * Nom des champs de la table  User
 * sous la forme de fonctions
 * pour detecter plus facilement les zones
 * potentielles d'erreurs
 * @author DEVERDUN Jeremy
 *
 */
public class UserTable {
	
	// Les noms des champs
	public final String TNAME = "user"; // nom de la table
	private String id;
	private String nom;
	private String prenom;
	private String email;
	private String login;
	private String password;
	private String level;
	private String firstconnect;
	
	// Constructeur
	public UserTable(){
		id = "id";
		nom = "nom";
		prenom = "prenom";
		email = "email";
		login = "login";
		password = "password";
		level = "level";
		firstconnect = "firstconnect";
	}

	public String getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public String getEmail() {
		return email;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getLevel() {
		return level;
	}

	public String getFirstconnect() {
		return firstconnect;
	}
}
