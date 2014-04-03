package tools.cluster.condor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class CondorUtils {
	
	public static void getJobStatus() throws IOException{
		java.lang.Runtime cs = java.lang.Runtime.getRuntime();
		java.lang.Process p = cs.exec("condor_q");
        try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Process exited with code = " + p.exitValue());
        InputStream is = p.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        // And print each line
        String ligne = null;
        ArrayList<String> sortie_condorq = new ArrayList<String>();
        while ((ligne = reader.readLine()) != null) {
        	sortie_condorq.add(ligne);
        }
        //System.out.println(sortie_condorq);
        String[] liste= new String[sortie_condorq.size()];
        int i=0;
        for(String fi:sortie_condorq)
        {
        	liste[i++]=fi;
        }
        System.out.println(liste[3]);
        is.close();
        
	}
	
	public static void main(String[] args){
		
		try {
			getJobStatus();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
