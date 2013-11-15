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


import settings.SQLSettings;
import settings.UserProfile;
import settings.sql.DBTables;
import settings.sql.tables.DicomImageTable;
import settings.sql.tables.NiftiImageTable;



import model.NiftiImage;
import model.Patient;
import model.Project;


public class MySQLNiftiImageDAO implements NiftiImageDAO {
	public Collection<NiftiImage> retrieveAll() throws SQLException {
		Collection<NiftiImage> niftis = new ArrayList<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			SerieDAO sdao = new MySQLSerieDAO();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+"");
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+"_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = new NiftiImage();
				nifti.setId(rset.getInt(SQLSettings.TABLES.getNiftiImage().getId()));
				nifti.setName(rset.getString(SQLSettings.TABLES.getNiftiImage().getName()));
				nifti.setSlices(rset.getInt(SQLSettings.TABLES.getNiftiImage().getSlices()));
				nifti.setSerie(sdao.retrieveSerie(rset.getInt(SQLSettings.TABLES.getNiftiImage().getId_serie())));
				nifti.setProtocole(nifti.getSerie().getProtocole());
				// instantiation en cascade grace � acquisitiondate
				nifti.setAcquistionDate(nifti.getProtocole().getAcquisitionDate());
				nifti.setPatient(nifti.getAcquistionDate().getPatient());
				nifti.setProjet(nifti.getPatient().getProject());
				niftis.add(nifti);
			}
			return niftis;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

	
	

	public boolean newNiftiImage( String nom, int slices, int project_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			
			try {
				connection = SQLSettings.getPDS().getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into "+SQLSettings.TABLES.getNiftiImage().TNAME+" values (NULL,'"
						+ nom + "',"+slices+", "+project_id+","+patient_id+","+id_acqdate+", "+id_protocol+", "+id_serie+")");
				
				return true;
				
			}
			catch(Exception e){
				System.err.println("Warning : SQL -> " + e);
				throw e;
			}
			finally {
				try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
				try { if(connection!=null) connection.close();  } catch (Exception e) {};
				
			}	
	}
	

	public int idmax() throws SQLException{
		
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;		
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getNiftiImage().getId()+") from "+SQLSettings.TABLES.getNiftiImage().TNAME+" ;");
			else
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getNiftiImage().getId()+") from "+SQLSettings.TABLES.getNiftiImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" ;");
			
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
	

	public NiftiImage retrieveNiftiImage(int id) throws SQLException {
		// TODO Auto-generated method stub
		NiftiImage nifti = new NiftiImage();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			SerieDAO sdao = new MySQLSerieDAO();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+" where "+SQLSettings.TABLES.getNiftiImage().getId()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getNiftiImage().getId()+"="+id);
			
			while(rset.next()){
				nifti.setId(rset.getInt(SQLSettings.TABLES.getNiftiImage().getId()));
				nifti.setName(rset.getString(SQLSettings.TABLES.getNiftiImage().getName()));
				nifti.setSlices(rset.getInt(SQLSettings.TABLES.getNiftiImage().getSlices()));
				nifti.setSerie(sdao.retrieveSerie(rset.getInt(SQLSettings.TABLES.getNiftiImage().getId_serie())));
				nifti.setProtocole(nifti.getSerie().getProtocole());
				// instantiation en cascade grace � acquisitiondate
				nifti.setAcquistionDate(nifti.getProtocole().getAcquisitionDate());
				nifti.setPatient(nifti.getAcquistionDate().getPatient());
				nifti.setProjet(nifti.getPatient().getProject());
			}
		
			return nifti;
		
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
	public void removeEntry(String name,int slices, int id_project, int id_patient,
			int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("delete from "+SQLSettings.TABLES.getNiftiImage().TNAME+" where "+SQLSettings.TABLES.getNiftiImage().getName()+"='"+name+"' and " +
					""+SQLSettings.TABLES.getNiftiImage().getSlices()+"="+slices+" and "+SQLSettings.TABLES.getNiftiImage().getId_project()+"="+id_project+" and " +
							""+SQLSettings.TABLES.getNiftiImage().getId_patient()+"="+id_patient+" and "+SQLSettings.TABLES.getNiftiImage().getId_acqdate()+"="+id_acqdate+" and " +
									""+SQLSettings.TABLES.getNiftiImage().getId_protocol()+"="+id_protocol+" and "+SQLSettings.TABLES.getNiftiImage().getId_serie()+"="+id_serie);
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
	public boolean updateNiftiImage(int id, String name,int slices, int id_project, int id_patient, int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getNiftiImage().TNAME+" set "+SQLSettings.TABLES.getNiftiImage().getName()+"='"+name+"'," +
					""+SQLSettings.TABLES.getNiftiImage().getSlices()+"="+slices+" , "+SQLSettings.TABLES.getNiftiImage().getId_project()+"="+id_project+", " +
							""+SQLSettings.TABLES.getNiftiImage().getId_patient()+"="+id_patient+", "+SQLSettings.TABLES.getNiftiImage().getId_acqdate()+"="+id_acqdate+", " +
									""+SQLSettings.TABLES.getNiftiImage().getId_protocol()+"="+id_protocol+", "+SQLSettings.TABLES.getNiftiImage().getId_serie()+"="+id_serie+" " +
											"where "+SQLSettings.TABLES.getNiftiImage().getId()+"="+id);
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
	public Set<NiftiImage> getNiftiImageForPatient(int id)
			throws SQLException {
		Set<NiftiImage> niftis = new HashSet<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+" where "+SQLSettings.TABLES.getNiftiImage().getId_patient()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getNiftiImage().getId_patient()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt(SQLSettings.TABLES.getNiftiImage().getId()));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
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
	public Set<NiftiImage> getNiftiImageForProject(int id)
			throws SQLException {
		Set<NiftiImage> niftis = new HashSet<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+" where "+SQLSettings.TABLES.getNiftiImage().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getNiftiImage().getId_project()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt(SQLSettings.TABLES.getNiftiImage().getId()));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
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
	public Set<NiftiImage> getNiftiImageForAcqDate(int id)
			throws SQLException {
		Set<NiftiImage> niftis = new HashSet<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+" where "+SQLSettings.TABLES.getNiftiImage().getId_acqdate()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getNiftiImage().getId_acqdate()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt(SQLSettings.TABLES.getNiftiImage().getId()));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
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
	public Set<NiftiImage> getNiftiImageForProtocol(int id)
			throws SQLException {
		Set<NiftiImage> niftis = new HashSet<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+" where "+SQLSettings.TABLES.getNiftiImage().getId_protocol()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getNiftiImage().getId_protocol()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt(SQLSettings.TABLES.getNiftiImage().getId()));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
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
	public Set<NiftiImage> getNiftiImageForSerie(int id)
			throws SQLException {
		Set<NiftiImage> niftis = new HashSet<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();

			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+" where "+SQLSettings.TABLES.getNiftiImage().getId_serie()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getNiftiImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getNiftiImage().getId_serie()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt(SQLSettings.TABLES.getNiftiImage().getId()));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
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
	 * Supprime une entree nifti via ses noms de serie, patient etc
	 */
	@Override
	public void removeNifti(String project, String patient, String acqdate,
			String protocol, String serie, String image) throws SQLException {
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
			NiftiImageTable nt = tab.getNiftiImage();
			// id
			int id = getNiftiIdFor(project, patient, acqdate, protocol, serie, image);

			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.TNAME+"."+nt.getId()+" = " +id);
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	/**
	 * Supprime une entree nifti via ses nom de patient 
	 */
	@Override
	public void removeNiftisForPatient(String project, String patient) throws SQLException {
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
			NiftiImageTable nt = tab.getNiftiImage();
			// id
			PatientDAO pat = new MySQLPatientDAO();
			int id = pat.getPatientIdFor(project, patient);

			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.TNAME+"."+nt.getId_patient()+" = " +id);
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	/**
	 * Supprime une entree nifti via nom de projet
	 */
	@Override
	public void removeNiftisForProject(String project) throws SQLException {
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
			NiftiImageTable nt = tab.getNiftiImage();
			// id
			ProjectDAO p = new MySQLProjectDAO();
			Project pr = p.retrieveProject(project);
			

			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.TNAME+"."+nt.getId_project()+" = " +pr.getId());
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	/**
	 * Supprime une entree nifti via date
	 */
	@Override
	public void removeNiftisForAcqDate(String project,String patient, String acqdate) throws SQLException {
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
			NiftiImageTable nt = tab.getNiftiImage();
			// id
			AcquisitionDateDAO ac = new MySQLAcquisitionDateDAO();
			int id = ac.getAcqdateIdFor(project, patient, acqdate);

			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.TNAME+"."+nt.getId_acqdate()+" = " +id);
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	/**
	 * Supprime une entree nifti via protocol
	 */
	@Override
	public void removeNiftisForProtocol(String project,String patient, String acqdate, String protocol) throws SQLException {
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
			NiftiImageTable nt = tab.getNiftiImage();
			// id
			ProtocolDAO p = new MySQLProtocolDAO();
			int id = p.getProtocolIdFor(project, patient, acqdate, protocol);

			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.TNAME+"."+nt.getId_protocol()+" = " +id);
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	/**
	 * Supprime une entree nifti via Serie
	 */
	@Override
	public void removeNiftisForSerie(String project,String patient, String acqdate, String protocol, String serie) throws SQLException {
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
			NiftiImageTable nt = tab.getNiftiImage();
			// id
			SerieDAO sdao = new MySQLSerieDAO();
			int id = sdao.getSerieIdFor(project, patient, acqdate, protocol, serie);

			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.TNAME+"."+nt.getId_serie()+" = " +id);
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	
	
	/**
	 * Permet de recuperer les id associ�s a une serie de noms de projets etc
	 * utilisable que par un admin
	 * @param project
	 * @param patient
	 * @param acqdate
	 * @param protocol
	 * @param serie
	 * @param image
	 * @throws SQLException
	 */
	public int getNiftiIdFor(String project, String patient, String acqdate,
			String protocol, String serie, String image) throws SQLException {
		int idr = -1; 
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			DBTables tab = SQLSettings.TABLES;
			NiftiImageTable nt = tab.getNiftiImage();
			rset = stmt.executeQuery("select "+nt.TNAME+"."+nt.getId()+" from "+nt.TNAME+", " +
					""+tab.getSerie().TNAME+", "+tab.getProtocol().TNAME+", "+tab.getAcquisitionDate().TNAME+", " +
					""+tab.getPatient().TNAME+", "+tab.getProject().TNAME+" where "+nt.TNAME+"."+nt.getId_project()+"="+tab.getProject().TNAME+"."+tab.getProject().getId()+" and "+
					""+nt.TNAME+"."+nt.getId_serie()+"="+tab.getSerie().TNAME+"."+tab.getSerie().getId()+" and " +
					""+nt.TNAME+"."+nt.getId_protocol()+"="+tab.getProtocol().TNAME+"."+tab.getProtocol().getId()+" and " +
					""+nt.TNAME+"."+nt.getId_acqdate()+"="+tab.getAcquisitionDate().TNAME+"."+tab.getAcquisitionDate().getId()+" and " +
					"" +nt.TNAME+"."+nt.getId_patient()+"="+tab.getPatient().TNAME+"."+tab.getPatient().getId()+" and " +
					""+tab.getProject().TNAME+"."+tab.getProject().getName()+"='"+project+"' and "
					+tab.getPatient().TNAME+"."+tab.getPatient().getName()+"='"+patient+"' and " +
					""+tab.getAcquisitionDate().TNAME+"."+tab.getAcquisitionDate().getDate()+"="+acqdate+" and " +
					""+tab.getProtocol().TNAME+"."+tab.getProtocol().getName()+"='"+protocol+"' and " +
					""+tab.getSerie().TNAME+"."+tab.getSerie().getName()+"='"+serie+"' and " +
					""+nt.TNAME+"."+nt.getName()+"='"+image+"'");
			
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
	
	@Override
	public void removeEntry(String name, int id_project, int id_patient,
			int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("delete from "+SQLSettings.TABLES.getNiftiImage().TNAME+" where "+SQLSettings.TABLES.getNiftiImage().getName()+"='"+name+"' and " +
					""+SQLSettings.TABLES.getNiftiImage().getId_project()+"="+id_project+" and " +
							""+SQLSettings.TABLES.getNiftiImage().getId_patient()+"="+id_patient+" and "+SQLSettings.TABLES.getNiftiImage().getId_acqdate()+"="+id_acqdate+" and " +
									""+SQLSettings.TABLES.getNiftiImage().getId_protocol()+"="+id_protocol+" and "+SQLSettings.TABLES.getNiftiImage().getId_serie()+"="+id_serie);
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
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
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getNiftiImage().TNAME+" set "+SQLSettings.TABLES.getNiftiImage().getId_project()+"="+toproj.getId()+" where "+SQLSettings.TABLES.getNiftiImage().getId_patient()+"="+pat.getId()+" ;");
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
