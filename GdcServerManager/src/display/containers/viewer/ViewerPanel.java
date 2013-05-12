package display.containers.viewer;



import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.Rotator;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

import daemon.tools.nifti.CoordinateMapper;
import daemon.tools.nifti.Coordinate_Viewer;
import daemon.tools.nifti.Nifti_Reader;
import daemon.tools.nifti.Slicer;
import display.MainWindow;
import display.containers.UserCreationPanel;
import display.containers.WarningPanel;
import display.containers.viewer.NiftiImagePanel.Plan;
import net.miginfocom.swing.MigLayout;
import javax.swing.JSplitPane;

import settings.WindowManager;

public class ViewerPanel extends JPanel{

	// Attributs
	private Nifti_Reader niftiAxial;
	private int[] coord; // coordonnee actuelle du pointeur (x,y,z)
	private NiftiImagePanel axialPanel;//XY
	private NiftiImagePanel coronalPanel;//XZ
	private NiftiImagePanel sagittalPanel;//YZ
	private InformationViewer infoViewer;
	private JSplitPane splitRight;
	private JSplitPane splitLeft;
	private JSplitPane splitLeftRight;
	
	public ViewerPanel(){
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		coord = new int[]{-1,-1,-1};
		niftiAxial = null;
		axialPanel = new NiftiImagePanel(this,Plan.AXIAL);
		coronalPanel = new NiftiImagePanel(this,Plan.CORONAL);
		sagittalPanel = new NiftiImagePanel(this,Plan.SAGITTAL);
		
		
		splitLeftRight = new JSplitPane();
		infoViewer = new InformationViewer();
		
		add(splitLeftRight, "cell 0 0,grow");
		
		splitLeft = new JSplitPane();
		splitLeft.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitLeftRight.setLeftComponent(splitLeft);
		splitLeftRight.setResizeWeight(0.5f);
		splitRight = new JSplitPane();
		splitRight.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitLeftRight.setRightComponent(splitRight);
		
		splitLeft.setLeftComponent(coronalPanel);
		splitLeft.setRightComponent(axialPanel);
		splitLeft.setResizeWeight(0.5f);
		
		
		splitRight.setLeftComponent(sagittalPanel);
		splitRight.setRightComponent(infoViewer);
		splitRight.setResizeWeight(0.5f);
		
		this.setDropTarget(new DropTarget() {
	        public synchronized void drop(DropTargetDropEvent evt) {
	            try{
	                evt.acceptDrop(DnDConstants.ACTION_COPY);
	                WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(true);
	                final List<File> listFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	                Thread openThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							boolean ok = false;
							for(File fi:listFiles){
			                	ok = open(Paths.get(fi.getAbsolutePath()));
								if(ok)
									break;
			                }
							if(!ok){
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										WarningPanel wmess = new WarningPanel("Not a nifti");
										Popup popup = PopupFactory.getSharedInstance().getPopup(ViewerPanel.this, wmess, (int)getWidth()/2-20,(int)getHeight()/2-20);
										wmess.setPopupWindow(popup);
										popup.show();
									}
								});
								
							}
								
							WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
						}
					});
	                openThread.start();
	            } catch (Exception ex) {
	                System.out.println(ex.toString());
	                WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
	            }
	        }
	    });
		
		
		open(Paths.get("C:/Users/serge/Desktop/filetesto.nii"));//T1MPRAGE3DGADOC-C110.nii"));
	}
	
	public NiftiImagePanel getAxialPanel() {
		return axialPanel;
	}

	public void setAxialPanel(NiftiImagePanel axialPanel) {
		this.axialPanel = axialPanel;
	}

	public NiftiImagePanel getCoronalPanel() {
		return coronalPanel;
	}

	public void setCoronalPanel(NiftiImagePanel coronalPanel) {
		this.coronalPanel = coronalPanel;
	}

	public NiftiImagePanel getSagittalPanel() {
		return sagittalPanel;
	}

	public void setSagittalPanel(NiftiImagePanel sagittalPanel) {
		this.sagittalPanel = sagittalPanel;
	}

	public int[] getCoord() {
		return coord;
	}

	public void setCoord(int[] coord) {
		this.coord = coord;
	}

	/**
	 * Ouvre un fichier nifti
	 * @param path
	 */
	public boolean open(Path path) {
		if(niftiAxial!=null)
			reset();
		getCoronalPanel().reset();
		getAxialPanel().reset();
		getSagittalPanel().reset();
		if(!path.toString().endsWith(".nii") && !path.toString().endsWith(".img") && !path.toString().endsWith(".hdr") && !path.toString().endsWith(".nii.gz"))
			return false;
		niftiAxial = new Nifti_Reader(path.toFile());
		niftiAxial = checkAndRotate(niftiAxial); // on verifie  que le "point zero" est en bas a gauche
		//niftiAxial.setSlice(Math.round(niftiAxial.getNSlices()/2));
		//niftiAxial.setSlice(40);
		Slicer slicer = new Slicer(niftiAxial);
		ImagePlus niftiCoro = slicer.flip(false, true, "Top");
		ImagePlus niftiSag = slicer.flip(false, true, "Left");
		
		getCoronalPanel().setNiftiImage(niftiCoro);
		getAxialPanel().setNiftiImage(niftiAxial);
		getSagittalPanel().setNiftiImage(niftiSag);
		
		// on met a jours les coord actuelles
		coord[0] = getSagittalPanel().getSlice();
		coord[1] = getCoronalPanel().getSlice();
		coord[2] = getAxialPanel().getSlice();
		
		
		//Coordinate_Viewer c = new Coordinate_Viewer(niftiAxial);
		//niftiAxial.show();
		revalidate();
		return true;
	}

	
	
	/**
	 * Verifie  si l'image est bien positionne (c a d point de coordonnee minimale 
	 * en X Y en bas a gauche de l'image). Si ce n'est pas le cas on fait les flip en consequence
	 * @param im
	 * @return
	 */
	private Nifti_Reader checkAndRotate(Nifti_Reader im) {
		Nifti_Reader nr = (Nifti_Reader) im.clone();
		Object prop = im.getProperty("coors");
		if (prop == null) return null;

		CoordinateMapper[] mapper = (CoordinateMapper[] ) prop;
		// on test les 3 position mauvaises (en haut a gauche / droite, et en bas a droite)
		if(mapper[mapper.length-1].getX(1, 1, 1)<0 && mapper[mapper.length-1].getY(1, 1, 1)<0){
			ImageStack is = im.getImageStack();
			for(int i = 1 ; i <= im.getNSlices();i++){
				is.getProcessor(i).flipVertical();
			}
			nr.setStack(is);
		}else{
			if(mapper[mapper.length-1].getX(im.getProcessor().getWidth()-1, 1, 1)<0 && mapper[mapper.length-1].getY(im.getProcessor().getWidth()-1, 1, 1)<0){
				ImageStack is = im.getImageStack();
				for(int i = 1 ; i <= im.getNSlices();i++){
					is.getProcessor(i).flipVertical();
					is.getProcessor(i).flipHorizontal();
				}
				nr.setStack(is);
			}else{
				if(mapper[mapper.length-1].getX(im.getProcessor().getWidth()-1, im.getProcessor().getHeight()-1, 1)<0 && mapper[mapper.length-1].getY(im.getProcessor().getWidth()-1, im.getProcessor().getHeight()-1, 1)<0){
					ImageStack is = im.getImageStack();
					for(int i = 1 ; i <= im.getNSlices();i++){
						is.getProcessor(i).flipHorizontal();
					}
					nr.setStack(is);
				}
			}
		}
		return nr;
	}

	/**
	 * Ferme l'image actuellement ouverte
	 */
	public void reset(){
		niftiAxial = null;
		coord = new int[]{-1,-1,-1};
	}
	public static void main(String[] args){
		ViewerPanel v = new ViewerPanel();
		v.open(Paths.get("C:/Users/serge/Desktop/T1MPRAGE3DGADOC-C110.nii"));
		JFrame testf = new JFrame("test");
		testf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		testf.setSize(400,400);
		testf.getContentPane().add(v);
		testf.setVisible(true);
		
		
	}

	/**
	 * Met a jours le crosshair dans toutes les figures
	 */
	private void updateCrosshair() {
		getCoronalPanel().updateCrosshair(coord);
		getSagittalPanel().updateCrosshair(coord);
		getAxialPanel().updateCrosshair(coord);
	}
	
	/**
	 * Changement dans la vue axiale
	 * @param p
	 */
	public void setXY(Point p) {
		getCoronalPanel().setSlice((int)Math.round(p.getY()));
		getSagittalPanel().setSlice((int)Math.round(p.getX()));
		coord[0] = getSagittalPanel().getSlice();
		coord[1] = getCoronalPanel().getSlice();
		coord[2] = getAxialPanel().getSlice();
		updateCrosshair();
	}


	/**
	 * Changement dans la vue sagittale
	 * @param p
	 */
	public void setYZ(Point p) {
		getAxialPanel().setSlice((int)Math.round(p.getY()));
		getCoronalPanel().setSlice((int)Math.round(p.getX()));
		coord[0] = getSagittalPanel().getSlice();
		coord[1] = getCoronalPanel().getSlice();
		coord[2] = getAxialPanel().getSlice();
		updateCrosshair();
	}

	/**
	 * Changement dans la vue coronole
	 * @param p
	 */
	public void setXZ(Point p) {
		getAxialPanel().setSlice((int)Math.round(p.getY()));
		getSagittalPanel().setSlice((int)Math.round(p.getX()));
		coord[0] = getSagittalPanel().getSlice();
		coord[1] = getCoronalPanel().getSlice();
		coord[2] = getAxialPanel().getSlice();
		updateCrosshair();
	}

	public void setKeepRatio(boolean b) {
		getAxialPanel().setKeepRatio(b);
		getCoronalPanel().setKeepRatio(b);
		getSagittalPanel().setKeepRatio(b);
		updateCrosshair();
	}

	public boolean isCrosshairVisible() {
		return getAxialPanel().isShowCrosshair() && getCoronalPanel().isShowCrosshair() && getSagittalPanel().isShowCrosshair();
	}

	public void setCrosshairVisible(boolean b) {
		getAxialPanel().setShowCrosshair(b);
		getCoronalPanel().setShowCrosshair(b);
		getSagittalPanel().setShowCrosshair(b);
		updateCrosshair();
	}


}
