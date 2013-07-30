package daemon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;

import settings.WindowManager;

import dao.MySQLProjectDAO;
import dao.ProjectDAO;
import dao.project.MySQLPatientDAO;
import dao.project.PatientDAO;
import exceptions.AnonymizationException;
import exceptions.DicomException;

import model.Patient;
import model.Project;
import model.ServerInfo;

public abstract class DaemonWorker {

	// Attributs
	protected Path patientFolder;
	protected Path serieFolder;
	protected Path projectFolder;
	protected ServerInfo serverInfo;
	
	public DaemonWorker(){

	}
	// Accesseurs
	
	public Path getPatientFolder() {
		return patientFolder;
	}


	public void setPatientFolder(Path patientFolder) {
		this.patientFolder = patientFolder;
	}


	public Path getSerieFolder() {
		return serieFolder;
	}


	public void setSerieFolder(Path serieFolder) {
		this.serieFolder = serieFolder;
	}


	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(ServerInfo sinfo) {
		this.serverInfo = sinfo;
	}
	
	
	// Methodes
	
	public Path getProjectFolder() {
		return projectFolder;
	}


	public void setProjectFolder(Path projectFolder) {
		this.projectFolder = projectFolder;
	}


	abstract public void start() throws DicomException, AnonymizationException;
	
	
	
	// test si les repertoires existent (patient / protocoles etc) et on les créé au besoin
	// renvoi true si le repertoire existait
	// false si il a du etre cree
	protected boolean checkAndMakeDir(Path dir) {
		if(!Files.exists(dir)){
			// Si ce n'est pas le cas on le créé
			try {
				Files.createDirectory(dir);
			} catch (IOException e1) {
				WindowManager.mwLogger.log(Level.SEVERE, "Can't create directory",e1);
			}
			return false;
		}
		return true;
	}
	
	// get AES encrypt / decrypt pass
	protected String getAESPass() {
		String patientName = getPatientFolder().getFileName().toString();
		String projectName = getProjectFolder().getFileName().toString();
		DBCache cache = getServerInfo().getDbCache();
		PatientDAO pdao = new MySQLPatientDAO();
		String rkey = cache.getRkeyList().get(patientName+"_"+projectName);
		
		if(rkey!=null){
			// Attention ce patch permet de corriger le fait qu'on a change la facon d'encrypter
			if(sameChars(rkey, projectName)){
				// alors on a encrypter comme dans la v1 via le nom de projet -> on genere la clef local a partir
				// du nom du projet
				return rkey+Project.generateLocalKeyFrom(projectName);
			}else{
				// alors on a encrypter comme dans la v2 via le nom de patient -> on genere la clef local a partir
				// du nom du patient
				return rkey+Patient.generateLocalKeyFrom(patientName);
			}
		}
		try {
			Patient pat = pdao.retrievePatient(patientName,projectName);
			cache.getRkeyList().put(pat.getNom()+"_"+projectName, pat.getRemoteKey());
			String lkey;
			if(sameChars(pat.getRemoteKey(), projectName)){
				// alors on a encrypter comme dans la v1 via le nom de projet -> on genere la clef local a partir
				// du nom du projet
				lkey = Project.generateLocalKeyFrom(projectName);
			}else{
				lkey = Patient.generateLocalKeyFrom(pat.getNom());
			}
			return pat.getRemoteKey()+lkey;
		} catch (SQLException e) {
			WindowManager.mwLogger.log(Level.SEVERE, "Can't get decrypt password",e);
		}
		return null;
	}
	
	private static boolean sameChars(String firstStr, String secondStr) {
	  char[] first = firstStr.toCharArray();
	  char[] second = secondStr.toCharArray();
	  Arrays.sort(first);
	  Arrays.sort(second);
	  return Arrays.equals(first, second);
	}
	// Rajoute une entree d'un dossier / image
	// dans la table "table" de la base de donnee 
	abstract protected void addEntryToDB(Path name, String table);

}
