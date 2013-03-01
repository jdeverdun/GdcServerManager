package display;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import javax.swing.JToolBar;
import javax.swing.JTable;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import settings.WindowManager;

import display.containers.UserCreationPanel;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {
	
	private double screenWidth;
	private double screenHeight;
	
	// items
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOpen;
	private JMenu mnAdministration;
	private JMenu mnUsers;
	private JMenuItem mntmCreate;
	private JToolBar toolBar;
	
	
	public MainWindow() {
		
		// Init
		WindowManager.MAINWINDOW = this;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setScreenWidth(screenSize.getWidth());
		setScreenHeight(screenSize.getHeight());
		
		// Ajout des composants
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmOpen = new JMenuItem("Open");

		mnFile.add(mntmOpen);
		
		mnAdministration = new JMenu("Administration");
		mnAdministration.setActionCommand("Administration");
		menuBar.add(mnAdministration);
		
		mnUsers = new JMenu("Users");
		mnAdministration.add(mnUsers);
		
		mntmCreate = new JMenuItem("Create");
		mnUsers.add(mntmCreate);
		getContentPane().setLayout(new MigLayout("", "[][grow][][][grow][][][][][][][][][][][][][][][][][][][][][][][][][][132.00,fill]", "[][grow][grow][][][][][][][][][][][][][][][][][][][][][]"));
		
		toolBar = new JToolBar();
		getContentPane().add(toolBar, "cell 0 0 31 1,grow");
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, "cell 1 1 30 23,grow");
		panel.setLayout(new MigLayout("", "[410.00px,grow,fill]", "[186.00px,grow,fill]"));
		
		// Listeners
		
		mntmCreate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				UserCreationPanel ucreate = new UserCreationPanel();
				Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ucreate, (int)getX()+200,(int)getY()+150);
				ucreate.setPopupWindow(popup);
				popup.show();
			}
		});
	}
	
	 public void createAndShowGUI() {
        //Create and set up the window.
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       
        this.setSize(1000, 700);
        //Display the window.
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
	 
	public double getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(double screenWidth) {
		this.screenWidth = screenWidth;
	}

	public double getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(double screenHeight) {
		this.screenHeight = screenHeight;
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
				MainWindow mw = new MainWindow();
				
				UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
				mw.createAndShowGUI();
			}
		});
	}

}
