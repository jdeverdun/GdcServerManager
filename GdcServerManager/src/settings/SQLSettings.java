package settings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import model.AcquisitionDate;
import dao.project.MySQLPatientDAO;
import dao.project.PatientDAO;

import oracle.ucp.UniversalConnectionPoolAdapter;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.JDBCConnectionPoolStatisticsImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.oracle.OracleJDBCConnectionPoolStatistics;
import oracle.ucp.jdbc.oracle.OracleJDBCConnectionPoolStatisticsImpl;
import settings.sql.DBTables;

public class SQLSettings {
	public static final int reconnectTimeout = 1800000;// temps a attendre depuis la derniere requete pour reouvrir la connection a la bdd
			
	public static String ADDRESS = "192.168.55.10";//"localhost";
	public static String DATABASE_NAME = "gdcserver";
	public static DBTables TABLES = new DBTables();
	public static long lastRequestTime = 0; // date de la derniere requete
	public static long lastStartTime = 0; // date du dernier restart du pds
	// nom des tables beneficiant de vues pour chaque utilisateur
	// trie par niveau (le plus bas etant dicomimage puis niftiimage
	// le plus haut project, le dernier est user
	public static final String[] LOCAL_VIEW_NAME = new String[]{TABLES.getDicomImage().TNAME,TABLES.getNiftiImage().TNAME, TABLES.getSerie().TNAME,
		TABLES.getProtocol().TNAME, TABLES.getAcquisitionDate().TNAME,TABLES.getPatient().TNAME, TABLES.getProject().TNAME, TABLES.getUser().TNAME,TABLES.getUser_project().TNAME
	};
	// Pool Data Source pour gerer les connections SQL
	// en limitant la charge machine
	public static PoolDataSource PDS = null;
	private static UniversalConnectionPoolManager MGR = null;
	
	
	/**
	 * D�marre le Pool Data Source pour gerer les connections �
	 * la base de donnee en recuperant les informations de connexion
	 * depuis UserProfile.  La taille du pool est plac� par d�faut selon : 
	 * Initial pool size = 5 // Min pool size = 5 // Max pool size = 10
	 * J'utilise le pool manager pour controler le lancement - extinction du pool
	 * @throws SQLException
	 * @throws UniversalConnectionPoolException 
	 */
	public static void launchPDS() throws SQLException, UniversalConnectionPoolException{
		if(MGR==null)
			MGR = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
		if(PDS!=null){
			MGR.destroyConnectionPool("mgr_pool");
			PDS = null;
		}
		//Creating a pool-enabled data source
		PDS = PoolDataSourceFactory.getPoolDataSource();
		PDS.setConnectionPoolName("mgr_pool");
		//Setting connection properties of the data source
		PDS.setConnectionFactoryClassName("com.mysql.cj.jdbc.MysqlDataSource");
		   
		PDS.setURL("jdbc:mysql://"+ADDRESS+":3306/"+DATABASE_NAME+"?autoReconnect=true");
		PDS.setUser(UserProfile.LOGIN);
		PDS.setPassword(UserProfile.ENCRYPTEDPASS);

		// Create connection pool
		MGR.createConnectionPool((UniversalConnectionPoolAdapter) PDS);
		
		// Start pool
		MGR.startConnectionPool("mgr_pool");
		//Setting pool properties
		PDS.setInitialPoolSize(Runtime.getRuntime().availableProcessors()*2);
		PDS.setMinPoolSize(5);
		PDS.setMaxPoolSize(Runtime.getRuntime().availableProcessors()*3);
		   
		
	}
	
	/**
	 * /**
	 * D�marre le Pool Data Source pour gerer les connections �
	 * la base de donnee en recuperant les informations de connexion
	 * depuis UserProfile, la taille du pool est donnee en argument
	 * J'utilise le pool manager pour controler le lancement - extinction du pool
	 * @param ipoolsize initial pool size
	 * @param minpoolsize minimum pool size
	 * @param maxpoolsize max pool size
	 * @throws SQLException
	 * @throws UniversalConnectionPoolException 
	 */
	public static void launchPDS(int ipoolsize,int minpoolsize, int maxpoolsize) throws SQLException, UniversalConnectionPoolException{
		if(MGR==null)
			MGR = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
		if(PDS!=null){
			MGR.destroyConnectionPool("mgr_pool");
			PDS = null;
		}
		//Creating a pool-enabled data source
		PDS = PoolDataSourceFactory.getPoolDataSource();
		PDS.setConnectionPoolName("mgr_pool");

		//Setting connection properties of the data source
		PDS.setConnectionFactoryClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		   
		PDS.setURL("jdbc:mysql://"+ADDRESS+":3306/"+DATABASE_NAME+"?autoReconnect=true");
		PDS.setUser(UserProfile.LOGIN);
		PDS.setPassword(UserProfile.ENCRYPTEDPASS);
		   

		//Setting pool properties
		PDS.setInitialPoolSize(ipoolsize);
		PDS.setMinPoolSize(minpoolsize);
		PDS.setMaxPoolSize(maxpoolsize);
		
		// Create connection pool
		MGR.createConnectionPool((UniversalConnectionPoolAdapter) PDS);
		
		// Start pool
		MGR.startConnectionPool("mgr_pool");
		lastStartTime = System.currentTimeMillis();
		   
		
	}
	public static void stopPDS() throws UniversalConnectionPoolException{
		if(MGR!=null)
			MGR.destroyConnectionPool("mgr_pool");
		PDS = null;
	}
	
	public static PoolDataSource getPDS(){
		// si ca fait plus d'une heure que l'on a pas fait de requete on relance le pool
		if(lastRequestTime != 0 && ((System.currentTimeMillis()-lastRequestTime)>= reconnectTimeout)){
			try {
				stopPDS();
				launchPDS();
				WindowManager.mwLogger.log(Level.INFO,"Successfully reconnected to PDS.");
			} catch (UniversalConnectionPoolException e) {
				WindowManager.mwLogger.log(Level.SEVERE,"getPDS error.",e);
			} catch (SQLException e) {
				WindowManager.mwLogger.log(Level.SEVERE,"getPDS error.",e);
			}
		}
		lastRequestTime = System.currentTimeMillis();
		//String fcfInfo = ((JDBCConnectionPoolStatisticsImpl)PDS.getStatistics()).toString();
				//  getFCFProcessingInfo();
				//System.out.println("The FCF information: "+fcfInfo+".");
		return PDS;
	}
	public static Connection getConnection() throws SQLException{
		boolean continu = true;
		Connection conn = null;
		while(continu){
			try{
				conn = getPDS().getConnection();
				continu=false;
			}catch(SQLException e){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
				}
			}
		}
		return conn;
	}

	public static boolean connectionIsWorking() {
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery("select 1");
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}
}
