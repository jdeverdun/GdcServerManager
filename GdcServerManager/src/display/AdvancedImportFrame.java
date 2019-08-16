package display;

import ij.gui.ProgressBar;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import daemon.DecryptDaemon;
import daemon.NiftiDaemon;
import daemon.tools.ThreadPool;
import display.containers.FileManager;
import display.containers.ProgressPanel;
import display.containers.RequestPanel;
import display.containers.WaitingBarPanel;
import display.containers.viewer.ViewerPanel;
import es.vocali.util.AESCrypt;

import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;

import model.AcquisitionDate;
import model.BIDS_server;
import model.ServerInfo;
import net.miginfocom.swing.MigLayout;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Dimension;
import javax.swing.JCheckBox;
import java.awt.ComponentOrientation;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JSeparator;

/**
 * Classe permettant d'importer depuis le request panel des donnees vers un repertoire local
 * en selectionnant precisement la structure voulu en sortie
 * @author DEVERDUN Jeremy
 *
 */
public class AdvancedImportFrame extends JFrame {
	
	private static final String DEFAULT_NPROJECT_TEXT = "New Project Name [Optionnal]";
	private static final String DEFAULT_NPATIENT_TEXT = "New Patient Name [Optionnal]";
	private static final String DEFAULT_NDATE_TEXT = "New Date [Optionnal]";
	private static final String DEFAULT_NPROTOCOL_TEXT = "New Protocol Name [Optionnal]";
	


	// liste des fichiers a importer
	private File[] files;
	private String newProjectName;
	private String newPatientName;
	private String newProtocolName;
	private String newDate;
	
	
	private BIDS_server bidsServer;
	
	// permet d'eviter de reparcourir l'arborescence pour rien
	// si il y a un reciproque au niveau des repertoire selectionnee (NRI-DICOM vs NRI-ANALYSE et vice versa)
	// -1 on a pas teste // 0 pas de correspondance dicom // 1 une correspondance 
	private int hasNiftiDicomCorrespondance = -1;
	private File[] niftiFiles;
	private File[] dicomFiles;
	// variable permettant de stopper l'import
	private boolean stopImport;
	private boolean isLock; // permet de savoir si l'interface est lock
	
	private JTextField txtOutputDirectory;
	private JTextField txtProjectname;
	private JButton btnSelectdicomdir;
	private JLabel lblForceProjectName;
	private JLabel lblFolderStructure;
	private JButton btnImport ;
	private JButton btnClose;
	private JCheckBox chckbxProject;
	private JCheckBox checkBoxPatient;
	private JCheckBox checkBoxDate;
	private JCheckBox chckbxProtocol;
	private JCheckBox checkBoxSerie;
	private JList<String> list;
	private Component verticalGlue;
	private JLabel lblForcePatientName;
	private JTextField txtNewPatientName;
	private JLabel lblForceDate;
	private JTextField txtNewDateoptionnal;
	private JLabel lblForceProtocolName;
	private JTextField txtNewProtocolName;
	private JSeparator separator;
	private JSeparator separator_1;
	private JLabel lblNodeForImport;
	private JRadioButton rdbtnDicomNode;
	private JRadioButton rdbtnNiftiNode;
	private ProgressPanel progressBar;
	private JCheckBox chckbxSafeMode;
	private JCheckBox chckBoxBIDS;

	
	public AdvancedImportFrame(File[] files) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setFiles(files);
		setNewProjectName(null);
		setNewDate(null);
		setNewPatientName(null);
		setNewProtocolName(null);
		hasNiftiDicomCorrespondance = -1;
		stopImport = false;
		bidsServer = new BIDS_server();
		ImageIcon icon=new ImageIcon(MainWindow.class.getResource("/images/folder.png"));
		Image img = icon.getImage();  
		Image newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		ImageIcon icon2 = new ImageIcon(newimg); 
		
		
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[grow][grow][53px]", "[][29px,grow,center][grow]"));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Output directory", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_2, "cell 0 0 3 1,grow");
		panel_2.setLayout(new MigLayout("", "[grow][]", "[][]"));
		String dir = SystemSettings.APP_DIR.toString();
		if(UserProfile.LAST_SELECTED_DIR!=null)
			dir = UserProfile.LAST_SELECTED_DIR.toString();
		
		txtOutputDirectory = new JTextField(dir);
		panel_2.add(txtOutputDirectory, "cell 0 0,growx");
		
		btnSelectdicomdir = new JButton(icon2);
		panel_2.add(btnSelectdicomdir, "cell 1 0");
		btnSelectdicomdir.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFileChooser fc = new JFileChooser(txtOutputDirectory.getText());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = fc.showOpenDialog(AdvancedImportFrame.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	File file = fc.getSelectedFile();
	            	txtOutputDirectory.setText(file.getAbsolutePath());
	            	UserProfile.LAST_SELECTED_DIR = file.toPath();
	            	updateList();
	            }
			}
		});
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Import Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_1, "cell 0 1 3 2,grow");
		panel_1.setLayout(new MigLayout("", "[grow][grow]", "[][][][][][][][][][][15px][grow][]"));
		
		lblNodeForImport = new JLabel("Node for import");
		lblNodeForImport.setToolTipText("Attempt to import data from either NRI-DICOM or NRI-ANALYSE");
		panel_1.add(lblNodeForImport, "cell 0 0");
		
		rdbtnDicomNode = new JRadioButton("Dicom");
		rdbtnNiftiNode = new JRadioButton("Nifti");
		if(getFiles()[0].getAbsolutePath().contains(ServerInfo.NRI_DICOM_NAME))
			rdbtnDicomNode.setSelected(true);
		else
			rdbtnNiftiNode.setSelected(true);
		panel_1.add(rdbtnDicomNode, "flowx,cell 1 0");
		
		lblForceProjectName = new JLabel("Force project name");
		panel_1.add(lblForceProjectName, "cell 0 2,alignx left");
		
		txtProjectname = new JTextField();
		txtProjectname.setText(DEFAULT_NPROJECT_TEXT);
		txtProjectname.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel_1.add(txtProjectname, "cell 1 2,growx");
		txtProjectname.setColumns(10);
		
		lblForcePatientName = new JLabel("Force patient name");
		panel_1.add(lblForcePatientName, "cell 0 3,alignx left");
		
		txtNewPatientName = new JTextField();
		txtNewPatientName.setText(DEFAULT_NPATIENT_TEXT);
		txtNewPatientName.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtNewPatientName.setColumns(10);
		panel_1.add(txtNewPatientName, "cell 1 3,growx");
		
		lblForceDate = new JLabel("Force date");
		panel_1.add(lblForceDate, "cell 0 4,alignx left");
		
		txtNewDateoptionnal = new JTextField();
		txtNewDateoptionnal.setText(DEFAULT_NDATE_TEXT);
		txtNewDateoptionnal.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtNewDateoptionnal.setColumns(10);
		panel_1.add(txtNewDateoptionnal, "cell 1 4,growx");
		
		lblForceProtocolName = new JLabel("Force Protocol name");
		panel_1.add(lblForceProtocolName, "cell 0 5,alignx trailing");
		
		txtNewProtocolName = new JTextField();
		txtNewProtocolName.setText(DEFAULT_NPROTOCOL_TEXT);
		txtNewProtocolName.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtNewProtocolName.setColumns(10);
		panel_1.add(txtNewProtocolName, "cell 1 5,growx");
		
		separator_1 = new JSeparator();
		panel_1.add(separator_1, "cell 0 6 2 1,growx");
		
		lblFolderStructure = new JLabel("Folder structure");
		panel_1.add(lblFolderStructure, "cell 0 7,alignx left");
		
		chckbxProject = new JCheckBox("Project");
		panel_1.add(chckbxProject, "flowx,cell 1 7,alignx left");
		
		checkBoxPatient = new JCheckBox("Patient");
		panel_1.add(checkBoxPatient, "cell 1 7,alignx left");
		
		checkBoxDate = new JCheckBox("Date");
		panel_1.add(checkBoxDate, "cell 1 7,alignx left");
		
		chckbxProtocol = new JCheckBox("Protocol");
		chckbxProtocol.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		panel_1.add(chckbxProtocol, "cell 1 7,alignx left");
		checkBoxSerie = new JCheckBox("Serie");
		checkBoxSerie.setEnabled(false);
		checkBoxSerie.setSelected(true);
		panel_1.add(checkBoxSerie, "cell 1 7,alignx left");
		DefaultListModel<String> model = new DefaultListModel<String>();  
		list = new JList<String>();
		list.setModel(model);
		updateList();
		
		chckbxSafeMode = new JCheckBox("Safe mode");
		panel_1.add(chckbxSafeMode, "cell 0 8");
		
		chckBoxBIDS = new JCheckBox("BIDS");
		panel_1.add(chckBoxBIDS, "cell 1 8");
		
		separator = new JSeparator();
		panel_1.add(separator, "cell 0 9 2 1,growx");
		
		verticalGlue = Box.createVerticalGlue();
		verticalGlue.setPreferredSize(new Dimension(20, 0));
		panel_1.add(verticalGlue, "cell 0 10,grow");
		panel_1.add(new JScrollPane(list), "cell 0 11 2 2,grow");
		
		
		panel_1.add(rdbtnNiftiNode, "cell 1 0");
		
		JPanel panelSaveClose = new JPanel();
		getContentPane().add(panelSaveClose, BorderLayout.SOUTH);
		panelSaveClose.setLayout(new MigLayout("", "[7.00,grow,fill][grow,fill]", "[][]"));
		
		btnImport = new JButton("Import");
		panelSaveClose.add(btnImport, "cell 0 0,growx");
		
		btnClose = new JButton("Close");
		panelSaveClose.add(btnClose, "cell 1 0,growx");
		progressBar = new ProgressPanel();
		progressBar.setPreferredSize(new Dimension(150, 10));
		progressBar.setVisible(false);
		panelSaveClose.add(progressBar, "cell 0 1,alignx left");
		chckbxSafeMode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(chckbxSafeMode.isSelected()){
					ThreadPool.resetCores();
					ThreadPool.AVAILABLE_CORES = 1;
					ThreadPool.launchThreadPool(1);
				}else{
					ThreadPool.resetCores();
				}
			}
		});
		txtProjectname.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtProjectname.getText().equals("")){
					txtProjectname.setText(DEFAULT_NPROJECT_TEXT);
					txtProjectname.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtProjectname.getText().equals(DEFAULT_NPROJECT_TEXT)){
					txtProjectname.setText("");
					txtProjectname.setFont(new Font("Tahoma", Font.PLAIN, 11));
				}
			}
		});
		txtProjectname.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
				if(!txtProjectname.getText().equals("") && !txtProjectname.getText().equals(DEFAULT_NPROJECT_TEXT))
					setNewProjectName(txtProjectname.getText());
				else
					setNewProjectName(null);
				updateList();
			  }
			  public void removeUpdate(DocumentEvent e) {
				if(!txtProjectname.getText().equals("") && !txtProjectname.getText().equals(DEFAULT_NPROJECT_TEXT))
					setNewProjectName(txtProjectname.getText());
				else
					setNewProjectName(null);
				updateList();
			  }
			  public void insertUpdate(DocumentEvent e) {
				if(!txtProjectname.getText().equals("") && !txtProjectname.getText().equals(DEFAULT_NPROJECT_TEXT))
					setNewProjectName(txtProjectname.getText());
				else
					setNewProjectName(null);
				updateList();
			  }
		});
		txtNewPatientName.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtNewPatientName.getText().equals("")){
					txtNewPatientName.setText(DEFAULT_NPATIENT_TEXT);
					txtNewPatientName.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtNewPatientName.getText().equals(DEFAULT_NPATIENT_TEXT)){
					txtNewPatientName.setText("");
					txtNewPatientName.setFont(new Font("Tahoma", Font.PLAIN, 11));
				}
			}
		});
		txtNewPatientName.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
				if(!txtNewPatientName.getText().equals("") && !txtNewPatientName.getText().equals(DEFAULT_NPATIENT_TEXT))
					setNewPatientName(txtNewPatientName.getText());
				else
					setNewPatientName(null);
				updateList();
			  }
			  public void removeUpdate(DocumentEvent e) {
				if(!txtNewPatientName.getText().equals("") && !txtNewPatientName.getText().equals(DEFAULT_NPATIENT_TEXT))
					setNewPatientName(txtNewPatientName.getText());
				else
					setNewPatientName(null);
				updateList();
			  }
			  public void insertUpdate(DocumentEvent e) {
				if(!txtNewPatientName.getText().equals("") && !txtNewPatientName.getText().equals(DEFAULT_NPATIENT_TEXT))
					setNewPatientName(txtNewPatientName.getText());
				else
					setNewPatientName(null);
				updateList();
			  }
		});
		txtNewDateoptionnal.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtNewDateoptionnal.getText().equals("")){
					txtNewDateoptionnal.setText(DEFAULT_NDATE_TEXT);
					txtNewDateoptionnal.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtNewDateoptionnal.getText().equals(DEFAULT_NDATE_TEXT)){
					txtNewDateoptionnal.setText("");
					txtNewDateoptionnal.setFont(new Font("Tahoma", Font.PLAIN, 11));
				}
			}
		});
		txtNewDateoptionnal.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				if(!txtNewDateoptionnal.getText().equals("") && !txtNewDateoptionnal.getText().equals(DEFAULT_NDATE_TEXT))
					setNewDate(txtNewDateoptionnal.getText());
				else
					setNewDate(null);
				updateList();
			}
			public void removeUpdate(DocumentEvent e) {
				if(!txtNewDateoptionnal.getText().equals("") && !txtNewDateoptionnal.getText().equals(DEFAULT_NDATE_TEXT))
					setNewDate(txtNewDateoptionnal.getText());
				else
					setNewDate(null);
				updateList();
			}
			public void insertUpdate(DocumentEvent e) {
				if(!txtNewDateoptionnal.getText().equals("") && !txtNewDateoptionnal.getText().equals(DEFAULT_NDATE_TEXT))
					setNewDate(txtNewDateoptionnal.getText());
				else
					setNewDate(null);
				updateList();
			}
		});
		txtNewProtocolName.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtNewProtocolName.getText().equals("")){
					txtNewProtocolName.setText(DEFAULT_NPROTOCOL_TEXT);
					txtNewProtocolName.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtNewProtocolName.getText().equals(DEFAULT_NPROTOCOL_TEXT)){
					txtNewProtocolName.setText("");
					txtNewProtocolName.setFont(new Font("Tahoma", Font.PLAIN, 11));
				}
			}
		});
		txtNewProtocolName.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				if(!txtNewProtocolName.getText().equals("") && !txtNewProtocolName.getText().equals(DEFAULT_NPROTOCOL_TEXT))
					setNewProtocolName(txtNewProtocolName.getText());
				else
					setNewProtocolName(null);
				updateList();
			}
			public void removeUpdate(DocumentEvent e) {
				if(!txtNewProtocolName.getText().equals("") && !txtNewProtocolName.getText().equals(DEFAULT_NPROTOCOL_TEXT))
					setNewProtocolName(txtNewProtocolName.getText());
				else
					setNewProtocolName(null);
				updateList();
			}
			public void insertUpdate(DocumentEvent e) {
				if(!txtNewProtocolName.getText().equals("") && !txtNewProtocolName.getText().equals(DEFAULT_NPROTOCOL_TEXT))
					setNewProtocolName(txtNewProtocolName.getText());
				else
					setNewProtocolName(null);
				updateList();
			}
		});
		txtOutputDirectory.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
				updateList();
			  }
			  public void removeUpdate(DocumentEvent e) {
				updateList();
			  }
			  public void insertUpdate(DocumentEvent e) {
				updateList();
			  }
		});
		btnClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		btnImport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(txtOutputDirectory.getText().equals("")){
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(AdvancedImportFrame.this,
								    "Please select an output directory",
								    "Warning",
								    JOptionPane.WARNING_MESSAGE);
						}
					});
					return;
				}
				if(!new File(txtOutputDirectory.getText()).exists()){
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(AdvancedImportFrame.this,
								    "Output directory does not exist",
								    "Warning",
								    JOptionPane.WARNING_MESSAGE);
						}
					});
					return;
				}
				
				// Si tout est ok on lance l'import
				if(chckBoxBIDS.isSelected()) {
					if(!bidsServer.connect()) {
						System.out.println("Pas connecte desole");
						return;
					}
					bidsServer.sendText("coucou");
					bidsServer.disconnect();
				}
				UserProfile.LAST_SELECTED_DIR = Paths.get(txtOutputDirectory.getText());
				final File[] selectedFiles = getFiles();
			    String[] toFiles = getOutputFilesAsStringArray();
				final HashMap<File, String> fromTo = new HashMap<File, String>();
				for(int i =0; i<selectedFiles.length;i++){
					fromTo.put(selectedFiles[i], toFiles[i]);
					
				}
				// on copie
				setLock(true);
				final WaitingBarPanel ppanel = new WaitingBarPanel(AdvancedImportFrame.this); // mode creation de liens
				final String title = "Import & Decrypt ...";
				ppanel.setTitle(title);
				JFrame tmp = new JFrame();
				tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
				final Popup popup = PopupFactory.getSharedInstance().getPopup(AdvancedImportFrame.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
				tmp = null;
				resetImportStatus();
				// Thread pour la copie
				Thread importThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(!SystemSettings.DECRYPT_DAEMON.isAlive()){
							SystemSettings.DECRYPT_DAEMON = new DecryptDaemon();
							SystemSettings.DECRYPT_DAEMON.start();
						}else{
							SystemSettings.DECRYPT_DAEMON.setCrashed(false, "");
						}
						for(File fi:selectedFiles){
							if(stopImport){
								stopImport = false;
								popup.hide();
								setLock(false);
								return;
							}
							if(!fi.getName().contains("..")){
								WindowManager.mwLogger.log(Level.INFO,"Decrypt "+fi.getAbsolutePath()+" to "+fromTo.get(fi));
								
								// on prend le parent car on va automatiquement rajouter la serie dans le copyAndDecrypt
								FileManager.copyAndDecrypt(fi, new File(fromTo.get(fi)).getParentFile());
								String path = fi.getParent();
								String rootname = fi.getName();
								if(rootname.endsWith(".nii")){
									rootname = rootname.substring(0,rootname.lastIndexOf("."));//sans le .nii
									for(String suf:NiftiDaemon.suffixeToRemoveWithNifti){
										if(stopImport){
											stopImport = false;
											popup.hide();
											setLock(false);
											return;
										}
										if(new File(path+File.separator+rootname+suf+AESCrypt.ENCRYPTSUFFIX).exists()){
											FileManager.copyAndDecrypt(new File(path+File.separator+rootname+suf+AESCrypt.ENCRYPTSUFFIX), new File(fromTo.get(fi)));
										}
									}	
								}
							}
						}
						while(!SystemSettings.DECRYPT_DAEMON.isWaiting() && !stopImport){
							// si erreur durant l'import
							if(SystemSettings.DECRYPT_DAEMON.isCrashed()){
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										JDialog.setDefaultLookAndFeelDecorated(true);
										JOptionPane.showMessageDialog(AdvancedImportFrame.this,
											    "Erreur durant l'import : "+SystemSettings.DECRYPT_DAEMON.getCrashmsg(),
											    "Import error",
											    JOptionPane.ERROR_MESSAGE);
										SystemSettings.DECRYPT_DAEMON.setCrashed(false, "");
									}
								});
								stopImport = true;
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						SystemSettings.DECRYPT_DAEMON.cleanList();
						popup.hide();
						setLock(false);
						//if(!stopImport)
							//dispose();
						stopImport = false;
					}
					
				});
				ppanel.setPopup(popup);
				popup.show();
				
				importThread.start();	
				
				// On attend que tout se termine
				Thread updateStatusThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(isLock){
							ppanel.setTitle(title+"<br /><center>"+(SystemSettings.DECRYPT_DAEMON.getDoneEncryptedFile())+" / "+SystemSettings.DECRYPT_DAEMON.getTotalEncryptedFile()+"</center>");
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
		
		chckbxProject.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateList();
			}
		});
		chckbxProtocol.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateList();
			}
		});
		checkBoxDate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateList();
			}
		});
		
		checkBoxPatient.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateList();
			}
		});
		
		checkBoxSerie.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateList();
			}
		});
		chckBoxBIDS.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(chckBoxBIDS.isSelected()) {
					rdbtnDicomNode.setSelected(true);
					rdbtnNiftiNode.setSelected(false);
				}
				
			}
		});
		
		rdbtnDicomNode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!rdbtnNiftiNode.isSelected()){
					rdbtnDicomNode.setSelected(true);
					//return;
				}
				rdbtnNiftiNode.setSelected(!rdbtnDicomNode.isSelected());
				progressBar.setVisible(false);
				if(hasNiftiDicomCorrespondance == -1){// si on a pas encore checke la correspondance
					// on verifie que tout les fichiers dans file ont leur correspondance dans
					// NRI-DICOM
					Thread tr = new Thread(new Runnable() {
						
						@Override
						public void run() {
							rdbtnNiftiNode.setEnabled(false);
							progressBar.setVisible(true);
							File[] nfiles = new File[getFiles().length];
							int count = 0;
							for(File fi:getFiles()){
								File nfi = new File(fi.getAbsolutePath().replaceAll(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME));
								if(!nfi.exists()){
									rdbtnDicomNode.setSelected(true);
									rdbtnNiftiNode.setSelected(false);
									rdbtnDicomNode.setEnabled(true);
									rdbtnNiftiNode.setEnabled(false);
									progressBar.setVisible(false);
									hasNiftiDicomCorrespondance = 0;
									return;
								}
								nfiles[count++] = nfi;
							}
							setDicomFiles(getFiles());
							setNiftiFiles(nfiles);
							hasNiftiDicomCorrespondance = 1;
							rdbtnDicomNode.setEnabled(true);
							rdbtnNiftiNode.setEnabled(true);
							progressBar.setVisible(false);
						}
					});
					tr.start();
				}else{
					setFiles(getDicomFiles());
				}
			}
		});
		rdbtnNiftiNode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!rdbtnDicomNode.isSelected()){
					rdbtnNiftiNode.setSelected(true);
					//return;
				}
				progressBar.setVisible(false);
				rdbtnDicomNode.setSelected(!rdbtnNiftiNode.isSelected());
				if(hasNiftiDicomCorrespondance==-1){// si on a pas encore checke la correspondance
					// on verifie que tout les fichiers dans file ont leur correspondance dans
					// NRI-ANALYSE
					Thread tr = new Thread(new Runnable() {
						
						@Override
						public void run() {
							rdbtnDicomNode.setEnabled(false);
							progressBar.setVisible(true);
							File[] nfiles = new File[getFiles().length];
							int count = 0;
							for(File fi:getFiles()){
								File nfi = new File(fi.getAbsolutePath().replaceAll(ServerInfo.NRI_ANALYSE_NAME,ServerInfo.NRI_DICOM_NAME));
								if(!nfi.exists()){
									rdbtnDicomNode.setSelected(false);
									rdbtnNiftiNode.setSelected(true);
									rdbtnDicomNode.setEnabled(false);
									rdbtnNiftiNode.setEnabled(true);
									progressBar.setVisible(false);
									hasNiftiDicomCorrespondance = 0;
									
									return;
								}
								nfiles[count++] = nfi;
							}
							setDicomFiles(nfiles);
							setNiftiFiles(getFiles());
							hasNiftiDicomCorrespondance = 1;
							rdbtnDicomNode.setEnabled(true);
							rdbtnNiftiNode.setEnabled(true);
							progressBar.setVisible(false);
						}
					});
					tr.start();
				}else{
					setFiles(getNiftiFiles());
				}
			}
		});
		if(getFiles()[0].getAbsolutePath().contains(ServerInfo.NRI_DICOM_NAME))
			rdbtnDicomNode.doClick();
		else
			rdbtnNiftiNode.doClick();
		
	}


	/**
	 * Verrouille la frame
	 * @param b
	 */
	protected void setLock(boolean b) {
		isLock = b;
		txtOutputDirectory.setEnabled(!b);
		txtProjectname.setEnabled(!b);
		btnSelectdicomdir.setEnabled(!b);
		lblForceProjectName.setEnabled(!b);
		lblFolderStructure.setEnabled(!b);
		btnImport.setEnabled(!b);
		btnClose.setEnabled(!b);
		list.setEnabled(!b);
		chckbxProject.setEnabled(!b);
		lblFolderStructure.setEnabled(!b);
		chckbxProtocol.setEnabled(!b);
		checkBoxDate.setEnabled(!b);
		checkBoxPatient.setEnabled(!b);
		txtNewDateoptionnal.setEnabled(!b);
		txtNewPatientName.setEnabled(!b);
		txtNewProtocolName.setEnabled(!b);
		lblForceDate.setEnabled(!b);
		lblForcePatientName.setEnabled(!b);
		lblForceProtocolName.setEnabled(!b);
		lblNodeForImport.setEnabled(!b);
		rdbtnDicomNode.setEnabled(!b);
		rdbtnNiftiNode.setEnabled(!b);
		chckbxSafeMode.setEnabled(!b);
	}

	/**
	 * @return the newProjectName
	 */
	public String getNewProjectName() {
		return newProjectName;
	}


	/**
	 * @param newProjectName the newProjectName to set
	 */
	public void setNewProjectName(String newProjectName) {
		this.newProjectName = newProjectName;
	}


	/**
	 * @return the newPatientName
	 */
	public String getNewPatientName() {
		return newPatientName;
	}


	/**
	 * @param newPatientName the newPatientName to set
	 */
	public void setNewPatientName(String newPatientName) {
		this.newPatientName = newPatientName;
	}


	/**
	 * @return the newProtocolName
	 */
	public String getNewProtocolName() {
		return newProtocolName;
	}


	/**
	 * @param newProtocolName the newProtocolName to set
	 */
	public void setNewProtocolName(String newProtocolName) {
		this.newProtocolName = newProtocolName;
	}


	/**
	 * @return the newDate
	 */
	public String getNewDate() {
		return newDate;
	}


	/**
	 * @param newDate the newDate to set
	 */
	public void setNewDate(String newDate) {
		this.newDate = newDate;
	}


	public void createAndShowGUI(){
		JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
        } catch (Exception e) {
          System.out.println("Substance Graphite failed to initialize");
        }
		UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
		setTitle("Advanced import (from server to local)");
		setSize(490, 600);
		if(SystemSettings.isMac())
			this.pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon(this.getClass().getResource("/images/logo32.png")).getImage());
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Permet de stopper de l'import de donnees vers le serveur
	 */
	public void stopImport() {
		this.stopImport = true;
		FileManager.stopAction();
	}
	
	/**
	 * Reset le statut de l'import (entre autre le stop action dans le fileManager)
	 */
	public void resetImportStatus() {
		this.stopImport = false;
		FileManager.setStopAction(false);
	}
	
	/**
	 * @return the files
	 */
	public File[] getFiles() {
		return files;
	}


	/**
	 * @param files the files to set
	 */
	public void setFiles(File[] files) {
		this.files = files;
	}

	/**
	 * @return the niftiFiles
	 */
	public File[] getNiftiFiles() {
		return niftiFiles;
	}


	/**
	 * @param niftiFiles the niftiFiles to set
	 */
	public void setNiftiFiles(File[] niftiFiles) {
		this.niftiFiles = niftiFiles;
	}


	/**
	 * @return the dicomFiles
	 */
	public File[] getDicomFiles() {
		return dicomFiles;
	}


	/**
	 * @param dicomFiles the dicomFiles to set
	 */
	public void setDicomFiles(File[] dicomFiles) {
		this.dicomFiles = dicomFiles;
	}


	private void updateList()  
    {  
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
		        DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();  
		        String[] values = getOutputFilesAsStringArray();
		        model.removeAllElements();  
		        for(String s:values)  
		        {   
		            model.addElement(s);  
		        }  
			}
		});

    }
	
	
	/**
	 * Renvoi sous forme d'une string array les chemins vers les fichiers de sortie
	 * en prenant en compte les checkbox coche dans l'interface et l'ouputdir
	 * @return
	 */
	public String[] getOutputFilesAsStringArray() {
		if(files==null || files.length==0)
			return new String[]{};
		String[] list = new String[files.length];
		int count=0;
		for(File fi:files){
			try {
				list[count++] = formatOuput(fi.getAbsolutePath());
			} catch (Exception e) {
				WindowManager.mwLogger.log(Level.WARNING,"Error while formatting output files for the import [getOutputFilesAsStringArray] with "+fi.getAbsolutePath(),e);
			}
		}
		return list;
	}
	
	/**
	 * Formatte un chemin pour le convertir de chemin sur le serveur en chemin vers la machine locale 
	 * en prenant en compte tout les item selectionnee dans l'interface
	 * ex : J:/NRI_DICOM/CRESCENDO/bernard/10212011/serie/prot
	 * devient : c:/users/analyse/bernard/10212011/serie/prot   si tout est coche sauf le projet
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private String formatOuput(String path) throws Exception {
		File fi = new File(path);
		String[] parts = path.split(Pattern.quote(File.separator));
		int serverdirlen = (SystemSettings.SERVER_INFO.getServerDir().toString().split(Pattern.quote(File.separator))).length +1;// +1 pour NRI-ANALYSE et NRI-DICOM
		if(parts.length==(serverdirlen)) 
			return "Unknown";
		if(!fi.getName().contains("..")){
			
			int count = 0;
			for(int i = serverdirlen;i <parts.length;i++){
				if(!parts[i].isEmpty()){
					count++;
				}else{
					throw new Exception("Error with file path structure.");
				}
			}
			// on s'assure de la structure
			String project = "";
			String patient = "";
			String acqdate = "";
			String protocol = "";
			String serie = "";
			switch(count){
			case 1://project
				chckbxProject.setSelected(true);
				checkBoxPatient.setSelected(true);
				checkBoxDate.setSelected(true);
				chckbxProtocol.setSelected(true);
				checkBoxSerie.setSelected(true);
				chckbxProject.setEnabled(false);
				checkBoxPatient.setEnabled(false);
				checkBoxDate.setEnabled(false);
				chckbxProtocol.setEnabled(false);
				checkBoxSerie.setEnabled(false);
				if(getNewProjectName()!=null)
					project = getNewProjectName();
				else
					project = parts[serverdirlen];
				break;
			case 2://patient
				checkBoxPatient.setSelected(true);
				checkBoxDate.setSelected(true);
				chckbxProtocol.setSelected(true);
				checkBoxSerie.setSelected(true);
				checkBoxPatient.setEnabled(false);
				checkBoxDate.setEnabled(false);
				chckbxProtocol.setEnabled(false);
				checkBoxSerie.setEnabled(false);
				if(chckbxProject.isSelected()){
					if(getNewProjectName()!=null)
						project = getNewProjectName();
					else
						project = parts[serverdirlen];
				}
				if(checkBoxPatient.isSelected()){
					if(getNewPatientName()!=null)
						patient = getNewPatientName();
					else
						patient = parts[serverdirlen+1];
				}
				break;
			case 3://acqdate
				checkBoxDate.setSelected(true);
				chckbxProtocol.setSelected(true);
				checkBoxSerie.setSelected(true);
				checkBoxDate.setEnabled(false);
				chckbxProtocol.setEnabled(false);
				checkBoxSerie.setEnabled(false);
				if(chckbxProject.isSelected()){
					if(getNewProjectName()!=null)
						project = getNewProjectName();
					else
						project = parts[serverdirlen];
				}
				if(checkBoxPatient.isSelected()){
					if(getNewPatientName()!=null)
						patient = getNewPatientName();
					else
						patient = parts[serverdirlen+1];
				}
				if(checkBoxDate.isSelected()){
					if(getNewDate()!=null)
						acqdate = getNewDate();
					else
						acqdate = parts[serverdirlen+2];
				}
				break;
			case 4://protocol
				chckbxProtocol.setSelected(true);
				checkBoxSerie.setSelected(true);
				chckbxProtocol.setEnabled(false);
				checkBoxSerie.setEnabled(false);
				if(chckbxProject.isSelected()){
					if(getNewProjectName()!=null)
						project = getNewProjectName();
					else
						project = parts[serverdirlen];
				}
				if(checkBoxPatient.isSelected()){
					if(getNewPatientName()!=null)
						patient = getNewPatientName();
					else
						patient = parts[serverdirlen+1];
				}
				if(checkBoxDate.isSelected()){
					if(getNewDate()!=null)
						acqdate = getNewDate();
					else
						acqdate = parts[serverdirlen+2];
				}
				if(chckbxProtocol.isSelected()){
					if(getNewProtocolName()!=null)
						protocol = getNewProtocolName();
					else
						protocol = parts[serverdirlen+3];
				}
				break;
			case 5://serie
				checkBoxSerie.setSelected(true);
				checkBoxSerie.setEnabled(false);
				if(chckbxProject.isSelected()){
					if(getNewProjectName()!=null)
						project = getNewProjectName();
					else
						project = parts[serverdirlen];
				}
				if(checkBoxPatient.isSelected()){
					if(getNewPatientName()!=null)
						patient = getNewPatientName();
					else
						patient = parts[serverdirlen+1];
				}
				if(checkBoxDate.isSelected()){
					if(getNewDate()!=null)
						acqdate = getNewDate();
					else
						acqdate = parts[serverdirlen+2];
				}
				if(chckbxProtocol.isSelected()){
					if(getNewProtocolName()!=null)
						protocol = getNewProtocolName();
					else
						protocol = parts[serverdirlen+3];
				}
				if(checkBoxSerie.isSelected())
					serie = parts[serverdirlen+4];
				break;
			default:
				return "Unknown";
			}
			// cast en file pour eviter les multiple slash
			String outputdir = txtOutputDirectory.getText();
			if(chckBoxBIDS.isSelected())
				outputdir = bidsServer.getDirectory();
			return new File(outputdir+File.separator+project+File.separator+patient+File.separator+acqdate+File.separator+protocol+File.separator+serie).getAbsolutePath();
		}
		return "Unknow";
	}


	public static void main(String[] args){
		AdvancedImportFrame ad = new AdvancedImportFrame(null);
		ad.createAndShowGUI();
	}



}
