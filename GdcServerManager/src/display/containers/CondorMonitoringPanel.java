package display.containers;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.TitledBorder;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class CondorMonitoringPanel extends JPanel{
	private JTable table;
	private JPanel jobProgressPanel;
	private JPanel specificStatusPanel;
	private JProgressBar progressBar;
	private JProgressBar progressBar_1;
	
	public CondorMonitoringPanel() {
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
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null},
			},
			new String[] {
				"ID", "Description", "Status", "Action"
			}
		));
		specificStatusPanel.add(new JScrollPane(table), "cell 0 0,grow");

	}

}
