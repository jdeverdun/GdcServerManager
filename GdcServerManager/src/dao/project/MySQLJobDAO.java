package dao.project;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


import settings.SQLSettings;
import settings.sql.DBTables;
import settings.sql.tables.JobTable;


import dao.MySQLUserDAO;
import dao.UserDAO;

import model.User;
import model.Job;

public class MySQLJobDAO implements JobDAO {
	public ArrayList<Job> retrieveAllJob() throws SQLException {
		ArrayList<Job> jobs = new ArrayList<Job>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			UserDAO userdao=new MySQLUserDAO();	
			stmt = connection.createStatement();
			
			rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getJob().TNAME+"");

			// boucle sur les resultats de la requête
			while (rset.next()) {
				Job job = new Job();
				job.setId(rset.getInt(SQLSettings.TABLES.getJob().getId()));
				job.setUserId(userdao.retrieveUser(rset.getInt(SQLSettings.TABLES.getJob().getUserId())));
				job.setJobId(rset.getString(SQLSettings.TABLES.getJob().getJobId()));
				job.setOs(rset.getString(SQLSettings.TABLES.getJob().getOs()));
				job.setDescription(rset.getString(SQLSettings.TABLES.getJob().getDescription()));
				jobs.add(job);
			}
			return jobs;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

	public boolean newJob(int user_id,String jobid,String submitDate,String os,String description) throws SQLException {

		boolean rset = false;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.execute("insert into "+SQLSettings.TABLES.getJob().TNAME+" values (NULL,"
					+ user_id + ",'"+jobid+"','"+submitDate+"','"+os+"','"+description+"')");

			return true;

		}
		catch(Exception e){
			e.printStackTrace();	throw e;
		}
		finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	public Job retrieveJobById(int id) throws SQLException {
		Job job = new Job();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			UserDAO userdao = new MySQLUserDAO();

			rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getJob().TNAME+" where "+SQLSettings.TABLES.getJob().getId()+"="+id);

			while(rset.next()){
				job.setId(rset.getInt(SQLSettings.TABLES.getJob().getId()));
				job.setUserId(userdao.retrieveUser(rset.getInt(SQLSettings.TABLES.getJob().getUserId())));
				job.setJobId(rset.getString(SQLSettings.TABLES.getJob().getJobId()));
				job.setOs(rset.getString(SQLSettings.TABLES.getJob().getOs()));
				job.setDescription(rset.getString(SQLSettings.TABLES.getJob().getDescription()));
			}

			return job;

		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}

	}
		
	public Job retrieveJobByJobId(String jobid) throws SQLException {
		Job job = new Job();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			UserDAO userdao = new MySQLUserDAO();

			rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getJob().TNAME+" where "+SQLSettings.TABLES.getJob().getJobId()+"="+jobid);

			while(rset.next()){
				job.setId(rset.getInt(SQLSettings.TABLES.getJob().getId()));
				job.setUserId(userdao.retrieveUser(rset.getInt(SQLSettings.TABLES.getJob().getUserId())));
				job.setJobId(rset.getString(SQLSettings.TABLES.getJob().getJobId()));
				job.setOs(rset.getString(SQLSettings.TABLES.getJob().getOs()));
				job.setDescription(rset.getString(SQLSettings.TABLES.getJob().getDescription()));
			}

			return job;

		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}

	}
	
	public ArrayList<Job> retrieveJobByLogin(String login) throws SQLException{
		ArrayList<Job> jobs = new ArrayList<Job>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			UserDAO userdao = new MySQLUserDAO();		
			User user = userdao.retrieveUser(login);

			rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getJob().TNAME+" where "+SQLSettings.TABLES.getJob().getUserId()+"="+user.getId());

			while(rset.next()){
				Job job = new Job();
				job.setId(rset.getInt(SQLSettings.TABLES.getJob().getId()));
				job.setUserId(userdao.retrieveUser(rset.getInt(SQLSettings.TABLES.getJob().getUserId())));
				job.setJobId(rset.getString(SQLSettings.TABLES.getJob().getJobId()));
				job.setOs(rset.getString(SQLSettings.TABLES.getJob().getOs()));
				job.setDescription(rset.getString(SQLSettings.TABLES.getJob().getDescription()));
				jobs.add(job);
			}

			return jobs;

		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	public ArrayList<Job> retrieveJobByUserId(int user_id) throws SQLException {
		ArrayList<Job> jobs = new ArrayList<Job>();
		UserDAO userdao = new MySQLUserDAO();		
		User user = userdao.retrieveUser(user_id);
		JobDAO jobdao = new MySQLJobDAO();
		jobs = jobdao.retrieveJobByLogin(user.getLogin()) ;
		return jobs;

	}
	
	public void removeJob(String jobid, int user_id) throws SQLException {
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			DBTables tab = SQLSettings.TABLES;
			JobTable nt = tab.getJob();	
			
			rset = stmt.executeUpdate("delete from "+nt.TNAME+" where "+nt.getJobId()+"=" +jobid+" and "+nt.getUserId()+"="+user_id);
			
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

