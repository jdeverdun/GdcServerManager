package tools.cluster.condor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;

import settings.WindowManager;


public class CondorUtils {

	public static String getJobStatus(String JobId) throws IOException{
		
		String status="";
		java.lang.Runtime cs = java.lang.Runtime.getRuntime();
		java.lang.Process p = cs.exec("condor_q -autoformat clusterid jobstatus procid");
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Process exited with code = " + p.exitValue());
		InputStream is = p.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		// And print each line
		String ligne = null;
		ArrayList<String> sortie_condorq = new ArrayList<String>();
		while ((ligne = reader.readLine()) != null) {
			if (ligne.isEmpty()==false){
				sortie_condorq.add(ligne);}
		}
		ArrayList<String> liste= new ArrayList<String>();
		for(int i = 0 ; i < sortie_condorq.size(); i++){
			String[] liste3= sortie_condorq.get(i).split(" ");
			liste.add(liste3[0]+","+liste3[1]+","+liste3[2]);
		}
		int j=0;
		while(j<sortie_condorq.size()){
			String[] liste3=liste.get(j).split(",");
			if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("2"))
			{
				status="R";
				System.out.println("The job "+ JobId +" is running");
				j=sortie_condorq.size();
			}
			else if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("1")){
				status="I";
				System.out.println("The job "+ JobId +"  is idle");
				j=sortie_condorq.size();
			}
			else if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("5")){
				status="H";
				System.out.println("The job "+ JobId +"  is held");
				j=sortie_condorq.size();
			}
			else {j++;}
		}
		is.close();

		cs = java.lang.Runtime.getRuntime();
		p = cs.exec("condor_history -autoformat clusterid jobstatus procid");
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Process exited with code = " + p.exitValue());
		is = p.getInputStream();
		reader = new BufferedReader(new InputStreamReader(is));
		// And print each line
		ligne = null;
		ArrayList<String> sortie_condorh = new ArrayList<String>();
		while ((ligne = reader.readLine()) != null) {
			if (ligne.isEmpty()==false){
				sortie_condorh.add(ligne);}

		}
		liste= new ArrayList<String>();
		for(int i = 0 ; i < sortie_condorh.size(); i++){
			String[] liste3= sortie_condorh.get(i).split(" ");
			liste.add(liste3[0]+","+liste3[1]+","+liste3[2]);
		}
		j=0;
		while(j<sortie_condorh.size()){
			String[] liste3=liste.get(j).split(",");
			if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("4"))
			{
				status="C";
				System.out.println("The job "+ JobId +" is completed");
				j=sortie_condorh.size();
			}
			else if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("3")){
				status="X";
				 WindowManager.mwLogger.log(Level.FINE, "The job "+ JobId +"  is removed");
				j=sortie_condorh.size();
			}
			else {j++;}
		}

		is.close();
		return status;
	}

	public static void main(String[] args){

		try {
			String status=getJobStatus("1.0");
			System.out.println(status);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
