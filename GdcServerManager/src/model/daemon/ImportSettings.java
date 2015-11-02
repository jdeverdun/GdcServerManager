package model.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;

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
	private Hashtable<String, String> nameToNewName;
	private String newProjectName;
	private String newPatientName;
	private String xlsName;
	private DicomNamingTag namingTag;
	private File anonymizationFile;
	private LinkedHashSet<String> anonymizedName;
	private DicomJobDispatcher dispatcher; // le dispatcher associe a l'import
	private NiftiDaemon niftid; // le daemon nifti associe a l'impot
	
	public ImportSettings(String newProjectName,String patname,String xlsname, DicomNamingTag tag, DicomJobDispatcher dispatcher, NiftiDaemon niftid) throws AnonymizationException{
		anonymizationFile = null;
		nameToNewName = new Hashtable<String,String>();
		this.setNewProjectName(newProjectName);
		setNamingTag(tag);
		this.setNewPatientName(patname); 
		setDispatcher(dispatcher);
		setNiftid(niftid);
		setXlsName(xlsname);
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
		return newPatientName != null || xlsName != null;
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
		//if(this.namingTag == DicomNamingTag.ANONYMIZE){
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
		//}
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
	 * @return the xlsName
	 */
	public String getXlsName() {
		return xlsName;
	}




	/**
	 * @param xlsName the xlsName to set
	 */
	public void setXlsName(String xlsName) {
		this.xlsName = xlsName;
		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(this.xlsName));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			HSSFRow row;
			HSSFCell cell;

			int rows; // No of rows
			rows = sheet.getPhysicalNumberOfRows();

			int cols = 0; // No of columns
			int tmp = 0;

			// This trick ensures that we get the data properly even if it doesn't start from first few rows
			for(int i = 0; i < 10 || i < rows; i++) {
				row = sheet.getRow(i);
				if(row != null) {
					tmp = sheet.getRow(i).getPhysicalNumberOfCells();
					if(tmp > cols) cols = tmp;
				}
			}

			for(int r = 0; r < rows; r++) {
				row = sheet.getRow(r);
				if(row != null) {
					nameToNewName.put(row.getCell(0).getStringCellValue(),row.getCell(1).getStringCellValue());
				}
			}
		} catch(Exception ioe) {
			ioe.printStackTrace();
		}
		
	}




	public Hashtable<String, String> getNameToNewName() {
		return nameToNewName;
	}




	public void setNameToNewName(Hashtable<String, String> nameToNewName) {
		this.nameToNewName = nameToNewName;
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
