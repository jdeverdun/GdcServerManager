package daemon;

import java.util.ArrayList;
import java.util.List;

import model.ServerInfo;

import settings.SystemSettings;

public abstract class EncryptDaemon extends Thread{
	protected boolean stop;
	protected boolean waiting;// variable pour savoir si on est en etat d'attente (aucune image ne reste a encrypter ou si on travail)
	protected boolean crashed; // pour l'import permet de savoir si le daemon a crashe
	protected int maxWorkers;// nombre max d'encryptage en parallele
	protected List<Thread> workers;
	protected ServerInfo serverInfo;
	
	public EncryptDaemon(){
		stop = false;
		crashed = false;
		maxWorkers = SystemSettings.AVAILABLE_CORES;
		workers = new ArrayList<Thread>();
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}

	public int getMaxWorkers() {
		return maxWorkers;
	}

	public void setMaxWorkers(int maxWorkers) {
		this.maxWorkers = maxWorkers;
	}

	public List<Thread> getWorkers() {
		return workers;
	}

	public void setWorkers(List<Thread> workers) {
		this.workers = workers;
	}

	protected ServerInfo getServerInfo() {
		return serverInfo;
	}

	protected void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}
}
