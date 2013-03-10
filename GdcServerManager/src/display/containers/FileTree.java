package display.containers;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.JMenuItem;


/**
 * Class that display files list as a tree
 * @author Jérémy DEVERDUN & base algorithm from Ian Darwin
 *
 */
public class FileTree extends JPanel {
  /** Construct a FileTree */
	private JTree tree;
	private JPopupMenu popupmenu;
	private JMenuItem mntmEdit;
	private JMenuItem mntmSelectForProcess;
	private JMenuItem mntmClose;
	private File root=new File(".");
	private File selected;
	
	/**
	 * 
	 * @param dir
	 */
  public FileTree(File dir) {
    setLayout(new BorderLayout());

    // Make a tree list with all the nodes, and make it a JTree
    tree = new JTree(addNodes(null, dir,"",0));
    root=dir;
    // Add a listener
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
            .getPath().getLastPathComponent();
      }
    });
    popupmenu=new JPopupMenu();
    tree.add(popupmenu);
    

    mntmClose = new JMenuItem("Close");
    mntmClose.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
    		popupmenu.setVisible(false);
		}
	});
    popupmenu.add(mntmClose);
  
    MouseListener ml = new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            if(popupmenu.isVisible()) popupmenu.setVisible(false);
            if(selRow != 0) {
            	try{
	            	tree.setSelectionRow(selRow);
	            	String[] nodes = selPath.toString().substring(1, selPath.toString().length()-1).split(",");
	        		StringBuilder sb = new StringBuilder();
	        		sb.append(File.separatorChar).append(nodes[0]);
	        		for(int i = 1;i < nodes.length;i++) {
	        			sb.append(File.separatorChar).append(nodes[i].substring(1));
	        		}
	        		
	        		String path = sb.toString();
	        		path=root+path.substring(path.indexOf(File.separator, 2));
	        		File f=new File(path);
	        		selected=f;
	        		System.out.println(f.getName());
	            	if(SwingUtilities.isRightMouseButton(e)){
	            		mntmEdit.setVisible(true);
	            		mntmSelectForProcess.setVisible(true);
	            		if(f.isFile()) mntmSelectForProcess.setVisible(false);
	            		else if(f.isDirectory()) mntmEdit.setVisible(false);
	            		Point coord=new Point(e.getX(),e.getY());
	            		SwingUtilities.convertPointToScreen(coord, tree);
	            		popupmenu.setLocation(coord.x,coord.y);
	            		popupmenu.setVisible(true);
	            	}else{
		            	if(e.getClickCount() == 2) {
		            		if(f.isFile()){

		            		}else{
		            			changeTree(f);
		            		}
		                }else{
		                	BufferedImage imgtmp = null;
		            		try {
		            		    imgtmp = ImageIO.read(f);
		            		} catch (IOException ee) {
		            			System.out.println(ee.toString());
		            			System.out.println("ou est l'image");
		            		}
		                }
	            	}
            	}catch(Exception ex){}
            }
        }
    };
    tree.addMouseListener(ml);


    // Lastly, put the JTree into a JScrollPane.
    JScrollPane scrollpane = new JScrollPane();
    scrollpane.setViewportView(tree);
    add(BorderLayout.CENTER, scrollpane);
  }
  
  /**
   * 
   * @return
   */
  public File getRoot() {
	return root;
  }
  /**
   * 
   * @param root
   */
	public void setRoot(File root) {
		this.root = root;
	}
	
	/**
	 * Change root of the tree
	 * @param f
	 */
	public void changeTree(File f){
	  final File tmpf=f;
	  root=f;

	 Thread monThread = new Thread() {
         public void run() {
        	 tree.setModel(new DefaultTreeModel(addNodes(null,tmpf,"",0)));
			this.stop();
         }
	 };
	 monThread.start();
  }

  /** Add nodes from under "dir" into curTop. Highly recursive. */
  DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir,String ajout,int level) {
	 if(level>2 || dir.list()==null) return null;
    String curPath = dir.getName();//.getPath();
    DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(curPath);
    if (curTop != null && level<=2) { // should only be null at root
      curTop.add(curDir);
    }
    Vector ol = new Vector();
    String[] tmp = dir.list();
    for (int i = 0; i < tmp.length; i++)
      ol.addElement(tmp[i]);
    Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
    File f;
    Vector files = new Vector();
    // Make two passes, one for Dirs and one for Files. This is #1.
    for (int i = 0; i < ol.size(); i++) {
      String thisObject = (String) ol.elementAt(i);
      String newPath;
      if (curPath.equals("."))
        newPath = thisObject;
      else
    	  newPath = root.getAbsolutePath() +File.separator + thisObject;
    	  if(curTop!=null){
    		  newPath = root.getAbsolutePath() +File.separator+ajout+thisObject;
    	  }
      if ((f = new File(newPath)).isDirectory()){
        if(level<=2) addNodes(curDir, f,ajout+f.getName()+File.separator,level+1);
      }else
        files.addElement(thisObject);
    }
    // Pass two: for files.
    for (int fnum = 0; fnum < files.size(); fnum++)
      curDir.add(new DefaultMutableTreeNode(files.elementAt(fnum)));
    
    return curDir;
  }
  
  /**
   * 
   * @param n
   * @return
   */
  public String nodetoString(DefaultMutableTreeNode n){
	  TreePath selPath = tree.getPathForRow(tree.getRowCount()-1);
		String[] nodes = selPath.toString().substring(1, selPath.toString().length()-1).split(",");
		StringBuilder sb = new StringBuilder();
		sb.append(File.separatorChar).append(nodes[0]);
		for(int i = 1;i < nodes.length;i++) {
			sb.append(File.separatorChar).append(nodes[i].substring(1));
		}
	  return sb.toString();
  }
}