package display.containers.viewer;


import ij.ImagePlus;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

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
	private BufferedImage image = null; // on stock l'image courante
	private ViewerPanel viewer = null;
	private boolean isMousePressed = false;
	private int slice = 1;
	private Plan orientation; // vue associe a ce panel (axial sagital etc)
	private Dimension imageDim; // dimension de l'ImagePlus (dim 2D)
	private Point currentLocation; // dernier point clique
	private boolean showCrosshair = true; // si on montre le crosshair ou pas
	private boolean keepRatio = false; // si on contraint l'affichage pourver le ratio original
	
    /**
     * @wbp.parser.constructor
     */
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
    	setBackground(Color.BLACK);
       	isMousePressed = false;
    }
    public ImagePlus getNiftiImage() {
		return niftiImage;
	}

	public void setNiftiImage(ImagePlus niftiImage) {
		this.niftiImage = niftiImage;
		setSlice(Math.round(this.niftiImage.getNSlices()/2));
		imageDim = new Dimension(this.niftiImage.getWidth(), this.niftiImage.getHeight());
		addMouseMotionListener(this);
       	addMouseListener(this);
	}

	public int getSlice() {
		return slice;
	}

	public void setSlice(int slice) {
		this.slice = slice;
		if(niftiImage!=null){
			niftiImage.setSlice(this.slice);
			image = niftiImage.getBufferedImage();
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
            if(!keepRatio){
            	g.drawImage(image,0,0, width, height, this);
            }else{
            	// on garde le ratio A FAIRE
            	g.drawImage(image,0,0, width, height, this);
            }
            if(showCrosshair && currentLocation!=null){
            	g.setColor(Color.BLUE);
            	g.drawLine((int) currentLocation.getX(), 0, (int) currentLocation.getX(), getHeight());
            	g.drawLine(0, (int) currentLocation.getY(), getWidth(), (int) currentLocation.getY());
            }
        }
    }

	public void mouseDragged(MouseEvent e) {
		if(isMousePressed){
			currentLocation = e.getPoint();
			Point p = panelXYtoImageXY(e.getPoint());
			switch(this.orientation){
			case AXIAL:
				getViewer().setXY(p);
				break;
			case SAGITTAL:
				getViewer().setYZ(p);
				break;
			case CORONAL:
				getViewer().setXZ(p);
				break;
			}
			repaint();
		}
	}

	public void mouseMoved(MouseEvent e) {
		
	}



	public void mouseClicked(MouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1){
			currentLocation = e.getPoint();
			Point p = panelXYtoImageXY(e.getPoint());
			switch(this.orientation){
			case AXIAL:
				getViewer().setXY(p);
				break;
			case SAGITTAL:
				getViewer().setYZ(p);
				break;
			case CORONAL:
				getViewer().setXZ(p);
				break;
			}
			repaint();
		}
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
		return impt;
	}

	/**
	 * convertie une coordonnee dans le referentiel image 
	 * au referentiel panel
	 * @param point
	 * @return
	 */
	private Point imageXYtoPanelXY(Point p) {
		Point impt = null;
		int pwidth = this.getWidth();
		int pheight = this.getHeight();
		double ws = (float)pwidth / imageDim.getWidth();
		double hs = (float)pheight / imageDim.getHeight();
		impt = new Point((int)Math.round(ws*p.getX()),getHeight()-(int)Math.round(hs*p.getY()));
		if(impt.getX()>getWidth())
			impt.x = (int)getWidth();
		if(impt.getX()<0)
			impt.x = 0;
		if(impt.getY()>getHeight())
			impt.y = (int)getHeight();
		if(impt.getY()<0)
			impt.y = 0;
		return impt;
	}
	
	/**
	 * Met a jours le crosshair 
	 * a partir de coordonnees dans l'espace image
	 * @param coord
	 */
	public void updateCrosshair(int[] coord) {
		Point p = null;
		switch(this.orientation){
		case AXIAL:
			p = new Point(coord[0],coord[1]);
			break;
		case SAGITTAL:
			p = new Point(coord[1],coord[2]);
			break;
		case CORONAL:
			p = new Point(coord[0],coord[2]);
			break;
		}
		currentLocation = imageXYtoPanelXY(p);
		repaint();
	}

	/**
	 * Remet le panel a 0 
	 */
	public void reset() {
		niftiImage = null;
		currentLocation = null;
		image = null;
		removeMouseMotionListener(this);
		removeMouseListener(this);
		repaint();
	}
}