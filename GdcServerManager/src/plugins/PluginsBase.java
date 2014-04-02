package plugins;

/**
 * Interface de base pour les plugins de notre application.
 * @author Lainé Vincent (dev01, http://vincentlaine.developpez.com/ )
 *
 * Cette interface n'est destinée à être directement implémenté dans un plugins, 
 * elle sert à définir un comportement commun à toutes les interfaces de plugins. 
 *
 */
public interface PluginsBase {

	public enum PluginCategory{BatchImageProcessing};
	/**
	 * Obtient le libellé à afficher dans les menu ou autre pour le plugins
	 * @return Le libellé sous forme de String. Ce libellé doit être clair et compréhensible facilement 
	 */
	public String getLabel();
	
	/**
	 * Obtient la catégorie du plugins. Cette catégorie est celle dans laquelle le menu du plugins sera ajouté une fois chargé
	 * @return
	 */
	public PluginCategory getCategory();
	
}