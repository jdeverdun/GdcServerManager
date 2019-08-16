package model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class BIDS_server {

	private static final String DEFAULT_BIDS_DIR = "C:\\Users\\mobilettator\\Downloads\\test";
	private String host = "120.40.30.101";
	private int port = 3021;
	private String directory = null;
	
	
	private Socket socket = null;


	public BIDS_server() {
		directory = DEFAULT_BIDS_DIR;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public boolean connect() {
		try {
			socket = new Socket(host, port);
			System.out.println("connected to "+host+":"+port);
			return true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean sendText(String text) {
		if(socket==null) {
			System.out.println("Open socket (connect()) first");
			return false;
		}
		// get the output stream from the socket.
		System.out.println("Sending string to the ServerSocket");
		try {
	        OutputStream outputStream = socket.getOutputStream();
	        // create a data output stream from the output stream so we can send data through it
	        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
	        // write the message we want to send
	        dataOutputStream.writeUTF(text);
	        dataOutputStream.flush(); // send the message
	        dataOutputStream.close(); // close the output stream when we're done.
	        System.out.println("Envoi ok");
	        return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean disconnect() {
		if(socket==null) return true;
        try {
        	System.out.println("Closing socket and terminating program.");
			socket.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
