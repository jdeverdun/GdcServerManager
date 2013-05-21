package daemon;

import ij.ImagePlus;
import ij.util.DicomTools;

import java.io.File;
import java.io.FileNotFoundException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
			studyName = "null";
			System.out.println(e.toString());
		}

		// on definit le nom "null" au repertoire si les champs facultatifs  sont null
		// si c'est des champs importants on transfere  l'erreur
		patientName = getPatientName();
		
		try {
			birthdate = getBirthdate();
		} catch (DicomException e) {
			birthdate = "null";
			System.out.println(e.toString());
		}
		try {
			sex = getSex();
		} catch (DicomException e) {
			sex = "null";
			System.out.println(e.toString());
		}

		try {
			protocolName = getProtocolName();
		} catch (DicomException e) {
			protocolName = "null";
			System.out.println(e.toString());
		}

		serieName = getSeriesDescription();
		
		try {
			acqDate = getAcquisitionDate();
		} catch (DicomException e) {
			acqDate = "null";
			System.out.println(e.toString());
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
		dicomImage.setMri_name(getMri_name());
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
