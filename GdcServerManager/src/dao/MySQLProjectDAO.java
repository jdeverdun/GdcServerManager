package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import model.Patient;
import model.Project;
import model.User;

/**
 * Classe faisant les requête sur la table Project
 * 
 *
 */
public class MySQLProjectDAO implements ProjectDAO {


	public Collection<Project> retrieveAll() throws SQLException {
		// la liste de grimpeurs
		Collection<Project> projects = new ArrayList<Project>();
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
			PatientDAO patdao=new MySQLPatientDAO();
			UserProjectDAO updao=new MySQLUserProjectDAO();
			rset = stmt.executeQuery("select * from Project");

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Project proj = new Project();
				proj.setId(rset.getInt("id"));
				proj.setNom(rset.getString("nom"));				
				// ajout a la liste des grimpeurs
				projects.add(proj);
			}
			return projects;
		} catch (Exception e) {
			System.err.println("Erreur SQL " + e);
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newProject( int id, String nom) throws SQLException {
		
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
				
				rset = stmt.execute("insert into Project values ("+id+",'"
						+ nom + "')");
				
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
     * Récupère le plus grand ID de la table Project
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
	
			rset = stmt.executeQuery("select max(id) from Project ;");
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
     * Récupère le projet ayant l'id "id"
     * @param id
     * @return
     * @throws SQLException
     */
	public Project retrieveProject(int id) throws SQLException {
		// TODO Auto-generated method stub
		Project projectC = new Project();
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
		
			rset = stmt.executeQuery("select * from Project where id="+id);
			while(rset.next()){
				projectC.setNom(rset.getString("nom"));
				projectC.setId(rset.getInt("id"));
				
			}
		
			
			return projectC;
		
		} catch (SQLException e) {
			System.err.println("Erreur SQL " + e);
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}
	
	




	@Override
	public boolean updateProject(int id, String name) throws SQLException {
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
			rset = stmt.executeUpdate("update Project set name='"+name+"' where id="+id);
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