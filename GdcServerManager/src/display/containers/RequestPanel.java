package display.containers;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.DropMode;
import javax.swing.border.TitledBorder;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class RequestPanel extends JPanel {
	private JSplitPane splitPane;
	private JTable table;
	private JTextField txtPutCustomSql;
	
	public RequestPanel() {
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, "cell 0 0,grow");
		splitPane.setResizeWeight(0.5);
		
		JPanel requestFieldpanel = new JPanel();
		requestFieldpanel.setBorder(new TitledBorder(null, "Request", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(requestFieldpanel);
		requestFieldpanel.setLayout(new MigLayout("", "[grow]", "[][][][]"));
		
		txtPutCustomSql = new JTextField();
		txtPutCustomSql.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtPutCustomSql.setText("Put custom SQL request here");
		requestFieldpanel.add(txtPutCustomSql, "cell 0 3,growx");
		txtPutCustomSql.setColumns(10);
		
		table = new JTable();
		table.setName("");
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{"Crescendo", "30255", "23121980", "ASL", "ASL_PERFUSION", "machine_ASL.nii"},
			},
			new String[] {
				"Project", "Patient", "Date", "Protocol", "Serie", "Image"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class, String.class, String.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});

		// on centre les colonnes
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		table.setDefaultRenderer(String.class, centerRenderer);
		
		
		splitPane.setRightComponent(new JScrollPane(table));
		
		txtPutCustomSql.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtPutCustomSql.getText().equals("")){
					txtPutCustomSql.setText("Put custom SQL request here");
					txtPutCustomSql.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				txtPutCustomSql.setText("");
				txtPutCustomSql.setFont(new Font("Tahoma", Font.PLAIN, 11));
			}
		});
	}

}
