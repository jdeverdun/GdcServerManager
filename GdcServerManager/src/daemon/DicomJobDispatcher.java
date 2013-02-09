package daemon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;


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
	private ArrayList<DicomWorker> dicomWorkerList;
	private LinkedList<Path> dicomToMove;
	private int numberOfRuns;
	private boolean stop;
	
	




	public DicomJobDispatcher(DicomDaemon dicomDaemon) {
		dicomWorkerList = new ArrayList<DicomWorker>();
		dicomToMove = new LinkedList<Path>();
		numberOfRuns = 0;
		setDicomDaemon(dicomDaemon);
		setStop(false);
		setServerInfo(getDicomDaemon().getServerInfo());
	}

	
	
	
	// Accesseurs
	
	public DicomDaemon getDicomDaemon() {
		return dicomDaemon;
	}

	public void setDicomDaemon(DicomDaemon parentDaemon) {
		this.dicomDaemon = parentDaemon;
	}

	
	public ArrayList<DicomWorker> getDicomWorkerList() {
		return dicomWorkerList;
	}


	public void setDicomWorkerList(ArrayList<DicomWorker> dicomWorkerList) {
		this.dicomWorkerList = dicomWorkerList;
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




	public boolean isStop() {
		return stop;
	}


	public void setStop(boolean stop) {
		this.stop = stop;
	}
	
	// Methodes
	public void run(){
		System.out.println("Dispatcher Online.");
		while(!isStop()){
			while(dicomToMove.isEmpty()){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// on place un delai entre les deplacement pour eviter les erreurs
			// incoherentes liés à un copie trop rapide
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			DicomWorker dworker = new DicomWorker(this, (Path)dicomToMove.pop());
	        dicomWorkerList.add(dworker);
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
	
	public void removeWorker(DicomWorker dicomWorker) {
		if(dicomWorker.getPatientFolder() != null)
			getDicomDaemon().getNiftiDaemon().addDir(dicomWorker.getSerieFolder());
		dicomWorkerList.remove(dicomWorker);
		dicomWorker = null;
	}
	
	public void addDicomToMove(Path p){
		dicomToMove.push(p);
	}
}
