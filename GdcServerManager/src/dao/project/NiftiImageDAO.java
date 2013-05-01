package dao.project;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;


import model.NiftiImage;

public interface NiftiImageDAO {
	public Collection<NiftiImage> retrieveAll() throws SQLException;

    public NiftiImage retrieveNiftiImage(int id) throws SQLException;  

    public int idmax() throws SQLException;
      

    public boolean newNiftiImage(String name,String mri_name, int projet_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException;
      
    public boolean updateNiftiImage(int id, String name,String mri_name, int projet_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException;
      
	public Set<NiftiImage> getNiftiImageForPatient(int id) throws SQLException;
	public Set<NiftiImage> getNiftiImageForProject(int id) throws SQLException; 
	public Set<NiftiImage> getNiftiImageForAcqDate(int id) throws SQLException; 
	public Set<NiftiImage> getNiftiImageForProtocol(int id) throws SQLException; 
	public Set<NiftiImage> getNiftiImageForSerie(int id) throws SQLException;

	public void removeEntry(String string, String mri_name, int id_project, int id_patient, int id_acqdate, int id_protocol,
			int id_serie) throws SQLException; 
	public void removeNifti(String project, String patient, String acqdate,
			String protocol, String serie, String image) throws SQLException;
	public String getNiftiIdFor(String project, String patient, String acqdate,
			String protocol, String serie, String image) throws SQLException;

}
