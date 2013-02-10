package dao;

import java.sql.SQLException;
import java.util.Collection;
import model.User;

/**
 * Classe faisant les requ�te sur la table User
 * 
 *
 */
public interface UserDAO {
	
	/**
	 * R�cup�re la liste des Grimpeurs pr�sent dans la table User
	 * @return
	 * @throws SQLException
	 */
      public Collection<User> retrieveAll()throws SQLException;
      
      /**
       * Authentifie un utilisateur � partir du login et du mot de passe
       * fournit
       * 
       * @param login
       * @param password
       * @return
       * @throws SQLException
       */
      public User connexion(String login, String password) throws SQLException;
      
      /**
       * R�cup�re le User ayant l'id "id"
       * @param id
       * @return
       * @throws SQLException
       */
      public User retrieveUser(int id) throws SQLException;
      
      /**
       * Ins�re un tuple dans la table User
       * 
       * @param id
       * @param nom
       * @param prenom
       * @param email
       * @param login
       * @param password
       * @return
       * @throws SQLException
       */
      public boolean newUser(int id, String nom, String prenom, String email, String login, String password) throws SQLException;
      
      /**
       * R�cup�re le plus grand ID de la table User
       * @return
       */
      public int idmax();
      
      /**
       * Met � jours un User
       * @param i
       * @param l
       * @param pass
       * @param n
       * @param pr
       * @param e
       * @return
       * @throws SQLException
       */
      public boolean updateUser(int i,String l, String pass,String n,String pr,String e) throws SQLException;
}

