package dao.project;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import model.Patient;
import model.Project;
import model.Serie;

public interface SerieDAO {
	public Collection<Serie> retrieveAll() throws SQLException;

    public Serie retrieveSerie(int id) throws SQLException;  
    
    /**
     * Recupere une serie via son nom et les id de ses parents (projet,patient etc)
     * @param name
     * @param project_id
     * @param patient_id
     * @param acqDate_id
     * @param protocol_id
     * @return
     * @throws SQLException 
     */
    public Serie retrieveSerie(String name, int project_id, int patient_id,
			int acqDate_id, int protocol_id) throws SQLException; 
    public int idmax() throws SQLException;
      

    public boolean newSerie(String name,String mri_name, float repetitiontime, float echotime, float slicethickness, float voxelwidth, float voxelheight,int hasnifti,int impossibleNiftiConversion, int projet_id, int patient_id, int id_acqdate, int id_protocol) throws SQLException;
      
    public boolean updateSerie(int id, String name,String mri_name, float repetitiontime, float echotime, float slicethickness, float voxelwidth, float voxelheight,int hasnifti,int impossibleNiftiConversion, int projet_id, int patient_id, int id_acqdate, int id_protocol) throws SQLException;
    public boolean updateHasNifti(int idserie,int hasnifti) throws SQLException;
	public Set<Serie> getSerieForPatient(int id) throws SQLException;
	public Set<Serie> getSerieForProject(int id) throws SQLException; 
	public Set<Serie> getSerieForAcqDate(int id) throws SQLException; 
	public Set<Serie> getSerieForProtocol(int id) throws SQLException;

	void removeSerie(String project, String patient, String acqdate,
			String protocol, String serie) throws SQLException;

	int getSerieIdFor(String project, String patient, String acqdate,
			String protocol, String serie) throws SQLException;
	public boolean changeProject(Patient pat, Project toproj) throws SQLException;

	boolean updateImpossibleNiftiConversion(int idserie,
			int impossibleNiftiConversion) throws SQLException;
	int isImpossibleNiftiConversion(int idserie) throws SQLException;

	
}
