package display.containers.viewer;



import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.measure.Calibration;
import ij.plugin.filter.Rotator;
import ij.process.ImageStatistics;
import ij.process.LUT;

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
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

import daemon.tools.nifti.CoordinateMapper;
import daemon.tools.nifti.Coordinate_Viewer;
import daemon.tools.nifti.LutLoader;
import daemon.tools.nifti.LutLoader.ALUT;
import daemon.tools.nifti.Nifti_Reader;
import daemon.tools.nifti.Slicer;
import display.MainWindow;
import display.containers.UserCreationPanel;
import display.containers.WarningPanel;
import display.containers.viewer.NiftiImagePanel.Plan;
import net.miginfocom.swing.MigLayout;
import javax.swing.JSplitPane;

import settings.UserProfile;
import settings.WindowManager;

public class ViewerPanel extends JPanel{

	// Attributs
	private Nifti_Reader niftiAxial;
	private Nifti_Reader niftiOverlayAxial; // nifti de l'overlay
	private int rotateCheck;// le rotate qu'on a fait sur le nifti axial
	private int tempCheckRotate; //variable temporaire pour savoir le resultat du dernier checkandrotate lance
	private int[] coord; // coordonnee actuelle du pointeur (x,y,z)
	private CoordinateMapper[] coordinateMapper;
	private NiftiImagePanel axialPanel;//XY
	private NiftiImagePanel coronalPanel;//XZ
	private NiftiImagePanel sagittalPanel;//YZ
	private InformationViewer infoViewer;
	private JSplitPane splitRight;
	private JSplitPane splitLeft;
	private JSplitPane splitLeftRight;
	private LutLoader lutLoader; // loader qui permet de changer de colormap

	
	public ViewerPanel(){
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		lutLoader = new LutLoader();
		coord = new int[]{-1,-1,-1};
		niftiAxial = null;
		axialPanel = new NiftiImagePanel(this,Plan.AXIAL);
		coronalPanel = new NiftiImagePanel(this,Plan.CORONAL);
		sagittalPanel = new NiftiImagePanel(this,Plan.SAGITTAL);
		
		
		splitLeftRight = new JSplitPane();
		infoViewer = new InformationViewer(this);
		
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
		splitLeftRight.setDividerLocation(900/2);
		splitRight.setDividerLocation(525/2);
		splitLeft.setDividerLocation(525/2);

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
		
		
		//open(Paths.get("C:/Users/serge/Desktop/filetesto.nii"));//T1MPRAGE3DGADOC-C110.nii"));
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
	 * @return the coordinateMapper
	 */
	public CoordinateMapper[] getCoordinateMapper() {
		return coordinateMapper;
	}

	/**
	 * @param coordinateMapper the coordinateMapper to set
	 */
	public void setCoordinateMapper(CoordinateMapper[] coordinateMapper) {
		this.coordinateMapper = coordinateMapper;
	}

	/**
	 * Ouvre un fichier nifti
	 * @param path
	 */
	public boolean open(Path path) {
		if(niftiAxial!=null)
			reset();
		UserProfile.LAST_SELECTED_DIR = path.getParent();
		splitLeftRight.setDividerLocation(splitLeftRight.getWidth()/2);
		splitRight.setDividerLocation(splitRight.getHeight()/2);
		splitLeft.setDividerLocation(splitLeft.getHeight()/2);
		infoViewer.reset();
		getCoronalPanel().reset();
		getAxialPanel().reset();
		getSagittalPanel().reset();
		if(!path.toString().endsWith(".nii") && !path.toString().endsWith(".img") && !path.toString().endsWith(".hdr") && !path.toString().endsWith(".nii.gz"))
			return false;
		try{
			niftiAxial = new Nifti_Reader(path.toFile());
			Nifti_Reader nr = checkAndRotate(niftiAxial); // on verifie  que le "point zero" est en bas a gauche
			rotateCheck = tempCheckRotate;
			if((nr == null || niftiAxial.getProperty("coors")==null) && niftiAxial!=null){
				// si le nifti a pas un format correct on affiche un warning
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						WarningPanel wmess = new WarningPanel("Data has incorrect header ... attempt to continue");
						Popup popup = PopupFactory.getSharedInstance().getPopup(ViewerPanel.this, wmess, (int)getWidth()/2-20,(int)getHeight()/2-20);
						wmess.setPopupWindow(popup);
						popup.show();
					}
				});
				WindowManager.mwLogger.log(Level.WARNING,"Data has incorrect header ... attempt to continue [open]");
				
			}else{
				niftiAxial = nr;
				nr = null;
			}
			// on applique la colormap par defaut
			lutLoader.run(niftiAxial, infoViewer.getCurrentLUT());
			// on definit la taille du voxel
			double pixelWidth = -1; 
			double pixelHeight = -1;
			double pixelDepth = -1;
			if(niftiAxial.getCalibration() != null){
				pixelWidth = niftiAxial.getCalibration().pixelWidth; 
				pixelHeight = niftiAxial.getCalibration().pixelHeight; 
				pixelDepth = niftiAxial.getCalibration().pixelDepth; 
			}
			infoViewer.setVoxelSize(new double[]{pixelWidth,pixelHeight,pixelDepth});
			infoViewer.setFilename(path.getFileName().toString());
			// on update le mapper
			coordinateMapper = (CoordinateMapper[]) niftiAxial.getProperty("coors");	
			//niftiAxial.setSlice(Math.round(niftiAxial.getNSlices()/2));
			//niftiAxial.setSlice(40);
			Slicer slicer = new Slicer(niftiAxial);
			ImagePlus niftiCoro = slicer.flip(false, true, "Top");
			ImagePlus niftiSag = slicer.flip(false, true, "Left");

			getCoronalPanel().setNiftiImage(niftiCoro);
			getAxialPanel().setNiftiImage(niftiAxial);
			getSagittalPanel().setNiftiImage(niftiSag);
			//getAxialPanel().setOverlayImage(new Nifti_Reader(new File("C:/Users/Mobilette/Downloads/wmh_regT12.nii")));
			
			// on met a jours les coord actuelles
			coord[0] = getSagittalPanel().getSlice();
			coord[1] = getCoronalPanel().getSlice();
			coord[2] = getAxialPanel().getSlice();
			
			// on calcul les min et max de l'image
			ImageStack is = niftiAxial.getImageStack();
			double min = -Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			if(niftiAxial.getCalibration() != null && niftiAxial.getCalibration().getCoefficients()!=null){
				double[] coef = niftiAxial.getCalibration().getCoefficients();// on applique les coef pour avoir les vrai valeurs
				max = coef[0]+coef[1]*niftiAxial.getDisplayRangeMax();
				min = coef[0]+coef[1]*niftiAxial.getDisplayRangeMin();
			}else{
				max = niftiAxial.getDisplayRangeMax();
				min = niftiAxial.getDisplayRangeMin();
			}
			// update infoViewer a partir des donnees du plan axial
			infoViewer.setSpinnerParams(new Integer[]{niftiAxial.getWidth(), niftiAxial.getHeight(),
					niftiAxial.getNSlices()}, getAxialPanel().getMricronCoord(),new double[]{min,max});
			
			//Coordinate_Viewer c = new Coordinate_Viewer(niftiAxial);
			//niftiAxial.show();
			revalidate();
	
			return true;
		}catch(final Exception e){
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					JOptionPane.showMessageDialog(ViewerPanel.this,
						    "Exception : "+e.toString(),
						    "Openning error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Ouvre un overlay si il a la meme taille que l'image charge et si l'image est deja charge
	 * @param path
	 * @return
	 */
	public boolean openOverlay(Path path) {
		UserProfile.LAST_SELECTED_DIR = path.getParent();
		if(niftiAxial == null){
			// si on a pas d'image ouverte, on ne charge pas d'overlay
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					WarningPanel wmess = new WarningPanel("Can't overlay anything on nothing.");
					Popup popup = PopupFactory.getSharedInstance().getPopup(ViewerPanel.this, wmess, (int)getWidth()/2-20,(int)getHeight()/2-20);
					wmess.setPopupWindow(popup);
					popup.show();
				}
			});
			WindowManager.mwLogger.log(Level.WARNING, "Attempt to overlay on an empty image [ViewerPanel]");
			return false;
		}
		if(niftiOverlayAxial!=null)
			resetOverlay();
		if(!path.toString().endsWith(".nii") && !path.toString().endsWith(".img") && !path.toString().endsWith(".hdr") && !path.toString().endsWith(".nii.gz"))
			return false;
		try{
			niftiOverlayAxial = new Nifti_Reader(path.toFile());
			Nifti_Reader nr = checkAndRotate(niftiOverlayAxial); // on verifie  que le "point zero" est en bas a gauche
			if((nr == null || niftiOverlayAxial.getProperty("coors")==null) && niftiOverlayAxial!=null){
				// si on a pas reussi parceque le header du nifti n'est pas complet, on applique la meme rotation que
				// pour l'image originale
				niftiOverlayAxial = rotate(niftiOverlayAxial,rotateCheck); 
				// si le nifti a pas un format correct on affiche un warning
				/*SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						JOptionPane.showMessageDialog(ViewerPanel.this,
							    "Overlay has incorrect header ... using image information",
							    "Openning error",
							    JOptionPane.WARNING_MESSAGE);
					}
				});*/
				WindowManager.mwLogger.log(Level.WARNING,"Overlay has incorrect header ... using image information");
			}else{
				niftiOverlayAxial = nr;
				nr = null;
			}
			// on verifie que les dimensions sont identiques a celles de l'image affiche
			// on applique la colormap par defaut
			if(niftiOverlayAxial.getWidth()!=niftiAxial.getWidth() || niftiOverlayAxial.getHeight()!=niftiAxial.getHeight() 
					|| niftiOverlayAxial.getNSlices()!=niftiAxial.getNSlices()){
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						WarningPanel wmess = new WarningPanel("Overlay and image have different sizes.");
						Popup popup = PopupFactory.getSharedInstance().getPopup(ViewerPanel.this, wmess, (int)getWidth()/2-20,(int)getHeight()/2-20);
						wmess.setPopupWindow(popup);
						popup.show();
					}
				});
				WindowManager.mwLogger.log(Level.SEVERE,"Overlay and image have different sizes.");
				return false;
			}
			lutLoader.run(niftiOverlayAxial, infoViewer.getCurrentOverlayLUT());

			infoViewer.setOverlayFilename(path.getFileName().toString());
			// on update le mapper
			coordinateMapper = (CoordinateMapper[]) niftiOverlayAxial.getProperty("coors");	
			//niftiAxial.setSlice(Math.round(niftiAxial.getNSlices()/2));
			//niftiAxial.setSlice(40);
			Slicer slicer = new Slicer(niftiOverlayAxial);
			ImagePlus niftiOverlayCoro = slicer.flip(false, true, "Top");
			ImagePlus niftiOverlaySag = slicer.flip(false, true, "Left");

			getCoronalPanel().setOverlayImage(niftiOverlayCoro);
			getAxialPanel().setOverlayImage(niftiOverlayAxial);
			getSagittalPanel().setOverlayImage(niftiOverlaySag);

			
			// on calcul les min et max de l'image
			double min = -Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			double[] coef = niftiOverlayAxial.getCalibration().getCoefficients();// on applique les coef pour avoir les vrai valeurs
			max = coef[0]+coef[1]*niftiOverlayAxial.getDisplayRangeMax();
			min = coef[0]+coef[1]*niftiOverlayAxial.getDisplayRangeMin();
			// update infoViewer a partir des donnees du plan axial
			infoViewer.setOverlaySpinnerParams(new double[]{min,max});
			infoViewer.unlockOverlayPanel();
			revalidate();
			updateCrosshair();
			return true;
		}catch(final Exception e){
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					JOptionPane.showMessageDialog(ViewerPanel.this,
						    "Exception : "+e.toString(),
						    "Openning error",
						    JOptionPane.ERROR_MESSAGE);
				}
			});
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Tourne l'image en fonction du code rotation (0: ne tourne pas, 1: flip vertical, 2: flip vertical
	 * et horizontal, 3: fliphorizontal)
	 * @param im
	 * @param rotation
	 * @return
	 */
	private Nifti_Reader rotate(Nifti_Reader im,
			int rotation) {
		Nifti_Reader nr = (Nifti_Reader) im.clone();
		ImageStack is;
		switch(rotation){
		case 0:
			return nr;
		case 1:
			is = im.getImageStack();
			for(int i = 1 ; i <= im.getNSlices();i++){
				is.getProcessor(i).flipVertical();
			}
			nr.setStack(is);
			return nr;
		case 2:
			is = im.getImageStack();
			for(int i = 1 ; i <= im.getNSlices();i++){
				is.getProcessor(i).flipVertical();
				is.getProcessor(i).flipHorizontal();
			}
			nr.setStack(is);
			return nr;
		case 3:
			is = im.getImageStack();
			for(int i = 1 ; i <= im.getNSlices();i++){
				is.getProcessor(i).flipHorizontal();
			}
			nr.setStack(is);
			return nr;
		default:
			WindowManager.mwLogger.log(Level.SEVERE,"Unknow rotation code");
			return null;
		}
	}

	/**
	 * Verifie  si l'image est bien positionne (c a d point de coordonnee minimale 
	 * en X Y en bas a gauche de l'image). Si ce n'est pas le cas on fait les flip en consequence
	 * @param im
	 * @return
	 */
	private Nifti_Reader checkAndRotate(Nifti_Reader im) {
		Object prop = im.getProperty("coors");
		if (prop == null){tempCheckRotate = 0;return rotate(im, tempCheckRotate);}

		CoordinateMapper[] mapper = (CoordinateMapper[] ) prop;
		// on test les 3 position mauvaises (en haut a gauche / droite, et en bas a droite)
		if(mapper[mapper.length-1].getX(1, 1, 1)<0 && mapper[mapper.length-1].getY(1, 1, 1)<0){
			tempCheckRotate = 1;
		}else{
			if(mapper[mapper.length-1].getX(im.getProcessor().getWidth()-1, 1, 1)<0 && mapper[mapper.length-1].getY(im.getProcessor().getWidth()-1, 1, 1)<0){
				tempCheckRotate = 2;
			}else{
				if(mapper[mapper.length-1].getX(im.getProcessor().getWidth()-1, im.getProcessor().getHeight()-1, 1)<0 && mapper[mapper.length-1].getY(im.getProcessor().getWidth()-1, im.getProcessor().getHeight()-1, 1)<0){
					tempCheckRotate = 3;
				}
			}
		}
		return rotate(im, tempCheckRotate);
	}

	/**
	 * Ferme l'image actuellement ouverte
	 */
	public void reset(){
		niftiAxial = null;
		coord = new int[]{-1,-1,-1};
		getCoronalPanel().reset();
		getSagittalPanel().reset();
		getAxialPanel().reset();
	}
	
	/**
	 * Ferme l'overlay actuellement ouvert
	 */
	public void resetOverlay(){
		niftiOverlayAxial = null;
		infoViewer.lockOverlayPanel();
		getCoronalPanel().resetOverlay();
		getSagittalPanel().resetOverlay();
		getAxialPanel().resetOverlay();
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
		infoViewer.setRawCoord(getAxialPanel().getMricronCoord());
		infoViewer.setVoxelValue((float) getAxialPanel().getVoxelValue());
		if(niftiOverlayAxial!=null)
			infoViewer.setVoxelOverlayValue((float) getAxialPanel().getVoxelOverlayValue());
		if(coordinateMapper!=null)
			infoViewer.setAlignedCoord(new float[]{(float) coordinateMapper[coordinateMapper.length-1].getX(coord[0],coord[1],coord[2]),(float) coordinateMapper[coordinateMapper.length-1].getY(coord[0],coord[1],coord[2]),(float) coordinateMapper[coordinateMapper.length-1].getZ(coord[0],coord[1],coord[2])});
		else
			infoViewer.setAlignedCoord("NaN","NaN","NaN");
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
		if(niftiAxial!=null){
			getAxialPanel().setKeepRatio(b);
			getCoronalPanel().setKeepRatio(b);
			getSagittalPanel().setKeepRatio(b);
			updateCrosshair();
		}
	}

	public boolean isCrosshairVisible() {
		return getAxialPanel().isShowCrosshair() && getCoronalPanel().isShowCrosshair() && getSagittalPanel().isShowCrosshair();
	}

	public void setCrosshairVisible(boolean b) {
		if(niftiAxial!=null){
			getAxialPanel().setShowCrosshair(b);
			getCoronalPanel().setShowCrosshair(b);
			getSagittalPanel().setShowCrosshair(b);
			updateCrosshair();
		}
	}

	/**
	 * Modifie la valeur en XYZ (issue de coordonnee MRI cron like)
	 * @param value
	 */
	public void setXYZfromMricron(Integer[] value) {
		Integer[] val = getAxialPanel().mricronCoordToLocal(value);
		for(int i = 0; i<3;i++)
			coord[i] = val[i];
		getAxialPanel().setSlice(coord[2]);
		getCoronalPanel().setSlice(coord[1]);
		getSagittalPanel().setSlice(coord[0]);
		updateCrosshair();
	}
	
	/**
	 * Definit les min et max pour la colormap
	 * @param min
	 * @param max
	 */
	public void setDisplayMinMax(double min,double max){
		getAxialPanel().getNiftiImage().setDisplayRange(min, max);
		getCoronalPanel().getNiftiImage().setDisplayRange(min, max);
		getSagittalPanel().getNiftiImage().setDisplayRange(min, max);
		getAxialPanel().refreshImage();
		getCoronalPanel().refreshImage();
		getSagittalPanel().refreshImage();
	}

	/**
	 * Definit les min max pour l'overlay
	 * @param min
	 * @param max
	 */
	public void setDisplayOverlayMinMax(double min, double max) {
		getAxialPanel().getOverlayImage().setDisplayRange(min, max);
		getCoronalPanel().getOverlayImage().setDisplayRange(min, max);
		getSagittalPanel().getOverlayImage().setDisplayRange(min, max);
		getAxialPanel().refreshOverlay();
		getCoronalPanel().refreshOverlay();
		getSagittalPanel().refreshOverlay();
	}

	/**
	 * Change la colormap
	 * @param lut
	 */
	public void setLUT(ALUT lut){
		lutLoader.run(getAxialPanel().getNiftiImage(), lut);
		lutLoader.run(getCoronalPanel().getNiftiImage(), lut);
		lutLoader.run(getSagittalPanel().getNiftiImage(), lut);
		getAxialPanel().refreshImage();
		getCoronalPanel().refreshImage();
		getSagittalPanel().refreshImage();
	}

	/**
	 * Change la colormap de l'overlay
	 * @param selectedLut
	 */
	public void setOverlayLUT(ALUT lut) {
		lutLoader.run(getAxialPanel().getOverlayImage(), lut);
		lutLoader.run(getCoronalPanel().getOverlayImage(), lut);
		lutLoader.run(getSagittalPanel().getOverlayImage(), lut);
		getAxialPanel().refreshOverlay();
		getCoronalPanel().refreshOverlay();
		getSagittalPanel().refreshOverlay();
	}

	/**
	 * Definit l'alpha de l'overlay
	 * @param alpha
	 */
	public void setOverlayAlpha(float alpha) {
		getAxialPanel().setAlphaOverlay(alpha);
		getCoronalPanel().setAlphaOverlay(alpha);
		getSagittalPanel().setAlphaOverlay(alpha);
		getAxialPanel().refreshOverlay();
		getCoronalPanel().refreshOverlay();
		getSagittalPanel().refreshOverlay();
	}
}
