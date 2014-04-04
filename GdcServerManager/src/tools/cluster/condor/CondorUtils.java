package tools.cluster.condor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;

import settings.WindowManager;



public class CondorUtils {
	enum OS {UNIX, WINDOWS};
	enum Arch {bit_64,bit_32}; 
	public static String mapdriveDir;
	
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
				//WindowManager.mwLogger.log(Level.FINE, "The job "+ JobId +" is running");
				j=sortie_condorq.size();
			}
			else if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("1")){
				status="I";
				//WindowManager.mwLogger.log(Level.FINE, "The job "+ JobId +" is idle");
				j=sortie_condorq.size();
			}
			else if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("5")){
				status="H";
				//WindowManager.mwLogger.log(Level.FINE, "The job "+ JobId +" is held");
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
				//WindowManager.mwLogger.log(Level.FINE, "The job "+ JobId +" is completed");
				j=sortie_condorh.size();
			}
			else if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("3")){
				status="X";
				//WindowManager.mwLogger.log(Level.FINE, "The job "+ JobId +"  is removed");
				j=sortie_condorh.size();
			}
			else {j++;}
		}

		is.close();
		return status;
	}
	
	public static void submitJob(File path){//, ArrayList<File> commande, int cpu, int memory, OS Arch){
		
			Long time=System.nanoTime();
			String nom="job_"+time.toString();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path.toString()+File.separator+nom+".submit")));
			// normalement si le fichier n'existe pas, il est crée à la racine du projet
			writer.write("Universe = vanilla\n");
			writer.write("Executable = .bat\n");
			writer.write("Arguments =\n");
			writer.write("Output = "+nom+"\n");
			writer.write("Error = "+nom+"\n");
			writer.write("Log = "+nom+"\n");
			writer.write("request_cpus = \n");
			writer.write("request_memory = \n");
			writer.write("trequirements = TARGET.OpSys== OS && TARGET.Arch == Arch \n");
			writer.write("Queue\n");
			writer.close();
			}
			catch (IOException e)
			{
			e.printStackTrace();
			}
	}
	public static void main(String[] args){

		try {
			String status=getJobStatus("1.0");
			System.out.println(status);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File path =new File("C:\\Users\\Administrateur\\Documents\\MATLAB\\Joris\\test_java");;
		submitJob(path);
	}

}
