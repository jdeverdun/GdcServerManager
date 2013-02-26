package daemon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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

public class NiftiWorker extends DaemonWorker {

	private Path path;
	private Path niftiPath;
	private NiftiDaemon niftiDaemon;
	
	
	public NiftiWorker(NiftiDaemon nDaemon, Path filename) {
		// TODO Auto-generated constructor stub
		setNiftiDaemon(nDaemon);
		setPath(filename);
		setServerInfo(getNiftiDaemon().getServerInfo());
	}
	public Path getPath() {
		return path;
	}
	public void setPath(Path path) {
		this.path = path;
	}
	public NiftiDaemon getNiftiDaemon() {
		return niftiDaemon;
	}
	public void setNiftiDaemon(NiftiDaemon nDaemon) {
		this.niftiDaemon = nDaemon;
	}
	public Path getNiftiPath() {
		return niftiPath;
	}
	public void setNiftiPath(Path niftiPath) {
		this.niftiPath = niftiPath;
	}
	@Override
	public void start() {
		// On recherche l'arborescence et on créé les répertoire si besoin +
		// NIFTIDIR / NOM_ETUDE / NOM_PATIENT / DATE_IRM / PROTOCOL / SERIE 
		Path studyName = path.getParent().getParent().getParent().getParent().getFileName();
		Path patientName = path.getParent().getParent().getParent().getFileName();
		Path acqDate = path.getParent().getParent().getFileName();
		Path protocolAcqName = path.getParent().getFileName() ;
		Path serieName = path.getFileName();
		
		Path studyDir = Paths.get(serverInfo.getNiftiDir().toString() + File.separator + studyName);
		Path patientDir = Paths.get(studyDir + File.separator +  patientName);
		Path acqDateDir = Paths.get(patientDir + File.separator +  acqDate);
		Path protocolDir = Paths.get(acqDateDir + File.separator +  protocolAcqName);
		Path serieDir = Paths.get(protocolDir + File.separator +  serieName);
		
		checkAndMakeDir(studyDir);
		checkAndMakeDir(patientDir);
		checkAndMakeDir(acqDateDir);
		checkAndMakeDir(protocolDir);
		checkAndMakeDir(serieDir);
		
		Path niftiPath = serieDir;
		System.out.println("Nifti convert : "+path);
		String command = buildConvertCommandFor(path);
		Process process;
		try {
			//process = Runtime.getRuntime().exec("mcverter.exe "+ path +" -o "+ niftiPath.toString() + " -f fsl -x -r");//-x 
			// On recupere la liste des nifti qui existait avant la conversion
			// sous la forme "nom_datamodif"
			HashMap<String,Path> niftiBefore = getNiftiListIn(niftiPath);
			process = Runtime.getRuntime().exec(command);
			process.waitFor();
			// On recupere les nom des fichiers nifti cree
			// et on ajoute les infos à la database
			ArrayList<Path> niftis = extractNewNiftiFrom(getNiftiListIn(niftiPath),niftiBefore);
			for(Path currNifti:niftis)
				addEntryToDB(currNifti,"NiftiImage");
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	// Construit la commande pour convertir un repertoire dicom (dicomPath) en nifti
	private String buildConvertCommandFor(Path dicomPath) {
		// -i id in filename | -p protocol in filename
		String command = "dcm2nii.exe -i y -p y -e n -a n -d n -e n -f n -l 0  ";
		switch(getNiftiDaemon().getFormat()){
		case NiftiDaemon.ANALYZE_7_5:
			command+=" -n n -s y -g n ";break;
		case NiftiDaemon.SPM5_NIFTI:
			command+=" -n n -g n ";break;
		case NiftiDaemon.NIFTI_4D://A selectionner en prio ?
			command+=" -n y -g n ";break;
		case NiftiDaemon.FSL_NIFTI:
			command+=" -n y -g y ";break;
		default:
			System.err.println("Unknow nifti format");
		}
		command+=" -o "+this.niftiPath+" "+dicomPath;
		return command;
	}
	
	@Override
	protected void addEntryToDB(Path name, String table) {
		switch(table){
		case "NiftiImage":
			DicomImageDAO dicdao = new MySQLDicomImageDAO();
			try {
				dicdao.newDicomImage(name.toString(), getProject_id(), getPatient_id(),getAcqDate_id(),getProtocol_id(),getSerie_id());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		default:
			System.err.println("I don't know tale : "+table+" ... sorry");	
		}
	}

	// renvoi une arrayList avec les nifti nouvellement cree
	// compare le contenu de 2 hashmap issuent 
	// de getNiftiListIn
	private ArrayList<Path> extractNewNiftiFrom(HashMap<String, Path> niftiAfter,
			HashMap<String, Path> niftiBefore) {
		ArrayList<Path> niftis = new ArrayList<Path>();
		boolean oldIsEmpty = niftiBefore.isEmpty();
		for(String name:niftiAfter.keySet()){
			if(oldIsEmpty || !niftiBefore.containsKey(name))
				niftis.add(niftiAfter.get(name));
		}
		return niftis;
	}

	// recupere la liste des nifti qui existe dans le repertoire "niftiPath"
	// sous la forme couple Key/Value "nom_datamodif"/Path
	private HashMap<String, Path> getNiftiListIn(Path niftiPath) {
		HashMap<String, Path> niftiList = new HashMap<String, Path>();
		String[] list = niftiPath.toFile().list();
		String ext;
		switch(getNiftiDaemon().getFormat()){
		case NiftiDaemon.NIFTI_4D:
			ext = ".nii";break;
		case NiftiDaemon.FSL_NIFTI:
			ext = ".nii.gz";break;
		default:
			ext = ".img";
		}
		for(String name:list){
			String fullpath = niftiPath + "/" + name;
			if(name.endsWith(ext))
				niftiList.put(name+"_"+(new File(fullpath).lastModified()),Paths.get(fullpath));
		}
		return niftiList;
	}

}
