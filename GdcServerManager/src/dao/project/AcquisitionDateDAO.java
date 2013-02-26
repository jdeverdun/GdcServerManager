package dao.project;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import model.AcquisitionDate;
import model.Patient;
import model.User;

public interface AcquisitionDateDAO {


    public Collection<AcquisitionDate> retrieveAll() throws SQLException;


    public AcquisitionDate retrieveAcqDate(int id) throws SQLException;
      
      

    public int idmax() throws SQLException;
      

    public boolean newAcqDate(String name, int projet_id, int patient_id) throws SQLException;
      
    public boolean updateAcqDate(int id, String name, int projet_id, int patient_id) throws SQLException;
      
	public Set<AcquisitionDate> getAcqDateForPatient(int id) throws SQLException;
	public Set<AcquisitionDate> getAcqDateForProject(int id) throws SQLException; 

}
