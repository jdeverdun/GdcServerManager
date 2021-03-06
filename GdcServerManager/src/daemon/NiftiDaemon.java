package daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import dao.MySQLProjectDAO;
import dao.ProjectDAO;
import dao.project.AcquisitionDateDAO;
import dao.project.MySQLAcquisitionDateDAO;
import dao.project.MySQLPatientDAO;
import dao.project.MySQLProtocolDAO;
import dao.project.MySQLSerieDAO;
import dao.project.PatientDAO;
import dao.project.ProtocolDAO;
import dao.project.SerieDAO;

import es.vocali.util.AESCrypt;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.WindowManager;



import model.AcquisitionDate;
import model.DicomImage;
import model.Patient;
import model.Project;
import model.Protocol;
import model.Serie;
import model.ServerInfo;
import model.daemon.CustomConversionSettings;
import model.daemon.CustomConversionSettings.ServerMode;


/**
 *  Classe gerant les conversions en nifti
 *  elle receptionne les repertoires susceptibles 
 *  d'avoir � �tre convertit, et les convertit lorsque
 *  ce repertoire n'a pas �t� modifi� depuis un certain temps
 *  
 * @author Analyse
 *
 */

public class NiftiDaemon extends Thread{

	// Attributs
	public static enum FORMAT{ANALYZE,SPM,NIFTI,FSL};
	public static final int ANALYZE = 0; //SPM2 style
	public static final int SPM = 1; //SPM5 style
	public static final int NIFTI = 2; // Nifti format
	public static final int FSL = 3; // FSL format (la gauche est a gauche?)
	public static int defaultFormat = FSL;
	// liste des suffixes permettant de determiner lors de la suppression d'un nifti 
	// les fichiers associes a supprimer : ex : DTI les fichiers bval ...
	public static final String[] suffixeToRemoveWithNifti = new String[]{"_bvecs.txt","_bvals.txt","_moco.txt"};
	
	
	// permet de savoir si le daemon a crashe
	private boolean crashed;
	
	// fichier de backup
	private static final String BACKUP_FILE = "niftiDaemon.bak";
	
	// la hashmap qui contient les repertoires a convertir et la date de la derniere modif de ce repertoire
	private ConcurrentHashMap<Path, DicomImage> dir2convert; 
	private ServerInfo serverInfo;
	private int format = defaultFormat; // ANALYZE, NIFTI etc 
	private boolean stop;
	private CustomConversionSettings settings;
	private float waitTimeToConvert; // cf setServerMode();
	
	// le worker nifti
	private NiftiWorker nworker;
	
	// Constructeur
	public NiftiDaemon(){
		setSettings(new CustomConversionSettings());
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		crashed = false;
		nworker = null;
	}

	public NiftiDaemon(ServerInfo si, CustomConversionSettings customConvsettings){
		setSettings(customConvsettings);
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		setServerInfo(si);
		crashed = false;
		nworker = null;
	}
	
	public NiftiDaemon(ServerInfo si){
		setSettings(new CustomConversionSettings());
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		setServerInfo(si);
		crashed = false;
		nworker = null;
	}
	
	// format 
	public NiftiDaemon(ServerInfo si,int format){
		setSettings(new CustomConversionSettings());
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		setServerInfo(si);
		setFormat(format);
		crashed = false;
		nworker = null;
	}	
	
	/**
	 * Constructeur pour mode client surtout
	 * @param si
	 * @param format
	 * @param customConvsettings
	 */
	public NiftiDaemon(ServerInfo si,int format, CustomConversionSettings customConvsettings){
		setSettings(customConvsettings);
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		setServerInfo(si);
		setFormat(format);
		crashed = false;
		nworker = null;
	}
	
	// Accesseurs
	public ConcurrentHashMap<Path, DicomImage> getDir2convert() {
		return dir2convert;
	}


	public void setDir2convert(ConcurrentHashMap<Path, DicomImage> dir2convert) {
		this.dir2convert = dir2convert;
	}
	
	
	public boolean isStop() {
		return stop;
	}


	public void setStop(boolean stop) {
		this.stop = stop;
		
		if(stop){
			WindowManager.mwLogger.log(Level.INFO, "Stopping NiftiDaemon");
			if(!dir2convert.isEmpty() && settings.getServerMode() == ServerMode.SERVER)
				saveBackup();
		}
	}


	public ServerInfo getServerInfo() {
		return serverInfo;
	}


	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}


	// Methodes
	public void run(){
		WindowManager.mwLogger.log(Level.INFO, "Nifti Daemon Online.");
		if(getSettings().getServerMode() == ServerMode.SERVER){
			// on charge le backup
			loadBackup();
			// on supprime le fichier de backup si il existe (on l'a deja charge si il existe)
			File backupfile = new File(SystemSettings.APP_DIR+File.separator+ServerInfo.BACKUP_DIR+File.separator+BACKUP_FILE);
			if(backupfile.exists())
				backupfile.delete();
		}
		while(!isStop()){
			// On evite une utilisation trop importante du CPU
			// avec des boucles infini
			if(getSettings().getServerMode() == ServerMode.SERVER || getSettings().getServerMode() == ServerMode.IMPORT ){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			Set<Path> keys = dir2convert.keySet();
			Iterator<Path> it = keys.iterator();
			HashLoop:
			while(it.hasNext() && !isStop()){
				if(isStop())
					break;
				Path path = it.next();
				long timeSincemod = timeSinceModif(path);
				// Si == -1 alors le dicom d'origine a ete supprime entre temps -> pas besoin de convertir le repertoire
				if(timeSincemod==-1){
					dir2convert.remove(path);
					continue;
				}else{
					if( timeSincemod > waitTimeToConvert ){
						// Si ca fait plus de 2 min on convertit (ou si on est pas en servermode
						// /!\ convertizer.exe DOIT etre dans le path
						if(getSettings().getServerMode() == ServerMode.SERVER || getSettings().getServerMode() == ServerMode.IMPORT){
							// si il reste des donnees a deplacer vers le serveur on attend la fin de se deplacement
							if(getSettings().getServerMode() == ServerMode.SERVER && !SystemSettings.DICOM_DISPATCHER.getDicomToMove().isEmpty())
								continue HashLoop;
							if(getSettings().getServerMode() == ServerMode.IMPORT && !getSettings().getImportSettings().getDispatcher().getDicomToMove().isEmpty())
								continue HashLoop;
							// on s'assure que tout le repertoire dicom a ete encrypte avant de convertir
							// si ce n'est pas le cas on
							if(!isDirFullyEncrypted(path)) 
								continue HashLoop;
							nworker = null;
							try{
								nworker = new NiftiWorker(this, path,dir2convert.get(path));
								nworker.start();
								// on enleve le repertoire qu'on vient de convertir de la liste
								dir2convert.remove(path);
								//it.remove();
							}catch(Exception e){// on securise le process 
								WindowManager.mwLogger.log(Level.SEVERE, "Critical error during nifti conversion",e);
								crashed = true;
								if(nworker!=null){
									try {
										nworker.clean();
										WindowManager.mwLogger.log(Level.INFO, "Bugged worker cleaned");
									} catch (IOException e1) {
										WindowManager.mwLogger.log(Level.SEVERE, "Can't clean bugged worker",e1);
									}
									if(settings.getServerMode() == ServerMode.SERVER)
										saveBackup();
									// on fait crasher le daemon car si on arrive la c'est qu'il y a une erreur 
									// dans le code
									crashed = true;
									throw e;
								}
							}
						}else{
							NiftiWorkerClient nworker = new NiftiWorkerClient(this, path,dir2convert.get(path));
							nworker.start();
							// on enleve le repertoire qu'on vient de convertir de la liste
							//it.remove();
							dir2convert.remove(path);
						}
					}
				}
			}
		}
	}
	


	/**
	 * Test si le repertoire path ne contient que des fichiers encrypte (termine par l'extension .enc)
	 * @param path
	 * @return
	 */
	public boolean isDirFullyEncrypted(Path path) {
		boolean returnval = true;
		for(String p:path.toFile().list()){
			if(p.length()>2 && !p.endsWith(AESCrypt.ENCRYPTSUFFIX)){
				try {
					SystemSettings.MISSING_DAEMON.moveNotEncodedDicomFile(new File(path.toString()+File.separator+p));
					if(new File(path.toString()+File.separator+p+AESCrypt.ENCRYPTSUFFIX).exists())
						new File(path.toString()+File.separator+p+AESCrypt.ENCRYPTSUFFIX).delete();
					returnval = false;
				} catch (Exception e) {
					WindowManager.mwLogger.log(Level.SEVERE,"Unable to move not encoded dicom. ["+path.toString()+File.separator+p+"]",e);
				}
			}
		}
		return returnval;
	}

	public boolean addDir(Path dir,DicomImage di){
		if(dir2convert.isEmpty()){
			if(this.settings.getServerMode() != ServerMode.CLIENT && !SQLSettings.connectionIsWorking()){
				setCrashed(true);
				setStop(true);
				return false;
			}
		}
		if(dir2convert.containsKey(dir)) return false;
		WindowManager.mwLogger.log(Level.INFO, "Added dir : "+dir+" to convert");
		dir2convert.put(dir, di);
		return true;
	}
	
	// On recupere le temps depuis la derniere modif du repertoire
	public long timeSinceModif(Path dir){
		BasicFileAttributes attrs = null;
		try {
			attrs = Files.readAttributes(dir, BasicFileAttributes.class);
			long time = System.currentTimeMillis() - attrs.lastModifiedTime().toMillis();
			return time;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		
	}

	public float getWaitTimeToConvert() {
		return waitTimeToConvert;
	}

	public void setWaitTimeToConvert(float waitTimeToConvert) {
		this.waitTimeToConvert = waitTimeToConvert;
	}


	public CustomConversionSettings getSettings() {
		return settings;
	}

	public void setSettings(CustomConversionSettings settings) {
		this.settings = settings;
		if(this.settings.getServerMode() == ServerMode.SERVER)
			waitTimeToConvert = 600000.0f;
		else if(this.settings.getServerMode() == ServerMode.IMPORT)
			waitTimeToConvert = 60000.0f;
		else
			waitTimeToConvert = 1000.0f;
	}
	
	/**
	 * Force l'arret du daemon
	 */
	public void forceStop(){
		this.stop = true;
		System.out.println("Nifti Daemon offline");
	}
	
	/**
	 * Sauvegarde le daemon pour une reprise ulterieure
	 * sauvegarde dans le repertoire du programme "/cache"
	 */
	private void saveBackup(){
		Path savePath = Paths.get(SystemSettings.APP_DIR+File.separator+ServerInfo.BACKUP_DIR);
		if(!savePath.toFile().exists())
			savePath.toFile().mkdir();
		
		try{
			// Open a file to write to
			FileOutputStream saveFile=new FileOutputStream(savePath+File.separator+BACKUP_FILE);
	
			// Create an ObjectOutputStream to put objects into save file.
			ObjectOutputStream save = new ObjectOutputStream(saveFile);
			
			// sauvegarde des donnees dans une structure serializable
			ConcurrentHashMap<String, DicomImage> toSave = new ConcurrentHashMap<String, DicomImage>();
			for(Path p:dir2convert.keySet()){
				toSave.put(p.toString(), dir2convert.get(p));
			}
			save.writeObject(toSave);
			
			save.close();
		}catch(IOException e){
			WindowManager.mwLogger.log(Level.SEVERE, "saveBackup error",e);
			new File(savePath+File.separator+BACKUP_FILE).delete();
		}
	}

	/**
	 * Charge des donnees de backup
	 */
	private void loadBackup(){
		Path savePath = Paths.get(SystemSettings.APP_DIR+File.separator+ServerInfo.BACKUP_DIR+File.separator+BACKUP_FILE);
		if(!savePath.toFile().exists())
			return;
		
		try{
			// Open file to read from, named SavedObj.sav.
			FileInputStream saveFile = new FileInputStream(savePath.toString());
	
			// Create an ObjectInputStream to get objects from save file.
			ObjectInputStream save = new ObjectInputStream(saveFile);
			
			ConcurrentHashMap<String, DicomImage> dir2convertbak = (ConcurrentHashMap<String, DicomImage>) save.readObject();
			save.close();
			
			// On rajoute les donnnees sauvegardees 
			for(String p:dir2convertbak.keySet()){
				Path lp = Paths.get(p);
				if(!dir2convert.contains(lp))
					dir2convert.put(lp,dir2convertbak.get(p));
			}

			
		}catch(IOException | ClassNotFoundException e){
			WindowManager.mwLogger.log(Level.SEVERE, "loadBackup error",e);
			WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningniftidaemon().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
		}
		
	}
	
	public String getStatus() {
		if(isAlive())
			return dir2convert.size()+" directories to convert.";
		else
			return "";
	}

	/**
	 * @return the crashed
	 */
	public boolean isCrashed() {
		return crashed;
	}

	/**
	 * @param crashed the crashed to set
	 */
	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}

	public boolean addDir(Path path, String project, String patient,
			String acqdate, String protocol, String serie) throws SQLException {
		DicomImage dicomImage = new DicomImage();
		ProjectDAO pdao = new MySQLProjectDAO();
		dicomImage.setProjet(pdao.retrieveProject(project));
		PatientDAO padao = new MySQLPatientDAO();
		dicomImage.setPatient(new Patient(padao.getPatientIdFor(project, patient)));
		AcquisitionDateDAO acqdao = new MySQLAcquisitionDateDAO();
		dicomImage.setAcquistionDate(new AcquisitionDate(acqdao.getAcqdateIdFor(project, patient, acqdate)));
		ProtocolDAO prdao = new MySQLProtocolDAO();
		dicomImage.setProtocole(new Protocol(prdao.getProtocolIdFor(project, patient, acqdate, protocol)));
		SerieDAO sdao = new MySQLSerieDAO();
		dicomImage.setSerie(new Serie(sdao.getSerieIdFor(project, patient, acqdate, protocol, serie)));
		return addDir(path, dicomImage);
	}

	public void clear() {
		dir2convert.clear();
	}

}
