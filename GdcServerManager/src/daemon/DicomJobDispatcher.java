package daemon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;

import settings.SystemSettings;


import model.ServerInfo;


/**
 * Classe contenant une liste mis à jours régulierement 
 * des fichiers à déplacer
 * 
 * Les deplacements se font avec un délais de 50 ms pour éviter des
 * erreurs d'overload
 * @author Analyse
 *
 */
public class DicomJobDispatcher extends Thread{
	
	
	private DicomDaemon dicomDaemon;
	private ServerInfo serverInfo;
	private LinkedList<Path> dicomToMove;
	private int numberOfRuns; // nombre de threads qui ont été lancé
	private int maxWorker; // nombre de coeurs disponibles 
	private DicomWorker dworker;
	private boolean stop;
	
	




	public DicomJobDispatcher(DicomDaemon dicomDaemon) {
		dicomToMove = new LinkedList<Path>();
		numberOfRuns = 0;
		setDicomDaemon(dicomDaemon);
		setStop(false);
		setServerInfo(getDicomDaemon().getServerInfo());
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
			while(dicomToMove.isEmpty()){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// on lance le deplacement du fichier
			// on le fait fich/fich (pas multithread) car windows
			// gere tres bien les coeurs tout seul /!\ dworker n'est pas un Thread !
			dworker = new DicomWorker(this, (Path)dicomToMove.pop());
	        dworker.start();  

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
	 * Indique au serveur nifti qu'il faut convertir ce repertoire
	 * @param dicomWorker
	 */
	public void sendToNiftiDaemon(DicomWorker dicomWorker) {
		if(dicomWorker.getPatientFolder() != null){
			getDicomDaemon().getNiftiDaemon().addDir(dicomWorker.getSerieFolder(),dicomWorker.getDicomImage());
		}
		dicomWorker = null;
	}
	
	public void addDicomToMove(Path p){
		dicomToMove.push(p);
	}
}
