package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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
		// on identifie le repertoire du programme
		try {
			SystemSettings.APP_DIR = Paths.get(new File(".").getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// On définit les paramètres du serveur
		SystemSettings.SERVER_INFO = new ServerInfo(SystemSettings.APP_DIR);
		
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
