package daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;

import settings.SystemSettings;

import static java.nio.file.StandardWatchEventKinds.*;

import model.DicomImage;
import model.ServerInfo;


/**
 * Daemon principal, point de depart du traitement des dicom 
 * @author Mobilette
 *
 */
public class DicomDaemon extends Thread{

	
	
	
	// Attributs
	private ServerInfo serverInfo;
	private DicomJobDispatcher dicomJobDispatcher;
	private EncryptDaemon encryptDaemon;
	private boolean stop;
	private WatchService watcher;
	
	
	public DicomDaemon(ServerInfo si) {
		setServerInfo(si);
		
		// on stop les daemons si ils tournent deja
		if(SystemSettings.ENCRYPT_DAEMON!=null && SystemSettings.ENCRYPT_DAEMON.isAlive())
			SystemSettings.ENCRYPT_DAEMON.setStop(true);
		if(SystemSettings.DICOM_DISPATCHER!=null && SystemSettings.DICOM_DISPATCHER.isAlive())
			SystemSettings.DICOM_DISPATCHER.setStop(true);
		
		dicomJobDispatcher = new DicomJobDispatcher(this);
		encryptDaemon = new EncryptDaemon(this);
		SystemSettings.ENCRYPT_DAEMON = encryptDaemon;
		SystemSettings.DICOM_DISPATCHER = dicomJobDispatcher;
	}
	
	/*
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	 public void run() { 
		System.out.println("Dicom Daemon Online.");
		// on lance le dispatcher
		dicomJobDispatcher.start();
		// puis l'encrypteur
		encryptDaemon.start();
		
		Path dir = serverInfo.getIncomingDir();
		try {
			watcher = FileSystems.getDefault().newWatchService();
		    WatchKey key2 = dir.register(watcher,
		                           ENTRY_MODIFY);
		    for (;;) {

		        // wait for key to be signaled
		        WatchKey key;
		        try {
		            key = watcher.take();
		        } catch (Exception x) {
		        	System.out.println("Exception with DicomDaemon : "+x.toString());
		            return;
		        }
		        for (WatchEvent<?> event: key.pollEvents()) {
		            WatchEvent.Kind<?> kind = event.kind();
		            // This key is registered only
		            // for ENTRY_CREATE events,
		            // but an OVERFLOW event can
		            // occur regardless if events
		            // are lost or discarded.
		            /*if (kind == OVERFLOW) {
		                continue;
		            }*/

		            // The filename is the
		            // context of the event.
		            WatchEvent<Path> ev = (WatchEvent<Path>)event;
		            Path filename = ev.context();
		            Path realPath = Paths.get(dir.toString() + File.separator + filename.getFileName());
	            	dicomJobDispatcher.addDicomToMove(realPath);// on va tester si c'est un dicom ou pas par la suite
		        }

		        // Reset the key -- this step is critical if you want to
		        // receive further watch events.  If the key is no longer valid,
		        // the directory is inaccessible so exit the loop.
		        boolean valid = key.reset();
		        if (!valid) {
		            break;
		        }
		        if(isStop()){
		        	encryptDaemon.setStop(true);
		        	dicomJobDispatcher.setStop(true);
		        	break;
		        }
		    }
		} catch (IOException x) {
			System.out.println(x.toString());
		}
	  }
	public ServerInfo getServerInfo() {
		return serverInfo;
	}
	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}
	public DicomJobDispatcher getDicomJobDispatcher() {
		return dicomJobDispatcher;
	}
	public void setDicomJobDispatcher(DicomJobDispatcher dicomJobDispatcher) {
		this.dicomJobDispatcher = dicomJobDispatcher;
		SystemSettings.DICOM_DISPATCHER = this.dicomJobDispatcher;
	}
	public EncryptDaemon getEncryptDaemon() {
		return encryptDaemon;
	}
	public void setEncryptDaemon(EncryptDaemon encryptDaemon) {
		this.encryptDaemon = encryptDaemon;
		SystemSettings.ENCRYPT_DAEMON = this.encryptDaemon;
	}
	public void setStop(boolean b) {
		stop = b;
		if(b){
			try {
				watcher.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			encryptDaemon.setStop(true);
	    	dicomJobDispatcher.setStop(true);
	    	System.out.println("DicomDaemon offline");
		}
	}
	public void forceStop(){
		encryptDaemon.setStop(true);
		dicomJobDispatcher.forceStop(true);
		System.out.println("DicomDaemon offline");
	}
	public boolean isStop() {
		return stop;
	}

	public String getStatus() {
		if(this.isAlive())
			return "Running";
		else
			return "";
	}
}
