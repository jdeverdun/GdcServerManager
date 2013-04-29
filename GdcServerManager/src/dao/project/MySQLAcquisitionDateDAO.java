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

import model.AcquisitionDate;
import model.Patient;


public class MySQLAcquisitionDateDAO implements AcquisitionDateDAO {
	public Collection<AcquisitionDate> retrieveAll() throws SQLException {
		Collection<AcquisitionDate> acqDates = new ArrayList<AcquisitionDate>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
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
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newAcqDate(String nom, int project_id, int patient_id) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			try {
				connection = SQLSettings.PDS.getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" values (NULL,'"
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
			connection = SQLSettings.PDS.getConnection();
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
			rset.close();
			stmt.close();
			connection.close();
		}
	}
	

	public AcquisitionDate retrieveAcqDate(int id) throws SQLException {
		AcquisitionDate acq = new AcquisitionDate();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
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
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}
	
	@Override
	public AcquisitionDate retrieveAcqDate(String name, int project_id, int patient_id) throws SQLException {
		AcquisitionDate acq = new AcquisitionDate();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			PatientDAO pdao=new MySQLPatientDAO();		
			if(UserProfile.CURRENT_USER.getLevel() == 3){
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getDate()+"='"+name+"' " +
						"and "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+project_id+" and " +
								""+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+patient_id);
			}else{
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getDate()+"='"+name+"' " +
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" set "+SQLSettings.TABLES.getAcquisitionDate().getDate()+"='"+name+"'," +
					" "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id_project+", "+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+id_patient+" " +
							"where "+SQLSettings.TABLES.getAcquisitionDate().getId()+"="+id);
			return true;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_patient()+"="+id);

			// boucle sur les resultats de la requÃªte
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getAcquisitionDate().getId_project()+"="+id);

			// boucle sur les resultats de la requÃªte
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
			rset.close();
			stmt.close();
			connection.close();
		}
	}

}
