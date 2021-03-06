package dao;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import model.AcquisitionDate;
import model.ServerInfo;

import daemon.DBCache;
import display.containers.RequestPanel.IMAGE_TYPE;

import exceptions.IllegalSQLRequest;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.UserProfile;
import settings.sql.DBTables;
import settings.sql.tables.AcquisitionDateTable;
import settings.sql.tables.DicomImageTable;
import settings.sql.tables.NiftiImageTable;
import settings.sql.tables.PatientTable;
import settings.sql.tables.ProjectTable;
import settings.sql.tables.ProtocolTable;
import settings.sql.tables.SerieTable;

public class MySQLGenericRequestDAO implements GenericRequestDAO {

	private final String[] excludeBegin = new String[]{"id_"};
	private final String[] excludeEquals = new String[]{"id","rkey","password"};
	// Prefixe des champs (dans les requetes) rajoute par le logiciel
	private final String customFieldSuffixe = "panthercustfieldpref";
	private boolean stopCurrentRequest = false;
	
	/**
	 * Renvoi le header du result SQL + les lignes de resultats
	 * @throws IllegalSQLRequest 
	 */
	@Override
	public HashMap<String, ArrayList<String[]>> executeSelect(String request)
			throws Exception, SQLException, IllegalSQLRequest {
		stopCurrentRequest = false;
		request = request.toLowerCase();
		if(!request.startsWith("select "))
			throw new IllegalSQLRequest("Only select request are supported in function executeSelect.");
		HashMap<String,ArrayList<String[]>> resultats = new HashMap<String,ArrayList<String[]>>();
		
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
		// si on recupere "*" ex select * -> on rajoute une virgule pour empecher uen erreur sql
		/*if(splitFrom[0].contains(" *") && !splitFrom[0].contains(" *,") && type!=-1)
			splitFrom[0] = splitFrom[0].replaceAll(" \\*", " \\* , ");*/
		DBTables tables = SQLSettings.TABLES;
		

		// On rajoute les champs pour reconstruire un chemin vers un fichier
		String fieldToAdd = "";
		String opt = "";// chaine de caractere optionnelle pour gerer les vues utilisateurs
		if(UserProfile.CURRENT_USER.getLevel()!=3)
			opt="_"+UserProfile.CURRENT_USER.getId();
		switch(type){
			case 0:// DicomImage
				fieldToAdd = ", "+tables.getDicomImage().TNAME+opt+"."+tables.getDicomImage().getId_project()+" as "+tables.getDicomImage().getId_project()+""+customFieldSuffixe+", " +
						""+tables.getDicomImage().TNAME+opt+"."+tables.getDicomImage().getId_patient()+" as "+tables.getDicomImage().getId_patient()+""+customFieldSuffixe+", " +
								""+tables.getDicomImage().TNAME+opt+"."+tables.getDicomImage().getId_acqdate()+" as "+tables.getDicomImage().getId_acqdate()+""+customFieldSuffixe+", " +
						""+tables.getDicomImage().TNAME+opt+"."+tables.getDicomImage().getId_protocol()+" as "+tables.getDicomImage().getId_protocol()+""+customFieldSuffixe+", " +
								""+tables.getDicomImage().TNAME+opt+"."+tables.getDicomImage().getId_serie()+" as "+tables.getDicomImage().getId_serie()+""+customFieldSuffixe+", " +
										""+tables.getDicomImage().TNAME+opt+"."+tables.getDicomImage().getName()+" as "+tables.getDicomImage().getName()+""+customFieldSuffixe+" "; 
				break;
			case 1:// NiftiImage
				fieldToAdd = ", "+tables.getNiftiImage().TNAME+opt+"."+tables.getNiftiImage().getId_project()+" as "+tables.getNiftiImage().getId_project()+""+customFieldSuffixe+", " +
						""+tables.getNiftiImage().TNAME+opt+"."+tables.getNiftiImage().getId_patient()+" as "+tables.getNiftiImage().getId_patient()+""+customFieldSuffixe+", " +
								""+tables.getNiftiImage().TNAME+opt+"."+tables.getNiftiImage().getId_acqdate()+" as "+tables.getNiftiImage().getId_acqdate()+""+customFieldSuffixe+", " +
						""+tables.getNiftiImage().TNAME+opt+"."+tables.getNiftiImage().getId_protocol()+" as "+tables.getNiftiImage().getId_protocol()+""+customFieldSuffixe+", " +
								""+tables.getNiftiImage().TNAME+opt+"."+tables.getNiftiImage().getId_serie()+" as "+tables.getNiftiImage().getId_serie()+""+customFieldSuffixe+", " +
										""+tables.getNiftiImage().TNAME+opt+"."+tables.getNiftiImage().getName()+" as "+tables.getNiftiImage().getName()+""+customFieldSuffixe+" "; 
				break;
			case 2://Serie
				fieldToAdd = ", "+tables.getSerie().TNAME+opt+"."+tables.getSerie().getId_project()+" as "+tables.getSerie().getId_project()+""+customFieldSuffixe+", " +
						""+tables.getSerie().TNAME+opt+"."+tables.getSerie().getId_patient()+" as "+tables.getSerie().getId_patient()+""+customFieldSuffixe+", " +
								""+tables.getSerie().TNAME+opt+"."+tables.getSerie().getId_acqdate()+" as "+tables.getSerie().getId_acqdate()+""+customFieldSuffixe+", " +
						""+tables.getSerie().TNAME+opt+"."+tables.getSerie().getId_protocol()+" as "+tables.getSerie().getId_protocol()+""+customFieldSuffixe+", " +
								""+tables.getSerie().TNAME+opt+"."+tables.getSerie().getName()+" as "+tables.getSerie().getName()+""+customFieldSuffixe+" "; 
				break;
			case 3://Protocol
				fieldToAdd = ", "+tables.getProtocol().TNAME+opt+"."+tables.getProtocol().getId_project()+" as "+tables.getProtocol().getId_project()+""+customFieldSuffixe+", " +
						""+tables.getProtocol().TNAME+opt+"."+tables.getProtocol().getId_patient()+" as "+tables.getProtocol().getId_patient()+""+customFieldSuffixe+", " +
								""+tables.getProtocol().TNAME+opt+"."+tables.getProtocol().getId_acqdate()+" as "+tables.getProtocol().getId_acqdate()+""+customFieldSuffixe+", " +
						""+tables.getProtocol().TNAME+opt+"."+tables.getProtocol().getName()+" as "+tables.getProtocol().getName()+""+customFieldSuffixe+" "; 
				break;
			case 4://Acqdate
				fieldToAdd = ", "+tables.getAcquisitionDate().TNAME+opt+"."+tables.getAcquisitionDate().getId_project()+" as "+tables.getAcquisitionDate().getId_project()+""+customFieldSuffixe+", " +
						""+tables.getAcquisitionDate().TNAME+opt+"."+tables.getAcquisitionDate().getId_patient()+" as "+tables.getAcquisitionDate().getId_patient()+""+customFieldSuffixe+", " +
						""+tables.getAcquisitionDate().TNAME+opt+"."+tables.getAcquisitionDate().getDate()+" as "+tables.getAcquisitionDate().getDate()+""+customFieldSuffixe+" "; 
				break;
			case 5://Patient
				fieldToAdd = ", "+tables.getPatient().TNAME+opt+"."+tables.getPatient().getId_project()+" as "+tables.getPatient().getId_project()+""+customFieldSuffixe+", " +
						""+tables.getPatient().TNAME+opt+"."+tables.getPatient().getName()+" as "+tables.getPatient().getName()+""+customFieldSuffixe+" "; 
				break;
			case 6://Project
				fieldToAdd = ", "+tables.getProject().TNAME+opt+"."+tables.getProject().getName()+" as "+tables.getProject().getName()+""+customFieldSuffixe+" "; 
				break;				
		}
		//temp = splitFrom[0] + fieldToAdd;
		String temp  = splitFrom[0] + fieldToAdd;
		for(int i = 0; i < splitFrom.length-1; i++){
			temp = temp +  " from " + splitFrom[i+1];
		}
		nrequest = temp;

		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			rset = stmt.executeQuery(nrequest);

			
			ResultSetMetaData rsmd = rset.getMetaData();
			// on stock les indices des colonnes associe a un label
			// pour eviter les erreur avec 2 colonnes portant le meme nom
			HashMap<String,Integer> indices = new HashMap<String,Integer>();
			for(int i = 1; i <= rsmd.getColumnCount(); i++){
				String name = rsmd.getColumnLabel(i);
				String cname = rsmd.getColumnName(i);
				// on evite d'afficher les champs id etc et les champs rajoute par le logiciel
				if(!isIllegalHeader(cname) && !name.endsWith(customFieldSuffixe)){
					// si le nom de la colonne existe deja on 
					// rajoute le nom de la table pour preciser
					if(resultats.containsKey(name)){
						// on update le champs qu'on a deja rentre
						int ind = indices.get(name);
						// on enleve l'ancienne entree
						indices.remove(name);
						resultats.remove(name);
						indices.put(rsmd.getTableName(ind) +"."+name, ind);
						resultats.put(rsmd.getTableName(ind) +"."+name, new ArrayList<String[]>());
						
						// on change le nom de la colonne courante
						name = rsmd.getTableName(i) +"."+name;
						
					}
					indices.put(name, i);
					resultats.put(name, new ArrayList<String[]>());
				}
			}
			boolean isempty=true;
			while(rset.next() && !stopCurrentRequest){
				isempty = false;
				String[] customFields = null;
				switch(type){
				case 0:
					DicomImageTable dt = tables.getDicomImage();
					customFields = new String[6];
					customFields[0] = rset.getString(dt.getId_project()+customFieldSuffixe);
					customFields[1] = rset.getString(dt.getId_patient()+customFieldSuffixe);
					customFields[2] = rset.getString(dt.getId_acqdate()+customFieldSuffixe);
					customFields[3] = rset.getString(dt.getId_protocol()+customFieldSuffixe);
					customFields[4] = rset.getString(dt.getId_serie()+customFieldSuffixe);
					customFields[5] = rset.getString(dt.getName()+customFieldSuffixe);
					break;
				case 1:
					NiftiImageTable nt = tables.getNiftiImage();
					customFields = new String[6];
					customFields[0] = rset.getString(nt.getId_project()+customFieldSuffixe);
					customFields[1] = rset.getString(nt.getId_patient()+customFieldSuffixe);
					customFields[2] = rset.getString(nt.getId_acqdate()+customFieldSuffixe);
					customFields[3] = rset.getString(nt.getId_protocol()+customFieldSuffixe);
					customFields[4] = rset.getString(nt.getId_serie()+customFieldSuffixe);
					customFields[5] = rset.getString(nt.getName()+customFieldSuffixe);
					break;
				case 2:
					SerieTable st = tables.getSerie();
					customFields = new String[5];
					customFields[0] = rset.getString(st.getId_project()+customFieldSuffixe);
					customFields[1] = rset.getString(st.getId_patient()+customFieldSuffixe);
					customFields[2] = rset.getString(st.getId_acqdate()+customFieldSuffixe);
					customFields[3] = rset.getString(st.getId_protocol()+customFieldSuffixe);
					customFields[4] = rset.getString(st.getName()+customFieldSuffixe);
					break;
				case 3:
					ProtocolTable pt = tables.getProtocol();
					customFields = new String[4];
					customFields[0] = rset.getString(pt.getId_project()+customFieldSuffixe);
					customFields[1] = rset.getString(pt.getId_patient()+customFieldSuffixe);
					customFields[2] = rset.getString(pt.getId_acqdate()+customFieldSuffixe);
					customFields[3] = rset.getString(pt.getName()+customFieldSuffixe);
					break;
				case 4:
					AcquisitionDateTable at = tables.getAcquisitionDate();
					customFields = new String[3];
					customFields[0] = rset.getString(at.getId_project()+customFieldSuffixe);
					customFields[1] = rset.getString(at.getId_patient()+customFieldSuffixe);
					customFields[2] = rset.getString(at.getDate()+customFieldSuffixe);
					break;
				case 5:
					PatientTable ppt = tables.getPatient();
					customFields = new String[2];
					customFields[0] = rset.getString(ppt.getId_project()+customFieldSuffixe);
					customFields[1] = rset.getString(ppt.getName()+customFieldSuffixe);
					break;
				case 6:
					ProjectTable prt = tables.getProject();
					customFields = new String[1];
					customFields[0] = rset.getString(prt.getName()+customFieldSuffixe);
					break;
				}
				// si on est bien sur une requete portant sur les donnees
				if(type>=0 && type<=6){
					File file = buildPathFromIdList(customFields,type);
					if(file!=null){
						for(String n:indices.keySet()){
							resultats.get(n).add(new String[]{rset.getString(indices.get(n)),file.getAbsolutePath()});
						}
					}
				}else{
					for(String n:indices.keySet()){
						resultats.get(n).add(new String[]{rset.getString(indices.get(n)),null});
					}
				}
				
			}
		
			if(stopCurrentRequest)
				stopCurrentRequest = false;
			if(isempty){
				resultats.clear();
				indices.clear();
			}
			return resultats;
		
		} catch (SQLException e) {
			System.out.println("SQL Error : "+e.toString());
			throw e;
		} finally {
			try{
				try { if(rset!=null) rset.close();  } catch (Exception e) {};
				try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
				try { if(connection!=null) connection.close();  } catch (Exception e) {};
			}catch(Exception e){};
		}
	}

	/**
	 * Envoie des requetes (si donnees ne sont pas en cache)
	 * pour trouver les nom correspondant aux id donnees 
	 * @param customFields
	 * @param type
	 * @param cache cache sur la db
	 * @return
	 * @throws SQLException 
	 */
	public File buildPathFromIdList(String[] customFields, int type) throws SQLException {
		// On cree un cache pour eviter un trop gros nombre de requetes SQL (pr retrouver un nom qu'on connait deja)
		DBCache cache = SystemSettings.SERVER_INFO.getDbCache();
		DBTables tables = SQLSettings.TABLES;
		String selectClause = "select ";
		String fromClause = " from ";
		String whereClause = " where ";
		String path = "";
		String project="";
		String patient="";
		String acqdate = "";
		String protocol = "";
		String serie = "";
		String name = "";
		switch(type){
		case 0:
			name = customFields[customFields.length-1];
			if(cache.getIdToProjectNameList().containsKey(Integer.parseInt(customFields[0])) && cache.getIdToPatientNameList().containsKey(Integer.parseInt(customFields[1])) && 
					cache.getIdToAcqDateNameList().containsKey(Integer.parseInt(customFields[2])) && cache.getIdToProtocolNameList().containsKey(Integer.parseInt(customFields[3])) &&
					cache.getIdToSerieNameList().containsKey(Integer.parseInt(customFields[4])) ){
				return new File(SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_DICOM_NAME+"/" +
						""+cache.getIdToProjectNameList().get(Integer.parseInt(customFields[0]))+"/"+cache.getIdToPatientNameList().get(Integer.parseInt(customFields[1]))+"/"
						+cache.getIdToAcqDateNameList().get(Integer.parseInt(customFields[2]))+"/"+cache.getIdToProtocolNameList().get(Integer.parseInt(customFields[3]))+"/" +
								""+cache.getIdToSerieNameList().get(Integer.parseInt(customFields[4]))+"/"+name);
			}else{
				path += SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_DICOM_NAME;
				if(!cache.getIdToProjectNameList().containsKey(Integer.parseInt(customFields[0]))){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getProject().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getProject().getName()+" as item0 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getProject().getId()+"="+customFields[0]+" ";
				}else{
					project = cache.getIdToProjectNameList().get(customFields[0]);
				}
				if(!cache.getIdToPatientNameList().containsKey(Integer.parseInt(customFields[1]))){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getPatient().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getPatient().getName()+" as item1 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getPatient().getId()+"="+customFields[1]+" ";
				}else{
					patient = cache.getIdToPatientNameList().get(customFields[1]);
				}
				if(!cache.getIdToAcqDateNameList().containsKey(Integer.parseInt(customFields[2]))){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getAcquisitionDate().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getAcquisitionDate().getDate()+" as item2 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getAcquisitionDate().getId()+"="+customFields[2]+" ";
				}else{
					acqdate = cache.getIdToAcqDateNameList().get(customFields[2]);
				}
				if(!cache.getIdToProtocolNameList().containsKey(Integer.parseInt(customFields[3]))){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getProtocol().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getProtocol().getName()+" as item3 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getProtocol().getId()+"="+customFields[3]+" ";
				}else{
					protocol = cache.getIdToProtocolNameList().get(customFields[3]);
				}
				if(!cache.getIdToSerieNameList().containsKey(Integer.parseInt(customFields[4]))){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getSerie().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getSerie().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getSerie().getName()+" as item4 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getSerie().getId()+"="+customFields[4]+" ";
				}else{
					serie = cache.getIdToSerieNameList().get(customFields[4]);
				}
				ResultSet rset = null;
				Statement stmt = null;
				Connection connection = null;
				try {
					connection = SQLSettings.getPDS().getConnection();
					stmt = connection.createStatement();
					rset = stmt.executeQuery(selectClause+" "+fromClause+" "+whereClause);
					if(!rset.next())
						return null;
					ResultSetMetaData rsmd = rset.getMetaData();
					for(int i = 1;i<=rsmd.getColumnCount();i++){
						String cname = rsmd.getColumnLabel(i);
						switch(cname){
							case "item0":
								project = rset.getString(i);
								cache.getIdToProjectNameList().put(Integer.parseInt(customFields[0]), project);break;
							case "item1":
								patient = rset.getString(i);
								cache.getIdToPatientNameList().put(Integer.parseInt(customFields[1]), patient);break;
							case "item2":
								acqdate = AcquisitionDate.yyyy_MM_dd_To_yyyyMMdd(rset.getString(i));
								cache.getIdToAcqDateNameList().put(Integer.parseInt(customFields[2]), acqdate);break;
							case "item3":
								protocol = rset.getString(i);
								cache.getIdToProtocolNameList().put(Integer.parseInt(customFields[3]), protocol);break;
							case "item4":
								serie = rset.getString(i);
								cache.getIdToSerieNameList().put(Integer.parseInt(customFields[4]), serie);break;
						}
					}
					return new File(path+"/"+project+"/"+patient+"/"+acqdate+"/"+protocol+"/"+serie+"/"+name);
				} catch (SQLException e) {
					e.printStackTrace();
					throw e;
				} finally {
					try { if(rset!=null) rset.close();  } catch (Exception e) {};
					try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
					try { if(connection!=null) connection.close();  } catch (Exception e) {};
				}
			}
		case 1:
			name = customFields[customFields.length-1];
			if(cache.getIdToProjectNameList().containsKey(Integer.parseInt(customFields[0])) && cache.getIdToPatientNameList().containsKey(Integer.parseInt(customFields[1])) && 
					cache.getIdToAcqDateNameList().containsKey(Integer.parseInt(customFields[2])) && cache.getIdToProtocolNameList().containsKey(Integer.parseInt(customFields[3])) &&
					cache.getIdToSerieNameList().containsKey(Integer.parseInt(customFields[4])) ){
				return new File(SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME+"/" +
						""+cache.getIdToProjectNameList().get(Integer.parseInt(customFields[0]))+"/"+cache.getIdToPatientNameList().get(Integer.parseInt(customFields[1]))+"/"
						+cache.getIdToAcqDateNameList().get(Integer.parseInt(customFields[2]))+"/"+cache.getIdToProtocolNameList().get(Integer.parseInt(customFields[3]))+"/" +
								""+cache.getIdToSerieNameList().get(Integer.parseInt(customFields[4]))+"/"+name);
			}else{
				path += SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME;
				if(!cache.getIdToProjectNameList().containsKey(customFields[0])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getProject().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getProject().getName()+" as item0 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getProject().getId()+"="+customFields[0]+" ";
				}else{
					project = cache.getIdToProjectNameList().get(customFields[0]);
				}
				if(!cache.getIdToPatientNameList().containsKey(customFields[1])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getPatient().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getPatient().getName()+" as item1 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getPatient().getId()+"="+customFields[1]+" ";
				}else{
					patient = cache.getIdToPatientNameList().get(customFields[1]);
				}
				if(!cache.getIdToAcqDateNameList().containsKey(customFields[2])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getAcquisitionDate().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getAcquisitionDate().getDate()+" as item2 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getAcquisitionDate().getId()+"="+customFields[2]+" ";
				}else{
					acqdate = cache.getIdToAcqDateNameList().get(customFields[2]);
				}
				if(!cache.getIdToProtocolNameList().containsKey(customFields[3])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getProtocol().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getProtocol().getName()+" as item3 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getProtocol().getId()+"="+customFields[3]+" ";
				}else{
					protocol = cache.getIdToProtocolNameList().get(customFields[3]);
				}
				if(!cache.getIdToSerieNameList().containsKey(customFields[4])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getSerie().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getSerie().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getSerie().getName()+" as item4 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getSerie().getId()+"="+customFields[4]+" ";
				}else{
					serie = cache.getIdToSerieNameList().get(customFields[4]);
				}
				ResultSet rset = null;
				Statement stmt = null;
				Connection connection = null;
				try {
					connection = SQLSettings.getPDS().getConnection();
					stmt = connection.createStatement();
					rset = stmt.executeQuery(selectClause+" "+fromClause+" "+whereClause);
					if(!rset.next())
						return null;
					ResultSetMetaData rsmd = rset.getMetaData();
					for(int i = 1;i<=rsmd.getColumnCount();i++){
						String cname = rsmd.getColumnLabel(i);
						switch(cname){
							case "item0":
								project = rset.getString(i);
								cache.getIdToProjectNameList().put(Integer.parseInt(customFields[0]), project);break;
							case "item1":
								patient = rset.getString(i);
								cache.getIdToPatientNameList().put(Integer.parseInt(customFields[1]), patient);break;
							case "item2":
								acqdate = AcquisitionDate.yyyy_MM_dd_To_yyyyMMdd(rset.getString(i));
								cache.getIdToAcqDateNameList().put(Integer.parseInt(customFields[2]), acqdate);break;
							case "item3":
								protocol = rset.getString(i);
								cache.getIdToProtocolNameList().put(Integer.parseInt(customFields[3]), protocol);break;
							case "item4":
								serie = rset.getString(i);
								cache.getIdToSerieNameList().put(Integer.parseInt(customFields[4]), serie);break;
						}
					}
					return new File(path+"/"+project+"/"+patient+"/"+acqdate+"/"+protocol+"/"+serie+"/"+name);
				} catch (SQLException e) {
					e.printStackTrace();
					throw e;
				} finally {
					try { if(rset!=null) rset.close();  } catch (Exception e) {};
					try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
					try { if(connection!=null) connection.close();  } catch (Exception e) {};
				}
			}
		case 2:// par defaut repertoire analyze
			name = customFields[customFields.length-1];
			if(cache.getIdToProjectNameList().containsKey(Integer.parseInt(customFields[0])) && cache.getIdToPatientNameList().containsKey(Integer.parseInt(customFields[1])) && 
					cache.getIdToAcqDateNameList().containsKey(Integer.parseInt(customFields[2])) && cache.getIdToProtocolNameList().containsKey(Integer.parseInt(customFields[3])) ){
				return new File(SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME+"/" +
						""+cache.getIdToProjectNameList().get(Integer.parseInt(customFields[0]))+"/"+cache.getIdToPatientNameList().get(Integer.parseInt(customFields[1]))+"/"
						+cache.getIdToAcqDateNameList().get(Integer.parseInt(customFields[2]))+"/"+cache.getIdToProtocolNameList().get(Integer.parseInt(customFields[3]))+"/" +name);
			}else{
				path += SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME;
				if(!cache.getIdToProjectNameList().containsKey(customFields[0])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getProject().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getProject().getName()+" as item0 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getProject().getId()+"="+customFields[0]+" ";
				}else{
					project = cache.getIdToProjectNameList().get(customFields[0]);
				}
				if(!cache.getIdToPatientNameList().containsKey(customFields[1])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getPatient().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getPatient().getName()+" as item1 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getPatient().getId()+"="+customFields[1]+" ";
				}else{
					patient = cache.getIdToPatientNameList().get(customFields[1]);
				}
				if(!cache.getIdToAcqDateNameList().containsKey(customFields[2])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getAcquisitionDate().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getAcquisitionDate().getDate()+" as item2 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getAcquisitionDate().getId()+"="+customFields[2]+" ";
				}else{
					acqdate = cache.getIdToAcqDateNameList().get(customFields[2]);
				}
				if(!cache.getIdToProtocolNameList().containsKey(customFields[3])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getProtocol().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getProtocol().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getProtocol().getName()+" as item3 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getProtocol().getId()+"="+customFields[3]+" ";
				}else{
					protocol = cache.getIdToProtocolNameList().get(customFields[3]);
				}
				ResultSet rset = null;
				Statement stmt = null;
				Connection connection = null;
				try {
					connection = SQLSettings.getPDS().getConnection();
					stmt = connection.createStatement();
					rset = stmt.executeQuery(selectClause+" "+fromClause+" "+whereClause);
					if(!rset.next())
						return null;
					ResultSetMetaData rsmd = rset.getMetaData();
					for(int i = 1;i<=rsmd.getColumnCount();i++){
						String cname = rsmd.getColumnLabel(i);
						switch(cname){
							case "item0":
								project = rset.getString(i);
								cache.getIdToProjectNameList().put(Integer.parseInt(customFields[0]), project);break;
							case "item1":
								patient = rset.getString(i);
								cache.getIdToPatientNameList().put(Integer.parseInt(customFields[1]), patient);break;
							case "item2":
								acqdate = AcquisitionDate.yyyy_MM_dd_To_yyyyMMdd(rset.getString(i));
								cache.getIdToAcqDateNameList().put(Integer.parseInt(customFields[2]), acqdate);break;
							case "item3":
								protocol = rset.getString(i);
								cache.getIdToProtocolNameList().put(Integer.parseInt(customFields[3]), protocol);break;
						}
					}
					return new File(path+"/"+project+"/"+patient+"/"+acqdate+"/"+protocol+"/"+name);
				} catch (SQLException e) {
					e.printStackTrace();
					throw e;
				} finally {
					try { if(rset!=null) rset.close();  } catch (Exception e) {};
					try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
					try { if(connection!=null) connection.close();  } catch (Exception e) {};
				}
			}
		case 3:
			name = customFields[customFields.length-1];
			if(cache.getIdToProjectNameList().containsKey(Integer.parseInt(customFields[0])) && cache.getIdToPatientNameList().containsKey(Integer.parseInt(customFields[1])) && 
					cache.getIdToAcqDateNameList().containsKey(Integer.parseInt(customFields[2])) ){
				return new File(SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME+"/" +
						""+cache.getIdToProjectNameList().get(Integer.parseInt(customFields[0]))+"/"+cache.getIdToPatientNameList().get(Integer.parseInt(customFields[1]))+"/"
						+cache.getIdToAcqDateNameList().get(Integer.parseInt(customFields[2]))+"/"+name);
			}else{
				path += SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME;
				if(!cache.getIdToProjectNameList().containsKey(customFields[0])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getProject().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getProject().getName()+" as item0 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getProject().getId()+"="+customFields[0]+" ";
				}else{
					project = cache.getIdToProjectNameList().get(customFields[0]);
				}
				if(!cache.getIdToPatientNameList().containsKey(customFields[1])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getPatient().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getPatient().getName()+" as item1 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getPatient().getId()+"="+customFields[1]+" ";
				}else{
					patient = cache.getIdToPatientNameList().get(customFields[1]);
				}
				if(!cache.getIdToAcqDateNameList().containsKey(customFields[2])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getAcquisitionDate().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getAcquisitionDate().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getAcquisitionDate().getDate()+" as item2 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getAcquisitionDate().getId()+"="+customFields[2]+" ";
				}else{
					acqdate = cache.getIdToAcqDateNameList().get(customFields[2]);
				}
				ResultSet rset = null;
				Statement stmt = null;
				Connection connection = null;
				try {
					connection = SQLSettings.getPDS().getConnection();
					stmt = connection.createStatement();
					rset = stmt.executeQuery(selectClause+" "+fromClause+" "+whereClause);
					if(!rset.next())
						return null;
					ResultSetMetaData rsmd = rset.getMetaData();
					for(int i = 1;i<=rsmd.getColumnCount();i++){
						String cname = rsmd.getColumnLabel(i);
						switch(cname){
							case "item0":
								project = rset.getString(i);
								cache.getIdToProjectNameList().put(Integer.parseInt(customFields[0]), project);break;
							case "item1":
								patient = rset.getString(i);
								cache.getIdToPatientNameList().put(Integer.parseInt(customFields[1]), patient);break;
							case "item2":
								acqdate = AcquisitionDate.yyyy_MM_dd_To_yyyyMMdd(rset.getString(i));
								cache.getIdToAcqDateNameList().put(Integer.parseInt(customFields[2]), acqdate);break;
						}
					}
					return new File(path+"/"+project+"/"+patient+"/"+acqdate+"/"+name);
				} catch (SQLException e) {
					e.printStackTrace();
					throw e;
				} finally {
					try { if(rset!=null) rset.close();  } catch (Exception e) {};
					try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
					try { if(connection!=null) connection.close();  } catch (Exception e) {};
				}
			}
		case 4:
			name = customFields[customFields.length-1];
			if(cache.getIdToProjectNameList().containsKey(Integer.parseInt(customFields[0])) && cache.getIdToPatientNameList().containsKey(Integer.parseInt(customFields[1])) ){
				return new File(SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME+"/" +
						""+cache.getIdToProjectNameList().get(Integer.parseInt(customFields[0]))+"/"+cache.getIdToPatientNameList().get(Integer.parseInt(customFields[1]))+"/"+name);
			}else{
				path += SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME;
				if(!cache.getIdToProjectNameList().containsKey(customFields[0])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getProject().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getProject().getName()+" as item0 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getProject().getId()+"="+customFields[0]+" ";
				}else{
					project = cache.getIdToProjectNameList().get(customFields[0]);
				}
				if(!cache.getIdToPatientNameList().containsKey(customFields[1])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getPatient().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getPatient().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getPatient().getName()+" as item1 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getPatient().getId()+"="+customFields[1]+" ";
				}else{
					patient = cache.getIdToPatientNameList().get(customFields[1]);
				}
				ResultSet rset = null;
				Statement stmt = null;
				Connection connection = null;
				try {
					connection = SQLSettings.getPDS().getConnection();
					stmt = connection.createStatement();
					rset = stmt.executeQuery(selectClause+" "+fromClause+" "+whereClause);
					if(!rset.next())
						return null;
					ResultSetMetaData rsmd = rset.getMetaData();
					for(int i = 1;i<=rsmd.getColumnCount();i++){
						String cname = rsmd.getColumnLabel(i);
						switch(cname){
							case "item0":
								project = rset.getString(i);
								cache.getIdToProjectNameList().put(Integer.parseInt(customFields[0]), project);break;
							case "item1":
								patient = rset.getString(i);
								cache.getIdToPatientNameList().put(Integer.parseInt(customFields[1]), patient);break;
						}
					}
					return new File(path+"/"+project+"/"+patient+"/"+name);
				} catch (SQLException e) {
					e.printStackTrace();
					throw e;
				} finally {
					try { if(rset!=null) rset.close();  } catch (Exception e) {};
					try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
					try { if(connection!=null) connection.close();  } catch (Exception e) {};
				}
			}
		case 5:
			name = customFields[customFields.length-1];
			if(cache.getIdToProjectNameList().containsKey(Integer.parseInt(customFields[0])) ){
				return new File(SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME+"/" +
						""+cache.getIdToProjectNameList().get(Integer.parseInt(customFields[0]))+"/"+name);
			}else{
				path += SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME;
				if(!cache.getIdToProjectNameList().containsKey(customFields[0])){
					if(!selectClause.equals("select "))
						selectClause += " , ";
					String tname = tables.getProject().TNAME;
					if(UserProfile.CURRENT_USER.getLevel()!=3)
						tname = tables.getProject().TNAME+"_"+UserProfile.CURRENT_USER.getId();
					selectClause += " "+tname+"."+tables.getProject().getName()+" as item0 ";
					if(!fromClause.equals(" from "))
						fromClause += " , ";
					fromClause += " "+tname+" ";
					if(!whereClause.equals(" where "))
						whereClause += " and ";
					whereClause += " "+tname+"."+tables.getProject().getId()+"="+customFields[0]+" ";
				}else{
					project = cache.getIdToProjectNameList().get(customFields[0]);
				}
				ResultSet rset = null;
				Statement stmt = null;
				Connection connection = null;
				try {
					connection = SQLSettings.getPDS().getConnection();
					stmt = connection.createStatement();
					rset = stmt.executeQuery(selectClause+" "+fromClause+" "+whereClause);
					if(!rset.next())
						return null;
					ResultSetMetaData rsmd = rset.getMetaData();
					for(int i = 1;i<=rsmd.getColumnCount();i++){
						String cname = rsmd.getColumnLabel(i);
						switch(cname){
							case "item0":
								project = rset.getString(i);
								cache.getIdToProjectNameList().put(Integer.parseInt(customFields[0]), project);break;
						}
					}
					return new File(path+"/"+project+"/"+name);
				} catch (SQLException e) {
					e.printStackTrace();
					throw e;
				} finally {
					try { if(rset!=null) rset.close();  } catch (Exception e) {};
					try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
					try { if(connection!=null) connection.close();  } catch (Exception e) {};
				}
			}
		case 6 :
			name = customFields[customFields.length-1];
			return new File(SystemSettings.SERVER_INFO.getServerDir()+"/"+SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME+"/" +
						""+name);
		}
		return null;
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
		
		if(splitFrom.length==2){
			for(int i=1;i<splitFrom.length;i++){
				rightpart += " "+splitFrom[i];
			}
		}else{
			for(int i=1;i<splitFrom.length-1;i++){
				rightpart += " "+splitFrom[i]+ " from "+splitFrom[i+1];
			}
		}
		for(String view:SQLSettings.LOCAL_VIEW_NAME){
			// on rajoute les "_num" dans les nom de table
			rightpart = rightpart.replaceAll(" "+view+" ", " "+view+"_"+UserProfile.CURRENT_USER.getId()+" ");
			rightpart = rightpart.replaceAll(" "+view+",", " "+view+"_"+UserProfile.CURRENT_USER.getId()+",");
			rightpart = rightpart.replaceAll(" "+view+"\\.", " "+view+"_"+UserProfile.CURRENT_USER.getId()+".");
			rightpart = rightpart.replaceAll(","+view+"\\.", ","+view+"_"+UserProfile.CURRENT_USER.getId()+".");
			rightpart = rightpart.replaceAll(" "+view+"$", view+"_"+UserProfile.CURRENT_USER.getId());
			rightpart = rightpart.replaceAll(","+view+"$", ","+view+"_"+UserProfile.CURRENT_USER.getId());
			rightpart = rightpart.replaceAll(","+view+" ", ","+view+"_"+UserProfile.CURRENT_USER.getId()+" ");	
			rightpart = rightpart.replaceAll("="+view+"$", "="+view+"_"+UserProfile.CURRENT_USER.getId());
			rightpart = rightpart.replaceAll("="+view+"\\.", "="+view+"_"+UserProfile.CURRENT_USER.getId()+".");
			rightpart = rightpart.replaceAll("="+view+",", "="+view+"_"+UserProfile.CURRENT_USER.getId()+",");
			rightpart = rightpart.replaceAll("="+view+" ", "="+view+"_"+UserProfile.CURRENT_USER.getId()+" ");
			rightpart = rightpart.replaceAll("\\("+view+"\\)", "("+view+"_"+UserProfile.CURRENT_USER.getId()+")");
			rightpart = rightpart.replaceAll("\\("+view+"\\.", "("+view+"_"+UserProfile.CURRENT_USER.getId()+".");
		}
		for(String view:SQLSettings.LOCAL_VIEW_NAME){
			// on rajoute les "_num" dans les nom de table

			nrequest = nrequest.replaceAll(" "+view+" ", " "+view+"_"+UserProfile.CURRENT_USER.getId()+" ");
			nrequest = nrequest.replaceAll(" "+view+",", " "+view+"_"+UserProfile.CURRENT_USER.getId()+",");
			nrequest = nrequest.replaceAll(" "+view+"\\.", " "+view+"_"+UserProfile.CURRENT_USER.getId()+".");
			nrequest = nrequest.replaceAll(","+view+"\\.", ","+view+"_"+UserProfile.CURRENT_USER.getId()+".");
			nrequest = nrequest.replaceAll(" "+view+"$", view+"_"+UserProfile.CURRENT_USER.getId());
			nrequest = nrequest.replaceAll(","+view+"$", ","+view+"_"+UserProfile.CURRENT_USER.getId());
			nrequest = nrequest.replaceAll(","+view+" ", ","+view+"_"+UserProfile.CURRENT_USER.getId()+" ");
			nrequest = nrequest.replaceAll("="+view+"$", "="+view+"_"+UserProfile.CURRENT_USER.getId());
			nrequest = nrequest.replaceAll("="+view+"\\.", "="+view+"_"+UserProfile.CURRENT_USER.getId()+".");
			nrequest = nrequest.replaceAll("="+view+",", "="+view+"_"+UserProfile.CURRENT_USER.getId()+",");
			nrequest = nrequest.replaceAll("="+view+" ", "="+view+"_"+UserProfile.CURRENT_USER.getId()+" ");
			nrequest = nrequest.replaceAll("\\("+view+"\\)", "("+view+"_"+UserProfile.CURRENT_USER.getId()+")");
			nrequest = nrequest.replaceAll("\\("+view+"\\.", "("+view+"_"+UserProfile.CURRENT_USER.getId()+".");
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

	/**
	 * Permet de gerer la requete du requestpanel
	 * @throws SQLException 
	 */
	@Override
	public HashMap<String,ArrayList<String[]>> executeFromRequestPanel(String project, String patient,
			String protocol, String serie, String begin, String end,
			IMAGE_TYPE imagetype) throws SQLException {
		stopCurrentRequest = false;
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			DBTables tab = SQLSettings.TABLES;
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();
			String select = "select ";
			String from = " from ";
			String where = " where ";
			String opt = "";// chaine de caractere optionnelle pour gerer les vues utilisateurs
			String whereopt = " "; // devient " and " quand on a commence a remplir les donnees
			if(project==null)
				project = "";
			if(UserProfile.CURRENT_USER.getLevel()!=3)
				opt="_"+UserProfile.CURRENT_USER.getId();	

			// ----------  project --------------
			if(select.equals("select "))
				select += tab.getProject().TNAME+opt+"."+tab.getProject().getName()+" as "+tab.getProject().TNAME;
			else
				select += ", "+tab.getProject().TNAME+opt+"."+tab.getProject().getName()+" as "+tab.getProject();

			from += tab.getProject().TNAME+opt;
			if(!project.equals("")){
				if(where.equals(" where "))
					where += tab.getProject().TNAME+opt+"."+tab.getProject().getName()+" = '"+project+"'";
				else
					where += " and "+tab.getProject().TNAME+opt+"."+tab.getProject().getName()+" = '"+project+"'";
				whereopt = " and ";
			}

			// ----------  patient --------------
			select += ", "+tab.getPatient().TNAME+opt+"."+tab.getPatient().getName()+" as "+tab.getPatient().TNAME;
			from += ", "+tab.getPatient().TNAME+opt;
			if(!patient.equals("")){
				if(where.equals(" where "))
					where += tab.getPatient().TNAME+opt+"."+tab.getPatient().getName()+" regexp '"+patient+"'";
				else
					where += " and "+tab.getPatient().TNAME+opt+"."+tab.getPatient().getName()+" regexp '"+patient+"'";
				whereopt = " and ";
			}
			
			// ----------  AcqDate --------------
			
			
			select += ", "+tab.getAcquisitionDate().TNAME+opt+"."+tab.getAcquisitionDate().getDate()+" as "+tab.getAcquisitionDate().TNAME;
			from += ", "+tab.getAcquisitionDate().TNAME+opt;
			if(!begin.equals("")){
				if(where.equals(" where "))
					where += tab.getAcquisitionDate().TNAME+opt+"."+tab.getAcquisitionDate().getDate()+">="+begin;
				else
					where += " and "+tab.getAcquisitionDate().TNAME+opt+"."+tab.getAcquisitionDate().getDate()+">="+begin;
				whereopt = " and ";
			}
			if(!end.equals("")){
				if(where.equals(" where "))
					where += tab.getAcquisitionDate().TNAME+opt+"."+tab.getAcquisitionDate().getDate()+"<="+end;
				else
					where += " and "+tab.getAcquisitionDate().TNAME+opt+"."+tab.getAcquisitionDate().getDate()+"<="+end;
				whereopt = " and ";
			}			
			// ----------  protocol --------------
			
			select += ", "+tab.getProtocol().TNAME+opt+"."+tab.getProtocol().getName()+" as "+tab.getProtocol().TNAME;
			from += ", "+tab.getProtocol().TNAME+opt;
			if(!protocol.equals("")){
				if(where.equals(" where "))
					where += tab.getProtocol().TNAME+opt+"."+tab.getProtocol().getName()+" regexp '"+protocol+"'";
				else
					where += " and "+tab.getProtocol().TNAME+opt+"."+tab.getProtocol().getName()+" regexp '"+protocol+"'";
				whereopt = " and ";
			}
			
			// ----------  serie --------------

			select += ", "+tab.getSerie().TNAME+opt+"."+tab.getSerie().getName()+" as "+tab.getSerie().TNAME;
			from += ", "+tab.getSerie().TNAME+opt;
			if(!serie.equals("")){
				if(where.equals(" where "))
					where += tab.getSerie().TNAME+opt+"."+tab.getSerie().getName()+" regexp '"+serie+"'";
				else
					where += " and "+tab.getSerie().TNAME+opt+"."+tab.getSerie().getName()+" regexp '"+serie+"'";
				whereopt = " and ";
			}
			switch(imagetype){
			case DICOM:
				where += whereopt+tab.getProject().TNAME+opt+"."+tab.getProject().getId()+"="+tab.getSerie().TNAME+opt+"."+tab.getSerie().getId_project()+" and " +
						tab.getPatient().TNAME+opt+"."+tab.getPatient().getId()+"="+tab.getSerie().TNAME+opt+"."+tab.getSerie().getId_patient()+" and " +
						tab.getAcquisitionDate().TNAME+opt+"."+tab.getAcquisitionDate().getId()+"="+tab.getSerie().TNAME+opt+"."+tab.getSerie().getId_acqdate()+" and " +
						tab.getProtocol().TNAME+opt+"."+tab.getProtocol().getId()+"="+tab.getSerie().TNAME+opt+"."+tab.getSerie().getId_protocol();
				break;
			case NIFTI:
				where += whereopt+tab.getProject().TNAME+opt+"."+tab.getProject().getId()+"="+tab.getSerie().TNAME+opt+"."+tab.getSerie().getId_project()+" and " +
						tab.getPatient().TNAME+opt+"."+tab.getPatient().getId()+"="+tab.getSerie().TNAME+opt+"."+tab.getSerie().getId_patient()+" and " +
						tab.getAcquisitionDate().TNAME+opt+"."+tab.getAcquisitionDate().getId()+"="+tab.getSerie().TNAME+opt+"."+tab.getSerie().getId_acqdate()+" and " +
						tab.getProtocol().TNAME+opt+"."+tab.getProtocol().getId()+"="+tab.getSerie().TNAME+opt+"."+tab.getSerie().getId_protocol()+" and " +
						tab.getSerie().TNAME+opt+"."+tab.getSerie().getHasnifti()+"=1";
				break;
			}
			
			// ----------- REQUETE -------------

			rset = stmt.executeQuery(select+" "+from+" "+where);

			
			ResultSetMetaData rsmd = rset.getMetaData();
			// on stock les indices des colonnes associe a un label
			// pour eviter les erreur avec 2 colonnes portant le meme nom
			HashMap<String,Integer> indices = new HashMap<String,Integer>();
			HashMap<String,ArrayList<String[]>> resultats = new HashMap<String,ArrayList<String[]>>();
			for(int i = 1; i <= rsmd.getColumnCount(); i++){
				String name = rsmd.getColumnLabel(i);
				String cname = rsmd.getColumnName(i);

				indices.put(name, i);
				resultats.put(name, new ArrayList<String[]>());
			}
			boolean isempty=true;
			Set<String> res = new HashSet<String>();//permet d'eviter les resultats doublons 
			while(rset.next() && !stopCurrentRequest){
				isempty = false;
				File file = null;
				String projname = rset.getString(tab.getProject().TNAME);
				String patname = rset.getString(tab.getPatient().TNAME);
				String date = AcquisitionDate.yyyy_MM_dd_To_yyyyMMdd(rset.getString(tab.getAcquisitionDate().TNAME));
				String protname = rset.getString(tab.getProtocol().TNAME);
				String sname = rset.getString(tab.getSerie().TNAME);
				switch(imagetype){
				case DICOM:
					file = new File(SystemSettings.SERVER_INFO.getServerDir()+"/"+ServerInfo.NRI_DICOM_NAME+"/"+projname+"/"+patname+"/"+date+"/" +
							""+protname+"/"+sname);break;
				case NIFTI:
					file = new File(SystemSettings.SERVER_INFO.getServerDir()+"/"+ServerInfo.NRI_ANALYSE_NAME+"/"+projname+"/"+patname+"/"+date+"/" +
							""+protname+"/"+sname);break;
				}
				
				if(!res.contains(file.getAbsolutePath())){
					res.add(file.getAbsolutePath());
					for(String n:indices.keySet()){
						resultats.get(n).add(new String[]{rset.getString(indices.get(n)),file.getAbsolutePath()});
					}
				}

				
			}
			if(stopCurrentRequest)
				stopCurrentRequest = false;
			if(isempty)
				resultats.clear();
			return resultats;
		}catch(SQLException e){
			throw e;
		}finally{
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
	}

	public void setStopCurrentRequest(boolean b) {
		stopCurrentRequest = b;
	}
	
	/**
	 * Envoi une requete fictive simple vers la bdd
	 * dans l'unique but d'assurer le maintient de la connection
	 * avec celle ci
	 */
	public void maintainConnection() throws SQLException{
		
		ResultSet rset = null;
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = SQLSettings.getPDS().getConnection();
			stmt = connection.createStatement();

			rset = stmt.executeQuery("select password('na');");
		
		}catch(Exception e){
			throw e;
		}finally {
			try { if(rset!=null) rset.close();  } catch (Exception e) {};
			try { if(stmt!=null) stmt.close();  } catch (Exception e) {};
			try { if(connection!=null) connection.close();  } catch (Exception e) {};
		}
		
	}
}

