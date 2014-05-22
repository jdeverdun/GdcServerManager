package daemon;

import java.sql.SQLException;
import java.util.logging.Level;

import settings.SystemSettings;
import settings.WindowManager;
import dao.GenericRequestDAO;
import dao.MySQLGenericRequestDAO;
import display.containers.ServerStatusPanel;


/**
 * Permet de connaitre l'etat des daemon (fonctionnement ou pas etc)
 * @author Mobilette
 *
 */
public class DaemonStatusThread extends Thread { 

	private static final int DEFAULT_REFRESH_TIME = 5000;
	private int refresh_time;
	private ServerStatusPanel statusPanel; // panel a mettre a jours
	private boolean stop; // variable pour savoir si on termine le thread
	
	public DaemonStatusThread(ServerStatusPanel sp){
		setStatusPanel(sp);
		stop = false;
		setRefresh_time(DEFAULT_REFRESH_TIME);
	}
	
	public DaemonStatusThread(ServerStatusPanel sp, int refreshtime){
		setStatusPanel(sp);
		stop = false;
		setRefresh_time(refreshtime);
	}

	public void run(){
		while(!isStop()){
			// on met a jours le panel
			updatePanel();
			// on attend
			try {
				Thread.sleep(getRefresh_time());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	// accesseurs
	public ServerStatusPanel getStatusPanel() {
		return statusPanel;
	}

	public void setStatusPanel(ServerStatusPanel statusPanel) {
		this.statusPanel = statusPanel;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public int getRefresh_time() {
		return refresh_time;
	}

	public void setRefresh_time(int refresh_time) {
		this.refresh_time = refresh_time;
	}
	
	
	private void updatePanel() {
		if(SystemSettings.DECRYPT_DAEMON!=null){
			statusPanel.setDecrypterAlive(SystemSettings.DECRYPT_DAEMON.isAlive());
			statusPanel.getLblCommentdecrypter().setText(SystemSettings.DECRYPT_DAEMON.getStatus());
		}else{
			statusPanel.setDecrypterAlive(false);
			statusPanel.getLblCommentdecrypter().setText("");
		}
		if(SystemSettings.ENCRYPT_DAEMON!=null){
			statusPanel.setEncrypterAlive(SystemSettings.ENCRYPT_DAEMON.isAlive());
			statusPanel.getLblCommentencrypter().setText(SystemSettings.ENCRYPT_DAEMON.getStatus());
		}else{
			statusPanel.setEncrypterAlive(false);
			statusPanel.getLblCommentencrypter().setText("");
		}
		if(SystemSettings.DICOM_DAEMON!=null){
			statusPanel.setDicomDaemonAlive(SystemSettings.DICOM_DAEMON.isAlive());
			statusPanel.getLblCommentdicomdaemon().setText(SystemSettings.DICOM_DAEMON.getStatus());
		}else{
			statusPanel.setDicomDaemonAlive(false);
			statusPanel.getLblCommentdicomdaemon().setText("");
		}
		if(SystemSettings.DICOM_DISPATCHER!=null){
			statusPanel.setDicomDispatcherAlive(SystemSettings.DICOM_DISPATCHER.isAlive());
			statusPanel.getLblCommentdicomdispatcher().setText(SystemSettings.DICOM_DISPATCHER.getStatus());
		}else{
			statusPanel.setDicomDispatcherAlive(false);
			statusPanel.getLblCommentdicomdispatcher().setText("");
		}
		if(SystemSettings.DICOM_NODE!=null){
			statusPanel.setDicomNodeAlive(SystemSettings.DICOM_NODE.isAlive());
			statusPanel.getLblCommentdicomnode().setText(SystemSettings.DICOM_NODE.getStatus());
		}else{
			statusPanel.setDicomNodeAlive(false);
			statusPanel.getLblCommentdicomnode().setText("");
		}
		if(SystemSettings.NIFTI_DAEMON!=null){
			statusPanel.setNiftiDaemonAlive(SystemSettings.NIFTI_DAEMON.isAlive());
			statusPanel.getLblCommentniftidaemon().setText(SystemSettings.NIFTI_DAEMON.getStatus());
		}else{
			statusPanel.setNiftiDaemonAlive(false);
			statusPanel.getLblCommentniftidaemon().setText("");
		}
		if(SystemSettings.MISSING_DAEMON!=null){
			statusPanel.setMissingDaemonAlive(SystemSettings.MISSING_DAEMON.isAlive());
			statusPanel.getLblCommentMissingdaemon().setText(SystemSettings.MISSING_DAEMON.getStatus());
		}else{
			statusPanel.setMissingDaemonAlive(false);
			statusPanel.getLblCommentMissingdaemon().setText("");
		}
		
	}
}
