package daemon;

import java.io.IOException;
import java.util.logging.Level;


import dicomnode.DcmRcv;
import settings.SystemSettings;
import settings.WindowManager;

public class DicomNode {

	// Static
	public static int DEFAULT_PORT = 104;//11112;
	public static String DEFAULT_HOSTNAME = "127.0.0.1";//"120.40.30.110";
	public static String DEFAULT_AE_TITLE = "RECHPASS";
	// Attributs
	private DcmRcv dcmrcv;
	private int port;
	private String hostname;
	private boolean alive;

	public DicomNode(){
		WindowManager.mwLogger.log(Level.INFO,"DICOM NODE INIT0");
		dcmrcv = new DcmRcv("DCMRCV");
		dcmrcv.setPort(DEFAULT_PORT);
		setPort(DEFAULT_PORT);
		setHostname(DEFAULT_HOSTNAME);
		initDcmRcv();
		WindowManager.mwLogger.log(Level.INFO,"DICOM NODE INIT1");
	}
	
	public DicomNode(int port){
		dcmrcv = new DcmRcv("DCMRCV");
		dcmrcv.setPort(port);
		setHostname(DEFAULT_HOSTNAME);
		setPort(port);
		initDcmRcv();
	}
	
	public DicomNode(String hostname, int port){
		dcmrcv = new DcmRcv("DCMRCV");
		dcmrcv.setPort(port);
		dcmrcv.setHostname(hostname);
		setPort(port);
		setHostname(hostname);
		initDcmRcv();
		
	}
	public void initDcmRcv(){
		alive = false;
		dcmrcv.setAEtitle(DEFAULT_AE_TITLE);
		dcmrcv.setDestination(SystemSettings.SERVER_INFO.getIncomingDir().toString());
		dcmrcv.setStgCmtReuseFrom(false);
        dcmrcv.setStgCmtReuseTo(false);
        dcmrcv.setPackPDV(true);
        dcmrcv.setTcpNoDelay(true);
        dcmrcv.initTransferCapability();
        dcmrcv.setHostname(getHostname());
	}
	
	public void start(){
        try {
			WindowManager.mwLogger.log(Level.INFO,"DICOM NODE START0");
            dcmrcv.start();
			WindowManager.mwLogger.log(Level.INFO,"DICOM NODE STARTED");
            alive = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void stop(){
		WindowManager.mwLogger.log(Level.INFO,"DICOM NODE STOP");
		dcmrcv.stop();
		alive = false;
	}
	
	public DcmRcv getDcmrcv() {
		return dcmrcv;
	}

	public void setDcmrcv(DcmRcv dcmrcv) {
		this.dcmrcv = dcmrcv;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public boolean isAlive() {
		return alive;
	}
	public String getStatus() {
		if(isAlive())
			return "Running";
		else
			return "";
	}
}
