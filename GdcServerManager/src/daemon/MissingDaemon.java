package daemon;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import es.vocali.util.AESCrypt;

import model.ServerInfo;

import settings.SystemSettings;
import settings.WindowManager;

/**
 * Classe parcourrant le serveur dicom a le recherche de fichiers non encrypte (et pas en attente de decryptage)
 * pour les replacer dans l'incoming dir pour les retraiter
 * @author Mobilette
 *
 */
public class MissingDaemon extends Thread{

	private static final int DELAY = 10000; // en ms temps entre les iteration
	private boolean stop; // permet de stopper proprement le thread
	private Path listenDirectory; // repertoire dans lequel rechercher les fichiers non encrypte
	private int nbIteration; // nombre de parcours de l'arborescence qu'a fait le programme
	private int nbMoved; // nombre de fichiers deplace
	
	public MissingDaemon(){
		setStop(false);
		listenDirectory = Paths.get(SystemSettings.SERVER_INFO.getServerDir()+File.separator+ServerInfo.NRI_DICOM_NAME);
		nbIteration = 0;
		nbMoved = 0;
	}
	public MissingDaemon(Path p){
		setStop(false);
		listenDirectory = p;
		nbIteration = 0;
		nbMoved = 0;
	}
	
	public void run(){
		System.out.println("Missing Daemon Online");
		while(!isStop()){
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
				moveNotEncodedDicom(listenDirectory.toFile());
			} catch (Exception e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningMissingdaemon().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)).substring(0, Math.min(e.toString().length(), 100)));
				e.printStackTrace();
			}
			nbIteration++;
		}
	}

	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
		if(this.stop)
			System.out.println("Stopping Missing Daemon.");
	}
	
	public int getNbIteration() {
		if(nbIteration>1000000)
			nbIteration = 0;
		return nbIteration;
	}
	public void setNbIteration(int nbIteration) {
		this.nbIteration = nbIteration;
	}
	public int getNbMoved() {
		if(nbMoved>1000000)
			nbMoved = 0;
		return nbMoved;
	}
	public void setNbMoved(int nbMoved) {
		this.nbMoved = nbMoved;
	}
	/**
	 * Recherche de maniere iterative les dicom non encrypte (et pas en attente de decryptage)
	 *  et les deplace dans le repertoire d'incoming
	 * @param dir
	 * @throws Exception 
	 */
	private void moveNotEncodedDicom(File dir) throws Exception {
		if(isStop())
			return;
		if(!dir.isDirectory())
			return;
		for(File fi:dir.listFiles()){
			if(isStop())
				return;
			if(fi.isDirectory()){
				moveNotEncodedDicom(fi);
			}else{
				if(!fi.toString().endsWith(AESCrypt.ENCRYPTSUFFIX) && !SystemSettings.ENCRYPT_DAEMON.getDicomToEncrypt().contains(fi.toPath())){
					try{
						FileUtils.moveFileToDirectory(fi, SystemSettings.SERVER_INFO.getIncomingDir().toFile(), false);
						nbMoved++;
					}catch(Exception e){
						throw e;
					}
				}
			}
		}
	}
	
	/**
	 * Renvoi le status actuelle du thread
	 * @return
	 */
	public String getStatus() {
		if(this.isAlive())
			return getNbIteration()+" it | "+getNbMoved()+" moved";
		else
			return "";
	}
}
