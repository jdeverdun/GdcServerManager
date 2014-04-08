package tools.cluster.condor;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;


import settings.SystemSettings;
import settings.WindowManager;



public class CondorUtils {
	enum OS {UNIX, WINDOWS};
	enum Arch {X86_64, INTEL}; 
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
		reader.close();
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
			reader.close();
			is.close();
		}
		if(status==null)
			status="The job "+JobId+" doesn't exist.";
		return status;
	}

	public static String submitJob(File path, ArrayList<String> filesToTransfer, String executable, int cpu, int memory, OS os, Arch arch) throws IOException{

		Long time=System.nanoTime();
		String nom="job_"+time.toString();
		File dir=new File(path.toString()+File.separator+nom);
		dir.mkdirs();
		File exe=new File(executable);
		File exe_copy=new File(dir+File.separator+"job_22668661.bat");
		Files.copy(exe.toPath(), exe_copy.toPath());
		File m=new File(filesToTransfer.get(0));
		File m_copy=new File(dir+File.separator+"job_22668661.m");
		Files.copy(m.toPath(), m_copy.toPath());
		File md=new File("C:\\Users\\Administrateur\\git\\GdcServerManager\\GdcServerManager\\lib\\MATLAB\\mapdrive.p");
		File md_copy=new File(dir+File.separator+"mapdrive.p");
		Files.copy(md.toPath(), md_copy.toPath());
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir+File.separator+nom+".submit")));
			// normalement si le fichier n'existe pas, il est crée à la racine du projet
			writer.write("Universe = vanilla\n");
			writer.write("Executable = "+exe.getName().toString()+"\n");
			writer.write("Arguments = $$(MATLAB_PATH)\n");
			writer.write("Output = "+nom+".out \n");
			writer.write("Error = "+nom+".err \n");
			writer.write("Log = "+nom+".log \n");
			String a="";
			for(int i=0;i<filesToTransfer.size();i++){
				/*if(i==0)
					a=filesToTransfer.get(i);
				else*/
				a=a+", "+filesToTransfer.get(i);
			}
			//writer.write("transfer_input_files= "+SystemSettings.APP_DIR+File.separator+"lib"+File.separator+"MATLAB"+File.separator+"mapdrive.p"+a+" \n");
			writer.write("transfer_input_files= mapdrive.p, "+m.getName().toString()+" \n");
			writer.write("request_cpus = "+cpu+" \n");
			writer.write("request_memory = "+memory+" \n");
			if(os.equals(OS.UNIX) && arch.equals(Arch.X86_64))
				writer.write("requirements = TARGET.OpSys == \"LINUX\" && TARGET.Arch == \"X86_64\" \n");
			if(os.equals(OS.UNIX) && arch.equals(Arch.INTEL))
				writer.write("requirements = TARGET.OpSys == \"LINUX\" && TARGET.Arch == \"INTEL\" \n");
			if(os.equals(OS.WINDOWS) && arch.equals(Arch.X86_64))
				writer.write("requirements = TARGET.OpSys == \"WINDOWS\" && TARGET.Arch == \"X86_64\" \n");
			if(os.equals(OS.UNIX) && arch.equals(Arch.INTEL))
				writer.write("requirements = TARGET.OpSys == \"WINDOWS\" && TARGET.Arch == \"INTEL\" \n");
			writer.write("Queue\n");
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		java.lang.Runtime cs = java.lang.Runtime.getRuntime();
		java.lang.Process p = cs.exec("condor_submit "+nom+".submit",null,dir);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Process exited with code = " + p.exitValue());

		BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String line;
		while((line = br.readLine()) != null) {
			System.out.println(line);
		}
		InputStream is = p.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		// And print each line
		String ligne = null;
		ArrayList<String> sortie_condors = new ArrayList<String>();
		while ((ligne = reader.readLine()) != null) {
			if (ligne.isEmpty()==false){
				sortie_condors.add(ligne);}
		}
		String[] sortie=sortie_condors.get(1).split(" ");
		reader.close();
		is.close();
		return sortie[5]+"0";
	}

	public static void removeJob(String JobId) throws IOException{
		java.lang.Runtime cs = java.lang.Runtime.getRuntime();
		java.lang.Process p = cs.exec("condor_rm "+JobId);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// get the error stream of the process and print it
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String line;
		while((line = br.readLine()) != null) {
			System.out.println(line);
		}
		//System.out.println("Process exited with code = " + p.exitValue());
		InputStream is = p.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		// And print each line
		String ligne = null;
		String sortie_condorrm = new String();
		while ((ligne = reader.readLine()) != null) {
			sortie_condorrm=ligne;
		}
		System.out.println(sortie_condorrm);
		reader.close();
		is.close();
	}
	public static void main(String[] args){

		/*try {
			removeJob("178.0");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		ArrayList<String> job_id= new ArrayList<String>() ;
		String status=null;
		File path =new File("C:\\Users\\Administrateur\\Documents\\MATLAB\\Joris\\test_java");
		int m = 1024;
		int cpu = 2;
		ArrayList<String> files = new ArrayList<String>();
		files.add(0,"C:\\Users\\Administrateur\\Documents\\MATLAB\\Joris\\test_java\\job_22668661.m");
		String exe="C:\\Users\\Administrateur\\Documents\\MATLAB\\Joris\\test_java\\job_22668661.bat";
		try {
			for(int i=0;i<2;i++)
				job_id.add(submitJob(path,files,exe,cpu,m,OS.WINDOWS,Arch.X86_64));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(job_id.isEmpty()==false){
			try{
				for(int i=0;i<job_id.size();i++){
					status=getJobStatus(job_id.get(i));
					System.out.println(i+"  "+status);
					if(status.equals("C")){
						job_id.remove(i);
						System.out.println("Un en moins ...");
					}
					Thread.sleep(10000);
				}
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
