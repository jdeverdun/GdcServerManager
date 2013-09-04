package daemon;

import ij.ImagePlus;
import ij.util.DicomTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

import settings.SystemSettings;
import settings.WindowManager;

import model.DicomImage;
import model.ServerInfo;
import model.daemon.CustomConversionSettings;
import model.daemon.CustomConversionSettings.ServerMode;
import model.daemon.ImportSettings.DicomNamingTag;

import dao.project.DicomImageDAO;
import dao.project.MySQLDicomImageDAO;
import es.vocali.util.AESCrypt;


/**
 * Classe realisant l'encryptage des dicom en mode import
 * @author Mobilette
 *
 */
public class DicomEncryptWorkerImport extends DicomEncryptWorker {
	
	private CustomConversionSettings settings;
	private boolean crashed;
	private Path oldpath; // emplacement du fichier avant un quelconque deplacement
	
	/**
	 * 
	 * @param set
	 * @param oldpath chemin avant encryptage 
	 * @param p chemin vers le fichiers distant en gros le "new path"
	 * @param di
	 */
	public DicomEncryptWorkerImport(CustomConversionSettings set,Path oldpath, Path p, DicomImage di){
		super(p,di);
		settings = set;
		this.setOldpath(oldpath);
		setCrashed(false);
		
	}
	
	@Override
	public void start() {
		System.out.println("Encrypt : "+dicomFile.getFileName());
		setCrashed(false);
		WindowManager.mwLogger.log(Level.FINE,"Encrypt : "+dicomFile.getFileName());
		// AES crypt
		// Encrypte le fichier en rajoutant l'extension definit dans AESCrypt.ENCRYPTSUFFIX
		try {
			AESCrypt aes = new AESCrypt(false, getAESPass());
			aes.encrypt(2, oldpath.toString(), dicomFile+AESCrypt.ENCRYPTSUFFIX);
		
			
			// On ajoute l'entree du DICOM dans la database
			addEntryToDB(dicomFile.getFileName(),"DicomImage");
			
			if(settings.getImportSettings().getNamingTag() == DicomNamingTag.ANONYMIZE)
				Files.delete(oldpath);
			
		} catch (Exception e) {
			WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningencrypter().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)).substring(0, Math.min(e.toString().length(), 100)));
			WindowManager.mwLogger.log(Level.SEVERE, "Exception with encrypter",e);
		}
		
		
		prepareToStop();
	}



	
		
	/**
	 * envoi les infos vers le daemon nifti
	 */
	private void prepareToStop() {
		imp = null;
		// On enleve le worker de la liste des worker et on ajoute
		// le patient à la liste des patients à convertir en nifti
		// On ne le ratjoute que si le workspace du protocole existe prefixe de serverInfo.WORKSPACE_PREFIXE
		
		/* /!\  A decommenter dans la version finale */
		if(new File(getServerInfo().getServerDir() + File.separator + ServerInfo.WORKSPACE_PREFIXE + getProjectFolder().getFileName()).exists()){
			if(getPatientFolder() != null){
				NiftiDaemon niftiToSendTo = settings.getImportSettings().getNiftid();
				if(niftiToSendTo!=null && niftiToSendTo.isAlive()){
					niftiToSendTo.addDir(getSerieFolder(),getDicomImage());
				}else{
					WindowManager.mwLogger.log(Level.SEVERE,"Critical error : Nifti Daemon offline, can't forward ... Please restart");
					setCrashed(true);
				}
			}
		}
	}

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}

	public Path getOldpath() {
		return oldpath;
	}

	public void setOldpath(Path oldpath) {
		this.oldpath = oldpath;
	}

}
