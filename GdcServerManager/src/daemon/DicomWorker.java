package daemon;

import ij.ImagePlus;
import ij.util.DicomTools;

import java.io.File;
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

import model.AcquisitionDate;
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
	private Path dicomFile;
	private DicomJobDispatcher dispatcher;
	private DicomImage dicomImage;
	private ImagePlus imp;
	private int project_id;
	private int patient_id;
	private int acqDate_id;
	private int protocol_id;
	private int serie_id;
	
	public DicomWorker(DicomJobDispatcher pDaemon, Path filename) {
		// TODO Auto-generated constructor stub
		setDispatcher(pDaemon);
		setDicomFile(filename);
		setServerInfo(getDispatcher().getServerInfo());
	}

	
	// Accesseurs
	
	public Path getDicomFile() {
		return dicomFile;
	}

	public void setDicomFile(Path dicomFile) {
		this.dicomFile = dicomFile;
		setImp(new ImagePlus(dicomFile.toFile().getAbsolutePath()));
	}

	public ImagePlus getImp() {
		return imp;
	}


	public void setImp(ImagePlus imp) {
		this.imp = imp;
	}


	public DicomJobDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(DicomJobDispatcher parentDaemon) {
		this.dispatcher = parentDaemon;
	}
	
	
	
	// Methodes

	public void start(){
		// On recupere le nom du protocole medical
		String studyName = getStudyDescription();
		// Si le protocole est null alors le fichier est encore en cours de copie
		if(studyName == null){
			prepareToStop();
			return;
		}	
		String patientName = getPatientName();
		String protocolName = getProtocolName();
		String serieName = getSeriesDescription();
		String acqDate = getAcquisitionDate();		
		
		// On créé les chemins vers les répertoires
		Path studyFolder = Paths.get(serverInfo.getDicomDir() + File.separator + studyName);
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
		
		// AES crypt
		// Encrypte le fichier en rajoutant l'extension definit dans AESCrypt.ENCRYPTSUFFIX
		try {
			AESCrypt aes = new AESCrypt(false, getAESPass(getProject_id()));
			aes.encrypt(2, newPath.toString(), newPath+AESCrypt.ENCRYPTSUFFIX);
			// On supprime le fichier non encrypte
			Files.deleteIfExists(newPath);
			
			
			// On ajoute l'entree du DICOM dans la database
			addEntryToDB(dicomFile.getFileName(),"DicomImage");
			
		} catch (GeneralSecurityException | IOException e) {
			// Si le cryptage ne reussi pas je deplace vers un repertoire specifique
			e.printStackTrace();
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
			Patient p = pdao.retrievePatient(fileName.toString(),getProject_id());
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
				e.printStackTrace();
			}
			break;
		case "Patient":
			PatientDAO patdao = new MySQLPatientDAO();
			try {
				patdao.newPatient(name.toString(), getProject_id());
				setPatient_id(patdao.idmax());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		case "AcqDate":
			AcquisitionDateDAO acqdao = new MySQLAcquisitionDateDAO();
			try {
				acqdao.newAcqDate(name.toString(), getProject_id(), getPatient_id());
				setAcqDate_id(acqdao.idmax());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		case "Protocol":
			ProtocolDAO protdao = new MySQLProtocolDAO();
			try {
				protdao.newProtocol(name.toString(), getProject_id(), getPatient_id(),getAcqDate_id());
				setProtocol_id(protdao.idmax());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		case "Serie":
			SerieDAO sdao = new MySQLSerieDAO();
			try {
				sdao.newSerie(name.toString(), 0, getProject_id(), getPatient_id(),getAcqDate_id(),getProtocol_id());
				setSerie_id(sdao.idmax());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		case "DicomImage":
			DicomImageDAO dicdao = new MySQLDicomImageDAO();
			try {
				dicdao.newDicomImage(name.toString(), getProject_id(), getPatient_id(),getAcqDate_id(),getProtocol_id(),getSerie_id());
				dicomImage = new DicomImage();
				dicomImage.setId(dicdao.idmax());
				dicomImage.setName(name.toString());
				dicomImage.setProjet(new Project(getProject_id()));
				dicomImage.setPatient(new Patient(getPatient_id()));
				dicomImage.setProtocole(new Protocol(getProtocol_id()));
				dicomImage.setAcquistionDate(new AcquisitionDate(getAcqDate_id()));
				dicomImage.setSerie(new Serie(getSerie_id()));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			break;
		default:
			System.err.println("Unknow table : "+table);
		}
		
		
	}


	// Deplace dicomFile à l'emplacement donné et update la date de modification
	// du repertoire patient pour la conversion nifti
	private void moveDicomTo(Path newPath) {
		try {
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
	public String getStudyDescription(){
		String prot = DicomTools.getTag(imp, "0008,1030");
		if(prot == null){
			return null;
		}
		if(prot.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(prot.charAt(0) == ' ')
			prot = prot.substring(1);	
		// on remplace les caracteres complique par "_"
		prot = prot.replaceAll("[^A-Za-z0-9]" , "_");
		return prot;
	}
	
	// Nom du patient
	public String getPatientName(){
		String pname = DicomTools.getTag(imp, "0010,0010");
		if(pname == null){
			return null;
		}
		if(pname.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(pname.charAt(0) == ' ')
			pname = pname.substring(1);			
		// on remplace les caracteres complique par "_"
		pname = pname.replaceAll("[^A-Za-z0-9]" , "_");
		return pname;
	}
	
	
	// Nom de la sequence (ex:  Series Description: PHA_IMAGES)
	public String getSeriesDescription(){
		String sdesc = DicomTools.getTag(imp, "0008,103E");
		if(sdesc == null){
			return null;
		}
		if(sdesc.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(sdesc.charAt(0) == ' ')
			sdesc = sdesc.substring(1);	
		// on remplace les caracteres complique par "_"
		sdesc = sdesc.replaceAll("[^A-Za-z0-9]" , "_");
		return sdesc;
	}
	
	// Nom du protocole d'acquisition (ex:  SWI3D TRA 1.5mm JEREMY)
	public String getProtocolName(){
		String pprot = DicomTools.getTag(imp, "0018,1030");
		if(pprot == null){
			return null;
		}
		if(pprot.isEmpty())
			return "Unknown";
		// On enleve les espace en debut de chaine
		while(pprot.charAt(0) == ' ')
			pprot = pprot.substring(1);	
		// on remplace les caracteres complique par "_"
		pprot = pprot.replaceAll("[^A-Za-z0-9]" , "_");
		return pprot;
	}
	// Date de l'acquisition ex : 20130122
	public String getAcquisitionDate(){
		String pdate = DicomTools.getTag(imp, "0008,0022");
		if(pdate == null){
			return null;
		}
		if(pdate.isEmpty())
			return "Unknown";
		while(pdate.charAt(0) == ' ')
			pdate = pdate.substring(1);	
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
		setImp(null);
		// On enleve le worker de la liste des worker et on ajoute
		// le patient à la liste des patients à convertir en nifti
		dispatcher.sendToNiftiDaemon(this);
	}
}
