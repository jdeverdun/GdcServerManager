package main;

import gsm.update.Updater;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.WindowManager;

import daemon.DicomDaemon;
import daemon.NiftiDaemon;
import display.Authentificator;
import display.ImportFrame;
import display.containers.viewer.ViewerPanel;
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
		init();			
		
		
		// ------------ Fin de verif des mises a jours -------
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				JFrame.setDefaultLookAndFeelDecorated(true);
				try {
			          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
			        } catch (Exception e) {
			          System.out.println("Substance Graphite failed to initialize");
			        }
				
				// ------------ Verification des mises a jours -------
				
				UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
				Updater updater = new Updater();
				ArrayList<String> versions = updater.getVersions();
				if(!versions.isEmpty() && !versions.get(versions.size()-1).equals(WindowManager.VERSION)){
					Image img = new ImageIcon(this.getClass().getResource("/images/logo32.png")).getImage();
					
				    JFrame f = new JFrame(""); // juste pour afficher l'icone du l'optionPane
				    f.setIconImage(img);
					JDialog.setDefaultLookAndFeelDecorated(true);
					String[] choices = new String[]{"Update","Cancel"};
					int versionChoisie = JOptionPane.showOptionDialog(f,"A new update is available." ,"Information", 
				            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
				            null, choices, choices[0]);
	
					//S'il veut la télécharger
					if(versionChoisie == 0){
						String jarfile = new File(GDCSM.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
						ProcessBuilder pb = new ProcessBuilder("java", "-jar", SystemSettings.APP_DIR+File.separator+WindowManager.UPDATER_JARFILE,versions.get(versions.size()-1),jarfile);
						pb.directory(SystemSettings.APP_DIR.toFile());
						try {
							Process p = pb.start();
							f.dispose();
							System.exit(0);
						} catch (final IOException e) {
							e.printStackTrace();
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									JDialog.setDefaultLookAndFeelDecorated(true);
									JOptionPane.showMessageDialog(null,
										    "Error during update ("+e.toString()+")",
										    "Error",
										    JOptionPane.ERROR_MESSAGE);
								}
							});
						}
					}else{
						f.dispose();
					}
				}
				
				// ------------ Fin de verif des mises a jours -------
				
				Authentificator auth = new Authentificator();
				
				auth.setVisible(true);
				
			}
		});
	}

	/**
	 * nettoi les repertoires au demarrage
	 */
	private static void init() {
		for(File fi:SystemSettings.SERVER_INFO.getTempDir().toFile().listFiles()){
			fi.delete();
		}
	}

}
