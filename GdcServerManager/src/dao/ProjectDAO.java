package dao;

import java.sql.SQLException;
import java.util.Collection;

import model.Project;

public interface ProjectDAO {
	
	/**
	 * Récupère la liste des projets
	 * @return
	 * @throws SQLException
	 */
      public Collection<Project> retrieveAll() throws SQLException;
      
      /**
       * Récupère le projet d'ID "id"
       * @param id
       * @return
       * @throws SQLException
       */
      public Project retrieveProject(int id) throws SQLException;
      
      /**
       * Récupère le plus grand ID de la table Project
       * @return
     * @throws SQLException 
       */
      public int idmax() throws SQLException;
      
      public boolean newProject(String string) throws SQLException; 
      
      public boolean updateProject(int id, String name, String rkey) throws SQLException;

      public Project retrieveProject(String name)  throws SQLException;

	

}
