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
import model.Protocol;

public class MySQLProtocolDAO implements ProtocolDAO{
	public Collection<Protocol> retrieveAll() throws SQLException {
		Collection<Protocol> protocols = new ArrayList<Protocol>();
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
			AcquisitionDateDAO adao = new MySQLAcquisitionDateDAO();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from Protocol");

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Protocol prot = new Protocol();
				prot.setId(rset.getInt("id"));
				prot.setName(rset.getString("name"));
				prot.setAcquisitionDate(adao.retrieveAcqDate(rset.getInt("id_acqdate")));
				prot.setPatient(prot.getAcquisitionDate().getPatient());
				prot.setProjet(prot.getPatient().getProject());
				protocols.add(prot);
			}
			return protocols;
		} catch (Exception e) {
			System.err.println("Erreur SQL " + e);
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newProtocol( String nom, int project_id, int patient_id, int id_acqdate) throws SQLException {
		
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
				
				rset = stmt.execute("insert into Protocol values ('"
						+ nom + "', "+project_id+","+patient_id+","+id_acqdate+")");
				
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
     * R�cup�re le plus grand ID de la table AcqDate
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
	
			rset = stmt.executeQuery("select max(id) from Protocol ;");
			if (rset != null) {
				while(rset.next()){
					//System.out.println("id max= "+rset.getInt(1));
					ident=rset.getInt(1);
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
	

	public Protocol retrieveProtocol(int id) throws SQLException {
		// TODO Auto-generated method stub
		Protocol prot = new Protocol();
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
			AcquisitionDateDAO adao = new MySQLAcquisitionDateDAO();	
			rset = stmt.executeQuery("select * from Protocol where id="+id);
			while(rset.next()){
				prot.setId(rset.getInt("id"));
				prot.setName(rset.getString("name"));
				prot.setAcquisitionDate(adao.retrieveAcqDate(rset.getInt("id_acqdate")));
				prot.setPatient(prot.getAcquisitionDate().getPatient());
				prot.setProjet(prot.getPatient().getProject());
			}
		
			return prot;
		
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
	public boolean updateProtocol(int id, String name, int id_project, int id_patient, int id_acqdate) throws SQLException {
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
			rset = stmt.executeUpdate("update Protocol set name='"+name+"', id_project="+id_project+", id_patient="+id_patient+", id_acqdate="+id_acqdate+" where id="+id);
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
	public Set<Protocol> getProtocolForPatient(int id)
			throws SQLException {
		Set<Protocol> prots = new HashSet<Protocol>();
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
			rset = stmt.executeQuery("select * from Protocol where id_patient="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Protocol prot = retrieveProtocol(rset.getInt("id"));	
				if(prot!=null) 
					prots.add(prot);
			}
			return prots;
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
	public Set<Protocol> getProtocolForProject(int id)
			throws SQLException {
		Set<Protocol> prots = new HashSet<Protocol>();
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
			rset = stmt.executeQuery("select * from Protocol where id_project="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Protocol prot = retrieveProtocol(rset.getInt("id"));	
				if(prot!=null) 
					prots.add(prot);
			}
			return prots;
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
	public Set<Protocol> getProtocolForAcqDate(int id)
			throws SQLException {
		Set<Protocol> prots = new HashSet<Protocol>();
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
			rset = stmt.executeQuery("select * from Protocol where id_acqdate="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Protocol prot = retrieveProtocol(rset.getInt("id"));	
				if(prot!=null) 
					prots.add(prot);
			}
			return prots;
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
