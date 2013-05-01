package dao.project;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

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
      

    public boolean newSerie(String name,int hasNifit, int projet_id, int patient_id, int id_acqdate, int id_protocol) throws SQLException;
      
    public boolean updateSerie(int id, String name,int hasNifit, int projet_id, int patient_id, int id_acqdate, int id_protocol) throws SQLException;
      
	public Set<Serie> getSerieForPatient(int id) throws SQLException;
	public Set<Serie> getSerieForProject(int id) throws SQLException; 
	public Set<Serie> getSerieForAcqDate(int id) throws SQLException; 
	public Set<Serie> getSerieForProtocol(int id) throws SQLException;

	void removeSerie(String project, String patient, String acqdate,
			String protocol, String serie) throws SQLException;

	String getSerieIdFor(String project, String patient, String acqdate,
			String protocol, String serie) throws SQLException;

	
}
