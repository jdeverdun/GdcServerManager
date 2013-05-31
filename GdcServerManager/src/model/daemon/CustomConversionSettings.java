package model.daemon;

import model.daemon.CustomConversionSettings.ServerMode;

/**
 * Classe "DicomJobDispatcher Settings"
 * permettant de stocker les parametres pour le 
 * dispatcher (savoir si il tourne en mode serveur ou pas,
 * si il faut creer les repetoire Project ou pas etc
 * @author Mobilette
 *
 */
public class CustomConversionSettings {

	public enum ServerMode{SERVER,CLIENT,IMPORT};
	// Attributs
	private ServerMode serverMode;
	private boolean workingWithProjectDir;
	private boolean workingWithAcqDateDir;
	private boolean workingWithProtocolDir;
	private boolean keepDicom;
	private boolean niftiDebugMode;// les messages du dcm2nii.exe
	private boolean dicomDebugMode;// les messages de la copie des dicoms
	private boolean nifti4D; // si vrai, on stock les fichiers en mode 4D sinon 3D FALSE par defaut
	private ImportSettings importSettings; // optionnel, options pour l'import (nouveau nom de projet etc)
	
	// Constructeurs
	


	/**
	 * Par defaut en mode serveur
	 */
	public CustomConversionSettings(){
		setServerMode(ServerMode.SERVER);
		setWorkingWithAcqDateDir(true);
		setWorkingWithProjectDir(true);
		setWorkingWithProtocolDir(true);
		setDicomDebugMode(true);
		setNiftiDebugMode(false);
		setKeepDicom(true);
		setNifti4D(false);
		importSettings = null;
	}
	
	public CustomConversionSettings(ServerMode servermode){
		setServerMode(servermode);
		setWorkingWithProjectDir(true);
		setWorkingWithAcqDateDir(true);
		setWorkingWithProtocolDir(true);
		setDicomDebugMode(false);
		setNiftiDebugMode(false);
		setKeepDicom(true);
		setNifti4D(false);
		importSettings = null;
	}
	
	/**
	 * Constructeur pour l'import
	 * @param servermode
	 * @param is
	 */
	public CustomConversionSettings(ServerMode servermode, ImportSettings is){
		setServerMode(servermode);
		setWorkingWithProjectDir(true);
		setWorkingWithAcqDateDir(true);
		setWorkingWithProtocolDir(true);
		setDicomDebugMode(false);
		setNiftiDebugMode(false);
		setKeepDicom(true);
		setNifti4D(false);
		setImportSettings(is);
	}
	
	public CustomConversionSettings(ServerMode servermode,boolean workWithProjectDir, boolean workWithAcqDateDir, boolean workWithProtocolDir){
		setServerMode(servermode);
		setWorkingWithProjectDir(workWithProjectDir);
		setWorkingWithAcqDateDir(workWithAcqDateDir);
		setWorkingWithProtocolDir(workWithProtocolDir);
		setDicomDebugMode(false);
		setNiftiDebugMode(false);
		setKeepDicom(true);
		setNifti4D(false);
		importSettings = null;
	}
	
	public CustomConversionSettings(ServerMode servermode,boolean workWithProjectDir, boolean workWithAcqDateDir, boolean workWithProtocolDir, boolean keepDicom, boolean nifti4d){
		setServerMode(servermode);
		setWorkingWithProjectDir(workWithProjectDir);
		setWorkingWithAcqDateDir(workWithAcqDateDir);
		setWorkingWithProtocolDir(workWithProtocolDir);
		setDicomDebugMode(false);
		setNiftiDebugMode(false);
		setKeepDicom(keepDicom);
		setNifti4D(nifti4d);
		importSettings = null;
	}
	
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
	
	public ServerMode getServerMode() {
		return serverMode;
	}

	public void setServerMode(ServerMode serverMode) {
		this.serverMode = serverMode;
	}

	public boolean isNiftiDebugMode() {
		return niftiDebugMode;
	}

	public void setNiftiDebugMode(boolean niftiDebugMode) {
		this.niftiDebugMode = niftiDebugMode;
	}

	public boolean isDicomDebugMode() {
		return dicomDebugMode;
	}

	public void setDicomDebugMode(boolean dicomDebugMode) {
		this.dicomDebugMode = dicomDebugMode;
	}

	public boolean keepDicom() {
		return keepDicom;
	}

	public void setKeepDicom(boolean keepDicom) {
		this.keepDicom = keepDicom;
	}

	public boolean isNifti4D() {
		return nifti4D;
	}
	public void setNifti4D(boolean nifti4d) {
		nifti4D = nifti4d;
	}

	/**
	 * @return the importSettings
	 */
	public ImportSettings getImportSettings() {
		return importSettings;
	}

	/**
	 * @param importSettings the importSettings to set
	 */
	public void setImportSettings(ImportSettings importSettings) {
		this.importSettings = importSettings;
	}

	
	// Methodes 
	

}
