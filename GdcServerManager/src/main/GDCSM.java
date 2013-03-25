package main;

import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import settings.SQLSettings;
import settings.SystemSettings;

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
		SystemSettings.SERVER_INFO = new ServerInfo("C:\\Users\\Analyse\\Downloads\\test\\buffer","C:\\Users\\Analyse\\Downloads\\test\\saveDicom","C:\\Users\\Analyse\\Downloads\\test\\saveNifti",
				"C:\\Users\\Analyse\\Downloads\\test\\temp");
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
