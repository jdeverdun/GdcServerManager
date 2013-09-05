package model.daemon;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;

import settings.SystemSettings;
import settings.WindowManager;

import daemon.DicomJobDispatcher;
import daemon.DicomEncryptDaemon;
import daemon.NiftiDaemon;
import exceptions.AnonymizationException;

/**
 * Classe contenant  les informations sur une commande d'import
 * depuis un repertoire vers le serveur (via ImportFrame)
 * @author DEVERDUN Jeremy
 *
 */
public class ImportSettings {
	public static enum DicomNamingTag{PATIENTNAME,PATIENTID,ANONYMIZE};
	private String newProjectName;
	private String newPatientName;
	private DicomNamingTag namingTag;
	private File anonymizationFile;
	private LinkedHashSet<String> anonymizedName;
	private DicomJobDispatcher dispatcher; // le dispatcher associe a l'import
	private NiftiDaemon niftid; // le daemon nifti associe a l'impot
	
	
	public ImportSettings(String newProjectName,String patname, DicomNamingTag tag, DicomJobDispatcher dispatcher, NiftiDaemon niftid) throws AnonymizationException{
		anonymizationFile = null;
		this.setNewProjectName(newProjectName);
		setNamingTag(tag);
		this.setNewPatientName(patname); 
		setDispatcher(dispatcher);
		setNiftid(niftid);
	}




	/**
	 * @return the newProjectName
	 */
	public String getNewProjectName() {
		return newProjectName;
	}


	/**
	 * @param newProjectName the newProjectName to set
	 */
	public void setNewProjectName(String newProjectName) {
		this.newProjectName = newProjectName;
	}


	/**
	 * @return the newPatientName
	 */
	public String getNewPatientName() {
		return newPatientName;
	}


	/**
	 * @param newPatientName the newPatientName to set
	 */
	public void setNewPatientName(String newPatientName) {
		this.newPatientName = newPatientName;
	}


	public boolean changeProjectName(){
		return newProjectName != null;
	}
	
	public boolean changePatientName(){
		return newPatientName != null;
	}
	

	public DicomJobDispatcher getDispatcher() {
		return dispatcher;
	}


	public void setDispatcher(DicomJobDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}


	public NiftiDaemon getNiftid() {
		return niftid;
	}


	public void setNiftid(NiftiDaemon niftid) {
		this.niftid = niftid;
	}


	/**
	 * @return the namingTag
	 */
	public DicomNamingTag getNamingTag() {
		return namingTag;
	}


	/**
	 * @param namingTag the namingTag to set
	 * @throws AnonymizationException 
	 */
	public void setNamingTag(DicomNamingTag namingTag) throws AnonymizationException {
		this.namingTag = namingTag;
		if(this.namingTag == DicomNamingTag.ANONYMIZE){
			anonymizedName = new LinkedHashSet<String>();
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
			Date date = new Date();
			File fian = new File(SystemSettings.APP_DIR+File.separator+"Anonymization_"+dateFormat.format(date)+".csv");
			if(fian.exists())
				fian.delete();
			try(PrintWriter output = new PrintWriter(new FileWriter(fian,true))) 
			{
			    output.printf("## %s\t%s\r\n", "Old_name","New_name");
			    output.close();
			} 
			catch (Exception e) {
				WindowManager.mwLogger.log(Level.SEVERE,"Couldn't create Anonymization file.",e);
				throw new AnonymizationException("Couldn't create Anonymization file.");
			}
			setAnonymizationFile(fian);
		}
	}


	/**
	 * @return the anonymizationFile
	 */
	public File getAnonymizationFile() {
		return anonymizationFile;
	}


	/**
	 * @param anonymizationFile the anonymizationFile to set
	 */
	public void setAnonymizationFile(File anonymizationFile) {
		this.anonymizationFile = anonymizationFile;
	}
	
	/**
	 * Rajoute une ligne au fichier d'anonymisation si elle ne la contient pas déjà
	 * @throws AnonymizationException 
	 */
	public void addAnomynizationLine(String oldname,String newname) throws AnonymizationException{
		if(getAnonymizationFile()==null){
			throw new AnonymizationException("Anonymization file is not initialized.");
		}
		if(anonymizedName.contains(oldname))
			return;
		anonymizedName.add(oldname);
		try(PrintWriter output = new PrintWriter(new FileWriter(getAnonymizationFile(),true))) 
		{
		    output.printf("%s\t%s\r\n", oldname,newname);
		    output.close();
		} 
		catch (Exception e) {
			WindowManager.mwLogger.log(Level.SEVERE,"Couldn't add line to Anonymization file.",e);
			throw new AnonymizationException("Couldn't add line to Anonymization file.");
		}
	}
	
	
}
