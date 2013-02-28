package dao;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import settings.SQLSettings;
import settings.UserProfile;

import model.User;

public class MySQLDataBaseAdminDAO implements DataBaseAdminDAO{

	public static final String projectTablesCreationFile = "ptablesCreation.sql";
	
	@Override
	public boolean createUser(User user) throws SQLException {
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} 
		catch (ClassNotFoundException e) {
			System.err.println("Erreur de chargement du driver " + e);
			return false;
		}
		
		try {
			String url = "jdbc:mysql://"+SQLSettings.ADDRESS+":3306/"+SQLSettings.DATABASE_NAME;
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
			stmt = connection.createStatement();
			String encryptedPass = null;		
	
			rset = stmt.executeQuery("select PASSWORD("+user.getPassword()+") ;");
			if (rset != null) {
				while(rset.next()){
					encryptedPass=rset.getString(1);
				}
				rset = stmt.executeQuery("create user '"+user.getLogin()+"'@'%' IDENTIFIED BY PASSWORD '"+encryptedPass+"') ;");
				String[] viewCommand = Scripts.getCreateUserViews(user);
				rset = stmt.executeQuery("GRANT SELECT ON * . * TO '"+user.getLogin()+"'@'%' IDENTIFIED BY '"+encryptedPass+"' WITH MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0	MAX_USER_CONNECTIONS 0 ;");
				return true;
			}
			
			return false;
		
		}catch(Exception e){
			System.err.println("Erreur de chargement du driver" + e);	return false;
		}finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	@Override
	public boolean exists(String databaseName) throws SQLException {
		// Connection connection = <your java.sql.Connection>
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} 
		catch (ClassNotFoundException e) {
			System.err.println("Erreur de chargement du driver " + e);
			return false;
		}
		Connection connection = null;
		ResultSet resultSet = null;
		try {
			String url = "jdbc:mysql://"+SQLSettings.ADDRESS+":3306/"+SQLSettings.DATABASE_NAME;
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
			resultSet = connection.getMetaData().getCatalogs();
	
			//iterate each catalog in the ResultSet
			while (resultSet.next()) {
			  // Get the database name, which is at position 1
			  String dbName = resultSet.getString(1);
			  if(dbName.equals(databaseName)){
				  return true;
			  }
			}
			return false;
		}catch(Exception e){
			System.err.println("Erreur de chargement du driver" + e);	return false;
		}finally{
			resultSet.close();
		}
	}

	@Override
	public boolean createProject(String databaseName) throws SQLException {
		Statement stmt = null;
		Connection connection = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} 
		catch (ClassNotFoundException e) {
			System.err.println("Erreur de chargement du driver " + e);
			return false;
		}
		
		try {
			String url = "jdbc:mysql://"+SQLSettings.ADDRESS+":3306/"+SQLSettings.DATABASE_NAME;
			connection = DriverManager.getConnection(url, "root", "jdeverdun");
			stmt = connection.createStatement();	
	
			stmt.executeUpdate("create database "+databaseName+" ;");

			return true;
		
		}catch(Exception e){
			System.err.println("Erreur de chargement du driver" + e);	return false;
		}finally {
			stmt.close();
			connection.close();
		}
	}

}
