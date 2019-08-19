package model;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BIDS_server {

	private static final String DEFAULT_BIDS_DIR = "C:\\Users\\mobilettator\\Downloads\\test";
	private String host = "120.40.30.101";
	private int port = 3021;
	private String directory = null;
	
	
	private Socket socket = null;
	private OutputStream output;
    private PrintWriter writer;
    private InputStream input;
    private BufferedReader reader;

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
			output = socket.getOutputStream();
			input  = socket.getInputStream();
			writer = new PrintWriter(output, true);
			reader = new BufferedReader(new InputStreamReader(input));
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
	
	public String sendText(String text) {
		
		if(socket==null) {
			System.out.println("Open socket (connect()) first");
			return null;
		}
		try {
			
			// on envoi le message
            writer.println(text);
            writer.flush();

            // on recupere la reponse
            String response = reader.readLine();     
	        return response;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
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
