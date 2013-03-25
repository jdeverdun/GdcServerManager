package settings;

import java.nio.file.Path;

import daemon.DecryptDaemon;
import daemon.DicomDaemon;
import daemon.NiftiDaemon;
import model.ServerInfo;

public class SystemSettings {
	public static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
	public static Path APP_DIR;
	public static ServerInfo SERVER_INFO;
	public static DicomDaemon DICOM_DAEMON;
	public static NiftiDaemon NIFTI_DAEMON;
	public static DecryptDaemon DECRYPT_DAEMON;
}
