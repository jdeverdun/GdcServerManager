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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select * from Project");

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				Project proj = new Project();
				proj.setId(rset.getInt("id"));
				proj.setNom(rset.getString("name"));	
				proj.setRemoteKey(rset.getString("rkey"));
				// ajout a la liste des grimpeurs
				projects.add(proj);
			}
			return projects;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	
	

	public boolean newProject(String nom) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			
			try {
				connection = SQLSettings.PDS.getConnection();
				stmt = connection.createStatement();
				
				
				List<Character> characters = new ArrayList<Character>();  
		        for(char c : nom.toCharArray()) {  
		            characters.add(c);  
		        }  
		        Collections.shuffle(characters,new Random(nom.length()-1));  
		        StringBuilder sb = new StringBuilder();  
		        for(char c : characters) {  
		            sb.append(c);  
		        }  
		        String rkey = sb.toString();  
		        
		        
				rset = stmt.execute("insert into Project values (NULL,'"
						+ nom + "','"+rkey+"')");
				
				return true;
				
			}
			catch(Exception e){
				System.err.println("Erreur de chargement du driver ");
				e.printStackTrace();
				return false;
			}
			finally {
				stmt.close();
				connection.close();
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			rset = stmt.executeQuery("select max(id) from Project ;");
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
		
			rset = stmt.executeQuery("select * from Project where id="+id);
			while(rset.next()){
				projectC.setNom(rset.getString("name"));
				projectC.setId(rset.getInt("id"));
				projectC.setRemoteKey(rset.getString("rkey"));
			}
		
			
			return projectC;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
		
			rset = stmt.executeQuery("select * from Project where name='"+name+"'");
			while(rset.next()){
				projectC.setNom(rset.getString("name"));
				projectC.setId(rset.getInt("id"));
				projectC.setRemoteKey(rset.getString("rkey"));
			}
		
			
			return projectC;
		
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
	public boolean updateProject(int id, String name, String rkey) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeUpdate("update Project set name='"+name+"', rkey='"+rkey+"' where id="+id);
			return true;
		} catch (SQLException e2) {
			e2.printStackTrace();
			return false;
		} finally {
			stmt.close();
			connection.close();
		}
	}



}