package daemon;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

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
		WindowManager.mwLogger.log(Level.INFO, "Missing Daemon Online.");
		while(!isStop()){
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
				moveNotEncodedDicom(listenDirectory.toFile());
			} catch (Exception e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningMissingDaemon().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)).substring(0, Math.min(e.toString().length(), 100)));
				WindowManager.mwLogger.log(Level.WARNING, "Missing Daemon error",e);
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
			WindowManager.mwLogger.log(Level.INFO, "Stopping Missing Daemon");
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
						File fileTo = new File(SystemSettings.SERVER_INFO.getIncomingDir()+File.separator+fi.getName()+".part");
						// on s'assure que rien ne va bloquer le deplacement
						if(fi.exists() && fi.canWrite() && !new File(fi.toString()+AESCrypt.ENCRYPTSUFFIX).exists()){
							// on deplace en rajoutant l'extension .part tant que la copie n'est pas termine
							FileUtils.moveFile(fi, fileTo);
							File renameFile = new File(fileTo.toString().substring(0,fileTo.toString().length()-5)); // nom sans le .part
							if(!fileTo.renameTo(renameFile))// securite
								throw new Exception("Unable to rename "+fileTo.toString()+" to "+renameFile.toString());
							nbMoved++;
						}
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