package dao.project;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import model.Patient;
import model.Project;

public interface PatientDAO {

	/**
	 * Recupere tout les patients
	 * @return
	 * @throws SQLException
	 */
	public Collection<Patient> retrieveAll() throws SQLException;

	/**
	 * Recupere le patient avec l'id  id
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public Patient retrievePatient(int id) throws SQLException;

	/**
	 * Recupere un patient via son nom et l'id du projet
	 * @param name
	 * @param project_id
	 * @return
	 * @throws SQLException 
	 */
	public Patient retrievePatient(String name,String birthdate,String sex,float size,float weight, int project_id) throws SQLException;

	public Patient retrievePatient(String name, int id_project) throws SQLException;
	
	public Patient retrievePatient(String name, String projectname) throws SQLException;
	/**
	 * Renvoi l'id max de la table (idmax+1)
	 * @return
	 * @throws SQLException 
	 */
	public int idmax() throws SQLException;

	/**
	 * créé un nouveau patient
	 * @param id
	 * @param name
	 * @param projet_id
	 * @return
	 * @throws SQLException
	 */
	public boolean newPatient(String name,String birthdate,String sex,float size,float weight, int projet_id) throws SQLException;

	/**
	 * met à jours un tuple
	 * @param id
	 * @param name
	 * @param projet_id
	 * @return
	 * @throws SQLException
	 */
	public boolean updatePatient(int id, String name,String birthdate,String sex,float size,float weight, String rkey, int projet_id) throws SQLException;

	public Set<Patient> getPatientsForProject(int project_id)  throws SQLException;

	void removePatient(String project, String patient) throws SQLException;

	int getPatientIdFor(String project, String patient)
			throws SQLException;

	/**
	 * Renvoi le nombre de patients pour un projet donne
	 * @param project_id
	 * @return
	 * @throws SQLException
	 */
	public int getPatientsCountForProject(int project_id) throws SQLException;
	/**
	 * Veirifie si le couple nom de patient, projet existe deja
	 * @param pat
	 * @param toproj
	 * @return
	 * @throws SQLException
	 */
	public boolean exists(Patient pat, Project toproj) throws SQLException;
	
	/**
	 * Change le projet associe a un patient donnee et en cascade  les serie, protocoles, etc
	 * @param pat
	 * @param toproj
	 * @return
	 * @throws SQLException
	 */
	public boolean changeProject(Patient pat, Project toproj) throws SQLException;


}
