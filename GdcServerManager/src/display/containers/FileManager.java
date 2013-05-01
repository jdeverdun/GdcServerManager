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

import settings.SystemSettings;
import settings.UserProfile;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import java.io.*;
import java.nio.channels.FileChannel;
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

	
    /** Title of the application */
    public static final String APP_TITLE = "FileMan";
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
							try {
								deleteSelectedFiles();
							} catch (IOException e) {
								parentFrame.setLock(false);
								JOptionPane.showMessageDialog(parentFrame,
									    "Error during the deletion.",
									    "Copy error",
									    JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							}
							refresh();
							parentFrame.setLock(false);	
						}else{
							if(UserProfile.CURRENT_USER.getLevel()==3){
								parentFrame.setLock(true);
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
										e.printStackTrace();
									}
								}
								refresh();
								parentFrame.setLock(false);
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
    
        //}
        return tableScroll;
    }


    /**
     * Rentre dans un repertoire
     * @param row
     * @param column
     */
    protected void mouseDblClicked(int row, int column) {
		File file = ((FileTableModel)table.getModel()).getFile(row);
		if(file.isDirectory()){
			try {
				setCurrentDir(file.getCanonicalFile());
				showChildren(Paths.get(file.getCanonicalPath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			try {
                desktop.open(file);
            } catch(Throwable t) {
                showThrowable(t);
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
			e.printStackTrace();
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
				image = image.substring(0, image.length()-4);
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
			if(fi.isDirectory())
				FileUtils.deleteQuietly(fi);
			else
				fi.delete();
		}
	}
	/**
     * Deplace les fichiers / repertoire selectionnees vers le dossier dir
     * et decrypte
     * @param currentDir2
     * @throws IOException 
     */
	public void copySelectedFilesAndDecryptTo(File dir) throws IOException {
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
		while(!SystemSettings.DECRYPT_DAEMON.getFileToDecrypt().isEmpty() && continueAction){
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
	 */
	private void copyAndDecrypt(File fi, File dir) {
		if(fi.isDirectory()){
			File ndir = new File(dir.getAbsolutePath()+ "/" + fi.getName());
			ndir.mkdir();
			for(File cf:fi.listFiles()){
				if(!cf.getName().contains("..") && !cf.getName().equals(".")){
					copyAndDecrypt(cf, ndir);
				}
			}
		}else{
			SystemSettings.DECRYPT_DAEMON.addFileToDecrypt(fi.toPath(), dir.toPath());
		}
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
                    fileTableModel = new FileTableModel();
                    table.setModel(fileTableModel);
                }
                table.getSelectionModel().removeListSelectionListener(listSelectionListener);
                fileTableModel.setFiles(files);
                table.getSelectionModel().addListSelectionListener(listSelectionListener);
                if (!cellSizesSet) {
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
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
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
                    				if(p.getNom().equals(fi.getName())){
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
                    if(filesWithParent != null && filesWithParent.length>0)
                    	setTableData(filesWithParent);
                }
                return null;
            }

            @Override
            protected void process(List<File> chunks) {
                /*for (File child : chunks) {
                  //  node.add(new DefaultMutableTreeNode(child));
                }*/
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
               // tree.setEnabled(true);
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

}

/** A TableModel to hold File[]. */
class FileTableModel extends AbstractTableModel {

    private File[] files;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private String[] columns = {
        "Icon",
        "File",
        "Size",
        "Last Modified",
    };

    FileTableModel() {
        this(new File[0]);
    }

    FileTableModel(File[] files) {
        this.files = files;
    }

    public Object getValueAt(int row, int column) {
        File file = files[row];
        switch (column) {
            case 0:
                return fileSystemView.getSystemIcon(file);
            case 1:
                return fileSystemView.getSystemDisplayName(file);
            case 2:
            	long nbytes = file.length();
            	long kilobytes = (nbytes / 1024);
            	long megabytes = (kilobytes / 1024);
            	long gigabytes = (megabytes / 1024);
            	if(kilobytes>1 && megabytes<1)
            		return kilobytes + " Kb";
            	else if(megabytes>=1 && gigabytes<1)
            		return megabytes + " Mb";
            	else if(gigabytes>=1)
            		return gigabytes + " Gb";
                return nbytes + " b";
            case 3:
                return file.lastModified();
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

    
    public void setFiles(File[] files) {
        this.files = files;
        fireTableDataChanged();
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