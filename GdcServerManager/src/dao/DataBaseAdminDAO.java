package dao;

import java.sql.SQLException;

import model.User;

public interface DataBaseAdminDAO {
	public boolean createUser(User u) throws SQLException;
	public boolean createProject(String databaseName) throws SQLException;
	public boolean exists(String databaseName) throws SQLException;
}
