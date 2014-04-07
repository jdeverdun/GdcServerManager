package tools.cluster.condor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;

import settings.WindowManager;



public class CondorUtils {
	enum OS {UNIX, WINDOWS};
	enum Arch {X84_64, INTEL}; 
	public static String mapdriveDir;
	
	public static String getJobStatus(String JobId) throws IOException{
		
		String status=null;
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
		if(status==null)
		{
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
		}
		if(status==null)
			status="The job "+JobId+" doesn't exist.";
		return status;
	}
	
	public static void submitJob(File path, ArrayList<String> files, int cpu, int memory, OS os, Arch arch){
		
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
			String a="";
			for(int i=0;i<files.size();i++){
				if(i==0)
					a=files.get(i);
				else
					a=a+","+files.get(i);
			}
			System.out.println(a);
			writer.write("transfer_input_files= "+a+" \n");
			writer.write("request_cpus = "+cpu+" \n");
			writer.write("request_memory = "+memory+" \n");
			if(os.equals(OS.UNIX) && arch.equals(Arch.X84_64))
				writer.write("requirements = TARGET.OpSys== LINUX && TARGET.Arch == X86_64 \n");
			if(os.equals(OS.UNIX) && arch.equals(Arch.INTEL))
				writer.write("requirements = TARGET.OpSys== LINUX && TARGET.Arch == INTEL \n");
			if(os.equals(OS.WINDOWS) && arch.equals(Arch.X84_64))
				writer.write("requirements = TARGET.OpSys== WINDOWS && TARGET.Arch == X86_64 \n");
			if(os.equals(OS.UNIX) && arch.equals(Arch.INTEL))
				writer.write("requirements = TARGET.OpSys== WINDOWS && TARGET.Arch == INTEL \n");
			writer.write("Queue\n");
			writer.close();
			}
			catch (IOException e)
			{
			e.printStackTrace();
			}
	}
	
	public static void removeJob(String JobId) throws IOException{
		java.lang.Runtime cs = java.lang.Runtime.getRuntime();
		java.lang.Process p = cs.exec("condor_rm 181.0");//+JobId);
		//System.out.println("condor_rm "+JobId);
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
		ArrayList<String> sortie_condorrm = new ArrayList<String>();
		while ((ligne = reader.readLine()) != null) {
			//if (ligne.isEmpty()==false){
				sortie_condorrm.add(ligne);
				//}
			//System.out.println("condor_rm "+JobId);
		}
		//System.out.println(sortie_condorrm);
		is.close();
	}
	public static void main(String[] args){

		try {
			String status=getJobStatus("177.0");
			System.out.println(status);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File path =new File("C:\\Users\\Administrateur\\Documents\\MATLAB\\Joris\\test_java");
		int m = 1024;
		int cpu = 2;
		ArrayList<String> files = new ArrayList<String>();
		files.add(0,"mapdrive.p");
		files.add(1,"job_154563.m");
		submitJob(path,files,cpu,m,OS.UNIX,Arch.X84_64);
		/*try {
			removeJob("176.0");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
