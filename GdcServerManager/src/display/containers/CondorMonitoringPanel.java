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
import javax.swing.JButton;
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

import settings.UserProfile;
import tools.cluster.condor.CondorUtils;

import dao.project.JobDAO;
import dao.project.MySQLJobDAO;
import java.awt.Color;
import java.awt.Component;
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
		DefaultTableModel model =new DefaultTableModel(){
			public boolean isCellEditable(int row, int column) {
				if(column==3)
					return true;
				else
					return false;
			}
		};
		
		model.addColumn("ID");
		model.addColumn("Description");
		model.addColumn("Status");
		model.addColumn("Action");
		updateTable();

		table.setModel(model);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		for(int i=0;i<model.getColumnCount();i++)
			table.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
		TableCellRenderer buttonRenderer = new JTableButtonRenderer();
		
		table.getColumn("Action").setCellRenderer(buttonRenderer);
		//table.setDefaultRenderer(Object.class, new JTableButtonRenderer());
		table.setDefaultEditor(Object.class, new JTableButtonRenderer());
		//table.addMouseListener(new JTableButtonMouseListener(table));
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
					description.add(jobs.get(i).getDescription());
					try {
						status.add(CondorUtils.getJobStatus(jobid.get(i)));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					model.addRow(new Object[]{jobid.get(i),description.get(i),status.get(i),null});

				}
				
			}
		});
		updatethread.start();
	}

	public class JTableButtonRenderer  extends JButton implements TableCellRenderer, TableCellEditor {        
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Object value;
		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, final int row, int column) {
			
			JButton button = new JButton("delete");
			//JButton button = (JButton)value;
			return button;  
		}
		@Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			this.value=value;
			//JButton button = new JButton("delete");
			System.out.println("111111111111");
			addActionListener(new ActionListener()
			{

				public void actionPerformed(ActionEvent e)
				{
					System.out.println("pourquoi?");
				}

			});
			return this;
		}
		public void cancelCellEditing(){}

		public boolean stopCellEditing(){
			return false;
		}

		public Object getCellEditorValue(){
			return value;
		}

		public boolean isCellEditable(EventObject anEvent){
			return true;
		}

		public boolean shouldSelectCell(EventObject anEvent){
			return false;
		}

		public void addCellEditorListener(CellEditorListener l){}

		public void removeCellEditorListener(CellEditorListener l){}

	}
	/*private static class JTableButtonMouseListener extends MouseAdapter {
		private final JTable table;

		public JTableButtonMouseListener(JTable table) {
			this.table = table;
		}

		public void mouseClicked(MouseEvent e) {
			int column = table.getColumnModel().getColumnIndexAtX(e.getX());
			int row    = e.getY()/table.getRowHeight(); 
			System.out.println("aaaaaaaaaaaa");
			if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
				Object value = table.getValueAt(row, column);
				System.out.println("bbbbbbbbbbbbbb");
				System.out.println(value);
				if (value instanceof JButton) {
					((JButton)value).doClick();
				}
			}
		}
	}*/
}
