package model;

import java.sql.Date;
import java.sql.SQLException;
import java.util.logging.Level;

import settings.WindowManager;

import dao.MySQLProjectDAO;
import dao.ProjectDAO;
import dao.project.AcquisitionDateDAO;
import dao.project.MySQLAcquisitionDateDAO;
import dao.project.MySQLPatientDAO;
import dao.project.PatientDAO;

/**
 * Classe permettant de stocker les statistiques d'un projet (apres requete sur la bdd)
 * @author Mobilette
 *
 */
public class ProjectStatistics {
	private Project project; //  projet
	private int npatients; // nombre de patients
	private int nacquisitions; // nombre d'acquisitions
	private Date firstAcquisitionDate; // date de la premiere acquisition
	private Date lastAcquisitionDate; // date de la derniere acquisition
	private int meanAge; //age moyen
	private int minAge; //age minimal
	private int maxAge; // age max
	private float stdAge; // ecart type age
	
	public ProjectStatistics(String projectname){
		project = new Project("unknown");
		project.setId(-1);
		npatients = -1;
		nacquisitions = -1;
		firstAcquisitionDate = null;
		lastAcquisitionDate = null;
		meanAge = -1;
		minAge = -1;
		maxAge = -1;
		stdAge = -1;
		ProjectDAO prdao = new MySQLProjectDAO();
		try {
			project = prdao.retrieveProject(projectname);
		} catch (SQLException e) {
			e.printStackTrace();
			WindowManager.mwLogger.log(Level.SEVERE,"Could'nt get project for statistics ["+projectname+"]",e);
			return;
		}
		PatientDAO pdao = new MySQLPatientDAO();
		try {
			npatients = pdao.getPatientsCountForProject(project.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			WindowManager.mwLogger.log(Level.SEVERE,"Could'nt get npatients for statistics ["+projectname+"]",e);
		}
		AcquisitionDateDAO acqdao = new MySQLAcquisitionDateDAO();
		try {
			nacquisitions = acqdao.getAcqDateCountForProject(project.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			WindowManager.mwLogger.log(Level.SEVERE,"Could'nt get nacquisitions for statistics ["+projectname+"]",e);
		}
		try {
			firstAcquisitionDate = acqdao.getAcqDateMinForProject(project.getId());
		} catch (SQLException e) {
			WindowManager.mwLogger.log(Level.SEVERE,"Could'nt get firstAcquisitionDate for statistics ["+projectname+"]",e);
			e.printStackTrace();
		}
		try {
			lastAcquisitionDate = acqdao.getAcqDateMaxForProject(project.getId());
		} catch (SQLException e) {
			WindowManager.mwLogger.log(Level.SEVERE,"Could'nt get lastAcquisitionDate for statistics ["+projectname+"]",e);
			e.printStackTrace();
		}
	}
	
	
	public String toString(){
		return "Project : "+project.getNom()+" / NPatients : "+npatients+" / NAcquisitions : "+nacquisitions+" / first Acquisition : "+firstAcquisitionDate+" / " +
				" Last Acquisition : "+lastAcquisitionDate;
				
	}
	public ProjectStatistics(int projectid){
		
	}

	public int getNpatients() {
		return npatients;
	}

	public void setNpatients(int npatients) {
		this.npatients = npatients;
	}

	public int getNacquisitions() {
		return nacquisitions;
	}

	public void setNacquisitions(int nacquisitions) {
		this.nacquisitions = nacquisitions;
	}

	public Date getFirstAcquisitionDate() {
		return firstAcquisitionDate;
	}

	public void setFirstAcquisitionDate(Date firstAcquisitionDate) {
		this.firstAcquisitionDate = firstAcquisitionDate;
	}

	public Date getLastAcquisitionDate() {
		return lastAcquisitionDate;
	}

	public void setLastAcquisitionDate(Date lastAcquisitionDate) {
		this.lastAcquisitionDate = lastAcquisitionDate;
	}

	public int getMeanAge() {
		return meanAge;
	}

	public void setMeanAge(int meanAge) {
		this.meanAge = meanAge;
	}

	public int getMinAge() {
		return minAge;
	}

	public void setMinAge(int minAge) {
		this.minAge = minAge;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public float getStdAge() {
		return stdAge;
	}

	public void setStdAge(float stdAge) {
		this.stdAge = stdAge;
	}

}
