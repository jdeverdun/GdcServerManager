package daemon;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;

import settings.SystemSettings;
import settings.WindowManager;

import es.vocali.util.AESCrypt;

import model.DicomImage;
import model.ServerInfo;


/**
 * Gere le decryptage des fichiers dicom/nifti (uniquement)
 * @author Mobilette
 *
 */
public class DecryptDaemon extends Thread {

	private LinkedList<Path[]> fileToDecrypt;
	private ServerInfo serverInfo;
	private boolean stop;
	private boolean waiting;// variable pour savoir si on est en etat d'attente (aucune image ne reste a decrypter ou si on travail)
	private ArrayList<Thread> workers;
	private int maxWorkers;
	
	
	public DecryptDaemon(){
		fileToDecrypt = new LinkedList<Path[]>();
		stop = false;
		workers = new ArrayList<Thread>();
		maxWorkers = SystemSettings.AVAILABLE_CORES;
	}
	
	
	@Override
	public void run() {
		WindowManager.mwLogger.log(Level.INFO, "Decrypter Online.");
		setWaiting(false);
		while(!isStop()){
			// check si il y a des donnees a decrypter
			while(fileToDecrypt.isEmpty()){
				if(isStop())
					return;
				try {
					setWaiting(true);
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			setWaiting(false);
			while(workers!=null && workers.size()<maxWorkers && !fileToDecrypt.isEmpty() && !isStop()){
				try {
					// limite l'overhead
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// on essai de lancer autant de worker qu'il y a de coeurs
				for(int i=0;i<maxWorkers;i++){
					if(!fileToDecrypt.isEmpty() && (workers.isEmpty() || (i>workers.size()) || (i<workers.size() && !workers.get(i).isAlive()))){
						if(i<workers.size())
							workers.remove(i);
						// permet de securiser le thread
						final Path[] toWork = (Path[])fileToDecrypt.pop();
						Thread tr = new Thread(new Runnable() {
							@Override
							public void run() {
								// on lance le decryptage du fichier
								FileDecryptWorker fileDecryptWorker = new FileDecryptWorker(DecryptDaemon.this, toWork[0], toWork[1]);
								fileDecryptWorker.start();  
							}
						});
						workers.add(tr);
						tr.start();
					}
				}				
			}
		}
	}

	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
		if(stop)
			WindowManager.mwLogger.log(Level.INFO, "Stopping DecryptDaemon");
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
		fileToDecrypt.push(new Path[]{source,to});
	}


	public void cleanList() {
		fileToDecrypt.clear();
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
	

}
