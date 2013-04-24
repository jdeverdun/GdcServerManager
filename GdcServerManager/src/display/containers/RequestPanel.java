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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.jdesktop.swingx.JXDatePicker;

import settings.SystemSettings;
import settings.UserProfile;
import javax.swing.UIManager;

public class RequestPanel extends JPanel {
	
	private static final String DEFAULT_SQL_REQUEST_TEXT = "Put custom SQL request here";
	private static final String DEFAULT_PATIENT_TEXT = "Patient ID";
	private static final String DEFAULT_PROTOCOL_TEXT = "Protocol";
	private static final String DEFAULT_BEGIN_DATE = "From";
	private static final String DEFAULT_END_DATE = "To";
	private JSplitPane splitPane;
	private JTable table;
	private JTextField txtPutCustomSql;
	private JButton btnExecute;
	private JButton btnCancel;
	private RequestTableModel rqModel;
	private JComboBox projectComboBox;
	private JTextField txtPatient;
	private JTextField txtProtocol;
	private JXDatePicker pickerDateBegin;
	private JXDatePicker pickerDateEnd;
	
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
		requestFieldpanel.setLayout(new MigLayout("", "[][][73.00][][][grow]", "[][][][][]"));
		
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

		pickerDateBegin = new JXDatePicker();
		pickerDateBegin.getEditor().setFont(new Font("Tahoma", Font.ITALIC, 11));
		pickerDateBegin.getEditor().setBorder(UIManager.getBorder("TextField.border"));
		pickerDateBegin.setDate(null);//Calendar.getInstance().getTime());
		pickerDateBegin.setFormats(new SimpleDateFormat("dd-MM-yyyy"));
		pickerDateBegin.getEditor().setText(DEFAULT_BEGIN_DATE);
		pickerDateBegin.getEditor().setColumns(10);
		requestFieldpanel.add(pickerDateBegin, "cell 3 1,growx");
		
		pickerDateEnd = new JXDatePicker();
		pickerDateEnd.getEditor().setFont(new Font("Tahoma", Font.ITALIC, 11));
		pickerDateEnd.getEditor().setBorder(UIManager.getBorder("TextField.border"));
		pickerDateEnd.setDate(null);//Calendar.getInstance().getTime());
		pickerDateEnd.setFormats(new SimpleDateFormat("dd-MM-yyyy"));
		pickerDateEnd.getEditor().setText(DEFAULT_END_DATE);
		pickerDateEnd.getEditor().setColumns(10);
		requestFieldpanel.add(pickerDateEnd, "cell 4 1,growx");
		
		txtPutCustomSql = new JTextField();
		txtPutCustomSql.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtPutCustomSql.setText("Put custom SQL request here");
		requestFieldpanel.add(txtPutCustomSql, "cell 0 3 5 1,growx");
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
		
		
		table = new JTable();
		table.setName("");
		rqModel = new RequestTableModel();
		table.setModel(rqModel);

		// on centre les colonnes
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		table.setDefaultRenderer(String.class, centerRenderer);
		
		
		splitPane.setRightComponent(new JScrollPane(table));
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

		pickerDateBegin.getEditor().addFocusListener(new FocusListener() {
			
			
			@Override
			public void focusGained(FocusEvent arg0) {
				pickerDateBegin.setFont(new Font("Tahoma", Font.PLAIN, 11));
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				
				
			}
		});
		
		pickerDateEnd.getEditor().addFocusListener(new FocusListener() {
			
			
			@Override
			public void focusGained(FocusEvent arg0) {
				pickerDateEnd.setFont(new Font("Tahoma", Font.PLAIN, 11));
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				
				
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
