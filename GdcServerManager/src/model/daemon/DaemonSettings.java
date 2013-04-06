package model.daemon;

/**
 * Modele pour les settings des differents daemon
 * @author Mobilette
 *
 */
public abstract class DaemonSettings {

	protected boolean serverMode;

	public boolean isServerMode() {
		return serverMode;
	}

	public void setServerMode(boolean serverMode) {
		this.serverMode = serverMode;
	}
	
	
}
