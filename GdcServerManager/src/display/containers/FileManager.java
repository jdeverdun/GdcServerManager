package display.containers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileSystemView;

import javax.imageio.ImageIO;

import model.Project;
import model.ServerInfo;

import org.apache.commons.io.FileUtils;

import daemon.DecryptDaemon;
import daemon.NiftiDaemon;
import dao.MySQLProjectDAO;
import dao.ProjectDAO;
import dao.project.AcquisitionDateDAO;
import dao.project.DicomImageDAO;
import dao.project.MySQLAcquisitionDateDAO;
import dao.project.MySQLDicomImageDAO;
import dao.project.MySQLNiftiImageDAO;
import dao.project.MySQLPatientDAO;
import dao.project.MySQLProtocolDAO;
import dao.project.MySQLSerieDAO;
import dao.project.NiftiImageDAO;
import dao.project.PatientDAO;
import dao.project.ProtocolDAO;
import dao.project.SerieDAO;
import display.MainWindow;
import display.containers.viewer.ViewerPanel;
import es.vocali.util.AESCrypt;

import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Pattern;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.net.URL;

/**
A basic File Manager.  Requires 1.6+ for the Desktop & SwingWorker
classes, amongst other minor things.

Includes support classes FileTableModel & FileTreeCellRenderer.

@TODO Bugs
<li>Still throws occasional AIOOBEs and NPEs, so some update on
the EDT must have been missed.
<li>Fix keyboard focus issues - especially when functions like
rename/delete etc. are called that update nodes & file lists.
<li>Needs more testing in general.

@TODO Functionality
<li>Implement Read/Write/Execute checkboxes
<li>Implement Copy
<li>Extra prompt for directory delete (camickr suggestion)
<li>Add File/Directory fields to FileTableModel
<li>Double clicking a directory in the table, should update the tree
<li>Move progress bar?
<li>Add other file display modes (besides table) in CardLayout?
<li>Menus + other cruft?
<li>Implement history/back
<li>Allow multiple selection
<li>Add file search

@author Andrew Thompson
@version 2011-06-01
@see http://stackoverflow.com/questions/6182110
@license LGPL
*/
public class FileManager {

	
    private static boolean stopAction;
	/** Used to open/edit/print files. */
    private Desktop desktop;
    /** Provides nice icons and names for files. */
    private FileSystemView fileSystemView;

    /** currently selected File. */
    private File currentDir;

    /** Main GUI container */
    private DefaultTreeModel treeModel;
    private MainWindow parentFrame;
    
    /** Directory listing */
    private JTable table;
    private JProgressBar progressBar;
    /** Table model for File[]. */
    private FileTableModel fileTableModel;
    private ListSelectionListener listSelectionListener;
    private boolean cellSizesSet = false;
    private int rowIconPadding = 6;
	private boolean continueAction;
	
	/** Attributs basiques **/
	private int mode; //mode definit le fonctionnement du filemanager
	private JPopupMenu Pmenu;
	private JMenuItem twitem;
	private JMenuItem tlitem;
	private JMenuItem changeProjectitem;
	private JMenuItem renameProjectitem;
	
    public FileManager(MainWindow parent,Path defdir){
    	setCurrentDir(defdir.toFile());
    	parentFrame = parent;
    	continueAction = true;
    	setMode(0);
    }
    
    /**
     * Le mode definit le fonctionnement du filemanager
     * 
     * Mode 0 = explorateur simple (defaut)
     * Mode 1 = explorateur workspace sur le serveur distant (n'affiche que les workspace autorises)
     * Mode 2 = explorateur serveur partie sauvegarde (NRI-DICOM, NRI-ANALYSE)
     * @param parent
     * @param defdir
     * @param mode : mode de fonctionnement
     */
    public FileManager(MainWindow parent,Path defdir,int mode){
    	this(parent,defdir);
    	setMode(mode);
    }
    public FileManager(MainWindow parent){
    	setCurrentDir(new File(System.getProperty("user.home")));
    	parentFrame = parent;
    	continueAction = true;
    	setMode(0);
    }

    public Container getPane() {
        //if (gui==null) {

            fileSystemView = FileSystemView.getFileSystemView();
            desktop = Desktop.getDesktop();

            JPanel detailView = new JPanel(new BorderLayout(3,3));
            //fileTableModel = new FileTableModel();


            table = new JTable();
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setAutoCreateRowSorter(true);
            table.setShowVerticalLines(false);
            table.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                  if (e.getClickCount() >= 2) {
                  	Point p = e.getPoint();
                  	int row = table.convertRowIndexToModel(table.rowAtPoint(p));
                    int column = table.convertColumnIndexToModel(table.columnAtPoint(p));
                    if (row >= 0 && column >= 0) {
                      mouseDblClicked(row, column);
                    }
                  }
                }
              });
            table.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent arg0) {
					
				}
				
				@Override
				public void keyReleased(KeyEvent arg0) {
					if(KeyEvent.VK_DELETE == arg0.getKeyCode()){
						if(mode!=2){
							parentFrame.setLock(true);
							parentFrame.getProgressBarPanel().setVisible(true);
							Thread t = new Thread(new Runnable() {
								
								@Override
								public void run() {
									try {
										deleteSelectedFiles();
									} catch (IOException e) {
										JOptionPane.showMessageDialog(parentFrame,
											    "Error during the deletion.",
											    "Deletion error",
											    JOptionPane.ERROR_MESSAGE);
										WindowManager.mwLogger.log(Level.SEVERE, "Error during the deletion.",e);
									}finally{
										parentFrame.setLock(false);
										refresh();
										parentFrame.getProgressBarPanel().setVisible(false);
									}
								}
							});
							t.start();
							
						}else{
							if(UserProfile.CURRENT_USER.getLevel()==3){
								parentFrame.setLock(true);
								parentFrame.getProgressBarPanel().setVisible(true);
								Thread delThread = new Thread(new Runnable() {
									
									@Override
									public void run() {
										int[] rows = table.getSelectedRows();
										int[] columns = table.getSelectedColumns();
										for(int i = 0; i<rows.length;i++){
											if(!continueAction){
												continueAction = true;
												return;
											}
											int row = table.convertRowIndexToModel(rows[i]);
											try {
												deleteServerFile(row);
											} catch (Exception e) {
												WindowManager.mwLogger.log(Level.SEVERE, "Error during the deletion.",e);
											}
										}
										refresh();
										parentFrame.setLock(false);
										parentFrame.getProgressBarPanel().setVisible(false);
									}
								});
								delThread.start();
								
							}
						}	
					}
				}
				
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
			});
            table.getSelectionModel().addListSelectionListener(listSelectionListener);
            JScrollPane tableScroll = new JScrollPane(table);
            Dimension d = tableScroll.getPreferredSize();
            tableScroll.setPreferredSize(new Dimension((int)d.getWidth(), (int)d.getHeight()/2));
            detailView.add(tableScroll, BorderLayout.CENTER);

            // the File tree
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            treeModel = new DefaultTreeModel(root);
            table.getRowSorter().addRowSorterListener(
	            new RowSorterListener() {
	
	                @Override
	                public void sorterChanged(RowSorterEvent e) {
	                	((FileTableModel)table.getModel()).fireTableDataChanged();
	                }
	            });
            
            // show the file system roots.
            File[] roots = fileSystemView.getRoots();
            for (File fileSystemRoot : roots) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
                root.add( node );
                //showChildren(node);
                //
                File[] files = fileSystemView.getFiles(fileSystemRoot, true);
                for (File file : files) {
                    if (file.isDirectory()) {
                        node.add(new DefaultMutableTreeNode(file));
                    }
                }
                //
            }
            JScrollPane treeScroll = new JScrollPane();

            Dimension preferredSize = treeScroll.getPreferredSize();
            Dimension widePreferred = new Dimension(
                200,
                (int)preferredSize.getHeight());
            treeScroll.setPreferredSize( widePreferred );


            JPanel fileView = new JPanel(new BorderLayout(3,3));

            detailView.add(fileView, BorderLayout.SOUTH);

            JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                treeScroll,
                detailView);

            JPanel simpleOutput = new JPanel(new BorderLayout(3,3));
            progressBar = new JProgressBar();
            simpleOutput.add(progressBar, BorderLayout.EAST);
            progressBar.setVisible(false);
            showChildren(getCurrentDir().toPath());
            //table.setDragEnabled(true);
            table.setColumnSelectionAllowed(false);
    
            // Menu popup
            Pmenu = new JPopupMenu();
            changeProjectitem = new JMenuItem("Reassign");
            renameProjectitem = new JMenuItem("Rename");
    		twitem = new JMenuItem("To workspace");
    		tlitem = new JMenuItem("To local");
    		switch(mode){
    		case 0:
    			Pmenu.add(twitem);
    			twitem.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						parentFrame.getBtnlocalTowork().doClick();
					}
				});
    			break;
    		case 1:
    			Pmenu.add(tlitem);
    			tlitem.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						parentFrame.getBtnWorkTolocal().doClick();
					}
				});
				break;
    		case 2:
    			if(UserProfile.CURRENT_USER.getLevel()==3){
    				Pmenu.add(changeProjectitem);
    				Pmenu.add(renameProjectitem);
    			}
    			Pmenu.add(twitem);
    			twitem.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						parentFrame.getBtndistToWorkspace().doClick();
					}
				});
    			Pmenu.add(tlitem);
    			tlitem.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						parentFrame.getBtndistToLocal().doClick();
					}
				});
				break;
    		}
    		changeProjectitem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					table.setEnabled(false);
					File from = ((FileTableModel)table.getModel()).getFile(table.convertRowIndexToModel(table.getSelectedRows()[0]));
									
					ReassignProjectPanel reas = new ReassignProjectPanel(from.toPath()); // mode creation de liens
					Popup popup = PopupFactory.getSharedInstance().getPopup(WindowManager.MAINWINDOW, reas, (int)WindowManager.MAINWINDOW.getX()+200,(int)WindowManager.MAINWINDOW.getY()+150);
					reas.setPopupWindow(popup);
					popup.show();
					table.setEnabled(true);
				}
			});
    		renameProjectitem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					table.setEnabled(false);
					final File from = ((FileTableModel)table.getModel()).getFile(table.convertRowIndexToModel(table.getSelectedRows()[0]));
					JDialog.setDefaultLookAndFeelDecorated(true);		
					String s = (String)JOptionPane.showInputDialog(
					                    WindowManager.MAINWINDOW,
					                    "New project name ?",
					                    "Rename project",
					                    JOptionPane.PLAIN_MESSAGE,
					                    null,
					                    null,
					                    from.getName());

					//If a string was returned, say so.
					if ((s != null) && (s.length() > 0)) {
					    ProjectDAO pdao = new MySQLProjectDAO();
					    try {
							boolean succeed = pdao.renameProject(from.getName(), s);
							if(!succeed){
								SwingUtilities.invokeLater(new Runnable() {

									@Override
									public void run() {
										JDialog.setDefaultLookAndFeelDecorated(true);
										JOptionPane.showMessageDialog(WindowManager.MAINWINDOW,
												"Couldn't rename "+from.getName()+" (no project with this name)",
												"Renaming error",
												JOptionPane.ERROR_MESSAGE);
									}
								});
							}else{
								from.renameTo(new File(from.getParent()+File.separator+s));
								// on renomme le repertoire nifti ou dicom correspondant si il existe
								switch(from.getParentFile().getName()){
								case ServerInfo.NRI_ANALYSE_NAME:
									if(new File(from.getAbsolutePath().replaceAll(ServerInfo.NRI_ANALYSE_NAME, ServerInfo.NRI_DICOM_NAME)).exists())
										try {
											Files.move(Paths.get(from.getAbsolutePath().replaceAll(ServerInfo.NRI_ANALYSE_NAME, ServerInfo.NRI_DICOM_NAME)), Paths.get(from.getParent().replaceAll(ServerInfo.NRI_ANALYSE_NAME, ServerInfo.NRI_DICOM_NAME)+File.separator+s));
										} catch (IOException e) {
											e.printStackTrace();
											SwingUtilities.invokeLater(new Runnable() {

												@Override
												public void run() {
													JDialog.setDefaultLookAndFeelDecorated(true);
													JOptionPane.showMessageDialog(WindowManager.MAINWINDOW,
															"Couldn't rename "+from.getName()+" (error with file system)",
															"Renaming error",
															JOptionPane.ERROR_MESSAGE);
												}
											});
											WindowManager.mwLogger.log(Level.SEVERE,"Error during file project renaming ("+from.getName()+")",e);
										}//from.renameTo(new File(from.getParent().replaceAll(ServerInfo.NRI_ANALYSE_NAME, ServerInfo.NRI_DICOM_NAME)+File.separator+s));
									break;
								case ServerInfo.NRI_DICOM_NAME:
									if(new File(from.getAbsolutePath().replaceAll(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME)).exists())
										try {
											Files.move(Paths.get(from.getAbsolutePath().replaceAll(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME)), Paths.get(from.getParent().replaceAll(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME)+File.separator+s));
										} catch (IOException e) {
											SwingUtilities.invokeLater(new Runnable() {

												@Override
												public void run() {
													JDialog.setDefaultLookAndFeelDecorated(true);
													JOptionPane.showMessageDialog(WindowManager.MAINWINDOW,
															"Couldn't rename "+from.getName()+" (error with file system)",
															"Renaming error",
															JOptionPane.ERROR_MESSAGE);
												}
											});
											e.printStackTrace();
											WindowManager.mwLogger.log(Level.SEVERE,"Error during file project renaming ("+from.getName()+")",e);
										}//from.renameTo(new File(from.getParent().replaceAll(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME)+File.separator+s));
									break;
								}
								refresh();
							}
					    } catch (final SQLException e) {
					    	WindowManager.mwLogger.log(Level.SEVERE,"Error during SQL project renaming",e);
					    	SwingUtilities.invokeLater(new Runnable() {

					    		@Override
					    		public void run() {
					    			JDialog.setDefaultLookAndFeelDecorated(true);
					    			JOptionPane.showMessageDialog(WindowManager.MAINWINDOW,
					    					"Exception : "+e.toString(),
					    					"Openning error",
					    					JOptionPane.ERROR_MESSAGE);
					    		}
					    	});
					    }
					}
					table.setEnabled(true);
				}
			});
			table.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent me) {
					
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}


				public void mousePressed(MouseEvent e) {
				}


				public void mouseReleased(MouseEvent me) {
					if(me.isPopupTrigger() && table.getSelectedRowCount()>0){
						int row = table.convertRowIndexToModel(table.rowAtPoint(me.getPoint()));
						changeProjectitem.setVisible(isPatient(((FileTableModel)table.getModel()).getFile(row)));	
						renameProjectitem.setVisible(isProject(((FileTableModel)table.getModel()).getFile(row)));	
						Pmenu.show(me.getComponent(), me.getX(), me.getY());
					}
				}
			});
    		//
            
        //}
        return tableScroll;
    }


    public boolean isPatient(File fi) {
    	String[] parts = fi.getAbsolutePath().split(Pattern.quote(File.separator));
		int serverdirlen = (SystemSettings.SERVER_INFO.getServerDir().toString().split(Pattern.quote(File.separator))).length +1;// +1 pour NRI-ANALYSE et NRI-DICOM
		if(parts.length==(serverdirlen)) 
			return false;
		if(!fi.getName().contains("..")){
			int count = 0;
			for(int i = serverdirlen;i <parts.length;i++){
				if(!parts[i].isEmpty()){
					count++;
				}
			}
			return count==2;
		}
		return false;
	}
    
    public boolean isProject(File fi) {
    	String[] parts = fi.getAbsolutePath().split(Pattern.quote(File.separator));
		int serverdirlen = (SystemSettings.SERVER_INFO.getServerDir().toString().split(Pattern.quote(File.separator))).length +1;// +1 pour NRI-ANALYSE et NRI-DICOM
		if(parts.length==(serverdirlen)) 
			return false;
		if(!fi.getName().contains("..")){
			int count = 0;
			for(int i = serverdirlen;i <parts.length;i++){
				if(!parts[i].isEmpty()){
					count++;
				}
			}
			return count==1;
		}
		return false;
	}

	/**
     * Rentre dans un repertoire
     * @param row
     * @param column
     */
    protected void mouseDblClicked(int row, int column) {
		final File file = ((FileTableModel)table.getModel()).getFile(row);
		if(file.isDirectory()){
			try {
				setCurrentDir(file.getCanonicalFile());
				showChildren(Paths.get(file.getCanonicalPath()));
			} catch (IOException e) {
				WindowManager.mwLogger.log(Level.WARNING, "FileManager mouseDblClicked error.",e);
			}
		}else{
			// si on est dans la vue admin et qu'on clic sur un nifti encrypte
			if(file.getName().endsWith(".nii"+AESCrypt.ENCRYPTSUFFIX) && mode == 2){
				WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(true);
				Thread tr = new Thread(new Runnable() {
						
					@Override
					public void run() {
						try {
							copySelectedFilesAndDecryptTo(SystemSettings.SERVER_INFO.getTempDir().toFile());
							File copiedFile = new File(SystemSettings.SERVER_INFO.getTempDir()+File.separator+file.getName().substring(0, file.getName().length()-4));
							WindowManager.MAINWINDOW.getViewerPanel().open(copiedFile.toPath());
							WindowManager.MAINWINDOW.getOngletPane().setSelectedComponent(WindowManager.MAINWINDOW.getViewerPanel());
							copiedFile.deleteOnExit();
							WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
						} catch (final Exception e) {
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									JOptionPane.showMessageDialog(WindowManager.MAINWINDOW,
										    "Exception : "+e.toString(),
										    "Openning error",
										    JOptionPane.ERROR_MESSAGE);
								}
							});
							WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
							WindowManager.mwLogger.log(Level.WARNING, "FileManager Openning error.",e);
						}
					}
				});
				tr.start();
			}else{
				// si c'est un nifti non crypte
				if(file.getName().endsWith(".nii") || file.getName().endsWith(".img") || file.getName().endsWith(".nii.gz") || file.getName().endsWith(".hdr")){
					WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(true);
					WindowManager.MAINWINDOW.getOngletPane().setSelectedComponent(WindowManager.MAINWINDOW.getViewerPanel());
					Thread tr = new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								WindowManager.MAINWINDOW.getViewerPanel().open(file.toPath());
								WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
							} catch (final Exception e) {
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										JOptionPane.showMessageDialog(WindowManager.MAINWINDOW,
											    "Exception : "+e.toString(),
											    "Openning error",
											    JOptionPane.ERROR_MESSAGE);
									}
								});
								WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
								WindowManager.mwLogger.log(Level.WARNING, "FileManager Openning error.",e);
							}
						}
					});
					tr.start();
				}else{
					try {
		                desktop.open(file);
		            } catch(Throwable t) {
		                showThrowable(t);
		                WindowManager.mwLogger.log(Level.WARNING, "FileManager Openning error.",t);
		            }
				}
			}
		}
	}

    
    /**
     * Change le repertoire actuel du filemanager
     * @param dir
     */
    public void switchToDir(Path dir){
    	try {
			setCurrentDir(dir.toFile().getCanonicalFile());
			showChildren(dir);
		} catch (IOException e) {
			WindowManager.mwLogger.log(Level.WARNING, "FileManager switchToDir error.",e);
		}	
    }
    
    /**
     * Deplace les fichiers / repertoire selectionnees vers le dossier dir
     * @param currentDir2
     * @throws IOException 
     */
	public void copySelectedFilesTo(File dir) throws IOException {
		// Recupere les lignes selectionnees
		int[] indices = table.getSelectedRows();
		// On recupere les fichiers correspondants
		ArrayList<File> files = new ArrayList<File>();
		for(int i=0;i<indices.length;i++){
			int row = table.convertRowIndexToModel(indices[i]);
			File fi = ((FileTableModel)table.getModel()).getFile(row);
			if(!continueAction){
				continueAction = true;
				return;
			}
			if(!fi.getName().contains("..")){
				if(fi.isDirectory())
					FileUtils.copyDirectoryToDirectory(fi, dir);
				else
					FileUtils.copyFileToDirectory(fi, dir);
			}
		}
	}

	/**
     * Supprime les fichiers / repertoire selectionnees vers 
     * @throws IOException 
     */
	public void deleteSelectedFiles() throws IOException {
		// Recupere les lignes selectionnees
		int[] indices = table.getSelectedRows();
		// On recupere les fichiers correspondants
		ArrayList<File> files = new ArrayList<File>();
		for(int i=0;i<indices.length;i++){
			int row = table.convertRowIndexToModel(indices[i]);
			File fi = ((FileTableModel)table.getModel()).getFile(row);
			if(!continueAction){
				continueAction = true;
				return;
			}
			if(!fi.getName().contains("..")){
				if(fi.isDirectory())
					FileUtils.deleteQuietly(fi);
				else
					fi.delete();
			}
		}
	}
	
	/**
     * Supprime les fichiers / repertoire sur le serveur
	 * @throws Exception 
     */
	public void deleteServerFile(int row) throws Exception {	
		File fi = ((FileTableModel)table.getModel()).getFile(row);
		WindowManager.mwLogger.log(Level.INFO,"Deleting "+fi.getAbsolutePath());
		deleServerFile(fi);
	}

	/**
     * Supprime les fichiers / repertoire sur le serveur
	 * @throws Exception 
     */
	public void deleServerFile(File fi) throws Exception {
		String[] parts = fi.getAbsolutePath().split(Pattern.quote(File.separator));
		int serverdirlen = (SystemSettings.SERVER_INFO.getServerDir().toString().split(Pattern.quote(File.separator))).length +1;// +1 pour NRI-ANALYSE et NRI-DICOM
		if(parts.length==(serverdirlen)) 
			return;
		if(!fi.getName().contains("..")){
			int count = 0;
			for(int i = serverdirlen;i <parts.length;i++){
				if(!parts[i].isEmpty()){
					count++;
				}else{
					throw new Exception("Error with file path structure.");
				}
			}
			String project = null;
			String patient = null;
			String acqdate = null;
			String protocol = null;
			String serie = null;
			String image = null;
			switch(count){
			case 1:// on delete un projet complet
				project = parts[serverdirlen];
				ProjectDAO pdao = new MySQLProjectDAO();
				pdao.removeProject(project);
				break;
			case 2: // delete d'un patient
				project = parts[serverdirlen];
				patient = parts[serverdirlen+1];
				PatientDAO patdao = new MySQLPatientDAO();
				patdao.removePatient(project,patient);
				break;
			case 3://acqdate
				project = parts[serverdirlen];
				patient = parts[serverdirlen+1];
				acqdate = parts[serverdirlen+2];
				AcquisitionDateDAO adao = new MySQLAcquisitionDateDAO();
				adao.removeAcqdate(project,patient,acqdate);
				break;
			case 4://protocol
				project = parts[serverdirlen];
				patient = parts[serverdirlen+1];
				acqdate = parts[serverdirlen+2];
				protocol = parts[serverdirlen+3];
				ProtocolDAO prdao = new MySQLProtocolDAO();
				prdao.removeProtocol(project,patient,acqdate,protocol);
				break;
			case 5://serie
				project = parts[serverdirlen];
				patient = parts[serverdirlen+1];
				acqdate = parts[serverdirlen+2];
				protocol = parts[serverdirlen+3];
				serie = parts[serverdirlen+4];
				SerieDAO sdao = new MySQLSerieDAO();
				sdao.removeSerie(project,patient,acqdate,protocol,serie);
				break;
			case 6://Image
				project = parts[serverdirlen];
				patient = parts[serverdirlen+1];
				acqdate = parts[serverdirlen+2];
				protocol = parts[serverdirlen+3];
				serie = parts[serverdirlen+4];
				image = parts[serverdirlen+5];
				image = image.substring(0, image.length()-AESCrypt.ENCRYPTSUFFIX.length());//-4
				switch(parts[serverdirlen-1]){//NRI-ANALYZE ou DICOM
				case ServerInfo.NRI_DICOM_NAME:
					DicomImageDAO ddao = new MySQLDicomImageDAO();
					ddao.removeDicom(project,patient,acqdate,protocol,serie,image);
					break;
				case ServerInfo.NRI_ANALYSE_NAME:
					NiftiImageDAO ndao = new MySQLNiftiImageDAO();
					ndao.removeNifti(project,patient,acqdate,protocol,serie,image);
					break;
				}
				
				break;
			}
			if(fi.isDirectory()){
				FileUtils.deleteQuietly(fi);
				// on supprime l'equivalent dans l'autre repertoire (DICOM ou NIFTI)
				switch(parts[serverdirlen-1]){//NRI-ANALYZE ou DICOM
				case ServerInfo.NRI_DICOM_NAME:
					parts[serverdirlen-1] = ServerInfo.NRI_ANALYSE_NAME;
					break;
				case ServerInfo.NRI_ANALYSE_NAME:
					parts[serverdirlen-1] = ServerInfo.NRI_DICOM_NAME;
					break;
				}
				String opath = "";
				for(String p:parts)
					opath += p+File.separator;
				// on enleve le dernier separator 
				opath = opath.substring(0,opath.length()-1);
				File temp = new File(opath);
				if(temp.exists())
					FileUtils.deleteQuietly(new File(opath));
			}else{
				fi.delete();
				if(parts[serverdirlen-1].equals(ServerInfo.NRI_ANALYSE_NAME)){
					// si c'est un nifti on supprime aussi les fichiers associes
					try{
						// on verifie si il n'y a pas des fichiers associe a supprimer (fichiers texte genre bval)
						String path = fi.getParent();
						String rootname = fi.getName();
						if(rootname.endsWith(AESCrypt.ENCRYPTSUFFIX)){
							rootname = fi.getName().substring(0,fi.getName().lastIndexOf("."));//sans le .enc
							rootname = rootname.substring(0,rootname.lastIndexOf("."));//sans le .nii
						}else{
							rootname = fi.getName().substring(0,fi.getName().lastIndexOf(".")-1);
						}
						// on essai de supprimer les fichiers si ils existent
						for(String suf:NiftiDaemon.suffixeToRemoveWithNifti){
							if(new File(path+File.separator+rootname+suf).exists())
								new File(path+File.separator+rootname+suf).delete();
							else
								if(new File(path+File.separator+rootname+suf+AESCrypt.ENCRYPTSUFFIX).exists())
									new File(path+File.separator+rootname+suf+AESCrypt.ENCRYPTSUFFIX).delete();
						}
					}catch(Exception e){}
				}
			}
		}
		// on vide le cache pour eviter des erreurs issue de decalage db // cache
		SystemSettings.SERVER_INFO.getDbCache().clear();
	}
	/**
     * Deplace les fichiers / repertoire selectionnees vers le dossier dir
     * et decrypte
     * @param currentDir2
     * @throws IOException 
     */
	public void copySelectedFilesAndDecryptTo(File dir) throws IOException {
		if(!SystemSettings.DECRYPT_DAEMON.isAlive()){
			SystemSettings.DECRYPT_DAEMON = new DecryptDaemon();
			SystemSettings.DECRYPT_DAEMON.start();
		}
		// Recupere les lignes selectionnees
		int[] indices = table.getSelectedRows();
		// On recupere les fichiers correspondants
		ArrayList<File> files = new ArrayList<File>();
		for(int i=0;i<indices.length;i++){
			int row = table.convertRowIndexToModel(indices[i]);
			File fi = ((FileTableModel)table.getModel()).getFile(row);
			if(!continueAction){
				continueAction = true;
				return;
			}
			if(!fi.getName().contains("..")){
				copyAndDecrypt(fi, dir);
			}
		}
		while(!SystemSettings.DECRYPT_DAEMON.isWaiting() && continueAction){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		continueAction = true;
		SystemSettings.DECRYPT_DAEMON.cleanList();
	}

	/**
	 * Recursive copie & decrypt
	 * @param fi
	 * @param dir
	 * @param level le niveau de recursivite dans lequel on se trouve (0)
	 */
	public static void copyAndDecrypt(File fi, File dir,int level) {
		if(stopAction){
			if(level == 0)
				stopAction = false;
			return;
		}
		if(fi.isDirectory()){
			File ndir = new File(dir.getAbsolutePath()+ File.separator + fi.getName());
			ndir.mkdirs();
			for(File cf:fi.listFiles()){
				if(!cf.getName().contains("..") && !cf.getName().equals(".")){
					copyAndDecrypt(cf, ndir,level+1);
				}
				if(stopAction){
					if(level == 0)
						stopAction = false;
					return;
				}
			}
		}else{
			SystemSettings.DECRYPT_DAEMON.addFileToDecrypt(fi.toPath(), dir.toPath());
		}
	}
	
	/**
	 * On definit le niveau 0 de recursivite par defaut
	 * @param fi
	 * @param dir
	 */
	public static void copyAndDecrypt(File fi, File dir){
		copyAndDecrypt(fi, dir, 0);
	}
	
	/**
	 * Stoppe l'action que realise ce filetree (copie de fichier // decryptage etc)
	 */
	public void terminateAction() {
		continueAction = false;
	}
	public void terminateCopyAndDecrypt(){
		SystemSettings.DECRYPT_DAEMON.cleanList();
	}
	private void showThrowable(Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(
            parentFrame,
            t.toString(),
            t.getMessage(),
            JOptionPane.ERROR_MESSAGE
            );
        parentFrame.repaint();
    }

    /** Update the table on the EDT */
    private void setTableData(final File[] files) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (fileTableModel==null) {
                    fileTableModel = new FileTableModel(table);
                    table.setModel(fileTableModel);
                }
                table.getSelectionModel().removeListSelectionListener(listSelectionListener);
                fileTableModel.setFiles(files);
                table.getSelectionModel().addListSelectionListener(listSelectionListener);
                if (!cellSizesSet && files!=null && files.length>0) {
                    Icon icon = fileSystemView.getSystemIcon(files[0]);

                    // size adjustment to better account for icons
                    table.setRowHeight( icon.getIconHeight()+rowIconPadding );

                    setColumnWidth(0,-1);
                  //  setColumnWidth(3,70);
                  //  table.getColumnModel().getColumn(3).setMaxWidth(60);
 

                    cellSizesSet = true;
                }
            }
        });
    }

    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width<0) {
            // use the preferred width of the header..
            JLabel label = new JLabel( (String)tableColumn.getHeaderValue() );
            Dimension preferred = label.getPreferredSize();
            // altered 10->14 as per camickr comment.
            width = (int)preferred.getWidth()+14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    /** Add the files that are contained within the directory of this node.
    Thanks to Hovercraft Full Of Eels. */
    private void showChildren(final Path node) {
        //tree.setEnabled(false);

        SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
            	table.setEnabled(false);  
                File file = (File) node.toFile();
                setCurrentDir(file);
                if (file.isDirectory()) {
                    File[] files = fileSystemView.getFiles(file, !UserProfile.SHOW_HIDDEN_FILES); //!!
                    File[] filesTemp = new File[files.length+1];
                    File[] filesWithParent;
                    filesTemp[0] = new File(file.getAbsolutePath() + "/..");
                    for(int i = 1;i<filesTemp.length;i++){
                    	filesTemp[i] = files[i-1];
                    }
                    switch(getMode()){
                    case 1:
                    	List<File> list = new ArrayList<File>();
                    	if(file.toPath().equals(SystemSettings.SERVER_INFO.getServerDir())){
                    		for(File fi:filesTemp){
                    			for(Project p:UserProfile.CURRENT_USER.getProjects()){
                    				if((ServerInfo.WORKSPACE_PREFIXE+p.getNom()).equals(fi.getName())){
                    					list.add(fi);
                    				}
                    			}
                    		}
                    		filesWithParent = list.toArray(new File[list.size()]);
                    	}else{
                    		filesWithParent = filesTemp;
                    	}
                    	
                    	break;
                    case 2:
                    	List<File> list2 = new ArrayList<File>();
                    	if(file.toPath().equals(SystemSettings.SERVER_INFO.getServerDir())){
                    		for(File fi:filesTemp){
                				if(fi.getName().equals(ServerInfo.NRI_DICOM_NAME) || fi.getName().equals(ServerInfo.NRI_ANALYSE_NAME)){
                					list2.add(fi);
                				}
                    		}
                    		filesWithParent = list2.toArray(new File[list2.size()]);
                    	}else{
                    		filesWithParent = filesTemp;
                    	}
                    	break;
                	default:
                		filesWithParent = filesTemp;
                    }
                    if(filesWithParent != null){
                    	setTableData(filesWithParent);                   	
                    }
                }
                return null;
            }
        };
        worker.execute();
    }

    public File getCurrentDir() {
		return currentDir;
	}


	public void setCurrentDir(File currentDir) {
		this.currentDir = currentDir;
	}


	public static boolean copyFile(File from, File to) throws IOException {

        boolean created = to.createNewFile();

        if (created) {
            FileChannel fromChannel = null;
            FileChannel toChannel = null;
            try {
                fromChannel = new FileInputStream(from).getChannel();
                toChannel = new FileOutputStream(to).getChannel();

                toChannel.transferFrom(fromChannel, 0, fromChannel.size());

                // set the flags of the to the same as the from
                to.setReadable(from.canRead());
                to.setWritable(from.canWrite());
                to.setExecutable(from.canExecute());
            } finally {
                if (fromChannel != null) {
                    fromChannel.close();
                }
                if (toChannel != null) {
                    toChannel.close();
                }
                return false;
            }
        }
        return created;
    }



	public void refresh() {
		table.setEnabled(false);
		showChildren(currentDir.toPath());
		table.setEnabled(true);
	}
	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}
	public void setTreeModel(DefaultTreeModel treeModel) {
		this.treeModel = treeModel;
	}
	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public JTable getTable() {
		return table;
	}
	public void setTable(JTable table) {
		this.table = table;
	}
	public FileTableModel getFileTableModel() {
		return fileTableModel;
	}
	public void setFileTableModel(FileTableModel fileTableModel) {
		this.fileTableModel = fileTableModel;
	}

	/**
	 * Coupe une action realise statiquement (copyAndDecrypt)
	 */
	public static void stopAction() {
		stopAction = true;
	}

}

/** A TableModel to hold File[]. */
class FileTableModel extends AbstractTableModel {

	private JTable parent;
    private File[] files;
    private Object[] size;// on stock en cache les infos pour eviter les overhead
    private Object[] lastMod;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private String[] columns = {
        "Icon",
        "File",
        "Size",
        "Last Modified",
    };

    FileTableModel(JTable parent) {
        this(new File[0]);
        this.parent = parent;
    }

    FileTableModel(File[] files) {
        this.files = files;
    }

    // Attention beaucoup d'overhead
    public Object getValueAt(int row, int column) {
    	if(row>=files.length)
    		return null;
        File file = files[row];
        switch (column) {
            case 0:
                return fileSystemView.getSystemIcon(file);
            case 1:
                return fileSystemView.getSystemDisplayName(file);
            case 2:
            	return size[row];
            	/*long nbytes = file.length();
            	long kilobytes = (nbytes / 1024);
            	long megabytes = (kilobytes / 1024);
            	long gigabytes = (megabytes / 1024);
            	if(kilobytes>1 && megabytes<1)
            		return kilobytes + " Kb";
            	else if(megabytes>=1 && gigabytes<1)
            		return megabytes + " Mb";
            	else if(gigabytes>=1)
            		return gigabytes + " Gb";
                return nbytes + " b";*/
            case 3:
            	return lastMod[row];
                //return file.lastModified();
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return ImageIcon.class;
            case 2:
                return Long.class;
            case 3:
                return Date.class;
        }
        return String.class;
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return files.length;
    }

    public File getFile(int row) {
        return files[row];
    }

    
    public void setFiles(final File[] filesu) {
    	WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(true);
    	Thread tr = new Thread(new Runnable() {
			
			@Override
			public void run() {
				files = filesu;
		        updateSize();
		        updateLastMod();
		        WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
		        SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						fireTableDataChanged();
					}
				});
		        parent.setEnabled(true);  
			}
		});
        tr.start();
        
    }

    /**
     * Met a jours le cache pour les date de modification des fichiers
     * (empeche l'overhead)
     */
	private void updateLastMod() {
		lastMod = new Object[files.length];
		for(int i = 0; i<files.length;i++){
			lastMod[i] = files[i].lastModified();
		}
	}

	/**
	 * Met a jours les informations sur les tailles de fichiers
	 */
	private void updateSize() {
		size = new Object[files.length];
		for(int i = 0; i<files.length;i++){
			long nbytes = files[i].length();
	    	long kilobytes = (nbytes / 1024);
	    	long megabytes = (kilobytes / 1024);
	    	long gigabytes = (megabytes / 1024);
	    	if(kilobytes>1 && megabytes<1)
	    		size[i] = kilobytes + " Kb";
	    	else if(megabytes>=1 && gigabytes<1)
	    		size[i] = megabytes + " Mb";
	    	else if(gigabytes>=1)
	    		size[i] = gigabytes + " Gb";
	    	else
	    		size[i] = nbytes + " b";
		}
	}
}

/** A TreeCellRenderer for a File. */
class FileTreeCellRenderer extends DefaultTreeCellRenderer {

    private FileSystemView fileSystemView;

    private JLabel label;

    FileTreeCellRenderer() {
        label = new JLabel();
        label.setOpaque(true);
        fileSystemView = FileSystemView.getFileSystemView();
    }

    @Override
    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        File file = (File)node.getUserObject();
        label.setIcon(fileSystemView.getSystemIcon(file));
        label.setText(fileSystemView.getSystemDisplayName(file));
        label.setToolTipText(file.getPath());

        if (selected) {
            label.setBackground(backgroundSelectionColor);
            label.setForeground(textSelectionColor);
        } else {
            label.setBackground(backgroundNonSelectionColor);
            label.setForeground(textNonSelectionColor);
        }

        return label;
    }
}