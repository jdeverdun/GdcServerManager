package display;

import javax.imageio.ImageIO;
import javax.security.auth.Refreshable;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import model.ProjectStatistics;
import model.ServerInfo;
import model.User;
import net.miginfocom.swing.MigLayout;
import javax.swing.JToolBar;
import javax.swing.JTable;

import org.apache.commons.io.FileUtils;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;

import daemon.DaemonStatusThread;
import daemon.DecryptDaemon;
import daemon.DicomDaemon;
import daemon.DicomNode;
import daemon.NiftiDaemon;
import daemon.tools.ThreadPool;
import daemon.tools.firstNameDB;
import dao.DataBaseAdminDAO;
import dao.MySQLDataBaseAdminDAO;
import dao.MySQLUserDAO;
import dao.MySQLUserProjectDAO;
import dao.MySQLUserViewDAO;
import dao.UserDAO;
import dao.UserProjectDAO;
import dao.UserViewDAO;
import display.containers.DeleteUserPanel;
import display.containers.DicomSortConvertPanel;
import display.containers.FileManager;
import display.containers.LinkProjectPanel;
import display.containers.PassChangePanel;
import display.containers.ProgressPanel;
import display.containers.ProjectStatsSelecter;
import display.containers.ServerStatusPanel;
import display.containers.StatisticsPanel;
import display.containers.UserCreationPanel;
import display.containers.WaitingBarPanel;
import display.containers.RequestPanel;
import display.containers.viewer.ViewerPanel;
import display.containers.viewer.ViewerToolbar;

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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuBar;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.JPopupMenu;

public class MainWindow extends JFrame {
	
	private double screenWidth;
	private double screenHeight;
	private boolean isLock;
	private boolean daemonLaunched;
	// items
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOpenNifti;
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
	private FileManager fileTreeLocal;
	private FileManager fileTreeWork;
	private FileManager fileTreeDist;
	private JMenu mnServer;
	private JMenuItem mntmStartstop;
	private JMenuItem mntmDelete;
	private JMenu mnManage;
	private JMenuItem mntmLinkProject;
	private JMenuItem mntmUnlinkProject;
	private RequestPanel requetePanel;
	private ServerStatusPanel sstatusPanel;
	private DicomSortConvertPanel dicomSortConvertPanel;
	private JMenu mnEdit;
	private JMenuItem mntmPreferences;
	private ViewerPanel viewerPanel;
	private ProgressPanel progressBarPanel;
	private ViewerToolbar viewerToolbar;
	private JMenuItem mntmResetList;
	private JButton btnImport;
	private JMenu mnTools;
	private JMenu mnProject;
	private JMenuItem mntmStatistics;
	private StatisticsPanel statsPanel;
	private JMenuItem mntmImportFnDb;
	
	/**
	 * Si i = 0 : mode offline
	 * i = 1 : mode online
	 * @param i
	 */
	public MainWindow(int i) {
		if(i==0){
			UserProfile.CURRENT_USER = new User("guest", "guest", "guest@guest.guest", "guest", 0);
		}
		// Init
		init();
		
		// Ajout des composants
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnFile = new JMenu("File      ");
		menuBar.add(mnFile);
		

		mntmOpenNifti = new JMenuItem("Open Nifti");
		mntmOpenNifti.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String defdir = SystemSettings.APP_DIR.toString();
				if(UserProfile.LAST_SELECTED_DIR != null){
					defdir = UserProfile.LAST_SELECTED_DIR.toString();
				}
				JFileChooser fc = new JFileChooser(defdir);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return "Nifti images (nii,hdr/img,nii.gz)";
					}
					
					@Override
					public boolean accept(File f) {
						if(f.isDirectory())
							return true;
						if(f.toString().endsWith(".nii") || f.toString().endsWith(".img") || 
								f.toString().endsWith(".hdr") || 
								f.toString().endsWith(".nii.gz"))
							return true;
						return false;
						
					}
				});
				int retval = fc.showOpenDialog(MainWindow.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	final File file = fc.getSelectedFile();
	            	progressBarPanel.setVisible(true);
	            	getOngletPane().setSelectedComponent(getViewerPanel());
	            	revalidate();
	            	Thread tr = new Thread(new Runnable() {
						@Override
						public void run() {
							getViewerPanel().open(file.toPath());
							progressBarPanel.setVisible(false);
						}
					});
	            	tr.start();
	            }
			}
		});
		mnFile.add(mntmOpenNifti);
		
		mnAdministration = new JMenu("Administration");
		mnAdministration.setActionCommand("Administration");
		
		// On masque le bouton si l'utilisateur n'est pas admin
		if(UserProfile.CURRENT_USER.getLevel()<3)
			mnAdministration.setVisible(false);
		
		mnEdit = new JMenu("Edit     ");
		menuBar.add(mnEdit);

		
		mntmPreferences = new JMenuItem("Preferences");
		mnEdit.add(mntmPreferences);
		mnTools = new JMenu("Tools      ");
		menuBar.add(mnTools);
		menuBar.add(mnAdministration);
		
		mnUsers = new JMenu("Users         ");
		mnUsers.setToolTipText("");
		mnAdministration.add(mnUsers);
		
		mntmCreate = new JMenuItem("Create");
		mntmCreate.setToolTipText("Create new user");
		mnUsers.add(mntmCreate);
		
		mntmDelete = new JMenuItem("Delete");
		mntmDelete.setToolTipText("Remove existing user");
		mnUsers.add(mntmDelete);
		
		mnManage = new JMenu("Manage");
		mnManage.setToolTipText("Manage users permissions - params ");
		mnUsers.add(mnManage);
		
		mntmLinkProject = new JMenuItem("Link project");
		mntmLinkProject.setToolTipText("Add an access to a specific project to the specified user");
		mnManage.add(mntmLinkProject);
		
		mntmUnlinkProject = new JMenuItem("Unlink project");
		mntmUnlinkProject.setToolTipText("Remove existing link between user and a project");
		mnManage.add(mntmUnlinkProject);
		
		mnServer = new JMenu("Server");
		mnAdministration.add(mnServer);
		
		mntmStartstop = new JMenuItem("Start");
		mnServer.add(mntmStartstop);
		
		mntmResetList = new JMenuItem("Reset");
		mntmResetList.setToolTipText("Delete existing backup server files (the server needs to be stopped)");
		mnServer.add(mntmResetList);
		
		mntmImportFnDb = new JMenuItem("Import FN DB");
		mntmImportFnDb.setToolTipText("Flood the first name database with selected text file.");
		mnServer.add(mntmImportFnDb);
		getContentPane().setLayout(new MigLayout("", "[][grow][][][grow][][][][][][][][][][][][][][][][][][][][][][][][][][132.00,fill]", "[][grow][grow][][][][][][][][][][][][][][][][][][][][][]"));
		mnTools.setVisible(false);
		toolBar = new JToolBar();
		//getContentPane().add(toolBar, BorderLayout.NORTH);
		getContentPane().add(toolBar, "cell 0 0 31 1,grow");
		ImageIcon icon;
		Image img;
		Image newimg;
		ImageIcon icon2;
		// si on a bien un utilisateur connecte on rajoute les boutons refresh
		if(i!=0){
			icon=new ImageIcon(MainWindow.class.getResource("/images/refresh.png"));
			img = icon.getImage();  
			newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
			icon2 = new ImageIcon(newimg); 
			btnRefresh = new JButton(icon2);
			btnRefresh.setToolTipText("Refresh (Views & conf files)");
			toolBar.add(btnRefresh);
		}
		
		if(UserProfile.CURRENT_USER.getLevel()==3){
			//si mode admin on ajoute le bouton d'import
			icon=new ImageIcon(MainWindow.class.getResource("/images/import.png"));
			img = icon.getImage();  
			newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
			icon2 = new ImageIcon(newimg); 
			btnImport = new JButton(icon2);
			btnImport.setToolTipText("Import data from a directory to the server");
			toolBar.add(btnImport);
			btnImport.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					ImportFrame imf = new ImportFrame();
					imf.createAndShowGUI();
				}
			});
		}
		

		JPanel panel = new JPanel();
		getContentPane().add(panel, "cell 1 1 30 23,grow");
		panel.setLayout(new MigLayout("", "[][144.00,left][158.00][719px,grow]", "[525px,grow][][2px]"));
		
		ongletPane = new JTabbedPane(JTabbedPane.RIGHT);

		panel.add(ongletPane, "cell 0 0 4 1,grow");
		
		distautresplitPane = new JSplitPane();
		distautresplitPane.setResizeWeight(0.35);
		ongletPane.addTab("Explorer", null, distautresplitPane, null);
		requetePanel = new RequestPanel();
		ongletPane.addTab("Selecter", null, requetePanel,
                "Retrieve selective DATA");

		// nifti viewer
		viewerPanel = new ViewerPanel();
		viewerToolbar = new ViewerToolbar(viewerPanel);
		viewerToolbar.setVisible(false);
		
		toolBar.add(viewerToolbar);
		
		ongletPane.addTab("Viewer", null, viewerPanel,
                "Viewer for images");
		
		ongletPane.addChangeListener(new ChangeListener() {
		      public void stateChanged(ChangeEvent changeEvent) {
		        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
		        int index = sourceTabbedPane.getSelectedIndex();
		        if(index == 2){
		        	viewerToolbar.setVisible(true);
		        }else{
		        	viewerToolbar.setVisible(false);
		        }
		      }
		    });
		// converter
		dicomSortConvertPanel = new DicomSortConvertPanel();
		ongletPane.addTab("Sort & convert", null, dicomSortConvertPanel,
                "Sort DICOM & convert to nifti");
		
		progressBarPanel = new ProgressPanel();
		progressBarPanel.setSize(50, 20);
		progressBarPanel.setVisible(false);
		
		if(i==0)
			panel.add(progressBarPanel, "cell 1 1,grow");
		else
			panel.add(progressBarPanel, "cell 1 1 2 1,grow");
		//ongletPane.setEnabledAt(2, false); 
		if(i!=0){
			
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
			
			fileTreeWork = new FileManager(this,SystemSettings.SERVER_INFO.getServerDir(),1);
			treeworkbuttonPane.setLeftComponent(fileTreeWork.getPane());
			icon2=new ImageIcon(MainWindow.class.getResource("/images/createFolder.png"));
			img = icon2.getImage();  
			newimg = img.getScaledInstance(30,30,  java.awt.Image.SCALE_SMOOTH);  
			icon = new ImageIcon(newimg); 
			btnCreateWork = new JButton(icon);
			GridBagConstraints gbc_btnCreateWork = new GridBagConstraints();
			gbc_btnCreateWork.fill = GridBagConstraints.BOTH;
			gbc_btnCreateWork.insets = new Insets(0, 0, 0, 5);
			gbc_btnCreateWork.gridx = 0;
			gbc_btnCreateWork.gridy = 0;
			btnWorkpanel.add(btnCreateWork, gbc_btnCreateWork);
			
			icon2=new ImageIcon(MainWindow.class.getResource("/images/trash.png"));
			img = icon2.getImage();  
			newimg = img.getScaledInstance(30,30,  java.awt.Image.SCALE_SMOOTH);  
			icon = new ImageIcon(newimg); 
			btnDeleteWork = new JButton(icon);
			GridBagConstraints gbc_btnDeleteWork = new GridBagConstraints();
			gbc_btnDeleteWork.fill = GridBagConstraints.BOTH;
			gbc_btnDeleteWork.insets = new Insets(0, 0, 0, 5);
			gbc_btnDeleteWork.gridx = 1;
			gbc_btnDeleteWork.gridy = 0;
			btnWorkpanel.add(btnDeleteWork, gbc_btnDeleteWork);
			
			icon2=new ImageIcon(MainWindow.class.getResource("/images/toWork.png"));
			img = icon2.getImage();  
			newimg = img.getScaledInstance(30,30,  java.awt.Image.SCALE_SMOOTH);  
			icon = new ImageIcon(newimg); 
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
			
			icon2=new ImageIcon(MainWindow.class.getResource("/images/toLeft.png"));
			img = icon2.getImage();  
			newimg = img.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH);  
			icon = new ImageIcon(newimg); 
			btnlocalTowork = new JButton(icon);
			
			GridBagConstraints gbc_btnlocalTowork = new GridBagConstraints();
			gbc_btnlocalTowork.insets = new Insets(0, 0, 0, 5);
			gbc_btnlocalTowork.fill = GridBagConstraints.BOTH;
			gbc_btnlocalTowork.gridx = 0;
			gbc_btnlocalTowork.gridy = 0;
			btnLocalpanel.add(btnlocalTowork, gbc_btnlocalTowork);
			
			icon2=new ImageIcon(MainWindow.class.getResource("/images/createFolder.png"));
			img = icon2.getImage();  
			newimg = img.getScaledInstance(30,30,  java.awt.Image.SCALE_SMOOTH);  
			icon = new ImageIcon(newimg); 
			btnCreateLocal = new JButton(icon);
			GridBagConstraints gbc_btnCreateLocal = new GridBagConstraints();
			gbc_btnCreateLocal.fill = GridBagConstraints.BOTH;
			gbc_btnCreateLocal.insets = new Insets(0, 0, 0, 5);
			gbc_btnCreateLocal.gridx = 1;
			gbc_btnCreateLocal.gridy = 0;
			btnLocalpanel.add(btnCreateLocal, gbc_btnCreateLocal);
			
			icon2=new ImageIcon(MainWindow.class.getResource("/images/trash.png"));
			img = icon2.getImage();  
			newimg = img.getScaledInstance(30,30,  java.awt.Image.SCALE_SMOOTH);  
			icon = new ImageIcon(newimg); 
			btnDeleteLocal = new JButton(icon);
			GridBagConstraints gbc_btnDeleteLocal = new GridBagConstraints();
			gbc_btnDeleteLocal.insets = new Insets(0, 0, 0, 5);
			gbc_btnDeleteLocal.fill = GridBagConstraints.BOTH;
			gbc_btnDeleteLocal.gridx = 2;
			gbc_btnDeleteLocal.gridy = 0;
			btnLocalpanel.add(btnDeleteLocal, gbc_btnDeleteLocal);
			
			
			//fileTreeLocal = new FileTree(new File("."));
			//treelocalbuttonPane.setLeftComponent(fileTreeLocal);
			fileTreeLocal = new FileManager(this);
			treelocalbuttonPane.setLeftComponent(fileTreeLocal.getPane());
			treedistbuttonPane = new JSplitPane();
			treedistbuttonPane.setResizeWeight(0.95);
			treedistbuttonPane.setAlignmentX(Component.CENTER_ALIGNMENT);
			treedistbuttonPane.setAlignmentY(Component.CENTER_ALIGNMENT);
			treedistbuttonPane.setOneTouchExpandable(true);
			treedistbuttonPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			fileTreeDist = new FileManager(this,SystemSettings.SERVER_INFO.getServerDir(),2);
			treedistbuttonPane.setLeftComponent(fileTreeDist.getPane());
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
			
			icon2=new ImageIcon(MainWindow.class.getResource("/images/download2.png"));
			img = icon2.getImage();  
			newimg = img.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH);  
			icon = new ImageIcon(newimg); 
			btndistToLocal = new JButton(icon);
			btndistToLocal.setAlignmentX(Component.CENTER_ALIGNMENT);
			GridBagConstraints gbc_btndistToLocal = new GridBagConstraints();
			gbc_btndistToLocal.fill = GridBagConstraints.BOTH;
			gbc_btndistToLocal.insets = new Insets(0, 0, 0, 5);
			gbc_btndistToLocal.gridx = 0;
			gbc_btndistToLocal.gridy = 0;
			buttonsDistpanel.add(btndistToLocal, gbc_btndistToLocal);
			
			icon2=new ImageIcon(MainWindow.class.getResource("/images/toWork.png"));
			img = icon2.getImage();  
			newimg = img.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH);  
			icon = new ImageIcon(newimg); 
			btndistToWorkspace = new JButton(icon);
			btndistToWorkspace.setAlignmentX(Component.CENTER_ALIGNMENT);
			GridBagConstraints gbc_btndistToWorkspace = new GridBagConstraints();
			gbc_btndistToWorkspace.fill = GridBagConstraints.BOTH;
			gbc_btndistToWorkspace.gridx = 1;
			gbc_btndistToWorkspace.gridy = 0;
			buttonsDistpanel.add(btndistToWorkspace, gbc_btndistToWorkspace);
			
			sstatusPanel = new ServerStatusPanel();
			ongletPane.addTab("Monitor", null, sstatusPanel,
	                "Status of daemons Threads");
			
			SystemSettings.DAEMON_STATUS_THREAD = new DaemonStatusThread(sstatusPanel);
			SystemSettings.DAEMON_STATUS_THREAD.start();
			
			mnTools.setVisible(true);
			mnProject = new JMenu("Project");
			
			mnTools.add(mnProject);
			
			mntmStatistics = new JMenuItem("Statistics");
			mnProject.add(mntmStatistics);
			// Listeners
			mntmStatistics.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					ProjectStatsSelecter pstat = new ProjectStatsSelecter();
					Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, pstat, (int)getX()+200,(int)getY()+150);
					pstat.setPopupWindow(popup);
					popup.show();
				}
			});
			btnRefresh.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					UserProjectDAO pdao = new MySQLUserProjectDAO();
					try {
						UserProfile.CURRENT_USER.setProjects(pdao.getProjectsForUser(UserProfile.CURRENT_USER.getId()));
					} catch (SQLException e1) {
						WindowManager.mwLogger.log(Level.WARNING, "Refreshing error", e1);
					}
					getFileTreeDist().refresh();
					getFileTreeLocal().refresh();
					getFileTreeWork().refresh();
					// Refresh conf
					SystemSettings.SERVER_INFO.refresh();
				}
			});

			mntmCreate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					UserCreationPanel ucreate = new UserCreationPanel();
					Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ucreate, (int)getX()+200,(int)getY()+150);
					ucreate.setPopupWindow(popup);
					popup.show();
				}
			});
			
			mntmStartstop.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(!daemonLaunched){
						startDeamons();
						mntmResetList.setVisible(false);
					}else{
						stopDaemons();
						mntmResetList.setVisible(true);
					}
				}
			});
			
			mntmImportFnDb.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					firstNameDB.importDB();
				}
			});

			mntmResetList.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try{
						FileUtils.deleteDirectory(new File(SystemSettings.APP_DIR+File.separator+ServerInfo.BACKUP_DIR));
						WindowManager.mwLogger.log(Level.INFO, "Server backup files deleted.");
					}catch(Exception e){
						WindowManager.mwLogger.log(Level.SEVERE, "Can't clear backup file", e);
						JOptionPane.showMessageDialog(MainWindow.this,
							    "Error during the reset.",
							    "Reset error",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			mntmDelete.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					DeleteUserPanel udelete = new DeleteUserPanel();
					Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, udelete, (int)getX()+200,(int)getY()+150);
					udelete.setPopupWindow(popup);
					popup.show();
					
				}
			});
			
			mntmLinkProject.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					LinkProjectPanel ulink = new LinkProjectPanel(0); // mode creation de liens
					Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ulink, (int)getX()+200,(int)getY()+150);
					ulink.setPopupWindow(popup);
					popup.show();
				}
			});
			
			mntmUnlinkProject.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					LinkProjectPanel ulink = new LinkProjectPanel(1); // mode creation de liens
					Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ulink, (int)getX()+200,(int)getY()+150);
					ulink.setPopupWindow(popup);
					popup.show();
				}
			});
			
			btnWorkTolocal.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setLock(true);
					WaitingBarPanel ppanel = new WaitingBarPanel(getFileTreeLocal()); // mode creation de liens
					ppanel.setTitle("Copying ...");
					JFrame tmp = new JFrame();
					tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
					final Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
					tmp = null;
					// Thread pour la copie
					Thread copyThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								getFileTreeWork().copySelectedFilesTo(getFileTreeLocal().getCurrentDir());
								popup.hide();
								setLock(false);
								resfreshFileTree();
								WindowManager.mwLogger.log(Level.INFO, "Copy Ok");
							} catch (IOException e) {
								setLock(false);
								popup.hide();
								JDialog.setDefaultLookAndFeelDecorated(true);
								JOptionPane.showMessageDialog(MainWindow.this,
									    "Error during the copy.",
									    "Copy error",
									    JOptionPane.ERROR_MESSAGE);
								WindowManager.mwLogger.log(Level.SEVERE, "Copy error", e);
							}
						}
	
						private void resfreshFileTree() {
							getFileTreeLocal().refresh();
							getFileTreeWork().refresh();
						}
					});
					ppanel.setPopup(popup);
					popup.show();
					
					copyThread.start();				
				}
			});
			btnlocalTowork.addActionListener(new ActionListener() {
						
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setLock(true);
					WaitingBarPanel ppanel = new WaitingBarPanel(getFileTreeLocal()); // mode creation de liens
					ppanel.setTitle("Copying ...");
					JFrame tmp = new JFrame();
					tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
					final Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
					tmp = null;
					// Thread pour la copie
					Thread copyThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								getFileTreeLocal().copySelectedFilesTo(getFileTreeWork().getCurrentDir());
								popup.hide();
								setLock(false);
								resfreshFileTree();
								WindowManager.mwLogger.log(Level.INFO, "Copy Ok");
							} catch (IOException e) {
								setLock(false);
								popup.hide();
								JDialog.setDefaultLookAndFeelDecorated(true);
								JOptionPane.showMessageDialog(MainWindow.this,
									    "Error during the copy.",
									    "Copy error",
									    JOptionPane.ERROR_MESSAGE);
								WindowManager.mwLogger.log(Level.SEVERE, "Copy error", e);
							}
						}
	
						private void resfreshFileTree() {
							getFileTreeLocal().refresh();
							getFileTreeWork().refresh();
						}
					});
					ppanel.setPopup(popup);
					popup.show();
					
					copyThread.start();				
				}
			});
			btndistToWorkspace.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setLock(true);
					final WaitingBarPanel ppanel = new WaitingBarPanel(getFileTreeDist()); // mode creation de liens
					final String title = "Copy & Decrypt ...";
					ppanel.setTitle(title);
					JFrame tmp = new JFrame();
					tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
					final Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
					tmp = null;
					// Thread pour la copie
					Thread copyThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								// si on est bien dans un repertoire dans lequel on peut ecrire
								if(!getFileTreeWork().getCurrentDir().canWrite()){
									popup.hide();
									setLock(false);
									resfreshFileTree();
									return;
								}
								getFileTreeDist().copySelectedFilesAndDecryptTo(getFileTreeWork().getCurrentDir());
								popup.hide();
								setLock(false);
								resfreshFileTree();
								WindowManager.mwLogger.log(Level.INFO, "Copy Ok");
							} catch (IOException e) {
								setLock(false);
								popup.hide();
								JDialog.setDefaultLookAndFeelDecorated(true);
								JOptionPane.showMessageDialog(MainWindow.this,
									    "Error during the copy.",
									    "Copy error",
									    JOptionPane.ERROR_MESSAGE);
								WindowManager.mwLogger.log(Level.SEVERE, "Copy error", e);
							}
						}
	
						private void resfreshFileTree() {
							getFileTreeDist().refresh();
							getFileTreeWork().refresh();
						}
						
					});
					ppanel.setPopup(popup);
					ppanel.setParentObject(getFileTreeDist());
					popup.show();
					
					copyThread.start();	
					
					// On attend que tout se termine
					Thread updateStatusThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							while(isLock){
								ppanel.setTitle(title+"<br /><center>"+(SystemSettings.DECRYPT_DAEMON.getTotalEncryptedFile()-SystemSettings.DECRYPT_DAEMON.getFileToDecrypt().size())+" / "+SystemSettings.DECRYPT_DAEMON.getTotalEncryptedFile()+"</center>");
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									WindowManager.mwLogger.log(Level.WARNING, "Sleeping error", e);
								}
							}
						}
					});
					updateStatusThread.start();
				}
			});
			
			btndistToLocal.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setLock(true);
					final WaitingBarPanel ppanel = new WaitingBarPanel(getFileTreeDist()); // mode creation de liens
					final String title = "Copy & Decrypt ...";
					ppanel.setTitle(title);
					JFrame tmp = new JFrame();
					tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
					final Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
					tmp = null;
					// Thread pour la copie
					Thread copyThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								getFileTreeDist().copySelectedFilesAndDecryptTo(getFileTreeLocal().getCurrentDir());
								popup.hide();
								setLock(false);
								resfreshFileTree();
								WindowManager.mwLogger.log(Level.INFO, "Copy Ok");
							} catch (IOException e) {
								setLock(false);
								popup.hide();
								JDialog.setDefaultLookAndFeelDecorated(true);
								JOptionPane.showMessageDialog(MainWindow.this,
									    "Error during the copy.",
									    "Copy error",
									    JOptionPane.ERROR_MESSAGE);
								WindowManager.mwLogger.log(Level.SEVERE, "Copy error", e);
							}
						}
	
						private void resfreshFileTree() {
							getFileTreeDist().refresh();
							getFileTreeLocal().refresh();
						}
						
					});
					ppanel.setPopup(popup);
					ppanel.setParentObject(getFileTreeDist());
					popup.show();
					
					copyThread.start();	
					
					// On attend que tout se termine
					Thread updateStatusThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							while(isLock){
								ppanel.setTitle(title+"<br /><center>"+(SystemSettings.DECRYPT_DAEMON.getTotalEncryptedFile()-SystemSettings.DECRYPT_DAEMON.getFileToDecrypt().size())+" / "+SystemSettings.DECRYPT_DAEMON.getTotalEncryptedFile()+"</center>");
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									WindowManager.mwLogger.log(Level.WARNING, "Sleeping error", e);
								}
							}
						}
					});
					updateStatusThread.start();
				}
			});
			
			btnDeleteLocal.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setLock(true);
					WaitingBarPanel ppanel = new WaitingBarPanel(getFileTreeLocal()); // mode creation de liens
					ppanel.setTitle("Deleting ...");
					JFrame tmp = new JFrame();
					tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
					final Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
					tmp = null;
					// Thread pour la copie
					Thread copyThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								getFileTreeLocal().deleteSelectedFiles();
								popup.hide();
								setLock(false);
								resfreshFileTree();
								WindowManager.mwLogger.log(Level.INFO, "Deletion Ok");
							} catch (IOException e) {
								setLock(false);
								popup.hide();
								JDialog.setDefaultLookAndFeelDecorated(true);
								JOptionPane.showMessageDialog(MainWindow.this,
									    "Error during the deletion.",
									    "Copy error",
									    JOptionPane.ERROR_MESSAGE);
								WindowManager.mwLogger.log(Level.SEVERE, "Deleting error", e);
							}
						}
	
						private void resfreshFileTree() {
							getFileTreeLocal().refresh();
						}
					});
					ppanel.setPopup(popup);
					popup.show();
					
					copyThread.start();				
				}
			});
			
			btnDeleteWork.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setLock(true);
					WaitingBarPanel ppanel = new WaitingBarPanel(getFileTreeWork()); // mode creation de liens
					ppanel.setTitle("Deleting ...");
					JFrame tmp = new JFrame();
					tmp.setLocationRelativeTo(null);// pour recupere la position optimale du popup
					final Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, ppanel, (int)tmp.getX()-20,(int)tmp.getY()-50);
					tmp = null;
					// Thread pour la suppresion
					Thread copyThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								getFileTreeWork().deleteSelectedFiles();
								popup.hide();
								setLock(false);
								resfreshFileTree();
								WindowManager.mwLogger.log(Level.INFO, "Deletion Ok");
							} catch (IOException e) {
								setLock(false);
								popup.hide();
								JDialog.setDefaultLookAndFeelDecorated(true);
								JOptionPane.showMessageDialog(MainWindow.this,
									    "Error during the deletion.",
									    "Copy error",
									    JOptionPane.ERROR_MESSAGE);
								WindowManager.mwLogger.log(Level.SEVERE, "Deleting error", e);
							}
						}
	
						private void resfreshFileTree() {
							getFileTreeWork().refresh();
						}
					});
					ppanel.setPopup(popup);
					popup.show();
					
					copyThread.start();				
				}
			});
			
			btnCreateLocal.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setLock(true);
					JDialog.setDefaultLookAndFeelDecorated(true);
					String response = JOptionPane.showInputDialog(null,
							  "Folder creation",
							  "Folder name ?",
							  JOptionPane.QUESTION_MESSAGE);
					if(response!=null && !response.equals("")){
						File fi = new File(getFileTreeLocal().getCurrentDir() + "/" + response);
						if(!fi.exists()){
							fi.mkdir();
							getFileTreeLocal().refresh();
						}else{
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(MainWindow.this,
								    "Directory already exists.",
								    "Create dir error",
								    JOptionPane.ERROR_MESSAGE);
						}
					}	
					setLock(false);
				}
			});
			btnCreateWork.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(getFileTreeWork().getCurrentDir().getAbsolutePath().equals(SystemSettings.SERVER_INFO.getServerDir().toString())){
						return;
					}
					setLock(true);
					JDialog.setDefaultLookAndFeelDecorated(true);
					String response = JOptionPane.showInputDialog(null,
							  "Folder creation",
							  "Folder name ?",
							  JOptionPane.QUESTION_MESSAGE);
					if(!response.equals("")){
						File fi = new File(getFileTreeWork().getCurrentDir() + "/" + response);
						if(!fi.exists()){
							fi.mkdir();
							getFileTreeWork().refresh();
						}else{
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(MainWindow.this,
								    "Directory already exists.",
								    "Create dir error",
								    JOptionPane.ERROR_MESSAGE);
						}
					}	
					setLock(false);
				}
			});
		}else{
			// si mode guest
			ongletPane.setEnabledAt(0, false);
			ongletPane.setEnabledAt(1, false);
			ongletPane.setSelectedIndex(2);
		}
		mntmPreferences.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final SettingsFrame sframe = new SettingsFrame();
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						sframe.createAndShowGUI();
					}
				});
			}
		});
	}
	
	

	/**
	 * Methode permettant d'initialiser certaines variables 
	 * et lancer des daemon au demarrage
	 */
	private void init() {
		ThreadPool.launchThreadPool();
		// On nettoie le repertoire temporaire quand on quitte le programme
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		    	forceStopDaemons();
		    	ThreadPool.stopThreadPool();
		    	try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(SystemSettings.SERVER_INFO.getTempDir())) {
			    	for(Path p:directoryStream){
			    		if(!p.endsWith(".") && p.toFile().isDirectory())
			    			FileUtils.deleteDirectory(p.toFile());
			    		else
			    			if(!p.endsWith("."))
			    				p.toFile().delete();
			    	}
		    	}catch(Exception e){
		    		WindowManager.mwLogger.log(Level.SEVERE, "Shutdown error", e);
		    	}
		    }
		}));
		WindowManager.MAINWINDOW = this;
		File logfiledir = new File(SystemSettings.APP_DIR+File.separator+SystemSettings.logdir);
		if(!logfiledir.exists())
			logfiledir.mkdir();
		WindowManager.mwLogger = Logger.getLogger("flogger");
		try {
			FileHandler fh=new FileHandler(logfiledir.toString()+File.separator+WindowManager.PROGRAM_NAME+".log",1000000000,1);// taille max 1 Go
			fh.setFormatter(new SimpleFormatter());
			WindowManager.mwLogger.addHandler(fh);
			WindowManager.mwLogger.setLevel(Level.INFO);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setScreenWidth(screenSize.getWidth());
		setScreenHeight(screenSize.getHeight());
		daemonLaunched = false;
		isLock = false;
		// On lance par defaut le decrypteur
		SystemSettings.DECRYPT_DAEMON = new DecryptDaemon();
		SystemSettings.DECRYPT_DAEMON.start();
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
		// On change le mdp si c'est la premiere connexion
		if(UserProfile.CURRENT_USER.firstConnect() == 1){
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					JFrame.setDefaultLookAndFeelDecorated(true);
					try {
				          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
			        } catch (Exception e) {
			          System.out.println("Substance Graphite failed to initialize");
			          WindowManager.mwLogger.log(Level.WARNING, "Substance Graphite failed to initialize", e);
			        }
					UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
					PassChangePanel pchange = new PassChangePanel();
					Popup popup = PopupFactory.getSharedInstance().getPopup(MainWindow.this, pchange, (int)getX()+200,(int)getY()+150);
					pchange.setPopup(popup);
					popup.show();
				}
			});
		}
    }
	 


	public JMenu getMnFile() {
		return mnFile;
	}

	public void setMnFile(JMenu mnFile) {
		this.mnFile = mnFile;
	}

	public JMenuItem getMntmOpen() {
		return mntmOpenNifti;
	}

	public void setMntmOpen(JMenuItem mntmOpen) {
		this.mntmOpenNifti = mntmOpen;
	}

	public JMenu getMnAdministration() {
		return mnAdministration;
	}

	public void setMnAdministration(JMenu mnAdministration) {
		this.mnAdministration = mnAdministration;
	}

	public JMenu getMnUsers() {
		return mnUsers;
	}

	public void setMnUsers(JMenu mnUsers) {
		this.mnUsers = mnUsers;
	}

	public JMenuItem getMntmCreate() {
		return mntmCreate;
	}

	public void setMntmCreate(JMenuItem mntmCreate) {
		this.mntmCreate = mntmCreate;
	}

	public JToolBar getToolBar() {
		return toolBar;
	}

	public void setToolBar(JToolBar toolBar) {
		this.toolBar = toolBar;
	}

	public ProgressPanel getProgressBarPanel() {
		return progressBarPanel;
	}



	public void setProgressBarPanel(ProgressPanel progressBarPanel) {
		this.progressBarPanel = progressBarPanel;
	}



	public JSplitPane getDistautresplitPane() {
		return distautresplitPane;
	}

	public void setDistautresplitPane(JSplitPane distautresplitPane) {
		this.distautresplitPane = distautresplitPane;
	}

	public JSplitPane getDistworklocalPane() {
		return distworklocalPane;
	}

	public void setDistworklocalPane(JSplitPane distworklocalPane) {
		this.distworklocalPane = distworklocalPane;
	}

	public JSplitPane getTreedistbuttonPane() {
		return treedistbuttonPane;
	}

	public void setTreedistbuttonPane(JSplitPane treedistbuttonPane) {
		this.treedistbuttonPane = treedistbuttonPane;
	}

	public JSplitPane getTreeworkbuttonPane() {
		return treeworkbuttonPane;
	}

	public void setTreeworkbuttonPane(JSplitPane treeworkbuttonPane) {
		this.treeworkbuttonPane = treeworkbuttonPane;
	}

	public JSplitPane getTreelocalbuttonPane() {
		return treelocalbuttonPane;
	}

	public ServerStatusPanel getSstatusPanel() {
		return sstatusPanel;
	}



	public RequestPanel getRequetePanel() {
		return requetePanel;
	}



	public void setRequetePanel(RequestPanel requetePanel) {
		this.requetePanel = requetePanel;
	}



	public DicomSortConvertPanel getDicomSortConvertPanel() {
		return dicomSortConvertPanel;
	}



	public void setDicomSortConvertPanel(DicomSortConvertPanel dicomSortConvertPanel) {
		this.dicomSortConvertPanel = dicomSortConvertPanel;
	}



	public ViewerPanel getViewerPanel() {
		return viewerPanel;
	}



	public void setViewerPanel(ViewerPanel viewerPanel) {
		this.viewerPanel = viewerPanel;
	}



	public void setSstatusPanel(ServerStatusPanel sstatusPanel) {
		this.sstatusPanel = sstatusPanel;
	}



	public void setTreelocalbuttonPane(JSplitPane treelocalbuttonPane) {
		this.treelocalbuttonPane = treelocalbuttonPane;
	}

	public JTabbedPane getOngletPane() {
		return ongletPane;
	}

	public void setOngletPane(JTabbedPane ongletPane) {
		this.ongletPane = ongletPane;
	}

	public JButton getBtnRefresh() {
		return btnRefresh;
	}

	public void setBtnRefresh(JButton btnRefresh) {
		this.btnRefresh = btnRefresh;
	}

	public JPanel getButtonsDistpanel() {
		return buttonsDistpanel;
	}

	public void setButtonsDistpanel(JPanel buttonsDistpanel) {
		this.buttonsDistpanel = buttonsDistpanel;
	}

	public JButton getBtndistToWorkspace() {
		return btndistToWorkspace;
	}

	public void setBtndistToWorkspace(JButton btndistToWorkspace) {
		this.btndistToWorkspace = btndistToWorkspace;
	}

	public JButton getBtndistToLocal() {
		return btndistToLocal;
	}

	public void setBtndistToLocal(JButton btndistToLocal) {
		this.btndistToLocal = btndistToLocal;
	}

	public JPanel getBtnWorkpanel() {
		return btnWorkpanel;
	}

	public void setBtnWorkpanel(JPanel btnWorkpanel) {
		this.btnWorkpanel = btnWorkpanel;
	}

	public JPanel getBtnLocalpanel() {
		return btnLocalpanel;
	}

	public void setBtnLocalpanel(JPanel btnLocalpanel) {
		this.btnLocalpanel = btnLocalpanel;
	}

	public JButton getBtnWorkTolocal() {
		return btnWorkTolocal;
	}

	public void setBtnWorkTolocal(JButton btnWorkTolocal) {
		this.btnWorkTolocal = btnWorkTolocal;
	}

	public JButton getBtnDeleteWork() {
		return btnDeleteWork;
	}

	public void setBtnDeleteWork(JButton btnDeleteWork) {
		this.btnDeleteWork = btnDeleteWork;
	}

	public JButton getBtnCreateWork() {
		return btnCreateWork;
	}

	public void setBtnCreateWork(JButton btnCreateWork) {
		this.btnCreateWork = btnCreateWork;
	}

	public JButton getBtnCreateLocal() {
		return btnCreateLocal;
	}

	public void setBtnCreateLocal(JButton btnCreateLocal) {
		this.btnCreateLocal = btnCreateLocal;
	}

	public JButton getBtnDeleteLocal() {
		return btnDeleteLocal;
	}

	public void setBtnDeleteLocal(JButton btnDeleteLocal) {
		this.btnDeleteLocal = btnDeleteLocal;
	}

	public JButton getBtnlocalTowork() {
		return btnlocalTowork;
	}

	public void setBtnlocalTowork(JButton btnlocalTowork) {
		this.btnlocalTowork = btnlocalTowork;
	}

	public FileManager getFileTreeLocal() {
		return fileTreeLocal;
	}

	public void setFileTreeLocal(FileManager fileTreeLocal) {
		this.fileTreeLocal = fileTreeLocal;
	}

	public FileManager getFileTreeWork() {
		return fileTreeWork;
	}

	public void setFileTreeWork(FileManager fileTreeWork) {
		this.fileTreeWork = fileTreeWork;
	}

	public FileManager getFileTreeDist() {
		return fileTreeDist;
	}

	public void setFileTreeDist(FileManager fileTreeDist) {
		this.fileTreeDist = fileTreeDist;
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


	public void startDeamons(){
		// on ne lance les daemon que si on arrive a charger la base des prenom 
		// pour limiter le risque d'integrer sur le serveur des donnees non anonymise
		if(firstNameDB.init()){
			if(SystemSettings.NIFTI_DAEMON!=null)
				SystemSettings.NIFTI_DAEMON = null;
			if(SystemSettings.DICOM_DAEMON!=null)
				SystemSettings.DICOM_DAEMON = null;
			// On lance le daemon Nifti
			SystemSettings.NIFTI_DAEMON = new NiftiDaemon(SystemSettings.SERVER_INFO);
			SystemSettings.NIFTI_DAEMON.start();
			// On lance le daemon Dicom
			SystemSettings.DICOM_DAEMON = new DicomDaemon(SystemSettings.SERVER_INFO);
			SystemSettings.DICOM_DAEMON.start();
			SystemSettings.DICOM_NODE  = new DicomNode();
			SystemSettings.DICOM_NODE.start();
			daemonLaunched = true;
			mntmStartstop.setText("Stop");
		}
	}
	
	/**
	 * Coupe les daemon en demandant a l'utilisateur
	 * l'action a realiser si il reste des donnees dans les listes
	 */
	public void stopDaemons(){
		firstNameDB.stop();
		SystemSettings.stopDaemons();
		daemonLaunched = false;
		mntmStartstop.setText("Start");
	}
	
	/**
	 * Coupe les daemons sans rien demander a l'utilisateur
	 */
	public void forceStopDaemons(){
		firstNameDB.stop();
		if(SystemSettings.DICOM_NODE!=null){
			SystemSettings.DICOM_NODE.stop();
			SystemSettings.DICOM_NODE = null;
		}
		if(SystemSettings.DICOM_DAEMON!=null){
				SystemSettings.DICOM_DAEMON.setStop(true);
		}
		if(SystemSettings.NIFTI_DAEMON!=null){
			SystemSettings.NIFTI_DAEMON.setStop(true);
		}
		if(SystemSettings.DECRYPT_DAEMON!=null)
			SystemSettings.DECRYPT_DAEMON.setStop(true);
		if(SystemSettings.DAEMON_STATUS_THREAD!=null)
			SystemSettings.DAEMON_STATUS_THREAD.setStop(true);
		daemonLaunched = false;
	}
	
	
	public void setLock(boolean b) {
		isLock = b;
		getFileTreeDist().getTable().setEnabled(!b);
		getFileTreeLocal().getTable().setEnabled(!b);
		getFileTreeWork().getTable().setEnabled(!b);
		getBtnCreateLocal().setEnabled(!b);
		getBtnCreateWork().setEnabled(!b);
		getBtnDeleteLocal().setEnabled(!b);
		getBtnDeleteWork().setEnabled(!b);
		getBtndistToLocal().setEnabled(!b);
		getBtndistToWorkspace().setEnabled(!b);
		getBtnLocalpanel().setEnabled(!b);
		getBtnlocalTowork().setEnabled(!b);
		btnImport.setEnabled(!b);
		getBtnRefresh().setEnabled(!b);
		getBtnWorkpanel().setEnabled(!b);
		getBtnWorkTolocal().setEnabled(!b);
		getDistautresplitPane().setEnabled(!b);
		getDistworklocalPane().setEnabled(!b);
	}
	
	public void addStatisticPane(String projectname){
		if(statsPanel != null)
			ongletPane.remove(statsPanel);
		final ProjectStatistics stats = new ProjectStatistics(projectname);
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				statsPanel = new StatisticsPanel(stats);
				ongletPane.addTab("Statistics", null, statsPanel,
		                "Project statistics");
				ongletPane.setSelectedComponent(statsPanel);
			}
		});
		
	}
	
	public void addStatisticPane(int projectid){
		if(statsPanel != null)
			ongletPane.remove(statsPanel);
		final ProjectStatistics stats = new ProjectStatistics(projectid);
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				statsPanel = new StatisticsPanel(stats);
				ongletPane.addTab("Statistics", null, statsPanel,
		                "Project statistics");
				ongletPane.setSelectedComponent(statsPanel);
			}
		});
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
				MainWindow mw = new MainWindow(0);
				
				UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
				mw.createAndShowGUI();
			}
		});
	}

}
