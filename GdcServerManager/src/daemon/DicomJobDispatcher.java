package daemon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import settings.SystemSettings;


import model.DicomImage;
import model.ServerInfo;
import model.daemon.DJDSettings;


/**
 * Classe contenant une liste mis à jours régulierement 
 * des fichiers à déplacer
 * 
 * @author Analyse
 *
 */
public class DicomJobDispatcher extends Thread{
	
	
	private DicomDaemon dicomDaemon;
	private ServerInfo serverInfo;
	private LinkedList<Path> dicomToMove;
	private int numberOfRuns; // nombre de threads qui ont été lancé
	private int maxWorker; // nombre de coeurs disponibles [obsolete]
	private DicomWorker dworker;
	private boolean stop;
	private int waitCounter;
	private DJDSettings settings; // Parametres pour le DicomJobDispatcher 
	private NiftiDaemon niftiDaemon;



	public DicomJobDispatcher(DicomDaemon dicomDaemon) {
		dicomToMove = new LinkedList<Path>();
		numberOfRuns = 0;
		setDicomDaemon(dicomDaemon);
		setSettings(new DJDSettings());
		setStop(false);
		setServerInfo(getDicomDaemon().getServerInfo());
		setMaxWorker(SystemSettings.AVAILABLE_CORES);
		
	}

	/**
	 *  principalement pour mode conversion only
	 * @param si
	 * @param settings
	 * @param nifti
	 */
	public DicomJobDispatcher(ServerInfo si,DJDSettings settings, NiftiDaemon nifti) {
		setNiftiDaemon(nifti);
		dicomToMove = new LinkedList<Path>();
		numberOfRuns = 0;
		setSettings(settings);
		setDicomDaemon(null);
		setStop(false);
		setServerInfo(si);
		setMaxWorker(SystemSettings.AVAILABLE_CORES);
		
	}
	
	
	// Accesseurs
	
	public DicomDaemon getDicomDaemon() {
		return dicomDaemon;
	}

	public void setDicomDaemon(DicomDaemon parentDaemon) {
		this.dicomDaemon = parentDaemon;
	}


	public LinkedList<Path> getDicomToMove() {
		return dicomToMove;
	}


	public void setDicomToMove(LinkedList<Path> dicomToMove) {
		this.dicomToMove = dicomToMove;
	}


	public ServerInfo getServerInfo() {
		return serverInfo;
	}




	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}




	public int getMaxWorker() {
		return maxWorker;
	}




	public void setMaxWorker(int maxWorker) {
		this.maxWorker = maxWorker;
	}

	public DicomWorker getDworker() {
		return dworker;
	}




	public void setDworker(DicomWorker dworker) {
		this.dworker = dworker;
	}

	/**
	 * Que quand serverMode = false !!!
	 * @return
	 */
	public NiftiDaemon getNiftiDaemon() {
		return niftiDaemon;
	}

	public void setNiftiDaemon(NiftiDaemon niftiDaemon) {
		this.niftiDaemon = niftiDaemon;
	}

	public DJDSettings getSettings() {
		return settings;
	}

	public void setSettings(DJDSettings settings) {
		this.settings = settings;
	}

	public boolean isStop() {
		return stop;
	}


	public void setStop(boolean stop) {
		this.stop = stop;
	}
	
	// Methodes
	public void run(){
		System.out.println("Dispatcher Online with "+getMaxWorker()+" CPU cores.");
		while(!isStop()){
			// check si il y a des donnees a deplacer
			while(dicomToMove.isEmpty() && !isStop()){
				if(waitCounter>10000 && getSettings().isServerMode()){
					//Si aucun fichier n'a ete ajouter depuis plus de 10 sec
					// on verifie si on a pas rate des event a cause d'overflow
					try {
						checkForMissedFiles();
					} catch (IOException e) {
						e.printStackTrace();
					}
					waitCounter = 0;
				}
				try {
					Thread.sleep(5000);
					waitCounter += 5000;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(isStop())
				return;
			// on lance le deplacement du fichier
			// on le fait fich/fich (pas multithread) car windows
			// gere tres bien les coeurs tout seul /!\ dworker n'est pas un Thread !
			if(getSettings().isServerMode()){
				dworker = new DicomWorker(this, (Path)dicomToMove.pop());
		        dworker.start();
			}else{
				dworker = new DicomWorkerClient(this, (Path)dicomToMove.pop());
		        dworker.start();
			}

	        numberOfRuns++;
	        if(numberOfRuns>2000){
	        	// On dit au garbage collector de nettoyer 
	        	// quand on a fait 2000 copies
	        	System.gc();
	        	numberOfRuns = 0;
	        }
		}
	}
	
	
	/**
	 * Rajoute a la liste des dicom a deplacer ceux qui on pu etre oublie
	 * a cause d'event overflow
	 * @throws IOException 
	 */
	private void checkForMissedFiles() throws IOException {
		String[] filesInInc = getServerInfo().getIncomingDir().toFile().list();
		for(String name:filesInInc){
			File lfile = new File(getServerInfo().getIncomingDir() + "/" + name);
			if(name.length()>2){
				if(DicomImage.isDicom(lfile)){
					String fullpath = getServerInfo().getIncomingDir() + "/" + name;
					addDicomToMove(Paths.get(fullpath));
				}else{
					lfile.delete();
				}
			}
		}
	}

	
	public void addDicomToMove(Path p){
		if(!dicomToMove.contains(p))
			dicomToMove.push(p);
	}
}
