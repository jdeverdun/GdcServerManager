package model.daemon;

/**
 * Classe "DicomJobDispatcher Settings"
 * permettant de stocker les parametres pour le 
 * dispatcher (savoir si il tourne en mode serveur ou pas,
 * si il faut creer les repetoire Project ou pas etc
 * @author Mobilette
 *
 */
public class DJDSettings extends DaemonSettings{

	// Attributs
	private boolean workingWithProjectDir;
	private boolean workingWithAcqDateDir;
	private boolean workingWithProtocolDir;
	
	
	
	// Constructeurs
	
	
	// Accesseurs

	public boolean isWorkingWithProjectDir() {
		return workingWithProjectDir;
	}
	public void setWorkingWithProjectDir(boolean workingWithProjectDir) {
		this.workingWithProjectDir = workingWithProjectDir;
	}
	public boolean isWorkingWithAcqDateDir() {
		return workingWithAcqDateDir;
	}
	public void setWorkingWithAcqDateDir(boolean workingWithAcqDateDir) {
		this.workingWithAcqDateDir = workingWithAcqDateDir;
	}
	public boolean isWorkingWithProtocolDir() {
		return workingWithProtocolDir;
	}
	public void setWorkingWithProtocolDir(boolean workingWithProtocolDir) {
		this.workingWithProtocolDir = workingWithProtocolDir;
	}
	
	// Methodes 
	

}
