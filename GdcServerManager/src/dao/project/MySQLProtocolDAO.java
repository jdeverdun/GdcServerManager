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
import settings.sql.tables.ProtocolTable;
import settings.sql.tables.SerieTable;

import model.AcquisitionDate;
import model.Protocol;

public class MySQLProtocolDAO implements ProtocolDAO{
	public Collection<Protocol> retrieveAll() throws SQLException {
		Collection<Protocol> protocols = new ArrayList<Protocol>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			AcquisitionDateDAO adao = new MySQLAcquisitionDateDAO();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+"");
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Protocol prot = new Protocol();
				prot.setId(rset.getInt(SQLSettings.TABLES.getProtocol().getId()));
				prot.setName(rset.getString(SQLSettings.TABLES.getProtocol().getName()));
				prot.setAcquisitionDate(adao.retrieveAcqDate(rset.getInt(SQLSettings.TABLES.getProtocol().getId_acqdate())));
				prot.setPatient(prot.getAcquisitionDate().getPatient());
				prot.setProjet(prot.getPatient().getProject());
				protocols.add(prot);
			}
			return protocols;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
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
				connection = SQLSettings.PDS.getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into "+SQLSettings.TABLES.getProtocol().TNAME+" values (NULL,'"
						+ nom + "', "+project_id+","+patient_id+","+id_acqdate+")");
				
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getProtocol().getId()+") from "+SQLSettings.TABLES.getProtocol().TNAME+" ;");
			else
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getProtocol().getId()+") from "+SQLSettings.TABLES.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" ;");
			
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
	

	public Protocol retrieveProtocol(int id) throws SQLException {
		Protocol prot = new Protocol();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			AcquisitionDateDAO adao = new MySQLAcquisitionDateDAO();	
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+" where "+SQLSettings.TABLES.getProtocol().getId()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getProtocol().getId()+"="+id);
			
			
			while(rset.next()){
				prot.setId(rset.getInt(SQLSettings.TABLES.getProtocol().getId()));
				prot.setName(rset.getString(SQLSettings.TABLES.getProtocol().getName()));
				prot.setAcquisitionDate(adao.retrieveAcqDate(rset.getInt(SQLSettings.TABLES.getProtocol().getId_acqdate())));
				prot.setPatient(prot.getAcquisitionDate().getPatient());
				prot.setProjet(prot.getPatient().getProject());
			}
		
			return prot;
		
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
	public Protocol retrieveProtocol(String name, int project_id,
			int patient_id, int acqDate_id) throws SQLException {
		Protocol prot = new Protocol();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			AcquisitionDateDAO adao = new MySQLAcquisitionDateDAO();	
			
			if(UserProfile.CURRENT_USER.getLevel() == 3){
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+" where "+SQLSettings.TABLES.getProtocol().getName()+"='"+name+"' and "+SQLSettings.TABLES.getProtocol().getId_project()+"="+project_id+" and " +
					" "+SQLSettings.TABLES.getProtocol().getId_patient()+"="+patient_id+" and "+SQLSettings.TABLES.getProtocol().getId_acqdate()+"="+acqDate_id);
			}else{
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getProtocol().getName()+"='"+name+"' and "+SQLSettings.TABLES.getProtocol().getId_project()+"="+project_id+" and " +
						" "+SQLSettings.TABLES.getProtocol().getId_patient()+"="+patient_id+" and "+SQLSettings.TABLES.getProtocol().getId_acqdate()+"="+acqDate_id);
			}
			while(rset.next()){
				prot.setId(rset.getInt(SQLSettings.TABLES.getProtocol().getId()));
				prot.setName(rset.getString(SQLSettings.TABLES.getProtocol().getName()));
				prot.setAcquisitionDate(adao.retrieveAcqDate(rset.getInt(SQLSettings.TABLES.getProtocol().getId_acqdate())));
				prot.setPatient(prot.getAcquisitionDate().getPatient());
				prot.setProjet(prot.getPatient().getProject());
			}
		
			return prot;
		
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
	public boolean updateProtocol(int id, String name, int id_project, int id_patient, int id_acqdate) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getProtocol().TNAME+" set "+SQLSettings.TABLES.getProtocol().getName()+"='"+name+"', "+SQLSettings.TABLES.getProtocol().getId_project()+"="+id_project+", "+SQLSettings.TABLES.getProtocol().getId_patient()+"="+id_patient+", "+SQLSettings.TABLES.getProtocol().getId_acqdate()+"="+id_acqdate+" where "+SQLSettings.TABLES.getProtocol().getId()+"="+id);
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
	public Set<Protocol> getProtocolForPatient(int id)
			throws SQLException {
		Set<Protocol> prots = new HashSet<Protocol>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+" where "+SQLSettings.TABLES.getProtocol().getId_patient()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getProtocol().getId_patient()+"="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Protocol prot = retrieveProtocol(rset.getInt(SQLSettings.TABLES.getProtocol().getId()));	
				if(prot!=null) 
					prots.add(prot);
			}
			return prots;
		} catch (Exception e) {
			e.printStackTrace();
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+" where "+SQLSettings.TABLES.getProtocol().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getProtocol().getId_project()+"="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Protocol prot = retrieveProtocol(rset.getInt(SQLSettings.TABLES.getProtocol().getId()));	
				if(prot!=null) 
					prots.add(prot);
			}
			return prots;
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
	public Set<Protocol> getProtocolForAcqDate(int id)
			throws SQLException {
		Set<Protocol> prots = new HashSet<Protocol>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+" where "+SQLSettings.TABLES.getProtocol().getId_acqdate()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getProtocol().getId_acqdate()+"="+id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Protocol prot = retrieveProtocol(rset.getInt(SQLSettings.TABLES.getProtocol().getId()));	
				if(prot!=null) 
					prots.add(prot);
			}
			return prots;
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
	 * Supprime une entree protocol via ses noms de patient etc
	 */
	@Override
	public void removeProtocol(String project, String patient, String acqdate,
			String protocol) throws SQLException {
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
			ProtocolTable nt = tab.getProtocol();
			// id
			String id = getProtocolIdFor(project, patient, acqdate, protocol);

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
	 * Permet de recuperer les id associés a une protocol de noms de projets etc
	 * utilisable que par un admin
	 * @param project
	 * @param patient
	 * @param acqdate
	 * @param protocol
	 * @throws SQLException
	 */
	public String getProtocolIdFor(String project, String patient, String acqdate,
			String protocol) throws SQLException {
		String idr = null; 
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			DBTables tab = SQLSettings.TABLES;
			ProtocolTable nt = tab.getProtocol();
			rset = stmt.executeQuery("select "+nt.TNAME+"."+nt.getId()+" from "+nt.TNAME+", "+
					""+tab.getPatient().TNAME+", "+tab.getProject().TNAME+", "+tab.getAcquisitionDate().TNAME+" where "+nt.TNAME+"."+nt.getId_project()+"="+tab.getProject().TNAME+"."+tab.getProject().getId()+" and "+
					""+nt.TNAME+"."+nt.getId_acqdate()+"="+tab.getAcquisitionDate().TNAME+"."+tab.getAcquisitionDate().getId()+" and " +
					"" +nt.TNAME+"."+nt.getId_patient()+"="+tab.getPatient().TNAME+"."+tab.getPatient().getId()+" and " +
					""+tab.getProject().TNAME+"."+tab.getProject().getName()+"='"+project+"' and "
					+tab.getPatient().TNAME+"."+tab.getPatient().getName()+"='"+patient+"' and " +
					""+tab.getAcquisitionDate().TNAME+"."+tab.getAcquisitionDate().getDate()+"="+acqdate+" and " +
					""+tab.getProtocol().TNAME+"."+tab.getProtocol().getName()+"='"+protocol+"'");
			
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
