package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import oracle.ucp.UniversalConnectionPoolException;

import settings.SQLSettings;
import settings.UserProfile;

import model.Project;
import model.User;

/**
 * Classe faisant les requête sur la table Grimpeur
 * 
 *
 */
public class MySQLUserDAO implements UserDAO {

	/**
	 * Récupère la liste des Grimpeurs présent dans la table Grimpeur
	 * @return
	 * @throws SQLException
	 */
	public Collection<User> retrieveAll() throws SQLException {
		// la liste de grimpeurs
		Collection<User> users = new ArrayList<User>();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getUser().TNAME);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getUser().TNAME+"_"+UserProfile.CURRENT_USER.getId());

			// boucle sur les resultats de la requÃªte
			while (rset.next()) {
				User user = new User();
				user.setId(rset.getInt(SQLSettings.TABLES.getUser().getId()));
				user.setEmail(rset.getString(SQLSettings.TABLES.getUser().getEmail()));
				user.setLogin(rset.getString(SQLSettings.TABLES.getUser().getLogin()));
				user.setNom(rset.getString(SQLSettings.TABLES.getUser().getNom()));
				user.setPassword(rset.getString(SQLSettings.TABLES.getUser().getPassword()));
				user.setPrenom(rset.getString(SQLSettings.TABLES.getUser().getPrenom()));
				user.setLevel(rset.getInt(SQLSettings.TABLES.getUser().getLevel()));
				user.setFirstConnect(rset.getInt(SQLSettings.TABLES.getUser().getFirstconnect()));
				users.add(user);
			}
			return users;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

	/**
     * Authentifie un utilisateur à partir du login et du mot de passe
     * fournit
     * 
     * @param login
     * @param password
     * @return
     * @throws SQLException
     */
	public User connexion(String login, String password)
			throws SQLException {
		// TODO Auto-generated method stub
		User userC = new User();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			String url = "jdbc:mysql://"+SQLSettings.ADDRESS+":3306/";
			connection = DriverManager.getConnection(url, "fakeuser", "");
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select password('" + password + "');");
			if (rset != null) {
				rset.next();
				UserProfile.ENCRYPTEDPASS = rset.getString(1);
				UserProfile.LOGIN = login;
			}
			// On lance le pool PDS apres avoir mis à jours les infos de connexion
			try {
				SQLSettings.launchPDS();
			} catch (SQLException | UniversalConnectionPoolException e1) {
				System.err.println("PDS could not be launch ... will exit");
				e1.printStackTrace();
			}
			// On recupere une connexion
			try{
				connection = SQLSettings.getPDS().getConnection();
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
			stmt = connection.createStatement();
			
			UserViewDAO uvdao = new MySQLUserViewDAO();
			int idview = uvdao.getViewForLogin(login);
			if(idview == UserViewDAO.ADMIN_VIEW_NUM){
				rset = stmt.executeQuery("select * from  "+SQLSettings.TABLES.getUser().TNAME+"  where "+SQLSettings.TABLES.getUser().getLogin()+"='"
						+ login + "' and  "+SQLSettings.TABLES.getUser().getPassword()+"='" + UserProfile.ENCRYPTEDPASS + "'");
			}else{
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getUser().TNAME+"_"+idview+" where "+SQLSettings.TABLES.getUser().getLogin()+"='"
						+ login + "' and  "+SQLSettings.TABLES.getUser().getPassword()+"='" + UserProfile.ENCRYPTEDPASS + "'");
			}
			if (rset != null) {
				rset.next();
				userC.setNom(rset.getString(SQLSettings.TABLES.getUser().getNom()));
				userC.setPrenom(rset.getString(SQLSettings.TABLES.getUser().getPrenom()));
				userC.setEmail(rset.getString(SQLSettings.TABLES.getUser().getEmail()));
				userC.setLogin(rset.getString(SQLSettings.TABLES.getUser().getLogin()));
				userC.setPassword(rset.getString(SQLSettings.TABLES.getUser().getPassword()));
				userC.setId(Integer.parseInt(rset.getString(SQLSettings.TABLES.getUser().getId())));
				userC.setLevel(rset.getInt(SQLSettings.TABLES.getUser().getLevel()));
				userC.setFirstConnect(rset.getInt(SQLSettings.TABLES.getUser().getFirstconnect()));
				UserProfile.CURRENT_USER = userC;
				try { if(rset!=null) rset.close();  } catch (Exception e) {};
			}else{
				return null;
			}

			return userC;

		} catch (SQLException e) {
			e.printStackTrace();
			//e.printStackTrace();
			throw e;
			//return null;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}

	}
	
	
	
	/**
     * Insère un tuple dans la table User et renvoi 0 si tout c'est bien passe
     * 1 si le login existe deja
     * 2 si erreur SQL autre
     * firstConnect est obligatoirement place a 1
     * 
     * @param id
     * @param nom
     * @param prenom
     * @param email
     * @param login
     * @param password
     * @param level
     * @return
     * @throws SQLException
     */
	public int newUser( String nom, String prenom,  String email, String login, String password, int level) throws SQLException {
		
			boolean rset = false;
			Statement stmt = null;
			Connection connection = null;
			try {
				connection = SQLSettings.getPDS().getConnection();
				stmt = connection.createStatement();
				
				rset = stmt.execute("insert into  "+SQLSettings.TABLES.getUser().TNAME+"  values (NULL,'"
						+ nom + "' ,'" + prenom + "', '"+email+ "', '"+login+"', '"+password+"', "+level+",1)");
				
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
	
	/**
     * Cree un nouvel utilisateur 
     * Renvoi 3 si le "User" n'est pas complet
     * 
     * @param u
     * @return
     * @throws SQLException
     */
	public int newUser(User u) throws SQLException {
		if(u.isReadyForInsert())
			return newUser(u.getNom(), u.getPrenom(), u.getEmail(), u.getLogin(), u.getPassword(),u.getLevel());
		else
			return 3;
	}
	
	/**
     * Récupère le plus grand ID de la table User
     * @return
	 * @throws SQLException 
     */
	public int idmax() throws SQLException{
		
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getUser().getId()+") from  "+SQLSettings.TABLES.getUser().TNAME+" ;");
			else
				rset = stmt.executeQuery("select max("+SQLSettings.TABLES.getUser().getId()+") from "+SQLSettings.TABLES.getUser().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" ;");
				
			if (rset != null) {
				while(rset.next()){
					//System.out.println("id max= "+rset.getInt(1));
					ident=rset.getInt(1);
				}
			}
			
			return ident;
		
		}catch(Exception e){
			e.printStackTrace();	throw e;
		}finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
		
	}
	
	
	public int aivalue() throws SQLException{
		
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			int ident=-1;		
	
			rset = stmt.executeQuery("SHOW TABLE STATUS LIKE '"+SQLSettings.TABLES.getUser().TNAME+"' ;");

				
			if (rset != null) {
				while(rset.next()){
					//System.out.println("id max= "+rset.getInt(1));
					ident=rset.getInt("Auto_increment");
				}
			}
			
			return ident;
		
		}catch(Exception e){
			e.printStackTrace();	
			throw e;
		}finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
		
	}
	/**
     * Récupère l'utilisateur ayant l'id SQLSettings.TABLES.getUser().getId()
     * @param id
     * @return
     * @throws SQLException
     */
	public User retrieveUser(int id) throws SQLException {
		// TODO Auto-generated method stub
		User userC = new User();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
		
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from  "+SQLSettings.TABLES.getUser().TNAME+"  where "+SQLSettings.TABLES.getUser().getId()+"="+id);
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getUser().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getUser().getId()+"="+id);
			
			while(rset.next()){
				userC.setNom(rset.getString(SQLSettings.TABLES.getUser().getNom()));
				userC.setPrenom(rset.getString(SQLSettings.TABLES.getUser().getPrenom()));
				userC.setEmail(rset.getString(SQLSettings.TABLES.getUser().getEmail()));
				userC.setLogin(rset.getString(SQLSettings.TABLES.getUser().getLogin()));
				userC.setPassword(rset.getString(SQLSettings.TABLES.getUser().getPassword()));
				userC.setId(Integer.parseInt(rset.getString(SQLSettings.TABLES.getUser().getId())));
				userC.setFirstConnect(rset.getInt(SQLSettings.TABLES.getUser().getFirstconnect()));
				userC.setLevel(rset.getInt(SQLSettings.TABLES.getUser().getLevel()));
			}
		
			return userC;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
		
	}
	
	
	/**
     * Récupère l'utilisateur ayant le login SQLSettings.TABLES.getUser().getLogin()
     * @param id
     * @return
     * @throws SQLException
     */
	public User retrieveUser(String login) throws SQLException {
		// TODO Auto-generated method stub
		User userC = new User();
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
		
			if(UserProfile.CURRENT_USER.getLevel() == 3)
				rset = stmt.executeQuery("select * from  "+SQLSettings.TABLES.getUser().TNAME+"  where "+SQLSettings.TABLES.getUser().getLogin()+"='"+login+"'");
			else
				rset = stmt.executeQuery("select * from "+SQLSettings.TABLES.getUser().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" where "+SQLSettings.TABLES.getUser().getLogin()+"='"+login+"'");
			
			while(rset.next()){
				userC.setNom(rset.getString(SQLSettings.TABLES.getUser().getNom()));
				userC.setPrenom(rset.getString(SQLSettings.TABLES.getUser().getPrenom()));
				userC.setEmail(rset.getString(SQLSettings.TABLES.getUser().getEmail()));
				userC.setLogin(rset.getString(SQLSettings.TABLES.getUser().getLogin()));
				userC.setPassword(rset.getString(SQLSettings.TABLES.getUser().getPassword()));
				userC.setId(Integer.parseInt(rset.getString(SQLSettings.TABLES.getUser().getId())));
				userC.setLevel(rset.getInt(SQLSettings.TABLES.getUser().getLevel()));
				userC.setFirstConnect(rset.getInt(SQLSettings.TABLES.getUser().getFirstconnect()));
			}
		
			return userC;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
		
	}
	
	/**
     * Met à jours un user
     * @param i
     * @param l
     * @param pass
     * @param n
     * @param pr
     * @param e
     * @param level
     * @param fconnect
     * @return
     * @throws SQLException
     */
	public boolean updateUser(int i,String l, String pass,String n,String pr,String e,int level, int fconnect) throws SQLException{
		// la liste de grimpeurs
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			if(UserProfile.CURRENT_USER.getLevel() == 3){
				rset = stmt.executeUpdate("update  "+SQLSettings.TABLES.getUser().TNAME+"  set "+SQLSettings.TABLES.getUser().getLogin()+"='"+l+"',"+SQLSettings.TABLES.getUser().getPassword()+"='"+pass+"'," +
						""+SQLSettings.TABLES.getUser().getPrenom()+"='"+pr+"',"+SQLSettings.TABLES.getUser().getNom()+"='"+n+"',"+SQLSettings.TABLES.getUser().getEmail()+"='"+e+"', " +
								""+SQLSettings.TABLES.getUser().getLevel()+"="+level+", "+SQLSettings.TABLES.getUser().getFirstconnect()+"="+fconnect+" where id="+i);
			}else{
				rset = stmt.executeUpdate("update  "+SQLSettings.TABLES.getUser().TNAME+"_"+UserProfile.CURRENT_USER.getId()+" set "+SQLSettings.TABLES.getUser().getLogin()+"='"+l+"',"+SQLSettings.TABLES.getUser().getPassword()+"='"+pass+"'," +
						""+SQLSettings.TABLES.getUser().getPrenom()+"='"+pr+"',"+SQLSettings.TABLES.getUser().getNom()+"='"+n+"',"+SQLSettings.TABLES.getUser().getEmail()+"='"+e+"', " +
								""+SQLSettings.TABLES.getUser().getLevel()+"="+level+", "+SQLSettings.TABLES.getUser().getFirstconnect()+"="+fconnect+" where id="+i);
			}
			return true;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			return false;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e1) {};
			try { if(connection!=null) connection.close();  } catch (Exception e1) {};
		}
	}
	
	public boolean updateUser(User user) throws SQLException{
		return updateUser(user.getId(), user.getLogin(), user.getPassword(), user.getNom(), user.getPrenom(), user.getEmail(), user.getLevel(), user.getFirstConnect());
	}
	
	public String encryptPass(String password)
			throws SQLException {
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			String url = "jdbc:mysql://"+SQLSettings.ADDRESS+":3306/";
			connection = DriverManager.getConnection(url, "fakeuser", "");
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select password('" + password + "');");
			if (rset != null) {
				rset.next();
				return rset.getString(1);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
	
	public boolean removeUser(User u) throws SQLException{
		// la liste de grimpeurs
		int rset = 0;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			
			rset = stmt.executeUpdate("delete from  "+SQLSettings.TABLES.getUser().TNAME+"  where id="+u.getId());

			UserProjectDAO udao = new MySQLUserProjectDAO();
			udao.removeUser(u);
			return true;
		} catch (SQLException e2) {
			System.err.println("Erreur SQL " + e2);
			throw e2;
			//return false;
		} finally {
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
}