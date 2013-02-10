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

import model.Project;
import model.User;

public class MySQLUserProjectDAO implements UserProjectDAO {

	@Override
	public Set<Project> getProjectsForUser(int id) throws SQLException {
		Set<Project> projects = new HashSet<Project>();
		ResultSet rset = null;
		ResultSet rset2 = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Erreur de chargement du driver " + e);
			return null;
		}
		try {
			String url = "jdbc:mysql://localhost:3306/jdeverdun";
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
			stmt = connection.createStatement();
			PatientDAO patdao=new MySQLPatientDAO();
			UserProjectDAO updao=new MySQLUserProjectDAO();
			rset = stmt.executeQuery("select * from User_Project where id_user="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				rset2 = stmt.executeQuery("select * from Project where id="+rset.getInt("id_project"));
				while (rset2.next()) {
					Project proj = new Project();
					proj.setId(rset.getInt("id"));
					proj.setNom(rset.getString("nom"));
					proj.setPatients(patdao.getPatientsForProject(proj.getId()));
					proj.setUsers(updao.getUsersForProject(proj.getId()));
	
					projects.add(proj);
				}
			}
			return projects;
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
	public Set<User> getUsersForProject(int id) throws SQLException {
		Set<User> users = new HashSet<User>();
		ResultSet rset = null;
		ResultSet rset2 = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Erreur de chargement du driver " + e);
			return null;
		}
		try {
			String url = "jdbc:mysql://localhost:3306/jdeverdun";
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
			stmt = connection.createStatement();
			PatientDAO patdao=new MySQLPatientDAO();
			UserProjectDAO updao=new MySQLUserProjectDAO();
			rset = stmt.executeQuery("select * from User_Project where id_project="+id);

			// boucle sur les resultats de la requête
			while (rset.next()) {
				rset2 = stmt.executeQuery("select * from User where id="+rset.getInt("id_user"));
				while(rset2.next()){
					User us = new User();
					us.setId(rset.getInt("id"));
					us.setNom(rset.getString("nom"));
					us.setProjects(updao.getProjectsForUser(us.getId()));
	
					users.add(us);
				}
			}
			return users;
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
