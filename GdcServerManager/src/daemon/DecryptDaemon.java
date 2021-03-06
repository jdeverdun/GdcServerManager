package daemon;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;

import daemon.tools.ThreadPool;
import daemon.tools.ThreadPool.DAEMONTYPE;

import settings.SystemSettings;
import settings.WindowManager;

import es.vocali.util.AESCrypt;
import exceptions.ThreadPoolException;

import model.DicomImage;
import model.ServerInfo;


/**
 * Gere le decryptage des fichiers dicom/nifti (uniquement)
 * @author Mobilette
 *
 */
public class DecryptDaemon extends Thread {
	private static final DAEMONTYPE DTYPE = DAEMONTYPE.DecryptDaemon;
	private LinkedList<Path[]> fileToDecrypt;
	private ServerInfo serverInfo;
	private boolean stop;
	private boolean waiting;// variable pour savoir si on est en etat d'attente (aucune image ne reste a decrypter ou si on travail)
	private int totalEncryptedFile;
	private int pid;
	// permet de savoir si le daemon a crashe
	private boolean crashed;
	private String crashmsg;
	
	public DecryptDaemon(){
		fileToDecrypt = new LinkedList<Path[]>();
		stop = false;
		totalEncryptedFile = 0;
		pid = System.identityHashCode(this); 
		crashed = false;
		setCrashmsg("");
	}
	
	
	@Override
	public void run() {
		WindowManager.mwLogger.log(Level.FINE, "Decrypter Online.");
		setTotalEncryptedFile(0);
		setWaiting(false);
		while(!isStop()){
			// check si il y a des donnees a decrypter
			while(fileToDecrypt.isEmpty()){
				if(isStop()){
					ThreadPool.stopThreadsByPid(pid);
					setTotalEncryptedFile(0);
					return;
				}
				try {
					if(ThreadPool.contains(DTYPE)){
						setWaiting(false);
					}else{
						if(fileToDecrypt.isEmpty()){
							setWaiting(true);
							setTotalEncryptedFile(0);
						}
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			setWaiting(false);
			final Path[] toWork = (Path[])fileToDecrypt.pop();
			Thread tr = new Thread(new Runnable() {
				@Override
				public void run() {
					// on lance le decryptage du fichier
					FileDecryptWorker fileDecryptWorker = new FileDecryptWorker(DecryptDaemon.this, toWork[0], toWork[1]);
					fileDecryptWorker.start();  
				}
			});
			try{
				while(!ThreadPool.addThread(tr,pid,DTYPE) && !isStop() ){//ThreadPool.numberOfThreadFor(DTYPE) >= SystemSettings.AVAILABLE_CORES &&
					try{
						Thread.sleep(50);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				if(isStop())
					ThreadPool.stopThreadsByPid(pid);
				tr.start();
			}catch(ThreadPoolException te){
				// on replace le dernier fichier a encrypter dans la liste (il sera sauvegarder lorsque le nifti daemon se coupera)
				WindowManager.mwLogger.log(Level.SEVERE,"Critical error in Decrypter (addThread) ... ",te);
				SystemSettings.stopDaemons();
			}
		}
	}

	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
		if(stop)
			WindowManager.mwLogger.log(Level.FINE, "Stopping DecryptDaemon");
	}

	public LinkedList<Path[]> getFileToDecrypt() {
		return fileToDecrypt;
	}


	public void setFileToDecrypt(LinkedList<Path[]> fileToDecrypt) {
		this.fileToDecrypt = fileToDecrypt;
	}


	public ServerInfo getServerInfo() {
		return serverInfo;
	}


	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}


	public void addFileToDecrypt(Path source, Path to){
		// Si le fichier ne contient pas l'extension ".enc" on la rajoute
		if(!source.toString().endsWith(AESCrypt.ENCRYPTSUFFIX))
			source = Paths.get(source.getParent().toString()+File.separator+source.getFileName()+AESCrypt.ENCRYPTSUFFIX);
		setWaiting(false);
		totalEncryptedFile++;
		fileToDecrypt.push(new Path[]{source,to});
	}


	public void cleanList() {
		fileToDecrypt.clear();
		setTotalEncryptedFile(0);
		ThreadPool.stopThreadsByPid(pid);
	}


	public String getStatus() {
		if(isAlive())
			return fileToDecrypt.size()+" files to decrypt.";
		else
			return "";
	}


	public boolean isWaiting() {
		return waiting;
	}


	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}


	/**
	 * @return the totalEncryptedFile
	 */
	public int getTotalEncryptedFile() {
		return totalEncryptedFile;
	}


	/**
	 * @param totalEncryptedFile the totalEncryptedFile to set
	 */
	public void setTotalEncryptedFile(int totalEncryptedFile) {
		this.totalEncryptedFile = totalEncryptedFile;
	}
	
	
	public int getDoneEncryptedFile(){
		return getTotalEncryptedFile() - (getFileToDecrypt().size() + ThreadPool.numberOfThreadFor(pid));
	}


	/**
	 * @return the crashed
	 */
	public boolean isCrashed() {
		return crashed;
	}


	/**
	 * @param crashed the crashed to set
	 * @param crashmsg 
	 */
	public void setCrashed(boolean crashed, String crashmsg) {
		this.crashed = crashed;
		if(crashed)
			this.setCrashmsg(crashmsg);
		else
			this.setCrashmsg("");
	}


	/**
	 * @return the crashmsg
	 */
	public String getCrashmsg() {
		return crashmsg;
	}


	/**
	 * @param crashmsg the crashmsg to set
	 */
	public void setCrashmsg(String crashmsg) {
		this.crashmsg = crashmsg;
	}



}
