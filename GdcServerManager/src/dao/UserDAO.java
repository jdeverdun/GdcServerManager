package dao;

import java.sql.SQLException;
import java.util.Collection;
import model.User;

/**
 * Classe faisant les requ�te sur la table Grimpeur
 * 
 *
 */
public interface UserDAO {
	
	/**
	 * R�cup�re la liste des Grimpeurs pr�sent dans la table Grimpeur
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
       * R�cup�re le grimpeur ayant l'id "id"
       * @param id
       * @return
       * @throws SQLException
       */
      public User afficheGrimpeur(String id) throws SQLException;
      
      /**
       * Ins�re un tuple dans la table Grimpeur
       * 
       * @param id
       * @param nom
       * @param prenom
       * @param email
       * @param poids
       * @param age
       * @param login
       * @param password
       * @return
       * @throws SQLException
       */
      public boolean nouveauGrimpeur(int id, String nom, String prenom, String email, int poids,int age, String login, String password) throws SQLException;
      
      /**
       * R�cup�re le plus grand ID de la table Grimpeur
       * @return
       */
      public int idmax();
      
      /**
       * Met � jours un grimpeur
       * @param i
       * @param l
       * @param pass
       * @param n
       * @param pr
       * @param e
       * @param a
       * @param p
       * @return
       * @throws SQLException
       */
      public boolean updateGrimpeur(String i,String l, String pass,String n,String pr,String e, String a,String p) throws SQLException;
}

