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

import daemon.tools.firstNameDB;
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


public class DicomWorker extends DaemonWorker {

	protected static String DEFAULT_STRING = "Unknown"; // chaine de caractere par defaut quand un champs dicom est vide
	protected static float DEFAULT_FLOAT = -1.0f; // idem pour les float
	// Attributs
	protected Path dicomFile;
	protected DicomJobDispatcher dispatcher;
	protected DicomImage dicomImage;
	protected String header;
	protected String birthdate;
	protected String sex;
	protected String sopinstanceid;
	protected float size;
	protected float weight;
	protected String mri_name;
	protected float repetitiontime;
	protected float echotime;
	protected float slicethickness;
	protected float voxelwidth;
	protected float voxelheight;
	protected int project_id;
	protected int patient_id;
	protected int acqDate_id;
	protected int protocol_id;
	protected int serie_id;
	protected Path newPath;// nouveau chemin vers le fichier (apres deplacement dans repertoire dicom)
	
	public DicomWorker(DicomJobDispatcher pDaemon, Path filename) throws DicomException, IOException{
		super();
		setDispatcher(pDaemon);
		setDicomFile(filename);
		setServerInfo(getDispatcher().getServerInfo());
	}

	/**
	 * Constructeur vide
	 */
	public DicomWorker(){
		super();
	}
	
	// Accesseurs
	
	public Path getDicomFile() {
		return dicomFile;
	}

	public void setDicomFile(Path dicomFile) throws DicomException, IOException {
		this.dicomFile = dicomFile;
		if(DicomImage.isRda(dicomFile.toFile())){
			header = DicomImage.getRdaHeader(dicomFile.toFile());
		}else{
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
	}


	public DicomJobDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(DicomJobDispatcher parentDaemon) {
		this.dispatcher = parentDaemon;
	}
	
	
	
	// Methodes

	public void start() throws DicomException, AnonymizationException{
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
		if(getServerInfo().getPpid().contains(studyName) || DicomImage.isRda(dicomFile.toFile()))
			patientName = getPatientId();
		else
			patientName = getPatientName();
		if(firstNameDB.matches(patientName))
			throw new AnonymizationException("DICOM not anonymized.");
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
		sopinstanceid = getSOPInstanceUID();
		protocolName = getProtocolName();
		serieName = getSeriesDescription();
		try{
			acqDate = getAcquisitionDate();	
		}catch(DicomException de){// on prend la date de la serie si on a pas la date d'acquisition
			acqDate = getSerieDate();
			WindowManager.mwLogger.log(Level.WARNING, "DicomWorker dicomException,acqDate set to serieDate",de);
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
		
		
		newPath = Paths.get(serieFolder + File.separator + sopinstanceid);
		
		// On deplace
		moveDicomTo(newPath);
		
		// On construit l'objet dicom
		dicomImage = new DicomImage();
		dicomImage.setName(sopinstanceid);
		dicomImage.setSliceLocation(getSliceLocation());
		dicomImage.setProjet(new Project(getProject_id()));
		dicomImage.setPatient(new Patient(getPatient_id()));
		dicomImage.setProtocole(new Protocol(getProtocol_id()));
		dicomImage.setAcquistionDate(new AcquisitionDate(getAcqDate_id()));
		dicomImage.setSerie(new Serie(getSerie_id()));
		
		// On ajoute le fichier brute dans la liste des fichiers
		// a encrypter 
		DicomEncryptDaemon encryptd = SystemSettings.ENCRYPT_DAEMON; // daemon pour l'encryptage
		if(encryptd!=null && encryptd.isAlive()){
			encryptd.addDicomToEncrypt(newPath, dicomImage);
		}else{
			WindowManager.mwLogger.log(Level.SEVERE,"Critical error : Encrypt Daemon offline, can't forward ... Please restart");
			getDispatcher().setCrashed(true);
			if(getDispatcher().getSettings().getServerMode() == ServerMode.SERVER){
				WindowManager.MAINWINDOW.getSstatusPanel().getLblCommentdicomdispatcher().setText("Critical error : Encrypt Daemon offline, can't forward ... Please restart");
				WindowManager.MAINWINDOW.getSstatusPanel().setCritical(WindowManager.MAINWINDOW.getSstatusPanel().getBtnDicomdispatcherstatus());
				while(!(encryptd!=null && encryptd.isAlive())){
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		// On termine
		prepareToStop();
	}

	// Set des ID serie // protocol // projet etc depuis la BDD
	protected void setSerie_idFromDB(Path fileName) {
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
			WindowManager.mwLogger.log(Level.WARNING, "setSerie_idFromDB exception",e);
		}
	}


	/**
	 * Definition de l'attribut Protocol_id grace a son nom (repertoire) depuis la BDD
	 * @param fileName
	 */
	protected void setProtocol_idFromDB(Path fileName) {
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
			WindowManager.mwLogger.log(Level.WARNING, "setProtocol_idFromDB exception",e);
		}
	}

	/**
	 * Definition de l'attribut Acqdate_id grace a son nom (repertoire) depuis la BDD
	 * @param fileName
	 */
	protected void setAcqDate_idFromDB(Path fileName) {
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
			WindowManager.mwLogger.log(Level.WARNING, "setAcqDate_idFromDB exception",e);
		}
	}


	/**
	 * Definition de l'attribut Patient_id grace a son nom (repertoire) depuis la BDD
	 * @param fileName
	 */
	protected void setPatient_idFromDB(Path fileName) {
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
			Patient p = pdao.retrievePatient(fileName.toString(),this.birthdate,this.sex,this.size,this.weight, getProject_id());
			setPatient_id(p.getId());
			cache.getIdPatientList().put(fileName.toString() + "@@" + getProject_id(), p.getId());
		} catch (SQLException e) {
			WindowManager.mwLogger.log(Level.WARNING, "setPatient_idFromDB exception",e);
		}
	}


	/**
	 * Definition de l'attribut Project_id grace a son nom (repertoire) depuis la BDD
	 * @param fileName
	 */
	protected void setProject_idFromDB(Path fileName) {
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
			WindowManager.mwLogger.log(Level.WARNING, "setProject_idFromDB exception",e);
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
				WindowManager.mwLogger.log(Level.WARNING, "addEntryToDB exception",e.toString());
			}
			break;
		case "Patient":
			PatientDAO patdao = new MySQLPatientDAO();
			try {
				patdao.newPatient(name.toString(),this.birthdate,this.sex,this.size, this.weight, getProject_id());
				setPatient_id(patdao.idmax());
			} catch (SQLException e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
				WindowManager.mwLogger.log(Level.WARNING, "addEntryToDB exception",e.toString());
			}
			break;
		case "AcqDate":
			AcquisitionDateDAO acqdao = new MySQLAcquisitionDateDAO();
			try {
				acqdao.newAcqDate(name.toString(), getProject_id(), getPatient_id());
				setAcqDate_id(acqdao.idmax());
			} catch (SQLException e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
				WindowManager.mwLogger.log(Level.WARNING, "addEntryToDB exception",e.toString());
			}
			break;
		case "Protocol":
			ProtocolDAO protdao = new MySQLProtocolDAO();
			try {
				protdao.newProtocol(name.toString(), getProject_id(), getPatient_id(),getAcqDate_id());
				setProtocol_id(protdao.idmax());
			} catch (SQLException e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
				WindowManager.mwLogger.log(Level.WARNING, "addEntryToDB exception",e.toString());
			}
			break;
		case "Serie":
			SerieDAO sdao = new MySQLSerieDAO();
			try {
				sdao.newSerie(name.toString(), this.mri_name,this.repetitiontime,this.echotime,this.slicethickness,this.voxelwidth,this.voxelheight,0,0, getProject_id(), getPatient_id(),getAcqDate_id(),getProtocol_id());
				setSerie_id(sdao.idmax());
			} catch (SQLException e) {
				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
				WindowManager.mwLogger.log(Level.WARNING, "addEntryToDB exception",e.toString());
			}
			break;
		default:
			WindowManager.mwLogger.log(Level.SEVERE, "Unknow table : "+table);
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
			WindowManager.mwLogger.log(Level.SEVERE, "Error moving dicom to "+newPath,e);
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
			WindowManager.mwLogger.log(Level.SEVERE, "Error copying dicom to "+newPath,e);
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
	
	// Renvoi le ntag specifie par tag sous forme d'une chaine de caractere
	public String getCustomTag(String tag) throws DicomException{
		String prot = getTag(tag);
		if(prot == null){
			throw new DicomException("Unable to decode DICOM header "+tag);
		}
		if(prot.isEmpty())
			return DEFAULT_STRING;
		// On enleve les espace en debut de chaine
		while(prot.length()>1 && prot.charAt(0) == ' ')
			prot = prot.substring(1);	
		if(prot.equals(" "))// si le champs est vide
			return DEFAULT_STRING;
		// on enleve les espaces en fin de chaine
		while(prot.length()>1 && prot.charAt(prot.length()-1) == ' ')
			prot = prot.substring(0,prot.length()-1);	
		// on remplace les caracteres complique par "_"
		prot = prot.replaceAll("[^A-Za-z0-9\\.]" , "_");
		WindowManager.mwLogger.log(Level.FINEST, "getCustomTag : "+tag);
		return prot;
	}
	
	// Renvoi le nom du protocole medical
	public String getStudyDescription() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "StudyDescription";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String prot = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					prot = pi.split(":")[1];
					if(prot.isEmpty())
						return DEFAULT_STRING;
					// On enleve les espace en debut de chaine
					while(prot.length()>1 && prot.charAt(0) == ' ')
						prot = prot.substring(1);	
					if(prot.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(prot.length()>1 && prot.charAt(prot.length()-1) == ' ')
						prot = prot.substring(0,prot.length()-1);	
					if(prot.contains("^"))
						prot = prot.substring(prot.lastIndexOf("^")+1,prot.length());
					// on remplace les caracteres complique par "_"
					prot = prot.replaceAll("[^A-Za-z0-9\\.]" , "_");
					return prot;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String prot = getTag("0008,1030");
			if(prot == null){
				throw new DicomException("Unable to decode DICOM header 0008,1030");
			}
			if(prot.isEmpty())
				return DEFAULT_STRING;
			// On enleve les espace en debut de chaine
			while(prot.length()>1 && prot.charAt(0) == ' ')
				prot = prot.substring(1);	
			if(prot.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(prot.length()>1 && prot.charAt(prot.length()-1) == ' ')
				prot = prot.substring(0,prot.length()-1);	
			if(prot.contains("^"))
				prot = prot.substring(prot.lastIndexOf("^")+1,prot.length());
			// on remplace les caracteres complique par "_"
			prot = prot.replaceAll("[^A-Za-z0-9\\.]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getStudyDescription : "+prot);
			return prot;
		}
		
	}
	
	// Nom du patient
	public String getPatientName() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "PatientName";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String pname = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					pname = pi.split(":")[1];
					if(pname.isEmpty())
						return DEFAULT_STRING;
					// On enleve les espace en debut de chaine
					while(pname.length()>1 && pname.charAt(0) == ' ')
						pname = pname.substring(1);	
					if(pname.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(pname.length()>1 && pname.charAt(pname.length()-1) == ' ')
						pname = pname.substring(0,pname.length()-1);	
					// on remplace les caracteres complique par "_"
					pname = pname.replaceAll("[^A-Za-z0-9\\.-]" , "_");
					// on enleve les champs en plus qui trainent a la fin et qu'on a remplace par des "_" 
					while(pname.length()>1 && pname.charAt(pname.length()-1) == '_')
						pname = pname.substring(0,pname.length()-1);	
					WindowManager.mwLogger.log(Level.FINEST, "getPatientName : "+pname);
					return pname;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String pname = getTag("0010,0010");
			if(pname == null){
				throw new DicomException("Unable to decode DICOM header 0010,0010");
			}
			if(pname.isEmpty())
				return DEFAULT_STRING;
			// On enleve les espace en debut de chaine
			while(pname.length()>1 && pname.charAt(0) == ' ')
				pname = pname.substring(1);	
			if(pname.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(pname.length()>1 && pname.charAt(pname.length()-1) == ' ')
				pname = pname.substring(0,pname.length()-1);	
			// on remplace les caracteres complique par "_"
			pname = pname.replaceAll("[^A-Za-z0-9\\.-]" , "_");
			// on enleve les champs en plus qui trainent a la fin et qu'on a remplace par des "_" 
			while(pname.length()>1 && pname.charAt(pname.length()-1) == '_')
				pname = pname.substring(0,pname.length()-1);	
			WindowManager.mwLogger.log(Level.FINEST, "getPatientName : "+pname);
			return pname;
		}
	}
	
	// Nom du patient
	public String getPatientId() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "PatientID";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String pname = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					pname = pi.split(":")[1];
					if(pname.isEmpty())
						return DEFAULT_STRING;
					// On enleve les espace en debut de chaine
					while(pname.length()>1 && pname.charAt(0) == ' ')
						pname = pname.substring(1);	
					if(pname.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(pname.length()>1 && pname.charAt(pname.length()-1) == ' ')
						pname = pname.substring(0,pname.length()-1);	
					// on remplace les caracteres complique par "_"
					pname = pname.replaceAll("[^A-Za-z0-9\\.-]" , "_");
					// on enleve les champs en plus qui trainent a la fin et qu'on a remplace par des "_" 
					while(pname.length()>1 && pname.charAt(pname.length()-1) == '_')
						pname = pname.substring(0,pname.length()-1);	
					WindowManager.mwLogger.log(Level.FINEST, "getPatientName : "+pname);
					return pname;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String pname = getTag("0010,0020");
			if(pname == null){
				throw new DicomException("Unable to decode DICOM header 0010,0020");
			}
			if(pname.isEmpty())
				return DEFAULT_STRING;
			// On enleve les espace en debut de chaine
			while(pname.length()>1 && pname.charAt(0) == ' ')
				pname = pname.substring(1);	
			if(pname.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(pname.length()>1 && pname.charAt(pname.length()-1) == ' ')
				pname = pname.substring(0,pname.length()-1);	
			// on remplace les caracteres complique par "_"
			pname = pname.replaceAll("[^A-Za-z0-9\\.-]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getPatientId : "+pname);
			return pname;
		}
	}
	
	// ID du dicom (SOP Instance UID)
	public String getSOPInstanceUID() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			return dicomFile.getFileName().toString();
		}else{
			String pname = getTag("0008,0018");
			if(pname == null){
				throw new DicomException("Unable to decode DICOM header 0008,0018");
			}
			if(pname.isEmpty())
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(pname.length()>1 && pname.charAt(pname.length()-1) == ' ')
				pname = pname.substring(0,pname.length()-1);
			while(pname.length()>1 && pname.charAt(0) == ' ')
				pname = pname.substring(1,pname.length());
			if(pname.equals(" "))// si le champs est vide
				throw new DicomException("Unable to decode DICOM header 0008,0018");
			WindowManager.mwLogger.log(Level.FINEST, "getSOPInstanceUID : "+pname);
			return pname;
		}
	}
	
	
	// Nom de la sequence (ex:  Series Description: PHA_IMAGES)
	public String getSeriesDescription() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "SeriesDescription";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String sdesc = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					sdesc = pi.split(":")[1];
					if(sdesc.isEmpty())
						return DEFAULT_STRING;
					// On enleve les espace en debut de chaine
					while(sdesc.length()>1 && sdesc.charAt(0) == ' ')
						sdesc = sdesc.substring(1);	
					if(sdesc.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(sdesc.length()>1 && sdesc.charAt(sdesc.length()-1) == ' ')
						sdesc = sdesc.substring(0,sdesc.length()-1);
					// on remplace les caracteres complique par "_"
					sdesc = sdesc.replaceAll("[^A-Za-z0-9\\.-]" , "_");
					WindowManager.mwLogger.log(Level.FINEST, "getSeriesDescription : "+sdesc);
					return sdesc;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String sdesc = getTag("0008,103E");
			if(sdesc == null){
				throw new DicomException("Unable to decode DICOM header 0008,103E");
			}
			if(sdesc.isEmpty())
				return DEFAULT_STRING;
			// On enleve les espace en debut de chaine
			while(sdesc.length()>1 && sdesc.charAt(0) == ' ')
				sdesc = sdesc.substring(1);	
			if(sdesc.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(sdesc.length()>1 && sdesc.charAt(sdesc.length()-1) == ' ')
				sdesc = sdesc.substring(0,sdesc.length()-1);
			// on remplace les caracteres complique par "_"
			sdesc = sdesc.replaceAll("[^A-Za-z0-9\\.-]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getSeriesDescription : "+sdesc);
			return sdesc;
		}
	}
	
	// Date de naissance
	public String getBirthdate() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "PatientBirthDate";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String bdate = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					bdate = pi.split(":")[1];
					if(bdate.isEmpty())
						return DEFAULT_STRING;
					// On enleve les espace en debut de chaine
					while(bdate.length()>1 && bdate.charAt(0) == ' ')
						bdate = bdate.substring(1);	
					if(bdate.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(bdate.length()>1 && bdate.charAt(bdate.length()-1) == ' ')
						bdate = bdate.substring(0,bdate.length()-1);
					// on remplace les caracteres complique par "_"
					bdate = bdate.replaceAll("[^A-Za-z0-9\\.]" , "_");
					WindowManager.mwLogger.log(Level.FINEST, "getBirthdate : "+bdate);
					return bdate;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String bdate = getTag("0010,0030");
			if(bdate == null){
				throw new DicomException("Unable to decode DICOM header 0010,0030");
			}
			if(bdate.isEmpty())
				return DEFAULT_STRING;
			// On enleve les espace en debut de chaine
			while(bdate.length()>1 && bdate.charAt(0) == ' ')
				bdate = bdate.substring(1);	
			if(bdate.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(bdate.length()>1 && bdate.charAt(bdate.length()-1) == ' ')
				bdate = bdate.substring(0,bdate.length()-1);
			// on remplace les caracteres complique par "_"
			bdate = bdate.replaceAll("[^A-Za-z0-9\\.]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getBirthdate : "+bdate);
			return bdate;
		}
	}
	// Sexe du patient
	public String getSex() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "PatientSex";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String psex = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					psex = pi.split(":")[1];
					if(psex.isEmpty())
						return DEFAULT_STRING;
					// On enleve les espace en debut de chaine
					while(psex.length()>1 && psex.charAt(0) == ' ')
						psex = psex.substring(1);	
					if(psex.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(psex.length()>1 && psex.charAt(psex.length()-1) == ' ')
						psex = psex.substring(0,psex.length()-1);
					// on remplace les caracteres complique par "_"
					psex = psex.replaceAll("[^A-Za-z0-9\\.]" , "_");
					WindowManager.mwLogger.log(Level.FINEST, "getSex : "+psex);
					return psex;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String psex = getTag("0010,0040");
			if(psex == null){
				throw new DicomException("Unable to decode DICOM header 0010,0040");
			}
			if(psex.isEmpty())
				return DEFAULT_STRING;
			// On enleve les espace en debut de chaine
			while(psex.length()>1 && psex.charAt(0) == ' ')
				psex = psex.substring(1);	
			if(psex.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(psex.length()>1 && psex.charAt(psex.length()-1) == ' ')
				psex = psex.substring(0,psex.length()-1);
			// on remplace les caracteres complique par "_"
			psex = psex.replaceAll("[^A-Za-z0-9\\.]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getSex : "+psex);
			return psex;
		}
	}	
	
	/**
	 * Patient weight
	 * @return
	 * @throws DicomException
	 */
	public float getPatientWeight() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "PatientWeight";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String pweight = "";
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					pweight = pi.split(":")[1];
					if(pweight.isEmpty())
						return DEFAULT_FLOAT;
					// On enleve les espace en debut de chaine
					while(pweight.length()>1 && pweight.charAt(0) == ' ')
						pweight = pweight.substring(1);	
					if(pweight.equals(" "))// si le champs est vide
						return DEFAULT_FLOAT;
					// on enleve les espaces en fin de chaine
					while(pweight.length()>1 && pweight.charAt(pweight.length()-1) == ' ')
						pweight = pweight.substring(0,pweight.length()-1);
					// on remplace les caracteres complique par "_"
					pweight = pweight.replaceAll("[^A-Za-z0-9\\.]" , "_");
					WindowManager.mwLogger.log(Level.FINEST, "getPatientWeight : "+pweight);
					return Float.parseFloat(pweight);
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String pweight = getTag("0010,1030");
			if(pweight == null){
				throw new DicomException("Unable to decode DICOM header 0010,1030");
			}
			if(pweight.isEmpty())
				return DEFAULT_FLOAT;
			// On enleve les espace en debut de chaine
			while(pweight.length()>1 && pweight.charAt(0) == ' ')
				pweight = pweight.substring(1);	
			if(pweight.equals(" "))// si le champs est vide
				return DEFAULT_FLOAT;
			// on enleve les espaces en fin de chaine
			while(pweight.length()>1 && pweight.charAt(pweight.length()-1) == ' ')
				pweight = pweight.substring(0,pweight.length()-1);
			// on remplace les caracteres complique par "_"
			pweight = pweight.replaceAll("[^A-Za-z0-9\\.]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getPatientWeight : "+pweight);
			return Float.parseFloat(pweight);
		}
	}
	
	/**
	 * Patient size
	 * @return
	 * @throws DicomException
	 */
	public float getPatientSize() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			return DEFAULT_FLOAT;
		}else{
			String psize = getTag("0010,1020");
			if(psize == null){
				return DEFAULT_FLOAT;
				//throw new DicomException("Unable to decode DICOM header 0010,1020");
			}
			if(psize.isEmpty())
				return DEFAULT_FLOAT;
			// On enleve les espace en debut de chaine
			while(psize.length()>1 && psize.charAt(0) == ' ')
				psize = psize.substring(1);	
			if(psize.equals(" "))// si le champs est vide
				return DEFAULT_FLOAT;
			// on enleve les espaces en fin de chaine
			while(psize.length()>1 && psize.charAt(psize.length()-1) == ' ')
				psize = psize.substring(0,psize.length()-1);
			// on remplace les caracteres complique par "_"
			psize = psize.replaceAll("[^A-Za-z0-9\\.]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getPatientSize : "+psize);
			return Float.parseFloat(psize);
		}
	}
	
	// Nom de l'IRM
	public String getMri_name() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "ModelName";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String iname = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					iname = pi.split(":")[1];
					if(iname.isEmpty())
						return DEFAULT_STRING;
					// On enleve les espace en debut de chaine
					while(iname.length()>1 && iname.charAt(0) == ' ')
						iname = iname.substring(1);	
					if(iname.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(iname.length()>1 && iname.charAt(iname.length()-1) == ' ')
						iname = iname.substring(0,iname.length()-1);
					// on remplace les caracteres complique par "_"
					iname = iname.replaceAll("[^A-Za-z0-9\\.]" , "_");
					WindowManager.mwLogger.log(Level.FINEST, "getMri_name : "+iname);
					return iname;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String iname = getTag("0008,1090");
			if(iname == null){
				throw new DicomException("Unable to decode DICOM header 0008,1090");
			}
			if(iname.isEmpty())
				return DEFAULT_STRING;
			// On enleve les espace en debut de chaine
			while(iname.length()>1 && iname.charAt(0) == ' ')
				iname = iname.substring(1);	
			if(iname.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(iname.length()>1 && iname.charAt(iname.length()-1) == ' ')
				iname = iname.substring(0,iname.length()-1);
			// on remplace les caracteres complique par "_"
			iname = iname.replaceAll("[^A-Za-z0-9\\.]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getMri_name : "+iname);
			return iname;
		}
	}
		
	// localisation de la slice
	// champs facultatif
	public float getSliceLocation(){
		if(DicomImage.isRda(dicomFile.toFile())){
			return DEFAULT_FLOAT;
		}else{
			String sl = getTag("0020,1041");
			if(sl == null){
				return DEFAULT_FLOAT; // comme c'est un champs facultatif on lance pas d'execption
			}
			if(sl.isEmpty())
				return DEFAULT_FLOAT;
			// On enleve les espace en debut de chaine
			while(sl.length()>1 && sl.charAt(0) == ' ')
				sl = sl.substring(1);	
			if(sl.equals(" "))// si le champs est vide
				return DEFAULT_FLOAT;
			// on enleve les espaces en fin de chaine
			while(sl.length()>1 && sl.charAt(sl.length()-1) == ' ')
				sl = sl.substring(0,sl.length()-1);
			// on remplace les caracteres complique par "_"
			WindowManager.mwLogger.log(Level.FINEST, "getSliceLocation : "+sl);
			return Float.parseFloat(sl);
		}
	}
		
	// Nom du protocole d'acquisition (ex:  SWI3D TRA 1.5mm JEREMY)
	public String getProtocolName() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "ProtocolName";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String pprot = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					pprot = pi.split(":")[1];
					if(pprot.isEmpty())
						return DEFAULT_STRING;
					// On enleve les espace en debut de chaine
					while(pprot.length()>1 && pprot.charAt(0) == ' ')
						pprot = pprot.substring(1);	
					if(pprot.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(pprot.length()>1 && pprot.charAt(pprot.length()-1) == ' ')
						pprot = pprot.substring(0,pprot.length()-1);
					// on remplace les caracteres complique par "_"
					pprot = pprot.replaceAll("[^A-Za-z0-9\\.-]" , "_");
					WindowManager.mwLogger.log(Level.FINEST, "getProtocolName : "+pprot);
					return pprot;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String pprot = getTag("0018,1030");
			if(pprot == null){
				throw new DicomException("Unable to decode DICOM header 0018,1030");
			}
			if(pprot.isEmpty())
				return DEFAULT_STRING;
			// On enleve les espace en debut de chaine
			while(pprot.length()>1 && pprot.charAt(0) == ' ')
				pprot = pprot.substring(1);	
			if(pprot.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(pprot.length()>1 && pprot.charAt(pprot.length()-1) == ' ')
				pprot = pprot.substring(0,pprot.length()-1);
			// on remplace les caracteres complique par "_"
			pprot = pprot.replaceAll("[^A-Za-z0-9\\.-]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getProtocolName : "+pprot);
			return pprot;
		}
	}
	
	// Taille du pixel en x y
	public String[] getPixelSpacing() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "PixelSpacingRow";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String[] ps = new String[]{DEFAULT_STRING,DEFAULT_STRING};
			boolean ok = false;
			for(int i = 0; i<params.length;i++){
				String pi = params[i];
				if(pi.contains(name)){
					ps[0] = pi.split(":")[1];
					ps[1] = params[i+1].split(":")[1];
					// On enleve les espace en debut de chaine
					while(ps[0].length()>1 && ps[0].charAt(0) == ' ')
						ps[0] = ps[0].substring(1);	
					while(ps[1].length()>1 && ps[1].charAt(0) == ' ')
						ps[1] = ps[1].substring(1);	
					if(ps[0].equals(" ") || ps[1].equals(" ") )// si le champs est vide
						return new String[]{DEFAULT_STRING,DEFAULT_STRING};
					// on enleve les espaces en fin de chaine
					while(ps[0].length()>1 && ps[0].charAt(ps[0].length()-1) == ' ')
						ps[0] = ps[0].substring(0,ps[0].length()-1);
					while(ps[1].length()>1 && ps[1].charAt(ps[1].length()-1) == ' ')
						ps[1] = ps[1].substring(0,ps[1].length()-1);
					// on split sur "\" pour recup x et y
					WindowManager.mwLogger.log(Level.FINEST, "getPixelSpacing : "+ps);
					return ps;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String ps = getTag("0028,0030");
			if(ps == null){
				throw new DicomException("Unable to decode DICOM header 0028,0030");
			}
			if(ps.isEmpty())
				return new String[]{DEFAULT_STRING,DEFAULT_STRING};
			// On enleve les espace en debut de chaine
			while(ps.length()>1 && ps.charAt(0) == ' ')
				ps = ps.substring(1);	
			if(ps.equals(" "))// si le champs est vide
				return new String[]{DEFAULT_STRING,DEFAULT_STRING};
			// on enleve les espaces en fin de chaine
			while(ps.length()>1 && ps.charAt(ps.length()-1) == ' ')
				ps = ps.substring(0,ps.length()-1);
			// on split sur "\" pour recup x et y
			String[] pixelSpacing = ps.split("\\\\");
			WindowManager.mwLogger.log(Level.FINEST, "getPixelSpacing : "+ps);
			return pixelSpacing;
		}
	}
	
	// recupere le TR
	public float getRepetitionTime() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "TR";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String rt = "";
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name+":")){
					rt = pi.split(":")[1];
					if(rt.isEmpty())
						return DEFAULT_FLOAT;
					// On enleve les espace en debut de chaine
					while(rt.length()>1 && rt.charAt(0) == ' ')
						rt = rt.substring(1);	
					if(rt.equals(" "))// si le champs est vide
						return DEFAULT_FLOAT;
					// on enleve les espaces en fin de chaine
					while(rt.length()>1 && rt.charAt(rt.length()-1) == ' ')
						rt = rt.substring(0,rt.length()-1);
					// on remplace les caracteres complique par "_"
					WindowManager.mwLogger.log(Level.FINEST, "getRepetitionTime : "+rt);
					return Float.parseFloat(rt);
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String rt = getTag("0018,0080");
			if(rt == null){
				return DEFAULT_FLOAT;//throw new DicomException("Unable to decode DICOM header 0018,0080");
			}
			if(rt.isEmpty())
				return DEFAULT_FLOAT;
			// On enleve les espace en debut de chaine
			while(rt.length()>1 && rt.charAt(0) == ' ')
				rt = rt.substring(1);	
			if(rt.equals(" "))// si le champs est vide
				return DEFAULT_FLOAT;
			// on enleve les espaces en fin de chaine
			while(rt.length()>1 && rt.charAt(rt.length()-1) == ' ')
				rt = rt.substring(0,rt.length()-1);
			// on remplace les caracteres complique par "_"
			WindowManager.mwLogger.log(Level.FINEST, "getRepetitionTime : "+rt);
			return Float.parseFloat(rt);
		}
	}
	
	// recupere le TE
	public Float getEchoTime() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "TE";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String et = "";
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name+":")){
					et = pi.split(":")[1];
					if(et.isEmpty())
						return DEFAULT_FLOAT;
					// On enleve les espace en debut de chaine
					while(et.length()>1 && et.charAt(0) == ' ')
						et = et.substring(1);	
					if(et.equals(" "))// si le champs est vide
						return DEFAULT_FLOAT;
					// on enleve les espaces en fin de chaine
					while(et.length()>1 && et.charAt(et.length()-1) == ' ')
						et = et.substring(0,et.length()-1);
					// on remplace les caracteres complique par "_"
					WindowManager.mwLogger.log(Level.FINEST, "getEchoTime : "+et);
					return Float.parseFloat(et);
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String et = getTag("0018,0081");
			if(et == null){
				return DEFAULT_FLOAT;//throw new DicomException("Unable to decode DICOM header 0018,0081");
			}
			if(et.isEmpty())
				return DEFAULT_FLOAT;
			// On enleve les espace en debut de chaine
			while(et.length()>1 && et.charAt(0) == ' ')
				et = et.substring(1);	
			if(et.equals(" "))// si le champs est vide
				return DEFAULT_FLOAT;
			// on enleve les espaces en fin de chaine
			while(et.length()>1 && et.charAt(et.length()-1) == ' ')
				et = et.substring(0,et.length()-1);
			// on remplace les caracteres complique par "_"
			WindowManager.mwLogger.log(Level.FINEST, "getEchoTime : "+et);
			return Float.parseFloat(et);
		}
	}
		
	// Taille du pixel en z
	public float getSliceThickness() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "SliceThickness";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String st = "";
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name+":")){
					st = pi.split(":")[1];
					if(st.isEmpty())
						return DEFAULT_FLOAT;
					// On enleve les espace en debut de chaine
					while(st.length()>1 && st.charAt(0) == ' ')
						st = st.substring(1);	
					if(st.equals(" "))// si le champs est vide
						return DEFAULT_FLOAT;
					// on enleve les espaces en fin de chaine
					while(st.length()>1 && st.charAt(st.length()-1) == ' ')
						st = st.substring(0,st.length()-1);
					WindowManager.mwLogger.log(Level.FINEST, "getSliceThickness : "+st);
					return Float.parseFloat(st);
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String st = getTag("0018,0050");
			if(st == null){
				throw new DicomException("Unable to decode DICOM header 0018,0050");
			}
			if(st.isEmpty())
				return DEFAULT_FLOAT;
			// On enleve les espace en debut de chaine
			while(st.length()>1 && st.charAt(0) == ' ')
				st = st.substring(1);	
			if(st.equals(" "))// si le champs est vide
				return DEFAULT_FLOAT;
			// on enleve les espaces en fin de chaine
			while(st.length()>1 && st.charAt(st.length()-1) == ' ')
				st = st.substring(0,st.length()-1);
			WindowManager.mwLogger.log(Level.FINEST, "getSliceThickness : "+st);
			return Float.parseFloat(st);
		}
	}
		

	// Date de l'acquisition ex : 20130122
	public String getAcquisitionDate() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "StudyDate";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String pdate = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					pdate = pi.split(":")[1];
					if(pdate.isEmpty())
						return DEFAULT_STRING;
					while(pdate.length()>1 && pdate.charAt(0) == ' ')
						pdate = pdate.substring(1);	
					if(pdate.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(pdate.length()>1 && pdate.charAt(pdate.length()-1) == ' ')
						pdate = pdate.substring(0,pdate.length()-1);
					// on remplace les caracteres complique par "_"
					pdate = pdate.replaceAll("[^A-Za-z0-9\\.]" , "_");
					WindowManager.mwLogger.log(Level.FINEST, "getAcquisitionDate : "+pdate);
					return pdate;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String pdate = getTag("0008,0022");
			if(pdate == null){
				throw new DicomException("Unable to decode DICOM header 0008,0022");
			}
			if(pdate.isEmpty())
				return DEFAULT_STRING;
			while(pdate.length()>1 && pdate.charAt(0) == ' ')
				pdate = pdate.substring(1);	
			if(pdate.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(pdate.length()>1 && pdate.charAt(pdate.length()-1) == ' ')
				pdate = pdate.substring(0,pdate.length()-1);
			// on remplace les caracteres complique par "_"
			pdate = pdate.replaceAll("[^A-Za-z0-9\\.]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getAcquisitionDate : "+pdate);
			return pdate;
		}
	}
	// Date de la serie ex : 20130122
	public String getSerieDate() throws DicomException{
		if(DicomImage.isRda(dicomFile.toFile())){
			String name = "SeriesDate";
			String[] params = header.split(DicomImage.RDA_SPLIT_CHAR);
			String pdate = DEFAULT_STRING;
			boolean ok = false;
			for(String pi:params){
				if(pi.contains(name)){
					pdate = pi.split(":")[1];
					if(pdate.isEmpty())
						return DEFAULT_STRING;
					while(pdate.length()>1 && pdate.charAt(0) == ' ')
						pdate = pdate.substring(1);	
					if(pdate.equals(" "))// si le champs est vide
						return DEFAULT_STRING;
					// on enleve les espaces en fin de chaine
					while(pdate.length()>1 && pdate.charAt(pdate.length()-1) == ' ')
						pdate = pdate.substring(0,pdate.length()-1);
					// on remplace les caracteres complique par "_"
					pdate = pdate.replaceAll("[^A-Za-z0-9\\.]" , "_");
					WindowManager.mwLogger.log(Level.FINEST, "getAcquisitionDate : "+pdate);
					return pdate;
				}
			}
			throw new DicomException("Unable to decode RDA header "+name);
		}else{
			String pdate = getTag("0008,0023");
			if(pdate == null){
				throw new DicomException("Unable to decode DICOM header 0008,0023");
			}
			if(pdate.isEmpty())
				return DEFAULT_STRING;
			while(pdate.length()>1 && pdate.charAt(0) == ' ')
				pdate = pdate.substring(1);	
			if(pdate.equals(" "))// si le champs est vide
				return DEFAULT_STRING;
			// on enleve les espaces en fin de chaine
			while(pdate.length()>1 && pdate.charAt(pdate.length()-1) == ' ')
				pdate = pdate.substring(0,pdate.length()-1);
			// on remplace les caracteres complique par "_"
			pdate = pdate.replaceAll("[^A-Za-z0-9\\.]" , "_");
			WindowManager.mwLogger.log(Level.FINEST, "getSerieDate : "+pdate);
			return pdate;
		}
	}
	public Path getNewPath() {
		return newPath;
	}


	public void setNewPath(Path newPath) {
		this.newPath = newPath;
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
	protected String getTag(String tag) {
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
	
	/**
	 * Reinitialise les variables pour un dicom
	 */
	protected void resetVars() {
		dicomFile = null;
		dicomImage = null;
		header = null;
		birthdate = null;
		sex = null;
		size = DEFAULT_FLOAT;
		weight = DEFAULT_FLOAT;
		mri_name = null;
		repetitiontime = DEFAULT_FLOAT;
		echotime = DEFAULT_FLOAT;
		slicethickness = DEFAULT_FLOAT;
		voxelwidth = DEFAULT_FLOAT;
		voxelheight = DEFAULT_FLOAT;
		project_id = -1;
		patient_id = -1;
		acqDate_id = -1;
		protocol_id = -1;
		serie_id = -1;
		newPath = null;//
	}

}
