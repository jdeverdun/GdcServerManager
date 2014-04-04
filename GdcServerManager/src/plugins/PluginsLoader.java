package plugins;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarFile;

import settings.SystemSettings;

/**
 * Classe gérant le chargement et la validation des plugins
 * @author Lainé Vincent (dev01, http://vincentlaine.developpez.com/ )
 *
 */
public class PluginsLoader {

	private File[] files;
	
	private ArrayList classFolderProcessingPlugins;
	
	/**
	 * Constructeur par défaut
	 *
	 */
	public PluginsLoader(){
		this.classFolderProcessingPlugins = new ArrayList<>();
		updateFiles();
	}

	
	/**
	 * Défini l'ensemble des fichiers à charger
	 * @param files
	 */
	public void updateFiles(){
		files = new File(SystemSettings.APP_DIR + File.separator + SystemSettings.RELATIVE_PLUGINS_DIR).listFiles(new FilenameFilter() {
		    public boolean accept(File directory, String fileName) {
		        return fileName.endsWith(".jar");
		    }
		});
	}
	
	/**
	 * Fonction de chargement de tout les plugins de type StringPlugins
	 * @return Une collection de StringPlugins contenant les instances des plugins
	 * @throws Exception si file = null ou file.length = 0
	 */
	public FolderProcessingPlugins[] loadAllFolderProcessingPlugins() throws Exception {
		
		this.initializeLoader();
		
		FolderProcessingPlugins[] tmpPlugins = new FolderProcessingPlugins[this.classFolderProcessingPlugins.size()];
		
		for(int index = 0 ; index < tmpPlugins.length; index ++ ){
			
			//On créer une nouvelle instance de l'objet contenu dans la liste grâce à newInstance() 
			//et on le cast en StringPlugins. Vu que la classe implémente StringPlugins, le cast est toujours correct
			tmpPlugins[index] = (FolderProcessingPlugins)((Class)this.classFolderProcessingPlugins.get(index)).newInstance() ;
			
		}
		
		return tmpPlugins;
	}
	
	
	
	private void initializeLoader() throws Exception{
		//On vérifie que la liste des plugins à charger à été initialisé
		if(this.files == null || this.files.length == 0 ){
			throw new Exception("Pas de fichier spécifié");
		}

		//Pour eviter le double chargement des plugins
		if(this.classFolderProcessingPlugins.size() != 0 ){
			return ;
		}
		
		File[] f = new File[this.files.length];
//		Pour charger le .jar en memoire
		URLClassLoader loader;
		//Pour la comparaison de chaines
		String tmp = "";
		//Pour le contenu de l'archive jar
		Enumeration enumeration;
		//Pour déterminé quels sont les interfaces implémentées
		Class tmpClass = null;
		
		for(int index = 0 ; index < f.length ; index ++ ){
			
			f[index] = this.files[index];
			
			if( !f[index].exists() ) {
				break;
			}
			
			URL u = f[index].toURL();
			//On créer un nouveau URLClassLoader pour charger le jar qui se trouve ne dehors du CLASSPATH
			loader = new URLClassLoader(new URL[] {u}); 
			
			//On charge le jar en mémoire
			JarFile jar = new JarFile(f[index].getAbsolutePath());
			
			//On récupére le contenu du jar
			enumeration = jar.entries();
			
			while(enumeration.hasMoreElements()){
				
				tmp = enumeration.nextElement().toString();

				//On vérifie que le fichier courant est un .class (et pas un fichier d'informations du jar )
				if(tmp.length() > 6 && tmp.substring(tmp.length()-6).compareTo(".class") == 0) {
					
					tmp = tmp.substring(0,tmp.length()-6);
					tmp = tmp.replaceAll("/",".");
					
					tmpClass = Class.forName(tmp ,true,loader);
					
					for(int i = 0 ; i < tmpClass.getInterfaces().length; i ++ ){
						
						//Une classe ne doit pas appartenir à deux catégories de plugins différents. 
						//Si tel est le cas on ne la place que dans la catégorie de la première interface correct
						// trouvée
						if(tmpClass.getInterfaces()[i].getName().toString().equals("plugins.FolderProcessingPlugins") ) {
							this.classFolderProcessingPlugins.add(tmpClass);
						}
					}
					
				}
			}
			
		
		}
		
	}
	
	
}
