package settings.sql.tables;

import model.AcquisitionDate;

/**
 * Nom des champs de la table AcquisitionDate
 * sous la forme de fonctions
 * pour detecter plus facilement les zones
 * potentielles d'erreurs
 * @author DEVERDUN Jeremy
 *
 */
public class AcquisitionDateTable {
	
	// Les noms des champs
	public final String TNAME = "acquisitiondate"; // nom de la table
	private String id;
	private String date;
	private String id_project;
	private String id_patient;
	
	// Constructeur
	public AcquisitionDateTable(){
		id = "id";
		date = "date";
		id_project = "id_project";
		id_patient = "id_patient";
	}

	public String getDate() {
		return date;
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
