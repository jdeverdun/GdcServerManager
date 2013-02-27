package model;

import java.util.Set;

public class Serie {

	private int id;
	private String name;
	private boolean hasNifti;
	private Protocol protocole;
	private Project projet;
	private Patient patient;
	private AcquisitionDate acquistionDate;
	private Set<DicomImage> dicomImages;
	private Set<NiftiImage> niftiImages;
	
	public Serie() {
		
	}

	public Serie(int serie_id) {
		this.id = serie_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Protocol getProtocole() {
		return protocole;
	}

	public void setProtocole(Protocol protocole) {
		this.protocole = protocole;
	}

	public Set<DicomImage> getDicomImages() {
		return dicomImages;
	}

	public void setDicomImages(Set<DicomImage> dicomImages) {
		this.dicomImages = dicomImages;
	}

	public Set<NiftiImage> getNiftiImages() {
		return niftiImages;
	}

	public void setNiftiImages(Set<NiftiImage> niftiImages) {
		this.niftiImages = niftiImages;
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
	public boolean doesHaveNifti() {
		return hasNifti;
	}

	public void setHasNifti(int hasNifti) {
		if(hasNifti == 0)
			this.hasNifti = false;
		else
			this.hasNifti = true;
	}

	public boolean equals(Serie p){
		return this.id==p.id;
	}

}
