package settings;

import daemon.DicomDaemon;
import daemon.NiftiDaemon;
import model.ServerInfo;

public class SystemSettings {
	public static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
	public static ServerInfo SERVER_INFO;
	public static DicomDaemon DICOM_DAEMON;
	public static NiftiDaemon NIFTI_DAEMON;
}
