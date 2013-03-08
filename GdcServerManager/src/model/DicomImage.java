package model;

public class DicomImage {

	private int id;
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

}
