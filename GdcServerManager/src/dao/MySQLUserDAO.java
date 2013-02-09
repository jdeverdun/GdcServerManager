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
		Collection<User> grimpeurs = new ArrayList<User>();
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
			AscensionDAO asdao=new MySQLAscensionDAO();
			rset = stmt.executeQuery("select * from Grimpeur");

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				User grimpeur = new User();
				grimpeur.setId(rset.getInt("id"));
				grimpeur.setAge(rset.getInt("age"));
				grimpeur.setEmail(rset.getString("email"));
				grimpeur.setLogin(rset.getString("login"));
				grimpeur.setNom(rset.getString("nom"));
				grimpeur.setPassword(rset.getString("password"));
				grimpeur.setPoids(rset.getInt("poids"));
				grimpeur.setPrenom(rset.getString("prenom"));
				grimpeur.setAscensions(asdao.getAscensionForGrimpeur(""+grimpeur.getId()));
				// ajout a la liste des grimpeurs
				grimpeurs.add(grimpeur);
			}
			return grimpeurs;
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
		User grimpeursC = new User();
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

			rset = stmt.executeQuery("select * from Grimpeur where login='"
					+ login + "' and  password='" + password + "'");
			if (rset != null) {
				rset.next();
				grimpeursC.setNom(rset.getString("nom"));
				grimpeursC.setPrenom(rset.getString("prenom"));
				grimpeursC.setAge(Integer.parseInt(rset.getString("age")));
				grimpeursC.setEmail(rset.getString("email"));
				grimpeursC.setPoids(Integer.parseInt(rset.getString("poids")));
				grimpeursC.setLogin(rset.getString("login"));
				grimpeursC.setPassword(rset.getString("password"));
				grimpeursC.setId(Integer.parseInt(rset.getString("id")));
			}

			return grimpeursC;

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
     * @param poids
     * @param age
     * @param login
     * @param password
     * @return
     * @throws SQLException
     */
	public boolean nouveauGrimpeur( int id, String nom, String prenom,  String email, int poids,  int age, String login, String password) throws SQLException {
		
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
				
				rset = stmt.execute("insert into Grimpeur values ("+id+",'"
						+ nom + "' ,'" + prenom + "', '"+email+ "', "+poids+", "+age+", '"+login+"', '"+password+"')");
				
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
     * Récupère le plus grand ID de la table Grimpeur
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
	
			rset = stmt.executeQuery("select max(id) from Grimpeur ;");
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
     * Récupère le grimpeur ayant l'id "id"
     * @param id
     * @return
     * @throws SQLException
     */
	public User afficheGrimpeur(String id) throws SQLException {
		// TODO Auto-generated method stub
		User grimpeursC = new User();
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
		
			rset = stmt.executeQuery("select * from Grimpeur where id="+id);
			while(rset.next()){
				grimpeursC.setNom(rset.getString("nom"));
				grimpeursC.setPrenom(rset.getString("prenom"));
				grimpeursC.setAge(Integer.parseInt(rset.getString("age")));
				grimpeursC.setEmail(rset.getString("email"));
				grimpeursC.setPoids(Integer.parseInt(rset.getString("poids")));
				grimpeursC.setLogin(rset.getString("login"));
				grimpeursC.setPassword(rset.getString("password"));
				grimpeursC.setId(Integer.parseInt(rset.getString("id")));
			}
		
			return grimpeursC;
		
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
     * Met à jours un grimpeur
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
	public boolean updateGrimpeur(String i,String l, String pass,String n,String pr,String e, String a,String p) throws SQLException{
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
			rset = stmt.executeUpdate("update Grimpeur set login='"+l+"',password='"+pass+"',prenom='"+pr+"',nom='"+n+"',email='"+e+"',age="+a+",poids="+p+" where id="+i);
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