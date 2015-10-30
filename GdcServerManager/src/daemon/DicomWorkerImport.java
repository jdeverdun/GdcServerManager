package daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

import settings.SystemSettings;
import settings.WindowManager;
import tools.DicomTools;

import daemon.tools.ThreadPool;
import daemon.tools.firstNameDB;
import daemon.tools.ThreadPool.DAEMONTYPE;
import dao.MySQLProjectDAO;
import dao.ProjectDAO;
import dao.project.AcquisitionDateDAO;
import dao.project.DicomImageDAO;
import dao.project.MySQLAcquisitionDateDAO;
import dao.project.MySQLDicomImageDAO;
import dao.project.MySQLPatientDAO;
import dao.project.MySQLProtocolDAO;
import dao.project.MySQLSerieDAO;
import dao.project.PatientDAO;
import dao.project.ProtocolDAO;
import dao.project.SerieDAO;
import es.vocali.util.AESCrypt;
import exceptions.AnonymizationException;
import exceptions.DicomException;
import exceptions.ThreadPoolException;

import model.AcquisitionDate;
import model.DICOM;
import model.DicomImage;
import model.Patient;
import model.Project;
import model.Protocol;
import model.Serie;
import model.ServerInfo;
import model.User;
import model.daemon.CustomConversionSettings.ServerMode;
import model.daemon.ImportSettings.DicomNamingTag;
import static java.nio.file.StandardCopyOption.*;

/**
 * Classe pour lire les dicoms (champs dicom) pour le mode import
 * travail par bloc : on copie en sequentiel les fichiers du bloc,
 * et on encrypt en parallele
 * @author Mobilette
 *
 */
public class DicomWorkerImport extends DicomWorker {

	private ArrayList<Path> dicomFileblock; // list contenant les fichiers a traite (on travail par bloc)
	private ArrayList<Thread> workerThread; // threads pour l'encryptage
	private int pid;
	
	public DicomWorkerImport(DicomJobDispatcher pDaemon, ArrayList<Path> paths) {
		super();
		setDispatcher(pDaemon);
		setDicomFileblock(paths);
		setServerInfo(getDispatcher().getServerInfo());
		workerThread = new ArrayList<Thread>();
		setPid(System.identityHashCode(this)); 
	}
	
	// Methodes

	public void start() throws DicomException, AnonymizationException{
		for(Path p:dicomFileblock){
			// on redefinit le fichier courant
			resetVars();
			try{
				setDicomFile(p);
			}catch(Exception e){
				WindowManager.mwLogger.log(Level.SEVERE,p.toString()+" setDicomFile error.",e);
				continue;
			}
			String studyName = null;
			String patientName = null;
			String protocolName = null;
			String serieName = null;
			String acqDate = null;
	
			// si une DicomException est genere je passe au fichier suivant
			try{
				// On recupere le nom du protocole medical
				studyName = getStudyDescription();
				if(getDispatcher().getSettings().getImportSettings().changeProjectName()){
					studyName = getDispatcher().getSettings().getImportSettings().getNewProjectName();
				}
				// Si le protocole est null alors le fichier est encore en cours de copie
				if(studyName == null){
					prepareToStop();
					throw new DicomException("Study Name is empty.");
				}	

				birthdate = getBirthdate();
				
				sopinstanceid = getSOPInstanceUID();
				sex = getSex();
				size = getPatientSize();
				weight = getPatientWeight();
				mri_name = getMri_name();
				repetitiontime = getRepetitionTime();
				echotime = getEchoTime();
				slicethickness = getSliceThickness();
				String[] pspacing = getPixelSpacing();
				voxelwidth = Float.parseFloat(pspacing[0]);
				voxelheight = Float.parseFloat(pspacing[1]);
				
				protocolName = getProtocolName();
				serieName = getSeriesDescription();
				try {
					acqDate = getAcquisitionDate();
				} catch (DicomException e) {
					acqDate = getSerieDate();
					WindowManager.mwLogger.log(Level.WARNING, "DicomWorkerImport dicomException,acqDate set to serieDate",e);
				}

				// -------------- Modif des noms etc ---------------
				if(getDispatcher().getSettings().getImportSettings().getNamingTag()==DicomNamingTag.PATIENTID)
					patientName = getPatientId();
				else
					patientName = getPatientName();
				if(getDispatcher().getSettings().getImportSettings().changePatientName()){
					if(getDispatcher().getSettings().getImportSettings().getXlsName() != null)
						patientName = getDispatcher().getSettings().getImportSettings().getNameToNewName().get(patientName);
					else
						patientName = getDispatcher().getSettings().getImportSettings().getNewPatientName();
					//String lpt = getDicomFile().getFileName().toString();
					String filename = sopinstanceid;//lpt;
					/*if(lpt.split(".").length>1){
						String[] parts = lpt.split("\\.");
						filename = parts[0];
					}*/
					Path newDicom = Paths.get(SystemSettings.SERVER_INFO.getTempDir()+File.separator+filename+"_a");
					DicomTools.changementTag(getDicomFile().toFile(), newDicom.toFile(), Tag.PatientName, VR.PN, patientName);
					setDicomFile(newDicom);
				}else{
					// si donnees non anonymises on anonymise
					if(getDispatcher().getSettings().getImportSettings().getNamingTag()==DicomNamingTag.ANONYMIZE || firstNameDB.matches(patientName)){
						patientName = anonymizeCurrentFile(patientName);					
					}
				}
				// ----------------- Fin modif des noms ------------
				
				
			}catch(DicomException di){
				WindowManager.mwLogger.log(Level.WARNING,"DicomException with "+p+" ... skipped.",di);
				continue;
			} catch (IOException e) {
				throw new AnonymizationException("Dicom Tag update [changementTag] failed with "+p);
			}
			
			
			
			// si protocol est vide ou serie  est vide, on met le nom du protocol et vice versa !
			if(protocolName == DEFAULT_STRING && serieName != DEFAULT_STRING){
				protocolName = serieName;
			}else{
				if(serieName == DEFAULT_STRING && protocolName != DEFAULT_STRING)
					serieName = protocolName;
			}
				
			
			// On créé les chemins vers les répertoires
			Path studyFolder = Paths.get(serverInfo.getServerDir()+File.separator+serverInfo.NRI_DICOM_NAME + File.separator + studyName);
			setProjectFolder(studyFolder);
			patientFolder = Paths.get(studyFolder + File.separator + patientName);
			Path dateFolder = Paths.get(patientFolder + File.separator + acqDate);
			Path protocolFolder = Paths.get(dateFolder + File.separator + protocolName);
			serieFolder = Paths.get(protocolFolder + File.separator + serieName);
			// on ne copie pas dans l'import, one fera la copie dans l'encryptage
			// on note quand meme l'arborescence "theorique"
			newPath = Paths.get(serieFolder + File.separator + dicomFile.getFileName());
			
			// si le fichier encrypte existe deja je sors
			if(new File(newPath.toString()+AESCrypt.ENCRYPTSUFFIX).exists()){
				if(getDispatcher().getSettings().getImportSettings().getNamingTag()==DicomNamingTag.ANONYMIZE || getDispatcher().getSettings().getImportSettings().changePatientName())
					try {
						Files.delete(getDicomFile());
					} catch (IOException e) {
						WindowManager.mwLogger.log(Level.WARNING,"Couldn't delete anonymized/renamed dicom "+getDicomFile().toString(),e);
					}
				continue;
			}
			// On test si les repertoires existent (patient / protocoles etc) et on les créé au besoin
			// si on les cree alors on doit rajouter l'info dans la database
			// sinon recuperer les ID des projets etc
			boolean dirExists = checkAndMakeDir(studyFolder);
			if(!dirExists)
				addEntryToDB(studyFolder.getFileName(),"Project");
			else
				setProject_idFromDB(studyFolder.getFileName());
			dirExists = checkAndMakeDir(patientFolder);
			if(!dirExists)
				addEntryToDB(patientFolder.getFileName(),"Patient");
			else
				setPatient_idFromDB(patientFolder.getFileName());
			dirExists = checkAndMakeDir(dateFolder);
			if(!dirExists)
				addEntryToDB(dateFolder.getFileName(),"AcqDate");
			else
				setAcqDate_idFromDB(dateFolder.getFileName());
			dirExists = checkAndMakeDir(protocolFolder);
			if(!dirExists)
				addEntryToDB(protocolFolder.getFileName(),"Protocol");
			else
				setProtocol_idFromDB(protocolFolder.getFileName());
			dirExists = checkAndMakeDir(serieFolder);
			if(!dirExists)
				addEntryToDB(serieFolder.getFileName(),"Serie");
			else
				setSerie_idFromDB(serieFolder.getFileName());
			
			
			// On construit l'objet dicom
			dicomImage = new DicomImage();
			dicomImage.setName(dicomFile.getFileName().toString());
			dicomImage.setSliceLocation(getSliceLocation());
			dicomImage.setProjet(new Project(getProject_id()));
			dicomImage.setPatient(new Patient(getPatient_id()));
			dicomImage.setProtocole(new Protocol(getProtocol_id()));
			dicomImage.setAcquistionDate(new AcquisitionDate(getAcqDate_id()));
			dicomImage.setSerie(new Serie(getSerie_id()));
			// donnees pour le thread
			final Path tpath = newPath;
			final DicomImage di = dicomImage;
			final Path df = dicomFile;
			// on lance l'encryptage du fichier sur un thread
			Thread tr = new Thread(new Runnable() {
				
				@Override
				public void run() {
					DicomEncryptWorkerImport dEncryptWorker = new DicomEncryptWorkerImport(getDispatcher().getSettings(), df, tpath, di);
					dEncryptWorker.start();  
					if(dEncryptWorker.isCrashed()){
						getDispatcher().setCrashed(true);
						WindowManager.mwLogger.log(Level.SEVERE,"Critical error : Encrypt Worker import has crashed ... ");
					}
				}
			});
			try{
				while(!ThreadPool.addThread(tr,getPid(),DAEMONTYPE.DicomEncryptDaemon)){
					try{
						Thread.sleep(50);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				tr.start();
				workerThread.add(tr);
			}catch(ThreadPoolException te){
				WindowManager.mwLogger.log(Level.SEVERE,"Critical error in DicomWorkerImport (addThread).",te);
				SystemSettings.stopDaemons();
			}
		}
		// on attend que l'encryptage soit ok
		boolean cont = true;
		while(cont){
			cont = false;
			for(Thread t:workerThread){
				if(t.isAlive())
					cont = true;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// On termine
		prepareToStop();
	}




	/**
	 * @return the dicomFileblock
	 */
	public ArrayList<Path> getDicomFileblock() {
		return dicomFileblock;
	}


	/**
	 * @param dicomFileblock the dicomFileblock to set
	 */
	public void setDicomFileblock(ArrayList<Path> dicomFileblock) {
		this.dicomFileblock = dicomFileblock;
	}

	/**
	 * @return the pid
	 */
	public int getPid() {
		return pid;
	}

	/**
	 * @param pid the pid to set
	 */
	public void setPid(int pid) {
		this.pid = pid;
	}

	private String anonymizeCurrentFile(String patientName) throws AnonymizationException, DicomException{
		String lpt = sopinstanceid;//getDicomFile().getFileName().toString();
		String filename = lpt;
		/*if(lpt.split(".").length>1){
			String[] parts = lpt.split("\\.");
			filename = parts[0];
		}*/
		Path newDicom = Paths.get(SystemSettings.SERVER_INFO.getTempDir()+File.separator+filename+"_a");
		String npatname;
		try {
			npatname = DicomTools.anonymize(getDicomFile(),newDicom,patientName,birthdate);
			setDicomFile(newDicom);
		} catch (IOException e) {
			throw new AnonymizationException("DICOM could not be anonymized.");
		}
		getDispatcher().getSettings().getImportSettings().addAnomynizationLine(patientName,npatname);
		return npatname;
	}
}
