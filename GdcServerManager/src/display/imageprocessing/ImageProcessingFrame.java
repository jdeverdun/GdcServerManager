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
import javax.swing.text.html.HTMLEditorKit;

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
import javax.swing.JEditorPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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
	private JButton btnCancel;
	private JPanel panel_1;
	private JLabel lblChooseTheProcess;
	private JSeparator separator_1;
	private JTextArea txtrSelectThe;
	private JPanel panel_2;
	private JSeparator separator_2;
	private JEditorPane dtrpnSelectThe;

	public ImageProcessingFrame(ArrayList<File> directories){
		super();
		this.setResizable(false);
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
		JPanel panel = new JPanel();
		getContentPane().add(panel);//, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[18px][130.00px,grow][100px][18px]", "[-31.00,grow][52.00][][32.00][125.00]"));



		separator_2 = new JSeparator();
		separator_2.setForeground(Color.BLACK);
		separator_2.setBackground(Color.BLACK);
		panel.add(separator_2, "cell 1 0 2 1,growx");

		lblChooseTheProcess = new JLabel("Choose the process");
		panel.add(lblChooseTheProcess, "cell 1 1,alignx left");

		comboBox = new JComboBox();
		panel.add(comboBox, "cell 2 1,growx");

		separator_1 = new JSeparator();
		separator_1.setBackground(Color.BLACK);
		separator_1.setForeground(Color.BLACK);
		panel.add(separator_1, "cell 1 2 2 1,growx");

		lblFolderStructure = new JLabel("Folder structure");
		lblFolderStructure.setToolTipText("Please select the folder structure of yours paths.");
		panel.add(lblFolderStructure, "cell 1 3,alignx left,aligny center");

		checkBoxPatient = new JCheckBox("Patient");
		panel.add(checkBoxPatient, "flowx,cell 2 3,alignx left,aligny center");

		checkBoxDate = new JCheckBox("Date");
		panel.add(checkBoxDate, "cell 2 3,alignx left,aligny center");

		checkboxProtocol = new JCheckBox("Protocol");
		checkboxProtocol.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		panel.add(checkboxProtocol, "cell 2 3,alignx left,aligny center");
		checkBoxSerie = new JCheckBox("Serie");
		checkBoxSerie.setEnabled(false);
		checkBoxSerie.setSelected(true);
		panel.add(checkBoxSerie, "cell 2 3,alignx left,aligny center");
		list = new JList<String>();
		list.setModel(model);

		//JList<String>list2 = new JList<String>(liste);
		/*verticalGlue = Box.createVerticalGlue();
																				verticalGlue.setPreferredSize(new Dimension(20, 0));
																				panel.add(verticalGlue, "cell 0 2,grow");*/
		panel.add(new JScrollPane(list), "cell 1 4 2 1,alignx center,grow");

		panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new MigLayout("", "[15.00px][195.00px][195.00px][15px]", "[23px]"));

		btnOk = new JButton("Ok");
		panel_1.add(btnOk, "cell 1 0,growx,aligny top");


		// =============== EVENTS ==============
		btnOk.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
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

		btnCancel = new JButton("Close");
		panel_1.add(btnCancel, "cell 2 0,growx,aligny top");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		btnCancel.setActionCommand("Close");

		panel_2 = new JPanel();
		getContentPane().add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new MigLayout("", "[20px][356px,grow]", "[58px,grow]"));
		

		txtrSelectThe = new JTextArea();
		txtrSelectThe.setText("1. Select the process\r\n2. Give the folder structure (eg : \\patient\\date\\serie\\....nii)\r\n3. Ok");
		txtrSelectThe.setDisabledTextColor(Color.LIGHT_GRAY);
		txtrSelectThe.setEnabled(false);
		txtrSelectThe.setForeground(Color.GRAY);
		txtrSelectThe.setEditable(false);
		panel_2.add(txtrSelectThe, "cell 1 0,alignx left,aligny top");
		//txtrSelectThe.setText("");
		// Autres
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(437,296);
		this.setTitle(TITLE);
		this.setLocationRelativeTo(null);//(WindowManager.MAINWINDOW.getLocation());
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
