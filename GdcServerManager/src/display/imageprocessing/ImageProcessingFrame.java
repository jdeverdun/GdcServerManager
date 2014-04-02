package display.imageprocessing;


import ij.gui.ProgressBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import daemon.DecryptDaemon;
import daemon.NiftiDaemon;
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

public class ImageProcessingFrame extends JFrame {
	
	// ----- Variables static --------
	public static final String TITLE = "Image Processing";
	public static final int WIDTH = 500;
	public static final int HEIGHT = 400;
	
	private String newProjectName;
	private String newPatientName;
	private String newProtocolName;
	private String newDate;
	
	private ArrayList<File> directories;
	private JList<String> list;
	private Component verticalGlue;
	private JSeparator separator;
	private JLabel lblFolderStructure;
	private JCheckBox chckbxProject;
	private JCheckBox checkBoxPatient;
	private JCheckBox checkBoxDate;
	private JCheckBox chckbxProtocol;
	private JCheckBox checkBoxSerie;
	
	public ImageProcessingFrame(ArrayList<File> directories){
		super();
		setDirectories(directories);
		
		// init de la fenetre
				createAndShowGUI();
				
		getContentPane().setLayout(new MigLayout("", "", ""));
		
		
		
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, "cell 4 10 2 11,aligny bottom");
		panel_1.setLayout(new MigLayout("", "[grow][grow]", "[][][][][][][][][15px][grow][]"));
		
		separator = new JSeparator();
		panel_1.add(separator, "cell 0 5 2 1,growx");
		
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
		
		

		// Affiche les repertoires selectionnes
		String[] liste = new String[directories.size()];
		int count=0;
		for(File fi:directories)
		{
			liste[count++] =fi.getAbsolutePath();
			//System.out.println(fi.getAbsolutePath());
			//System.out.println(Arrays.toString(liste));
			

		}
		
		//JList<String>list2 = new JList<String>(liste);
		verticalGlue = Box.createVerticalGlue();
		verticalGlue.setPreferredSize(new Dimension(20, 0));
		panel_1.add(verticalGlue, "cell 0 8,grow");
		panel_1.add(new JScrollPane(list), "cell 0 9 2 2,grow");
	}

	private void createAndShowGUI() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(480,400);
		this.setTitle(TITLE);
		this.setLocation(WindowManager.MAINWINDOW.getLocation());
		this.setIconImage(new ImageIcon(this.getClass().getResource("/images/logo32.png")).getImage());
		this.setVisible(true);
		
	}
	
	public void setDirectories(ArrayList<File> directories) {
		this.directories = directories;
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
	
	public String[] getOutputFilesAsStringArray() {
		if(directories==null || directories.size()==0)
			return new String[]{};
		String[] list = new String[directories.size()];
		int count=0;
		for(File fi:directories){
			try {
				list[count++] =fi.getAbsolutePath(); //formatOuput(fi.getAbsolutePath());
			} catch (Exception e) {
				WindowManager.mwLogger.log(Level.WARNING,"Error while formatting output files for the import [getOutputFilesAsStringArray] with "+fi.getAbsolutePath(),e);
			}
		}
		return list;
	}
	
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
			return new File(txtOutputDirectory.getText()+File.separator+project+File.separator+patient+File.separator+acqdate+File.separator+protocol+File.separator+serie).getAbsolutePath();
		}
		return "Unknow";
	}

}
