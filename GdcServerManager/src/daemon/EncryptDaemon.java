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
import java.util.logging.Level;

import javax.swing.JOptionPane;

import display.MainWindow;

import settings.SystemSettings;
import settings.WindowManager;

import model.DicomImage;
import model.ServerInfo;
import model.daemon.CustomConversionSettings;
import model.daemon.CustomConversionSettings.ServerMode;


/**
 * Gere le cryptage des fichiers dicom (uniquement)
 * @author Mobilette
 *
 */
public class EncryptDaemon extends Thread {

	private static final String BACKUP_FILE = "encryptDaemon.bak";
	private CustomConversionSettings settings; // pour import only
	private LinkedList<Path> dicomToEncrypt;
	private LinkedList<DicomImage> dicomImageToEncrypt;
	private DicomEncryptWorker dEncryptWorker;
	private ServerInfo serverInfo;
	private DicomDaemon dicomDaemon;
	private boolean stop;
	private boolean waiting;// variable pour savoir si on est en etat d'attente (aucune image ne reste a encrypter ou si on travail)
	private boolean crashed; // pour l'import permet de savoir si le daemon a crashe
	
	public EncryptDaemon(DicomDaemon dicomDaemon){
		dicomToEncrypt = new LinkedList<Path>();
		dicomImageToEncrypt = new  LinkedList<DicomImage>();
		stop = false;
		setDicomDaemon(dicomDaemon);
		setServerInfo(getDicomDaemon().getServerInfo());
		crashed = false;
	}
	
	public EncryptDaemon(){
		dicomToEncrypt = new LinkedList<Path>();
		dicomImageToEncrypt = new  LinkedList<DicomImage>();
		stop = false;
		setDicomDaemon(dicomDaemon);
		setServerInfo(SystemSettings.SERVER_INFO);
		crashed = false;
	}
	
	
	public EncryptDaemon(CustomConversionSettings ccs) {
		dicomToEncrypt = new LinkedList<Path>();
		dicomImageToEncrypt = new  LinkedList<DicomImage>();
		stop = false;
		setDicomDaemon(null);
		setSettings(ccs);
		setServerInfo(SystemSettings.SERVER_INFO);
	}

	@Override
	public void run() {
		WindowManager.mwLogger.log(Level.INFO, "Encrypter Online.");
		// on charge le backup si on est en mode server
		if(settings.getServerMode() == ServerMode.SERVER){
			loadBackup();
			// on supprime le fichier backup si il existe (car on l'a deja charge)
			File backupfile = new File(SystemSettings.APP_DIR+File.separator+ServerInfo.BACKUP_DIR+File.separator+BACKUP_FILE);
			if(backupfile.exists())
				backupfile.delete();
		}
		setWaiting(false);
		NiftiDaemon niftid = SystemSettings.NIFTI_DAEMON;
		while(!isStop()){
			// check si il y a des donnees a encrypter
			while(dicomToEncrypt.isEmpty() && !isStop()){
				try {
					setWaiting(true);
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(isStop())
				return;
			setWaiting(false);
			// on s'assure que le niftidaemon est online avant de lancer le traitement
			// afin d'eviter au maximum les erreurs
			// on attend le retour online du daemon pour continuer

			while(!(niftid!=null && niftid.isAlive())){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(isStop()){
					break;
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
		if(stop = true){
			this.stop = true;
			if(!dicomToEncrypt.isEmpty() && settings.getServerMode() == ServerMode.SERVER)
				saveBackup();
			dicomToEncrypt.clear();
			WindowManager.mwLogger.log(Level.INFO, "Stopping Encrypter");
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
			WindowManager.mwLogger.log(Level.SEVERE, "saveBackup error",e);
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
			WindowManager.mwLogger.log(Level.SEVERE, "loadBackup error",e);
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
	/**
	 * @return the settings
	 */
	public CustomConversionSettings getSettings() {
		return settings;
	}

	/**
	 * @param settings the settings to set
	 */
	public void setSettings(CustomConversionSettings settings) {
		this.settings = settings;
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
		setWaiting(false);
	}
	
	/**
	 * Indique au serveur nifti qu'il faut convertir ce repertoire
	 * @param dicomWorker
	 */
	public void sendToNiftiDaemon(DicomEncryptWorker dEncryptWorker) {
		if(dEncryptWorker.getPatientFolder() != null){
			NiftiDaemon niftiToSendTo = SystemSettings.NIFTI_DAEMON;
			if(settings != null && settings.getServerMode() == ServerMode.IMPORT)
				niftiToSendTo = settings.getImportSettings().getNiftid();
			if(niftiToSendTo!=null && niftiToSendTo.isAlive()){
				niftiToSendTo.addDir(dEncryptWorker.getSerieFolder(),dEncryptWorker.getDicomImage());
			}else{
				WindowManager.mwLogger.log(Level.SEVERE,"Critical error : Nifti Daemon offline, can't forward ... Please restart");
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningencrypter().setText("Critical error : Nifti Daemon offline, can't forward ... Please restart");
				WindowManager.MAINWINDOW.getSstatusPanel().setCritical(WindowManager.MAINWINDOW.getSstatusPanel().getBtnEncrypterdaemonstatus());
				// on attend tant que le nifti daemon n'est pas online
				while(!(niftiToSendTo!=null && niftiToSendTo.isAlive())){
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(isStop()){
						break;
					}
				}
				if(niftiToSendTo!=null && niftiToSendTo.isAlive())
					niftiToSendTo.addDir(dEncryptWorker.getSerieFolder(),dEncryptWorker.getDicomImage());
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


	public boolean isWaiting() {
		return waiting;
	}


	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}
}
