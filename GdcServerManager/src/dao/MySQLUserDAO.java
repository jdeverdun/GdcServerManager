package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import model.Falaise;
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
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Erreur de chargement du driver " + e);
			return null;
		}
		try {
			String url = "jdbc:mysql://localhost:3306/jdeverdun";
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
			stmt = connection.createStatement();
			ProjectDAO projdao=new MySQLProjectDAO();
			rset = stmt.executeQuery("select * from User");

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				User user = new User();
				user.setId(rset.getInt("id"));
				user.setEmail(rset.getString("email"));
				user.setLogin(rset.getString("login"));
				user.setNom(rset.getString("nom"));
				user.setPassword(rset.getString("password"));
				user.setPrenom(rset.getString("prenom"));
				user.setProjects(projdao.getProjectsForUser(""+user.getId()));
				// ajout a la liste des grimpeurs
				users.add(user);
			}
			return users;
		} catch (Exception e) {
			System.err.println("Erreur SQL " + e);
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
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Erreur de chargement du driver " + e);
			return null;
		}
		try {
			String url = "jdbc:mysql://localhost:3306/jdeverdun";
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
			stmt = connection.createStatement();

			rset = stmt.executeQuery("select * from User where login='"
					+ login + "' and  password='" + password + "'");
			if (rset != null) {
				rset.next();
				userC.setNom(rset.getString("nom"));
				userC.setPrenom(rset.getString("prenom"));
				userC.setEmail(rset.getString("email"));
				userC.setLogin(rset.getString("login"));
				userC.setPassword(rset.getString("password"));
				userC.setId(Integer.parseInt(rset.getString("id")));
			}

			return userC;

		} catch (SQLException e) {
			System.err.println("Erreur SQL " + e);
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}

	}
	
	/**
     * Insère un tuple dans la table Grimpeur
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
	public boolean newUser( int id, String nom, String prenom,  String email, String login, String password) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				System.err.println("Erreur de chargement du driver " + e);
				return false;
			}
			
			try {
				String url = "jdbc:mysql://localhost:3306/jdeverdun";
				connection = DriverManager.getConnection(url, "root", "jdeverdun");
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into User values ("+id+",'"
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
     * Récupère le plus grand ID de la table User
     * @return
     */
	public int idmax(){
		
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} 
		catch (ClassNotFoundException e) {
			System.err.println("Erreur de chargement du driver " + e);
			return -1;
		}
		
		try {
			String url = "jdbc:mysql://localhost:3306/jdeverdun";
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
			stmt = connection.createStatement();
			int ident=-1;		
	
			rset = stmt.executeQuery("select max(id) from User ;");
			if (rset != null) {
				while(rset.next()){
					System.out.println("id max= "+rset.getInt(1));
					ident=rset.getInt(1)+1;
				}
			}
			
			return ident;
		
		}catch(Exception e){
			System.err.println("Erreur de chargement du driver" + e);	return -1;
		}
		
	}
	
	/**
     * Récupère l'utilisateur ayant l'id "id"
     * @param id
     * @return
     * @throws SQLException
     */
	public User retrieveUser(String id) throws SQLException {
		// TODO Auto-generated method stub
		User userC = new User();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Erreur de chargement du driver " + e);
			return null;
		}
		try {
			String url = "jdbc:mysql://localhost:3306/jdeverdun";
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
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
			System.err.println("Erreur SQL " + e);
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
     * @return
     * @throws SQLException
     */
	public boolean updateUser(String i,String l, String pass,String n,String pr,String e) throws SQLException{
		// la liste de grimpeurs
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.err.println("Erreur de chargement du driver " + e1);
			return false;
		}
		try {
			String url = "jdbc:mysql://localhost:3306/jdeverdun";
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
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