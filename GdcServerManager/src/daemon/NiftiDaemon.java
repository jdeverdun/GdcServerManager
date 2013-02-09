package daemon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import daemon.tools.nifti.Nifti_Writer;

import model.ServerInfo;


/**
 *  Classe gerant les conversions en nifti
 *  elle receptionne les repertoires susceptibles 
 *  d'avoir à être convertit, et les convertit lorsque
 *  ce repertoire n'a pas été modifié depuis un certain temps
 *  
 * @author Analyse
 *
 */

public class NiftiDaemon extends Thread{

	// Attributs
	public static final int ANALYZE_7_5 = 0; //SPM2 img/hdr
	public static final int SPM5_NIFTI = 1; //SPM5 img/hdr
	public static final int NIFTI_4D = 2; // Nifti 4D nii
	public static final int FSL_NIFTI = 3; // nifti FSL nii.gz
	public static int defaultFormat = NIFTI_4D;
	
	
	// la hashmap qui contient les repertoires a convertir et la date de la derniere modif de ce repertoire
	private ConcurrentHashMap<Path, FileTime> dir2convert; 
	private ServerInfo serverInfo;
	private int format = defaultFormat; // ANALYZE, NIFTI etc 
	private boolean stop;
	
	
	// Constructeur
	public NiftiDaemon(){
		setDir2convert(new ConcurrentHashMap<Path, FileTime> ());
		setStop(false);
	}

	public NiftiDaemon(ServerInfo si){
		setDir2convert(new ConcurrentHashMap<Path, FileTime> ());
		setStop(false);
		setServerInfo(si);
	}
	
	// format issue de la classe Nifti_Writer
	public NiftiDaemon(ServerInfo si,int format){
		setDir2convert(new ConcurrentHashMap<Path, FileTime> ());
		setStop(false);
		setServerInfo(si);
		setFormat(format);
	}	
	// Accesseurs
	public ConcurrentHashMap<Path, FileTime> getDir2convert() {
		return dir2convert;
	}


	public void setDir2convert(ConcurrentHashMap<Path, FileTime> dir2convert) {
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
		String command = "";
		while(!isStop()){
			// On evite une utilisation trop importante du CPU
			// avec des boucles infini
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
			Set<Path> keys = dir2convert.keySet();
			Iterator<Path> it = keys.iterator();
			while(it.hasNext()){
				Path path = it.next();
				if(timeSinceModif(path) > 120000.0f){
					System.out.println(timeSinceModif(path)+"----"+dir2convert.get(path).toString());
					// Si ca fait plus de 2 min on convertit 
					// /!\ dcm2nii.exe DOIT etre dans le path
					
					// On recherche l'arborescence et on créé les répertoire si besoin +
					// NIFTIDIR / NOM_ETUDE / NOM_PATIENT / DATE_IRM / PROTOCOL / SERIE 
					Path studyName = path.getParent().getParent().getParent().getParent().getFileName();
					Path patientName = path.getParent().getParent().getParent().getFileName();
					Path acqDate = path.getParent().getParent().getFileName();
					Path protocolAcqName = path.getParent().getFileName() ;
					Path serieName = path.getFileName();
					
					Path studyDir = Paths.get(serverInfo.getNiftiDir().toString() + File.separator + studyName);
					Path patientDir = Paths.get(studyDir + File.separator +  patientName);
					Path acqDateDir = Paths.get(patientDir + File.separator +  acqDate);
					Path protocolDir = Paths.get(acqDateDir + File.separator +  protocolAcqName);
					Path serieDir = Paths.get(protocolDir + File.separator +  serieName);
					
					checkAndMakeDir(studyDir);
					checkAndMakeDir(patientDir);
					checkAndMakeDir(acqDateDir);
					checkAndMakeDir(protocolDir);
					checkAndMakeDir(serieDir);
					
					Path niftiPath = serieDir;
					System.out.println("Nifti convert : "+path);
					// -i id in filename | -p protocol in filename
					command = "dcm2nii.exe -i y -p y -e n -a n -d n -e n -f n -l 0  ";
					switch(getFormat()){
					case ANALYZE_7_5:
						command+=" -n n -s y -g n ";break;
					case SPM5_NIFTI:
						command+=" -n n -g n ";break;
					case NIFTI_4D://A selectionner en prio ?
						command+=" -n y -g n ";break;
					case FSL_NIFTI:
						command+=" -n y -g y ";break;
					default:
						System.err.println("Unknow nifti format");
					}
					command+=" -o "+niftiPath+" "+path;
					Process process;
					try {
						//process = Runtime.getRuntime().exec("mcverter.exe "+ path +" -o "+ niftiPath.toString() + " -f fsl -x -r");//-x 
						process = Runtime.getRuntime().exec(command);
						process.waitFor();
						// on enleve le repertoire qu'on vient de convertir de la liste
						it.remove();
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void addDir(Path dir){
		if(dir2convert.containsKey(dir)) return;
		BasicFileAttributes attrs;
		System.out.println("Ajout de : " + dir);
		try {
			attrs = Files.readAttributes(dir, BasicFileAttributes.class);
			dir2convert.put(dir, attrs.lastModifiedTime());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	// test si les repertoires existent (patient / protocoles etc) et on les créé au besoin
	private void checkAndMakeDir(Path dir) {
		if(!Files.exists(dir)){
			// Si ce n'est pas le cas on le créé
			try {
				Files.createDirectory(dir);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
}
