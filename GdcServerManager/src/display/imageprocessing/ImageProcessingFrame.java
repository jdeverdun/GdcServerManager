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

import plugins.FolderProcessingPlugins;
import plugins.FolderProcessingPlugins.FolderStructure;
import plugins.PluginsLoader;

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
import javax.swing.JComboBox;

public class ImageProcessingFrame extends JFrame {

	// ----- Variables static --------
	public static final String TITLE = "Image Processing";
	public static final int WIDTH = 500;
	public static final int HEIGHT = 400;

	// plugins
	private PluginsLoader pluginLoader;
	private FolderProcessingPlugins[] folderProcessingPlugins;
	private String newProjectName;
	private String newPatientName;
	private String newProtocolName;
	private String newDate;

	private ArrayList<File> directories;
	private JList<String> list;
	private Component verticalGlue;
	private JSeparator separator;
	private JLabel lblFolderStructure;
	private JCheckBox checkBoxPatient;
	private JCheckBox checkBoxDate;
	private JCheckBox checkboxProtocol;
	private JCheckBox checkBoxSerie;
	private JComboBox comboBox;
	private JButton btnOk;

	public ImageProcessingFrame(ArrayList<File> directories){
		super();
		setDirectories(directories);

		// init de la fenetre
		createAndShowGUI();
		// init plugin 
		pluginLoader = new PluginsLoader();
		try {
			folderProcessingPlugins = pluginLoader.loadAllFolderProcessingPlugins();
		} catch (Exception e) {
			WindowManager.mwLogger.log(Level.SEVERE,"Couldn't load folderProcessingPlugins ... ["+e.toString()+"]");
			e.printStackTrace();
		}
		comboBox.removeAllItems();
		for(FolderProcessingPlugins fp : folderProcessingPlugins){
			comboBox.addItem(fp.getLabel());
		}







	}

	private void createAndShowGUI() {
		// panels and co

		getContentPane().setLayout(new MigLayout("", "[][grow]", "[][][][]"));

		DefaultListModel<String> model = new DefaultListModel<String>();  
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

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, "cell 0 0 2 4,growx,aligny bottom");
		panel_1.setLayout(new MigLayout("", "[grow][grow]", "[][][][][15px][grow][]"));

		separator = new JSeparator();
		panel_1.add(separator, "cell 0 1 2 1,growx");

		comboBox = new JComboBox();
		panel_1.add(comboBox, "cell 0 2,growx");

		btnOk = new JButton("Ok");
		panel_1.add(btnOk, "cell 1 2");

		lblFolderStructure = new JLabel("Folder structure");
		lblFolderStructure.setToolTipText("Please select the folder structure of yours paths.");
		panel_1.add(lblFolderStructure, "cell 0 3,alignx left");

		checkBoxPatient = new JCheckBox("Patient");
		panel_1.add(checkBoxPatient, "flowx,cell 1 3,alignx left");

		checkBoxDate = new JCheckBox("Date");
		panel_1.add(checkBoxDate, "cell 1 3,alignx left");

		checkboxProtocol = new JCheckBox("Protocol");
		checkboxProtocol.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		panel_1.add(checkboxProtocol, "cell 1 3,alignx left");
		checkBoxSerie = new JCheckBox("Serie");
		checkBoxSerie.setEnabled(false);
		checkBoxSerie.setSelected(true);
		panel_1.add(checkBoxSerie, "cell 1 3,alignx left");
		list = new JList<String>();
		list.setModel(model);

		//JList<String>list2 = new JList<String>(liste);
		verticalGlue = Box.createVerticalGlue();
		verticalGlue.setPreferredSize(new Dimension(20, 0));
		panel_1.add(verticalGlue, "cell 0 4,grow");
		panel_1.add(new JScrollPane(list), "cell 0 5 2 2,grow");


		// =============== EVENTS ==============
		btnOk.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(getFolderStructure());
				if ( getFolderStructure()==null){
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(ImageProcessingFrame.this,
									"Unknown folder structure",
									"Parameters error",
									JOptionPane.ERROR_MESSAGE);
						}
					});
					return;
				}
				String label = (String) comboBox.getSelectedItem();
				for(FolderProcessingPlugins fp : folderProcessingPlugins){
					if(fp.getLabel().equals(label)){
						fp.actionOnFolders(directories, getFolderStructure());
					}
				}
				dispose();

			}
		});
		// Autres
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
				list[count++] =fi.getAbsolutePath();
			} catch (Exception e) {
				WindowManager.mwLogger.log(Level.WARNING,"Error while formatting output files for the import [getOutputFilesAsStringArray] with "+fi.getAbsolutePath(),e);
			}
		}
		return list;
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

	private FolderStructure getFolderStructure() {

		// on s'assure de la structure
		if (checkBoxPatient.isSelected() && checkBoxDate.isSelected() && checkboxProtocol.isSelected() && checkBoxSerie.isSelected())
			return FolderStructure.PatDatProtSer;
		else if(checkBoxPatient.isSelected() && checkboxProtocol.isSelected() && checkBoxSerie.isSelected() && !(checkBoxDate.isSelected()))
			return FolderStructure.PatProtSer;
		else if (checkBoxPatient.isSelected() && checkBoxDate.isSelected() && !(checkboxProtocol.isSelected()) && checkBoxSerie.isSelected())
			return FolderStructure.PatDatSer;
		else if (checkBoxPatient.isSelected() && !(checkBoxDate.isSelected() || checkboxProtocol.isSelected()) && checkBoxSerie.isSelected())
			return FolderStructure.PatSer;
		else if (checkBoxPatient.isSelected() && !(checkBoxDate.isSelected() || checkboxProtocol.isSelected() || checkBoxSerie.isSelected()))
			return FolderStructure.Pat;
		else if (!(checkBoxPatient.isSelected() || checkBoxDate.isSelected() || checkboxProtocol.isSelected()) && checkBoxSerie.isSelected())
			return FolderStructure.Ser;

		return null;

	}
}
