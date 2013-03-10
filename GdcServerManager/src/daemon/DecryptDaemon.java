package daemon;

import java.nio.file.Path;
import java.util.LinkedList;

import model.DicomImage;
import model.ServerInfo;


/**
 * Gere le cryptage des fichiers dicom (uniquement)
 * @author Mobilette
 *
 */
public class DecryptDaemon extends Thread {

	private LinkedList<Path[]> fileToDecrypt;
	private FileDecryptWorker fileDecryptWorker;
	private ServerInfo serverInfo;
	private boolean stop;
	
	public DecryptDaemon(DicomDaemon dicomDaemon){
		fileToDecrypt = new LinkedList<Path[]>();
		stop = false;
	}
	
	
	@Override
	public void run() {
		System.out.println("Encrypter Online.");
		while(!isStop()){
			// check si il y a des donnees a encrypter
			while(fileToDecrypt.isEmpty()){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// on lance le decryptage du fichier
			fileDecryptWorker = new FileDecryptWorker(this, (Path)fileToDecrypt.pop()[0], (Path)fileToDecrypt.pop()[1]);
			fileDecryptWorker.start();  

		}
	}

	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public LinkedList<Path[]> getFileToDecrypt() {
		return fileToDecrypt;
	}


	public void setFileToDecrypt(LinkedList<Path[]> fileToDecrypt) {
		this.fileToDecrypt = fileToDecrypt;
	}


	public FileDecryptWorker getFileDecryptWorker() {
		return fileDecryptWorker;
	}


	public void setFileDecryptWorker(FileDecryptWorker fileDecryptWorker) {
		this.fileDecryptWorker = fileDecryptWorker;
	}


	public ServerInfo getServerInfo() {
		return serverInfo;
	}


	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}


	public void addFileToDecrypt(Path source, Path to){
		fileToDecrypt.push(new Path[]{source,to});
	}
	

}
