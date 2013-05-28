package settings.sql.tables;

/**
 * Nom des champs de la table DicomImage
 * sous la forme de fonctions
 * pour detecter plus facilement les zones
 * potentielles d'erreurs
 * @author DEVERDUN Jeremy
 *
 */
public class DicomImageTable {
	
	// Les noms des champs
	public final String TNAME = "dicomimage"; // nom de la table
	private String id;
	private String name;
	private String slicelocation;
	private String id_project;
	private String id_patient;
	private String id_acqdate;
	private String id_protocol;
	private String id_serie;
	
	// Constructeur
	public DicomImageTable(){
		id = "id";
		name = "name";
		slicelocation = "slicelocation";
		id_project = "id_project";
		id_patient = "id_patient";
		id_acqdate = "id_acqdate";
		id_protocol = "id_protocol";
		id_serie = "id_serie";
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

	public String getSliceLocation() {
		return slicelocation;
	}

	public String getId_serie() {
		return id_serie;
	}
}
