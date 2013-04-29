package settings.sql.tables;

/**
 * Nom des champs de la table Protocol
 * sous la forme de fonctions
 * pour detecter plus facilement les zones
 * potentielles d'erreurs
 * @author DEVERDUN Jeremy
 *
 */
public class ProtocolTable {
	
	// Les noms des champs
	public final String TNAME = "protocol"; // nom de la table
	private String id;
	private String name;
	private String id_project;
	private String id_patient;
	private String id_acqdate;
	
	// Constructeur
	public ProtocolTable(){
		id = "id";
		name = "name";
		id_project = "id_project";
		id_patient = "id_patient";
		id_acqdate = "id_acqdate";
	}

	public String getName() {
		return name;
	}

	public String getId_acqdate() {
		return id_acqdate;
	}

	public String getId_project() {
		return id_project;
	}

	public String getId_patient() {
		return id_patient;
	}

	public String getId() {
		return id;
	}
}
