package settings.sql;

import settings.sql.tables.AcquisitionDateTable;
import settings.sql.tables.DicomImageTable;
import settings.sql.tables.NiftiImageTable;
import settings.sql.tables.PatientTable;
import settings.sql.tables.ProjectTable;
import settings.sql.tables.ProtocolTable;
import settings.sql.tables.SerieTable;
import settings.sql.tables.UserProjectTable;
import settings.sql.tables.UserTable;
import settings.sql.tables.UserViewTable;
import settings.sql.tables.JobTable;


/**
 * Classe stockant les noms des tables sous forme de fonction
 * permettant d'assurer plus facilement la gestion d'une modification
 * de ces tables (detecter plus facilement les zones du code a changer lors
 * d'un changement de la structure)
 * @author DEVERDUN Jeremy
 *
 */
public class DBTables {

	// les tables de la base
	private UserTable user;
	private UserViewTable user_view;
	private UserProjectTable user_project;
	private ProjectTable project;
	private PatientTable patient;
	private AcquisitionDateTable acquisitionDate;
	private ProtocolTable protocol;
	private SerieTable serie;
	private DicomImageTable dicomImage;
	private NiftiImageTable niftiImage;
	private JobTable job;
	
	// Constructeur
	public DBTables(){
		user = new UserTable();
		user_view = new UserViewTable();
		user_project = new UserProjectTable();
		project = new ProjectTable();
		patient = new PatientTable();
		acquisitionDate = new AcquisitionDateTable();
		protocol = new ProtocolTable();
		serie = new SerieTable();
		dicomImage = new DicomImageTable();
		niftiImage = new NiftiImageTable();
		job = new JobTable();
		
	}


	// Accesseur
	public UserTable getUser() {
		return user;
	}


	public UserViewTable getUser_view() {
		return user_view;
	}


	public UserProjectTable getUser_project() {
		return user_project;
	}


	public ProjectTable getProject() {
		return project;
	}


	public PatientTable getPatient() {
		return patient;
	}


	public AcquisitionDateTable getAcquisitionDate() {
		return acquisitionDate;
	}


	public ProtocolTable getProtocol() {
		return protocol;
	}


	public SerieTable getSerie() {
		return serie;
	}


	public DicomImageTable getDicomImage() {
		return dicomImage;
	}


	public NiftiImageTable getNiftiImage() {
		return niftiImage;
	}
	
	public JobTable getJob() {
		return job;
	}

}
