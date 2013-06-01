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

import settings.SystemSettings;
import settings.WindowManager;

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
import exceptions.DicomException;

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
import static java.nio.file.StandardCopyOption.*;

/**
 * Classe pour lire les dicoms (champs dicom) pour le mode import
 * @author Mobilette
 *
 */
public class DicomWorkerImport extends DicomWorker {


	public DicomWorkerImport(DicomJobDispatcher pDaemon, Path filename) throws FileNotFoundException, DicomException {
		super(pDaemon,filename);
	}

	
	// Accesseurs
	
	
	
	
	// Methodes

	public void start() throws DicomException{
		String studyName = null;
		String patientName = null;
		String protocolName = null;
		String serieName = null;
		String acqDate = null;

		// On recupere le nom du protocole medical
		studyName = getStudyDescription();
		if(getDispatcher().getSettings().getImportSettings().changeProjectName()){
			studyName = getDispatcher().getSettings().getImportSettings().getNewProjectName();
		}
		// Si le protocole est null alors le fichier est encore en cours de copie
		if(studyName == null){
			prepareToStop();
			return;
		}	
		if(!getDispatcher().getSettings().getImportSettings().isUsePatientName())
			patientName = getPatientId();
		else
			patientName = getPatientName();
		birthdate = getBirthdate();
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
		acqDate = getAcquisitionDate();	
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
		
		// on ne copie pas dans l'import, one fera la copie dans l'encryptage
		// on note quand meme l'arborescence "theorique"
		newPath = Paths.get(serieFolder + File.separator + dicomFile.getFileName());
		
		// On construit l'objet dicom
		dicomImage = new DicomImage();
		dicomImage.setName(dicomFile.getFileName().toString());
		dicomImage.setSliceLocation(getSliceLocation());
		dicomImage.setProjet(new Project(getProject_id()));
		dicomImage.setPatient(new Patient(getPatient_id()));
		dicomImage.setProtocole(new Protocol(getProtocol_id()));
		dicomImage.setAcquistionDate(new AcquisitionDate(getAcqDate_id()));
		dicomImage.setSerie(new Serie(getSerie_id()));
		
		// on lance l'encryptage du fichier
		DicomEncryptWorkerImport dEncryptWorker = new DicomEncryptWorkerImport(getDispatcher().getSettings(), dicomFile, newPath, dicomImage);
		dEncryptWorker.start();  
		if(dEncryptWorker.isCrashed()){
			getDispatcher().setCrashed(true);
			WindowManager.mwLogger.log(Level.SEVERE,"Critical error : Encrypt Worker import has crashed ... ");
		}

		
		// On termine
		prepareToStop();
	}

}
