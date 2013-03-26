package model;

public class NiftiImage {

	private int id;
	private String name;
	private String mri_name;
	private Project projet;
	private Patient patient;
	private AcquisitionDate acquistionDate;
	private Serie serie;
	private Protocol protocole;
	
	public NiftiImage() {
		// TODO Auto-generated constructor stub
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


	public Protocol getProtocole() {
		return protocole;
	}


	public void setProtocole(Protocol protocole) {
		this.protocole = protocole;
	}
	
	public boolean equals(NiftiImage p){
		return this.id==p.id;
	}


	public String getMri_name() {
		return mri_name;
	}


	public void setMri_name(String mri_name) {
		this.mri_name = mri_name;
	}

}
