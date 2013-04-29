package settings.sql.tables;

/**
 * Nom des champs de la table Serie
 * sous la forme de fonctions
 * pour detecter plus facilement les zones
 * potentielles d'erreurs
 * @author DEVERDUN Jeremy
 *
 */
public class SerieTable {
	
	// Les noms des champs
	public final String TNAME = "serie"; // nom de la table
	private String id;
	private String name;
	private String hasnifti;
	private String id_project;
	private String id_patient;
	private String id_acqdate;
	private String id_protocol;
	
	// Constructeur
	public SerieTable(){
		id = "id";
		name = "name";
		hasnifti = "hasnifti";
		id_project = "id_project";
		id_patient = "id_patient";
		id_acqdate = "id_acqdate";
		id_protocol = "id_protocol";
	}

	public String getHasnifti() {
		return hasnifti;
	}

	public String getId_protocol() {
		return id_protocol;
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
