package dao.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import settings.SQLSettings;
import settings.UserProfile;
import settings.sql.DBTables;
import settings.sql.tables.AcquisitionDateTable;
import settings.sql.tables.PatientTable;

import dao.MySQLProjectDAO;
import dao.ProjectDAO;

import model.AcquisitionDate;
import model.Patient;
import model.Project;
import model.User;
import model.Job;

public class MySQLJobDAO implements JobDAO {
	public Collection<Job> retrieveAll() throws SQLException {
		Collection<Job> jobs = new ArrayList<Job>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			ProjectDAO projdao=new MySQLProjectDAO();	
			stmt = connection.createStatement();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

}
