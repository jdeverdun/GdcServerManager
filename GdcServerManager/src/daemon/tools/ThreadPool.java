package daemon.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import daemon.DaemonWorker;
import daemon.tools.ThreadPool.DAEMONTYPE;

import exceptions.ThreadPoolException;

/**
 * Classe permettant de fournir des Threads aux daemon
 * de maniere a limiter le nombre total de thread alloue pour le serveur
 * @author DEVERDUN Jeremy
 *
 */
public class ThreadPool {

	public static enum DAEMONTYPE{NiftiEncryptDaemon,DecryptDaemon,DicomEncryptDaemon};
	public static HashMap<DAEMONTYPE, Integer> maxWorkerForDaemonType;
	public static int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();// possibilite de le changer pr passer en mode safe
	private static int maxWorker;
	private static List<Thread> workers;
	private static List<DAEMONTYPE> workersDType;
	private static List<Integer> workersParentPid; // contient un id du processus parent qui a lance le thread
	private static boolean isLaunched = false;
	
	
	/**
	 * Initialise le pool
	 * @param nworkers
	 * @return
	 */
	public static boolean launchThreadPool(int nworkers){
		if(isLaunched)
			stopThreadPool();
		isLaunched = true;
		maxWorker = nworkers;
		workers = Collections.synchronizedList(new ArrayList<Thread>(maxWorker));
		workersDType  = Collections.synchronizedList(new ArrayList<DAEMONTYPE>(maxWorker));
		workersParentPid = Collections.synchronizedList(new ArrayList<Integer>(maxWorker));
		
		maxWorkerForDaemonType = new HashMap<DAEMONTYPE,Integer>();
		maxWorkerForDaemonType.put(DAEMONTYPE.NiftiEncryptDaemon, Integer.MAX_VALUE);
		maxWorkerForDaemonType.put(DAEMONTYPE.DecryptDaemon, 3);
		maxWorkerForDaemonType.put(DAEMONTYPE.DicomEncryptDaemon, Integer.MAX_VALUE);// Integer.MAX_VALUE correspond a un nombre infini (on definit le nombre max de worker en fonction du daemon)
			
		return true;
	}
	public static boolean launchThreadPool(){
		return launchThreadPool(AVAILABLE_CORES*2);
	}
	
	/**
	 * Coupe le pool en prenant soin de verrouiller la liste
	 */
	public static void stopThreadPool() {
		if(workers!=null && !workers.isEmpty()){
			synchronized(workers){
				synchronized(workersDType){
					synchronized(workersParentPid){
						for(int i = 0 ; i<workers.size();i++){
							removeWorkerAt(i);
						}
						workers = null;
						workersDType = null;
						workersParentPid = null;
					}
				}
			}
		}
		isLaunched = false;
	}
	
	/**
	 * Verifie la disponibilite des Threads 
	 * et renvoi vrai si un thread est disponible (a ce moment la on insere le thread dans la liste)
	 * sinon renvoi false
	 * @param t 
	 * @param pid id du thread parent 
	 * @param dtype 
	 * @return
	 * @throws ThreadPoolException 
	 */
	public static boolean addThread(Thread t, int pid, DAEMONTYPE dtype) throws ThreadPoolException{
		if(!isLaunched)
			throw new ThreadPoolException("ThreadPool not launched.");
		if(!workers.isEmpty()){
			synchronized(workers){
				synchronized(workersDType){
					synchronized(workersParentPid){
						if(workers.size()<maxWorker && numberOfThreadFor(dtype)<maxWorkerForDaemonType.get(dtype)){
							addWorker(t,pid,dtype);
							return true;
						}
						for(int i = 0; i < workers.size();i++){
							if(!workers.get(i).isAlive()){
								removeWorkerAt(i);
								addWorker(t,pid,dtype);
								return true;
							}
						}
						return false;
					}
				}
			}
		}else{
			addWorker(t,pid,dtype);
			return true;
		}
	}
	
	/**
	 * Verifie si la liste contient des workers issue d'un certain type de daemon
	 * 
	 * @param dt
	 * @return
	 */
	public static boolean contains(DAEMONTYPE dt){
		if(!isLaunched)
			return false;
		if(!workers.isEmpty()){
			synchronized(workers){
				synchronized(workersDType){
					synchronized(workersParentPid){
						for(int i = 0; i < workers.size();i++){
							if(!workers.get(i).isAlive()){
								removeWorkerAt(i);
							}
						}
						return workersDType.contains(dt);
					}
				}
			}
		}else{
			return false;
		}
	}
	
	/**
	 * Supprime un worker des differentes listes
	 * @param i
	 */
	private static void removeWorkerAt(int i){
		workers.remove(i);
		workersDType.remove(i);
		workersParentPid.remove(i);
	}
	
	/**
	 * Rajoute un worker au pool
	 * @param t
	 * @param pid
	 * @param dtype
	 */
	private static void addWorker(Thread t, int pid, DAEMONTYPE dtype){
		workers.add(t);
		workersDType.add(dtype);
		workersParentPid.add(pid);
	}
	/**
	 * Renvoi le nombre de thread d'un certain type dans le pool
	 * @param dtype
	 * @return
	 */
	public static int numberOfThreadFor(DAEMONTYPE dtype) {
		if(!isLaunched)
			return 0;
		if(!workers.isEmpty()){
			synchronized(workers){
				synchronized(workersDType){
					synchronized(workersParentPid){
						// update des thread vivant
						for(int i = 0; i < workers.size();i++){
							if(!workers.get(i).isAlive()){
								removeWorkerAt(i);
							}
						}
						int count = 0;
						// on compte le nombre de thread du bon type
						for(int i = 0; i < workersDType.size();i++){
							if(workersDType.get(i) == dtype){
								count++;
							}
						}
						return count;
					}
				}
			}
		}else{
			return 0;
		}
	}
	
	/**
	 * Renvoi le nombre de thread issue du process pid
	 * @param dtype
	 * @return
	 */
	public static int numberOfThreadFor(int pid) {
		if(!isLaunched)
			return 0;
		if(!workers.isEmpty()){
			synchronized(workers){
				synchronized(workersDType){
					synchronized(workersParentPid){
						// update des thread vivant
						for(int i = 0; i < workers.size();i++){
							if(!workers.get(i).isAlive()){
								removeWorkerAt(i);
							}
						}
						int count = 0;
						// on compte le nombre de thread du bon type
						for(int i = 0; i < workersParentPid.size();i++){
							if(workersParentPid.get(i) == pid){
								count++;
							}
						}
						return count;
					}
				}
			}
		}else{
			return 0;
		}
	}
	
	/**
	 * Coupe les thread en fonction de leur pid
	 * @param pid
	 */
	public static void stopThreadsByPid(int pid) {
		if(!isLaunched)
			return;
		if(workers.isEmpty())
			return;
		
		synchronized(workers){
			synchronized(workersDType){
				synchronized(workersParentPid){
					// update des thread vivant
					for(int i = 0; i < workers.size();i++){
						if(!workers.get(i).isAlive()){
							removeWorkerAt(i);
						}else{
							if(workersParentPid.get(i) == pid){
								workers.get(i).stop();
								removeWorkerAt(i);
							}
						}
					}
				}
			}
		}
	}
	public static void resetCores() {
		// TODO Auto-generated method stub
		stopThreadPool();
		AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
		launchThreadPool(AVAILABLE_CORES*2);
	}

}
