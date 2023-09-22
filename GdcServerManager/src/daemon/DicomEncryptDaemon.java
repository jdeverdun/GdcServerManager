package daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import daemon.tools.ThreadPool;
import daemon.tools.ThreadPool.DAEMONTYPE;
import display.MainWindow;
import exceptions.ThreadPoolException;

import settings.SQLSettings;
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
public class DicomEncryptDaemon extends EncryptDaemon {
	private static final DAEMONTYPE DTYPE = DAEMONTYPE.DicomEncryptDaemon;
	private static final String BACKUP_FILE = "encryptDaemon.bak";
	private CustomConversionSettings settings; // pour import only
	private ConcurrentLinkedQueue<Path> dicomToEncrypt;
	private ConcurrentLinkedQueue<DicomImage> dicomImageToEncrypt;
	private DicomDaemon dicomDaemon;
	
	public DicomEncryptDaemon(DicomDaemon dicomDaemon){
		super();
		dicomToEncrypt = new ConcurrentLinkedQueue<Path>();
		dicomImageToEncrypt = new  ConcurrentLinkedQueue<DicomImage>();
		setDicomDaemon(dicomDaemon);
		setServerInfo(getDicomDaemon().getServerInfo());
	}
	
	public DicomEncryptDaemon(){
		super();
		dicomToEncrypt = new ConcurrentLinkedQueue<Path>();
		dicomImageToEncrypt = new  ConcurrentLinkedQueue<DicomImage>();
		setDicomDaemon(dicomDaemon);
		setServerInfo(SystemSettings.SERVER_INFO);
	}
	
	
	public DicomEncryptDaemon(CustomConversionSettings ccs) {
		super();
		dicomToEncrypt = new ConcurrentLinkedQueue<Path>();
		dicomImageToEncrypt = new  ConcurrentLinkedQueue<DicomImage>();
		setDicomDaemon(null);
		setSettings(ccs);
		setServerInfo(SystemSettings.SERVER_INFO);
	}

	@Override
	public void run() {
		WindowManager.mwLogger.log(Level.INFO, "Encrypter Online.");
		// on charge le backup si on est en mode server
		loadBackup();
		// on supprime le fichier backup si il existe (car on l'a deja charge)
		File backupfile = new File(SystemSettings.APP_DIR+File.separator+ServerInfo.BACKUP_DIR+File.separator+BACKUP_FILE);
		if(backupfile.exists())
			backupfile.delete();
		setWaiting(false);
		NiftiDaemon niftid = SystemSettings.NIFTI_DAEMON;
		while(!isStop()){
			// check si il y a des donnees a encrypter
			while(dicomToEncrypt.isEmpty() && !isStop()){
				try {
					if(ThreadPool.contains(DTYPE))
						setWaiting(false);
					else
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
			// permet de securiser le thread
			try {
				final Path lpath = (Path)dicomToEncrypt.poll();
				if(lpath==null) {
					continue;
				}
				final DicomImage di = (DicomImage)dicomImageToEncrypt.poll();

			
				Thread tr = new Thread(new Runnable() {
					@Override
					public void run() {
						// on lance l'encryptage du fichier
						DicomEncryptWorker dWorker = new DicomEncryptWorker(DicomEncryptDaemon.this,lpath,di);
						dWorker.start();  
					}
				});
				try{
					while(!ThreadPool.addThread(tr,getPid(),DTYPE) && !isStop() ){
						try{
							Thread.sleep(50);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					tr.start();
				}catch(ThreadPoolException te){
					// on replace le dernier fichier a encrypter dans la liste (avant de sauvegarder)
					dicomToEncrypt.add(lpath);
					WindowManager.mwLogger.log(Level.SEVERE,"Critical error in DicomEncryptDaemon (addThread) ... saving & shutdown",te);
					SystemSettings.stopDaemons();
				}
			}catch(NoSuchElementException e) {
				// on gere le bug qui fait tout crasher, surement à cause de thread concurrent
				e.printStackTrace();
				WindowManager.mwLogger.log(Level.SEVERE,"La fameuse erreur ! NoSuchElementException!",e);
				continue;
			}
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
	public ConcurrentLinkedQueue<Path> getDicomToEncrypt() {
		return dicomToEncrypt;
	}
	public void setDicomToEncrypt(ConcurrentLinkedQueue<Path> dicomToEncrypt) {
		this.dicomToEncrypt = dicomToEncrypt;
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

	public ConcurrentLinkedQueue<DicomImage> getDicomImageToEncrypt() {
		return dicomImageToEncrypt;
	}
	public void setDicomImageToEncrypt(ConcurrentLinkedQueue<DicomImage> dicomImageToEncrypt) {
		this.dicomImageToEncrypt = dicomImageToEncrypt;
	}
	public DicomDaemon getDicomDaemon() {
		return dicomDaemon;
	}
	public void setDicomDaemon(DicomDaemon dicomDaemon) {
		this.dicomDaemon = dicomDaemon;
	}
	public void addDicomToEncrypt(Path p, DicomImage di){
		if(isWaiting()){
			if(!SQLSettings.connectionIsWorking()){
				setCrashed(true);
				setStop(true);
				return;
			}
		}
		if(!dicomToEncrypt.contains(p)){
			dicomToEncrypt.add(p);
			dicomImageToEncrypt.add(di);
			setWaiting(false);
		}
	}
	
	/**
	 * Indique au serveur nifti qu'il faut convertir ce repertoire
	 * @param dicomWorker
	 */
	public void sendToNiftiDaemon(DicomEncryptWorker dEncryptWorker) {
		if(dEncryptWorker.getPatientFolder() != null){
			NiftiDaemon niftiToSendTo = SystemSettings.NIFTI_DAEMON;
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

	public String getStatus() {
		if(isAlive())
			return dicomToEncrypt.size()+" files to encrypt.";
		else
			return "";
	}
}
