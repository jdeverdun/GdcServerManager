package plugins;

/**
 * Interface de base pour les plugins de notre application.
 * @author Lain� Vincent (dev01, http://vincentlaine.developpez.com/ )
 *
 * Cette interface n'est destin�e � �tre directement impl�ment� dans un plugins, 
 * elle sert � d�finir un comportement commun � toutes les interfaces de plugins. 
 *
 */
public interface PluginsBase {

	public enum PluginCategory{BatchImageProcessing};
	/**
	 * Obtient le libell� � afficher dans les menu ou autre pour le plugins
	 * @return Le libell� sous forme de String. Ce libell� doit �tre clair et compr�hensible facilement 
	 */
	public String getLabel();
	
	/**
	 * Obtient la cat�gorie du plugins. Cette cat�gorie est celle dans laquelle le menu du plugins sera ajout� une fois charg�
	 * @return
	 */
	public PluginCategory getCategory();
	
}