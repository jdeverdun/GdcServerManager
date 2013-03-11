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
 * Classe faisant les requête sur la table Grimpeur
 * 
 *
 */
public class MySQLUserDAO implements UserDAO {

	/**
	 * Récupère la liste des Grimpeurs présent dans la table Grimpeur
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
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from User");
			else
				rset = stmt.executeQuery("select * from User_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				User user = new User();
				user.setId(rset.getInt("id"));
				user.setEmail(rset.getString("email"));
				user.setLogin(rset.getString("login"));
				user.setNom(rset.getString("nom"));
				user.setPassword(rset.getString("password"));
				user.setPrenom(rset.getString("prenom"));
				user.setLevel(rset.getInt("level"));
				user.setFirstConnect(rset.getInt("firstconnect"));
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
     * Authentifie un utilisateur à partir du login et du mot de passe
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
			// On lance le pool PDS apres avoir mis à jours les infos de connexion
			try {
				SQLSettings.launchPDS();
			} catch (SQLException | UniversalConnectionPoolException e1) {
				System.err.println("PDS could not be launch ... will exit");
				e1.printStackTrace();
			}
			// On recupere une connexion
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3){
				rset = stmt.executeQuery("select * from User where login='"
						+ login + "' and  password='" + UserProfile.ENCRYPTEDPASS + "'");
			}else{
				rset = stmt.executeQuery("select * from User_"+UserProfile.CURRENT_USER.getId()+" where login='"
						+ login + "' and  password='" + UserProfile.ENCRYPTEDPASS + "'");
			}
			if (rset != null) {
				rset.next();
				userC.setNom(rset.getString("nom"));
				userC.setPrenom(rset.getString("prenom"));
				userC.setEmail(rset.getString("email"));
				userC.setLogin(rset.getString("login"));
				userC.setPassword(rset.getString("password"));
				userC.setId(Integer.parseInt(rset.getString("id")));
				userC.setLevel(rset.getInt("level"));
				userC.setFirstConnect(rset.getInt("firstconnect"));
				UserProfile.CURRENT_USER = userC;
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
     * Insère un tuple dans la table User et renvoi 0 si tout c'est bien passe
     * 1 si le login existe deja
     * 2 si erreur SQL autre
     * firstConnect est obligatoirement place a 1
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
	public int newUser( String nom, String prenom,  String email, String login, String password, int level) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			try {
				connection = SQLSettings.PDS.getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into User values (NULL,'"
						+ nom + "' ,'" + prenom + "', '"+email+ "', '"+login+"', '"+password+"', "+level+",1)");
				
				return 0;
				
			}
			catch(Exception e){
				if(e.toString().contains("UNIQUE"))
					return 1;
				else{
					System.err.println("Erreur SQL " + e);
					return 2;
				}
			}
			finally {
				stmt.close();
				connection.close();
			}
		
	}
	
	/**
     * Cree un nouvel utilisateur 
     * Renvoi 3 si le "User" n'est pas complet
     * 
     * @param u
     * @return
     * @throws SQLException
     */
	public int newUser(User u) throws SQLException {
		if(u.isReadyForInsert())
			return newUser(u.getNom(), u.getPrenom(), u.getEmail(), u.getLogin(), u.getPassword(),u.getLevel());
		else
			return 3;
	}
	
	/**
     * Récupère le plus grand ID de la table User
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
	
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select max(id) from User ;");
			else
				rset = stmt.executeQuery("select max(id) from User_"+UserProfile.CURRENT_USER.getId()+" ;");
				
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
     * Récupère l'utilisateur ayant l'id "id"
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
		
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from User where id="+id);
			else
				rset = stmt.executeQuery("select * from User_"+UserProfile.CURRENT_USER.getId()+" where id="+id);
			
			while(rset.next()){
				userC.setNom(rset.getString("nom"));
				userC.setPrenom(rset.getString("prenom"));
				userC.setEmail(rset.getString("email"));
				userC.setLogin(rset.getString("login"));
				userC.setPassword(rset.getString("password"));
				userC.setId(Integer.parseInt(rset.getString("id")));
				userC.setFirstConnect(rset.getInt("firstconnect"));
				userC.setLevel(rset.getInt("level"));
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
     * Récupère l'utilisateur ayant le login "login"
     * @param id
     * @return
     * @throws SQLException
     */
	public User retrieveUser(String login) throws SQLException {
		// TODO Auto-generated method stub
		User userC = new User();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
		
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from User where login="+login);
			else
				rset = stmt.executeQuery("select * from User_"+UserProfile.CURRENT_USER.getId()+" where login="+login);
			
			while(rset.next()){
				userC.setNom(rset.getString("nom"));
				userC.setPrenom(rset.getString("prenom"));
				userC.setEmail(rset.getString("email"));
				userC.setLogin(rset.getString("login"));
				userC.setPassword(rset.getString("password"));
				userC.setId(Integer.parseInt(rset.getString("id")));
				userC.setLevel(rset.getInt("level"));
				userC.setFirstConnect(rset.getInt("firstconnect"));
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
     * Met à jours un user
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
	public boolean updateUser(int i,String l, String pass,String n,String pr,String e,int level, int fconnect) throws SQLException{
		// la liste de grimpeurs
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeUpdate("update User set login='"+l+"',password='"+pass+"',prenom='"+pr+"',nom='"+n+"',email='"+e+"', level="+level+", firstconnect="+fconnect+" where id="+i);
			else
				rset = stmt.executeUpdate("update User_"+UserProfile.CURRENT_USER.getId()+" set login='"+l+"',password='"+pass+"',prenom='"+pr+"',nom='"+n+"',email='"+e+"', level="+level+", firstconnect="+fconnect+" where id="+i);
			
			return true;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			return false;
		} finally {
			stmt.close();
			connection.close();
		}
	}
	
	public boolean updateUser(User user) throws SQLException{
		return updateUser(user.getId(), user.getLogin(), user.getPassword(), user.getNom(), user.getPrenom(), user.getEmail(), user.getLevel(), user.getFirstConnect());
	}
	
	public String encryptPass(String password)
			throws SQLException {
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
				return rset.getString(1);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}
}