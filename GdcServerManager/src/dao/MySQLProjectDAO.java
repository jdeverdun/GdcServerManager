package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import settings.SQLSettings;
import settings.UserProfile;
import settings.sql.DBTables;
import settings.sql.tables.PatientTable;
import settings.sql.tables.ProjectTable;

import dao.project.MySQLPatientDAO;
import dao.project.PatientDAO;

import model.Patient;
import model.Project;
import model.User;

/**
 * Classe faisant les requête sur la table Project
 * 
 *
 */
public class MySQLProjectDAO implements ProjectDAO {


	public Collection<Project> retrieveAll() throws SQLException {
		// la liste de grimpeurs
		Collection<Project> projects = new ArrayList<Project>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProject().TNAME);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Project proj = new Project();
				proj.setId(rset.getInt("id"));
				proj.setNom(rset.getString("name"));	
				// ajout a la liste des grimpeurs
				projects.add(proj);
			}
			return projects;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

	
	

	public boolean newProject(String nom) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			
			try {
				connection = SQLSettings.getPDS().getConnection();
				stmt = connection.createStatement();
				
		        
		        
				rset = stmt.execute("insert into "+SQLSettings.TABLES.getProject().TNAME+" values (NULL,'"
						+ nom + "')");
				
				return true;
				
			}
			catch(Exception e){
				System.err.println("Erreur de chargement du driver ");
				throw e;
			}
			finally {
				try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
				try { if(connection!=null) connection.close();  } catch (Exception e) {};
			}
		
	}
	
	/**
     * Récupère le plus grand ID de la table Project
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
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getProject().getId()+") from Project ;");
			else
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getProject().getId()+") from "+SQLSettings.TABLES.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" ;");
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
	
	/**
     * Récupère le projet ayant l'id "id"
     * @param id
     * @return
     * @throws SQLException
     */
	public Project retrieveProject(int id) throws SQLException {
		Project projectC = new Project();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
		
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProject().TNAME+" where "+SQLSettings.TABLES.getProject().getId()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getProject().getId()+"="+id);
			
			while(rset.next()){
				projectC.setNom(rset.getString(SQLSettings.TABLES.getProject().getName()));
				projectC.setId(rset.getInt(SQLSettings.TABLES.getProject().getId()));
			}
		
			
			return projectC;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
		
	}
	
	/**
	 * Recupere un projet par son nom
	 */
	@Override
	public Project retrieveProject(String name) throws SQLException {
		Project projectC = new Project();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
		
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProject().TNAME+" where "+SQLSettings.TABLES.getProject().getName()+"='"+name+"'");
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getProject().getName()+"='"+name+"'");
			
			while(rset.next()){
				projectC.setNom(rset.getString(SQLSettings.TABLES.getProject().getName()));
				projectC.setId(rset.getInt(SQLSettings.TABLES.getProject().getId()));
			}
		
			
			return projectC;
		
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
	public boolean updateProject(int id, String name) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update "+SQLSettings.TABLES.getProject().TNAME+" set "+SQLSettings.TABLES.getProject().getName()+"='"+name+"' where " +
					""+SQLSettings.TABLES.getProject().getId()+"="+id);
			return true;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

	/**
	 * Supprime une entree project via ses noms 
	 */
	@Override
	public void removeProject(String project) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			DBTables tab = SQLSettings.TABLES;
			ProjectTable nt = tab.getProject();

			// on recupere l'id du projet
			Project temp = retrieveProject(project);
			
			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.TNAME+"."+nt.getName()+"='" +project+"'");
			UserProjectDAO udao = new MySQLUserProjectDAO();
			udao.removeProject(temp.getId());
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			throw e2;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	

}