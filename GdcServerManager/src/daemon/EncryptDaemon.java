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
public class EncryptDaemon extends Thread {

	private LinkedList<Path> dicomToEncrypt;
	private LinkedList<DicomImage> dicomImageToEncrypt;
	private DicomEncryptWorker dEncryptWorker;
	private ServerInfo serverInfo;
	private DicomDaemon dicomDaemon;
	private boolean stop;
	
	public EncryptDaemon(DicomDaemon dicomDaemon){
		dicomToEncrypt = new LinkedList<Path>();
		dicomImageToEncrypt = new  LinkedList<DicomImage>();
		stop = false;
		setDicomDaemon(dicomDaemon);
		setServerInfo(getDicomDaemon().getServerInfo());
	}
	
	
	@Override
	public void run() {
		System.out.println("Encrypter Online.");
		while(!isStop()){
			// check si il y a des donnees a encrypter
			while(dicomToEncrypt.isEmpty()){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// on lance l'encryptage du fichier
			dEncryptWorker = new DicomEncryptWorker(this, (Path)dicomToEncrypt.pop(), (DicomImage)dicomImageToEncrypt.pop());
			dEncryptWorker.start();  

		}
	}

	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
	}
	public LinkedList<Path> getDicomToEncrypt() {
		return dicomToEncrypt;
	}
	public void setDicomToEncrypt(LinkedList<Path> dicomToEncrypt) {
		this.dicomToEncrypt = dicomToEncrypt;
	}
	public DicomEncryptWorker getdEncryptWorker() {
		return dEncryptWorker;
	}
	public void setdEncryptWorker(DicomEncryptWorker dEncryptWorker) {
		this.dEncryptWorker = dEncryptWorker;
	}
	public ServerInfo getServerInfo() {
		return serverInfo;
	}
	public LinkedList<DicomImage> getDicomImageToEncrypt() {
		return dicomImageToEncrypt;
	}
	public void setDicomImageToEncrypt(LinkedList<DicomImage> dicomImageToEncrypt) {
		this.dicomImageToEncrypt = dicomImageToEncrypt;
	}
	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}
	public DicomDaemon getDicomDaemon() {
		return dicomDaemon;
	}
	public void setDicomDaemon(DicomDaemon dicomDaemon) {
		this.dicomDaemon = dicomDaemon;
	}
	public void addDicomToEncrypt(Path p, DicomImage di){
		dicomToEncrypt.push(p);
		dicomImageToEncrypt.push(di);
	}
	
	/**
	 * Indique au serveur nifti qu'il faut convertir ce repertoire
	 * @param dicomWorker
	 */
	public void sendToNiftiDaemon(DicomEncryptWorker dEncryptWorker) {
		if(dEncryptWorker.getPatientFolder() != null){
			getDicomDaemon().getNiftiDaemon().addDir(dEncryptWorker.getSerieFolder(),dEncryptWorker.getDicomImage());
		}
		dEncryptWorker = null;
	}

}
