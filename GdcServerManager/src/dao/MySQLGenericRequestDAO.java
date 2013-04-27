package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import settings.SQLSettings;
import settings.UserProfile;

public class MySQLGenericRequestDAO implements GenericRequestDAO {

	private final String[] excludeBegin = new String[]{"id_"};
	private final String[] excludeEquals = new String[]{"id","rkey","password"};
	
	/**
	 * Renvoi le header du result SQL + les lignes de resultats
	 */
	@Override
	public HashMap<String, ArrayList<String>> execute(String request)
			throws SQLException {
		HashMap<String,ArrayList<String>> resultats = new HashMap<String,ArrayList<String>>();
		
		
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
		

			rset = stmt.executeQuery(request);

			ResultSetMetaData rsmd = rset.getMetaData();
			// on stock les indices des colonnes associe a un label
			// pour eviter les erreur avec 2 colonnes portant le meme nom
			HashMap<String,Integer> indices = new HashMap<String,Integer>();
			for(int i = 1; i <= rsmd.getColumnCount(); i++){
				String name = rsmd.getColumnLabel(i);
				// on evite d'afficher les champs id etc
				if(!isIllegalHeader(name)){
					// si le nom de la colonne existe deja on 
					// rajoute le nom de la table pour preciser
					if(resultats.containsKey(name))
						name = rsmd.getTableName(i) +"."+name;
					indices.put(name, i);
					resultats.put(name, new ArrayList<String>());
				}
			}
			while(rset.next()){
				for(String n:indices.keySet()){
					resultats.get(n).add(rset.getString(indices.get(n)));
				}
			}
		
			return resultats;
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			rset.close();
			stmt.close();
			connection.close();
		}
	}

	public boolean isIllegalHeader(String n){
		boolean isIllegalHeader = false;
		for(String ec:excludeBegin){
			if(n.startsWith(ec)){
				isIllegalHeader = true;
			}
		}
		for(String ee:excludeEquals){
			if(n.equals(ee)){
				isIllegalHeader = true;
			}
		}
		return isIllegalHeader;
	}
}
