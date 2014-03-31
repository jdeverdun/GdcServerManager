package display;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import model.ProjectStatistics;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import daemon.DicomNode;
import display.containers.viewer.ViewerPanel;
import display.lib.JIPTextField;

import settings.SQLSettings;
import settings.SystemSettings;
import settings.UserProfile;

import javax.swing.JSplitPane;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;

public class SettingsFrame extends JFrame {
	
	private JTabbedPane tabbedPane;
	private JTextField txtDicomDirectory;
	private JTextField niftiField;
	private JTextField txtBufferdir;
	private JTextField txtTempdir;
	private JTextField txtServerDir;
	private JIPTextField textIP;
	private JTextField txtNodeport;
	private JTextField txtDatabasename;
	private JTextField txtAetitle;
	private JIPTextField textDBip;
	
	public SettingsFrame() {
		getContentPane().setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		splitPane.setLeftComponent(tabbedPane);
		
		JPanel filesSettingPanel = new JPanel();
		JPanel serverSettingPanel = new JPanel();
		tabbedPane.addTab("Server", null, serverSettingPanel, null);
		serverSettingPanel.setLayout(new MigLayout("", "[][grow][grow]", "[][][][18.00][][][18.00][grow][]"));
		
		JLabel lblRootServerDirectory = new JLabel("Root server directory");
		serverSettingPanel.add(lblRootServerDirectory, "cell 0 0,alignx left");
		
		txtServerDir = new JTextField(SystemSettings.SERVER_INFO.getServerDir().toString());
		txtServerDir.setToolTipText("Directory of the data server ( Default : J:/ )");
		serverSettingPanel.add(txtServerDir, "cell 1 0,growx");
		txtServerDir.setColumns(10);
		ImageIcon icon=new ImageIcon(MainWindow.class.getResource("/images/folder.png"));
		Image img = icon.getImage();  
		Image newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		ImageIcon icon2 = new ImageIcon(newimg); 
		
		JButton btnSelectserverdir = new JButton(icon2);
		serverSettingPanel.add(btnSelectserverdir, "cell 2 0");
		tabbedPane.addTab("Converter", null, filesSettingPanel, null);
		filesSettingPanel.setLayout(new MigLayout("", "[][grow][]", "[][][][][][][]"));
		
		JLabel lblDicomDirectory = new JLabel("Dicom directory ");
		filesSettingPanel.add(lblDicomDirectory, "cell 0 0,alignx left");
		
		txtDicomDirectory = new JTextField(SystemSettings.SERVER_INFO.getDicomDir().toString());
		txtDicomDirectory.setToolTipText("Directory where the dicom images will be saved.");
		filesSettingPanel.add(txtDicomDirectory, "cell 1 0,growx");
		txtDicomDirectory.setColumns(10);
		
		
		
		JButton btnSelectdicomdir = new JButton(icon2);
		filesSettingPanel.add(btnSelectdicomdir, "cell 2 0");
		
		JLabel lblNiftiDirectry = new JLabel("Nifti directory");
		filesSettingPanel.add(lblNiftiDirectry, "cell 0 1,alignx left");
		
		niftiField = new JTextField(SystemSettings.SERVER_INFO.getNiftiDir().toString());
		niftiField.setToolTipText("Directory where nifti files will be saved.");
		filesSettingPanel.add(niftiField, "cell 1 1,growx");
		niftiField.setColumns(10);
		
		JButton niftiSelectbutton = new JButton(icon2);
		filesSettingPanel.add(niftiSelectbutton, "cell 2 1");
		
		JLabel lblBufferDirectory = new JLabel("Buffer directory");
		serverSettingPanel.add(lblBufferDirectory, "cell 0 2,alignx left");
		
		txtBufferdir = new JTextField(SystemSettings.SERVER_INFO.getIncomingDir().toString());
		serverSettingPanel.add(txtBufferdir, "cell 1 2,growx");
		txtBufferdir.setColumns(10);
		
		JButton btnbufferselect = new JButton(icon2);
		serverSettingPanel.add(btnbufferselect, "cell 2 2");
		
		JSeparator separator_1 = new JSeparator();
		serverSettingPanel.add(separator_1, "cell 0 3 3 1,growx");
		
		JLabel lblDicomNodeIp = new JLabel("Dicom node IP (*)");
		lblDicomNodeIp.setToolTipText("Require restart.");
		serverSettingPanel.add(lblDicomNodeIp, "cell 0 4,alignx left");
		
		textIP = new JIPTextField(DicomNode.DEFAULT_HOSTNAME);
		serverSettingPanel.add(textIP, "flowx,cell 1 4,alignx left,growy");
		textIP.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 5));
		
		JLabel label = new JLabel(":");
		serverSettingPanel.add(label, "cell 1 4");
		
		txtNodeport = new JTextField();
		txtNodeport.setHorizontalAlignment(SwingConstants.CENTER);
		serverSettingPanel.add(txtNodeport, "cell 1 4");
		txtNodeport.setColumns(4);
		txtNodeport.setText(""+DicomNode.DEFAULT_PORT);
		
		JLabel lblDicomNodeAETitle = new JLabel("Dicom node AETitle (*)");
		lblDicomNodeAETitle.setToolTipText("Require restart.");
		serverSettingPanel.add(lblDicomNodeAETitle, "cell 0 5,alignx left");
		
		txtAetitle = new JTextField();
		txtAetitle.setHorizontalAlignment(SwingConstants.CENTER);
		txtAetitle.setText(DicomNode.DEFAULT_AE_TITLE);
		serverSettingPanel.add(txtAetitle, "cell 1 5,growx");
		txtAetitle.setColumns(10);
		
		JSeparator separator = new JSeparator();
		serverSettingPanel.add(separator, "cell 0 6 3 1,growx");
		
		JLabel lblDBip = new JLabel("Database IP (*)");
		lblDBip.setToolTipText("Require restart.");
		serverSettingPanel.add(lblDBip, "cell 0 7");
		
		textDBip = new JIPTextField(SQLSettings.ADDRESS);
		serverSettingPanel.add(textDBip, "cell 1 7,alignx center,growy");
		textDBip.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 5));
		
		JLabel lblDatabaseName = new JLabel("Database name (*)");
		lblDatabaseName.setToolTipText("Require restart.");
		serverSettingPanel.add(lblDatabaseName, "cell 0 8,alignx left");
		
		txtDatabasename = new JTextField();
		txtDatabasename.setHorizontalAlignment(SwingConstants.CENTER);
		txtDatabasename.setText(SQLSettings.DATABASE_NAME);
		serverSettingPanel.add(txtDatabasename, "cell 1 8,growx");
		txtDatabasename.setColumns(10);
		
		JLabel lblTempDirectory = new JLabel("Temp directory");
		filesSettingPanel.add(lblTempDirectory, "cell 0 2,alignx left");
		
		txtTempdir = new JTextField(SystemSettings.SERVER_INFO.getTempDir().toString());
		txtTempdir.setToolTipText("Directory for temp files.");
		filesSettingPanel.add(txtTempdir, "cell 1 2,growx");
		txtTempdir.setColumns(10);
		
		JButton btnSelecttemp = new JButton(icon2);
		filesSettingPanel.add(btnSelecttemp, "cell 2 2");
		
		if(UserProfile.CURRENT_USER.getLevel()!=3){
			// on grise les options ininteressante pour l'utilisateur lambda
			lblBufferDirectory.setEnabled(false);
			txtNodeport.setEnabled(false);
			textIP.setEnabled(false);
			txtBufferdir.setEnabled(false);
			btnbufferselect.setEnabled(false);
			label.setEnabled(false);
			lblDicomNodeIp.setEnabled(false);
			lblDicomNodeAETitle.setEnabled(false);
			txtAetitle.setEnabled(false);
			//txtDatabasename.setEnabled(false);
			if(UserProfile.CURRENT_USER.getLevel() == 0){
				txtDatabasename.setEnabled(false);
				lblDatabaseName.setEnabled(false);
				lblDBip.setEnabled(false);
				textDBip.setEnabled(false);
				lblRootServerDirectory.setEnabled(false);
				txtServerDir.setEnabled(false);
			}
				
		}
		btnSelecttemp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getTempDir().toString());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = fc.showOpenDialog(SettingsFrame.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	File file = fc.getSelectedFile();
	            	txtTempdir.setText(file.getAbsolutePath());
	            }
			}
		});
		
		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		splitPane.setResizeWeight(0.9f);
		panel.setLayout(new MigLayout("", "[57px,grow][grow]", "[23px,grow]"));
		
		JButton btnSave = new JButton("Save");
		panel.add(btnSave, "cell 0 0,grow");
		
		JButton btnClose = new JButton("Close");
		panel.add(btnClose, "cell 1 0,grow");
		
		btnClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		btnSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					DicomNode.DEFAULT_PORT = Integer.parseInt(txtNodeport.getText());
				}catch(Exception e){
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(SettingsFrame.this,
									"Port should be an integer",
								    "Saving error",
								    JOptionPane.ERROR_MESSAGE);
						}
					});
					return;
				}
				// on check l'ip
				Pattern p = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
			    Matcher m = p.matcher(textIP.getText());
			    if(m.find()){
			    	DicomNode.DEFAULT_HOSTNAME = textIP.getText();
			    }else{
			    	SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(SettingsFrame.this,
									"DicomNode IP should contain only integer",
								    "Saving error",
								    JOptionPane.ERROR_MESSAGE);
						}
					});
			    	return;
			    }
			    if(txtAetitle.getText().length()>0){
			    	DicomNode.DEFAULT_AE_TITLE = txtAetitle.getText();
			    }else{
			    	SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(SettingsFrame.this,
									"AETitle should not be empty",
								    "Saving error",
								    JOptionPane.ERROR_MESSAGE);
						}
					});
			    	return;
			    }
			    
			    // on check l'ip de la bdd
				m = p.matcher(textDBip.getText());
			    if(m.find()){
			    	SQLSettings.ADDRESS = textDBip.getText();
			    }else{
			    	SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(SettingsFrame.this,
									"DB IP should contain only integer",
								    "Saving error",
								    JOptionPane.ERROR_MESSAGE);
						}
					});
			    	return;
			    }
			    SQLSettings.DATABASE_NAME = txtDatabasename.getText();
				SystemSettings.SERVER_INFO.setIncomingDir(txtBufferdir.getText());
				SystemSettings.SERVER_INFO.setDicomDir(txtDicomDirectory.getText());
				SystemSettings.SERVER_INFO.setNiftiDir(niftiField.getText());
				SystemSettings.SERVER_INFO.setTempDir(txtTempdir.getText());
				SystemSettings.SERVER_INFO.setServerDir(txtServerDir.getText());
				SystemSettings.SERVER_INFO.saveConfiguration();
				dispose();
				
			}
		});
		btnbufferselect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getIncomingDir().toString());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = fc.showOpenDialog(SettingsFrame.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	File file = fc.getSelectedFile();
	            	txtBufferdir.setText(file.getAbsolutePath());
	            }
			}
		});
		btnSelectdicomdir.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getDicomDir().toString());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = fc.showOpenDialog(SettingsFrame.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	File file = fc.getSelectedFile();
	            	txtDicomDirectory.setText(file.getAbsolutePath());
	            }
			}
		});
		niftiSelectbutton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getNiftiDir().toString());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = fc.showOpenDialog(SettingsFrame.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	File file = fc.getSelectedFile();
	            	niftiField.setText(file.getAbsolutePath());
	            }
			}
		});
		btnSelectserverdir.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFileChooser fc = new JFileChooser(SystemSettings.SERVER_INFO.getServerDir().toString());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = fc.showOpenDialog(SettingsFrame.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	File file = fc.getSelectedFile();
	            	txtServerDir.setText(file.getAbsolutePath());
	            }
			}
		});
	}

	/**
	 * i correspond a l'onglet actif par defaut
	 * @param i
	 */
	public SettingsFrame(int i) {
		this();
		tabbedPane.setSelectedIndex(i);
	}

	public void createAndShowGUI(){
		JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
        } catch (Exception e) {
          System.out.println("Substance Graphite failed to initialize");
        }
		UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
		setTitle("Settings");
		setSize(420, 350);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon(this.getClass().getResource("/images/logo32.png")).getImage());
		setLocationRelativeTo(null);
		setVisible(true);
		if(UserProfile.CURRENT_USER.getLevel()!=3)
			tabbedPane.setSelectedIndex(1);
	}
	public static void main(String args[]){

		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				JFrame.setDefaultLookAndFeelDecorated(true);
				try {
			          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
		        } catch (Exception e) {
		          System.out.println("Substance Graphite failed to initialize");
		        }
				UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
				JFrame jf = new JFrame("Settings");
				jf.getContentPane().add(new SettingsFrame());
				jf.setSize(400, 250);
				jf.setIconImage(new ImageIcon(this.getClass().getResource("/images/mainicon.png")).getImage());
				jf.setLocationRelativeTo(null);
				jf.setVisible(true);
				
				
			}
		});
	}
}