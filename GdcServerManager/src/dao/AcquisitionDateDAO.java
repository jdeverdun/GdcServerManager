package dao;

import java.util.Set;

import model.AcquisitionDate;

public interface AcquisitionDateDAO {

	Set<AcquisitionDate> getAcqDateForPatient(int id);

}
