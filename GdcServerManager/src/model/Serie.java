package model;

import java.util.Set;

public class Serie {

	private int id;
	private String name;
	private Protocol protocole;
	private Set<DicomImage> dicomImages;
	private Set<NiftiImage> niftiImages;
	
	public Serie() {
		
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

}
