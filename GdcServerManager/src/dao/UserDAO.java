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
	 * R�cup�re la liste des user pr�sent dans la table User
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
       * R�cup�re le User ayant le login "login"
       * @param id
       * @return
       * @throws SQLException
       */
      public User retrieveUser(String login) throws SQLException;
      
      /**
       * Ins�re un tuple dans la table User et renvoi 0 si tout c'est bien passe
       * 1 si le login existe deja
       * 2 si erreur SQL autre
       * firstconnect obligatoirement a 1
       * 
       * @param id
       * @param nom
       * @param prenom
       * @param email
       * @param login
       * @param password
       * @param level
       * @return
       * @throws SQLException
       */
      public int newUser(String nom, String prenom, String email, String login, String password, int level) throws SQLException;
      /**
       * Cree un nouvel utilisateur 
       * Renvoi 3 si le "User" n'est pas complet
       * 
       * @param u
       * @return
       * @throws SQLException
       */
      public int newUser(User u) throws SQLException;
      /**
       * R�cup�re le plus grand ID de la table User
       * @return
     * @throws SQLException 
       */
      public int idmax() throws SQLException;
      
      /**
       * Met � jours un User
       * @param i
       * @param l
       * @param pass
       * @param n
       * @param pr
       * @param e
       * @param level
       * @param fconnect
       * @return
       * @throws SQLException
       */
      public boolean updateUser(int i,String l, String pass,String n,String pr,String e,int level,int fconnect) throws SQLException;
      
      /**
       * Encrypt un mot de passe via la commande SQL
       * @param password
       * @return
       * @throws SQLException
       */
      public String encryptPass(String password) throws SQLException;

      /**
       * Met � jours un utilisateur grace a un objet user
       * @param user
       * @throws SQLException
       */
      public boolean updateUser(User user) throws SQLException;
}

