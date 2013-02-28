package settings;

import java.sql.SQLException;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class SQLSettings {
	public static String ADDRESS = "localhost";
	public static String DATABASE_NAME = "gdcserver";
	// Pool Data Source pour gerer les connections SQL
	// en limitant la charge machine
	public static PoolDataSource PDS = null;
	
	/**
	 * Démarre le Pool Data Source pour gerer les connections à
	 * la base de donnee en recuperant les informations de connexion
	 * depuis UserProfile.  La taille du pool est placé par défaut selon : 
	 * Initial pool size = 5 // Min pool size = 5 // Max pool size = 10
	 * @throws SQLException
	 */
	public static void launchPDS() throws SQLException{
		//Creating a pool-enabled data source
	   PDS = PoolDataSourceFactory.getPoolDataSource();

	   //Setting connection properties of the data source
	   PDS.setConnectionFactoryClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
	   
	   PDS.setURL("jdbc:mysql://"+ADDRESS+":3306/"+DATABASE_NAME);
	   PDS.setUser(UserProfile.LOGIN);
	   PDS.setPassword(UserProfile.ENCRYPTEDPASS);
	   
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
	 * @param ipoolsize initial pool size
	 * @param minpoolsize minimum pool size
	 * @param maxpoolsize max pool size
	 * @throws SQLException
	 */
	public static void launchPDS(int ipoolsize,int minpoolsize, int maxpoolsize) throws SQLException{
		//Creating a pool-enabled data source
	   PDS = PoolDataSourceFactory.getPoolDataSource();

	   //Setting connection properties of the data source
	   PDS.setConnectionFactoryClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
	   
	   PDS.setURL("jdbc:mysql://"+ADDRESS+":3306/"+DATABASE_NAME);
	   PDS.setUser(UserProfile.LOGIN);
	   PDS.setPassword(UserProfile.ENCRYPTEDPASS);
	   
	   //Setting pool properties
	   PDS.setInitialPoolSize(ipoolsize);
	   PDS.setMinPoolSize(minpoolsize);
	   PDS.setMaxPoolSize(maxpoolsize);
	}
}
