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


import model.DicomImage;


public class MySQLDicomImageDAO implements DicomImageDAO {
	public Collection<DicomImage> retrieveAll() throws SQLException {
		Collection<DicomImage> dicoms = new ArrayList<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			SerieDAO sdao = new MySQLSerieDAO();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+"");
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+"_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = new DicomImage();
				dicom.setId(rset.getInt(""+SQLSettings.TABLES.getDicomImage().getId()+""));
				dicom.setName(rset.getString(SQLSettings.TABLES.getDicomImage().getName()));
				dicom.setMri_name(rset.getString(SQLSettings.TABLES.getDicomImage().getMri_name()));
				dicom.setSerie(sdao.retrieveSerie(rset.getInt(SQLSettings.TABLES.getDicomImage().getId_serie())));
				dicom.setProtocole(dicom.getSerie().getProtocole());
				// instantiation en cascade grace � acquisitiondate
				dicom.setAcquistionDate(dicom.getProtocole().getAcquisitionDate());
				dicom.setPatient(dicom.getAcquistionDate().getPatient());
				dicom.setProjet(dicom.getPatient().getProject());
				dicoms.add(dicom);
			}
			return dicoms;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newDicomImage( String nom, String mri_name,  int project_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			try {
				connection = SQLSettings.PDS.getConnection();
				stmt = connection.createStatement();

				rset = stmt.execute("insert into "+SQLSettings.TABLES.getDicomImage().TNAME+" values (NULL,'"
						+ nom + "','" + mri_name + "',  "+project_id+","+patient_id+","+id_acqdate+", "+id_protocol+", "+id_serie+")");
				
				return true;
				
			}
			catch(Exception e){
				System.err.println("Warning : SQL -> " + e);
				throw e;
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
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getDicomImage().getId()+") from "+SQLSettings.TABLES.getDicomImage().TNAME+" ;");
			else
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getDicomImage().getId()+") from "+SQLSettings.TABLES.getDicomImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" ;");
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
	

	public DicomImage retrieveDicomImage(int id) throws SQLException {
		// TODO Auto-generated method stub
		DicomImage dicom = new DicomImage();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			SerieDAO sdao = new MySQLSerieDAO();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+" where "+SQLSettings.TABLES.getDicomImage().getId()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getDicomImage().getId()+"="+id);
			while(rset.next()){
				dicom.setId(rset.getInt(SQLSettings.TABLES.getDicomImage().getId()));
				dicom.setName(rset.getString(SQLSettings.TABLES.getDicomImage().getName()));
				dicom.setMri_name(rset.getString(SQLSettings.TABLES.getDicomImage().getMri_name()));
				dicom.setSerie(sdao.retrieveSerie(rset.getInt(SQLSettings.TABLES.getDicomImage().getId_serie())));
				dicom.setProtocole(dicom.getSerie().getProtocole());
				// instantiation en cascade grace � acquisitiondate
				dicom.setAcquistionDate(dicom.getProtocole().getAcquisitionDate());
				dicom.setPatient(dicom.getAcquistionDate().getPatient());
				dicom.setProjet(dicom.getPatient().getProject());
			}
		
			return dicom;
		
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
	public boolean updateDicomImage(int id, String name, String mri_name, int id_project, int id_patient, int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getDicomImage().TNAME+" set "+SQLSettings.TABLES.getDicomImage().getName()+"='"+name+"'," +
					""+SQLSettings.TABLES.getDicomImage().getMri_name()+"='"+mri_name+"', "+SQLSettings.TABLES.getDicomImage().getId_project()+"="+id_project+", " +
							""+SQLSettings.TABLES.getDicomImage().getId_patient()+"="+id_patient+", "+SQLSettings.TABLES.getDicomImage().getId_acqdate()+"="+id_acqdate+", " +
									""+SQLSettings.TABLES.getDicomImage().getId_protocol()+"="+id_protocol+", "+SQLSettings.TABLES.getDicomImage().getId_serie()+"="+id_serie+" where "+SQLSettings.TABLES.getDicomImage().getId()+"="+id);
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
	public Set<DicomImage> getDicomImageForPatient(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+" where "+SQLSettings.TABLES.getDicomImage().getId_patient()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getDicomImage().getId_patient()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt(SQLSettings.TABLES.getDicomImage().getId()));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
	public Set<DicomImage> getDicomImageForProject(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+" where "+SQLSettings.TABLES.getDicomImage().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getDicomImage().getId_project()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt(SQLSettings.TABLES.getDicomImage().getId()));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
	public Set<DicomImage> getDicomImageForAcqDate(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+" where "+SQLSettings.TABLES.getDicomImage().getId_acqdate()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getDicomImage().getId_acqdate()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt(SQLSettings.TABLES.getDicomImage().getId()));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
	public Set<DicomImage> getDicomImageForProtocol(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+" where "+SQLSettings.TABLES.getDicomImage().getId_protocol()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getDicomImage().getId_protocol()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt(SQLSettings.TABLES.getDicomImage().getId()));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
	public Set<DicomImage> getDicomImageForSerie(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+" where "+SQLSettings.TABLES.getDicomImage().getId_serie()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getDicomImage().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getDicomImage().getId_serie()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt(SQLSettings.TABLES.getDicomImage().getId()));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
