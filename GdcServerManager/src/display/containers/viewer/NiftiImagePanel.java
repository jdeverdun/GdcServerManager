package display.containers.viewer;


import ij.ImagePlus;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import daemon.tools.nifti.Nifti_Reader;

import java.io.*;
 
 
/**
 * Display image (ImagePlus) in a panel
 * @author Jérémy DEVERDUN
 *
 */
public class NiftiImagePanel extends JPanel implements MouseListener, MouseMotionListener {
	public static enum Plan{AXIAL, SAGITTAL, CORONAL}; // les differents plan possible
	public static final int AXIAL = 1;
	
	private ImagePlus niftiImage = null;
	private ViewerPanel viewer = null;
	private boolean isMousePressed = false;
	private int slice = 1;
	private Plan orientation; // vue associe a ce panel (axial sagital etc)
	private Dimension imageDim; // dimension de l'ImagePlus (dim 2D)

	
    public NiftiImagePanel(ViewerPanel v, ImagePlus nr, Plan plan) {
    	this.setOrientation(plan);
        this.setNiftiImage(nr); 
        setViewer(v);
        init();
        
    }
    
    /**
     * Plan par defaut : axial
     * @param v
     * @param nr
     */
    public NiftiImagePanel(ViewerPanel v, ImagePlus nr) {
    	this.setOrientation(Plan.AXIAL);
        this.setNiftiImage(nr); 
        setViewer(v);
        init();
    }
    
    public NiftiImagePanel(ViewerPanel v,Plan plan) {
    	this.setOrientation(plan);
    	setViewer(v);
    	init();
    }
    
    public NiftiImagePanel(ViewerPanel v) {
    	this.setOrientation(Plan.AXIAL);
    	setViewer(v);
    	init();

    }
   
    public void init(){
    	setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
       	isMousePressed = false;
       	addMouseMotionListener(this);
       	addMouseListener(this);
    }
    public ImagePlus getNiftiImage() {
		return niftiImage;
	}

	public void setNiftiImage(ImagePlus niftiImage) {
		this.niftiImage = niftiImage;
		setSlice(Math.round(this.niftiImage.getNSlices()/2));
		imageDim = new Dimension(this.niftiImage.getWidth(), this.niftiImage.getHeight());
	}

	public int getSlice() {
		return slice;
	}

	public void setSlice(int slice) {
		this.slice = slice;
		if(niftiImage!=null){
			niftiImage.setSlice(this.slice);
			repaint();
		}
	}

	public ViewerPanel getViewer() {
		return viewer;
	}

	public void setViewer(ViewerPanel viewer) {
		this.viewer = viewer;
	}

	public Plan getOrientation() {
		return orientation;
	}

	public void setOrientation(Plan orientation) {
		this.orientation = orientation;
	}

	/**
     * 
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g); //paint background
        if (niftiImage != null) { //there is a picture: draw it
            int height = this.getSize().height;
            int width = this.getSize().width;      
            g.drawImage(niftiImage.getBufferedImage(),0,0, width, height, this);
        }
    }

	public void mouseDragged(MouseEvent e) {
		System.out.println(isMousePressed);
		if(isMousePressed){
			Point p = panelXYtoImageXY(e.getPoint());
			/*switch(this.orientation){
			case AXIAL:
				getViewer().setXY(p);
				break;
			case SAGITTAL:
				getViewer().setYZ(p);
				break;
			case CORONAL:
				getViewer().setXZ(p);
				break;
			}*/
			
		}
	}

	public void mouseMoved(MouseEvent e) {
		
	}



	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			isMousePressed = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1)
			isMousePressed = false;
	}
	
	/**
	 * convertie une coordonnee dans le referentiel panel 
	 * au referentiel ImagePlus
	 * @param point
	 * @return
	 */
	private Point panelXYtoImageXY(Point point) {
		Point impt = null;
		int pwidth = this.getWidth();
		int pheight = this.getHeight();
		double ws = imageDim.getWidth() / (float)pwidth ;
		double hs = imageDim.getHeight() / (float)pheight;
		impt = new Point((int)Math.round(ws*point.getX()),(int)Math.round(hs*point.getY()));
		if(impt.getX()>imageDim.getWidth())
			impt.x = (int)imageDim.getWidth();
		if(impt.getX()<0)
			impt.x = 0;
		if(impt.getY()>imageDim.getHeight())
			impt.y = (int)imageDim.getHeight();
		if(impt.getY()<0)
			impt.y = 0;
		System.out.println(impt.getX()+"-"+impt.getY());
		return impt;
	}
}