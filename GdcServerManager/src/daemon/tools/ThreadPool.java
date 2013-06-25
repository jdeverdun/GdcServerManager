package daemon.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	private static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
	private static int maxWorker;
	private static List<Thread> workers;
	private static List<DAEMONTYPE> workersDType;
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
					for(int i = 0 ; i<workers.size();i++){
						workers.remove(i);
						workersDType.remove(i);
					}
					workers = null;
					workersDType = null;
				}
			}
		}
		isLaunched = false;
	}
	
	/**
	 * Verifie la disponibilite des Threads 
	 * et renvoi vrai si un thread est disponible (a ce moment la on insere le thread dans la liste)
	 * sinon renvoi false
	 * @param dtype 
	 * @return
	 * @throws ThreadPoolException 
	 */
	public static boolean addThread(Thread t, DAEMONTYPE dtype) throws ThreadPoolException{
		if(!isLaunched)
			throw new ThreadPoolException("ThreadPool not launched.");
		if(!workers.isEmpty()){
			synchronized(workers){
				synchronized(workersDType){
					if(workers.size()<maxWorker){
						workers.add(t);
						workersDType.add(dtype);
						return true;
					}
					for(int i = 0; i < workers.size();i++){
						if(!workers.get(i).isAlive()){
							workers.remove(i);
							workersDType.remove(i);
							workers.add(t);
							workersDType.add(dtype);
							return true;
						}
					}
					return false;
				}
			}
		}else{
			workers.add(t);
			workersDType.add(dtype);
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
					for(int i = 0; i < workers.size();i++){
						if(!workers.get(i).isAlive()){
							workers.remove(i);
							workersDType.remove(i);
						}
					}
					return workersDType.contains(dt);
				}
			}
		}else{
			return false;
		}
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
					// update des thread vivant
					for(int i = 0; i < workers.size();i++){
						if(!workers.get(i).isAlive()){
							workers.remove(i);
							workersDType.remove(i);
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
		}else{
			return 0;
		}
	}
}
