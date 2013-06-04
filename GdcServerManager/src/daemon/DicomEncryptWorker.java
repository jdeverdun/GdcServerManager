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

import dao.project.DicomImageDAO;
import dao.project.MySQLDicomImageDAO;
import es.vocali.util.AESCrypt;


/**
 * Classe realisant l'encryptage des dicom
 * @author Mobilette
 *
 */
public class DicomEncryptWorker extends DaemonWorker {

	protected Path dicomFile;
	private EncryptDaemon encryptDaemon;
	protected DicomImage dicomImage;
	protected ImagePlus imp;
	
	
	public DicomEncryptWorker(EncryptDaemon disp, Path p, DicomImage di){
		dicomFile = p;
		encryptDaemon = disp;
		dicomImage = di;
		setSerieFolder(dicomFile.getParent());
		setPatientFolder(getSerieFolder().getParent().getParent().getParent());
		setProjectFolder(getPatientFolder().getParent());
		setServerInfo(getEncryptDaemon().getServerInfo());
	}
	
	public DicomEncryptWorker(Path p, DicomImage di){
		dicomFile = p;
		dicomImage = di;
		setSerieFolder(dicomFile.getParent());
		setPatientFolder(getSerieFolder().getParent().getParent().getParent());
		setProjectFolder(getPatientFolder().getParent());
		setServerInfo(SystemSettings.SERVER_INFO);
	}
	
	@Override
	public void start() {
		System.out.println("Encrypt : "+dicomFile.getFileName());
		WindowManager.mwLogger.log(Level.FINE,"Encrypt : "+dicomFile.getFileName());
		// AES crypt
		// Encrypte le fichier en rajoutant l'extension definit dans AESCrypt.ENCRYPTSUFFIX
		try {
			AESCrypt aes = new AESCrypt(false, getAESPass());
			aes.encrypt(2, dicomFile.toString(), dicomFile+AESCrypt.ENCRYPTSUFFIX);
			// On supprime le fichier non encrypte
			FileUtils.deleteQuietly(dicomFile.toFile());
			//Files.deleteIfExists(dicomFile);
			
			
			// On ajoute l'entree du DICOM dans la database
			addEntryToDB(dicomFile.getFileName(),"DicomImage");
			
			
		} catch (Exception e) {
			// Si le cryptage ne reussi pas je supprime le fichier
			try {
				Files.deleteIfExists(dicomFile);
			} catch (IOException e1) {
				WindowManager.mwLogger.log(Level.WARNING, "Can't delete "+dicomFile,e1);
			}
			WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningencrypter().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)).substring(0, Math.min(e.toString().length(), 100)));
			WindowManager.mwLogger.log(Level.SEVERE, "Exception with encrypter",e);
		}
		
		
		prepareToStop();
	}



	public Path getDicomFile() {
		return dicomFile;
	}

	public void setDicomFile(Path dicomFile) {
		this.dicomFile = dicomFile;
	}

	public EncryptDaemon getEncryptDaemon() {
		return encryptDaemon;
	}

	public void setEncryptDaemon(EncryptDaemon encryptDaemon) {
		this.encryptDaemon = encryptDaemon;
	}

	public DicomImage getDicomImage() {
		return dicomImage;
	}

	public void setDicomImage(DicomImage dicomImage) {
		this.dicomImage = dicomImage;
	}

	@Override
	protected void addEntryToDB(Path name, String table) {
		switch(table){
			case "DicomImage":
				DicomImageDAO dicdao = new MySQLDicomImageDAO();
				try {
					dicdao.newDicomImage(name.toString(),dicomImage.getSliceLocation(), dicomImage.getProjet().getId(), dicomImage.getPatient().getId(),
							dicomImage.getAcquistionDate().getId(),dicomImage.getProtocole().getId(),dicomImage.getSerie().getId());
					dicomImage.setId(dicdao.idmax());
				} catch (SQLException e) {
					WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningencrypter().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)).substring(0, Math.min(e.toString().length(), 100)));
					WindowManager.mwLogger.log(Level.WARNING, "SQL error",e.toString());
				}
				
				break;
			default:
				WindowManager.mwLogger.log(Level.SEVERE, "Unknow table "+table+".");
		}
	}
	
	// Date de naissance
	public String getBirthdate(){
		if(imp==null){
			imp = new ImagePlus(dicomFile.toFile().getAbsolutePath());
		}
		String bdate = DicomTools.getTag(imp, "0010,0030");
		if(bdate == null){
			return null;
		}
		if(bdate.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(bdate.charAt(0) == ' ')
			bdate = bdate.substring(1);	
		// on remplace les caracteres complique par "_"
		bdate = bdate.replaceAll("[^A-Za-z0-9]" , "_");
		return bdate;
	}
	
		
	/**
	 * envoi les infos vers le daemon nifti
	 */
	private void prepareToStop() {
		this.imp = null;
		// On enleve le worker de la liste des worker et on ajoute
		// le patient à la liste des patients à convertir en nifti
		// On ne le ratjoute que si le workspace du protocole existe prefixe de serverInfo.WORKSPACE_PREFIXE
		
		/* /!\  A decommenter dans la version finale */
		if(new File(getServerInfo().getServerDir() + File.separator + ServerInfo.WORKSPACE_PREFIXE + getProjectFolder().getFileName()).exists())
			encryptDaemon.sendToNiftiDaemon(this);
	}

}
