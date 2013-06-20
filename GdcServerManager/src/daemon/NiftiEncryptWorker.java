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

import daemon.tools.nifti.Nifti_Reader;
import dao.project.DicomImageDAO;
import dao.project.MySQLDicomImageDAO;
import dao.project.MySQLNiftiImageDAO;
import dao.project.MySQLSerieDAO;
import dao.project.NiftiImageDAO;
import dao.project.SerieDAO;
import es.vocali.util.AESCrypt;


/**
 * Classe realisant l'encryptage des nifti
 * @author Mobilette
 *
 */
public class NiftiEncryptWorker extends DaemonWorker {

	protected Path niftiFile;
	protected Path toDir;
	protected DicomImage sourceDicomImage;
	private Nifti_Reader nr;
	private NiftiEncryptDaemon encryptDaemon;
	
	
	public NiftiEncryptWorker(NiftiEncryptDaemon disp, Path p,Path to, DicomImage di){
		niftiFile = p;
		toDir = to;
		sourceDicomImage = di;
		encryptDaemon = disp;
		// on definit le nom de projet
		Path studyName = to.getParent().getParent().getParent().getParent().getFileName();
		setProjectFolder(studyName);
		setPatientFolder(to.getParent().getParent().getParent());
		setServerInfo(getEncryptDaemon().getServerInfo());
	}
	
	@Override
	public void start() {
		System.out.println("Encrypt : "+niftiFile.getFileName());
		WindowManager.mwLogger.log(Level.FINE,"Encrypt : "+niftiFile.getFileName());
		// AES crypt
		// Encrypte le fichier en rajoutant l'extension definit dans AESCrypt.ENCRYPTSUFFIX
		try {
			if(sourceDicomImage!=null){
				if(nr!=null)
					nr = null;
				nr = new Nifti_Reader(niftiFile.toFile());
			}
			AESCrypt aes = new AESCrypt(false, getAESPass());
			aes.encrypt(2, niftiFile.toString(), toDir+File.separator+niftiFile.getFileName()+AESCrypt.ENCRYPTSUFFIX);
			// On supprime le fichier non encrypte
			FileUtils.deleteQuietly(niftiFile.toFile());
			//Files.deleteIfExists(dicomFile);
			
			
			if(sourceDicomImage!=null){
				// On ajoute l'entree du DICOM dans la database si c'est bien un nifti (sourcedicomimage !=null)
				addEntryToDB(niftiFile.getFileName(),"NiftiImage");
			}
			
			
		} catch (Exception e) {
			// Si le cryptage ne reussi pas je supprime le fichier
			try {
				Files.deleteIfExists(niftiFile);
			} catch (IOException e1) {
				WindowManager.mwLogger.log(Level.WARNING, "Can't delete "+niftiFile,e1);
			}
			WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningencrypter().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)).substring(0, Math.min(e.toString().length(), 100)));
			WindowManager.mwLogger.log(Level.SEVERE, "Exception with encrypter",e);
		}
		
		
		prepareToStop();
	}


	public NiftiEncryptDaemon getEncryptDaemon() {
		return encryptDaemon;
	}

	public void setEncryptDaemon(NiftiEncryptDaemon encryptDaemon) {
		this.encryptDaemon = encryptDaemon;
	}

	@Override
	protected void addEntryToDB(Path name, String table) {
		switch(table){
		case "NiftiImage":
			NiftiImageDAO dicdao = new MySQLNiftiImageDAO();
			SerieDAO sdao = new MySQLSerieDAO();
			try {
				dicdao.newNiftiImage(name.getFileName().toString(), nr.getNSlices(),sourceDicomImage.getProjet().getId(),sourceDicomImage.getPatient().getId(),
						sourceDicomImage.getAcquistionDate().getId(),sourceDicomImage.getProtocole().getId(),sourceDicomImage.getSerie().getId());
				// on indique a la serie qu'elle est dispo en nifti
				sdao.updateHasNifti(sourceDicomImage.getSerie().getId(),1);
			} catch (SQLException e) {
				WindowManager.mwLogger.log(Level.SEVERE,"SQLException with niftiWorker",e);
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningniftidaemon().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
			}
			break;
		default:
			WindowManager.mwLogger.log(Level.SEVERE, "I don't know table : "+table+" ... sorry");	
			
		}
	}
	
	
	
		
	/**
	 * RAS
	 */
	private void prepareToStop() {
		
	}

}
