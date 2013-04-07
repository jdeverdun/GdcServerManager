package display.containers;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.DropMode;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JButton;

public class RequestPanel extends JPanel {
	private JSplitPane splitPane;
	private JTable table;
	private JTextField txtPutCustomSql;
	private JButton btnExecute;
	private JButton btnCancel;
	private RequestTableModel rqModel;
	
	public RequestPanel() {
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, "cell 0 0,grow");
		splitPane.setResizeWeight(0.5);
		
		JPanel requestFieldpanel = new JPanel();
		requestFieldpanel.setBorder(new TitledBorder(null, "Request", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(requestFieldpanel);
		requestFieldpanel.setLayout(new MigLayout("", "[grow]", "[][][][grow][]"));
		
		txtPutCustomSql = new JTextField();
		txtPutCustomSql.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtPutCustomSql.setText("Put custom SQL request here");
		requestFieldpanel.add(txtPutCustomSql, "cell 0 3,growx");
		txtPutCustomSql.setColumns(10);
		
		btnExecute = new JButton("Execute");
		requestFieldpanel.add(btnExecute, "flowx,cell 0 4");
		
		btnCancel = new JButton("Cancel");
		btnCancel.setEnabled(false);
		requestFieldpanel.add(btnCancel, "cell 0 4");
		
		table = new JTable();
		table.setName("");
		rqModel = new RequestTableModel();
		table.setModel(rqModel);

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
		
		btnExecute.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getRqModel().setColumns(new String[]{"tata","titi"});
				getRqModel().setData(new Object[][]{{"lol","kewl"}});
				getRqModel().fireTableStructureChanged();
			}
		});
	}

	public RequestTableModel getRqModel() {
		return rqModel;
	}

	public void setRqModel(RequestTableModel rqModel) {
		this.rqModel = rqModel;
	}

}



class RequestTableModel extends AbstractTableModel {
	private File[] files;
	private String[] columnNames = {"Project", "Patient", "Date", "Protocol", "Serie", "Image"};
    private Object[][] data = {
    {"Crescendo", "30255", "23121980", "ASL", "ASL_PERFUSION", "machine_ASL.nii"}
    };

    public int getColumnCount() {
        return columnNames.length;
    }

    public void setColumns(String[] colnames) {
    	columnNames = colnames;
	}

    public void setData(Object[][] data){
    	this.data = data;
    }
	public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

	public boolean isCellEditable(int row, int col) {
    	 return false;
	}

     public void setValueAt(Object value, int row, int col) {            
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}
