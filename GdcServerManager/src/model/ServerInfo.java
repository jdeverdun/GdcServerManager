package model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.WindowManager;

import daemon.DBCache;

public class ServerInfo {

	public static final String NRI_DICOM_NAME = "NRI-DICOM";
	public static final String NRI_ANALYSE_NAME = "NRI-ANALYSE";
	public static final String BACKUP_DIR = "cache"; // Repertoire ou on stock les daemons (fichiers de sauvegarde)
	private static final String INCOMING_DIR_NAME = "incomingDir";
	private static final String DICOM_DIR_NAME = "dicomDir";
	private static final String NIFTI_DIR_NAME = "niftiDir";
	private static final String TEMP_DIR_NAME = "tempDir";
	private static final String SERVER_DIR_NAME = "serverDir";
	public static String CONF_FILE = "params.conf";
	// Attributs
	private Path incomingDir; // dossier des dicom en vrac
	private Path dicomDir; // dossier des dicom triés
	private Path niftiDir; // dossier des nifti triés
	private Path tempDir; // dossier temporaire, utilise surtout pour la conversion nifti (decryptage fichier)
	private Path serverDir; // dossier racine du serveur
	private DBCache dbCache; // cache de donnees de la bdd
	
	
	// Constructeurs
	public ServerInfo(){
		incomingDir = null;
		dicomDir = null;
		niftiDir = null;
		setTempDir(null);
		setServerDir(null);
		dbCache = new DBCache();
	}
	
	public ServerInfo(String inc, String dicom, String nifti,String temp,String serv){
		setIncomingDir(inc);
		setDicomDir(dicom);
		setNiftiDir(nifti);
		setTempDir(temp);
		setServerDir(serv);
		dbCache = new DBCache();
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
		}else{
			setIncomingDir(app_dir+ "/" +INCOMING_DIR_NAME);
			setDicomDir(app_dir + "/" +DICOM_DIR_NAME);
			setNiftiDir(app_dir + "/" + NIFTI_DIR_NAME);
			setTempDir(app_dir + "/" + TEMP_DIR_NAME);
			setServerDir(getDicomDir().toString());
			saveConfiguration();
		}
		dbCache = new DBCache();
	}



	public Path getIncomingDir() {
		return incomingDir;
	}

	public void setIncomingDir(String incomingDir) {
		this.incomingDir = (new File(incomingDir)).toPath();
		buildIfNotExist(this.incomingDir);
	}

	public Path getDicomDir() {
		return dicomDir;
	}

	public void setDicomDir(String dicomDir) {
		this.dicomDir = (new File(dicomDir)).toPath();
		buildIfNotExist(this.dicomDir);
	}

	public Path getNiftiDir() {
		return niftiDir;
	}

	public void setNiftiDir(String niftiDir) {
		this.niftiDir = (new File(niftiDir)).toPath();
		buildIfNotExist(this.niftiDir);
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
		this.tempDir = (new File(tempDir)).toPath();
		buildIfNotExist(this.tempDir);
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
					System.out.println("Error with params.conf.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
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
		try {
			writeSmallTextFile(lines, SystemSettings.APP_DIR+"/"+CONF_FILE);
		} catch (IOException e) {
			System.err.println("Error with params.conf.");
			e.printStackTrace();
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
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}



}
