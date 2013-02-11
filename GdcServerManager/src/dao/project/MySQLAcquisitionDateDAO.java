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

import model.AcquisitionDate;
import model.Patient;


public class MySQLAcquisitionDateDAO implements AcquisitionDateDAO {
	public Collection<AcquisitionDate> retrieveAll() throws SQLException {
		Collection<AcquisitionDate> acqDates = new ArrayList<AcquisitionDate>();
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
			PatientDAO pdao = new MySQLPatientDAO();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from AcquisitionDate");

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				AcquisitionDate acq = new AcquisitionDate();
				acq.setId(rset.getInt("id"));
				acq.setDate(rset.getDate("acqdate"));
				acq.setPatient(pdao.retrievePatient(rset.getInt("id_patient")));
				acq.setProjet(acq.getPatient().getProject());
				acqDates.add(acq);
			}
			return acqDates;
		} catch (Exception e) {
			System.err.println("Erreur SQL " + e);
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newAcqDate( int id, String nom, int project_id, int patient_id) throws SQLException {
		
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
				
				rset = stmt.execute("insert into Acquisitiondate values ("+id+",'"
						+ nom + "', "+project_id+","+patient_id+")");
				
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
     * Récupère le plus grand ID de la table AcqDate
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
	
			rset = stmt.executeQuery("select max(id) from AcquisitionDate ;");
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
	

	public AcquisitionDate retrieveAcqDate(int id) throws SQLException {
		// TODO Auto-generated method stub
		AcquisitionDate acq = new AcquisitionDate();
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
			PatientDAO pdao=new MySQLPatientDAO();			
			rset = stmt.executeQuery("select * from AcquisitionDate where id="+id);
			while(rset.next()){
				acq.setId(rset.getInt("id"));
				acq.setDate(rset.getDate("acqdate"));
				acq.setPatient(pdao.retrievePatient(rset.getInt("id_patient")));
				acq.setProjet(acq.getPatient().getProject());
			}
		
			return acq;
		
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
	public boolean updateAcqDate(int id, String name, int id_project, int id_patient) throws SQLException {
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
			rset = stmt.executeUpdate("update AcquisitionDate set name='"+name+"', id_project="+id_project+", id_patient="+id_patient+" where id="+id);
			return true;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			return false;
		} finally {
			stmt.close();
			connection.close();
		}
	}






	@Override
	public Set<AcquisitionDate> getAcqDateForPatient(int id)
			throws SQLException {
		Set<AcquisitionDate> acqs = new HashSet<AcquisitionDate>();
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
			rset = stmt.executeQuery("select * from AcquisitionDate where id_patient="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				AcquisitionDate acq = retrieveAcqDate(rset.getInt("id"));	
				if(acq!=null) 
					acqs.add(acq);
			}
			return acqs;
		} catch (Exception e) {
			System.err.println("Erreur SQL " + e);
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}




	@Override
	public Set<AcquisitionDate> getAcqDateForProject(int id)
			throws SQLException {
		Set<AcquisitionDate> acqs = new HashSet<AcquisitionDate>();
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
			rset = stmt.executeQuery("select * from AcquisitionDate where id_project="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				AcquisitionDate acq = retrieveAcqDate(rset.getInt("id"));	
				if(acq!=null) 
					acqs.add(acq);
			}
			return acqs;
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
