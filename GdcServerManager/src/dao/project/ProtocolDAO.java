package dao.project;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import model.Protocol;

public interface ProtocolDAO {
	public Collection<Protocol> retrieveAll() throws SQLException;

    public Protocol retrieveProtocol(int id) throws SQLException;  
    /**
     * Recupere un protocol via ses champs nom et les id du projet/patient etc
     * @param string
     * @param project_id
     * @param patient_id
     * @param acqDate_id
     * @return
     * @throws SQLException 
     */
    public Protocol retrieveProtocol(String name, int project_id,
			int patient_id, int acqDate_id) throws SQLException; 
    public int idmax() throws SQLException;
      

    public boolean newProtocol(String name, int projet_id, int patient_id, int id_acqdate) throws SQLException;
      
    public boolean updateProtocol(int id, String name, int projet_id, int patient_id, int id_acqdate) throws SQLException;
      
	public Set<Protocol> getProtocolForPatient(int id) throws SQLException;
	public Set<Protocol> getProtocolForProject(int id) throws SQLException; 
	public Set<Protocol> getProtocolForAcqDate(int id) throws SQLException;

	int getProtocolIdFor(String project, String patient, String acqdate,
			String protocol) throws SQLException;

	void removeProtocol(String project, String patient, String acqdate,
			String protocol) throws SQLException;

	
}
