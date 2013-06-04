package display;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JPanel;

import model.daemon.CustomConversionSettings;
import model.daemon.CustomConversionSettings.ServerMode;
import model.daemon.ImportSettings;
import net.miginfocom.swing.MigLayout;

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

import daemon.DicomJobDispatcher;
import daemon.EncryptDaemon;
import daemon.NiftiDaemon;
import display.containers.FileManager;
import display.containers.WaitingBarPanel;
import display.containers.WarningPanel;
import display.containers.viewer.ViewerPanel;

import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;

import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.border.TitledBorder;
import javax.swing.JRadioButton;

public class ImportFrame extends JFrame {
	
	private static final String DEFAULT_DICOMDIR_TEXT = "New Project Name [Optionnal]";
	
	
	// les daemons dedie a l'import
	private NiftiDaemon niftid;
	private DicomJobDispatcher dispatcher;// on appel directement le worker de l'envrypter et pas l'encrypt daemon
	
	private JTextField txtDicomDirectory;
	private JTextField txtProjectname;
	private JRadioButton rdbtnPatientname;
	private JRadioButton rdbtnPatientid;
	private JButton btnSelectdicomdir;
	private JLabel lblForceProjectName;
	private JLabel lblPatientIdentification;
	private JButton btnImport ;
	private JButton btnClose;

	// variable permettant de stopper l'import
	private boolean stopImport;
	
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
		panel_1.setLayout(new MigLayout("", "[][grow]", "[][]"));
		
		lblForceProjectName = new JLabel("Force project name");
		panel_1.add(lblForceProjectName, "cell 0 0,alignx left");
		
		txtProjectname = new JTextField();
		txtProjectname.setText(DEFAULT_DICOMDIR_TEXT);
		txtProjectname.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel_1.add(txtProjectname, "cell 1 0,growx");
		txtProjectname.setColumns(10);
		
		lblPatientIdentification = new JLabel("Patient Identification");
		panel_1.add(lblPatientIdentification, "cell 0 1,alignx left");
		
		rdbtnPatientname = new JRadioButton("PatientName");
		rdbtnPatientname.setSelected(true);
		panel_1.add(rdbtnPatientname, "flowx,cell 1 1");
		
		rdbtnPatientid = new JRadioButton("PatientID");
		panel_1.add(rdbtnPatientid, "cell 1 1");
		
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
					txtProjectname.setText(DEFAULT_DICOMDIR_TEXT);
					txtProjectname.setFont(new Font("Tahoma", Font.ITALIC, 11));
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtProjectname.getText().equals(DEFAULT_DICOMDIR_TEXT)){
					txtProjectname.setText("");
					txtProjectname.setFont(new Font("Tahoma", Font.PLAIN, 11));
				}
			}
		});
		
		rdbtnPatientname.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rdbtnPatientid.setSelected(!rdbtnPatientname.isSelected());
			}
		});
		rdbtnPatientid.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rdbtnPatientname.setSelected(!rdbtnPatientname.isSelected());
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
				String pname = null;
				if(!txtProjectname.getText().equals(DEFAULT_DICOMDIR_TEXT) && !txtProjectname.getText().equals(""))
					pname = txtProjectname.getText();
				
				// On definit les parametres de l'import
				ImportSettings is = new ImportSettings(pname, rdbtnPatientname.isSelected(),dispatcher,niftid);
				CustomConversionSettings ccs = new CustomConversionSettings(ServerMode.IMPORT, is);
				// On lance les daemons
				niftid = new NiftiDaemon(SystemSettings.SERVER_INFO,ccs);
				dispatcher = new DicomJobDispatcher(ccs);
				niftid.start();
				dispatcher.start();
				is.setDispatcher(dispatcher);
				is.setNiftid(niftid);
				stopImport = false;
				
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
										niftid.setStop(true);
									}
								});
								
								setLock(false);
							}
								
							// les fichiers restant a envoyer
							int nbdicomToMove = dispatcher.getDicomToMove().size();
							int nbdirToConvert = niftid.getDir2convert().size();
							// si tout est fini
							if(!fileSeeking.isAlive()  && nbdicomToMove==0 && nbdirToConvert==0){
								continu = false;
								setLock(false);
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
		btnSelectdicomdir.setEnabled(!b);
		lblForceProjectName.setEnabled(!b);
		lblPatientIdentification.setEnabled(!b);
		btnImport.setEnabled(!b);
		btnClose.setEnabled(!b);
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
		JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
        } catch (Exception e) {
          System.out.println("Substance Graphite failed to initialize");
        }
		UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
		setTitle("Import");
		setSize(400, 250);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon(this.getClass().getResource("/images/mainicon.png")).getImage());
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