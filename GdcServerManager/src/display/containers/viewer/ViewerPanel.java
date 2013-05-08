package display.containers.viewer;



import ij.ImagePlus;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import daemon.tools.nifti.Coordinate_Viewer;
import daemon.tools.nifti.Nifti_Reader;
import daemon.tools.nifti.Slicer;
import display.containers.viewer.NiftiImagePanel.Plan;
import net.miginfocom.swing.MigLayout;
import javax.swing.JSplitPane;

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
		
		splitLeft.setLeftComponent(sagittalPanel);
		splitLeft.setRightComponent(axialPanel);
		splitLeft.setResizeWeight(0.5f);
		

		splitRight.setLeftComponent(coronalPanel);
		splitRight.setRightComponent(infoViewer);
		splitRight.setResizeWeight(0.5f);
		open(Paths.get("C:/Users/serge/Desktop/T1MPRAGE3DGADOC-C110.nii"));
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

	/**
	 * Ouvre un fichier nifti
	 * @param path
	 */
	public void open(Path path) {
		if(!path.toString().endsWith(".nii") && !path.toString().endsWith(".img") && !path.toString().endsWith(".hdr") && !path.toString().endsWith(".nii.gz"))
			return;
		if(niftiAxial!=null)
			close();
		niftiAxial = new Nifti_Reader(path.toFile());
		//niftiAxial.setSlice(Math.round(niftiAxial.getNSlices()/2));
		niftiAxial.setSlice(40);
		Slicer slicer = new Slicer(niftiAxial);
		ImagePlus niftiCoro = slicer.flip(false, true, "Top");
		ImagePlus niftiSag = slicer.flip(false, true, "Right");
		getCoronalPanel().setNiftiImage(niftiCoro);
		getAxialPanel().setNiftiImage(niftiAxial);
		getSagittalPanel().setNiftiImage(niftiSag);
		Coordinate_Viewer c = new Coordinate_Viewer(niftiSag);
		revalidate();
		//Nifti_Reader niftiSag = (Nifti_Reader) niftiAxial.clone();
	}

	
	
	
	/**
	 * Ferme l'image actuellement ouverte
	 */
	public void close(){
		niftiAxial = null;
	}
	public static void main(String[] args){
		ViewerPanel v = new ViewerPanel();
		v.open(Paths.get(" C:/Users/serge/Desktop/T1MPRAGE3DGADOC-C110.nii"));
		JFrame testf = new JFrame("test");
		testf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		testf.setSize(400,400);
		testf.getContentPane().add(v);
		testf.setVisible(true);
		
		
	}


}
