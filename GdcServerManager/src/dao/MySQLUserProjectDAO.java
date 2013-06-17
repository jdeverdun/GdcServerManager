package dao;

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


import model.Project;
import model.User;

public class MySQLUserProjectDAO implements UserProjectDAO {

	@Override
	public Set<Project> getProjectsForUser(int id) throws SQLException {
		Set<Project> projects = new HashSet<Project>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			//PatientDAO patdao=new MySQLPatientDAO();
			ProjectDAO pdao=new MySQLProjectDAO();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getProject().TNAME);
			else
				rset = stmt.executeQuery("select * from  "+SQLSettings.TABLES.getUser_project().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getUser_project().getId_user()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Project proj;
				if(UserProfile.CURRENT_USER.getLevel() == 3)
					proj = pdao.retrieveProject(rset.getInt(""+SQLSettings.TABLES.getProject().getId()+""));
				else
					proj = pdao.retrieveProject(rset.getInt(""+SQLSettings.TABLES.getUser_project().getId_project()+""));
				if(proj!=null)
					projects.add(proj);
			}
			return projects;
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
	public Set<User> getUsersForProject(int id) throws SQLException {
		Set<User> users = new HashSet<User>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			//PatientDAO patdao=new MySQLPatientDAO();
			UserDAO udao=new MySQLUserDAO();
			
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getUser_project().TNAME+" where "+SQLSettings.TABLES.getUser_project().getId_project()+"="+id);
			else
				rset = stmt.executeQuery("select * from  "+SQLSettings.TABLES.getUser_project().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getUser_project().getId_project()+"="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				User us = udao.retrieveUser(rset.getInt(""+SQLSettings.TABLES.getUser_project().getId_user()+""));
					// On limite l'instantiation (usage memoire)
					//us.setProjects(updao.getProjectsForUser(us.getId()));
					
				if(us!=null) 
					users.add(us);
			}
			return users;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}
	
	public boolean removeUser(User u) throws SQLException{
		// la liste de grimpeurs
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			rset = stmt.executeUpdate("delete from "+SQLSettings.TABLES.getUser_project().TNAME+" where "+SQLSettings.TABLES.getUser_project().getId_user()+"="+u.getId());

			return true;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			throw e2;
		} finally {
			stmt.close();
			connection.close();
		}
	}
	
	public int removeLink(int id_user, int id_project) throws SQLException{
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			rset = stmt.executeUpdate("delete from "+SQLSettings.TABLES.getUser_project().TNAME+" where "+SQLSettings.TABLES.getUser_project().getId_user()+"="+id_user+" and "+SQLSettings.TABLES.getUser_project().getId_project()+"="+id_project);

			return 0;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			throw e2;
		} finally {
			stmt.close();
			connection.close();
		}
	}
	public int removeLink(String login, String projectname) throws SQLException{
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			// On recupere les id user // projet
			UserDAO udao = new MySQLUserDAO();
			ProjectDAO pdao = new MySQLProjectDAO();
			User u = udao.retrieveUser(login);
			Project p = pdao.retrieveProject(projectname);
			return removeLink(u.getId(),p.getId());
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			throw e2;
		}
	}
	
	public boolean exists(int user_id, int project_id) throws SQLException{
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
		
			if(UserProfile.CURRENT_USER.getLevel() == 3){
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getUser_project().TNAME+" where "+SQLSettings.TABLES.getUser_project().getId_user()+"="+user_id+" and " +
						""+SQLSettings.TABLES.getUser_project().getId_project()+"="+project_id);
			}else{
				rset = stmt.executeQuery("select * from  "+SQLSettings.TABLES.getUser_project().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getUser_project().getId_user()+"="+user_id+" " +
						"and "+SQLSettings.TABLES.getUser_project().getId_project()+"="+project_id);
			}
			return rset.next();		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}
	
	public int addLink(String login, String projectname) throws SQLException {
		
		boolean rset = false;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			// On recupere les id user // projet
			UserDAO udao = new MySQLUserDAO();
			ProjectDAO pdao = new MySQLProjectDAO();
			User u = udao.retrieveUser(login);
			Project p = pdao.retrieveProject(projectname);
			
			// Check si le tuple existe deja
			boolean tupleExists = exists(u.getId(), p.getId());
			if(!tupleExists){
				rset = stmt.execute("insert into "+SQLSettings.TABLES.getUser_project().TNAME+" values ("
						+ u.getId() + " ," + p.getId() + ")");
				return 0;
			}else{
				return 1;
			}		
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		finally {
			stmt.close();
			connection.close();
		}
	
	}

	@Override
	public boolean removeProject(int idproject) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			rset = stmt.executeUpdate("delete from "+SQLSettings.TABLES.getUser_project().TNAME+" where "+SQLSettings.TABLES.getUser_project().getId_project()+"="+idproject);

			return true;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			throw e2;
		} finally {
			stmt.close();
			connection.close();
		}
	}

}
