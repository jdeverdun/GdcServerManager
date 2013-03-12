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
				rset = stmt.executeQuery("select * from Protocol");
			else
				rset = stmt.executeQuery("select * from Protocol_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Protocol prot = new Protocol();
				prot.setId(rset.getInt("id"));
				prot.setName(rset.getString("name"));
				prot.setAcquisitionDate(adao.retrieveAcqDate(rset.getInt("id_acqdate")));
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
				
				rset = stmt.execute("insert into Protocol values (NULL,'"
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
     * R�cup�re le plus grand ID de la table AcqDate
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
				rset = stmt.executeQuery("select max(id) from Protocol ;");
			else
				rset = stmt.executeQuery("select max(id) from Protocol_"+UserProfile.CURRENT_USER.getId()+" ;");
			
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
				rset = stmt.executeQuery("select * from Protocol where id="+id);
			else
				rset = stmt.executeQuery("select * from Protocol_"+UserProfile.CURRENT_USER.getId()+" where id="+id);
			
			
			while(rset.next()){
				prot.setId(rset.getInt("id"));
				prot.setName(rset.getString("name"));
				prot.setAcquisitionDate(adao.retrieveAcqDate(rset.getInt("id_acqdate")));
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
				rset = stmt.executeQuery("select * from Protocol where name='"+name+"' and id_project="+project_id+" and " +
					" id_patient="+patient_id+" and id_acqdate="+acqDate_id);
			}else{
				rset = stmt.executeQuery("select * from Protocol_"+UserProfile.CURRENT_USER.getId()+" where name='"+name+"' and id_project="+project_id+" and " +
						" id_patient="+patient_id+" and id_acqdate="+acqDate_id);
			}
			while(rset.next()){
				prot.setId(rset.getInt("id"));
				prot.setName(rset.getString("name"));
				prot.setAcquisitionDate(adao.retrieveAcqDate(rset.getInt("id_acqdate")));
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
			rset = stmt.executeUpdate("update Protocol set name='"+name+"', id_project="+id_project+", id_patient="+id_patient+", id_acqdate="+id_acqdate+" where id="+id);
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
				rset = stmt.executeQuery("select * from Protocol where id_patient="+id);
			else
				rset = stmt.executeQuery("select * from Protocol_"+UserProfile.CURRENT_USER.getId()+" where id_patient="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Protocol prot = retrieveProtocol(rset.getInt("id"));	
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
				rset = stmt.executeQuery("select * from Protocol where id_project="+id);
			else
				rset = stmt.executeQuery("select * from Protocol_"+UserProfile.CURRENT_USER.getId()+" where id_project="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Protocol prot = retrieveProtocol(rset.getInt("id"));	
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
				rset = stmt.executeQuery("select * from Protocol where id_acqdate="+id);
			else
				rset = stmt.executeQuery("select * from Protocol_"+UserProfile.CURRENT_USER.getId()+" where id_acqdate="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Protocol prot = retrieveProtocol(rset.getInt("id"));	
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
}
