package settings;

import java.nio.file.Path;

import daemon.DaemonStatusThread;
import daemon.DecryptDaemon;
import daemon.DicomDaemon;
import daemon.DicomJobDispatcher;
import daemon.DicomNode;
import daemon.EncryptDaemon;
import daemon.MissingDaemon;
import daemon.NiftiDaemon;
import model.ServerInfo;

public class SystemSettings {
	public static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
	public static Path APP_DIR;
	public static ServerInfo SERVER_INFO;
	public static DicomDaemon DICOM_DAEMON;
	public static NiftiDaemon NIFTI_DAEMON;
	public static DecryptDaemon DECRYPT_DAEMON;
	public static EncryptDaemon ENCRYPT_DAEMON;
	public static DicomNode DICOM_NODE;
	public static DicomJobDispatcher DICOM_DISPATCHER;
	public static DaemonStatusThread DAEMON_STATUS_THREAD;
	public static MissingDaemon MISSING_DAEMON;
}
