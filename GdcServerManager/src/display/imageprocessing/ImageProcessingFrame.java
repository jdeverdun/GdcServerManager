package display.imageprocessing;


import ij.gui.ProgressBar;

import java.awt.BorderLayout;
import java.awt.Color;
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
	public static final int WIDTH = 600;
	public static final int HEIGHT = 700;

	
	public ImageProcessingFrame(ArrayList<File> directories){
		super();
		getContentPane().setLayout(new MigLayout("", "[][][][][367.00,grow][114.00,grow]", "[][][][][][][grow][][][grow][][][grow][grow][][][][][][][][][grow]"));
		
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		getContentPane().add(panel, "cell 4 16 1 7,grow");
		
		
		
		
		JTextArea TextArea = new JTextArea();
		TextArea.setEditable(false);
		// init de la fenetre
		createAndShowGUI();
		JTextArea txtOutputDirectory = new JTextArea();
		txtOutputDirectory.setEditable(false);
		// Affiche les repertoires selectionnes
		for(File fi:directories)
		{
			//System.out.println(fi.getAbsolutePath());;
			txtOutputDirectory.append(fi.getAbsolutePath()+"\n");
			panel.add(txtOutputDirectory, "cell 0 0,growx");
			//String text = fi.getAbsolutePath();
	        //TextArea.append(text);
			//TextArea.append(fi.getAbsolutePath());
			//panel.add(TextArea);			
		}
		
	
	}

	private void createAndShowGUI() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(WIDTH,HEIGHT);
		this.setTitle(TITLE);
		this.setLocation(WindowManager.MAINWINDOW.getLocation());
		this.setIconImage(new ImageIcon(this.getClass().getResource("/images/logo32.png")).getImage());
		this.setVisible(true);
		
	}
	
	/*private void updateList()  
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
		if(directories==null || files.length==0)
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
	}*/
}
