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
import settings.sql.tables.AcquisitionDateTable;
import settings.sql.tables.PatientTable;

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
			connection = SQLSettings.getPDS().getConnection();
			ProjectDAO projdao=new MySQLProjectDAO();	
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+"");
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Patient pat = new Patient();
				pat.setId(rset.getInt(SQLSettings.TABLES.getPatient().getId()));
				pat.setNom(rset.getString(SQLSettings.TABLES.getPatient().getName()));
				pat.setBirthdate(rset.getDate(SQLSettings.TABLES.getPatient().getBirthdate()));
				pat.setSex(rset.getString(SQLSettings.TABLES.getPatient().getSex()));
				pat.setSize(rset.getFloat(SQLSettings.TABLES.getPatient().getSize()));
				pat.setWeight(rset.getFloat(SQLSettings.TABLES.getPatient().getWeight()));
				pat.setProject(projdao.retrieveProject(rset.getInt(SQLSettings.TABLES.getPatient().getId_project())));
				patients.add(pat);
			}
			return patients;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newPatient( String nom,String birthdate, String sex,float size,float weight, int project_id) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			try {
				connection = SQLSettings.getPDS().getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into "+SQLSettings.TABLES.getPatient().TNAME+" values (NULL,'"
						+ nom + "','"+birthdate+"','"+sex+"',"+size+","+weight+", "+project_id+")");
				
				return true;
				
			}
			catch(Exception e){
				System.err.println("Erreur de chargement du driver " + e);	throw e;
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
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getPatient().getId()+") from "+SQLSettings.TABLES.getPatient().TNAME+" ;");
			else
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getPatient().getId()+") from "+SQLSettings.TABLES.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" ;");
			
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
	

	public Patient retrievePatient(int id) throws SQLException {
		Patient pat = new Patient();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			ProjectDAO projdao=new MySQLProjectDAO();		
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+" where "+SQLSettings.TABLES.getPatient().getId()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getPatient().getId()+"="+id);
			
			while(rset.next()){
				pat.setNom(rset.getString(SQLSettings.TABLES.getPatient().getName()));
				pat.setId(rset.getInt(SQLSettings.TABLES.getPatient().getId()));
				pat.setBirthdate(rset.getDate(SQLSettings.TABLES.getPatient().getBirthdate()));
				pat.setSex(rset.getString(SQLSettings.TABLES.getPatient().getSex()));
				pat.setSize(rset.getFloat(SQLSettings.TABLES.getPatient().getSize()));
				pat.setWeight(rset.getFloat(SQLSettings.TABLES.getPatient().getWeight()));
				pat.setProject(projdao.retrieveProject(rset.getInt(SQLSettings.TABLES.getPatient().getId_project())));
			}
		
			return pat;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}
	
	public Patient retrievePatient(String name, int id_project) throws SQLException {
		Patient pat = new Patient();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			ProjectDAO projdao=new MySQLProjectDAO();		
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+" where "+SQLSettings.TABLES.getPatient().getName()+"='"+name+"' and "+SQLSettings.TABLES.getPatient().getId_project()+"="+id_project);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getPatient().getName()+"='"+name+"' and "+SQLSettings.TABLES.getPatient().getId_project()+"="+id_project);
			
			while(rset.next()){
				pat.setNom(rset.getString(SQLSettings.TABLES.getPatient().getName()));
				pat.setId(rset.getInt(SQLSettings.TABLES.getPatient().getId()));
				pat.setBirthdate(rset.getDate(SQLSettings.TABLES.getPatient().getBirthdate()));
				pat.setSex(rset.getString(SQLSettings.TABLES.getPatient().getSex()));
				pat.setSize(rset.getFloat(SQLSettings.TABLES.getPatient().getSize()));
				pat.setWeight(rset.getFloat(SQLSettings.TABLES.getPatient().getWeight()));
				pat.setProject(projdao.retrieveProject(rset.getInt(SQLSettings.TABLES.getPatient().getId_project())));
			}
		
			return pat;
		
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
	public Patient retrievePatient(String name,String birthdate,String sex,float size,float weight, int project_id) throws SQLException {
		Patient pat = new Patient();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			ProjectDAO projdao=new MySQLProjectDAO();	
			
			if(UserProfile.CURRENT_USER.getLevel() == 3){
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+" where "+SQLSettings.TABLES.getPatient().getName()+"='"+name+"' and "+SQLSettings.TABLES.getPatient().getBirthdate()+"='"+birthdate+"' and "+SQLSettings.TABLES.getPatient().getSex()+"='"+sex+"' and "+SQLSettings.TABLES.getPatient().getId_project()+"="+project_id);
			}else{
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getPatient().getName()+"='"+name+"' and "+SQLSettings.TABLES.getPatient().getBirthdate()+"='"+birthdate+"' and "+SQLSettings.TABLES.getPatient().getSex()+"='"+sex+"' and "+SQLSettings.TABLES.getPatient().getId_project()+"="+project_id);
			}
			while(rset.next()){
				pat.setNom(rset.getString(SQLSettings.TABLES.getPatient().getName()));
				pat.setId(rset.getInt(SQLSettings.TABLES.getPatient().getId()));
				pat.setBirthdate(rset.getDate(SQLSettings.TABLES.getPatient().getBirthdate()));
				pat.setSex(rset.getString(SQLSettings.TABLES.getPatient().getSex()));
				pat.setSize(rset.getFloat(SQLSettings.TABLES.getPatient().getSize()));
				pat.setWeight(rset.getFloat(SQLSettings.TABLES.getPatient().getWeight()));
				pat.setProject(projdao.retrieveProject(rset.getInt(SQLSettings.TABLES.getPatient().getId_project())));
			}
		
			return pat;
		
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
	public boolean updatePatient(int id, String name, String birthdate, String sex,float size,float weight, int id_project) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getPatient().TNAME+" set "+SQLSettings.TABLES.getPatient().getName()+"='"+name+"',"+SQLSettings.TABLES.getPatient().getBirthdate()+"='"+birthdate+"'," +
					" "+SQLSettings.TABLES.getPatient().getSex()+"='"+sex+"', "+SQLSettings.TABLES.getPatient().getSize()+"="+size+", "+SQLSettings.TABLES.getPatient().getWeight()+"="+weight+", "+SQLSettings.TABLES.getPatient().getId_project()+"="+id_project+" where "+SQLSettings.TABLES.getPatient().getId()+"="+id);
			return true;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
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
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+" where "+SQLSettings.TABLES.getPatient().getId_project()+"="+project_id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getPatient().getId_project()+"="+project_id);

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Patient pat = retrievePatient(rset.getInt(SQLSettings.TABLES.getPatient().getId()));	
				if(pat!=null) 
					patients.add(pat);
			}
			return patients;
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
	 * Supprime une entree patient via ses noms de patient etc
	 */
	@Override
	public void removePatient(String project, String patient) throws SQLException {
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
			PatientTable nt = tab.getPatient();
			// id
			int id = getPatientIdFor(project, patient);

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
	 * Permet de recuperer les id associés a un patient de noms de projets etc
	 * utilisable que par un admin
	 * @param project
	 * @param patient
	 * @throws SQLException
	 */
	public int getPatientIdFor(String project, String patient) throws SQLException {
		int idr = -1; 
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			DBTables tab = SQLSettings.TABLES;
			PatientTable nt = tab.getPatient();
			rset = stmt.executeQuery("select "+nt.TNAME+"."+nt.getId()+" from "+nt.TNAME+", " +
					""+tab.getProject().TNAME+" where "+nt.TNAME+"."+nt.getId_project()+"="+tab.getProject().TNAME+"."+tab.getProject().getId()+" and "+
					""+tab.getProject().TNAME+"."+tab.getProject().getName()+"='"+project+"' and "
					+tab.getPatient().TNAME+"."+tab.getPatient().getName()+"='"+patient+"'");
			
			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				idr = rset.getInt(1);	
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
