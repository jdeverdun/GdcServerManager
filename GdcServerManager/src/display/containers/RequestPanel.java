package display.containers;

import javax.swing.JPanel;

import model.Project;
import model.ServerInfo;
import net.miginfocom.swing.MigLayout;
import display.AdvancedImportFrame;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.DropMode;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.apache.commons.io.FileUtils;
import org.jdesktop.swingx.JXDatePicker;

import daemon.DecryptDaemon;
import daemon.NiftiDaemon;
import dao.GenericRequestDAO;
import dao.MySQLGenericRequestDAO;
import display.containers.viewer.ViewerPanel;
import es.vocali.util.AESCrypt;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;
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
	private boolean islock;// a t'on lock l'affichage
	private boolean continueAction;
	private GenericRequestDAO genericReqDAO ; // DAO pour lancer les requetes generiques
	private JSplitPane splitPane;
	private JTable table;
	private JTextField txtPutCustomSql;
	private JButton btnExecute;
	private JButton btnReset;
	private RequestTableModel rqModel;
	private JComboBox<String> projectComboBox;
	private JTextField txtPatient;
	private JTextField txtProtocol;
	private JXDatePicker pickerDateBegin;
	private JXDatePicker pickerDateEnd;
	private JLabel lblError;
	private JTextField textSerie;
	private JComboBox comboBoxImageType;
	private ProgressPanel progressPanel;
	private JPopupMenu Pmenu;
	private JMenuItem Mitem;
	private JMenuItem MAdvImportitem;
	private JMenuItem MDelitem;
	private JMenuItem MViewItem;
	private JButton btnCancel;
	
	public RequestPanel() {
		if(UserProfile.CURRENT_USER.getLevel()==0)
			return;
		genericReqDAO = new MySQLGenericRequestDAO();
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		islock=false;
		continueAction=true;
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, "cell 0 0,grow");
		splitPane.setResizeWeight(0.04);
		
		JPanel requestFieldpanel = new JPanel();
		requestFieldpanel.setBorder(new TitledBorder(null, "Request", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(requestFieldpanel);
		requestFieldpanel.setLayout(new MigLayout("", "[][][73.00][][][][grow][grow]", "[][][][][][][213.00][]"));
		
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
		//------ Menu -------
		Pmenu = new JPopupMenu();
		Mitem = new JMenuItem("Import");
		MAdvImportitem = new JMenuItem("Advanced import");
		MDelitem = new JMenuItem("Delete");
		MViewItem = new JMenuItem("View");
		Pmenu.add(Mitem);
		Pmenu.add(MAdvImportitem);
		Pmenu.add(MDelitem);
		Pmenu.add(MViewItem);
		MDelitem.setVisible(false);
		MViewItem.setVisible(false);
		// --------
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
		
		btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtPatient.setText(DEFAULT_PATIENT_TEXT);
				txtProtocol.setText(DEFAULT_PROTOCOL_TEXT);
				textSerie.setText(DEFAULT_SERIE_TEXT);
				pickerDateBegin.getEditor().setText(DEFAULT_BEGIN_DATE);
				pickerDateEnd.getEditor().setText(DEFAULT_END_DATE);
				txtPutCustomSql.setText(DEFAULT_SQL_REQUEST_TEXT);
				getRqModel().setColumns(new String[]{"Nothing"});
				getRqModel().setData(new Object[][]{{"No results found"}});
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						getRqModel().fireTableDataChanged();
						getRqModel().fireTableStructureChanged();
					}
				});
				setLock(false);
				progressPanel.setVisible(false);
			}
		});
		
		btnCancel = new JButton("Cancel");
		btnCancel.setEnabled(false);
		requestFieldpanel.add(btnCancel, "cell 0 4");
		requestFieldpanel.add(btnReset, "cell 0 4");
		
		txtProtocol = new JTextField();
		txtProtocol.setText(DEFAULT_PROTOCOL_TEXT);
		txtProtocol.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtProtocol.setColumns(10);
		requestFieldpanel.add(txtProtocol, "cell 2 1,growx");
		
		lblError = new JLabel("none");
		lblError.setForeground(Color.RED);
		lblError.setVisible(false);
		requestFieldpanel.add(lblError, "cell 0 6 6 1,growx");
		progressPanel = new ProgressPanel();
		progressPanel.setPreferredSize(new Dimension(160, 10));
		progressPanel.setVisible(false);
		requestFieldpanel.add(progressPanel, "cell 0 7");
		
		
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
		txtProtocol.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode()==KeyEvent.VK_ENTER)
					btnExecute.doClick();
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		txtPatient.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode()==KeyEvent.VK_ENTER)
					btnExecute.doClick();
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		textSerie.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode()==KeyEvent.VK_ENTER)
					btnExecute.doClick();
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		txtPutCustomSql.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode()==KeyEvent.VK_ENTER)
					btnExecute.doClick();
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
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
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				((MySQLGenericRequestDAO)genericReqDAO).setStopCurrentRequest(true);
			}
		});
		MDelitem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final File[] selectedFiles = new File[table.getSelectedRowCount()];
			    int[] indices = table.getSelectedRows();
			    int vmin = Integer.MAX_VALUE;
				for(int i = 0; i < indices.length; i++){
					int row = table.convertRowIndexToModel(indices[i]);
					selectedFiles[i] = getRqModel().getFileAt(row);				
				}

				// on delete
				setLock(true);
				final WaitingBarPanel ppanel = new WaitingBarPanel(RequestPanel.this); // mode creation de liens
				final String title = "Deleting ...";
				ppanel.setTitle(title);
				JFrame tmp = new JFrame();
				tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
				final Popup popup = PopupFactory.getSharedInstance().getPopup(RequestPanel.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
				tmp = null;
				// Thread pour la copie
				Thread delThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						for(File fi:selectedFiles){
							if(!continueAction){
								WindowManager.mwLogger.log(Level.INFO, "delThread cancelled.");
								break;
							}
							try {
								if((fi.isDirectory() && fi.getAbsolutePath().contains(ServerInfo.NRI_ANALYSE_NAME)) || new File(fi.getAbsolutePath().replace(ServerInfo.NRI_ANALYSE_NAME, ServerInfo.NRI_DICOM_NAME)).isDirectory()){
									WindowManager.MAINWINDOW.getFileTreeDist().deleServerFile(fi);
									WindowManager.MAINWINDOW.getFileTreeDist().deleServerFile(new File(fi.getAbsolutePath().replace(ServerInfo.NRI_ANALYSE_NAME, ServerInfo.NRI_DICOM_NAME)));
								}
								if((fi.isDirectory() && fi.getAbsolutePath().contains(ServerInfo.NRI_DICOM_NAME))  || new File(fi.getAbsolutePath().replace(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME)).isDirectory()){
									WindowManager.MAINWINDOW.getFileTreeDist().deleServerFile(fi);
									WindowManager.MAINWINDOW.getFileTreeDist().deleServerFile(new File(fi.getAbsolutePath().replace(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME)));
								}
								if(!fi.isDirectory() && new File(fi.toPath()+AESCrypt.ENCRYPTSUFFIX).exists()){
									WindowManager.MAINWINDOW.getFileTreeDist().deleServerFile(new File(fi.toPath()+AESCrypt.ENCRYPTSUFFIX));
									btnExecute.doClick();
								}
							} catch (Exception e) {
								WindowManager.mwLogger.log(Level.SEVERE, "delThread error.",e);
							}
						}
						popup.hide();
						setLock(false);
						// on met a jours le tableau
						btnExecute.doClick();
					}
					
				});
				ppanel.setPopup(popup);
				popup.show();
				
				delThread.start();	
				
				// On attend que tout se termine
				Thread updateStatusThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(islock){
							ppanel.setTitle(title);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				});
				updateStatusThread.start();
			}
		});
		MAdvImportitem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// on recupere la liste des fichiers selectionne
				File[] selectedFiles = new File[table.getSelectedRowCount()];
			    int[] indices = table.getSelectedRows();
			    int vmin = Integer.MAX_VALUE;
				for(int i = 0; i < indices.length; i++){
					int row = table.convertRowIndexToModel(indices[i]);
					selectedFiles[i] = getRqModel().getFileAt(row);	
				}
				AdvancedImportFrame adv = new AdvancedImportFrame(selectedFiles);
				adv.createAndShowGUI();
			}
		});
		MViewItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final File selectedFiles;
			    int[] indices = table.getSelectedRows();

				selectedFiles = getRqModel().getFileAt(indices[0]);				


				// on cherche les niftis
				setLock(true);
				final WaitingBarPanel ppanel = new WaitingBarPanel(RequestPanel.this); // mode creation de liens
				final String title = "Looking for niftis ...";
				ppanel.setTitle(title);
				JFrame tmp = new JFrame();
				tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
				final Popup popup = PopupFactory.getSharedInstance().getPopup(RequestPanel.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
				tmp = null;
				// Thread pour la copie
				Thread findThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(!selectedFiles.exists()){
							// si le fichier n'existe pas
							popup.hide();
							setLock(false);
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									JDialog.setDefaultLookAndFeelDecorated(true);
									JOptionPane.showMessageDialog(RequestPanel.this,
											"No file associated to this result (maybe wrong db ? [NRI-DICOM/ANALYSE])",
										    "File error",
										    JOptionPane.WARNING_MESSAGE);
								}
							});
							return;
						}
						final File[] niftis = findNiftiIn(selectedFiles);
						if(niftis==null || !continueAction){
							if(!continueAction)
								WindowManager.mwLogger.log(Level.INFO, "findThread cancelled.");
							popup.hide();
							setLock(false);
							return;
						}
						popup.hide();
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								JFrame tmp = new JFrame();
								ViewSelecterPanel vpanel = new ViewSelecterPanel(niftis); // mode creation de liens
								tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
								Popup popup2 = PopupFactory.getSharedInstance().getPopup(RequestPanel.this, vpanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
								vpanel.setPopupWindow(popup2);
								popup2.show();
								vpanel.checkToClose();
							}
						});
						
						setLock(false);
					}

					private File[] findNiftiIn(File selectedFiles) {
						if(selectedFiles==null)
							return null;
						File[] niftis;
						ArrayList<File> niftiList = new ArrayList<File>();
						if(!selectedFiles.isDirectory() && selectedFiles.getName().endsWith(".nii")){
							// si on a fait une requete sur un nifti directement (fichier termine par .nii car data issue de la base)
							niftis = new File[]{new File(selectedFiles.toString()+AESCrypt.ENCRYPTSUFFIX)};
							return niftis;
						}
							
						for(String p:selectedFiles.list()){
							if(!continueAction)
								return null;
							File lfile = new File(selectedFiles.toString()+File.separator+p);
							if(!lfile.isDirectory() && lfile.getName().endsWith(".nii"+AESCrypt.ENCRYPTSUFFIX)){
								niftiList.add(lfile);
							}else{
								if(lfile.isDirectory()){
									File[] tempnift = findNiftiIn(lfile);
									if(tempnift==null || tempnift.length==0)
										continue;
									for(File fi:tempnift){
										niftiList.add(fi);
									}
								}
							}
									
						}
						niftis = new File[niftiList.size()];
						niftis = niftiList.toArray(niftis);
						return niftis;
					}
					
				});
				ppanel.setPopup(popup);
				popup.show();
				
				findThread.start();	
				
				// On attend que tout se termine
				Thread updateStatusThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(islock){
							ppanel.setTitle(title);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				});
				updateStatusThread.start();
			}
		});
		Mitem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Select directory destination for import");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    //    
			    if (chooser.showOpenDialog(RequestPanel.this) == JFileChooser.APPROVE_OPTION) { 
			    	System.out.println("getCurrentDirectory(): " 
			    			+  chooser.getCurrentDirectory());
			    	System.out.println("getSelectedFile() : " 
			    			+  chooser.getSelectedFile());
			    }else {
			    	System.out.println("No Selection ");
			    	return;
			    }
			    final File dirsave = chooser.getSelectedFile();
			    final File[] selectedFiles = new File[table.getSelectedRowCount()];
			    int[] indices = table.getSelectedRows();
			    int vmin = Integer.MAX_VALUE;
				for(int i = 0; i < indices.length; i++){
					int row = table.convertRowIndexToModel(indices[i]);
					selectedFiles[i] = getRqModel().getFileAt(row);
					//System.out.println(selectedFiles[i].getAbsolutePath());
					if(selectedFiles[i].getAbsolutePath().split(Pattern.quote(File.separator)).length<vmin);
						vmin = selectedFiles[i].getAbsolutePath().split(Pattern.quote(File.separator)).length;
						
				}
				// on recupere l'indice du premier nom de repertoire qui differe
				String[] parts = selectedFiles[0].getAbsolutePath().split(Pattern.quote(File.separator));
				for(int i = 1;i < selectedFiles.length; i++){
					String[] parts_loc = selectedFiles[i].getAbsolutePath().split(Pattern.quote(File.separator));
					for(int j=0;j<vmin; j++){
						if(!parts[j].equals(parts_loc[j])){
							vmin=j;break;
						}
					}
				}
				final HashMap<File, String> fromTo = new HashMap<File, String>();
				for(File fi:selectedFiles){
					fromTo.put(fi, buildDirFor(fi,dirsave,vmin));
					
				}
				// on copie
				setLock(true);
				final WaitingBarPanel ppanel = new WaitingBarPanel(RequestPanel.this); // mode creation de liens
				final String title = "Import & Decrypt ...";
				ppanel.setTitle(title);
				JFrame tmp = new JFrame();
				tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
				final Popup popup = PopupFactory.getSharedInstance().getPopup(RequestPanel.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
				tmp = null;
				// Thread pour la copie
				Thread copyThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(!SystemSettings.DECRYPT_DAEMON.isAlive()){
							SystemSettings.DECRYPT_DAEMON = new DecryptDaemon();
							SystemSettings.DECRYPT_DAEMON.start();
						}
						for(File fi:selectedFiles){
							if(!continueAction){
								continueAction = true;
								return;
							}
							if(!fi.getName().contains("..")){
								
								FileManager.copyAndDecrypt(fi, new File(fromTo.get(fi)));
								String path = fi.getParent();
								String rootname = fi.getName();
								if(rootname.endsWith(".nii")){
									rootname = rootname.substring(0,rootname.lastIndexOf("."));//sans le .nii
									for(String suf:NiftiDaemon.suffixeToRemoveWithNifti){
										if(!continueAction){
											continueAction = true;
											return;
										}
										if(new File(path+File.separator+rootname+suf+AESCrypt.ENCRYPTSUFFIX).exists()){
											FileManager.copyAndDecrypt(new File(path+File.separator+rootname+suf+AESCrypt.ENCRYPTSUFFIX), new File(fromTo.get(fi)));
										}
									}	
								}
							}
						}
						while(!SystemSettings.DECRYPT_DAEMON.getFileToDecrypt().isEmpty() && continueAction){
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						continueAction = true;
						SystemSettings.DECRYPT_DAEMON.cleanList();
						popup.hide();
						setLock(false);
					}
					
				});
				ppanel.setPopup(popup);
				popup.show();
				
				copyThread.start();	
				
				// On attend que tout se termine
				Thread updateStatusThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(islock){
							ppanel.setTitle(title+"<br /><center>"+SystemSettings.DECRYPT_DAEMON.getFileToDecrypt().size()+" left</center>");
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				});
				updateStatusThread.start();
				
			}


		});
		table.addMouseListener(new MouseListener(){
			public void mouseReleased(MouseEvent Me){
				if(Me.isPopupTrigger() && table.getSelectedRowCount()>0 && getRqModel().getFileAt(0) != null){
					Pmenu.show(Me.getComponent(), Me.getX(), Me.getY());
				}
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
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
				setLock(true);
				progressPanel.setVisible(true);
				Thread tr = new Thread(new Runnable(){
					public void run(){
						if(!txtPutCustomSql.getText().equals(DEFAULT_SQL_REQUEST_TEXT)){
							// On execute la requete custom
							try {
								HashMap<String,ArrayList<String[]>> results = genericReqDAO.executeSelect(txtPutCustomSql.getText());
								if(results.isEmpty()){
									getRqModel().setColumns(new String[]{"Nothing"});
									getRqModel().fireTableStructureChanged();
									getRqModel().setData(new Object[][]{{"No results found"}});
									getRqModel().fireTableDataChanged();

									setLock(false);
									progressPanel.setVisible(false);
									return;
								}
								getRqModel().setColumns(results.keySet().toArray(new String[results.keySet().size()]));
								Object[][] data = null;
								int count = 0;
								File[] files = null;
								for(String header:results.keySet()){
									if(data==null){
										data = new Object[results.get(header).size()][results.keySet().size()];
										files = new File[results.get(header).size()];
									}
									ArrayList<String[]> cres = results.get(header);
									for(int j = 0; j < cres.size(); j++){
										data[j][count] =  cres.get(j)[0];
										if(cres.get(j)[1]!=null)
											files[j] = new File(cres.get(j)[1]);
										else
											files[j] = null;
									}
									count++;
								}
								final Object[][] cdata = data;
								final File[] cfiles = files;
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										getRqModel().setData(cdata);
										getRqModel().fireTableStructureChanged();
										getRqModel().setFiles(cfiles);
										
										getRqModel().fireTableDataChanged();
									}
								});
								
								
							} catch (SQLException e) {
								setWarning("SQL Error : "+e.toString());
								WindowManager.mwLogger.log(Level.WARNING, "btnExecute SQL error.",e);
							} catch (exceptions.IllegalSQLRequest e) {
								setWarning("Unsupported SQL command : "+e.toString());
								WindowManager.mwLogger.log(Level.WARNING, "btnExecute Unsupported SQL command.",e);
							} catch (Exception e) {
								setWarning("SQL Error : "+e.toString());
								WindowManager.mwLogger.log(Level.WARNING, "btnExecute error.",e);
							}
						}else{
							// requete 
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
								HashMap<String,ArrayList<String[]>> results = genericReqDAO.executeFromRequestPanel((String)projectComboBox.getSelectedItem(),txtPatient.getText(),txtProtocol.getText(),textSerie.getText(),begin,end,(IMAGE_TYPE)comboBoxImageType.getSelectedItem());
								// on replace les champs texte
								if(txtPatient.getText().equals(""))
									txtPatient.setText(DEFAULT_PATIENT_TEXT);
								if(txtProtocol.getText().equals(""))
									txtProtocol.setText(DEFAULT_PROTOCOL_TEXT);
								if(textSerie.getText().equals(""))
									textSerie.setText(DEFAULT_SERIE_TEXT);
								if(pickerDateEnd.getEditor().getText().equals(""))
									pickerDateEnd.getEditor().setText(DEFAULT_END_DATE);
								if(pickerDateBegin.getEditor().getText().equals(""))
									pickerDateBegin.getEditor().setText(DEFAULT_BEGIN_DATE);
								// on traite le resultat
								if(results.isEmpty()){
									SwingUtilities.invokeLater(new Runnable() {
										
										@Override
										public void run() {
											getRqModel().setColumns(new String[]{"Nothing"});
											getRqModel().fireTableStructureChanged();
											getRqModel().setData(new Object[][]{{"No results found"}});
											getRqModel().fireTableStructureChanged();
										}
									});
									
									setLock(false);
									progressPanel.setVisible(false);
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
										files = new File[results.get(header).size()];
									}
									ArrayList<String[]> cres = results.get(header);
									for(int j = 0; j < cres.size(); j++){
										data[j][count] =  cres.get(j)[0];
										if(cres.get(j)[1]!=null)
											files[j] = new File(cres.get(j)[1]);
										else
											files[j] = null;
									}
									count++;
								}
								final Object[][] cdata = data;
								final File[] cfiles = files;
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										getRqModel().setData(cdata);
										getRqModel().fireTableStructureChanged();
										getRqModel().setFiles(cfiles);
										
										getRqModel().fireTableDataChanged();
									}
								});
								/*getRqModel().setData(data);
								getRqModel().fireTableStructureChanged();
								getRqModel().setFiles(files);
								getRqModel().fireTableStructureChanged();*/
							} catch (SQLException e) {
								setWarning("SQL Error : "+e.toString().substring(0, Math.min(e.toString().length(), 100)));
								WindowManager.mwLogger.log(Level.WARNING, "btnExecute error.",e);
							}
						}
						setLock(false);
						progressPanel.setVisible(false);
						// sur EDT ?

						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								getRqModel().fireTableDataChanged();
								getRqModel().fireTableStructureChanged();
								if(getRqModel().getFileAt(0) == null){
									Mitem.setVisible(false);
									MDelitem.setVisible(false);
									MViewItem.setVisible(false);
									MAdvImportitem.setVisible(false);
								}else{
									Mitem.setVisible(true);
									MAdvImportitem.setVisible(true);
									if(UserProfile.CURRENT_USER.getLevel()==3)
										MDelitem.setVisible(true);
									if(getRqModel().getFileAt(0).getAbsolutePath().contains(SystemSettings.SERVER_INFO.NRI_ANALYSE_NAME) && isSerie(getRqModel().getFileAt(0)))
										MViewItem.setVisible(true);
									else
										MViewItem.setVisible(false);
									
								}
							}
						});

						
						System.gc();
					}
				});
				tr.start();
			}
		});
	}

	public void setLock(boolean b){
		btnExecute.setEnabled(!b);
		table.setEnabled(!b);
		txtPatient.setEnabled(!b);
		txtProtocol.setEnabled(!b);
		txtPutCustomSql.setEnabled(!b);
		textSerie.setEnabled(!b);
		comboBoxImageType.setEnabled(!b);
		projectComboBox.setEnabled(!b);
		btnReset.setEnabled(!b);
		pickerDateBegin.setEnabled(!b);
		pickerDateEnd.setEnabled(!b);
		btnCancel.setEnabled(b);
		islock=b;
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

	public void terminateAction() {
		continueAction = false;
		setLock(false);
	}
	
	/**
	 * Verifie que le fichier pointe bien vers une serie sur le serveur
	 * @param file
	 * @return
	 */
	private boolean isSerie(File file) {
		String[] parts = file.getAbsolutePath().split(Pattern.quote(File.separator));
		int serverdirlen = (SystemSettings.SERVER_INFO.getServerDir().toString().split(Pattern.quote(File.separator))).length +1;// +1 pour NRI-ANALYSE et NRI-DICOM
		if(parts.length==(serverdirlen)) 
			return false;
		if(!file.getName().contains("..")){
			
			int count = 0;
			for(int i = serverdirlen;i <parts.length;i++){
				if(!parts[i].isEmpty()){
					count++;
				}else{
					return false;
				}
			}
			return count == 5;
		}
		return false;
	}
	/**
	 * Construit l'arborescende de fi dans dirsave a partir du repertoire vmin de fi
	 * renvoi  le path 
	 * @param fi
	 * @param dirsave
	 * @param vmin
	 * @return 
	 */
	private String buildDirFor(File fi, File dirsave, int vmin) {
		String[] parts = fi.getAbsolutePath().split(Pattern.quote(File.separator));
		String path = dirsave.getAbsolutePath();
		String bpath = path;
		for(int i = vmin ; i < parts.length; i++){
			path +=  File.separator + parts[i];
			if(i>vmin)
				bpath += File.separator + parts[i-1];
			
		}
		File tmp = new File(path);
		tmp.mkdirs();
		return bpath;
	}
	
}



class RequestTableModel extends AbstractTableModel {
	private File[] files;
	private static final String[] DEFAULT_COLUMN_NAME =  {"Project", "Patient", "Date", "Protocol", "Serie"};
	private String[] columnNames = {"Project", "Patient", "AcquisitionDate", "Protocol", "Serie"};
    private Object[][] data = {
    {"", "", "", "", ""}
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

    public File getFileAt(int row){
    	if(files==null)
    		return null;
    	return files[row];
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
