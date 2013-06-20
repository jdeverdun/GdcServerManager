package dao.project;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import model.AcquisitionDate;
import model.Patient;
import model.Project;
import model.User;

public interface AcquisitionDateDAO {


    public Collection<AcquisitionDate> retrieveAll() throws SQLException;


    public AcquisitionDate retrieveAcqDate(int id) throws SQLException;
	/**
	 * Recupere une date grace a son nom et les id des projet/patient
	 * @param name
	 * @param project_id
	 * @param patient_id
	 * @return
	 * @throws SQLException
	 */
    public AcquisitionDate retrieveAcqDate(String date, int project_id, int patient_id) throws SQLException; 
      

    public int idmax() throws SQLException;
      

    public boolean newAcqDate(String date, int projet_id, int patient_id) throws SQLException;
      
    public boolean updateAcqDate(int id, String date, int projet_id, int patient_id) throws SQLException;
      
	public Set<AcquisitionDate> getAcqDateForPatient(int id) throws SQLException;
	public Set<AcquisitionDate> getAcqDateForProject(int id) throws SQLException;


	void removeAcqdate(String project, String patient, String acqdate)
			throws SQLException;


	int getAcqdateIdFor(String project, String patient, String acqdate)
			throws SQLException;
	public boolean changeProject(Patient pat, Project toproj) throws SQLException;



}
