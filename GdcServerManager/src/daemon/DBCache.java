package daemon;

import java.util.HashMap;

/**
 * Permet de stocker en cache un certain nombre d'entree de la bdd
 * pour eviter un nombre de requetes trop eleve
 * @author DEVERDUN Jeremy
 *
 */
public class DBCache {
	private HashMap<String, String> rkeyList; // stock les rkey utilisé pendant la session 
	private HashMap<String, Integer> idProjectList; // stock les id des projet identifie par leur nom
	private HashMap<String, Integer> idPatientList; // stock les id des patient identife par leur nom et id projet (nom@@idprojet)
	private HashMap<String, Integer> idAcqDateList; // stock les id des acqdate identife par nom@@idpatient@@idprojet
	private HashMap<String, Integer> idProtocolList; // stock les id des protocol identife par nom@@idacqdate@@idpatient@@idprojet
	private HashMap<String, Integer> idSerieList; // stock les id des series identife par nom@@idprotocol@@idacqdate@@idpatient@@idprojet
	private HashMap<Integer, String> idToProjectNameList; // stock les nom des projet identifie par leur id
	private HashMap<Integer, String> idToPatientNameList; // stock les nom des patient identifie par leur id
	private HashMap<Integer, String> idToAcqDateNameList; // stock les nom des acqdate identifie par leur id
	private HashMap<Integer, String> idToProtocolNameList; // stock les nom des protocol identifie par leur id
	private HashMap<Integer, String> idToSerieNameList; // stock les nom des series identifie par leur id
	
	public DBCache(){
		rkeyList = new HashMap<String, String>();
		idProjectList = new HashMap<String, Integer>();
		idPatientList = new HashMap<String, Integer>();
		idAcqDateList = new HashMap<String, Integer>();
		idProtocolList = new HashMap<String, Integer>();
		idSerieList = new HashMap<String, Integer>();
		idToProjectNameList = new HashMap<Integer,String>();
		idToPatientNameList = new HashMap<Integer,String>();
		idToAcqDateNameList = new HashMap<Integer,String>();
		idToProtocolNameList = new HashMap<Integer,String>();
		idToSerieNameList = new HashMap<Integer,String>();
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

	public HashMap<Integer, String> getIdToProjectNameList() {
		if(idToProjectNameList.size()>500)
			idToProjectNameList.clear();
		return idToProjectNameList;
	}

	public void setIdToProjectNameList(HashMap<Integer, String> idToProjectNameList) {
		this.idToProjectNameList = idToProjectNameList;
	}

	public HashMap<Integer, String> getIdToPatientNameList() {
		if(idToPatientNameList.size()>500)
			idToPatientNameList.clear();
		return idToPatientNameList;
	}

	public void setIdToPatientNameList(HashMap<Integer, String> idToPatientNameList) {
		this.idToPatientNameList = idToPatientNameList;
	}

	public HashMap<Integer, String> getIdToAcqDateNameList() {
		if(idToAcqDateNameList.size()>500)
			idToAcqDateNameList.clear();
		return idToAcqDateNameList;
	}

	public void setIdToAcqDateNameList(HashMap<Integer, String> idToAcqDateNameList) {
		this.idToAcqDateNameList = idToAcqDateNameList;
	}

	public HashMap<Integer, String> getIdToProtocolNameList() {
		if(idToProtocolNameList.size()>500)
			idToProtocolNameList.clear();
		return idToProtocolNameList;
	}

	public void setIdToProtocolNameList(
			HashMap<Integer, String> idToProtocolNameList) {
		this.idToProtocolNameList = idToProtocolNameList;
	}

	public HashMap<Integer, String> getIdToSerieNameList() {
		if(idToSerieNameList.size()>500)
			idToSerieNameList.clear();
		return idToSerieNameList;
	}

	public void setIdToSerieNameList(HashMap<Integer, String> idToSerieNameList) {
		this.idToSerieNameList = idToSerieNameList;
	}

	public void clear() {
		rkeyList.clear();
		idProjectList.clear();
		idPatientList.clear();
		idAcqDateList.clear();
		idProtocolList.clear();
		idSerieList.clear();
		idToProjectNameList.clear();
		idToPatientNameList.clear();
		idToAcqDateNameList.clear();
		idToProtocolNameList.clear();
		idToSerieNameList.clear();
	}
}
