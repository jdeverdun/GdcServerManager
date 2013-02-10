package dao;

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
       * Renvoi l'id max de la table (idmax+1)
       * @return
       */
      public int idmax();
      
      /**
       * cr�� un nouveau patient
       * @param id
       * @param name
       * @param projet_id
       * @return
       * @throws SQLException
       */
      public boolean newPatient(int id, String name, int projet_id) throws SQLException;
      
      /**
       * met � jours un tuple
       * @param id
       * @param name
       * @param projet_id
       * @return
       * @throws SQLException
       */
      public boolean updatePatient(int id, String name, int projet_id) throws SQLException;

      public Set<Patient> getPatientsForProject(int project_id)  throws SQLException;
}
