package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerInfo {

	// Attributs
	private Path incomingDir; // dossier des dicom en vrac
	private Path dicomDir; // dossier des dicom triés
	private Path niftiDir; // dossier des nifti triés
	
	// Constructeurs
	public ServerInfo(){
		incomingDir = null;
		dicomDir = null;
		niftiDir = null;
	}
	
	public ServerInfo(String inc, String dicom, String nifti){
		setIncomingDir(inc);
		setDicomDir(dicom);
		setNiftiDir(nifti);
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
