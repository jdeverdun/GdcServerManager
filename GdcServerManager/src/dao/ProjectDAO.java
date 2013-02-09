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
      public Project retrieve(int id) throws SQLException;
      
      /**
       * Récupère le plus grand ID de la table Project
       * @return
       */
      public int idmax();
      
      public void newProject(int id) throws SQLException;

}
