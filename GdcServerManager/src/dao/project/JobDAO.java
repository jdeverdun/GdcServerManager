package dao.project;

import java.sql.SQLException;
import java.util.ArrayList;


import model.Job;


public interface JobDAO {

	
	/**
	 * Recupere tout les jobs
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<Job> retrieveAllJob() throws SQLException;

	/**
	 * Recupere le job avec l'id  id
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public Job retrieveJobById(int id) throws SQLException;
	public Job retrieveJobByJobId(String jobid) throws SQLException;
	public ArrayList<Job> retrieveJobByLogin(String login) throws SQLException;
	public ArrayList<Job> retrieveJobByUserId(int user_id) throws SQLException;
	/**
	 * créé un nouveau job
	 * @param id
	 * @param user_id
	 * @param jobid
	 * @return
	 * @throws SQLException
	 */
	public boolean newJob(int user_id,String jobid,String submitDate,String os,String description) throws SQLException;
	public void removeJob(String jobid, int user_id) throws SQLException;
	/**
	 * met à jours un tuple
	 * @param id
	 * @param user_id
	 * @param jobid
	 * @return
	 * @throws SQLException
	 */
	//public boolean updateJob(int user_id,String jobid,String submitDate,String os,String description) throws SQLException;
}
