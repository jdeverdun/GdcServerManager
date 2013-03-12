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

public class MySQLUserViewDAO implements UserViewDAO {

	
	public int addUserView( String login, int view) throws SQLException {
		
		boolean rset = false;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			rset = stmt.execute("insert into User_View values (NULL,'"
					+ login + "' ," + view + ")");
			
			return 0;
			
		}
		catch(Exception e){
			if(e.toString().contains("UNIQUE"))
				return 1;
			else{
				System.err.println("Erreur SQL " + e);
				return 2;
			}
		}
		finally {
			stmt.close();
			connection.close();
		}
	
	}
	
	@Override
	public int getViewForLogin(String login) throws SQLException {
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		int res = -1;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			//PatientDAO patdao=new MySQLPatientDAO();
			ProjectDAO pdao=new MySQLProjectDAO();
			rset = stmt.executeQuery("select * from User_View where login_user='"+login+"'");

			// boucle sur les resultats de la requête
			while (rset.next()) {
				res = rset.getInt("view_num");
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}
	
	public boolean removeUser(User u) throws SQLException{
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
			
			rset = stmt.executeUpdate("delete from User_View where login_user='"+u.getLogin()+"'");

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
