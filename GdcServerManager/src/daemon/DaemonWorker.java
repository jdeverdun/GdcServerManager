package daemon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import model.ServerInfo;

public abstract class DaemonWorker {

	// Attributs
	protected Path patientFolder;
	protected Path serieFolder;
	protected ServerInfo serverInfo;
	
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
	
	abstract public void start();
	
	
	
	// test si les repertoires existent (patient / protocoles etc) et on les créé au besoin
	// renvoi true si le repertoire existait
	// false si il a du etre cree
	protected boolean checkAndMakeDir(Path dir) {
		if(!Files.exists(dir)){
			// Si ce n'est pas le cas on le créé
			try {
				Files.createDirectory(dir);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	
	// Rajoute une entree d'un dossier / image
	// dans la table "table" de la base de donnee 
	abstract protected void addEntryToDB(Path name, String table);
}
