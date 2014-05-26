package daemon;

import ij.ImagePlus;
import ij.util.DicomTools;

import java.io.File;
import java.io.FileNotFoundException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import settings.WindowManager;

import exceptions.DicomException;

import model.AcquisitionDate;
import model.DicomImage;
import model.Patient;
import model.Project;
import model.Protocol;
import model.Serie;
import model.daemon.CustomConversionSettings;



public class DicomWorkerClient extends DicomWorker {
	public static Path DICOMDIR = null; // Permet de conserver la trace, pour un run du dispatcher du repertoire des dicom 
	
	public DicomWorkerClient(DicomJobDispatcher pDaemon, Path filename) throws FileNotFoundException, DicomException {
		super(pDaemon,filename);
	}
	
	
	
	// Methodes

	public void start() throws DicomException{
		String studyName = null;
		String patientName = null;
		String protocolName = null;
		String serieName = null;
		String acqDate = null;
		CustomConversionSettings djdsettings = getDispatcher().getSettings();
		// On recupere le nom du protocole medical
		try {
			studyName = getStudyDescription();
		} catch (DicomException e) {
			studyName = DEFAULT_STRING;
			WindowManager.mwLogger.log(Level.WARNING, "DicomWorkerClient dicomException,studyName set to null",e);
		}

		// on definit le nom "null" au repertoire si les champs facultatifs  sont null
		// si c'est des champs importants on transfere  l'erreur
		patientName = getPatientName();
		
		try {
			birthdate = getBirthdate();
		} catch (DicomException e) {
			birthdate = DEFAULT_STRING;
			WindowManager.mwLogger.log(Level.WARNING, "DicomWorkerClient dicomException,birthdate set to null",e);
		}
		try {
			sex = getSex();
		} catch (DicomException e) {
			sex = "null";
			WindowManager.mwLogger.log(Level.WARNING, "DicomWorkerClient dicomException,sex set to null",e);
		}

		try {
			protocolName = getProtocolName();
		} catch (DicomException e) {
			protocolName = DEFAULT_STRING;
			WindowManager.mwLogger.log(Level.WARNING, "DicomWorkerClient dicomException,protocolName set to null",e);
		}

		serieName = getSeriesDescription();
		// si protocol est vide ou serie  est vide, on met le nom du protocol et vice versa !
		if(protocolName == DEFAULT_STRING && serieName != DEFAULT_STRING){
			protocolName = serieName;
		}else{
			if(serieName == DEFAULT_STRING && protocolName != DEFAULT_STRING)
				serieName = protocolName;
		}
		try {
			acqDate = getAcquisitionDate();
		} catch (DicomException e) {
			try{
				acqDate = getSerieDate();
			}catch(DicomException de){// on prend la date de la serie si on a pas la date d'acquisition
				acqDate = DEFAULT_STRING;
				WindowManager.mwLogger.log(Level.WARNING, "DicomWorkerClient dicomException,acqDate set to null",e);
			}
			
		}		
		
		// On créé les chemins vers les répertoires
		
		Path studyFolder;
		Path dateFolder;
		Path protocolFolder;

		Path dicomDir;
		if(djdsettings.keepDicom()){
			dicomDir = serverInfo.getDicomDir();
		}else{
			if(DICOMDIR==null){
				DICOMDIR = Paths.get(serverInfo.getTempDir() + File.separator + "export" + System.currentTimeMillis());
				checkAndMakeDir(DICOMDIR);
			}
			dicomDir = DICOMDIR;
		}
		if(djdsettings.isWorkingWithProjectDir()) // on verifie a chaque fois si on souhaite creer se repetoire
			studyFolder = Paths.get(dicomDir + File.separator + studyName);
		else
			studyFolder = dicomDir;
		setProjectFolder(studyFolder);
		patientFolder = Paths.get(studyFolder + File.separator + patientName);
		
		if(djdsettings.isWorkingWithAcqDateDir())
			dateFolder = Paths.get(patientFolder + File.separator + acqDate);
		else
			dateFolder = patientFolder;
		
		if(djdsettings.isWorkingWithProtocolDir())
			protocolFolder = Paths.get(dateFolder + File.separator + protocolName);
		else
			protocolFolder = dateFolder;
		
		serieFolder = Paths.get(protocolFolder + File.separator + serieName);
		
		// On test si les repertoires existent (patient / protocoles etc) et on les créé au besoin
		// si on les cree alors on doit rajouter l'info dans la database
		// sinon recuperer les ID des projets etc
		boolean dirExists = checkAndMakeDir(studyFolder);
		dirExists = checkAndMakeDir(patientFolder);
		dirExists = checkAndMakeDir(dateFolder);
		dirExists = checkAndMakeDir(protocolFolder);
		dirExists = checkAndMakeDir(serieFolder);
		
		Path newPath = Paths.get(serieFolder + File.separator + dicomFile.getFileName());
		
		// On deplace
		copyDicomTo(newPath);
		
		// On construit l'objet dicom
		dicomImage = new DicomImage();
		dicomImage.setName(dicomFile.getFileName().toString());
		dicomImage.setSliceLocation(getSliceLocation());
		dicomImage.setProjet(new Project(getProject_id()));
		dicomImage.setPatient(new Patient(getPatient_id()));
		dicomImage.setProtocole(new Protocol(getProtocol_id()));
		dicomImage.setAcquistionDate(new AcquisitionDate(getAcqDate_id()));
		dicomImage.setSerie(new Serie(getSerie_id()));
		
		if(getDispatcher().getNiftiDaemon()!=null){
			getDispatcher().getNiftiDaemon().addDir(newPath.getParent(), dicomImage);
		}
		
		// On termine
		prepareToStop();
	}


	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof DicomWorkerClient))return false;
	    DicomWorkerClient otherDicomWorker = (DicomWorkerClient)other;
	    if(otherDicomWorker.getDicomFile().equals(getDicomFile()))
	    	return true;
	    else
	    	return false;
	}
	
	


	public void prepareToStop(){
		// On libere de la memoire
		header = null;
	}
}
