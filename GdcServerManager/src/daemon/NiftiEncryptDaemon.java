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
import java.util.logging.Level;

import javax.swing.JOptionPane;

import daemon.tools.ThreadPool;
import daemon.tools.ThreadPool.DAEMONTYPE;
import display.MainWindow;
import exceptions.ThreadPoolException;

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
public class NiftiEncryptDaemon extends EncryptDaemon {
	private static final DAEMONTYPE DTYPE = DAEMONTYPE.NiftiEncryptDaemon;
	private LinkedList<Path> niftiToEncrypt;
	private LinkedList<DicomImage> sourceDicomImageList;
	private LinkedList<Path> outDirList;
	private DicomDaemon dicomDaemon;
	
	public NiftiEncryptDaemon(){
		super();
		niftiToEncrypt = new LinkedList<Path>();
		sourceDicomImageList = new LinkedList<DicomImage>();
		outDirList = new LinkedList<Path>();
		setServerInfo(SystemSettings.SERVER_INFO);
	}
	
	@Override
	public void run() {
		WindowManager.mwLogger.log(Level.FINE, "Nifti Encrypter Online.");
		setWaiting(false);
		while(!isStop()){
			// check si il y a des donnees a encrypter
			while(niftiToEncrypt.isEmpty() && !isStop()){
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

			// on essai de lancer autant de worker qu'il y a de coeurs
			// permet de securiser le thread
			final Path lpath = (Path)niftiToEncrypt.pop();
			final Path opath = (Path)outDirList.pop();
			final DicomImage di = (DicomImage)sourceDicomImageList.pop();
			Thread tr = new Thread(new Runnable() {
				@Override
				public void run() {
					// on lance l'encryptage du fichier
					NiftiEncryptWorker dWorker = new NiftiEncryptWorker(NiftiEncryptDaemon.this,lpath,opath,di);
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
				// on replace le dernier fichier a encrypter dans la liste (il sera sauvegarder lorsque le nifti daemon se coupera)
				WindowManager.mwLogger.log(Level.SEVERE,"Critical error in NiftiEncryptDaemon (addThread) ... ",te);
				if(SystemSettings.NIFTI_DAEMON!=null && SystemSettings.NIFTI_DAEMON.isAlive())
					SystemSettings.NIFTI_DAEMON.addDir(Paths.get(opath.toString().replaceAll(SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME, SystemSettings.SERVER_INFO.NRI_DICOM_NAME)), di);
				SystemSettings.stopDaemons();
				setStop(true);
			}
		}
	}

	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		if(stop = true){
			this.stop = true;
			niftiToEncrypt.clear();
			outDirList.clear();
			sourceDicomImageList.clear();
			WindowManager.mwLogger.log(Level.FINE, "Stopping Nifti Encrypter");
		}else{
			this.stop = stop;
		}
	}
	public LinkedList<Path> getNiftiToEncrypt() {
		return niftiToEncrypt;
	}
	public void setNiftiToEncrypt(LinkedList<Path> niftiToEncrypt) {
		this.niftiToEncrypt = niftiToEncrypt;
	}
	public DicomDaemon getDicomDaemon() {
		return dicomDaemon;
	}
	public void setDicomDaemon(DicomDaemon dicomDaemon) {
		this.dicomDaemon = dicomDaemon;
	}
	public void addNiftiToEncrypt(Path p,Path toDir, DicomImage source){
		if(!niftiToEncrypt.contains(p)){
			niftiToEncrypt.push(p);
			outDirList.push(toDir);
			sourceDicomImageList.push(source);
			setWaiting(false);
		}
	}

	/**
	 * On rajoute un fichier a encrypter qui n'est pas un nifti mais un fichier associe a un nifti (on ne
	 * le rajoute donc pas dans la base)
	 * @param string
	 * @param niftiPath
	 */
	public void addNiftiToEncrypt(Path p, Path toDir) {
		if(!niftiToEncrypt.contains(p)){
			niftiToEncrypt.push(p);
			outDirList.push(toDir);
			sourceDicomImageList.push(null);
			setWaiting(false);
		}
	}
	public String getStatus() {
		if(isAlive())
			return niftiToEncrypt.size()+" files to encrypt.";
		else
			return "";
	}

}
