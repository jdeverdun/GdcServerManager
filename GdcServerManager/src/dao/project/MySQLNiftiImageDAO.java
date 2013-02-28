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



import model.NiftiImage;


public class MySQLNiftiImageDAO implements NiftiImageDAO {
	public Collection<NiftiImage> retrieveAll() throws SQLException {
		Collection<NiftiImage> niftis = new ArrayList<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			SerieDAO sdao = new MySQLSerieDAO();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from NiftiImage");

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = new NiftiImage();
				nifti.setId(rset.getInt("id"));
				nifti.setName(rset.getString("name"));
				nifti.setSerie(sdao.retrieveSerie(rset.getInt("id_serie")));
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
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newNiftiImage( String nom, int project_id, int patient_id, int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			
			try {
				connection = SQLSettings.PDS.getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into NiftiImage values (NULL,'"
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
	
			rset = stmt.executeQuery("select max(id) from NiftiImage ;");
			if (rset != null) {
				while(rset.next()){
					//System.out.println("id max= "+rset.getInt(1));
					ident=rset.getInt(1);
				}
			}
			
			return ident;
		
		}catch(Exception e){
			e.printStackTrace();	return -1;
		}finally {
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}
	

	public NiftiImage retrieveNiftiImage(int id) throws SQLException {
		// TODO Auto-generated method stub
		NiftiImage nifti = new NiftiImage();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			SerieDAO sdao = new MySQLSerieDAO();
			rset = stmt.executeQuery("select * from NiftiImage where id="+id);
			while(rset.next()){
				nifti.setId(rset.getInt("id"));
				nifti.setName(rset.getString("name"));
				nifti.setSerie(sdao.retrieveSerie(rset.getInt("id_serie")));
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
			rset.close();
			stmt.close();
			connection.close();
		}
		
	}

	@Override
	public boolean updateNiftiImage(int id, String name, int id_project, int id_patient, int id_acqdate, int id_protocol, int id_serie) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update NiftiImage set name='"+name+"', id_project="+id_project+", id_patient="+id_patient+", id_acqdate="+id_acqdate+", id_protocol="+id_protocol+", id_serie="+id_serie+" where id="+id);
			return true;
		} catch (SQLException e2) {
			e2.printStackTrace();
			return false;
		} finally {
			stmt.close();
			connection.close();
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from NiftiImage where id_patient="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt("id"));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
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
	public Set<NiftiImage> getNiftiImageForProject(int id)
			throws SQLException {
		Set<NiftiImage> niftis = new HashSet<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from NiftiImage where id_project="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt("id"));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
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
	public Set<NiftiImage> getNiftiImageForAcqDate(int id)
			throws SQLException {
		Set<NiftiImage> niftis = new HashSet<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from NiftiImage where id_acqdate="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt("id"));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
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
	public Set<NiftiImage> getNiftiImageForProtocol(int id)
			throws SQLException {
		Set<NiftiImage> niftis = new HashSet<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from NiftiImage where id_protocol="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt("id"));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
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
	public Set<NiftiImage> getNiftiImageForSerie(int id)
			throws SQLException {
		Set<NiftiImage> niftis = new HashSet<NiftiImage>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from NiftiImage where id_serie="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				NiftiImage nifti = retrieveNiftiImage(rset.getInt("id"));	
				if(nifti!=null) 
					niftis.add(nifti);
			}
			return niftis;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}
}
