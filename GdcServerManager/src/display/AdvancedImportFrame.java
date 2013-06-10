package display;

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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import display.containers.viewer.ViewerPanel;

import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;

import model.AcquisitionDate;
import net.miginfocom.swing.MigLayout;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Dimension;
import javax.swing.JCheckBox;
import java.awt.ComponentOrientation;
import javax.swing.JList;
import javax.swing.AbstractListModel;

/**
 * Classe permettant d'importer depuis le request panel des donnees vers un repertoire local
 * en selectionnant precisement la structure voulu en sortie
 * @author DEVERDUN Jeremy
 *
 */
public class AdvancedImportFrame extends JFrame {
	
	private static final String DEFAULT_NPROJECT_TEXT = "New Project Name [Optionnal]";
	
	// liste des fichiers a importer
	private File[] files;
	private String newProjectName;
	// variable permettant de stopper l'import
	private boolean stopImport;
	
	
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
	
	public AdvancedImportFrame(File[] files) {
		setFiles(files);
		setNewProjectName(null);
		stopImport = false;
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
	            	updateList();
	            }
			}
		});
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Import Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_1, "cell 0 1 3 2,grow");
		panel_1.setLayout(new MigLayout("", "[grow][grow]", "[][][15px][grow][]"));
		
		lblForceProjectName = new JLabel("Force project name");
		panel_1.add(lblForceProjectName, "cell 0 0,alignx left");
		
		txtProjectname = new JTextField();
		txtProjectname.setText(DEFAULT_NPROJECT_TEXT);
		txtProjectname.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel_1.add(txtProjectname, "cell 1 0,growx");
		txtProjectname.setColumns(10);
		
		lblFolderStructure = new JLabel("Folder structure");
		panel_1.add(lblFolderStructure, "cell 0 1,alignx left");
		
		chckbxProject = new JCheckBox("Project");
		panel_1.add(chckbxProject, "flowx,cell 1 1,alignx left");
		
		checkBoxPatient = new JCheckBox("Patient");
		panel_1.add(checkBoxPatient, "cell 1 1,alignx left");
		
		checkBoxDate = new JCheckBox("Date");
		panel_1.add(checkBoxDate, "cell 1 1,alignx left");
		
		chckbxProtocol = new JCheckBox("Protocol");
		chckbxProtocol.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		panel_1.add(chckbxProtocol, "cell 1 1,alignx left");
		
		checkBoxSerie = new JCheckBox("Serie");
		checkBoxSerie.setEnabled(false);
		checkBoxSerie.setSelected(true);
		panel_1.add(checkBoxSerie, "cell 1 1,alignx left");
		DefaultListModel<String> model = new DefaultListModel<String>();  
		list = new JList<String>();
		list.setModel(model);
		updateList();
		
		verticalGlue = Box.createVerticalGlue();
		verticalGlue.setPreferredSize(new Dimension(20, 0));
		panel_1.add(verticalGlue, "cell 0 2,grow");
		panel_1.add(new JScrollPane(list), "cell 0 3 2 2,grow");
		
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
	}


	/**
	 * Verrouille la frame
	 * @param b
	 */
	protected void setLock(boolean b) {
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


	public void createAndShowGUI(){
		JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
        } catch (Exception e) {
          System.out.println("Substance Graphite failed to initialize");
        }
		UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
		setTitle("Advanced import (from server to local)");
		setSize(490, 400);
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
				if(chckbxProject.isSelected()){
					if(getNewProjectName()!=null)
						project = getNewProjectName();
					else
						project = parts[serverdirlen];
				}
				break;
			case 2://patient
				if(chckbxProject.isSelected()){
					if(getNewProjectName()!=null)
						project = getNewProjectName();
					else
						project = parts[serverdirlen];
				}
				if(checkBoxPatient.isSelected())
					patient = parts[serverdirlen+1];
				break;
			case 3://acqdate
				if(chckbxProject.isSelected()){
					if(getNewProjectName()!=null)
						project = getNewProjectName();
					else
						project = parts[serverdirlen];
				}
				if(checkBoxPatient.isSelected())
					patient = parts[serverdirlen+1];
				if(checkBoxDate.isSelected())
					acqdate = parts[serverdirlen+2];
				break;
			case 4://protocol
				if(chckbxProject.isSelected()){
					if(getNewProjectName()!=null)
						project = getNewProjectName();
					else
						project = parts[serverdirlen];
				}
				if(checkBoxPatient.isSelected())
					patient = parts[serverdirlen+1];
				if(checkBoxDate.isSelected())
					acqdate = parts[serverdirlen+2];
				if(chckbxProtocol.isSelected())
					protocol = parts[serverdirlen+3];
				break;
			case 5://serie
				if(chckbxProject.isSelected()){
					if(getNewProjectName()!=null)
						project = getNewProjectName();
					else
						project = parts[serverdirlen];
				}
				if(checkBoxPatient.isSelected())
					patient = parts[serverdirlen+1];
				if(checkBoxDate.isSelected())
					acqdate = parts[serverdirlen+2];
				if(chckbxProtocol.isSelected())
					protocol = parts[serverdirlen+3];
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


	public static void main(String[] args){
		AdvancedImportFrame ad = new AdvancedImportFrame(null);
		ad.createAndShowGUI();
	}
}
