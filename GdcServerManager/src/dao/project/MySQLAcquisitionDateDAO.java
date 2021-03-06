package dao.project;

import java.sql.Connection;
import java.sql.Date;
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
import settings.sql.tables.AcquisitionDateTable;
import settings.sql.tables.ProtocolTable;

import model.AcquisitionDate;
import model.Patient;
import model.Project;


public class MySQLAcquisitionDateDAO implements AcquisitionDateDAO {
	public Collection<AcquisitionDate> retrieveAll() throws SQLException {
		Collection<AcquisitionDate> acqDates = new ArrayList<AcquisitionDate>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			PatientDAO pdao = new MySQLPatientDAO();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"");
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requete
			while (rset.next()) {
				AcquisitionDate acq = new AcquisitionDate();
				acq.setId(rset.getInt(""+SQLSettings.TABLES.getAcquisitionDate().getId()+""));
				acq.setDate(rset.getString(""+SQLSettings.TABLES.getAcquisitionDate().getDate()+""));
				acq.setPatient(pdao.retrievePatient(rset.getInt(""+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"")));
				acq.setProjet(acq.getPatient().getProject());
				acqDates.add(acq);
			}
			return acqDates;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

	
	

	public boolean newAcqDate(String date, int project_id, int patient_id) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			try {
				connection = SQLSettings.getPDS().getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" values (NULL,'"
						+ date + "', "+project_id+","+patient_id+")");
				
				return true;
				
			}
			catch(Exception e){
				System.err.println("Erreur de chargement du driver " + e);	return false;
			}
			finally {
				try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
				try { if(connection!=null) connection.close();  } catch (Exception e) {};
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
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getAcquisitionDate().getId()+") from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" ;");
			else
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getAcquisitionDate().getId()+") from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" ;");
			
			if (rset != null) {
				while(rset.next()){
					//System.out.println("id max= "+rset.getInt(1));
					ident=rset.getInt(1);
				}
			}
			
			return ident;
		
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	

	public AcquisitionDate retrieveAcqDate(int id) throws SQLException {
		AcquisitionDate acq = new AcquisitionDate();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			PatientDAO pdao=new MySQLPatientDAO();		
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getId()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getId()+"="+id);
			
			while(rset.next()){
				acq.setId(rset.getInt(""+SQLSettings.TABLES.getAcquisitionDate().getId()+""));
				acq.setDate(rset.getString(""+SQLSettings.TABLES.getAcquisitionDate().getDate()+""));
				acq.setPatient(pdao.retrievePatient(rset.getInt(""+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"")));
				acq.setProjet(acq.getPatient().getProject());
			}
		
			return acq;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
		
	}
	
	@Override
	public AcquisitionDate retrieveAcqDate(String date, int project_id, int patient_id) throws SQLException {
		AcquisitionDate acq = new AcquisitionDate();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			PatientDAO pdao=new MySQLPatientDAO();		
			if(UserProfile.CURRENT_USER.getLevel() == 3){
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getDate()+"='"+date+"' " +
						"and "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+project_id+" and " +
								""+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+patient_id);
			}else{
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getDate()+"='"+date+"' " +
						"and "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+project_id+" and " +
								""+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+patient_id);
			}
			while(rset.next()){
				acq.setId(rset.getInt(""+SQLSettings.TABLES.getAcquisitionDate().getId()+""));
				acq.setDate(rset.getString(""+SQLSettings.TABLES.getAcquisitionDate().getDate()+""));
				acq.setPatient(pdao.retrievePatient(rset.getInt(""+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"")));
				acq.setProjet(acq.getPatient().getProject());
			}
		
			return acq;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

	@Override
	public boolean updateAcqDate(int id, String date, int id_project, int id_patient) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" set "+SQLSettings.TABLES.getAcquisitionDate().getDate()+"='"+date+"'," +
					" "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id_project+", "+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+id_patient+" " +
							"where "+SQLSettings.TABLES.getAcquisitionDate().getId()+"="+id);
			return true;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
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
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				AcquisitionDate acq = retrieveAcqDate(rset.getInt(""+SQLSettings.TABLES.getAcquisitionDate().getId()+""));	
				if(acq!=null) 
					acqs.add(acq);
			}
			return acqs;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
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
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				AcquisitionDate acq = retrieveAcqDate(rset.getInt(SQLSettings.TABLES.getAcquisitionDate().getId()));	
				if(acq!=null) 
					acqs.add(acq);
			}
			return acqs;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	@Override
	public int getAcqDateCountForProject(int id)
			throws SQLException {
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select count("+SQLSettings.TABLES.getAcquisitionDate().TNAME+"."+SQLSettings.TABLES.getAcquisitionDate().getId()+") from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select count("+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+"."+SQLSettings.TABLES.getAcquisitionDate().getId()+") from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);

			if(rset.next())
				return rset.getInt(1);
			else
				return -1;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	@Override
	public Date getAcqDateMinForProject(int id)
			throws SQLException {
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select min("+SQLSettings.TABLES.getAcquisitionDate().TNAME+"."+SQLSettings.TABLES.getAcquisitionDate().getDate()+") from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select min("+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+"."+SQLSettings.TABLES.getAcquisitionDate().getDate()+") from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);

			if(rset.next())
				return rset.getDate(1);
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	@Override
	public Date getAcqDateMaxForProject(int id)
			throws SQLException {
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getAcquisitionDate().TNAME+"."+SQLSettings.TABLES.getAcquisitionDate().getDate()+") from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+"."+SQLSettings.TABLES.getAcquisitionDate().getDate()+") from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);

			if(rset.next())
				return rset.getDate(1);
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

	/**
	 * Supprime une entree acqdate via ses noms de patient etc
	 */
	@Override
	public void removeAcqdate(String project, String patient, String acqdate) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			/*
			 *  delete from dicomimage where dicomimage.id in (select id from (select dicomimage.id from dicomimage, serie, protocol, acquisitiondate, patient, project where dicomimage.id_project=project.id and dicomimage.id_serie=serie.id and dicomimage.id_protocol=protocol.id and dicomimage.id_acqdate=acquisitiondate.id and dicomimage.id_patient=patient.id and project.name='RECHERCHE_PHRC_PARKIMAGE_MENJOT_' and patient.name='PHANTOM_SWI_' and acquisitiondate.date=20121016 and protocol.name='SWI3D_TRA_1_5mm_3__ECHOS' and serie.name='SWI3D_TRA_1_5mm_3__ECHOS' and dicomimage.name='IM000010') as tmp)
			 */
			DBTables tab = SQLSettings.TABLES;
			AcquisitionDateTable nt = tab.getAcquisitionDate();
			// id
			int id = getAcqdateIdFor(project, patient, acqdate);

			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.TNAME+"."+nt.getId()+"=" +id);
			
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	@Override
	/**
	 * Permet de recuperer les id associ�s a une acqdate de noms de projets etc
	 * utilisable que par un admin
	 * @param project
	 * @param patient
	 * @param acqdate
	 * @throws SQLException
	 */
	public int getAcqdateIdFor(String project, String patient, String acqdate) throws SQLException {
		int idr = -1; 
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			DBTables tab = SQLSettings.TABLES;
			AcquisitionDateTable nt = tab.getAcquisitionDate();
			rset = stmt.executeQuery("select "+nt.TNAME+"."+nt.getId()+" from "+nt.TNAME+", " +
					""+tab.getPatient().TNAME+", "+tab.getProject().TNAME+" where "+nt.TNAME+"."+nt.getId_project()+"="+tab.getProject().TNAME+"."+tab.getProject().getId()+" and "+
					"" +nt.TNAME+"."+nt.getId_patient()+"="+tab.getPatient().TNAME+"."+tab.getPatient().getId()+" and " +
					""+tab.getProject().TNAME+"."+tab.getProject().getName()+"='"+project+"' and "
					+tab.getPatient().TNAME+"."+tab.getPatient().getName()+"='"+patient+"' and " +
					""+tab.getAcquisitionDate().TNAME+"."+tab.getAcquisitionDate().getDate()+"="+acqdate+"");
			
			// boucle sur les resultats de la requête
			while (rset.next()) {
				idr = rset.getInt(1);	
			}
			return idr;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	public boolean changeProject(Patient pat, Project toproj) throws SQLException {
		int rset = -1;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" set "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+toproj.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+pat.getId()+" ;");
			ProtocolDAO pdao = new MySQLProtocolDAO();
			pdao.changeProject(pat,toproj);
			return true;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
}
