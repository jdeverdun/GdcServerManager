package daemon;

import java.util.ArrayList;
import java.util.List;

import model.ServerInfo;

import settings.SystemSettings;

public abstract class EncryptDaemon extends Thread{
	protected boolean stop;
	protected boolean waiting;// variable pour savoir si on est en etat d'attente (aucune image ne reste a encrypter ou si on travail)
	protected boolean crashed; // pour l'import permet de savoir si le daemon a crashe
	protected ServerInfo serverInfo;
	private int pid;
	
	public EncryptDaemon(){
		stop = false;
		crashed = false;
		pid = System.identityHashCode(this); 
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

	protected ServerInfo getServerInfo() {
		return serverInfo;
	}

	protected void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	/**
	 * @return the pid
	 */
	public int getPid() {
		return pid;
	}

	/**
	 * @param pid the pid to set
	 */
	public void setPid(int pid) {
		this.pid = pid;
	}
}
