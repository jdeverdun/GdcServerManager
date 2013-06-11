package model.daemon;

import daemon.DicomJobDispatcher;
import daemon.EncryptDaemon;
import daemon.NiftiDaemon;

/**
 * Classe contenant  les informations sur une commande d'import
 * depuis un repertoire vers le serveur (via ImportFrame)
 * @author DEVERDUN Jeremy
 *
 */
public class ImportSettings {
	private String newProjectName;
	private String newPatientName;
	private boolean usePatientName;
	private DicomJobDispatcher dispatcher; // le dispatcher associe a l'import
	private NiftiDaemon niftid; // le daemon nifti associe a l'impot
	
	
	public ImportSettings(String newProjectName,String patname, boolean usePatientName,DicomJobDispatcher dispatcher, NiftiDaemon niftid){
		this.setNewProjectName(newProjectName);
		this.setUsePatientName(usePatientName);
		this.setNewPatientName(patname);
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
	
	/**
	 * @return the usePatientName
	 */
	public boolean isUsePatientName() {
		return usePatientName;
	}


	/**
	 * @param usePatientName the usePatientName to set
	 */
	public void setUsePatientName(boolean usePatientName) {
		this.usePatientName = usePatientName;
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
	
	
	
	
}
