package display;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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

import display.containers.FileTree;
import display.containers.UserCreationPanel;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JSplitPane;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

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
	private JSplitPane distautresplitPane;
	private JSplitPane distworklocalPane;
	private JSplitPane treedistbuttonPane;
	private JSplitPane treeworkbuttonPane;
	private JSplitPane treelocalbuttonPane;
	private JTabbedPane ongletPane;
	private JButton btnRefresh;
	private JPanel buttonsDistpanel;
	private JButton btndistToWorkspace;
	private JButton btndistToLocal;
	private JPanel btnWorkpanel;
	private JPanel btnLocalpanel;
	private JButton btnWorkTolocal;
	private JButton btnDeleteWork;
	private JButton btnCreateWork;
	private JButton btnCreateLocal;
	private JButton btnDeleteLocal;
	private JButton btnlocalTowork;
	private FileTree fileTreeLocal;
	
	
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
		
		btnRefresh = new JButton("Refresh");
		toolBar.add(btnRefresh);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, "cell 1 1 30 23,grow");
		panel.setLayout(new MigLayout("", "[410.00px,grow,fill]", "[171.00,grow]"));
		
		ongletPane = new JTabbedPane(JTabbedPane.RIGHT);
		panel.add(ongletPane, "cell 0 0,grow");
		
		distautresplitPane = new JSplitPane();
		distautresplitPane.setResizeWeight(0.35);
		ongletPane.addTab("New tab", null, distautresplitPane, null);
		
		distworklocalPane = new JSplitPane();
		distworklocalPane.setResizeWeight(0.5);
		distautresplitPane.setRightComponent(distworklocalPane);
		
		treeworkbuttonPane = new JSplitPane();
		treeworkbuttonPane.setOneTouchExpandable(true);
		treeworkbuttonPane.setResizeWeight(0.95);
		treeworkbuttonPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		distworklocalPane.setLeftComponent(treeworkbuttonPane);
		
		btnWorkpanel = new JPanel();
		btnWorkpanel.setMinimumSize(new Dimension(10, 50));
		treeworkbuttonPane.setRightComponent(btnWorkpanel);
		GridBagLayout gbl_btnWorkpanel = new GridBagLayout();
		gbl_btnWorkpanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_btnWorkpanel.rowHeights = new int[]{0, 0};
		gbl_btnWorkpanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_btnWorkpanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		btnWorkpanel.setLayout(gbl_btnWorkpanel);
		
		btnCreateWork = new JButton("create");
		GridBagConstraints gbc_btnCreateWork = new GridBagConstraints();
		gbc_btnCreateWork.fill = GridBagConstraints.BOTH;
		gbc_btnCreateWork.insets = new Insets(0, 0, 0, 5);
		gbc_btnCreateWork.gridx = 0;
		gbc_btnCreateWork.gridy = 0;
		btnWorkpanel.add(btnCreateWork, gbc_btnCreateWork);
		
		btnDeleteWork = new JButton("delete");
		GridBagConstraints gbc_btnDeleteWork = new GridBagConstraints();
		gbc_btnDeleteWork.fill = GridBagConstraints.BOTH;
		gbc_btnDeleteWork.insets = new Insets(0, 0, 0, 5);
		gbc_btnDeleteWork.gridx = 1;
		gbc_btnDeleteWork.gridy = 0;
		btnWorkpanel.add(btnDeleteWork, gbc_btnDeleteWork);
		
		ImageIcon icon2=new ImageIcon(MainWindow.class.getResource("/images/forward.png"));
		Image img = icon2.getImage();  
		Image newimg = img.getScaledInstance(50, 20,  java.awt.Image.SCALE_SMOOTH);  
		ImageIcon icon = new ImageIcon(newimg); 
		btnWorkTolocal = new JButton(icon);
		GridBagConstraints gbc_btnWorkTolocal = new GridBagConstraints();
		gbc_btnWorkTolocal.fill = GridBagConstraints.BOTH;
		gbc_btnWorkTolocal.gridx = 2;
		gbc_btnWorkTolocal.gridy = 0;
		btnWorkpanel.add(btnWorkTolocal, gbc_btnWorkTolocal);
		
		treelocalbuttonPane = new JSplitPane();
		treelocalbuttonPane.setResizeWeight(0.95);
		treelocalbuttonPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		distworklocalPane.setRightComponent(treelocalbuttonPane);
		
		btnLocalpanel = new JPanel();
		btnLocalpanel.setMinimumSize(new Dimension(10, 50));
		treelocalbuttonPane.setRightComponent(btnLocalpanel);
		GridBagLayout gbl_btnLocalpanel = new GridBagLayout();
		gbl_btnLocalpanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_btnLocalpanel.rowHeights = new int[]{0, 0};
		gbl_btnLocalpanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_btnLocalpanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		btnLocalpanel.setLayout(gbl_btnLocalpanel);
		
		btnlocalTowork = new JButton("toWork");
		
		GridBagConstraints gbc_btnlocalTowork = new GridBagConstraints();
		gbc_btnlocalTowork.insets = new Insets(0, 0, 0, 5);
		gbc_btnlocalTowork.fill = GridBagConstraints.BOTH;
		gbc_btnlocalTowork.gridx = 0;
		gbc_btnlocalTowork.gridy = 0;
		btnLocalpanel.add(btnlocalTowork, gbc_btnlocalTowork);
		
		btnCreateLocal = new JButton("create");
		GridBagConstraints gbc_btnCreateLocal = new GridBagConstraints();
		gbc_btnCreateLocal.fill = GridBagConstraints.BOTH;
		gbc_btnCreateLocal.insets = new Insets(0, 0, 0, 5);
		gbc_btnCreateLocal.gridx = 1;
		gbc_btnCreateLocal.gridy = 0;
		btnLocalpanel.add(btnCreateLocal, gbc_btnCreateLocal);
		
		btnDeleteLocal = new JButton("delete");
		GridBagConstraints gbc_btnDeleteLocal = new GridBagConstraints();
		gbc_btnDeleteLocal.insets = new Insets(0, 0, 0, 5);
		gbc_btnDeleteLocal.fill = GridBagConstraints.BOTH;
		gbc_btnDeleteLocal.gridx = 2;
		gbc_btnDeleteLocal.gridy = 0;
		btnLocalpanel.add(btnDeleteLocal, gbc_btnDeleteLocal);
		
		fileTreeLocal = new FileTree(new File("."));
		treelocalbuttonPane.setLeftComponent(fileTreeLocal);
		
		treedistbuttonPane = new JSplitPane();
		treedistbuttonPane.setResizeWeight(0.95);
		treedistbuttonPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		treedistbuttonPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		treedistbuttonPane.setOneTouchExpandable(true);
		treedistbuttonPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		distautresplitPane.setLeftComponent(treedistbuttonPane);
		
		buttonsDistpanel = new JPanel();
		buttonsDistpanel.setMaximumSize(new Dimension(32767, 50));
		buttonsDistpanel.setMinimumSize(new Dimension(10, 50));
		treedistbuttonPane.setRightComponent(buttonsDistpanel);
		GridBagLayout gbl_buttonsDistpanel = new GridBagLayout();
		gbl_buttonsDistpanel.columnWidths = new int[]{0, 95, 0};
		gbl_buttonsDistpanel.rowHeights = new int[]{23, 0};
		gbl_buttonsDistpanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_buttonsDistpanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		buttonsDistpanel.setLayout(gbl_buttonsDistpanel);
		
		btndistToLocal = new JButton("toWorkspace");
		btndistToLocal.setAlignmentX(0.5f);
		GridBagConstraints gbc_btndistToLocal = new GridBagConstraints();
		gbc_btndistToLocal.fill = GridBagConstraints.BOTH;
		gbc_btndistToLocal.insets = new Insets(0, 0, 0, 5);
		gbc_btndistToLocal.gridx = 0;
		gbc_btndistToLocal.gridy = 0;
		buttonsDistpanel.add(btndistToLocal, gbc_btndistToLocal);
		
		btndistToWorkspace = new JButton("toWorkspace");
		btndistToWorkspace.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btndistToWorkspace = new GridBagConstraints();
		gbc_btndistToWorkspace.fill = GridBagConstraints.BOTH;
		gbc_btndistToWorkspace.gridx = 1;
		gbc_btndistToWorkspace.gridy = 0;
		buttonsDistpanel.add(btndistToWorkspace, gbc_btndistToWorkspace);
		
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
        setTitle(WindowManager.PROGRAM_NAME);
        setIconImage(new ImageIcon(this.getClass().getResource("/images/mainicon.png")).getImage());
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
