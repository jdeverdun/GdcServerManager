package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import daemon.DBCache;

import exceptions.IllegalSQLRequest;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.UserProfile;

public class MySQLGenericRequestDAO implements GenericRequestDAO {

	private final String[] excludeBegin = new String[]{"id_"};
	private final String[] excludeEquals = new String[]{"id","rkey","password"};
	// Prefixe des champs (dans les requetes) rajoute par le logiciel
	private final String customFieldSuffixe = "panthercustfieldpref";
	
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
		
		// On cree un cache pour eviter un trop gros nombre de requetes SQL (pr retrouver un nom qu'on connait deja)
		DBCache dbcache = new DBCache();
		// On rajoute les champs pour reconstruire un chemin vers un fichier
		String fieldToAdd = "";
		switch(type){
			case 0:case 1:// DicomImage ou NiftiImage
				fieldToAdd = " id_project as idproject"+customFieldSuffixe+", id_patient as idpatient"+customFieldSuffixe+", id_acqdate as idacqdate"+customFieldSuffixe+", " +
						"id_protocol as idprotocol"+customFieldSuffixe+", id_serie as idserie"+customFieldSuffixe+", name as nameimage"+customFieldSuffixe+" "; 
				break;
			case 2://Serie
				fieldToAdd = " id_project as idproject"+customFieldSuffixe+", id_patient as idpatient"+customFieldSuffixe+", " +
						"id_acqdate as idacqdate"+customFieldSuffixe+", " +
						"id_protocol as idprotocol"+customFieldSuffixe+", name as nameserie"+customFieldSuffixe+" ";
				break;
			case 3://Protocol
				fieldToAdd = " id_project as idproject"+customFieldSuffixe+", id_patient as idpatient"+customFieldSuffixe+", " +
						"id_acqdate as idacqdate"+customFieldSuffixe+", name as nameproto"+customFieldSuffixe+" ";
				break;
			case 4://Acqdate
				fieldToAdd = " id_project as idproject"+customFieldSuffixe+", id_patient as idpatient"+customFieldSuffixe+", name as nameacqdate"+customFieldSuffixe+" ";
				break;
			case 5://Patient
				fieldToAdd = " id_project as idproject"+customFieldSuffixe+", name as namepatient"+customFieldSuffixe+" ";
				break;
			case 6://Project
				fieldToAdd = " name as nameproject"+customFieldSuffixe+" ";
				break;
				
		}
		splitFrom[0] = splitFrom[0] + fieldToAdd;
		String temp  = "";
		for(int i = 0; i < splitFrom.length-1; i++)
			temp = temp + splitFrom[i] + " from " + splitFrom[i+1];
		nrequest = temp;
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
				String cname = rsmd.getColumnName(i);
				// on evite d'afficher les champs id etc et les champs rajouter par le logiciel
				if(!isIllegalHeader(cname) && !name.endsWith(customFieldSuffixe)){
					// si le nom de la colonne existe deja on 
					// rajoute le nom de la table pour preciser
					if(resultats.containsKey(name))
						name = rsmd.getTableName(i) +"."+name;
					indices.put(name, i);
					resultats.put(name, new ArrayList<String>());
				}
			}
			while(rset.next()){
				String[] customFields;
				switch(type){
				case 0:case 1:
					customFields = new String[6];
					customFields[0] = rset.getString("idproject"+customFieldSuffixe);
					customFields[1] = rset.getString("idpatient"+customFieldSuffixe);
					customFields[2] = rset.getString("idacqdate"+customFieldSuffixe);
					customFields[3] = rset.getString("idprotocol"+customFieldSuffixe);
					customFields[4] = rset.getString("idserie"+customFieldSuffixe);
					customFields[5] = rset.getString("nameimage"+customFieldSuffixe);
					
				}
				File file = buildPathFromId()
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

