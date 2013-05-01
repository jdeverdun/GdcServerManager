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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.jdesktop.swingx.JXDatePicker;

import dao.GenericRequestDAO;
import dao.MySQLGenericRequestDAO;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.UserProfile;
import settings.sql.DBTables;

import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.DefaultComboBoxModel;

public class RequestPanel extends JPanel {
	
	private static final String DEFAULT_SQL_REQUEST_TEXT = "Put custom SQL request here";
	private static final String DEFAULT_PATIENT_TEXT = "Patient ID";
	private static final String DEFAULT_PROTOCOL_TEXT = "Protocol";
	private static final String DEFAULT_SERIE_TEXT = "Serie";
	private static final String DEFAULT_BEGIN_DATE = "From";
	private static final String DEFAULT_END_DATE = "To";
	public static enum IMAGE_TYPE{DICOM,NIFTI};
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
	private JLabel lblError;
	private JTextField textSerie;
	private JComboBox comboBoxImageType;
	
	public RequestPanel() {
		if(UserProfile.CURRENT_USER.getLevel()==0)
			return;
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, "cell 0 0,grow");
		splitPane.setResizeWeight(0.04);
		
		JPanel requestFieldpanel = new JPanel();
		requestFieldpanel.setBorder(new TitledBorder(null, "Request", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(requestFieldpanel);
		requestFieldpanel.setLayout(new MigLayout("", "[][][73.00][][][][grow][grow]", "[][][][][][213.00]"));
		
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
		
		textSerie = new JTextField();
		textSerie.setText(DEFAULT_SERIE_TEXT);
		textSerie.setFont(new Font("Tahoma", Font.ITALIC, 11));
		textSerie.setColumns(10);
		requestFieldpanel.add(textSerie, "cell 3 1,growx");

		pickerDateBegin = new JXDatePicker();
		pickerDateBegin.getEditor().setFont(new Font("Tahoma", Font.ITALIC, 11));
		pickerDateBegin.getEditor().setBorder(UIManager.getBorder("TextField.border"));
		pickerDateBegin.setDate(null);//Calendar.getInstance().getTime());
		pickerDateBegin.setFormats(new SimpleDateFormat("dd-MM-yyyy"));
		pickerDateBegin.getEditor().setText(DEFAULT_BEGIN_DATE);
		pickerDateBegin.getEditor().setColumns(10);
		requestFieldpanel.add(pickerDateBegin, "cell 4 1,growx");
		
		pickerDateEnd = new JXDatePicker();
		pickerDateEnd.getEditor().setFont(new Font("Tahoma", Font.ITALIC, 11));
		pickerDateEnd.getEditor().setBorder(UIManager.getBorder("TextField.border"));
		pickerDateEnd.setDate(null);//Calendar.getInstance().getTime());
		pickerDateEnd.setFormats(new SimpleDateFormat("dd-MM-yyyy"));
		pickerDateEnd.getEditor().setText(DEFAULT_END_DATE);
		pickerDateEnd.getEditor().setColumns(10);
		requestFieldpanel.add(pickerDateEnd, "cell 5 1,growx");
		
		comboBoxImageType = new JComboBox();
		comboBoxImageType.setModel(new DefaultComboBoxModel(IMAGE_TYPE.values()));
		comboBoxImageType.setSelectedIndex(1);
		requestFieldpanel.add(comboBoxImageType, "cell 6 1");
		
		txtPutCustomSql = new JTextField();
		txtPutCustomSql.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtPutCustomSql.setText("Put custom SQL request here");
		requestFieldpanel.add(txtPutCustomSql, "cell 0 3 6 1,growx");
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
		
		lblError = new JLabel("none");
		lblError.setForeground(Color.RED);
		lblError.setVisible(false);
		requestFieldpanel.add(lblError, "cell 0 5 6 1,growx");
		
		
		table = new JTable();
		table.setName("");
		rqModel = new RequestTableModel();
		table.setModel(rqModel);
		table.setAutoCreateRowSorter(true);
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
		textSerie.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				if(textSerie.getText().equals("")){
					textSerie.setText(DEFAULT_SERIE_TEXT);
					textSerie.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(textSerie.getText().equals(DEFAULT_SERIE_TEXT)){
					textSerie.setText("");
					textSerie.setFont(new Font("Tahoma", Font.PLAIN, 11));
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
				setWarning("");
				if(!txtPutCustomSql.getText().equals(DEFAULT_SQL_REQUEST_TEXT)){
					// On execute la requete custom
					GenericRequestDAO greq = new MySQLGenericRequestDAO();
					try {
						HashMap<String,ArrayList<String[]>> results = greq.executeSelect(txtPutCustomSql.getText());
						if(results.isEmpty()){
							getRqModel().setColumns(new String[]{"Nothing"});
							getRqModel().setData(new Object[][]{{"No results found"}});
							getRqModel().fireTableStructureChanged();
							return;
						}
						getRqModel().setColumns(results.keySet().toArray(new String[results.keySet().size()]));
						Object[][] data = null;
						int count = 0;
						File[] files = null;
						for(String header:results.keySet()){
							if(data==null){
								data = new Object[results.get(header).size()][results.keySet().size()];
								files = new File[results.keySet().size()];
							}
							ArrayList<String[]> cres = results.get(header);
							for(int j = 0; j < cres.size(); j++){
								data[j][count] =  cres.get(j)[0];
								if(cres.get(j)[1]!=null)
									files[count] = new File(cres.get(j)[1]);
								else
									files[count] = null;
							}
							count++;
						}
						getRqModel().setData(data);
						getRqModel().setFiles(files);
					} catch (SQLException e) {
						e.printStackTrace();
						setWarning("SQL Error : "+e.toString());
					} catch (exceptions.IllegalSQLRequest e) {
						e.printStackTrace();
						setWarning("Unsupported SQL command : "+e.toString());
					}
				}else{
					// requete 
					GenericRequestDAO greq = new MySQLGenericRequestDAO();
					String begin="";
					String end="";
					if(pickerDateBegin.getEditor().getText().equals(DEFAULT_BEGIN_DATE) || pickerDateBegin.getEditor().getText().equals("")){
						begin = "";
					}else{
						String[] begina = pickerDateBegin.getEditor().getText().split("-");
						begin = begina[2]+begina[1]+begina[0];
					}
					if(pickerDateEnd.getEditor().getText().equals(DEFAULT_END_DATE) || pickerDateEnd.getEditor().getText().equals("")){
						end = "";
					}else{
						String[] enda = pickerDateEnd.getEditor().getText().split("-");
						end = enda[2]+enda[1]+enda[0];
					}
					if(!end.equals("") && !begin.equals("") && (Integer.parseInt(end)-Integer.parseInt(begin)) < 0){
						setWarning("Begin date should be older than end date.");
						return;
					}
					if(txtPatient.getText().equals(DEFAULT_PATIENT_TEXT))
						txtPatient.setText("");
					if(txtProtocol.getText().equals(DEFAULT_PROTOCOL_TEXT))
						txtProtocol.setText("");
					if(textSerie.getText().equals(DEFAULT_SERIE_TEXT))
						textSerie.setText("");
					try {
						HashMap<String,ArrayList<String[]>> results = greq.executeFromRequestPanel((String)projectComboBox.getSelectedItem(),txtPatient.getText(),txtProtocol.getText(),textSerie.getText(),begin,end,(IMAGE_TYPE)comboBoxImageType.getSelectedItem());
						if(results.isEmpty()){
							getRqModel().setColumns(new String[]{"Nothing"});
							getRqModel().setData(new Object[][]{{"No results found"}});
							getRqModel().fireTableStructureChanged();
							return;
						}
						DBTables tab = SQLSettings.TABLES;
						String[] headerarray = new String[]{tab.getProject().TNAME,tab.getPatient().TNAME,tab.getAcquisitionDate().TNAME,tab.getProtocol().TNAME,tab.getSerie().TNAME};
						getRqModel().setColumns(headerarray);//results.keySet().toArray(new String[results.keySet().size()]));
						Object[][] data = null;
						int count = 0;
						File[] files = null;
						for(String header:headerarray){
							if(data==null){
								data = new Object[results.get(header).size()][results.keySet().size()];
								files = new File[results.keySet().size()];
							}
							ArrayList<String[]> cres = results.get(header);
							for(int j = 0; j < cres.size(); j++){
								data[j][count] =  cres.get(j)[0];
								if(cres.get(j)[1]!=null)
									files[count] = new File(cres.get(j)[1]);
								else
									files[count] = null;
							}
							count++;
						}
						getRqModel().setData(data);
						getRqModel().setFiles(files);
						txtPatient.setText(DEFAULT_PATIENT_TEXT);
						txtProtocol.setText(DEFAULT_PROTOCOL_TEXT);
						textSerie.setText(DEFAULT_SERIE_TEXT);
					} catch (SQLException e) {
						setWarning("SQL Error : "+e.toString().substring(0, Math.min(e.toString().length(), 100)));
						e.printStackTrace();
					}
				}
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

	public void setWarning(String txt){
		if(txt.equals("")){
			lblError.setText("");
			lblError.setVisible(false);
		}else{
			lblError.setText(txt);
			lblError.setVisible(true);
		}
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
    public void setFiles(File[] fi){
    	files = fi;
    }
}
