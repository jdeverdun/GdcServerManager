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
 * Classe g�rant le chargement et la validation des plugins
 * @author Lain� Vincent (dev01, http://vincentlaine.developpez.com/ )
 *
 */
public class PluginsLoader {

	private File[] files;
	
	private ArrayList classFolderProcessingPlugins;
	
	/**
	 * Constructeur par d�faut
	 *
	 */
	public PluginsLoader(){
		this.classFolderProcessingPlugins = new ArrayList<>();
		updateFiles();
	}

	
	/**
	 * D�fini l'ensemble des fichiers � charger
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
			
			//On cr�er une nouvelle instance de l'objet contenu dans la liste gr�ce � newInstance() 
			//et on le cast en StringPlugins. Vu que la classe impl�mente StringPlugins, le cast est toujours correct
			tmpPlugins[index] = (FolderProcessingPlugins)((Class)this.classFolderProcessingPlugins.get(index)).newInstance() ;
			
		}
		
		return tmpPlugins;
	}
	
	
	
	private void initializeLoader() throws Exception{
		//On v�rifie que la liste des plugins � charger � �t� initialis�
		if(this.files == null || this.files.length == 0 ){
			throw new Exception("Pas de fichier sp�cifi�");
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
		//Pour d�termin� quels sont les interfaces impl�ment�es
		Class tmpClass = null;
		
		for(int index = 0 ; index < f.length ; index ++ ){
			
			f[index] = this.files[index];
			
			if( !f[index].exists() ) {
				break;
			}
			
			URL u = f[index].toURL();
			//On cr�er un nouveau URLClassLoader pour charger le jar qui se trouve ne dehors du CLASSPATH
			loader = new URLClassLoader(new URL[] {u}); 
			
			//On charge le jar en m�moire
			JarFile jar = new JarFile(f[index].getAbsolutePath());
			
			//On r�cup�re le contenu du jar
			enumeration = jar.entries();
			
			while(enumeration.hasMoreElements()){
				
				tmp = enumeration.nextElement().toString();

				//On v�rifie que le fichier courant est un .class (et pas un fichier d'informations du jar )
				if(tmp.length() > 6 && tmp.substring(tmp.length()-6).compareTo(".class") == 0) {
					
					tmp = tmp.substring(0,tmp.length()-6);
					tmp = tmp.replaceAll("/",".");
					
					tmpClass = Class.forName(tmp ,true,loader);
					
					for(int i = 0 ; i < tmpClass.getInterfaces().length; i ++ ){
						
						//Une classe ne doit pas appartenir � deux cat�gories de plugins diff�rents. 
						//Si tel est le cas on ne la place que dans la cat�gorie de la premi�re interface correct
						// trouv�e
						if(tmpClass.getInterfaces()[i].getName().toString().equals("plugins.FolderProcessingPlugins") ) {
							this.classFolderProcessingPlugins.add(tmpClass);
						}
					}
					
				}
			}
			
		
		}
		
	}
	
	
}
