package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import exceptions.IllegalSQLRequest;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.UserProfile;

public class MySQLGenericRequestDAO implements GenericRequestDAO {

	private final String[] excludeBegin = new String[]{"id_"};
	private final String[] excludeEquals = new String[]{"id","rkey","password"};
	
	/**
	 * Renvoi le header du result SQL + les lignes de resultats
	 * @throws IllegalSQLRequest 
	 */
	@Override
	public HashMap<String, ArrayList<String>> executeSelect(String request)
			throws SQLException, IllegalSQLRequest {
		request = request.toLowerCase();
		if(!request.startsWith("select "))
			throw new IllegalSQLRequest("Only select request are supported in function executeSelect.");
		HashMap<String,ArrayList<String>> resultats = new HashMap<String,ArrayList<String>>();
		
		String nrequest = "";
		// on utilise la bonne vue utilisateur sur la db
		if(UserProfile.CURRENT_USER.getLevel()==3)
			nrequest = request;
		else
			nrequest = adaptRequestToUser(request);
		// on recupere une id pour gerer les resultats sous forme de fichier
		// on ne prend pas le dernier niveau qui correspond aux utilisateurs
		int type = -1;
		String[] splitFrom = nrequest.split(" from ");
		for(int i = 0; i<SQLSettings.LOCAL_VIEW_NAME.length-1;i++){
			if(splitFrom[1].contains(SQLSettings.LOCAL_VIEW_NAME[i])){
				type = i;break;
			}
		}
		// On rajoute les champs pour recuperer un chemin vers un fichier
		String fieldToAdd = "";
		switch(type){
			case 0:case 1:
				fieldToAdd = " id_serie as idserie1 "; 
				break;
			case 2:
				fieldToAdd = " id_"+SQLSettings.LOCAL_VIEW_NAME[type+1];
				
		}
		// --------------- RAJOUTER LA REQUETE POUR RECUP LE CHEMIN COMPLET --- et terminer ligne ci dessus
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.PDS.getConnection();
			stmt = connection.createStatement();
		

			rset = stmt.executeQuery(nrequest);

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

	/**
	 * Corrige le nom de la vue a requeter ex : user id 1 
	 * select * from user; devient select * from user_1;
	 * Attention, buggue quand on a dans le select le nom de la table
	 * genre : select user from user qui devient -> select user_2 from user_2
	 * @param request
	 * @return
	 */
	private String adaptRequestToUser(String request) {
		String nrequest="";
		request = request.toLowerCase();

		String[] splitFrom;
		if(request.contains(" from "))
			splitFrom = request.split(" from ");
		else
			return null;
		if(splitFrom==null)
			return null;
		nrequest = splitFrom[0];

		String rightpart="";
		
		for(int i=1;i<splitFrom.length;i++){
			rightpart += " "+splitFrom[i];
		}
		for(String view:SQLSettings.LOCAL_VIEW_NAME){
			// on rajoute les "_num" dans les nom de table
			rightpart = rightpart.replaceAll(" "+view+" ", " "+view+"_"+UserProfile.CURRENT_USER.getId()+" ");
			rightpart = rightpart.replaceAll(" "+view+".", " "+view+"_"+UserProfile.CURRENT_USER.getId()+".");
			rightpart = rightpart.replaceAll(","+view+".", ","+view+"_"+UserProfile.CURRENT_USER.getId()+".");
		}
		nrequest = nrequest + " from " + rightpart;
		
		
		return nrequest;
	}

	/**
	 * Permet de s'assurer que le nom "n" n'est pas dans la liste
	 * a exclure du resultat de la requete
	 * @param n
	 * @return
	 */
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

