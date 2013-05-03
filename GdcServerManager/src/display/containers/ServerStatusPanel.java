package display.containers;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;


/**
 * Classe permettant de visualiser l'etat des Thread
 * Decrypter, Encrypter, DicomWorker, Nifti ...
 * 
 * Permet de relancer des Thread, de les stopper etc
 * @author DEVERDUN Jeremy
 *
 */
public class ServerStatusPanel extends JPanel {

	// Attributs
	private static final int DEFAULT_REFRESH_TIME = 2000;
	private int refreshTime; // temps d'attente en ms entre chaque refresh
	
	
	
	// Constructeur
	public ServerStatusPanel(){
		setRefreshTime(DEFAULT_REFRESH_TIME);
		setLayout(new MigLayout("", "[]", "[]"));
	}
	public ServerStatusPanel(int resfreshtime){
		super();
		setRefreshTime(resfreshtime);
	}
	
	
	// Accesseurs
	public int getRefreshTime() {
		return refreshTime;
	}
	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}
	
	
	// Methodes
}
