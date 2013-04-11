package daemon;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import daemon.dicomnode.DcmRcv;

public class DicomNode extends Thread {

	// Static
	private static final int DEFAULT_PORT = 11112;
	// Attributs
	private DcmRcv dcmrcv;
	private int port;
	private String hostname;
	
	public DicomNode(){
		dcmrcv = new DcmRcv("DCMRCV");
		dcmrcv.setPort(DEFAULT_PORT);
		setPort(DEFAULT_PORT);
		initDcmRcv();
	}
	
	public DicomNode(int port){
		dcmrcv = new DcmRcv("DCMRCV");
		dcmrcv.setPort(port);
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
		dcmrcv.setStgCmtReuseFrom(false);
        dcmrcv.setStgCmtReuseTo(false);
        dcmrcv.setPackPDV(true);
        dcmrcv.setTcpNoDelay(true);
        dcmrcv.initTransferCapability();
	}
	
	public void run(){
        try {
            dcmrcv.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
