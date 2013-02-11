package dao.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dao.MySQLProjectDAO;
import dao.ProjectDAO;

import model.AcquisitionDate;
import model.Patient;
import model.Project;
import model.User;

public class MySQLPatientDAO implements PatientDAO {
	public Collection<Patient> retrieveAll() throws SQLException {
		Collection<Patient> patients = new ArrayList<Patient>();
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
			ProjectDAO projdao=new MySQLProjectDAO();	
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from Patient");

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Patient pat = new Patient();
				pat.setId(rset.getInt("id"));
				pat.setNom(rset.getString("name"));
				pat.setProject(projdao.retrieveProject(rset.getInt("id_project")));
				patients.add(pat);
			}
			return patients;
		} catch (Exception e) {
			System.err.println("Erreur SQL " + e);
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newPatient( int id, String nom, int project_id) throws SQLException {
		
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
				
				rset = stmt.execute("insert into Patient values ("+id+",'"
						+ nom + "', "+id+")");
				
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
     * Récupère le plus grand ID de la table Patient
     * @return
	 * @throws SQLException 
     */
	public int idmax() throws SQLException{
		
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
	
			rset = stmt.executeQuery("select max(id) from Patient ;");
			if (rset != null) {
				while(rset.next()){
					System.out.println("id max= "+rset.getInt(1));
					ident=rset.getInt(1)+1;
				}
			}
			
			return ident;
		
		}catch(Exception e){
			System.err.println("Erreur de chargement du driver" + e);	return -1;
		}finally {
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}
	

	public Patient retrievePatient(int id) throws SQLException {
		// TODO Auto-generated method stub
		Patient pat = new Patient();
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
			rset = stmt.executeQuery("select * from Patient where id="+id);
			while(rset.next()){
				pat.setNom(rset.getString("nom"));
				pat.setId(rset.getInt("id"));
				pat.setProject(projdao.retrieveProject(rset.getInt("id_project")));
			}
		
			return pat;
		
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
	public boolean updatePatient(int id, String name, int id_project) throws SQLException {
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
			rset = stmt.executeUpdate("update Patient set name='"+name+"',id_project="+id_project+" where id="+id);
			return true;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			return false;
		} finally {
			stmt.close();
			connection.close();
		}
	}
	
	public Set<Patient> getPatientsForProject(int project_id) throws SQLException {
		Set<Patient> patients = new HashSet<Patient>();
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
			rset = stmt.executeQuery("select * from Patient where id_project="+project_id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Patient pat = retrievePatient(rset.getInt("id"));	
				if(pat!=null) 
					patients.add(pat);
			}
			return patients;
		} catch (Exception e) {
			System.err.println("Erreur SQL " + e);
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}
}
