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
import settings.sql.DBTables;
import settings.sql.tables.NiftiImageTable;
import settings.sql.tables.SerieTable;


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
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+"");
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+"_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = new Serie();
				serie.setId(rset.getInt(SQLSettings.TABLES.getSerie().getId()));
				serie.setName(rset.getString(SQLSettings.TABLES.getSerie().getName()));
				serie.setHasNifti(rset.getInt(SQLSettings.TABLES.getSerie().getHasnifti()));
				serie.setProtocole(pdao.retrieveProtocol(rset.getInt(SQLSettings.TABLES.getSerie().getId_protocol())));
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
				
				rset = stmt.execute("insert into "+SQLSettings.TABLES.getSerie().TNAME+" values (NULL,'"
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
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getSerie().getId()+") from "+SQLSettings.TABLES.getSerie().TNAME+" ;");
			else
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getSerie().getId()+") from "+SQLSettings.TABLES.getSerie().TNAME+"_"+UserProfile.CURRENT_USER+" ;");
			
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
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+" where "+SQLSettings.TABLES.getSerie().getId()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getSerie().getId()+"="+id);
			
			while(rset.next()){
				serie.setId(rset.getInt(SQLSettings.TABLES.getSerie().getId()));
				serie.setName(rset.getString(SQLSettings.TABLES.getSerie().getName()));
				serie.setHasNifti(rset.getInt(SQLSettings.TABLES.getSerie().getHasnifti()));
				serie.setProtocole(pdao.retrieveProtocol(rset.getInt(SQLSettings.TABLES.getSerie().getId_protocol())));
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
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+" where "+SQLSettings.TABLES.getSerie().getName()+"='"+name+"' and "+SQLSettings.TABLES.getSerie().getId_project()+"="+project_id+" and " +
					" "+SQLSettings.TABLES.getSerie().getId_patient()+"="+patient_id+" and "+SQLSettings.TABLES.getSerie().getId_acqdate()+"="+acqDate_id+" and "+SQLSettings.TABLES.getSerie().getId_protocol()+"="+protocol_id);
			}else{
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getSerie().getName()+"='"+name+"' and "+SQLSettings.TABLES.getSerie().getId_project()+"="+project_id+" and " +
						" "+SQLSettings.TABLES.getSerie().getId_patient()+"="+patient_id+" and "+SQLSettings.TABLES.getSerie().getId_acqdate()+"="+acqDate_id+" and "+SQLSettings.TABLES.getSerie().getId_protocol()+"="+protocol_id);
			}
			while(rset.next()){
				serie.setId(rset.getInt(SQLSettings.TABLES.getSerie().getId()));
				serie.setName(rset.getString(SQLSettings.TABLES.getSerie().getName()));
				serie.setHasNifti(rset.getInt(SQLSettings.TABLES.getSerie().getHasnifti()));
				serie.setProtocole(pdao.retrieveProtocol(rset.getInt(SQLSettings.TABLES.getSerie().getId_protocol())));
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
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getSerie().TNAME+" set "+SQLSettings.TABLES.getSerie().getName()+"='"+name+"',"+SQLSettings.TABLES.getSerie().getHasnifti()+"="+hasnifti+", "+SQLSettings.TABLES.getSerie().getId_project()+"="+id_project+", "+SQLSettings.TABLES.getSerie().getId_patient()+"="+id_patient+", "+SQLSettings.TABLES.getSerie().getId_acqdate()+"="+id_acqdate+", "+SQLSettings.TABLES.getSerie().getId_protocol()+"="+id_protocol+" where "+SQLSettings.TABLES.getSerie().getId()+"="+id);
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
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+" where "+SQLSettings.TABLES.getSerie().getId_patient()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getSerie().getId_patient()+"="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt(SQLSettings.TABLES.getSerie().getId()));	
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
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+" where "+SQLSettings.TABLES.getSerie().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getSerie().getId_project()+"="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt(SQLSettings.TABLES.getSerie().getId()));	
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
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+" where "+SQLSettings.TABLES.getSerie().getId_acqdate()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getSerie().getId_acqdate()+"="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt(SQLSettings.TABLES.getSerie().getId()));	
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
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+" where "+SQLSettings.TABLES.getSerie().getId_protocol()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getSerie().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getSerie().getId_protocol()+"="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Serie serie = retrieveSerie(rset.getInt(SQLSettings.TABLES.getSerie().getId()));	
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

	/**
	 * Supprime une entree serie via ses noms de serie, patient etc
	 */
	@Override
	public void removeSerie(String project, String patient, String acqdate,
			String protocol, String serie) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			/*
			 *  delete from dicomimage where dicomimage.id in (select id from (select dicomimage.id from dicomimage, serie, protocol, acquisitiondate, patient, project where dicomimage.id_project=project.id and dicomimage.id_serie=serie.id and dicomimage.id_protocol=protocol.id and dicomimage.id_acqdate=acquisitiondate.id and dicomimage.id_patient=patient.id and project.name='RECHERCHE_PHRC_PARKIMAGE_MENJOT_' and patient.name='PHANTOM_SWI_' and acquisitiondate.date=20121016 and protocol.name='SWI3D_TRA_1_5mm_3__ECHOS' and serie.name='SWI3D_TRA_1_5mm_3__ECHOS' and dicomimage.name='IM000010') as tmp)
			 */
			DBTables tab = SQLSettings.TABLES;
			SerieTable nt = tab.getSerie();
			// id
			String id = getSerieIdFor(project, patient, acqdate, protocol, serie);

			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.TNAME+"."+nt.getId()+"=" +id);
			
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			stmt.close();
			connection.close();
		}
	}
	
	@Override
	/**
	 * Permet de recuperer les id associés a une serie de noms de projets etc
	 * utilisable que par un admin
	 * @param project
	 * @param patient
	 * @param acqdate
	 * @param protocol
	 * @param serie
	 * @throws SQLException
	 */
	public String getSerieIdFor(String project, String patient, String acqdate,
			String protocol, String serie) throws SQLException {
		String idr = null; 
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			DBTables tab = SQLSettings.TABLES;
			SerieTable nt = tab.getSerie();
			rset = stmt.executeQuery("select "+nt.TNAME+"."+nt.getId()+" from "+nt.TNAME+", "+tab.getProtocol().TNAME+", "+tab.getAcquisitionDate().TNAME+", " +
					""+tab.getPatient().TNAME+", "+tab.getProject().TNAME+" where "+nt.TNAME+"."+nt.getId_project()+"="+tab.getProject().TNAME+"."+tab.getProject().getId()+" and "+
					""+nt.TNAME+"."+nt.getId_protocol()+"="+tab.getProtocol().TNAME+"."+tab.getProtocol().getId()+" and " +
					""+nt.TNAME+"."+nt.getId_acqdate()+"="+tab.getAcquisitionDate().TNAME+"."+tab.getAcquisitionDate().getId()+" and " +
					"" +nt.TNAME+"."+nt.getId_patient()+"="+tab.getPatient().TNAME+"."+tab.getPatient().getId()+" and " +
					""+tab.getProject().TNAME+"."+tab.getProject().getName()+"='"+project+"' and "
					+tab.getPatient().TNAME+"."+tab.getPatient().getName()+"='"+patient+"' and " +
					""+tab.getAcquisitionDate().TNAME+"."+tab.getAcquisitionDate().getDate()+"="+acqdate+" and " +
					""+tab.getProtocol().TNAME+"."+tab.getProtocol().getName()+"='"+protocol+"' and " +
					""+tab.getSerie().TNAME+"."+tab.getSerie().getName()+"='"+serie+"'");
			
			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				idr = rset.getString(1);	
			}
			return idr;
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
