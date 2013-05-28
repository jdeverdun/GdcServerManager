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
      public boolean updatePatient(int id, String name,String birthdate,String sex,float size,float weight, int projet_id) throws SQLException;

      public Set<Patient> getPatientsForProject(int project_id)  throws SQLException;

	void removePatient(String project, String patient) throws SQLException;

	String getPatientIdFor(String project, String patient)
			throws SQLException;

	
}
