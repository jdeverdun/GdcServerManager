package model;

import java.io.File;
import java.nio.file.Path;

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
	}

	public Path getDicomDir() {
		return dicomDir;
	}

	public void setDicomDir(String dicomDir) {
		this.dicomDir = (new File(dicomDir)).toPath();
	}

	public Path getNiftiDir() {
		return niftiDir;
	}

	public void setNiftiDir(String niftiDir) {
		this.niftiDir = (new File(niftiDir)).toPath();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
