package settings;

import java.sql.SQLException;

import oracle.ucp.UniversalConnectionPoolAdapter;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import settings.sql.DBTables;

public class SQLSettings {
	public static String ADDRESS = "localhost";
	public static String DATABASE_NAME = "gdcserver";
	public static DBTables TABLES = new DBTables();
	// nom des tables beneficiant de vues pour chaque utilisateur
	// trie par niveau (le plus bas etant dicomimage puis niftiimage
	// le plus haut project, le dernier est user
	public static final String[] LOCAL_VIEW_NAME = new String[]{TABLES.getDicomImage().TNAME,TABLES.getNiftiImage().TNAME, TABLES.getSerie().TNAME,
		TABLES.getProtocol().TNAME, TABLES.getAcquisitionDate().TNAME,TABLES.getPatient().TNAME, TABLES.getProject().TNAME, TABLES.getUser().TNAME
	};
	// Pool Data Source pour gerer les connections SQL
	// en limitant la charge machine
	public static PoolDataSource PDS = null;
	private static UniversalConnectionPoolManager MGR = null;
	
	
	/**
	 * Démarre le Pool Data Source pour gerer les connections à
	 * la base de donnee en recuperant les informations de connexion
	 * depuis UserProfile.  La taille du pool est placé par défaut selon : 
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
		PDS.setConnectionFactoryClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		   
		PDS.setURL("jdbc:mysql://"+ADDRESS+":3306/"+DATABASE_NAME);
		PDS.setUser(UserProfile.LOGIN);
		PDS.setPassword(UserProfile.ENCRYPTEDPASS);
		  
		// Create connection pool
		MGR.createConnectionPool((UniversalConnectionPoolAdapter) PDS);
		
		// Start pool
		MGR.startConnectionPool("mgr_pool");
		//Setting pool properties
		PDS.setInitialPoolSize(5);
		PDS.setMinPoolSize(5);
		PDS.setMaxPoolSize(10);
		   
		
	}
	
	/**
	 * /**
	 * Démarre le Pool Data Source pour gerer les connections à
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
		   
		PDS.setURL("jdbc:mysql://"+ADDRESS+":3306/"+DATABASE_NAME);
		PDS.setUser(UserProfile.LOGIN);
		PDS.setPassword(UserProfile.ENCRYPTEDPASS);
		   
		// Create connection pool
		MGR.createConnectionPool((UniversalConnectionPoolAdapter) PDS);
		
		// Start pool
		MGR.startConnectionPool("mgr_pool");
		
		//Setting pool properties
		PDS.setInitialPoolSize(ipoolsize);
		PDS.setMinPoolSize(minpoolsize);
		PDS.setMaxPoolSize(maxpoolsize);
		   
		
	}
	public static void stopPDS() throws UniversalConnectionPoolException{
		if(MGR!=null)
			MGR.destroyConnectionPool("mgr_pool");
		PDS = null;
	}
}
