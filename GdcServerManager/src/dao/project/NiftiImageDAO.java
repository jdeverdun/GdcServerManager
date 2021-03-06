package dao.project;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;


import model.NiftiImage;
import model.Patient;
import model.Project;

public interface NiftiImageDAO {
	public Collection<NiftiImage> retrieveAll() throws SQLException;

    public NiftiImage retrieveNiftiImage(int id) throws SQLException;  

    public int idmax() throws SQLException;
      

    public boolean newNiftiImage(String name,int slice, int projet_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException;
      
    public boolean updateNiftiImage(int id, String name,int slice, int projet_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException;
      
	public Set<NiftiImage> getNiftiImageForPatient(int id) throws SQLException;
	public Set<NiftiImage> getNiftiImageForProject(int id) throws SQLException; 
	public Set<NiftiImage> getNiftiImageForAcqDate(int id) throws SQLException; 
	public Set<NiftiImage> getNiftiImageForProtocol(int id) throws SQLException; 
	public Set<NiftiImage> getNiftiImageForSerie(int id) throws SQLException;

	public void removeEntry(String string, int slice, int id_project, int id_patient, int id_acqdate, int id_protocol,
			int id_serie) throws SQLException; 
	public void removeEntry(String string, int id_project, int id_patient, int id_acqdate, int id_protocol,
			int id_serie) throws SQLException; 
	public void removeNifti(String project, String patient, String acqdate,
			String protocol, String serie, String image) throws SQLException;
	public int getNiftiIdFor(String project, String patient, String acqdate,
			String protocol, String serie, String image) throws SQLException;

	public boolean changeProject(Patient pat, Project toproj) throws SQLException;

	void removeNiftisForSerie(String project, String patient, String acqdate,
			String protocol, String serie) throws SQLException;

	void removeNiftisForProtocol(String project, String patient,
			String acqdate, String protocol) throws SQLException;

	void removeNiftisForAcqDate(String project, String patient, String acqdate)
			throws SQLException;

	void removeNiftisForProject(String project) throws SQLException;

	void removeNiftisForPatient(String project, String patient)
			throws SQLException;

}
