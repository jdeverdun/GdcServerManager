package display;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

import javax.swing.JPanel;

import model.daemon.CustomConversionSettings;
import model.daemon.CustomConversionSettings.ServerMode;
import model.daemon.ImportSettings;
import model.daemon.ImportSettings.DicomNamingTag;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import com.sun.org.apache.bcel.internal.generic.FNEG;

import daemon.DicomJobDispatcher;
import daemon.DicomEncryptDaemon;
import daemon.NiftiDaemon;
import daemon.tools.firstNameDB;
import display.containers.FileManager;
import display.containers.WaitingBarPanel;
import display.containers.WarningPanel;
import display.containers.viewer.ViewerPanel;
import exceptions.AnonymizationException;

import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;

import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.border.TitledBorder;
import javax.swing.JRadioButton;
import javax.swing.Icon;

/**
 * Classe permettant d'importer des donnees depuis la machine locale vers
 * le serveur de stockage distant. Permet de forcer un certain nombre d'informations 
 * contenus dans le dicom (/!\ ne change pas le contenu du dicom !!)
 * @author DEVERDUN Jeremy
 *
 */
public class ImportFrame extends JFrame {
	
	private static final String DEFAULT_NPROJECT_TEXT = "New Project Name [Optionnal]";
	private static final String DEFAULT_NPATIENT_TEXT = "New Patient Name/Id [Optionnal]";
	
	
	// les daemons dedie a l'import
	private NiftiDaemon niftid;
	private DicomJobDispatcher dispatcher;// on appel directement le worker de l'envrypter et pas l'encrypt daemon 
	
	private JTextField txtDicomDirectory;
	private JTextField txtProjectname;
	private JRadioButton rdbtnPatientname;
	private JRadioButton rdbtnPatientid;
	private JTextField txtNewPatientName;
	private JRadioButton rdbtnAnonymize;
	private JButton btnSelectdicomdir;
	private JLabel lblForceProjectName;
	private JLabel lblPatientIdentification;
	private JButton btnImport ;
	private JButton btnClose;

	// variable permettant de stopper l'import
	private boolean stopImport;
	private JLabel lblForcePatientName;
	private JLabel lblForcePatientName_1;
	private JTextField textFieldXLS;
	private JButton buttonXLS;

	
	
	public ImportFrame() {
		stopImport = false;
		ImageIcon icon=new ImageIcon(MainWindow.class.getResource("/images/folder.png"));
		Image img = icon.getImage();  
		Image newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		ImageIcon icon2 = new ImageIcon(newimg); 
		

		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[grow][grow][53px]", "[grow][29px,grow,center][grow]"));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Directory", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_2, "cell 0 0 3 1,grow");
		panel_2.setLayout(new MigLayout("", "[grow][]", "[][]"));
		String dir = SystemSettings.APP_DIR.toString();
		if(UserProfile.LAST_SELECTED_DIR!=null)
			dir = UserProfile.LAST_SELECTED_DIR.toString();
		
		txtDicomDirectory = new JTextField(dir);
		panel_2.add(txtDicomDirectory, "cell 0 0,growx");
		
		btnSelectdicomdir = new JButton(icon2);
		panel_2.add(btnSelectdicomdir, "cell 1 0");
		btnSelectdicomdir.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFileChooser fc = new JFileChooser(txtDicomDirectory.getText());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = fc.showOpenDialog(ImportFrame.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	File file = fc.getSelectedFile();
	            	txtDicomDirectory.setText(file.getAbsolutePath());
	            }
			}
		});
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Import Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_1, "cell 0 2 3 1,grow");
		panel_1.setLayout(new MigLayout("", "[][grow]", "[][][][]"));
		
		lblForceProjectName = new JLabel("Force project name");
		panel_1.add(lblForceProjectName, "cell 0 0,alignx left");
		
		txtProjectname = new JTextField();
		txtProjectname.setText(DEFAULT_NPROJECT_TEXT);
		txtProjectname.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel_1.add(txtProjectname, "cell 1 0,growx");
		txtProjectname.setColumns(10);
		
		lblForcePatientName = new JLabel("Force Patient name");
		panel_1.add(lblForcePatientName, "cell 0 1,alignx left");
		
		txtNewPatientName = new JTextField();
		txtNewPatientName.setText(DEFAULT_NPATIENT_TEXT);
		txtNewPatientName.setFont(new Font("Tahoma", Font.ITALIC, 11));
		txtNewPatientName.setColumns(10);
		panel_1.add(txtNewPatientName, "flowx,cell 1 1,growx");
		
		lblForcePatientName_1 = new JLabel("Force Patient name (xls)");
		panel_1.add(lblForcePatientName_1, "cell 0 2,alignx trailing");
		
		textFieldXLS = new JTextField();
		textFieldXLS.setText("New Patient Name/Id [Optionnal]");
		textFieldXLS.setFont(new Font("Tahoma", Font.ITALIC, 11));
		textFieldXLS.setColumns(10);
		panel_1.add(textFieldXLS, "flowx,cell 1 2,growx");
		
		lblPatientIdentification = new JLabel("Patient Identification");
		panel_1.add(lblPatientIdentification, "cell 0 3,alignx left");
		
		rdbtnPatientname = new JRadioButton("PatientName");
		rdbtnPatientname.setSelected(true);
		panel_1.add(rdbtnPatientname, "flowx,cell 1 3");
		
		rdbtnPatientid = new JRadioButton("PatientID");
		panel_1.add(rdbtnPatientid, "cell 1 3");
		
		rdbtnAnonymize = new JRadioButton("Anonymize");
		panel_1.add(rdbtnAnonymize, "cell 1 3");
		
		buttonXLS = new JButton((Icon) null);
		buttonXLS.setText("...");
		panel_1.add(buttonXLS, "cell 1 2,growy");
		buttonXLS.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFileChooser fc = new JFileChooser(txtDicomDirectory.getText());
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int retval = fc.showOpenDialog(ImportFrame.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	File file = fc.getSelectedFile();
	            	textFieldXLS.setText(file.getAbsolutePath());
	            }
			}
		});

		JPanel panelSaveClose = new JPanel();
		getContentPane().add(panelSaveClose, BorderLayout.SOUTH);
		panelSaveClose.setLayout(new MigLayout("", "[grow][grow]", "[]"));
		
		btnImport = new JButton("Import");
		panelSaveClose.add(btnImport, "cell 0 0,growx");
		
		btnClose = new JButton("Close");
		panelSaveClose.add(btnClose, "cell 1 0,growx");
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
		
		rdbtnPatientname.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!rdbtnPatientname.isSelected()){
					rdbtnPatientname.setSelected(true);
					rdbtnPatientid.setSelected(false);
					rdbtnAnonymize.setSelected(false);
					return;
				}
				rdbtnPatientid.setSelected(false);
				rdbtnAnonymize.setSelected(false);
			}
		});
		rdbtnPatientid.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!rdbtnPatientid.isSelected()){
					rdbtnPatientid.setSelected(true);
					txtNewPatientName.setText(DEFAULT_NPATIENT_TEXT);
					txtNewPatientName.setFont(new Font("Tahoma", Font.ITALIC, 11));
					rdbtnPatientname.setSelected(false);
					rdbtnAnonymize.setSelected(false);
					return;
				}
				txtNewPatientName.setText(DEFAULT_NPATIENT_TEXT);
				txtNewPatientName.setFont(new Font("Tahoma", Font.ITALIC, 11));
				rdbtnPatientname.setSelected(false);
				rdbtnAnonymize.setSelected(false);
			}
		});
		rdbtnAnonymize.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!rdbtnAnonymize.isSelected()){
					rdbtnAnonymize.setSelected(true);
					txtNewPatientName.setText(DEFAULT_NPATIENT_TEXT);
					txtNewPatientName.setFont(new Font("Tahoma", Font.ITALIC, 11));
					rdbtnPatientname.setSelected(false);
					rdbtnPatientid.setSelected(false);
					return;
				}
				txtNewPatientName.setText(DEFAULT_NPATIENT_TEXT);
				txtNewPatientName.setFont(new Font("Tahoma", Font.ITALIC, 11));
				rdbtnPatientname.setSelected(false);
				rdbtnPatientid.setSelected(false);
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
				if(!firstNameDB.init())
					return;
				
				String pname = null;
				String patname = null;
				String xlsname = null;
				// on ne force pas le nom du patient quand on anonymise ou qu'on utilise l'id
				if(rdbtnAnonymize.isSelected() || rdbtnPatientid.isSelected()){
					txtNewPatientName.setText(DEFAULT_NPATIENT_TEXT);
					txtNewPatientName.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
				if(!txtProjectname.getText().equals(DEFAULT_NPROJECT_TEXT) && !txtProjectname.getText().equals(""))
					pname = txtProjectname.getText();
				if(!txtNewPatientName.getText().equals(DEFAULT_NPATIENT_TEXT) && !txtNewPatientName.getText().equals(""))
					patname = txtNewPatientName.getText();
				if(!textFieldXLS.getText().equals(DEFAULT_NPATIENT_TEXT) && !textFieldXLS.getText().equals("")){
					if(new File(textFieldXLS.getText()).exists())
						xlsname = textFieldXLS.getText();					
				}
				// On definit les parametres de l'import
				DicomNamingTag dtag;
				if(rdbtnAnonymize.isSelected())
					dtag = DicomNamingTag.ANONYMIZE;
				else 
					if(rdbtnPatientname.isSelected())
						dtag = DicomNamingTag.PATIENTNAME;
					else
						dtag = DicomNamingTag.PATIENTID;
				ImportSettings is;
				try {
					is = new ImportSettings(pname,patname,xlsname, dtag,dispatcher,niftid);
				} catch (final AnonymizationException e1) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(ImportFrame.this,
								    e1.toString(),
								    "Warning",
								    JOptionPane.WARNING_MESSAGE);
						}
					});
					WindowManager.mwLogger.log(Level.SEVERE, "Import error [selection]", e1);
					return;
				}
				CustomConversionSettings ccs = new CustomConversionSettings(ServerMode.IMPORT, is);
				// On lance les daemons
				niftid = new NiftiDaemon(SystemSettings.SERVER_INFO,ccs);
				dispatcher = new DicomJobDispatcher(ccs);
				niftid.start();
				dispatcher.start();
				is.setDispatcher(dispatcher);
				is.setNiftid(niftid);
				stopImport = false;
				final File anomFile = is.getAnonymizationFile();
				// on gere les thread status // recherche fichier
				setLock(true);
				final WaitingBarPanel ppanel = new WaitingBarPanel(ImportFrame.this); // mode creation de liens
				ppanel.setTitle("Looking for dicoms ...");
				final Popup popup = PopupFactory.getSharedInstance().getPopup(ImportFrame.this, ppanel, (int)ImportFrame.this.getX()+(getWidth()/2)-40,(int)ImportFrame.this.getY()+(getHeight()/2)-40);
				// Thread pour la recherche et l'ajout de dicom
				final Thread fileSeeking = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							if(!new File(txtDicomDirectory.getText()).exists()){
								popup.hide();
								setLock(false);
								WindowManager.mwLogger.log(Level.WARNING, "Directory does not exist [import]");
								return;
							}
							findAndAddDicom(new File(txtDicomDirectory.getText()));
							if(stopImport){
								dispatcher.setStop(true);
								niftid.setStop(true);
								WindowManager.mwLogger.log(Level.INFO, "Dicom selection cancelled [import]");
							}else{
								WindowManager.mwLogger.log(Level.INFO, "Dicom selection done [import]");
							}
						} catch (Exception e) {
							setLock(false);
							popup.hide();
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									WarningPanel wmess = new WarningPanel("Error during dicom selection.");
									Popup popup = PopupFactory.getSharedInstance().getPopup(ImportFrame.this, wmess, (int)ImportFrame.this.getX()+(getWidth()/2)-40,(int)ImportFrame.this.getY()+(getHeight()/2)-40);
									wmess.setPopupWindow(popup);
									popup.show();
								}
							});
							WindowManager.mwLogger.log(Level.SEVERE, "Import error [selection]", e);
						}
					}

					private void findAndAddDicom(File orig) {
						if(stopImport)
							return;
						if(orig.isFile()){
							dispatcher.addDicomToMove(orig.toPath());
						}else{
							for(File fi:orig.listFiles())
								findAndAddDicom(fi);
						}
					}
				});
				Thread statusThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						boolean continu = true;
						while(continu){
							if(stopImport){
								dispatcher.setStop(true);
								niftid.setStop(true);
								break;
							}
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							// si il y a eu un crash
							if(dispatcher.isCrashed() || niftid.isCrashed()){
								continu=false;
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										WarningPanel wmess = new WarningPanel("A daemon has crashed, see logs.");
										Popup popup = PopupFactory.getSharedInstance().getPopup(ImportFrame.this, wmess, (int)ImportFrame.this.getX()+(getWidth()/2)-40,(int)ImportFrame.this.getY()+(getHeight()/2)-40);
										wmess.setPopupWindow(popup);
										popup.show();
										dispatcher.setStop(true);
										dispatcher.clear();
										niftid.setStop(true);
										niftid.clear();
									}
								});
								
								setLock(false);
							}
								
							// les fichiers restant a envoyer
							int nbdicomToMove = dispatcher.getDicomToMove().size();
							int nbdirToConvert = niftid.getDir2convert().size();
							// si tout est fini
							if(!fileSeeking.isAlive() && nbdicomToMove==0 && nbdirToConvert==0){
								continu = false;
								setLock(false);
								WindowManager.mwLogger.log(Level.INFO, "Import succeeded");
								popup.hide();
							}
								
							if(nbdicomToMove>0)
								ppanel.setTitle("Encrypt : "+nbdicomToMove);
							else if(nbdirToConvert>0)
								ppanel.setTitle("Convert : "+nbdirToConvert);
							else
								ppanel.setTitle("Stand by");
						}
						setLock(false);
						popup.hide();
						
						// On check le nombre de ligne du fichier d'anonymisation
						try {
							BufferedReader reader = new BufferedReader(new FileReader(anomFile.getAbsolutePath()));
							int lines = 0;
						
							while (reader.readLine() != null) lines++;
							reader.close();
							
							if(lines>1){
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										JDialog.setDefaultLookAndFeelDecorated(true);
										String[] options = new String[] {"Open directory", "Close"};
									    int option = JOptionPane.showOptionDialog(ImportFrame.this, "Import terminated. An anonymization file has been saved to \n"+SystemSettings.APP_DIR+".","Information", 
									            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
									            null, options, options[0]);
										if(option==0){
											Desktop desktop = Desktop.getDesktop();
											try {
												desktop.open(anomFile.getParentFile());
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}
								});
							}else{
								anomFile.delete();
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				});
				ppanel.setPopup(popup);
				popup.show();
				
				// on lance les thread
				fileSeeking.start();				
				statusThread.start();		
				
				
				
			}

		});
	}

	public NiftiDaemon getNiftid() {
		return niftid;
	}

	/**
	 * Verrouille la frame
	 * @param b
	 */
	protected void setLock(boolean b) {
		txtDicomDirectory.setEnabled(!b);
		txtProjectname.setEnabled(!b);
		rdbtnPatientname.setEnabled(!b);
		rdbtnPatientid.setEnabled(!b);
		rdbtnAnonymize.setEnabled(!b);
		btnSelectdicomdir.setEnabled(!b);
		lblForceProjectName.setEnabled(!b);
		lblPatientIdentification.setEnabled(!b);
		btnImport.setEnabled(!b);
		btnClose.setEnabled(!b);
		lblForcePatientName.setEnabled(!b);
		txtNewPatientName.setEnabled(!b);
		buttonXLS.setEnabled(!b);
		textFieldXLS.setEnabled(!b);
		lblForcePatientName_1.setEnabled(!b);
	}
	public void setNiftid(NiftiDaemon niftid) {
		this.niftid = niftid;
	}

	public DicomJobDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(DicomJobDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public void createAndShowGUI(){
		//JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
        } catch (Exception e) {
          System.out.println("Substance Graphite failed to initialize");
        }
		//UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
		setTitle("Import");
		setSize(420, 270);
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
	}
}