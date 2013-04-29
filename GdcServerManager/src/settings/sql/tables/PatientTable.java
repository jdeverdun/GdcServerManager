package settings.sql.tables;

/**
 * Nom des champs de la table Patient
 * sous la forme de fonctions
 * pour detecter plus facilement les zones
 * potentielles d'erreurs
 * @author DEVERDUN Jeremy
 *
 */
public class PatientTable {
	
	// Les noms des champs
	public final String TNAME = "patient"; // nom de la table
	private String id;
	private String name;
	private String birthdate;
	private String sex;
	private String id_project;
	
	// Constructeur
	public PatientTable(){
		id = "id";
		name = "name";
		birthdate = "birthdate";
		sex = "sex";
		id_project = "id_project";
	}

	public String getBirthdate() {
		return birthdate;
	}

	public String getSex() {
		return sex;
	}

	public String getId_project() {
		return id_project;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
}
