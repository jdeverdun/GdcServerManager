package settings.sql.tables;

/**
 * Nom des champs de la table  User_Project
 * sous la forme de fonctions
 * pour detecter plus facilement les zones
 * potentielles d'erreurs
 * @author DEVERDUN Jeremy
 *
 */
public class UserProjectTable {
	
	// Les noms des champs
	public final String TNAME = "user_project"; // nom de la table
	private String id_user;
	private String id_project;
	
	// Constructeur
	public UserProjectTable(){
		id_user = "id_user";
		id_project = "id_project";
	}

	public String getId_project() {
		return id_project;
	}

	public String getId_user() {
		return id_user;
	}
}
