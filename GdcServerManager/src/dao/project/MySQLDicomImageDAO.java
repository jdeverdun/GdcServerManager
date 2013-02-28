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
			rset = stmt.executeQuery("select * from DicomImage");

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = new DicomImage();
				dicom.setId(rset.getInt("id"));
				dicom.setName(rset.getString("name"));
				dicom.setSerie(sdao.retrieveSerie(rset.getInt("id_serie")));
				dicom.setProtocole(dicom.getSerie().getProtocole());
				// instantiation en cascade grace � acquisitiondate
				dicom.setAcquistionDate(dicom.getProtocole().getAcquisitionDate());
				dicom.setPatient(dicom.getAcquistionDate().getPatient());
				dicom.setProjet(dicom.getPatient().getProject());
				dicoms.add(dicom);
			}
			return dicoms;
		} catch (Exception e) {
			System.err.println("Erreur SQL " + e);
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newDicomImage( String nom, int project_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			try {
				connection = SQLSettings.PDS.getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into DicomImage values ('"
						+ nom + "', "+project_id+","+patient_id+","+id_acqdate+", "+id_protocol+", "+id_serie+")");
				
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			rset = stmt.executeQuery("select max(id) from DicomImage ;");
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
			rset = stmt.executeQuery("select * from DicomImage where id="+id);
			while(rset.next()){
				dicom.setId(rset.getInt("id"));
				dicom.setName(rset.getString("name"));
				dicom.setSerie(sdao.retrieveSerie(rset.getInt("id_serie")));
				dicom.setProtocole(dicom.getSerie().getProtocole());
				// instantiation en cascade grace � acquisitiondate
				dicom.setAcquistionDate(dicom.getProtocole().getAcquisitionDate());
				dicom.setPatient(dicom.getAcquistionDate().getPatient());
				dicom.setProjet(dicom.getPatient().getProject());
			}
		
			return dicom;
		
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
	public boolean updateDicomImage(int id, String name, int id_project, int id_patient, int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update DicomImage set name='"+name+"', id_project="+id_project+", id_patient="+id_patient+", id_acqdate="+id_acqdate+", id_protocol="+id_protocol+", id_serie="+id_serie+" where id="+id);
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
	public Set<DicomImage> getDicomImageForPatient(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from DicomImage where id_patient="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt("id"));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
	public Set<DicomImage> getDicomImageForProject(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from DicomImage where id_project="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt("id"));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
	public Set<DicomImage> getDicomImageForAcqDate(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from DicomImage where id_acqdate="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt("id"));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
	public Set<DicomImage> getDicomImageForProtocol(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from DicomImage where id_protocol="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt("id"));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
	public Set<DicomImage> getDicomImageForSerie(int id)
			throws SQLException {
		Set<DicomImage> dicoms = new HashSet<DicomImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from DicomImage where id_serie="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				DicomImage dicom = retrieveDicomImage(rset.getInt("id"));	
				if(dicom!=null) 
					dicoms.add(dicom);
			}
			return dicoms;
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
