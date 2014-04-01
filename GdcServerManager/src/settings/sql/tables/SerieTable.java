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
	private String mri_name;
	private String repetitiontime;
	private String echotime;
	private String slicethickness;
	private String voxelwidth;
	private String voxelheight;
	private String hasnifti;
	private String impossibleNiftiConversion;
	private String id_project;
	private String id_patient;
	private String id_acqdate;
	private String id_protocol;
	
	// Constructeur
	public SerieTable(){
		id = "id";
		name = "name";
		mri_name = "mri_name";
		repetitiontime = "repetitiontime";
		echotime = "echotime";
		slicethickness = "slicethickness";
		voxelwidth = "voxelwidth";
		voxelheight = "voxelheight";
		hasnifti = "hasnifti";
		impossibleNiftiConversion = "impossibleNiftiConversion";
		id_project = "id_project";
		id_patient = "id_patient";
		id_acqdate = "id_acqdate";
		id_protocol = "id_protocol";
	}

	public String getMri_name() {
		return mri_name;
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

	/**
	 * @return the echotime
	 */
	public String getEchotime() {
		return echotime;
	}

	public String getRepetitiontime() {
		return repetitiontime;
	}

	public String getSlicethickness() {
		return slicethickness;
	}

	public String getVoxelwidth() {
		return voxelwidth;
	}

	public String getVoxelheight() {
		return voxelheight;
	}

	/**
	 * @return the hasnifti
	 */
	public String getHasnifti() {
		return hasnifti;
	}

	/**
	 * @param hasnifti the hasnifti to set
	 */
	public void setHasnifti(String hasnifti) {
		this.hasnifti = hasnifti;
	}

	/**
	 * @return the impossibleNiftiConversion
	 */
	public String getImpossibleNiftiConversion() {
		return impossibleNiftiConversion;
	}

	/**
	 * @param impossibleNiftiConversion the impossibleNiftiConversion to set
	 */
	public void setImpossibleNiftiConversion(String impossibleNiftiConversion) {
		this.impossibleNiftiConversion = impossibleNiftiConversion;
	}
}
