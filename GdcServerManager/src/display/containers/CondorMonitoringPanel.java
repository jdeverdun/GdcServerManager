package display.containers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.JPanel;

import model.Job;
import model.User;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import settings.UserProfile;
import tools.cluster.condor.CondorUtils;

import dao.project.JobDAO;
import dao.project.MySQLJobDAO;
import display.MainWindow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CondorMonitoringPanel extends JPanel{
	private JTable table;
	private JPanel jobProgressPanel;
	private JPanel specificStatusPanel;
	private JProgressBar progressBar;
	private JProgressBar progressBar_1;
	private JButton delete;

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

		table = new JTable();
		table.setCellSelectionEnabled(true);
		table.setColumnSelectionAllowed(true);
		table.setRowHeight(50);
		
		DefaultTableModel model =new DefaultTableModel(){
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		model.addColumn("ID");
		model.addColumn("Description");
		model.addColumn("Status");
		model.addColumn("Action");
		
		updateTable();
		
		table.setModel(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    table.getColumnModel().getColumn(0).setPreferredWidth(260);
	    table.getColumnModel().getColumn(1).setPreferredWidth(260);
	    table.getColumnModel().getColumn(2).setPreferredWidth(260);
	    table.getColumnModel().getColumn(3).setPreferredWidth(117);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.LEFT );
		centerRenderer.setVerticalAlignment(JLabel.CENTER);
		for(int i=0;i<model.getColumnCount();i++)
			table.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
		TableCellRenderer buttonRenderer = new JTableButtonRenderer();

		table.getColumn("Action").setCellRenderer(buttonRenderer);
		//table.setDefaultRenderer(Object.class, new JTableButtonRenderer());
		//table.setDefaultEditor(Object.class, new JTableButtonRenderer());
		table.addMouseListener(new JTableButtonMouseListener(table,model));
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setEnabled(false);
		specificStatusPanel.add(scrollPane, "cell 0 0,grow");

	}

	public void updateTable()  
	{  
		Thread updatethread = new Thread(new Runnable() {

			@Override
			public void run() {
				DefaultTableModel model = (DefaultTableModel)table.getModel();
				ArrayList<Job> jobs = new ArrayList<Job>();
				ArrayList<String> jobid = new ArrayList<String>();
				ArrayList<String> description = new ArrayList<String>();
				ArrayList<String> status = new ArrayList<String>();
				User user=UserProfile.CURRENT_USER;
				JobDAO jobdao = new MySQLJobDAO();
				try {
					jobs=jobdao.retrieveJobByUserId(user.getId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				int countr=model.getRowCount();
				if(countr!=0){
					for(int i=countr-1;i>=0;i--)
					{
						model.removeRow(i);
					}
				}
				for(int i=0;i<jobs.size();i++)
				{
					jobid.add(jobs.get(i).getJobId());
					description.add("<html>"+jobs.get(i).getDescription().replace("\n","<br>")+"</html>");
					try {
						status.add(CondorUtils.getJobStatus(jobid.get(i)));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					model.addRow(new Object[]{jobid.get(i),description.get(i),status.get(i),null});
					/*try
				    {
				        for (int row = 0; row < table.getRowCount(); row++)
				        {
				            int rowHeight = table.getRowHeight();

				                Component comp = table.prepareRenderer(table.getCellRenderer(row, 1), row, 1);
				                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);

				            table.setRowHeight(row, rowHeight);
				        }
				    }
				    catch(ClassCastException e) {}*/
				}
			}
		});
		updatethread.start();
	}

	public class JTableButtonRenderer  extends JButton implements TableCellRenderer {        
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Object value;
		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, final int row, int column) {
			Image img;
			Image newimg;
			ImageIcon icon;
			ImageIcon icon2;
			icon2 = new ImageIcon(CondorMonitoringPanel.class.getResource("/images/deleteJob.png"));
			img = icon2.getImage();  
			//newimg = img.getScaledInstance(15,15,  java.awt.Image.SCALE_SMOOTH); 
			newimg = img.getScaledInstance(30,30,  java.awt.Image.SCALE_SMOOTH);
			icon = new ImageIcon(newimg);
			JButton button = new JButton(icon);
			//JButton button = (JButton)value;
			return button;  
		}
	}
	private static class JTableButtonMouseListener extends MouseAdapter {
		private final JTable table;
		private final DefaultTableModel model;
		public JTableButtonMouseListener(JTable table,DefaultTableModel model) {
			this.table = table;
			this.model = model;
		}
		private int row;
		private int column;
		public void mouseClicked(MouseEvent e) {
			column = table.getColumnModel().getColumnIndexAtX(e.getX());
			row    = e.getY()/table.getRowHeight(); 
			if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
				Object value = table.getValueAt(row, column);
				if (value instanceof JButton) {
					((JButton)value).doClick();
				}
			}
			CondorUtils rm =new CondorUtils();
			String jobid = table.getValueAt(row, 0).toString();
			String status = table.getValueAt(row, 2).toString();
			if(column==3){
				JDialog.setDefaultLookAndFeelDecorated(true);
				int n = JOptionPane.showConfirmDialog(
						table,
						"Do you want really remove the job " + jobid + "?",
						null,
						JOptionPane.YES_NO_OPTION);

				if(n==0){
					model.removeRow(row);

					if(status.equals("Removed")==false && status.equals("Completed")==false){
						try {
							rm.removeJob(jobid);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					User user=UserProfile.CURRENT_USER;
					JobDAO jobdao = new MySQLJobDAO();
					if(status.equals("Running")==false && status.equals("Idle")==false && status.equals("Held")==false){
						try {
							jobdao.removeJob(jobid, user.getId());
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}
}
