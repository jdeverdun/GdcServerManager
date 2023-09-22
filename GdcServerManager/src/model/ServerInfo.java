package model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.WindowManager;

import daemon.DBCache;
import daemon.DicomNode;

public class ServerInfo {

	public static final String NRI_DICOM_NAME = "NRI-DICOM";
	public static final String NRI_ANALYSE_NAME = "NRI-ANALYSE";
	public static final String WORKSPACE_PREFIXE = "NRI-"; // prefixe des workspace "NRI-" ex : "NRI-CRESCENDO"
	public static final String BACKUP_DIR = "cache"; // Repertoire ou on stock les daemons (fichiers de sauvegarde)
	private static final String INCOMING_DIR_NAME = "incoming";
	private static final String DICOM_DIR_NAME = "dicom";
	private static final String NIFTI_DIR_NAME = "nifti";
	private static final String TEMP_DIR_NAME = "temp";
	private static final String SERVER_DIR_NAME = "server";
	private static final String DICOM_NODE_IP = "dicomNodeIp";
	private static final String DICOM_NODE_PORT = "dicomNodePort";
	private static final String DATABASE_NAME = "databaseName";
	private static final String DICOM_NODE_AETITLE = "dicomNodeAETitle";
	private static final String DATABASE_IP = "databaseIp";
	public static final String CONDOR_JOB_DIR_NAME = "jobs";
	
	// fichier contenant la liste des projets pour lesquels il faut utiliser le patient ID a la place du Patient name
	// le fichier contient un nom de projet par ligne
	private static final String PPID_FILE = "projectsId.conf";

	
	public static String CONF_FILE = "params.conf";
	// Attributs
	private Path incomingDir; // dossier des dicom en vrac
	private Path dicomDir; // dossier des dicom triés
	private Path niftiDir; // dossier des nifti triés
	private Path tempDir; // dossier temporaire, utilise surtout pour la conversion nifti (decryptage fichier)
	private Path serverDir; // dossier racine du serveur
	private DBCache dbCache; // cache de donnees de la bdd
	private HashSet<String> ppid; //set contenant la liste des projets pour lesquels il faut utiliser l'id du patient au lieu du name
	private Path condorJobDir;
	
	// Constructeurs
	public ServerInfo(){
		incomingDir = null;
		dicomDir = null;
		niftiDir = null;
		setCondorJobDir(null);
		setTempDir(null);
		setServerDir(null);
		dbCache = new DBCache();
		try {
			setProjectWithPatientId();
		} catch (IOException e) {
			WindowManager.mwLogger.log(Level.SEVERE,"Could not load "+PPID_FILE,e);
		}
	}
	
	public ServerInfo(String inc, String dicom, String nifti,String temp,String serv){
		setIncomingDir(inc);
		setDicomDir(dicom);
		setNiftiDir(nifti);
		setTempDir(temp);
		setServerDir(serv);
		dbCache = new DBCache();
		try {
			setProjectWithPatientId();
		} catch (IOException e) {
			WindowManager.mwLogger.log(Level.SEVERE,"Could not load "+PPID_FILE,e);
		}
	}
	
	
	
	// Accesseurs
	/**
	 * Cree le fichier serverinfo a partir d'un fichier "params.conf"
	 * present dans le repertoire app_dir
	 * Si il n'est pas present, on cree un params.conf generique
	 * @param app_dir
	 */
	public ServerInfo(Path app_dir) {
		File params_file = new File(app_dir + "/" + CONF_FILE);
		if(params_file.exists()){
			HashMap<String,String> params = getParams(params_file);
			setIncomingDir(params.get(INCOMING_DIR_NAME));
			setDicomDir(params.get(DICOM_DIR_NAME));
			setNiftiDir(params.get(NIFTI_DIR_NAME));
			setTempDir(params.get(TEMP_DIR_NAME));
			setServerDir(params.get(SERVER_DIR_NAME));
			
			if(params.containsKey(DICOM_NODE_IP))
				DicomNode.DEFAULT_HOSTNAME = params.get(DICOM_NODE_IP);
			if(params.containsKey(DICOM_NODE_PORT))
				DicomNode.DEFAULT_PORT = Integer.parseInt(params.get(DICOM_NODE_PORT));
			if(params.containsKey(DICOM_NODE_AETITLE))
				DicomNode.DEFAULT_AE_TITLE = params.get(DICOM_NODE_AETITLE);
			if(params.containsKey(DATABASE_IP))
				SQLSettings.ADDRESS = params.get(DATABASE_IP);
			if(params.containsKey(DATABASE_NAME))
				SQLSettings.DATABASE_NAME = params.get(DATABASE_NAME);
			if(params.containsKey(CONDOR_JOB_DIR_NAME))
				setCondorJobDir(params.get(CONDOR_JOB_DIR_NAME));
			else
				setCondorJobDir(app_dir + File.separator + CONDOR_JOB_DIR_NAME);
			
		}else{
			setIncomingDir(app_dir+ "/" +INCOMING_DIR_NAME);
			setDicomDir(app_dir + "/" +DICOM_DIR_NAME);
			setNiftiDir(app_dir + "/" + NIFTI_DIR_NAME);
			setTempDir(app_dir + "/" + TEMP_DIR_NAME);
			setServerDir(getDicomDir().toString());
			setCondorJobDir(app_dir + File.separator + CONDOR_JOB_DIR_NAME);
			saveConfiguration();
		}
		dbCache = new DBCache();
		try {
			setProjectWithPatientId();
		} catch (IOException e) {
			WindowManager.mwLogger.log(Level.SEVERE,"Could not load "+PPID_FILE,e);
		}
	}



	public Path getIncomingDir() {
		return incomingDir;
	}

	public void setIncomingDir(String incomingDir) {
		if(incomingDir == null)
			incomingDir = INCOMING_DIR_NAME;
		this.incomingDir = (new File(incomingDir)).toPath();
		buildIfNotExist(this.incomingDir);
	}

	public Path getDicomDir() {
		return dicomDir;
	}

	public void setDicomDir(String dicomDir) {
		if(dicomDir == null)
			dicomDir = DICOM_DIR_NAME;
		this.dicomDir = (new File(dicomDir)).toPath();
		buildIfNotExist(this.dicomDir);
	}

	public Path getNiftiDir() {
		return niftiDir;
	}

	public void setNiftiDir(String niftiDir) {
		if(niftiDir == null)
			niftiDir = NIFTI_DIR_NAME;
		this.niftiDir = (new File(niftiDir)).toPath();
		buildIfNotExist(this.niftiDir);
	}

	public HashSet<String> getPpid() {
		return ppid;
	}

	public void setPpid(HashSet<String> ppid) {
		this.ppid = ppid;
	}

	public DBCache getDbCache() {
		return dbCache;
	}

	public void setDbCache(DBCache dbCache) {
		this.dbCache = dbCache;
	}

	public Path getServerDir() {
		return serverDir;
	}
	public void setServerDir(String serv){
		this.serverDir = Paths.get(serv);
		if(WindowManager.MAINWINDOW!=null && WindowManager.MAINWINDOW.getFileTreeDist()!=null){
			WindowManager.MAINWINDOW.getFileTreeDist().switchToDir(serverDir);
		}
		if(WindowManager.MAINWINDOW!=null && WindowManager.MAINWINDOW.getFileTreeWork()!=null){
			WindowManager.MAINWINDOW.getFileTreeWork().switchToDir(serverDir);
		}
	}
	public Path getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		if(tempDir == null)
			tempDir = TEMP_DIR_NAME;
		this.tempDir = (new File(tempDir)).toPath();
		buildIfNotExist(this.tempDir);
	}

	/**
	 * @return the condorJobDir
	 */
	public Path getCondorJobDir() {
		return condorJobDir;
	}

	/**
	 * @param string the condorJobDir to set
	 */
	public void setCondorJobDir(String string) {
		this.condorJobDir = Paths.get(string);
	}

	public void buildIfNotExist(Path p){
		try {
			Files.createDirectories(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Parse params_file et renvoi une hasmap contenant les 
	 * valeurs de chaque parametre ("dir=C:/toto" etc);
	 * @param params_file
	 * @return
	 */
	private HashMap<String, String> getParams(File params_file) {
		HashMap<String,String> params = new HashMap<String,String>();
		try {
			List<String> lines = readSmallTextFile(params_file.toString());
			for(String s:lines){
				if(s.contains("=")){
					String[] elem = s.split("=");
					params.put(elem[0], elem[1]);
				}else{
					WindowManager.mwLogger.log(Level.SEVERE,"Error with params.conf");
				}
			}
		} catch (IOException e) {
			WindowManager.mwLogger.log(Level.SEVERE, "getParams error",e);
		}
		return params;
	}
	
	/**
	 * Sauvegarde le fichier de configuration
	 */
	public void saveConfiguration(){
		List<String> lines = new ArrayList<String>();
		lines.add(INCOMING_DIR_NAME+"="+getIncomingDir());
		lines.add(DICOM_DIR_NAME+"="+getDicomDir());
		lines.add(NIFTI_DIR_NAME+"="+getNiftiDir());
		lines.add(TEMP_DIR_NAME+"="+getTempDir());
		lines.add(SERVER_DIR_NAME+"="+getServerDir());
		lines.add(DICOM_NODE_IP+"="+DicomNode.DEFAULT_HOSTNAME);
		lines.add(DICOM_NODE_PORT+"="+DicomNode.DEFAULT_PORT);
		lines.add(DICOM_NODE_AETITLE+"="+DicomNode.DEFAULT_AE_TITLE);
		lines.add(DATABASE_IP+"="+SQLSettings.ADDRESS);
		lines.add(DATABASE_NAME+"="+SQLSettings.DATABASE_NAME);
		lines.add(CONDOR_JOB_DIR_NAME+"="+getCondorJobDir());
		try {
			System.out.println(SystemSettings.APP_DIR);
			writeSmallTextFile(lines, SystemSettings.APP_DIR+"/"+CONF_FILE);
		} catch (IOException e) {
			WindowManager.mwLogger.log(Level.SEVERE, "Error with params.conf.",e);
		}
	}
	public List<String> readSmallTextFile(String aFileName) throws IOException {
		Path path = Paths.get(aFileName);
		return Files.readAllLines(path, StandardCharsets.UTF_8);
	}
  
	public void writeSmallTextFile(List<String> aLines, String aFileName) throws IOException {
		Path path = Paths.get(aFileName);
		Files.write(path, aLines, StandardCharsets.UTF_8);
	}
	
	/**
	 * Recupere depuis le fichier PPID_FILE la liste des projets 
	 * pour lesquels utiliser le patientid au lieu du patientname
	 * On le cree si il n'existe pas 
	 * @throws IOException
	 */
	private void setProjectWithPatientId() throws IOException{
		ppid = new HashSet<String>();
		File fi = new File(SystemSettings.APP_DIR+File.separator+PPID_FILE);
		if(fi.exists()){
			List<String> lines = readSmallTextFile(SystemSettings.APP_DIR+File.separator+PPID_FILE);
			for(String s:lines){
				ppid.add(s);
			}
		}else{
			fi.createNewFile();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * Recharge la configuration
	 */
	public void refresh() {
		File params_file = new File(SystemSettings.APP_DIR + "/" + CONF_FILE);
		if(params_file.exists()){
			HashMap<String,String> params = getParams(params_file);
			setIncomingDir(params.get(INCOMING_DIR_NAME));
			setDicomDir(params.get(DICOM_DIR_NAME));
			setNiftiDir(params.get(NIFTI_DIR_NAME));
			setTempDir(params.get(TEMP_DIR_NAME));
			setServerDir(params.get(SERVER_DIR_NAME));
			if(params.containsKey(DICOM_NODE_IP))
				DicomNode.DEFAULT_HOSTNAME = params.get(DICOM_NODE_IP);
			if(params.containsKey(DICOM_NODE_PORT))
				DicomNode.DEFAULT_PORT = Integer.parseInt(params.get(DICOM_NODE_PORT));
			if(params.containsKey(DICOM_NODE_AETITLE))
				DicomNode.DEFAULT_AE_TITLE = params.get(DICOM_NODE_AETITLE);
			if(params.containsKey(DATABASE_IP))
				SQLSettings.ADDRESS = params.get(DATABASE_IP);
			if(params.containsKey(DATABASE_NAME))
				SQLSettings.DATABASE_NAME = params.get(DATABASE_NAME);
			if(params.containsKey(CONDOR_JOB_DIR_NAME))
				setCondorJobDir(params.get(CONDOR_JOB_DIR_NAME));
		}else{
			setIncomingDir(SystemSettings.APP_DIR+ "/" +INCOMING_DIR_NAME);
			setDicomDir(SystemSettings.APP_DIR + "/" +DICOM_DIR_NAME);
			setNiftiDir(SystemSettings.APP_DIR + "/" + NIFTI_DIR_NAME);
			setTempDir(SystemSettings.APP_DIR + "/" + TEMP_DIR_NAME);
			setCondorJobDir(SystemSettings.APP_DIR + File.separator + CONDOR_JOB_DIR_NAME);
			setServerDir(getDicomDir().toString());
			saveConfiguration();
		}
		dbCache = new DBCache();
		try {
			setProjectWithPatientId();
		} catch (IOException e) {
			WindowManager.mwLogger.log(Level.SEVERE,"Could not load "+PPID_FILE,e);
		}
	}



}
