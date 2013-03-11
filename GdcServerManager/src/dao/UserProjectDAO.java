package dao;

import java.sql.SQLException;
import java.util.Set;

import model.Project;
import model.User;

public interface UserProjectDAO {

	public Set<Project> getProjectsForUser(int id) throws SQLException;
	public Set<User> getUsersForProject(int id) throws SQLException;
	public boolean removeUser(User u) throws SQLException;
}
