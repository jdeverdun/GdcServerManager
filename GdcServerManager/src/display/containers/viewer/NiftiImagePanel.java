package display.containers.viewer;


import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import daemon.tools.nifti.Nifti_Reader;

import java.io.*;
 
 
/**
 * Display image (ImagePlus) in a panel
 * 
 * ATTTENTION : utiliser getMricronCoord pour repasser dans l'espace de 
 * coordonnee MRICRON like
 * @author Jérémy DEVERDUN
 *
 */
public class NiftiImagePanel extends JPanel implements ComponentListener, MouseWheelListener, MouseListener, MouseMotionListener {
	public static enum Plan{AXIAL, SAGITTAL, CORONAL}; // les differents plan possible
	public static final int AXIAL = 1;
	
	private ImagePlus niftiImage = null;
	private BufferedImage image = null; // on stock l'image courante
	private ViewerPanel viewer = null;
	private boolean isMousePressed = false;
	private int slice = 1;
	private Plan orientation; // vue associe a ce panel (axial sagital etc)
	private Dimension imageDim; // dimension de l'ImagePlus (dim 2D)
	private Point currentLocation; // dernier point clique dans referentiel panel
	private Point imageCurrentLocation; // dernier point clique dans referentiel image
	private boolean showCrosshair = true; // si on montre le crosshair ou pas
	private boolean keepRatio = true;//false; // si on contraint l'affichage pourver le ratio original
	private int zoom = 1;//zoom (multiplicateur)
	private Point zoomlocation; // coordonnees du zoom
	private double displayScaleFactor = 1d;
	private Dimension offsets; // quand on est en mode keepRatio les offsets
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
    	setBackground(new Color(0, 0, 0));
       	isMousePressed = false;
       	setZoom(1);
       	keepRatio = true;
       	displayScaleFactor = 1d;
       	offsets = new Dimension(0,0);
       	imageCurrentLocation = null;
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
       	addMouseWheelListener(this);
       	addComponentListener(this);
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

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	/**
     * 
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g); //paint background
        if (niftiImage != null) { //there is a picture: draw it
            int height = this.getSize().height;
            int width = this.getSize().width;      
            if(keepRatio){
            	int scaleWidth;
                int scaleHeight;
            	if(height>image.getHeight() && width>image.getWidth()){
            		displayScaleFactor = Math.max(1d, getScaleFactorToFit(new Dimension(image.getWidth(), image.getHeight()),getSize()));
            		scaleWidth = (int) Math.round(image.getWidth() * displayScaleFactor);
                    scaleHeight = (int) Math.round(image.getHeight() * displayScaleFactor);
            	}else{
            		displayScaleFactor = Math.min(1d, getScaleFactorToFit(new Dimension(image.getWidth(), image.getHeight()), getSize()));
            		scaleWidth = (int) Math.round(image.getWidth() * displayScaleFactor);
                    scaleHeight = (int) Math.round(image.getHeight() * displayScaleFactor);
            	}
                //Image scaled = image.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);

                int widthloc = getWidth() - 1;
                int heightloc = getHeight() - 1;

                int x = (widthloc - scaleWidth) / 2;
                int y = (heightloc - scaleHeight) / 2;
                offsets.setSize(x, y);
                //g.drawImage(scaled, x, y, this);
                if(zoom > 1 && zoomlocation !=null)// si on a zoome on se centre sur le pt zoome
                	g.drawImage(image,(int)(x*zoom-zoomlocation.getX()),(int)(y*zoom-zoomlocation.getY()), scaleWidth*zoom, scaleHeight*zoom, this);
                else
                	g.drawImage(image,x,y, scaleWidth*zoom, scaleHeight*zoom, this);
            }else{
            	g.drawImage(image,0,0, width*zoom, height*zoom, this);
            }
            if(showCrosshair && currentLocation!=null){
            	g.setColor(Color.BLUE);
            	//System.out.println(orientation+"@@"+imageCurrentLocation.x+"-"+imageCurrentLocation.y+"-"+getSlice());
            	g.drawLine((int)Math.round(currentLocation.getX()), 0, (int) Math.round(currentLocation.getX()), getHeight());
            	g.drawLine(0, (int) Math.round(currentLocation.getY()), getWidth(), (int) Math.round(currentLocation.getY()));
            }
        }
    }

    /**
     * Recupere la valeur du pixel p pour la slice courrante
     * qunad on est pas en AXIAL les y sont inverse attention
     * @param p
     */
    public double getValueAt(Point p){
    	switch(niftiImage.getBitDepth()){
    	case 8:
    		ByteProcessor bp = (ByteProcessor) niftiImage.getProcessor();
    		if(orientation != Plan.AXIAL)
    			return (double)bp.get((int)Math.round(p.getX())-1, (int)Math.round(imageDim.getHeight()-p.getY()));
    		else
    			return (double)bp.get((int)Math.round(p.getX())-1, (int)Math.round(p.getY())-1);
    	case 16:
    		ShortProcessor sp = (ShortProcessor) niftiImage.getProcessor();
    		if(orientation != Plan.AXIAL)
    			return (double)sp.getPixelValue((int)Math.round(p.getX())-1, (int)Math.round(imageDim.getHeight()-p.getY()));
    		else
    			return (double)sp.getPixelValue((int)Math.round(p.getX())-1, (int)Math.round(p.getY())-1);
    	case 32:
    		FloatProcessor fp = (FloatProcessor) niftiImage.getProcessor();
    		if(orientation != Plan.AXIAL)
    			return (double)fp.get((int)Math.round(p.getX())-1, (int)Math.round(p.getY())-1);
    		else
    			return (double)fp.get((int)Math.round(p.getX())-1, (int)Math.round(imageDim.getHeight()-p.getY()));
    	default:
    		return -1;
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
			imageCurrentLocation = p;
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
			imageCurrentLocation = p;
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
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL){
			zoom = Math.max(1, zoom - e.getWheelRotation());
			if(zoom>2)//zoom max x2
				zoom = 2;
			if(zoom == 1)
				zoomlocation = null;
			else
				zoomlocation = e.getPoint();
			repaint();
		}
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
		if(!keepRatio){
			double ws = imageDim.getWidth() / (float)pwidth ;
			double hs = imageDim.getHeight() / (float)pheight;
			if(orientation != Plan.AXIAL)
				impt = new Point((int)Math.round(ws*point.getX()),(int)imageDim.getHeight()-(int)Math.round(hs*point.getY()));
			else
				impt = new Point((int)Math.round(ws*point.getX()),(int)Math.round(hs*point.getY()));
			if(impt.getX()>imageDim.getWidth())
				impt.x = (int)imageDim.getWidth();
			if(impt.getX()<=0)
				impt.x = 1;
			if(impt.getY()>imageDim.getHeight())
				impt.y = (int)imageDim.getHeight();
			if(impt.getY()<=0)
				impt.y = 1;
		}else{
			double x = (point.getX() - offsets.width)/displayScaleFactor;
			double y = (point.getY() - offsets.height)/displayScaleFactor;
			if(orientation != Plan.AXIAL){
				impt = new Point((int)Math.round(x),(int)imageDim.getHeight()-(int)Math.round(y));
			}else
				impt = new Point((int)Math.round(x),(int)Math.round(y));
			if(impt.getX()>imageDim.getWidth())
				impt.x = (int)imageDim.getWidth();
			if(impt.getX()<=0)
				impt.x = 1;
			if(impt.getY()>imageDim.getHeight()){
				impt.y = (int)imageDim.getHeight();
			}
			if(impt.getY()<=0){
				impt.y = 1;
			}
		}
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
		if(!keepRatio){
			double ws = (float)pwidth / imageDim.getWidth();
			double hs = (float)pheight / imageDim.getHeight();
			if(orientation != Plan.AXIAL)
				impt = new Point((int)Math.round(ws*p.getX()),getHeight()-(int)Math.round(hs*p.getY()));
			else
				impt = new Point((int)Math.round(ws*p.getX()),(int)Math.round(hs*p.getY()));
			if(impt.getX()>getWidth())
				impt.x = (int)getWidth();
			if(impt.getX()<=0)
				impt.x = 1;
			if(impt.getY()>getHeight())
				impt.y = (int)getHeight();
			if(impt.getY()<=0)
				impt.y = 1;
		}else{
			double x = (p.getX()*displayScaleFactor) + offsets.width-1;
			double y = (p.getY()*displayScaleFactor) + offsets.height-1;
			if(orientation != Plan.AXIAL)
				impt = new Point((int)Math.round(x),(int)Math.round(pheight-(y)-1));
			else
				impt = new Point((int)Math.round(x),(int)Math.round(y));
			if(impt.getX()>=(getWidth()-offsets.width))
				impt.x = (int)getWidth()-offsets.width;
			if(impt.getX()<offsets.width)
				impt.x = offsets.width;
			if(impt.getY()>=(getHeight()-offsets.height))
				impt.y = (int)getHeight()-offsets.height;
			if(impt.getY()<offsets.height){
				impt.y = offsets.height;
			}
		}
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
		imageCurrentLocation = p;
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
		imageCurrentLocation = null;
		removeMouseMotionListener(this);
		removeMouseListener(this);
		removeMouseWheelListener(this);
		removeComponentListener(this);
		repaint();
	}
	
	/**
	 * Permet de repasser dans l'espace de coordonnee mricron (pt 0,0,0 en bas a gauche dans toute les vue)
	 * @return
	 */
	public Integer[] getMricronCoord(){
		switch(orientation){
		case AXIAL:
			return new Integer[]{(int) imageCurrentLocation.getX(),(int) (imageDim.height - imageCurrentLocation.getY()+1),getSlice()};
		case CORONAL:
			return new Integer[]{(int) imageCurrentLocation.getX(),(int) imageCurrentLocation.getY(),niftiImage.getNSlices()-getSlice()+1};
		case SAGITTAL:
			return new Integer[]{(int) ( imageDim.width - imageCurrentLocation.getX()+1),(int) (imageCurrentLocation.getY()),getSlice()};
		default:
			return null;
		}
	}
	
	/**
	 * Permet de passer de l'espace mricron a l'espace local image
	 * @return
	 */
	public Integer[] mricronCoordToLocal(Integer[] mric){
		switch(orientation){
		case AXIAL:
			return new Integer[]{mric[0],(int) (imageDim.height - mric[1]),mric[2]};
		case CORONAL:
			return new Integer[]{mric[0],mric[1],niftiImage.getNSlices()-mric[2]};
		case SAGITTAL:
			return new Integer[]{(int) ( imageDim.width - mric[0]),mric[1],mric[2]};
		default:
			return null;
		}
	}
	/**
	 * Recupere le facteur de scaling
	 * @param iMasterSize
	 * @param iTargetSize
	 * @return
	 */
	public double getScaleFactor(int iMasterSize, int iTargetSize) {

	    double dScale = 1;
	    if (iMasterSize > iTargetSize) {

	        dScale = (double) iTargetSize / (double) iMasterSize;

	    } else {

	        dScale = (double) iTargetSize / (double) iMasterSize;

	    }

	    return dScale;

	}
	
	/**
	 * Recupere le facteur de scaling en fonction de la taille du panel
	 * et de l'image
	 * @param original
	 * @param toFit
	 * @return
	 */
	public double getScaleFactorToFit(Dimension original, Dimension toFit) {

	    double dScale = 1d;

	    if (original != null && toFit != null) {

	        double dScaleWidth = getScaleFactor(original.width, toFit.width);
	        double dScaleHeight = getScaleFactor(original.height, toFit.height);

	        dScale = Math.min(dScaleHeight, dScaleWidth);

	    }

	    return dScale;

	}

	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void componentResized(ComponentEvent arg0) {
		updateCrosshair(getViewer().getCoord());
	}

	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}