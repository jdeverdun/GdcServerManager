package daemon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

import model.DicomImage;

import dao.project.DicomImageDAO;
import dao.project.MySQLDicomImageDAO;
import es.vocali.util.AESCrypt;


/**
 * Classe realisant l'encryptage des dicom
 * @author Mobilette
 *
 */
public class DicomEncryptWorker extends DaemonWorker {

	private Path dicomFile;
	private EncryptDaemon encryptDaemon;
	private DicomImage dicomImage;
	
	public DicomEncryptWorker(EncryptDaemon disp, Path p, DicomImage di){
		dicomFile = p;
		encryptDaemon = disp;
		dicomImage = di;
		setSerieFolder(dicomFile.getParent());
		setPatientFolder(getSerieFolder().getParent().getParent().getParent());
		setProjectFolder(getPatientFolder().getParent());
		setServerInfo(getEncryptDaemon().getServerInfo());
	}
	
	@Override
	public void start() {
		System.out.println("Encrypt : "+dicomFile.getFileName());
		// AES crypt
		// Encrypte le fichier en rajoutant l'extension definit dans AESCrypt.ENCRYPTSUFFIX
		try {
			AESCrypt aes = new AESCrypt(false, getAESPass());
			aes.encrypt(2, dicomFile.toString(), dicomFile+AESCrypt.ENCRYPTSUFFIX);
			// On supprime le fichier non encrypte
			Files.deleteIfExists(dicomFile);
			
			
			// On ajoute l'entree du DICOM dans la database
			addEntryToDB(dicomFile.getFileName(),"DicomImage");
			
			
		} catch (GeneralSecurityException | IOException e) {
			// Si le cryptage ne reussi pas je deplace vers un repertoire specifique
			e.printStackTrace();
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
					dicdao.newDicomImage(name.toString(), dicomImage.getProjet().getId(), dicomImage.getPatient().getId(),
							dicomImage.getAcquistionDate().getId(),dicomImage.getProtocole().getId(),dicomImage.getSerie().getId());
					dicomImage.setId(dicdao.idmax());
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				break;
			default:
				System.err.println("Unknow table "+table+".");
		}
	}
	
	
	/**
	 * envoi les infos vers le daemon nifti
	 */
	private void prepareToStop() {
		// On enleve le worker de la liste des worker et on ajoute
		// le patient à la liste des patients à convertir en nifti
		encryptDaemon.sendToNiftiDaemon(this);
	}

}
