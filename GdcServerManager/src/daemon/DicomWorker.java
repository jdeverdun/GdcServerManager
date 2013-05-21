package daemon;

import ij.ImagePlus;
import ij.util.DicomTools;

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
import static java.nio.file.StandardCopyOption.*;


public class DicomWorker extends DaemonWorker {

	// Attributs
	protected Path dicomFile;
	protected DicomJobDispatcher dispatcher;
	protected DicomImage dicomImage;
	protected String header;
	protected String birthdate;
	protected String sex;
	protected int project_id;
	protected int patient_id;
	protected int acqDate_id;
	protected int protocol_id;
	protected int serie_id;
	
	public DicomWorker(DicomJobDispatcher pDaemon, Path filename) throws FileNotFoundException, DicomException{
		setDispatcher(pDaemon);
		setDicomFile(filename);
		setServerInfo(getDispatcher().getServerInfo());
	}

	
	// Accesseurs
	
	public Path getDicomFile() {
		return dicomFile;
	}

	public void setDicomFile(Path dicomFile) throws FileNotFoundException, DicomException {
		this.dicomFile = dicomFile;
		FileInputStream fis = new FileInputStream(dicomFile.toString());
		header = new DICOM(fis).getInfo(dicomFile);
		if(header == null)
			throw new DicomException("Empty DICOM header");
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public DicomJobDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(DicomJobDispatcher parentDaemon) {
		this.dispatcher = parentDaemon;
	}
	
	
	
	// Methodes

	public void start() throws DicomException{
		String studyName = null;
		String patientName = null;
		String protocolName = null;
		String serieName = null;
		String acqDate = null;
		
		// On recupere le nom du protocole medical
		studyName = getStudyDescription();
		// Si le protocole est null alors le fichier est encore en cours de copie
		if(studyName == null){
			prepareToStop();
			return;
		}	
		patientName = getPatientName();
		birthdate = getBirthdate();
		sex = getSex();
		protocolName = getProtocolName();
		serieName = getSeriesDescription();
		acqDate = getAcquisitionDate();		
		
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
		
		Path newPath = Paths.get(serieFolder + File.separator + dicomFile.getFileName());
		
		// On deplace
		moveDicomTo(newPath);
		
		// On construit l'objet dicom
		dicomImage = new DicomImage();
		dicomImage.setName(dicomFile.getFileName().toString());
		dicomImage.setMri_name(getMri_name());
		dicomImage.setProjet(new Project(getProject_id()));
		dicomImage.setPatient(new Patient(getPatient_id()));
		dicomImage.setProtocole(new Protocol(getProtocol_id()));
		dicomImage.setAcquistionDate(new AcquisitionDate(getAcqDate_id()));
		dicomImage.setSerie(new Serie(getSerie_id()));
		
		// On ajoute le fichier brute dans la liste des fichiers
		// a encrypter 
		if(SystemSettings.ENCRYPT_DAEMON!=null && SystemSettings.ENCRYPT_DAEMON.isAlive()){
			SystemSettings.ENCRYPT_DAEMON.addDicomToEncrypt(newPath, dicomImage);
		}else{
			WindowManager.MAINWINDOW.getSstatusPanel().getLblCommentdicomdispatcher().setText("Critical error : Encrypt Daemon offline, can't forward ... Please restart");
			WindowManager.MAINWINDOW.getSstatusPanel().setCritical(WindowManager.MAINWINDOW.getSstatusPanel().getBtnDicomdispatcherstatus());
			while(!(SystemSettings.ENCRYPT_DAEMON!=null && SystemSettings.ENCRYPT_DAEMON.isAlive())){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		// On termine
		prepareToStop();
	}

	// Set des ID serie // protocol // projet etc depuis la BDD
	private void setSerie_idFromDB(Path fileName) {
		// on verifie d'abord si on a pas les infos dans le cache
		DBCache cache = getServerInfo().getDbCache();
		Integer id = cache.getIdSerieList().get(fileName.toString() + "@@" + getProtocol_id() + "@@" + getAcqDate_id() + "@@" +
				"" + getPatient_id() + "@@" + getProject_id());
		if(id!=null){
			setSerie_id(id);
			return;
		}
		
		// Si ce n'est pas le cas on fait la requete SQL 
		// et on met à jours le cache
		
		SerieDAO sdao = new MySQLSerieDAO();
		try {
			Serie s = sdao.retrieveSerie(fileName.toString(),getProject_id(),getPatient_id(),getAcqDate_id(),getProtocol_id());
			setSerie_id(s.getId());
			cache.getIdSerieList().put(fileName.toString() + "@@" + getProtocol_id() + "@@" + getAcqDate_id() + "@@" +
					"" + getPatient_id() + "@@" + getProject_id(), s.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Definition de l'attribut Protocol_id grace a son nom (repertoire) depuis la BDD
	 * @param fileName
	 */
	private void setProtocol_idFromDB(Path fileName) {
		// on verifie d'abord si on a pas les infos dans le cache
		DBCache cache = getServerInfo().getDbCache();
		Integer id = cache.getIdProtocolList().get(fileName.toString() + "@@" + getAcqDate_id() + "@@" +
				"" + getPatient_id() + "@@" + getProject_id());
		if(id!=null){
			setProtocol_id(id);
			return;
		}
			
		// Si ce n'est pas le cas on fait la requete SQL 
		// et on met à jours le cache
		
		ProtocolDAO pdao = new MySQLProtocolDAO();
		try {
			Protocol p = pdao.retrieveProtocol(fileName.toString(),getProject_id(),getPatient_id(),getAcqDate_id());
			setProtocol_id(p.getId());
			cache.getIdProtocolList().put(fileName.toString() + "@@" + getAcqDate_id() + "@@" +
					"" + getPatient_id() + "@@" + getProject_id(), p.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Definition de l'attribut Acqdate_id grace a son nom (repertoire) depuis la BDD
	 * @param fileName
	 */
	private void setAcqDate_idFromDB(Path fileName) {
		// on verifie d'abord si on a pas les infos dans le cache
		DBCache cache = getServerInfo().getDbCache();
		Integer id = cache.getIdAcqDateList().get(fileName.toString() + "@@" +
				"" + getPatient_id() + "@@" + getProject_id());
		if(id!=null){
			setAcqDate_id(id);
			return;
		}
			
		// Si ce n'est pas le cas on fait la requete SQL 
		// et on met à jours le cache
		
		AcquisitionDateDAO adao = new MySQLAcquisitionDateDAO();
		try {
			AcquisitionDate a = adao.retrieveAcqDate(fileName.toString(),getProject_id(),getPatient_id());
			setAcqDate_id(a.getId());
			cache.getIdAcqDateList().put(fileName.toString() + "@@" +
					"" + getPatient_id() + "@@" + getProject_id(), a.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Definition de l'attribut Patient_id grace a son nom (repertoire) depuis la BDD
	 * @param fileName
	 */
	private void setPatient_idFromDB(Path fileName) {
		// on verifie d'abord si on a pas les infos dans le cache
		DBCache cache = getServerInfo().getDbCache();
		Integer id = cache.getIdPatientList().get(fileName.toString() + "@@" + getProject_id());
		if(id!=null){
			setPatient_id(id);
			return;
		}
			
		// Si ce n'est pas le cas on fait la requete SQL 
		// et on met à jours le cache
		
		PatientDAO pdao = new MySQLPatientDAO();
		try {
			Patient p = pdao.retrievePatient(fileName.toString(),this.birthdate,this.sex, getProject_id());
			setPatient_id(p.getId());
			cache.getIdPatientList().put(fileName.toString() + "@@" + getProject_id(), p.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Definition de l'attribut Project_id grace a son nom (repertoire) depuis la BDD
	 * @param fileName
	 */
	private void setProject_idFromDB(Path fileName) {
		// on verifie d'abord si on a pas les infos dans le cache
		DBCache cache = getServerInfo().getDbCache();
		Integer id = cache.getIdProjectList().get(fileName.toString());
		if(id!=null){
			setProject_id(id);
			return;
		}
			
		// Si ce n'est pas le cas on fait la requete SQL 
		// et on met à jours le cache
		
		ProjectDAO pdao = new MySQLProjectDAO();
		try {
			Project p = pdao.retrieveProject(fileName.toString());
			setProject_id(p.getId());
			cache.getIdProjectList().put(fileName.toString(),p.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	
	
	// Rajoute une entree d'un dossier / image
	// dans la table "table" de la base de donnee 
	protected void addEntryToDB(Path name, String table) {
		switch(table){
		case "Project":
			ProjectDAO pdao = new MySQLProjectDAO();
			try {
				pdao.newProject(name.toString());
				setProject_id(pdao.idmax());
			} catch (SQLException e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
				System.out.println("SQL Error : "+e.toString());
			}
			break;
		case "Patient":
			PatientDAO patdao = new MySQLPatientDAO();
			try {
				patdao.newPatient(name.toString(),this.birthdate,this.sex, getProject_id());
				setPatient_id(patdao.idmax());
			} catch (SQLException e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
				System.out.println("SQL Error : "+e.toString());
			}
			break;
		case "AcqDate":
			AcquisitionDateDAO acqdao = new MySQLAcquisitionDateDAO();
			try {
				acqdao.newAcqDate(name.toString(), getProject_id(), getPatient_id());
				setAcqDate_id(acqdao.idmax());
			} catch (SQLException e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
				System.out.println("SQL Error : "+e.toString());
			}
			break;
		case "Protocol":
			ProtocolDAO protdao = new MySQLProtocolDAO();
			try {
				protdao.newProtocol(name.toString(), getProject_id(), getPatient_id(),getAcqDate_id());
				setProtocol_id(protdao.idmax());
			} catch (SQLException e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
				System.out.println("SQL Error : "+e.toString());
			}
			break;
		case "Serie":
			SerieDAO sdao = new MySQLSerieDAO();
			try {
				sdao.newSerie(name.toString(), 0, getProject_id(), getPatient_id(),getAcqDate_id(),getProtocol_id());
				setSerie_id(sdao.idmax());
			} catch (SQLException e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
				System.out.println("SQL Error : "+e.toString());
			}
			break;
		default:
			System.err.println("Unknow table : "+table);
		}
		
		
	}


	// Deplace dicomFile à l'emplacement donné et update la date de modification
	// du repertoire patient pour la conversion nifti
	protected void moveDicomTo(Path newPath) {
		try {
			if(getDispatcher().getSettings().isDicomDebugMode())
				System.out.println("Moving : " + dicomFile.getFileName() + " to " + newPath);
			Files.move(dicomFile, newPath, REPLACE_EXISTING);
			// update date de modification du repertoire du patient
			long currentTimeMillis = System.currentTimeMillis();
	        FileTime fileTime = FileTime.fromMillis(currentTimeMillis);
	        Files.setLastModifiedTime(getPatientFolder(), fileTime);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void copyDicomTo(Path newPath) {
		try {
			if(getDispatcher().getSettings().isDicomDebugMode())
				System.out.println("Copying : " + dicomFile.getFileName() + " to " + newPath);
			Files.copy(dicomFile, newPath, REPLACE_EXISTING);
			// update date de modification du repertoire du patient
			long currentTimeMillis = System.currentTimeMillis();
	        FileTime fileTime = FileTime.fromMillis(currentTimeMillis);
	        Files.setLastModifiedTime(getPatientFolder(), fileTime);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof DicomWorker))return false;
	    DicomWorker otherDicomWorker = (DicomWorker)other;
	    if(otherDicomWorker.getDicomFile().equals(getDicomFile()))
	    	return true;
	    else
	    	return false;
	}
	
	
	// Renvoi le nom du protocole medical
	public String getStudyDescription() throws DicomException{
		String prot = getTag("0008,1030");
		if(prot == null){
			throw new DicomException("Unable to decode DICOM header 0008,1030");
		}
		if(prot.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(prot.charAt(0) == ' ')
			prot = prot.substring(1);	
		// on enleve les espaces en fin de chaine
		while(prot.length()>1 && prot.charAt(prot.length()-1) == ' ')
			prot = prot.substring(0,prot.length()-1);	
		// on remplace les caracteres complique par "_"
		prot = prot.replaceAll("[^A-Za-z0-9]" , "_");
		return prot;
	}
	
	// Nom du patient
	public String getPatientName() throws DicomException{
		String pname = getTag("0010,0010");
		if(pname == null){
			throw new DicomException("Unable to decode DICOM header 0010,0010");
		}
		if(pname.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(pname.charAt(0) == ' ')
			pname = pname.substring(1);	
		// on enleve les espaces en fin de chaine
		while(pname.length()>1 && pname.charAt(pname.length()-1) == ' ')
			pname = pname.substring(0,pname.length()-1);	
		// on remplace les caracteres complique par "_"
		pname = pname.replaceAll("[^A-Za-z0-9]" , "_");
		return pname;
	}
	
	
	// Nom de la sequence (ex:  Series Description: PHA_IMAGES)
	public String getSeriesDescription() throws DicomException{
		String sdesc = getTag("0008,103E");
		if(sdesc == null){
			throw new DicomException("Unable to decode DICOM header 0008,103E");
		}
		if(sdesc.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(sdesc.charAt(0) == ' ')
			sdesc = sdesc.substring(1);	
		// on enleve les espaces en fin de chaine
		while(sdesc.length()>1 && sdesc.charAt(sdesc.length()-1) == ' ')
			sdesc = sdesc.substring(0,sdesc.length()-1);
		// on remplace les caracteres complique par "_"
		sdesc = sdesc.replaceAll("[^A-Za-z0-9]" , "_");
		return sdesc;
	}
	
	// Date de naissance
	public String getBirthdate() throws DicomException{
		String bdate = getTag("0010,0030");
		if(bdate == null){
			throw new DicomException("Unable to decode DICOM header 0010,0030");
		}
		if(bdate.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(bdate.charAt(0) == ' ')
			bdate = bdate.substring(1);	
		// on enleve les espaces en fin de chaine
		while(bdate.length()>1 && bdate.charAt(bdate.length()-1) == ' ')
			bdate = bdate.substring(0,bdate.length()-1);
		// on remplace les caracteres complique par "_"
		bdate = bdate.replaceAll("[^A-Za-z0-9]" , "_");
		return bdate;
	}
	// Sexe du patient
	public String getSex() throws DicomException{
		String psex = getTag("0010,0040");
		if(psex == null){
			throw new DicomException("Unable to decode DICOM header 0010,0040");
		}
		if(psex.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(psex.charAt(0) == ' ')
			psex = psex.substring(1);	
		// on enleve les espaces en fin de chaine
		while(psex.length()>1 && psex.charAt(psex.length()-1) == ' ')
			psex = psex.substring(0,psex.length()-1);
		// on remplace les caracteres complique par "_"
		psex = psex.replaceAll("[^A-Za-z0-9]" , "_");
		return psex;
	}	
	
	// Nom de l'IRM
	public String getMri_name() throws DicomException{
		String iname = getTag("0008,1090");
		if(iname == null){
			throw new DicomException("Unable to decode DICOM header 0008,1090");
		}
		if(iname.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(iname.charAt(0) == ' ')
			iname = iname.substring(1);	
		// on enleve les espaces en fin de chaine
		while(iname.length()>1 && iname.charAt(iname.length()-1) == ' ')
			iname = iname.substring(0,iname.length()-1);
		// on remplace les caracteres complique par "_"
		iname = iname.replaceAll("[^A-Za-z0-9]" , "_");
		return iname;
	}
		
	// Nom du protocole d'acquisition (ex:  SWI3D TRA 1.5mm JEREMY)
	public String getProtocolName() throws DicomException{
		String pprot = getTag("0018,1030");
		System.out.println(pprot);
		if(pprot == null){
			throw new DicomException("Unable to decode DICOM header 0018,1030");
		}
		if(pprot.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(pprot.charAt(0) == ' ')
			pprot = pprot.substring(1);	
		// on enleve les espaces en fin de chaine
		while(pprot.length()>1 && pprot.charAt(pprot.length()-1) == ' ')
			pprot = pprot.substring(0,pprot.length()-1);
		// on remplace les caracteres complique par "_"
		pprot = pprot.replaceAll("[^A-Za-z0-9]" , "_");
		return pprot;
	}
	// Date de l'acquisition ex : 20130122
	public String getAcquisitionDate() throws DicomException{
		String pdate = getTag("0008,0022");
		if(pdate == null){
			throw new DicomException("Unable to decode DICOM header 0008,0022");
		}
		if(pdate.isEmpty())
			return "Unknown";
		while(pdate.charAt(0) == ' ')
			pdate = pdate.substring(1);	
		// on enleve les espaces en fin de chaine
		while(pdate.length()>1 && pdate.charAt(pdate.length()-1) == ' ')
			pdate = pdate.substring(0,pdate.length()-1);
		// on remplace les caracteres complique par "_"
		pdate = pdate.replaceAll("[^A-Za-z0-9]" , "_");
		return pdate;
	}
	public int getProject_id() {
		return project_id;
	}


	public void setProject_id(int project_id) {
		this.project_id = project_id;
	}


	public int getPatient_id() {
		return patient_id;
	}


	public void setPatient_id(int patient_id) {
		this.patient_id = patient_id;
	}


	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}


	public int getAcqDate_id() {
		return acqDate_id;
	}


	public void setAcqDate_id(int acqDate_id) {
		this.acqDate_id = acqDate_id;
	}


	public int getProtocol_id() {
		return protocol_id;
	}


	public void setProtocol_id(int protocol_id) {
		this.protocol_id = protocol_id;
	}


	public int getSerie_id() {
		return serie_id;
	}


	public void setSerie_id(int serie_id) {
		this.serie_id = serie_id;
	}


	public DicomImage getDicomImage() {
		return dicomImage;
	}


	public void setDicomImage(DicomImage dicomImage) {
		this.dicomImage = dicomImage;
	}


	public void prepareToStop(){
		// On libere de la memoire
		header = null;
	}
	
	/**
	 * Permet de recupere un champ dicom
	 * @param tag
	 * @return
	 */
	private String getTag(String tag) {
		if (header==null) return null;
		int index1 = header.indexOf(tag);
		if (index1==-1) return null;
		//IJ.log(hdr.charAt(index1+11)+"   "+hdr.substring(index1,index1+20));
		if (header.charAt(index1+11)=='>') {
			// ignore tags in sequences
			index1 = header.indexOf(tag, index1+10);
			if (index1==-1) return null;
		}
		index1 = header.indexOf(":", index1);
		if (index1==-1) return null;
		int index2 = header.indexOf("\n", index1);
		String value = header.substring(index1+1, index2);
		return value;
	}
}
