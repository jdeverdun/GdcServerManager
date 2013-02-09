package model;

import java.util.Set;

public class Protocol {

	private int id;
	private int name;
	private AcquisitionDate acquisitionDate;
	private Set<Serie> series;
	
	public Protocol() {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getName() {
		return name;
	}

	public void setName(int name) {
		this.name = name;
	}

	public AcquisitionDate getAcquisitionDate() {
		return acquisitionDate;
	}

	public void setAcquisitionDate(AcquisitionDate acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
	}

	public Set<Serie> getSeries() {
		return series;
	}

	public void setSeries(Set<Serie> series) {
		this.series = series;
	}

}
