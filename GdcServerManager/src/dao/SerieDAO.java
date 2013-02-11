package dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import model.Serie;

public interface SerieDAO {
	public Collection<Serie> retrieveAll() throws SQLException;

    public Serie retrieveSerie(int id) throws SQLException;  

    public int idmax();
      

    public boolean newSerie(int id, String name, int projet_id, int patient_id, int id_acqdate, int id_protocol) throws SQLException;
      
    public boolean updateSerie(int id, String name, int projet_id, int patient_id, int id_acqdate, int id_protocol) throws SQLException;
      
	public Set<Serie> getSerieForPatient(int id) throws SQLException;
	public Set<Serie> getSerieForProject(int id) throws SQLException; 
	public Set<Serie> getSerieForAcqDate(int id) throws SQLException; 
	public Set<Serie> getSerieForProtocol(int id) throws SQLException; 
}
