package daemon.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;


import settings.SystemSettings;
import settings.WindowManager;


/**
 * Classe chargeant la liste des prenoms contenu dans un fichier
 * permettant de s'assurer que les patients rentres sur le serveur son anonymise
 * @author DEVERDUN Jeremy
 *
 */
public class firstNameDB {

	// fichier contenant l'objet hashset de la bdd
	private static final String bddRelativePath = "fnbdd.bin";
	
	// permet de savoir si on a reussi ou pas a charger la bdd
	private static boolean dbLoaded = false;
	
	// base de donnees locale contenant la liste des noms desaccentues
	private static HashSet<String> firstNameList;
	
	
	public static boolean init(){
		Path bddpath = Paths.get(SystemSettings.APP_DIR+File.separator+bddRelativePath);
		if(Files.exists(bddpath)){
			loadFNDB(bddpath);
			return true;
		}else{
			firstNameList = new HashSet<String>();
			// on demande de charger une base de donnee via un fichier texte (une ligne par prenom)
			JDialog.setDefaultLookAndFeelDecorated(true);
			JOptionPane.showMessageDialog(WindowManager.MAINWINDOW,
					"No first name DB found, please import one.",
				    "Select a first name DB",
				    JOptionPane.INFORMATION_MESSAGE);
			JFileChooser fc = new JFileChooser(SystemSettings.APP_DIR.toString());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setFileFilter(new FileFilter() {
				
				@Override
				public String getDescription() {
					return "Text files (.txt, .bdd)";
				}
				
				@Override
				public boolean accept(File f) {
					if(f.isDirectory())
						return true;
					if(f.toString().endsWith(".txt") || f.toString().endsWith(".bdd"))
						return true;
					return false;
					
				}
			});
			int retval = fc.showOpenDialog(WindowManager.MAINWINDOW);
            if (retval == JFileChooser.APPROVE_OPTION) {
            	final File file = fc.getSelectedFile();
            	WindowManager.mwLogger.log(Level.INFO, "Importing "+file.getAbsolutePath()+" as fnDB");
            	WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(true);
				try {
					if(!importDB(file.toPath())){
						JDialog.setDefaultLookAndFeelDecorated(true);
						JOptionPane.showMessageDialog(WindowManager.MAINWINDOW,
								"Couldn't import first name database.",
							    "Import BDD error",
							    JOptionPane.ERROR_MESSAGE);
					}else{
						WindowManager.mwLogger.log(Level.INFO, "Import successfull.");
						saveFNDB();
						setDbLoaded(true);
						WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
						return true;
					}
						
				} catch (IOException e) {
					WindowManager.mwLogger.log(Level.SEVERE, "couldn't import fnDB",e);
					WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
					return false;
				}
				WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
            }
            return false;
		}
	}
	
	
	private static void saveFNDB(){
		Path savePath = SystemSettings.APP_DIR;

		try{
			// Open a file to write to
			FileOutputStream saveFile=new FileOutputStream(savePath+File.separator+bddRelativePath);
	
			// Create an ObjectOutputStream to put objects into save file.
			ObjectOutputStream save = new ObjectOutputStream(saveFile);
			
			// sauvegarde des donnees
			HashSet<String> toSave = new HashSet<String>();
			for(String s:firstNameList){
				toSave.add(s);
			}
			save.writeObject(toSave);
			
			save.close();
		}catch(IOException e){
			WindowManager.mwLogger.log(Level.SEVERE, "saveFNDB error",e);
			new File(savePath+File.separator+bddRelativePath).delete();
		}
	}
	
	
	/**
	 * Charge la bdd des prenoms contenu dans fnbdd.bin
	 * @param p
	 */
	private static void loadFNDB(Path p) {
		try{
			// Open file to read from, named SavedObj.sav.
			FileInputStream saveFile = new FileInputStream(p.toString());
	
			// Create an ObjectInputStream to get objects from save file.
			ObjectInputStream save = new ObjectInputStream(saveFile);
			
			firstNameList = (HashSet<String>) save.readObject();
			save.close();
			setDbLoaded(true);
		}catch(IOException | ClassNotFoundException e){
			WindowManager.mwLogger.log(Level.SEVERE, "loadFNDB error",e);
			setDbLoaded(false);
		}
	}

	/**
	 * Charge un db depuis un fichier
	 * @param p
	 * @throws IOException 
	 */
	public static boolean importDB(Path p) throws IOException{
		if(!Files.exists(p) || Files.isDirectory(p)){
			return false;
		}
		firstNameList.clear();
		BufferedReader reader = Files.newBufferedReader(p, Charset.defaultCharset() );
		String line = null;
		while ( (line = reader.readLine()) != null ) {
			firstNameList.add(deAccent(line));
		}
		return true;
	}
	
	
	/**
	 * Remplace les caracteres accentues
	 * @param str
	 * @return
	 */
	public static String deAccent(String str) {
	    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
	
	/**
	 * Test si une chaine de caractere est presente dans la bdd
	 * @param s
	 * @return
	 */
	public static boolean contains(String s){
		return firstNameList.contains(s);
	}


	/**
	 * @return the dbLoaded
	 */
	public static boolean isDbLoaded() {
		return dbLoaded;
	}


	/**
	 * @param dbLoaded the dbLoaded to set
	 */
	public static void setDbLoaded(boolean dl) {
		dbLoaded = dl;
	}
}
