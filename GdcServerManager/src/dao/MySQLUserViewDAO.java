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
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			rset = stmt.execute("insert into "+SQLSettings.TABLES.getUser_view().TNAME+" values (NULL,'"
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
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	
	}
	
	@Override
	public int getViewForLogin(String login) throws SQLException {
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		int res = -1;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			//PatientDAO patdao=new MySQLPatientDAO();
			ProjectDAO pdao=new MySQLProjectDAO();
			rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getUser_view().TNAME+" where "+SQLSettings.TABLES.getUser_view().getLogin_user()+"='"+login+"'");

			// boucle sur les resultats de la requête
			while (rset.next()) {
				res = rset.getInt(SQLSettings.TABLES.getUser_view().getView_num());
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	public boolean removeUser(User u) throws SQLException{
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			rset = stmt.executeUpdate("delete from "+SQLSettings.TABLES.getUser_view().TNAME+" where "+SQLSettings.TABLES.getUser_view().getLogin_user()+"='"+u.getLogin()+"'");

			return true;
		} catch (SQLException e2) {
			e2.printStackTrace();
			return false;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

}
