package main;

import daemon.DicomDaemon;
import daemon.NiftiDaemon;
import model.ServerInfo;

public class GDCSM {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// On d�finit les param�tres du serveur
		ServerInfo sinfo = new ServerInfo("C:\\Users\\Mobilette\\Desktop\\ftl\\buffer","C:\\Users\\Mobilette\\Desktop\\ftl\\saveDicom","C:\\Users\\Mobilette\\Desktop\\ftl\\saveNifti");
		// On lance le daemon Nifti
		NiftiDaemon ndaemon = new NiftiDaemon(sinfo);
		ndaemon.start();
		// On lance le daemon Dicom
		DicomDaemon ddaemon = new DicomDaemon(sinfo,ndaemon);
		ddaemon.start();
	}

}
