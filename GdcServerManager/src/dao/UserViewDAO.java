package dao;

import java.sql.SQLException;
import java.util.Set;

import model.Project;
import model.User;

/**
 * Classe referencant la table user_view permettant de determiner la vue associee 
 * a un utilisateur
 * @author DEVERDUN Jeremy
 *
 */
public interface UserViewDAO {
	public static int ADMIN_VIEW_NUM = -10;
	
	/**
	 * Insere un tuple
	 * @param login
	 * @param view
	 * @return
	 * @throws SQLException
	 */
	public int addUserView(String login,int view) throws SQLException;
	
	
	/**
	 * Supprime un tuple
	 * @param u
	 * @return
	 * @throws SQLException
	 */
	public boolean removeUser(User u) throws SQLException;
	
	/**
	 * Recupere le numero de la vue associee a l'utilisateur
	 * @param login
	 * @return
	 * @throws SQLException
	 */
	public int getViewForLogin(String login) throws SQLException;
}
