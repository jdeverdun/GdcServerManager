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
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			//PatientDAO patdao=new MySQLPatientDAO();
			ProjectDAO pdao=new MySQLProjectDAO();
			rset = stmt.executeQuery("select * from User_Project where id_user="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Project proj = pdao.retrieveProject(rset.getInt("id_project"));
				if(proj!=null)
					projects.add(proj);
			}
			return projects;
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
	public Set<User> getUsersForProject(int id) throws SQLException {
		Set<User> users = new HashSet<User>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			//PatientDAO patdao=new MySQLPatientDAO();
			UserDAO udao=new MySQLUserDAO();
			rset = stmt.executeQuery("select * from User_Project where id_project="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				User us = udao.retrieveUser(rset.getInt("id_user"));
					// On limite l'instantiation (usage memoire)
					//us.setProjects(updao.getProjectsForUser(us.getId()));
					
				if(us!=null) 
					users.add(us);
			}
			return users;
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
