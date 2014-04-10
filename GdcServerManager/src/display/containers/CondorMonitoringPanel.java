package display.containers;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JPanel;

import model.Job;
import model.User;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.TitledBorder;
import javax.swing.DefaultListModel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import settings.UserProfile;

import dao.project.JobDAO;
import dao.project.MySQLJobDAO;
import java.awt.Color;

public class CondorMonitoringPanel extends JPanel{
	private JTable table;
	private JPanel jobProgressPanel;
	private JPanel specificStatusPanel;
	private JProgressBar progressBar;
	private JProgressBar progressBar_1;

	public CondorMonitoringPanel() throws SQLException {
		setLayout(new MigLayout("", "[grow]", "[][grow]"));

		jobProgressPanel = new JPanel();
		jobProgressPanel.setBorder(new TitledBorder(null, "Global progress", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(jobProgressPanel, "cell 0 0,grow");
		jobProgressPanel.setLayout(new MigLayout("", "[grow]", "[][][][]"));

		progressBar = new JProgressBar();
		jobProgressPanel.add(progressBar, "cell 0 1,grow");

		progressBar_1 = new JProgressBar();
		jobProgressPanel.add(progressBar_1, "cell 0 3,grow");

		specificStatusPanel = new JPanel();
		add(specificStatusPanel, "cell 0 1,grow");
		specificStatusPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));

		/*ArrayList<Job> jobs =new ArrayList<Job>();
		ArrayList<String> jobid =new ArrayList<String>();
		ArrayList<String> description =new ArrayList<String>();
		User user = new User();
		user=UserProfile.CURRENT_USER;
		JobDAO jobdao = new MySQLJobDAO();
		jobs=jobdao.retrieveJobByUserId(user.getId());
		for(int i=0;i<jobs.size();i++){
			jobid.add(jobs.get(i).getJobId());
			description.add(jobs.get(i).getDescription());

		}*/

		table = new JTable();
		table.setCellSelectionEnabled(true);
		table.setColumnSelectionAllowed(true);
		DefaultTableModel model =new DefaultTableModel();

		model.addColumn("ID");
		model.addColumn("Description");
		model.addColumn("Status");
		model.addColumn("Action");
		updateTable();
		/*for(int i=0;i<jobid.size();i++)
		{
			model.addRow(new Object[]{jobid.get(i),description.get(i),"status",null});
		}*/
		table.setModel(model);
		/*table.setModel(new DefaultTableModel(
			new Object[][] {
				{"jobid", "description", "status", null},
			},
			new String[] {
				"ID", "Description", "Status", "Action"
			}
		));*/
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setEnabled(false);
		specificStatusPanel.add(scrollPane, "cell 0 0,grow");

	}

	public void updateTable()  
	{  
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				DefaultTableModel model = (DefaultTableModel)table.getModel();
				ArrayList<Job> jobs =new ArrayList<Job>();
				ArrayList<String> jobid =new ArrayList<String>();
				ArrayList<String> description =new ArrayList<String>();
				User user=UserProfile.CURRENT_USER;
				JobDAO jobdao = new MySQLJobDAO();
				try {
					jobs=jobdao.retrieveJobByUserId(user.getId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(int i=0;i<jobs.size();i++){
					jobid.add(jobs.get(i).getJobId());
					description.add(jobs.get(i).getDescription());

				}
				int count=model.getRowCount();
				if(count!=0){
					for(int i=count-1;i>=0;i--)
					{
						System.out.println(i);
						System.out.println(count);
						//System.out.println(model.getValueAt(6, 0));
						model.removeRow(i);
					}
				}
				//model.
				//model.fireTableDataChanged();
				//model.fireTableStructureChanged();
				for(int i=0;i<jobid.size();i++)
				{
					model.addRow(new Object[]{jobid.get(i),description.get(i),"status",null});
				}
			}
		});

	}

}
