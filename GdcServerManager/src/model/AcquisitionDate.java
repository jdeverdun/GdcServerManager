package model;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;

public class AcquisitionDate {

	// Champs statiques, format par defaut sql
	public static String SQL_DATE_FORMAT = "yyyyMMdd";
	public static String DICOM_DATE_FORMAT = "yyyyMMdd";
	// Attributs
	private int id;
	private Date date; // format comme dans les dicom AAAAJJMM
	private Patient patient;
	private Project projet;
	private Set<Protocol> protocoles;
	
	public AcquisitionDate() {
		
	}

	public AcquisitionDate(int acqDate_id) {
		this.id = acqDate_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Renvoi la date sous forme d'une chaine de caracteres
	 * selon le format fmt
	 * Format doit avoir la forme : 
	 * - "ddMMyyyy"
	 * - "dd-MM-yyyy" etc ...
	 * @param fmt
	 * @return
	 */
	public String getStringDate(String fmt) {
		return new SimpleDateFormat(fmt, Locale.FRENCH).format(date);
	}
	
	/**
	 * Renvoi la date sous forme d'une chaine de caracteres
	 * formate selon DICOM_DATE_FORMAT
	 * @return
	 */
	public String getStringDate() {
		return new SimpleDateFormat(DICOM_DATE_FORMAT, Locale.FRENCH).format(date);
	}

	/**
	 * Date formatee selon "ddMMyyyy"
	 * @param date
	 */
	public void setDate(String date) {
		this.date  = java.sql.Date.valueOf( date );
		//this.date = new Date();//date;
	}

	/**
	 * Setter simple
	 * @param date
	 */
	public void setDate(Date date) {
		this.date  = date;
		//this.date = new Date();//date;
	}

	public Date getDate(){
		return date;
	}

	public Set<Protocol> getProtocoles() {
		return protocoles;
	}

	public void setProtocoles(Set<Protocol> protocoles) {
		this.protocoles = protocoles;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Project getProjet() {
		return projet;
	}

	public void setProjet(Project projet) {
		this.projet = projet;
	}
	
	// --- Methodes ---
	/**
	 * Convertie une chaine de caractere d'une date 
	 * de la forme ddMMyyy en yyyy-MM-dd
	 * @param date
	 * @return
	 */
	public String ddMMyyyy_To_yyyyMMdd(String date) {
		String dd = date.substring(0, 2);
		String MM = date.substring(2, 4);
		String yyyy = date.substring(4);
		return yyyy+"-"+MM+"-"+dd;
	}
	
	public static void main(String[] args){
		AcquisitionDate ad = new AcquisitionDate();
		ad.setDate("23062013");
		System.out.println(ad.getStringDate());
	}
}
