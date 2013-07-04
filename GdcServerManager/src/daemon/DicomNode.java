package daemon;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import settings.SystemSettings;

import daemon.dicomnode.DcmRcv;

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
		dcmrcv = new DcmRcv("DCMRCV");
		dcmrcv.setPort(DEFAULT_PORT);
		setPort(DEFAULT_PORT);
		setHostname(DEFAULT_HOSTNAME);
		initDcmRcv();
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
            dcmrcv.start();
            alive = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void stop(){
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
