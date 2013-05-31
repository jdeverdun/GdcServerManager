package daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

import exceptions.DicomException;

import settings.SystemSettings;
import settings.WindowManager;


import model.DicomImage;
import model.ServerInfo;
import model.daemon.CustomConversionSettings;
import model.daemon.CustomConversionSettings.ServerMode;


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
	private ConcurrentLinkedQueue<Path> dicomToMove;
	private int numberOfRuns; // nombre de threads qui ont été lancé
	private int maxWorker; // nombre de coeurs disponibles [obsolete]
	private DicomWorker dworker;
	private boolean stop;
	private int waitCounter;
	private CustomConversionSettings settings; // Parametres pour le DicomJobDispatcher 
	private NiftiDaemon niftiDaemon;
	private boolean crashed; // savoir si le daemon a crashe, pour l'import



	public DicomJobDispatcher(DicomDaemon dicomDaemon) {
		dicomToMove = new ConcurrentLinkedQueue<Path>();
		numberOfRuns = 0;
		setDicomDaemon(dicomDaemon);
		setSettings(new CustomConversionSettings());
		setStop(false);
		setServerInfo(getDicomDaemon().getServerInfo());
		setMaxWorker(SystemSettings.AVAILABLE_CORES);
		crashed = false;
		
	}
	
	/**
	 * Principalement pour l'import
	 * @param settings
	 */
	public DicomJobDispatcher(CustomConversionSettings settings) {
		dicomToMove = new ConcurrentLinkedQueue<Path>();
		numberOfRuns = 0;
		setSettings(settings);
		setStop(false);
		setServerInfo(SystemSettings.SERVER_INFO);
		setMaxWorker(SystemSettings.AVAILABLE_CORES);
		crashed = false;
		
	}

	/**
	 *  principalement pour mode conversion only
	 * @param si
	 * @param settings
	 * @param nifti
	 */
	public DicomJobDispatcher(ServerInfo si,CustomConversionSettings settings, NiftiDaemon nifti) {
		setNiftiDaemon(nifti);
		dicomToMove = new ConcurrentLinkedQueue<Path>();
		numberOfRuns = 0;
		setSettings(settings);
		setDicomDaemon(null);
		setStop(false);
		setServerInfo(si);
		setMaxWorker(SystemSettings.AVAILABLE_CORES);
		DicomWorkerClient.DICOMDIR = null;
		crashed = false;
	}
	
	
	// Accesseurs
	
	public DicomDaemon getDicomDaemon() {
		return dicomDaemon;
	}

	public void setDicomDaemon(DicomDaemon parentDaemon) {
		this.dicomDaemon = parentDaemon;
	}


	public ConcurrentLinkedQueue<Path> getDicomToMove() {
		return dicomToMove;
	}


	public void setDicomToMove(ConcurrentLinkedQueue<Path> dicomToMove) {
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

	public CustomConversionSettings getSettings() {
		return settings;
	}

	public void setSettings(CustomConversionSettings settings) {
		this.settings = settings;
	}

	public boolean isStop() {
		return stop;
	}


	public void setStop(boolean stop) {
		this.stop = stop;
		if(stop)
			WindowManager.mwLogger.log(Level.INFO, "Stopping DicomJobDispatcher");
	}
	
	// Methodes
	public void run(){
		WindowManager.mwLogger.log(Level.INFO, "Dispatcher Online.");
		while(!isStop()){
			// check si il y a des donnees a deplacer
			while(dicomToMove.isEmpty() && !isStop()){
				if(waitCounter>10000 && getSettings().getServerMode() == ServerMode.SERVER){
					//Si aucun fichier n'a ete ajouter depuis plus de 10 sec
					// on verifie si on a pas rate des event a cause d'overflow
					try {
						checkForMissedFiles();
					} catch (IOException e) {
						WindowManager.mwLogger.log(Level.WARNING, "Error with missed file checking in dispatcher",e);
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
			if(getSettings().getServerMode() == ServerMode.SERVER || getSettings().getServerMode() == ServerMode.IMPORT ){
				Path locp = (Path)dicomToMove.poll();
				// tant qu'on ne peut pas lire le fichier on attend
				// permet de gerer les problemes d'acces
				boolean cont=true;
				while(cont){
					try{
						try{
							if(!DicomImage.isDicom(locp.toFile())){
								locp.toFile().delete();
							}else{
								try {
									dworker = new DicomWorker(this, locp);
									dworker.start();
								} catch (DicomException e) {
									if(getSettings().getServerMode() == ServerMode.SERVER){
										// on gere le fait que les fichiers peuvent etre tagge comme dicom mais 
										// a cause d'une erreur de copie ne contiennent pas tout les champs
										WindowManager.mwLogger.log(Level.WARNING, locp+" : corrupted ... deleted",e);
										WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
										// on supprime le fichier dans le buffer (si il est present) 
										if(locp.toFile().exists())
											locp.toFile().delete();
										if(dworker.getNewPath()!=null && dworker.getNewPath().toFile().exists()){
											dworker.getNewPath().toFile().delete();
										}
									}else{
										WindowManager.mwLogger.log(Level.WARNING, locp+" : corrupted ... skipped [IMPORT]",e);
									}
									cont=false;
								}
							}
							cont=false;
						}catch(FileNotFoundException fe){
							cont=false;
						}
					}catch(IOException e){
						try {
							Thread.sleep(50);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}else{
				Path locp = (Path)dicomToMove.poll();
				try {
					try {
						dworker = new DicomWorkerClient(this, locp);
						dworker.start();
					} catch (DicomException e) {
						if(settings.isDicomDebugMode())
							WindowManager.mwLogger.log(Level.WARNING, locp+" : corrupted ... deleted");
					}
				} catch (FileNotFoundException e1) {
					WindowManager.mwLogger.log(Level.WARNING, "IOException",e1);
				}
		        
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
			File lfile = new File(getServerInfo().getIncomingDir() + File.separator + name);
			if(name.length()>2 && !name.endsWith(".part")){//on s'assure que ce n'est pas des fichiers en cours de copie ".part" ni des ..
				try{
					if(DicomImage.isDicom(lfile)){
						String fullpath = getServerInfo().getIncomingDir() + File.separator + name;
						addDicomToMove(Paths.get(fullpath));
					}else{
						lfile.delete();
					}
				}catch(Exception e){
					continue;
				}
			}
		}
	}

	
	public void addDicomToMove(Path p){
		if(!dicomToMove.contains(p)){
			dicomToMove.add(p);
		}
	}

	/**
	 * Force l'arret du dispatcher
	 * @param b
	 */
	public void forceStop(boolean b) {
		this.stop = true;
		WindowManager.mwLogger.log(Level.INFO, "Stopping DicomJobDispatcher");
	}
	
	public String getStatus() {
		if(isAlive())
			return dicomToMove.size()+" files to move.";
		else
			return "";
	}

	public void setCrashed(boolean b) {
		this.crashed = b;
		setStop(b);
	}
	public boolean isCrashed(){
		return this.crashed;
	}
}
