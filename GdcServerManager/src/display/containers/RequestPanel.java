package display.containers;

import javax.swing.JPanel;

import model.Project;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;

import settings.SystemSettings;
import settings.UserProfile;

public class RequestPanel extends JPanel {
	
	private static final String DEFAULT_SQL_REQUEST_TEXT = "Put custom SQL request here";
	private static final String DEFAULT_PATIENT_TEXT = "Patient ID";
	private static final String DEFAULT_PROTOCOL_TEXT = "Protocol";
	private JSplitPane splitPane;
	private JTable table;
	private JTextField txtPutCustomSql;
	private JButton btnExecute;
	private JButton btnCancel;
	private RequestTableModel rqModel;
	private JComboBox projectComboBox;
	private JTextField txtPatient;
	private JTextField txtProtocol;
	
	public RequestPanel() {
		if(UserProfile.CURRENT_USER.getLevel()==0)
			return;
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, "cell 0 0,grow");
		splitPane.setResizeWeight(0.5);
		
		JPanel requestFieldpanel = new JPanel();
		requestFieldpanel.setBorder(new TitledBorder(null, "Request", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(requestFieldpanel);
		requestFieldpanel.setLayout(new MigLayout("", "[][grow][grow]", "[][][][][]"));
		
		// on recupere la liste des projets en string en laissant une case vide au debut
		String[] projects = new String[UserProfile.CURRENT_USER.getProjects().size()+1];
		int c=1;
		for(Project p:UserProfile.CURRENT_USER.getProjects())
			projects[c++] = p.getNom();
		projectComboBox = new JComboBox(projects);
		requestFieldpanel.add(projectComboBox, "flowx,cell 0 1,growx");
		
		
		txtPatient = new JTextField();
		txtPatient.setText(DEFAULT_PATIENT_TEXT);
		txtPatient.setFont(new Font("Tahoma", Font.ITALIC, 11));
		requestFieldpanel.add(txtPatient, "cell 1 1,growx");
		txtPatient.setColumns(10);
		txtPatient.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtPatient.getText().equals("")){
					txtPatient.setText(DEFAULT_PATIENT_TEXT);
					txtPatient.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtPatient.getText().equals(DEFAULT_PATIENT_TEXT)){
					txtPatient.setText("");
					txtPatient.setFont(new Font("Tahoma", Font.PLAIN, 11));
				}
			}
		});
		
		txtPutCustomSql = new JTextField();
		txtPutCustomSql.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtPutCustomSql.setText("Put custom SQL request here");
		requestFieldpanel.add(txtPutCustomSql, "cell 0 3 3 1,growx");
		txtPutCustomSql.setColumns(10);
		
		btnExecute = new JButton("Execute");
		requestFieldpanel.add(btnExecute, "flowx,cell 0 4");
		
		btnCancel = new JButton("Cancel");
		btnCancel.setEnabled(false);
		requestFieldpanel.add(btnCancel, "cell 0 4");
		
		txtProtocol = new JTextField();
		txtProtocol.setText(DEFAULT_PROTOCOL_TEXT);
		txtProtocol.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtProtocol.setColumns(10);
		requestFieldpanel.add(txtProtocol, "cell 2 1,growx");
		
		txtProtocol.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtProtocol.getText().equals("")){
					txtProtocol.setText(DEFAULT_PROTOCOL_TEXT);
					txtProtocol.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtProtocol.getText().equals(DEFAULT_PROTOCOL_TEXT)){
					txtProtocol.setText("");
					txtProtocol.setFont(new Font("Tahoma", Font.PLAIN, 11));
				}
			}
		});
		
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
					txtPutCustomSql.setText(DEFAULT_SQL_REQUEST_TEXT);
					txtPutCustomSql.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtPutCustomSql.getText().equals(DEFAULT_SQL_REQUEST_TEXT)){
					txtPutCustomSql.setText("");
					txtPutCustomSql.setFont(new Font("Tahoma", Font.PLAIN, 11));
				}
			}
		});
		
		txtPutCustomSql.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				if(key.getKeyCode() == KeyEvent.VK_ENTER){
					btnExecute.doClick();
				}
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
	private static final String[] DEFAULT_COLUMN_NAME =  {"Project", "Patient", "Date", "Protocol", "Serie", "Image"};
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
