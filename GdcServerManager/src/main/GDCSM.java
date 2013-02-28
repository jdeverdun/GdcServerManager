package main;

import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import settings.SQLSettings;

import daemon.DicomDaemon;
import daemon.NiftiDaemon;
import display.Authentificator;
import model.ServerInfo;

public class GDCSM {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// On définit les paramètres du serveur
		ServerInfo sinfo = new ServerInfo("C:\\Users\\serge\\Documents\\GdcServer_simu\\essai1\\buffer","C:\\Users\\serge\\Documents\\GdcServer_simu\\essai1\\saveDicom","C:\\Users\\serge\\Documents\\GdcServer_simu\\essai1\\saveNifti");
		// On lance le daemon Nifti
		NiftiDaemon ndaemon = new NiftiDaemon(sinfo);
		ndaemon.start();
		// On lance le daemon Dicom
		DicomDaemon ddaemon = new DicomDaemon(sinfo,ndaemon);
		ddaemon.start();
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				JFrame.setDefaultLookAndFeelDecorated(true);
				try {
			          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
			        } catch (Exception e) {
			          System.out.println("Substance Graphite failed to initialize");
			        }
				Authentificator auth = new Authentificator();
				UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
				auth.setVisible(true);
				
			}
		});
	}

}
