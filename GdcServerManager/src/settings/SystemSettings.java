package settings;

import java.nio.file.Path;

import daemon.DaemonStatusThread;
import daemon.DecryptDaemon;
import daemon.DicomDaemon;
import daemon.DicomJobDispatcher;
import daemon.DicomNode;
import daemon.DicomEncryptDaemon;
import daemon.MissingDaemon;
import daemon.NiftiDaemon;
import model.ServerInfo;

public class SystemSettings {
	public static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
	public static Path APP_DIR;
	public static final String logdir = "logs";
	public static ServerInfo SERVER_INFO;
	public static DicomDaemon DICOM_DAEMON;
	public static NiftiDaemon NIFTI_DAEMON;
	public static DecryptDaemon DECRYPT_DAEMON;
	public static DicomEncryptDaemon ENCRYPT_DAEMON;
	public static DicomNode DICOM_NODE;
	public static DicomJobDispatcher DICOM_DISPATCHER;
	public static DaemonStatusThread DAEMON_STATUS_THREAD;
	public static MissingDaemon MISSING_DAEMON;
	
	/**
	 * Coupe les daemons du mode serveur
	 */
	public static void stopDaemons() {
		if(SystemSettings.DICOM_NODE!=null){
			SystemSettings.DICOM_NODE.stop();
			SystemSettings.DICOM_NODE = null;
		}
		if(SystemSettings.DICOM_DAEMON!=null && SystemSettings.DICOM_DAEMON.isAlive()){
			SystemSettings.DICOM_DAEMON.setStop(true);
			if(SystemSettings.DICOM_DAEMON.isStop()){
				if(SystemSettings.NIFTI_DAEMON!=null){
					SystemSettings.NIFTI_DAEMON.setStop(true);
				}
			}else{
				if(SystemSettings.DICOM_DAEMON.isAlive()){
					SystemSettings.NIFTI_DAEMON.setStop(true);
				}
			}
		}
	}
}
