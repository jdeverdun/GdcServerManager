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

import settings.SQLSettings;
import settings.UserProfile;


import model.Serie;

public class MySQLSerieDAO implements SerieDAO{
	public Collection<Serie> retrieveAll() throws SQLException {
		Collection<Serie> series = new ArrayList<Serie>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			ProtocolDAO pdao = new MySQLProtocolDAO();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from Serie");
			else
				rset = stmt.executeQuery("select * from Serie_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = new Serie();
				serie.setId(rset.getInt("id"));
				serie.setName(rset.getString("name"));
				serie.setHasNifti(rset.getInt("hasnifti"));
				serie.setProtocole(pdao.retrieveProtocol(rset.getInt("id_protocol")));
				// instantiation en cascade grace à acquisitiondate
				serie.setAcquistionDate(serie.getProtocole().getAcquisitionDate());
				serie.setPatient(serie.getAcquistionDate().getPatient());
				serie.setProjet(serie.getPatient().getProject());
				series.add(serie);
			}
			return series;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newSerie(String nom, int hasnifti, int project_id, int patient_id, int id_acqdate, int id_protocol) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			try {
				connection = SQLSettings.PDS.getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into Serie values (NULL,'"
						+ nom + "', "+hasnifti+","+project_id+","+patient_id+","+id_acqdate+", "+id_protocol+")");
				
				return true;
				
			}
			catch(Exception e){
				e.printStackTrace();	throw e;
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select max(id) from Serie ;");
			else
				rset = stmt.executeQuery("select max(id) from Serie_"+UserProfile.CURRENT_USER+" ;");
			
			if (rset != null) {
				while(rset.next()){
					//System.out.println("id max= "+rset.getInt(1));
					ident=rset.getInt(1);
				}
			}
			
			return ident;
		
		}catch(Exception e){
			e.printStackTrace();	throw e;
		}finally {
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}
	

	public Serie retrieveSerie(int id) throws SQLException {
		Serie serie = new Serie();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			ProtocolDAO pdao = new MySQLProtocolDAO();

			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from Serie where id="+id);
			else
				rset = stmt.executeQuery("select * from Serie_"+UserProfile.CURRENT_USER.getId()+" where id="+id);
			
			while(rset.next()){
				serie.setId(rset.getInt("id"));
				serie.setName(rset.getString("name"));
				serie.setHasNifti(rset.getInt("hasnifti"));
				serie.setProtocole(pdao.retrieveProtocol(rset.getInt("id_protocol")));
				// instantiation en cascade grace à acquisitiondate
				serie.setAcquistionDate(serie.getProtocole().getAcquisitionDate());
				serie.setPatient(serie.getAcquistionDate().getPatient());
				serie.setProjet(serie.getPatient().getProject());
			}
		
			return serie;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}

	@Override
	public Serie retrieveSerie(String name, int project_id, int patient_id,
			int acqDate_id, int protocol_id) throws SQLException {
		Serie serie = new Serie();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			ProtocolDAO pdao = new MySQLProtocolDAO();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3){
				rset = stmt.executeQuery("select * from Serie where name='"+name+"' and id_project="+project_id+" and " +
					" id_patient="+patient_id+" and id_acqdate="+acqDate_id+" and id_protocol="+protocol_id);
			}else{
				rset = stmt.executeQuery("select * from Serie_"+UserProfile.CURRENT_USER.getId()+" where name='"+name+"' and id_project="+project_id+" and " +
						" id_patient="+patient_id+" and id_acqdate="+acqDate_id+" and id_protocol="+protocol_id);
			}
			while(rset.next()){
				serie.setId(rset.getInt("id"));
				serie.setName(rset.getString("name"));
				serie.setHasNifti(rset.getInt("hasnifti"));
				serie.setProtocole(pdao.retrieveProtocol(rset.getInt("id_protocol")));
				// instantiation en cascade grace à acquisitiondate
				serie.setAcquistionDate(serie.getProtocole().getAcquisitionDate());
				serie.setPatient(serie.getAcquistionDate().getPatient());
				serie.setProjet(serie.getPatient().getProject());
			}
		
			return serie;
		
		} catch (SQLException e) {
			e.printStackTrace();
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update Serie set name='"+name+"',hasnifti="+hasnifti+", id_project="+id_project+", id_patient="+id_patient+", id_acqdate="+id_acqdate+", id_protocol="+id_protocol+" where id="+id);
			return true;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			throw e2;
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();

			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from Serie where id_patient="+id);
			else
				rset = stmt.executeQuery("select * from Serie_"+UserProfile.CURRENT_USER.getId()+" where id_patient="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt("id"));	
				if(serie!=null) 
					series.add(serie);
			}
			return series;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from Serie where id_project="+id);
			else
				rset = stmt.executeQuery("select * from Serie_"+UserProfile.CURRENT_USER.getId()+" where id_project="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt("id"));	
				if(serie!=null) 
					series.add(serie);
			}
			return series;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from Serie where id_acqdate="+id);
			else
				rset = stmt.executeQuery("select * from Serie_"+UserProfile.CURRENT_USER.getId()+" where id_acqdate="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt("id"));	
				if(serie!=null) 
					series.add(serie);
			}
			return series;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from Serie where id_protocol="+id);
			else
				rset = stmt.executeQuery("select * from Serie_"+UserProfile.CURRENT_USER.getId()+" where id_protocol="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt("id"));	
				if(serie!=null) 
					series.add(serie);
			}
			return series;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

}
