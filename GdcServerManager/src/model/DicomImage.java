package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DicomImage {

	private int id;
	private String mri_name;
	private Project projet;
	private Patient patient;
	private AcquisitionDate acquistionDate;
	private Serie serie;
	private Protocol procole;
	private String name;
	
	public DicomImage() {
		// TODO Auto-generated constructor stub
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Project getProjet() {
		return projet;
	}

	public void setProjet(Project projet) {
		this.projet = projet;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public AcquisitionDate getAcquistionDate() {
		return acquistionDate;
	}

	public void setAcquistionDate(AcquisitionDate acquistionDate) {
		this.acquistionDate = acquistionDate;
	}

	public Serie getSerie() {
		return serie;
	}

	public void setSerie(Serie serie) {
		this.serie = serie;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public Protocol getProtocole() {
		return procole;
	}
	public void setProtocole(Protocol procole) {
		this.procole = procole;
	}
	public boolean equals(DicomImage p){
		return this.id==p.id;
	}

	/**
	 * Check si le fichier est un dicom
	 * @param fi
	 * @return
	 * @throws IOException
	 */
	public static boolean isDicom(File fi) throws IOException {
		if(fi.getName().endsWith(".enc") || !fi.canRead())
			return false;
		RandomAccessFile in = new RandomAccessFile(fi.getAbsolutePath(), "r");
		// on se place sur le byte 128
		// et on cherche le mot clef "DICM"
		in.seek(128);
		String key = "";
		try{
			key = in.readLine().substring(0, 4);
		}catch(Exception e){
			e.printStackTrace();
			in.close();
			return false;
		}
		in.close();
		in = null;
		return key.equals("DICM");
	}
	public String getMri_name() {
		return mri_name;
	}
	public void setMri_name(String mri_name) {
		this.mri_name = mri_name;
	}
}
