package daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import display.MainWindow;

import settings.SystemSettings;
import settings.WindowManager;

import model.DicomImage;
import model.ServerInfo;


/**
 * Gere le cryptage des fichiers dicom (uniquement)
 * @author Mobilette
 *
 */
public class EncryptDaemon extends Thread {

	private static final String BACKUP_FILE = "encryptDaemon.bak";
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
		loadBackup();
	}
	
	
	@Override
	public void run() {
		System.out.println("Encrypter Online.");
		// on supprime le fichier backup si il existe (car on l'a deja charge)
		File backupfile = new File(SystemSettings.APP_DIR+File.separator+ServerInfo.BACKUP_DIR+File.separator+BACKUP_FILE);
		if(backupfile.exists())
			backupfile.delete();
		while(!isStop()){
			// check si il y a des donnees a encrypter
			while(dicomToEncrypt.isEmpty() && !isStop()){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(isStop())
				return;
			// on lance l'encryptage du fichier
			dEncryptWorker = new DicomEncryptWorker(this, (Path)dicomToEncrypt.pop(), (DicomImage)dicomImageToEncrypt.pop());
			dEncryptWorker.start();  
		}
	}

	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		if(stop = true){
			this.stop = true;
			if(!dicomToEncrypt.isEmpty())
				saveBackup();
			dicomToEncrypt.clear();
			System.out.println("Encrypter offline");
		}else{
			this.stop = stop;
		}
	}

	/**
	 * Sauvegarde le daemon pour une reprise ulterieure
	 * sauvegarde dans le repertoire du programme "/cache"
	 */
	private void saveBackup(){
		Path savePath = Paths.get(SystemSettings.APP_DIR+File.separator+ServerInfo.BACKUP_DIR);
		if(!savePath.toFile().exists())
			savePath.toFile().mkdir();
		
		try{
			// Open a file to write to
			FileOutputStream saveFile=new FileOutputStream(savePath+File.separator+BACKUP_FILE);
	
			// Create an ObjectOutputStream to put objects into save file.
			ObjectOutputStream save = new ObjectOutputStream(saveFile);
			
			// sauvegarde des donnees
			LinkedList<String> toSave = new LinkedList<String>();
			for(Path p:dicomToEncrypt){
				toSave.add(p.toString());
			}
			save.writeObject(toSave);
			save.writeObject(dicomImageToEncrypt);
			
			save.close();
		}catch(IOException e){
			e.printStackTrace();
			new File(savePath+File.separator+BACKUP_FILE).delete();
		}
	}

	/**
	 * Charge des donnees de backup
	 */
	private void loadBackup(){
		Path savePath = Paths.get(SystemSettings.APP_DIR+File.separator+ServerInfo.BACKUP_DIR+File.separator+BACKUP_FILE);
		if(!savePath.toFile().exists())
			return;
		
		try{
			// Open file to read from, named SavedObj.sav.
			FileInputStream saveFile = new FileInputStream(savePath.toString());
	
			// Create an ObjectInputStream to get objects from save file.
			ObjectInputStream save = new ObjectInputStream(saveFile);
			
			LinkedList<String> dicomToEncryptBak = (LinkedList<String>) save.readObject();
			LinkedList<DicomImage> dicomImageToEncryptBak = (LinkedList<DicomImage>) save.readObject();
			save.close();
			
			// On rajoute les donnnees sauvegardees 
			for(String p:dicomToEncryptBak){
				Path lp = Paths.get(p);
				if(!dicomToEncrypt.contains(lp))
					dicomToEncrypt.add(lp);
			}
			for(DicomImage di:dicomImageToEncryptBak){
				if(!dicomImageToEncrypt.contains(di))
					dicomImageToEncrypt.add(di);
			}
			
		}catch(IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
		
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
			if(SystemSettings.NIFTI_DAEMON!=null && SystemSettings.NIFTI_DAEMON.isAlive()){
				SystemSettings.NIFTI_DAEMON.addDir(dEncryptWorker.getSerieFolder(),dEncryptWorker.getDicomImage());
			}else{
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningencrypter().setText("Critical error : Nifti Daemon offline, can't forward ... Please restart");
				WindowManager.MAINWINDOW.getSstatusPanel().setCritical(WindowManager.MAINWINDOW.getSstatusPanel().getBtnEncrypterdaemonstatus());
				while(!(SystemSettings.NIFTI_DAEMON!=null && SystemSettings.NIFTI_DAEMON.isAlive())){
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		dEncryptWorker = null;
	}


	/**
	 * Force l'arret de l'encrypteur
	 * @param b
	 */
	public void forceStop(boolean b) {
		this.stop = true;
	}

	public String getStatus() {
		if(isAlive())
			return dicomToEncrypt.size()+" files to encrypt.";
		else
			return "";
	}
}
