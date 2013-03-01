package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import oracle.ucp.UniversalConnectionPoolException;

import settings.SQLSettings;
import settings.UserProfile;

import model.Project;
import model.User;

/**
 * Classe faisant les requ�te sur la table Grimpeur
 * 
 *
 */
public class MySQLUserDAO implements UserDAO {

	/**
	 * R�cup�re la liste des Grimpeurs pr�sent dans la table Grimpeur
	 * @return
	 * @throws SQLException
	 */
	public Collection<User> retrieveAll() throws SQLException {
		// la liste de grimpeurs
		Collection<User> users = new ArrayList<User>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from User");

			// boucle sur les resultats de la requête
			while (rset.next()) {
				User user = new User();
				user.setId(rset.getInt("id"));
				user.setEmail(rset.getString("email"));
				user.setLogin(rset.getString("login"));
				user.setNom(rset.getString("nom"));
				user.setPassword(rset.getString("password"));
				user.setPrenom(rset.getString("prenom"));
				user.setLevel(rset.getInt("level"));
				
				users.add(user);
			}
			return users;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	/**
     * Authentifie un utilisateur � partir du login et du mot de passe
     * fournit
     * 
     * @param login
     * @param password
     * @return
     * @throws SQLException
     */
	public User connexion(String login, String password)
			throws SQLException {
		// TODO Auto-generated method stub
		User userC = new User();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			String url = "jdbc:mysql://"+SQLSettings.ADDRESS+":3306/";
			connection = DriverManager.getConnection(url, "fakeuser", "");
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select password('" + password + "');");
			if (rset != null) {
				rset.next();
				UserProfile.ENCRYPTEDPASS = rset.getString(1);
				UserProfile.LOGIN = login;
			}
			// On lance le pool PDS apres avoir mis � jours les infos de connexion
			try {
				SQLSettings.launchPDS();
			} catch (SQLException | UniversalConnectionPoolException e1) {
				System.err.println("PDS could not be launch ... will exit");
				e1.printStackTrace();
			}
			// On recupere une connexion
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from User where login='"
					+ login + "' and  password='" + UserProfile.ENCRYPTEDPASS + "'");
			if (rset != null) {
				rset.next();
				userC.setNom(rset.getString("nom"));
				userC.setPrenom(rset.getString("prenom"));
				userC.setEmail(rset.getString("email"));
				userC.setLogin(rset.getString("login"));
				userC.setPassword(rset.getString("password"));
				userC.setId(Integer.parseInt(rset.getString("id")));
			}else{
				return null;
			}

			return userC;

		} catch (SQLException e) {
			e.printStackTrace();
			//e.printStackTrace();
			throw e;
			//return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}

	}
	
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
	public boolean newUser( String nom, String prenom,  String email, String login, String password) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			try {
				connection = SQLSettings.PDS.getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into User values (NULL,'"
						+ nom + "' ,'" + prenom + "', '"+email+ "', '"+login+"', '"+password+"')");
				
				return true;
				
			}
			catch(Exception e){
				System.err.println("Erreur de chargement du driver " + e);	return false;
			}
			finally {
				stmt.close();
				connection.close();
			}
		
	}
	
	/**
     * R�cup�re le plus grand ID de la table User
     * @return
	 * @throws SQLException 
     */
	public int idmax() throws SQLException{
		
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			rset = stmt.executeQuery("select max(id) from User ;");
			if (rset != null) {
				while(rset.next()){
					//System.out.println("id max= "+rset.getInt(1));
					ident=rset.getInt(1);
				}
			}
			
			return ident;
		
		}catch(Exception e){
			e.printStackTrace();	return -1;
		}finally {
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}
	
	/**
     * R�cup�re l'utilisateur ayant l'id "id"
     * @param id
     * @return
     * @throws SQLException
     */
	public User retrieveUser(int id) throws SQLException {
		// TODO Auto-generated method stub
		User userC = new User();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
		
			rset = stmt.executeQuery("select * from User where id="+id);
			while(rset.next()){
				userC.setNom(rset.getString("nom"));
				userC.setPrenom(rset.getString("prenom"));
				userC.setEmail(rset.getString("email"));
				userC.setLogin(rset.getString("login"));
				userC.setPassword(rset.getString("password"));
				userC.setId(Integer.parseInt(rset.getString("id")));
			}
		
			return userC;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}
	
	/**
     * Met � jours un user
     * @param i
     * @param l
     * @param pass
     * @param n
     * @param pr
     * @param e
     * @return
     * @throws SQLException
     */
	public boolean updateUser(int i,String l, String pass,String n,String pr,String e) throws SQLException{
		// la liste de grimpeurs
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update User set login='"+l+"',password='"+pass+"',prenom='"+pr+"',nom='"+n+"',email='"+e+"' where id="+i);
			return true;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			return false;
		} finally {
			stmt.close();
			connection.close();
		}
	}
}