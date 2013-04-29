package settings.sql.tables;

/**
 * Nom des champs de la table Project
 * sous la forme de fonctions
 * pour detecter plus facilement les zones
 * potentielles d'erreurs
 * @author DEVERDUN Jeremy
 *
 */
public class ProjectTable {
	
	// Les noms des champs
	public final String TNAME = "project"; // nom de la table
	private String id;
	private String name;
	private String rkey;
	
	// Constructeur
	public ProjectTable(){
		id = "id";
		name = "name";
		rkey = "rkey";
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getRkey() {
		return rkey;
	}
}
