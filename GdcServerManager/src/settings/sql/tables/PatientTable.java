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
	private String size;
	private String weight;
	private String rkey;
	private String id_project;

	
	// Constructeur
	public PatientTable(){
		id = "id";
		name = "name";
		birthdate = "birthdate";
		sex = "sex";
		id_project = "id_project";
		size = "size";
		weight = "weight";
		rkey = "rkey";
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

	/**
	 * @return the weight
	 */
	public String getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(String weight) {
		this.weight = weight;
	}

	/**
	 * @return the size
	 */
	public String getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * @return the rkey
	 */
	public String getRkey() {
		return rkey;
	}

	/**
	 * @param rkey the rkey to set
	 */
	public void setRkey(String rkey) {
		this.rkey = rkey;
	}
}
