package tools.cluster.condor;

import java.io.*;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

import model.Job;
import model.ServerInfo;
import model.User;

import dao.project.JobDAO;
import dao.project.MySQLJobDAO;


import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;



public class CondorUtils {
	public enum OS {UNIX, WINDOWS};
	public enum Arch {X86_64, INTEL}; 
	public static String mapdriveDir;

	public static String getJobStatus(String JobId) throws IOException{

		String status=null;
		java.lang.Runtime cs = java.lang.Runtime.getRuntime();
		java.lang.Process p = cs.exec("condor_q -autoformat clusterid jobstatus procid");
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			WindowManager.mwLogger.log(Level.SEVERE, "Error : cannot run condor_q",e);
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
				status="Running";
				//WindowManager.mwLogger.log(Level.FINE, "The job "+ JobId +" is running");
				j=sortie_condorq.size();
			}
			else if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("1")){
				status="Idle";
				//WindowManager.mwLogger.log(Level.FINE, "The job "+ JobId +" is idle");
				j=sortie_condorq.size();
			}
			else if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("5")){
				status="Held";
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
				WindowManager.mwLogger.log(Level.SEVERE, "Error : cannot run condor_history",e);
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
					status="Completed";
					//WindowManager.mwLogger.log(Level.FINE, "The job "+ JobId +" is completed");
					j=sortie_condorh.size();
				}
				else if(JobId.equals(liste3[0]+"."+liste3[2]) && liste3[1].equals("3")){
					status="Removed";
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
	public static boolean isLibrary(){
		//WindowManager.MAINWINDOW==null -->library
		if(WindowManager.MAINWINDOW==null && UserProfile.CURRENT_USER!=null)
			return true;
		else
			return false;
	}

	public static void submitJob(File path, ArrayList<File> filesToTransfer, File executable, int cpu, int memory, OS os, Arch arch, String description) throws IOException, SQLException{
		String[] nom_entier=executable.getName().split("\\.");
		String nom=nom_entier[0];
		File dir=new File(path.toString()+File.separator+nom);
		System.out.println(dir);
		dir.mkdirs();
		File exe=new File(executable.getAbsolutePath());
		File exe_move=new File(dir+File.separator+executable.getName());
		Files.move(exe.toPath(), exe_move.toPath());
		File m=new File(filesToTransfer.get(0).getAbsolutePath());
		File m_move=new File(dir+File.separator+filesToTransfer.get(0).getName());
		Files.move(m.toPath(), m_move.toPath());
		File md=new File(SystemSettings.APP_DIR+File.separator+"lib"+File.separator+"MATLAB"+File.separator+"mapdrive.p");
		File md_copy=new File(dir+File.separator+"mapdrive.p");
		Files.copy(md.toPath(), md_copy.toPath());
		/*if(filesToTransfer.size()>1){
			File xls=new File(filesToTransfer.get(1).toString());
			File xls_copy=new File(dir+File.separator+filesToTransfer.get(1).getName());
			Files.copy(xls.toPath(), xls_copy.toPath());
		}
		if(filesToTransfer.size()>2){
			File template=new File(filesToTransfer.get(2).toString());
			File template_copy=new File(dir+File.separator+filesToTransfer.get(1).getName());
			Files.copy(template.toPath(), template_copy.toPath());
		}*/
		int count = 0;
		for(File lfi:filesToTransfer){
			count++;
			if(count>1){
				System.out.println("Copy "+lfi.getAbsolutePath()+" to "+dir);
				Files.copy(lfi.toPath(), new File(dir+File.separator+lfi.getName()).toPath());
			}
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir+File.separator+nom+".submit"))); 
			
			writer.write("Universe = vanilla\n");
			writer.write("Executable = "+exe.getName().toString()+"\n");
			writer.write("Arguments = $$(MATLAB_PATH)\n");
			writer.write("Output = "+nom+".out \n");
			writer.write("Error = "+nom+".err \n");
			writer.write("Log = "+nom+".log \n");
			String files="";
			for(int i=0;i<filesToTransfer.size();i++){
				files=files+", "+filesToTransfer.get(i).getName();
			}
			writer.write("transfer_input_files= mapdrive.p, "+files+" \n");
			writer.write("request_cpus = "+cpu+" \n");
			writer.write("request_memory = "+memory+" \n");
			if(os.equals(OS.UNIX) && arch.equals(Arch.X86_64))
				writer.write("requirements = TARGET.OpSys == \"LINUX\" && TARGET.Arch == \"X86_64\" \n");
			if(os.equals(OS.UNIX) && arch.equals(Arch.INTEL))
				writer.write("requirements = TARGET.OpSys == \"LINUX\" && TARGET.Arch == \"INTEL\" \n");
			if(os.equals(OS.WINDOWS) && arch.equals(Arch.X86_64))
				writer.write("requirements = TARGET.OpSys == \"WINDOWS\" && TARGET.Arch == \"X86_64\" \n");
			if(os.equals(OS.WINDOWS) && arch.equals(Arch.INTEL))
				writer.write("requirements = TARGET.OpSys == \"WINDOWS\" && TARGET.Arch == \"INTEL\" \n");
			writer.write("Queue\n");
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			WindowManager.mwLogger.log(Level.SEVERE, "Error : cannot create .submit and .bat files",e);
		}
		java.lang.Runtime cs = java.lang.Runtime.getRuntime();
		java.lang.Process p = cs.exec("condor_submit "+nom+".submit",null,dir);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			WindowManager.mwLogger.log(Level.SEVERE, "Error : cannot run condor_submit",e);
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

		User user = new User();
		user=UserProfile.CURRENT_USER;
		JobDAO jobdao = new MySQLJobDAO();
		String jobid=sortie[5]+"0";
		Date d = new Date();
		SimpleDateFormat dateStandard = new SimpleDateFormat("yyyyMMdd");

		String submitDate = dateStandard.format(d);
		if(!isLibrary()){
			if(os.equals(OS.UNIX))
				jobdao.newJob(user.getId(), jobid, submitDate, "LINUX", description);

			else
				jobdao.newJob(user.getId(), jobid, submitDate, "WINDOWS", description);
		}
		ArrayList<Job> jobs = new ArrayList<Job>();
		jobs=jobdao.retrieveAllJob();
		for(int i=0;i<jobs.size();i++)
			System.out.println(jobs.get(i).getJobId());
		//return null;//sortie[5]+"0";*/
	}

	public static void removeJob(String jobid) throws IOException, SQLException{
		java.lang.Runtime cs = java.lang.Runtime.getRuntime();
		java.lang.Process p = cs.exec("condor_rm "+jobid);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			WindowManager.mwLogger.log(Level.WARNING, "Error : cannot run condor_rm for the job"+jobid,e);
			e.printStackTrace();
		}
		// get the error stream of the process and print it
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String line;
		while((line = br.readLine()) != null) {
			//System.out.println(line);
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
		//System.out.println(sortie_condorrm);
		reader.close();
		is.close();
		JobDAO jobdao = new MySQLJobDAO();
		ArrayList<Job> jobs = new ArrayList<Job>();
		jobs=jobdao.retrieveAllJob();
		for(int i=0;i<jobs.size();i++)
			System.out.println(jobs.get(i).getJobId());
	}
	public static void main(String[] args){

		/*try {
			removeJob("178.0");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		/*ArrayList<String> job_id= new ArrayList<String>() ;
		String status=null;
		File path =new File(SystemSettings.APP_DIR.toString());
		int m = 1024;
		int cpu = 2;
		ArrayList<File> files = new ArrayList<File>();
		File file =new File("C:\\Users\\Administrateur\\Documents\\MATLAB\\Joris\\test_java\\job_22668661.m");
		files.add(0,file.getAbsoluteFile());
		File exe= new File("C:\\Users\\Administrateur\\Documents\\MATLAB\\Joris\\test_java\\job_22668661.bat");
		try {
			submitJob(path,files,exe,cpu,m,OS.WINDOWS,Arch.X86_64);
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*try {
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
		}*/
	}

}
