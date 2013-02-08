package main;

import daemon.DicomDaemon;
import daemon.NiftiDaemon;
import modeles.ServerInfo;

public class GDCSM {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// On définit les paramètres du serveur
		ServerInfo sinfo = new ServerInfo("C:\\Users\\Analyse\\Downloads\\IMAGES\\im1","C:\\Users\\Analyse\\Downloads\\IMAGES\\im2","C:\\Users\\Analyse\\Downloads\\IMAGES\\im1\\rt");
		// On lance le daemon Nifti
		NiftiDaemon ndaemon = new NiftiDaemon(sinfo);
		ndaemon.start();
		// On lance le daemon Dicom
		DicomDaemon ddaemon = new DicomDaemon(sinfo,ndaemon);
		ddaemon.start();
	}

}
