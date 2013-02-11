package dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import model.Protocol;

public interface ProtocolDAO {
	public Collection<Protocol> retrieveAll() throws SQLException;

    public Protocol retrieveProtocol(int id) throws SQLException;  

    public int idmax();
      

    public boolean newProtocol(int id, String name, int projet_id, int patient_id, int id_acqdate) throws SQLException;
      
    public boolean updateProtocol(int id, String name, int projet_id, int patient_id, int id_acqdate) throws SQLException;
      
	public Set<Protocol> getProtocolForPatient(int id) throws SQLException;
	public Set<Protocol> getProtocolForProject(int id) throws SQLException; 
	public Set<Protocol> getProtocolForAcqDate(int id) throws SQLException; 
}
