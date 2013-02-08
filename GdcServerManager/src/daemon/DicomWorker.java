package daemon;

import ij.ImagePlus;
import ij.util.DicomTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import modeles.ServerInfo;
import static java.nio.file.StandardCopyOption.*;


public class DicomWorker extends Thread {

	// Attributs
	private Path dicomFile;
	private Path patientFolder;
	private Path serieFolder;
	private ServerInfo serverInfo;
	private DicomJobDispatcher dispatcher;
	private ImagePlus imp;
	
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


	public Path getPatientFolder() {
		return patientFolder;
	}


	public void setPatientFolder(Path patientFolder) {
		this.patientFolder = patientFolder;
	}


	public Path getSerieFolder() {
		return serieFolder;
	}


	public void setSerieFolder(Path serieFolder) {
		this.serieFolder = serieFolder;
	}


	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(ServerInfo sinfo) {
		this.serverInfo = sinfo;
	}

	public DicomJobDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(DicomJobDispatcher parentDaemon) {
		this.dispatcher = parentDaemon;
	}
	
	
	
	// Methodes

	public void run(){
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
		checkAndMakeDir(studyFolder);
		checkAndMakeDir(patientFolder);
		checkAndMakeDir(dateFolder);
		checkAndMakeDir(protocolFolder);
		checkAndMakeDir(serieFolder);
		
		Path newPath = Paths.get(serieFolder + File.separator + dicomFile.getFileName());
		
		moveDicomTo(newPath);

		prepareToStop();
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


	// test si les repertoires existent (patient / protocoles etc) et on les créé au besoin
	private void checkAndMakeDir(Path dir) {
		if(!Files.exists(dir)){
			// Si ce n'est pas le cas on le créé
			try {
				Files.createDirectory(dir);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
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
	public void prepareToStop(){
		// On libere de la memoire
		setImp(null);
		// On enleve le worker de la liste des worker et on ajoute
		// le patient à la liste des patients à convertir en nifti
		dispatcher.removeWorker(this);
	}

}
