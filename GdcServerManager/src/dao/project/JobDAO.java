package dao.project;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import model.Job;
import model.Patient;
import model.Serie;
import model.User;

public interface JobDAO {

	
	/**
	 * Recupere tout les jobs
	 * @return
	 * @throws SQLException
	 */
	//public Collection<Job> retrieveAll() throws SQLException;

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
	public void removeJob(String jobid) throws SQLException;
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
