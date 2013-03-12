package dao;

import java.sql.SQLException;
import java.util.Set;

import model.Project;
import model.User;

public interface UserProjectDAO {

	public Set<Project> getProjectsForUser(int id) throws SQLException;
	public Set<User> getUsersForProject(int id) throws SQLException;
	public boolean removeUser(User u) throws SQLException;
	public int addLink(String login,String projectname) throws SQLException;
	public boolean exists(int user_id, int project_id) throws SQLException;
	public int removeLink(String login, String projectname) throws SQLException;
	public int removeLink(int id_user, int id_project) throws SQLException;
}
