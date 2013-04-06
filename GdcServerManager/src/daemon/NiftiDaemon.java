package daemon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;



import model.DicomImage;
import model.ServerInfo;
import model.daemon.CustomConversionSettings;


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
	public static enum FORMAT{SPM2,SPM5,NIFTI,cNIFTI};
	public static final int ANALYZE_7_5 = 0; //SPM2 img/hdr
	public static final int SPM5_NIFTI = 1; //SPM5 img/hdr
	public static final int NIFTI_4D = 2; // Nifti 4D nii
	public static final int FSL_NIFTI = 3; // nifti FSL nii.gz
	public static int defaultFormat = NIFTI_4D;
	
	
	// la hashmap qui contient les repertoires a convertir et la date de la derniere modif de ce repertoire
	private ConcurrentHashMap<Path, DicomImage> dir2convert; 
	private ServerInfo serverInfo;
	private int format = defaultFormat; // ANALYZE, NIFTI etc 
	private boolean stop;
	private boolean waitingToStop;
	private CustomConversionSettings settings;
	private float waitTimeToConvert; // cf setServerMode();
	
	
	// Constructeur
	public NiftiDaemon(){
		setSettings(new CustomConversionSettings());
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		waitingToStop = false;
	}

	public NiftiDaemon(ServerInfo si){
		setSettings(new CustomConversionSettings());
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		setServerInfo(si);
		waitingToStop = false;
	}
	
	// format 
	public NiftiDaemon(ServerInfo si,int format){
		setSettings(new CustomConversionSettings());
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		setServerInfo(si);
		setFormat(format);
		waitingToStop = false;
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
		waitingToStop = false;
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
		System.out.println("Nifti Daemon Online.");
		while(!isStop()){
			// On evite une utilisation trop importante du CPU
			// avec des boucles infini
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
			if(dir2convert.isEmpty() && waitingToStop)
				setStop(true);
			Set<Path> keys = dir2convert.keySet();
			Iterator<Path> it = keys.iterator();
			while(it.hasNext() && !isStop()){
				Path path = it.next();
				if(timeSinceModif(path) > waitTimeToConvert ){
					// Si ca fait plus de 2 min on convertit (ou si on est pas en servermode
					// /!\ dcm2nii.exe DOIT etre dans le path
					if(getSettings().isServerMode()){
						NiftiWorker nworker = new NiftiWorker(this, path,dir2convert.get(path));
						nworker.start();
					}else{
						NiftiWorkerClient nworker = new NiftiWorkerClient(this, path,dir2convert.get(path));
						nworker.start();
					}
					// on enleve le repertoire qu'on vient de convertir de la liste
					it.remove();
				}
			}
		}
	}
	


	public void addDir(Path dir,DicomImage di){
		if(dir2convert.containsKey(dir)) return;
		System.out.println("Ajout de : " + dir);
		dir2convert.put(dir, di);
	}
	
	// On recupere le temps depuis la derniere modif du repertoire
	public long timeSinceModif(Path dir){
		BasicFileAttributes attrs = null;
		try {
			attrs = Files.readAttributes(dir, BasicFileAttributes.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		long time = System.currentTimeMillis() - attrs.lastModifiedTime().toMillis();
		return time;
	}

	public void setWaitingToStop(boolean b) {
		waitingToStop = b;
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
		if(this.settings.isServerMode())
			waitTimeToConvert = 120000.0f;
		else
			waitTimeToConvert = 1000.0f;
	}
	

	
	
}
