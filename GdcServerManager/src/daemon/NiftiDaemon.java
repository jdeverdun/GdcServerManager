package daemon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import daemon.tools.nifti.Nifti_Writer;

import model.DicomImage;
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
	private ConcurrentHashMap<Path, DicomImage> dir2convert; 
	private ServerInfo serverInfo;
	private int format = defaultFormat; // ANALYZE, NIFTI etc 
	private boolean stop;
	
	
	// Constructeur
	public NiftiDaemon(){
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
	}

	public NiftiDaemon(ServerInfo si){
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		setServerInfo(si);
	}
	
	// format issue de la classe Nifti_Writer
	public NiftiDaemon(ServerInfo si,int format){
		setDir2convert(new ConcurrentHashMap<Path, DicomImage> ());
		setStop(false);
		setServerInfo(si);
		setFormat(format);
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
			Set<Path> keys = dir2convert.keySet();
			Iterator<Path> it = keys.iterator();
			while(it.hasNext()){
				Path path = it.next();
				if(timeSinceModif(path) > 120000.0f){
					System.out.println(timeSinceModif(path)+"----"+dir2convert.get(path).toString());
					// Si ca fait plus de 2 min on convertit 
					// /!\ dcm2nii.exe DOIT etre dans le path
					
					NiftiWorker nworker = new NiftiWorker(this, path,dir2convert.get(path));
					nworker.start();
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
	

	
	
}
