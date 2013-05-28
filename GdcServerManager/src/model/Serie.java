package model;

import java.io.Serializable;
import java.util.Set;

public class Serie implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private String mri_name;
	private float repetitiontime;
	private float echotime;
	private float slicethickness;
	private float voxelwidth;
	private float voxelheight;
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

	public String getMri_name() {
		return mri_name;
	}

	public void setMri_name(String mri_name) {
		this.mri_name = mri_name;
	}

	public float getRepetitiontime() {
		return repetitiontime;
	}

	public void setRepetitiontime(float repetitiontime) {
		this.repetitiontime = repetitiontime;
	}

	public float getEchotime() {
		return echotime;
	}

	public void setEchotime(float echotime) {
		this.echotime = echotime;
	}

	public float getSlicethickness() {
		return slicethickness;
	}

	public void setSlicethickness(float slicethickness) {
		this.slicethickness = slicethickness;
	}

	public float getVoxelwidth() {
		return voxelwidth;
	}

	public void setVoxelwidth(float voxelwidth) {
		this.voxelwidth = voxelwidth;
	}

	public float getVoxelheight() {
		return voxelheight;
	}

	public void setVoxelheight(float voxelheight) {
		this.voxelheight = voxelheight;
	}

	public boolean equals(Serie p){
		return this.id==p.id;
	}

}
