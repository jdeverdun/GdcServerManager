package daemon;

import java.util.HashMap;

/**
 * Permet de stocker en cache un certain nombre d'entree de la bdd
 * pour eviter un nombre de requetes trop eleve
 * @author DEVERDUN Jeremy
 *
 */
public class DBCache {
	private HashMap<String, String> rkeyList; // stock les rkey utilis� pendant la session 
	private HashMap<String, Integer> idProjectList; // stock les id des projet identifie par leur nom
	private HashMap<String, Integer> idPatientList; // stock les id des patient identife par leur nom et id projet (nom@@idprojet)
	private HashMap<String, Integer> idAcqDateList; // stock les id des acqdate identife par nom@@idpatient@@idprojet
	private HashMap<String, Integer> idProtocolList; // stock les id des protocol identife par nom@@idacqdate@@idpatient@@idprojet
	private HashMap<String, Integer> idSerieList; // stock les id des series identife par nom@@idprotocol@@idacqdate@@idpatient@@idprojet
	
	public DBCache(){
		rkeyList = new HashMap<String, String>();
		idProjectList = new HashMap<String, Integer>();
		idPatientList = new HashMap<String, Integer>();
		idAcqDateList = new HashMap<String, Integer>();
		idProtocolList = new HashMap<String, Integer>();
		idSerieList = new HashMap<String, Integer>();
	}
	
	public HashMap<String, String> getRkeyList() {
		if(rkeyList.size()>500)
			rkeyList.clear();
		return rkeyList;
	}
	public void setRkeyList(HashMap<String, String> rkeyList) {
		this.rkeyList = rkeyList;
	}
	public HashMap<String, Integer> getIdProjectList() {
		if(idProjectList.size()>500)
			idProjectList.clear();
		return idProjectList;
	}
	public void setIdProjectList(HashMap<String, Integer> idProjectList) {
		this.idProjectList = idProjectList;
	}
	public HashMap<String, Integer> getIdPatientList() {
		if(idPatientList.size()>500)
			idPatientList.clear();
		return idPatientList;
	}
	public void setIdPatientList(HashMap<String, Integer> idPatientList) {
		this.idPatientList = idPatientList;
	}
	public HashMap<String, Integer> getIdAcqDateList() {
		if(idAcqDateList.size()>500)
			idAcqDateList.clear();
		return idAcqDateList;
	}
	public void setIdAcqDateList(HashMap<String, Integer> idAcqDateList) {
		this.idAcqDateList = idAcqDateList;
	}
	public HashMap<String, Integer> getIdProtocolList() {
		if(idProtocolList.size()>500)
			idProtocolList.clear();
		return idProtocolList;
	}
	public void setIdProtocolList(HashMap<String, Integer> idProtocolList) {
		this.idProtocolList = idProtocolList;
	}
	public HashMap<String, Integer> getIdSerieList() {
		if(idSerieList.size()>500)
			idSerieList.clear();
		return idSerieList;
	}
	public void setIdSerieList(HashMap<String, Integer> idSerieList) {
		this.idSerieList = idSerieList;
	}
}
