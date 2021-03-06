package daemon;

import ij.IJ;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;

import settings.WindowManager;

import model.DicomImage;
import model.daemon.CustomConversionSettings;


/**
 * Classe utilisee lors de la conversion en mode offline 
 * de fichiers dicoms en nifti
 * @author Mobilette
 *
 */
public class NiftiWorkerClient extends NiftiWorker {

	
	public NiftiWorkerClient(NiftiDaemon nDaemon, Path filename,DicomImage dimage) {
		super(nDaemon,filename,dimage);
	}
	
	@Override
	public void start() {
		// On recherche l'arborescence et on cr�� les r�pertoire si besoin +
		// NIFTIDIR / NOM_ETUDE / NOM_PATIENT / DATE_IRM / PROTOCOL / SERIE 
		
		CustomConversionSettings csettings = getNiftiDaemon().getSettings();
		
		// ---- les entrees (dicom) -----
		Path serieName = path.getFileName();
		Path serieDicomPath = path;
		Path protocolDicomPath;
		Path acqdateDicomPath;
		Path patientDicomPath;
		Path studyDicomPath;
		Path protocolAcqName;
		Path acqDate;
		Path studyName;
		
		if(csettings.isWorkingWithProtocolDir()){ // on verifie si on veut travailler avec les protocoles
			protocolAcqName = serieDicomPath.getParent().getFileName() ;
			protocolDicomPath = serieDicomPath.getParent();
		}else{
			protocolAcqName = serieName;
			protocolDicomPath = serieDicomPath;
		}
		
		if(csettings.isWorkingWithAcqDateDir()){
			acqDate = protocolDicomPath.getParent().getFileName();
			acqdateDicomPath = protocolDicomPath.getParent();
		}else{
			acqDate = protocolAcqName;
			acqdateDicomPath = protocolDicomPath;
		}
		Path patientName = acqdateDicomPath.getParent().getFileName(); // ce repertoire est obligatoire! ;)
		patientDicomPath = acqdateDicomPath.getParent();
		if(csettings.isWorkingWithProjectDir()){
			studyName = patientDicomPath.getParent().getFileName();
			studyDicomPath = patientDicomPath.getParent();
		}else{
			studyName = patientName;
			studyDicomPath = patientDicomPath;
		}
		setProjectFolder(studyName);

		
		// ----- les sorties (nifti) -----
		Path studyDir;
		if(IJ.isWindows()){
			if(csettings.isWorkingWithProjectDir())
				studyDir = Paths.get(serverInfo.getNiftiDir().toString() + File.separator + studyName);
			else
				studyDir = serverInfo.getNiftiDir();
			
			Path patientDir = Paths.get(studyDir + File.separator +  patientName);
			Path acqDateDir;
			if(csettings.isWorkingWithAcqDateDir())
				acqDateDir = Paths.get(patientDir + File.separator +  acqDate);
			else
				acqDateDir = patientDir;
			
			Path protocolDir;
			if(csettings.isWorkingWithProtocolDir())
				protocolDir = Paths.get(acqDateDir + File.separator +  protocolAcqName);
			else
				protocolDir = acqDateDir;
			
			Path serieDir = Paths.get(protocolDir + File.separator +  serieName);
			
			
			// --- On verifie que les repertoires sont crees ---//
			checkAndMakeDir(studyDir);
			checkAndMakeDir(patientDir);
			checkAndMakeDir(acqDateDir);
			checkAndMakeDir(protocolDir);
			checkAndMakeDir(serieDir);
		
		
			niftiPath = serieDir;
		}else{
			niftiPath = serverInfo.getNiftiDir();
		}
		WindowManager.mwLogger.log(Level.INFO,"Nifti convert : "+path);

		Process process;
		try {
			// sous windows on controle l'arborescence des nifti
			if(IJ.isWindows()){
				//process = Runtime.getRuntime().exec("mcverter.exe "+ path +" -o "+ niftiPath.toString() + " -f fsl -x -r");//-x 
				// On recupere la liste des nifti qui existait avant la conversion
				// sous la forme "nom_datamodif"
				HashMap<String,Path> niftiBefore = getNiftiListIn(niftiPath);
				// on les efface (car dcm2nii n'overwrite pas !)
				removeFiles(niftiBefore);
			}
			// ------------------------------------------------------------------- //
			// on decrypte les fichiers dicom temporairement (pour la conversion)  //
			// que l'on place dans le repertoire temporaire (tempDir)              //
			// ------------------------------------------------------------------- //
			String command = "";

			command = buildConvertizerConvertCommandFor(path,niftiPath,csettings.isNifti4D());
			
			// on convertie
			process = Runtime.getRuntime().exec(command);
			if(csettings.isNiftiDebugMode()){
				InputStream stdin = process.getInputStream();
	            InputStreamReader isr = new InputStreamReader(stdin);
	            BufferedReader br = new BufferedReader(isr);
	            String line = null;
	            WindowManager.mwLogger.log(Level.FINE, "Converting "+path);
	            WindowManager.mwLogger.log(Level.FINE, "<OUTPUT>");
	            while ( (line = br.readLine()) != null)
	            	WindowManager.mwLogger.log(Level.FINE, line);
	            WindowManager.mwLogger.log(Level.FINE, "</OUTPUT>");
			}
			process.waitFor();
			
			
		} catch (IOException e1) {
			WindowManager.mwLogger.log(Level.WARNING, "NiftiWorkerClient error",e1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 

	}

}
