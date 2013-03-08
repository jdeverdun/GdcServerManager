package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import daemon.DBCache;

public class ServerInfo {

	// Attributs
	private Path incomingDir; // dossier des dicom en vrac
	private Path dicomDir; // dossier des dicom triés
	private Path niftiDir; // dossier des nifti triés
	private Path tempDir; // dossier temporaire, utilise surtout pour la conversion nifti (decryptage fichier)
	private DBCache dbCache; // cache de donnees de la bdd
	
	// Constructeurs
	public ServerInfo(){
		incomingDir = null;
		dicomDir = null;
		niftiDir = null;
		setTempDir(null);
		dbCache = new DBCache();
	}
	
	public ServerInfo(String inc, String dicom, String nifti,String temp){
		setIncomingDir(inc);
		setDicomDir(dicom);
		setNiftiDir(nifti);
		setTempDir(temp);
		dbCache = new DBCache();
	}
	
	
	
	// Accesseurs
	
	public Path getIncomingDir() {
		return incomingDir;
	}

	public void setIncomingDir(String incomingDir) {
		this.incomingDir = (new File(incomingDir)).toPath();
		buildIfNotExist(this.incomingDir);
	}

	public Path getDicomDir() {
		return dicomDir;
	}

	public void setDicomDir(String dicomDir) {
		this.dicomDir = (new File(dicomDir)).toPath();
		buildIfNotExist(this.dicomDir);
	}

	public Path getNiftiDir() {
		return niftiDir;
	}

	public void setNiftiDir(String niftiDir) {
		this.niftiDir = (new File(niftiDir)).toPath();
		buildIfNotExist(this.niftiDir);
	}

	public DBCache getDbCache() {
		return dbCache;
	}

	public void setDbCache(DBCache dbCache) {
		this.dbCache = dbCache;
	}

	public Path getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = (new File(tempDir)).toPath();
		buildIfNotExist(this.tempDir);
	}

	public void buildIfNotExist(Path p){
		try {
			Files.createDirectories(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
