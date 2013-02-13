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


import model.Serie;

public class MySQLSerieDAO implements SerieDAO{
	public Collection<Serie> retrieveAll() throws SQLException {
		Collection<Serie> series = new ArrayList<Serie>();
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
			ProtocolDAO pdao = new MySQLProtocolDAO();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from Serie");

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Serie serie = new Serie();
				serie.setId(rset.getInt("id"));
				serie.setName(rset.getString("name"));
				serie.setHasNifti(rset.getInt("hasnifti"));
				serie.setProtocole(pdao.retrieveProtocol(rset.getInt("id_protocol")));
				// instantiation en cascade grace � acquisitiondate
				serie.setAcquistionDate(serie.getProtocole().getAcquisitionDate());
				serie.setPatient(serie.getAcquistionDate().getPatient());
				serie.setProjet(serie.getPatient().getProject());
				series.add(serie);
			}
			return series;
		} catch (Exception e) {
			System.err.println("Erreur SQL " + e);
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newSerie( int id, String nom, int hasnifti, int project_id, int patient_id, int id_acqdate, int id_protocol) throws SQLException {
		
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
				
				rset = stmt.execute("insert into Serie values ("+id+",'"
						+ nom + "', "+hasnifti+","+project_id+","+patient_id+","+id_acqdate+", "+id_protocol+")");
				
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
	
			rset = stmt.executeQuery("select max(id) from Serie ;");
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
	

	public Serie retrieveSerie(int id) throws SQLException {
		// TODO Auto-generated method stub
		Serie serie = new Serie();
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
			ProtocolDAO pdao = new MySQLProtocolDAO();
			rset = stmt.executeQuery("select * from Serie where id="+id);
			while(rset.next()){
				serie.setId(rset.getInt("id"));
				serie.setName(rset.getString("name"));
				serie.setHasNifti(rset.getInt("hasnifti"));
				serie.setProtocole(pdao.retrieveProtocol(rset.getInt("id_protocol")));
				// instantiation en cascade grace � acquisitiondate
				serie.setAcquistionDate(serie.getProtocole().getAcquisitionDate());
				serie.setPatient(serie.getAcquistionDate().getPatient());
				serie.setProjet(serie.getPatient().getProject());
			}
		
			return serie;
		
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
	public boolean updateSerie(int id, String name,int hasnifti, int id_project, int id_patient, int id_acqdate, int id_protocol) throws SQLException {
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
			rset = stmt.executeUpdate("update Serie set name='"+name+"',hasnifti="+hasnifti+", id_project="+id_project+", id_patient="+id_patient+", id_acqdate="+id_acqdate+", id_protocol="+id_protocol+" where id="+id);
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
	public Set<Serie> getSerieForPatient(int id)
			throws SQLException {
		Set<Serie> series = new HashSet<Serie>();
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
			rset = stmt.executeQuery("select * from Serie where id_patient="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt("id"));	
				if(serie!=null) 
					series.add(serie);
			}
			return series;
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
	public Set<Serie> getSerieForProject(int id)
			throws SQLException {
		Set<Serie> series = new HashSet<Serie>();
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
			rset = stmt.executeQuery("select * from Serie where id_project="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt("id"));	
				if(serie!=null) 
					series.add(serie);
			}
			return series;
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
	public Set<Serie> getSerieForAcqDate(int id)
			throws SQLException {
		Set<Serie> series = new HashSet<Serie>();
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
			rset = stmt.executeQuery("select * from Serie where id_acqdate="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt("id"));	
				if(serie!=null) 
					series.add(serie);
			}
			return series;
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
	public Set<Serie> getSerieForProtocol(int id)
			throws SQLException {
		Set<Serie> series = new HashSet<Serie>();
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
			rset = stmt.executeQuery("select * from Serie where id_protocol="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt("id"));	
				if(serie!=null) 
					series.add(serie);
			}
			return series;
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
