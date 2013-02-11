package dao;

import java.sql.SQLException;

public interface DataBaseAdminDAO {
	public boolean createUser(String login, String password) throws SQLException;
	public boolean createProject(String databaseName) throws SQLException;
	public boolean exists(String databaseName) throws SQLException;
}
