package model.daemon;

import daemon.DicomJobDispatcher;
import daemon.DicomEncryptDaemon;
import daemon.NiftiDaemon;

/**
 * Classe contenant  les informations sur une commande d'import
 * depuis un repertoire vers le serveur (via ImportFrame)
 * @author DEVERDUN Jeremy
 *
 */
public class ImportSettings {
	public static enum DicomNamingTag{PATIENTNAME,PATIENTID,ANONYMIZE};
	private String newProjectName;
	private String newPatientName;
	private DicomNamingTag namingTag;
	private DicomJobDispatcher dispatcher; // le dispatcher associe a l'import
	private NiftiDaemon niftid; // le daemon nifti associe a l'impot
	
	
	public ImportSettings(String newProjectName,String patname, DicomNamingTag tag, DicomJobDispatcher dispatcher, NiftiDaemon niftid){
		this.setNewProjectName(newProjectName);
		setNamingTag(tag);
		setDispatcher(dispatcher);
		setNiftid(niftid);
	}




	/**
	 * @return the newProjectName
	 */
	public String getNewProjectName() {
		return newProjectName;
	}


	/**
	 * @param newProjectName the newProjectName to set
	 */
	public void setNewProjectName(String newProjectName) {
		this.newProjectName = newProjectName;
	}


	/**
	 * @return the newPatientName
	 */
	public String getNewPatientName() {
		return newPatientName;
	}


	/**
	 * @param newPatientName the newPatientName to set
	 */
	public void setNewPatientName(String newPatientName) {
		this.newPatientName = newPatientName;
	}


	public boolean changeProjectName(){
		return newProjectName != null;
	}
	
	public boolean changePatientName(){
		return newPatientName != null;
	}
	

	public DicomJobDispatcher getDispatcher() {
		return dispatcher;
	}


	public void setDispatcher(DicomJobDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}


	public NiftiDaemon getNiftid() {
		return niftid;
	}


	public void setNiftid(NiftiDaemon niftid) {
		this.niftid = niftid;
	}


	/**
	 * @return the namingTag
	 */
	public DicomNamingTag getNamingTag() {
		return namingTag;
	}


	/**
	 * @param namingTag the namingTag to set
	 */
	public void setNamingTag(DicomNamingTag namingTag) {
		this.namingTag = namingTag;
	}
	
	
	
	
}
