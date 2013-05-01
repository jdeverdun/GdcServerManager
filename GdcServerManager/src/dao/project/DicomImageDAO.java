package dao.project;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import model.DicomImage;

public interface DicomImageDAO {
	public Collection<DicomImage> retrieveAll() throws SQLException;

    public DicomImage retrieveDicomImage(int id) throws SQLException;  

    public int idmax() throws SQLException;
      

    public boolean newDicomImage(String name, String mri_name, int projet_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException;
      
    public boolean updateDicomImage(int id, String name, String mri_name, int projet_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException;
      
	public Set<DicomImage> getDicomImageForPatient(int id) throws SQLException;
	public Set<DicomImage> getDicomImageForProject(int id) throws SQLException; 
	public Set<DicomImage> getDicomImageForAcqDate(int id) throws SQLException; 
	public Set<DicomImage> getDicomImageForProtocol(int id) throws SQLException; 
	public Set<DicomImage> getDicomImageForSerie(int id) throws SQLException;

	public void removeDicom(String project, String patient, String acqdate,
			String protocol, String serie, String image) throws SQLException; 
	public String getDicomIdFor(String project, String patient, String acqdate,
			String protocol, String serie, String image) throws SQLException;

	
}
