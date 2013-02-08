package daemon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;

import modeles.ServerInfo;


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
	// la hashmap qui contient les repertoires a convertir et la date de la derniere modif de ce repertoire
	private HashMap<Path, FileTime> dir2convert; 
	private ServerInfo serverInfo;
	private boolean stop;
	
	// Constructeur
	public NiftiDaemon(){
		setDir2convert(new HashMap<Path, FileTime> ());
		setStop(false);
	}

	public NiftiDaemon(ServerInfo si){
		setDir2convert(new HashMap<Path, FileTime> ());
		setStop(false);
		setServerInfo(si);
	}
	
	// Accesseurs
	public HashMap<Path, FileTime> getDir2convert() {
		return dir2convert;
	}


	public void setDir2convert(HashMap<Path, FileTime> dir2convert) {
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


	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}


	// Methodes
	public void run(){
		System.out.println("Nifti Daemon Online.");
		while(!isStop()){
			for(Path path:dir2convert.keySet()){
				if(timeSinceModif(path) > 120000.0f){
					// Si ca fait plus de 2 min on convertit 
					// /!\ mcverter.exe DOIT etre dans le path
					
					// on cree le nouveau nom du repertoire nifti avec le nom du projet (mcverter creera l'arborescence avec le nom du patient)
					Path niftiPath = Paths.get(serverInfo.getNiftiDir().toString() + File.separator + path.getParent().getFileName());
					System.out.println("Nifti convert : "+path);
					Process process;
					try {
						process = Runtime.getRuntime().exec("mcverter.exe "+ path +" -o "+ niftiPath.toString() + " -f fsl -x -r");
						process.waitFor();
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
		long time = System.currentTimeMillis() - dir2convert.get(dir).toMillis();
		return time;
	}
	
	
}
