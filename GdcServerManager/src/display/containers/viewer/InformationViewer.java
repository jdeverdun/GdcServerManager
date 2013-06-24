package display.containers.viewer;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JSlider;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import daemon.tools.nifti.LutLoader.ALUT;
import java.awt.Color;

/**
 * Classe gerant l'affichage des infos sur le nifti en cours de vue
 * @author serge
 *
 */
public class InformationViewer extends JPanel {

	private static final String DEFAULT_OVERLAY_TITLE = "Overlay ";
	private ViewerPanel viewer;
	private double[] voxelSize;//taille du voxel
	
	private TitledBorder titleValue;//titledborder contenant les valeurs et taille du voxel
	private TitledBorder titleOverlay;//titledborder pour l'overlay
	// pour empecher les stacks overflow entre le setvalue du spinner et le changeevent
	private boolean interceptXrawSpinner;
	private boolean interceptYrawSpinner;
	private boolean interceptZrawSpinner;
	
	// on stock lmes coefficients de scaling ax+b pour eviter de devoir tout le temsp les rechercher
	private double[] coefficientsOverlay;
	private double[] coefficientsImage;
	
	// button
	private JSpinner spinnerXraw;
	private JSpinner spinnerYraw;
	private JSpinner spinnerZraw;
	private JLabel lblValue;
	private JLabel lblAlignedval;
	private JLabel lblVoxelsize;
	private JSpinner spinnerMin;
	private JSpinner spinnerMax;
	private JComboBox comboBoxLUT;
	private final JPanel panelOverlay = new JPanel();
	private JLabel labelOverlayMin;
	private JSpinner spinnerOverlayMin;
	private JLabel labelOverlayMax;
	private JSpinner spinnerOverlayMax;
	private JComboBox comboBoxOverlayLUT;
	private JSlider sliderAlpha;
	private JLabel lblVoxelOverlayValue;
	public InformationViewer(ViewerPanel v){
		setViewer(v);
		interceptXrawSpinner = false;
		interceptYrawSpinner = false;
		interceptZrawSpinner = false;
		
		setLayout(new MigLayout("", "[grow]", "[][grow][grow]"));
		
		JPanel panelValue = new JPanel();
		add(panelValue, "cell 0 0,growx");
		titleValue = new TitledBorder(null, "None", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(210, 105, 30));
		panelValue.setBorder(titleValue);
		panelValue.setLayout(new MigLayout("", "[42.00px][][248.00,grow][28.00,left][]", "[14px]"));
		
		lblValue = new JLabel("NaN");
		lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelValue.add(lblValue, "flowx,cell 0 0,growx,aligny center");
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		panelValue.add(separator_1, "cell 1 0,grow");
		
		lblAlignedval = new JLabel("(NaN,NaN,NaN)");
		panelValue.add(lblAlignedval, "cell 2 0,alignx center,aligny center");
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		panelValue.add(separator, "cell 3 0,alignx left,growy");
		
		lblVoxelsize = new JLabel("NaN,NaN,NaN");
		panelValue.add(lblVoxelsize, "cell 4 0,growx,aligny center");
		
		lblVoxelOverlayValue = new JLabel("[NaN]");
		panelValue.add(lblVoxelOverlayValue, "cell 0 0");
		
		JPanel panelCoord = new JPanel();
		panelCoord.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(210, 105, 30)));
		add(panelCoord, "cell 0 1,grow");
		panelCoord.setLayout(new MigLayout("", "[grow][grow][grow][grow][grow][grow]", "[][]"));
		
		JLabel lblX = new JLabel("X");
		panelCoord.add(lblX, "cell 0 0,alignx center,aligny center");
		
		spinnerXraw = new JSpinner();
		spinnerXraw.setMaximumSize(new Dimension(70, 20));
		spinnerXraw.setMinimumSize(new Dimension(40, 20));
		spinnerXraw.setModel(new SpinnerNumberModel(0, 0, 0, 1));
		panelCoord.add(spinnerXraw, "cell 1 0,growx,aligny center");
		
		JLabel lblY = new JLabel("Y");
		panelCoord.add(lblY, "cell 2 0,alignx center,aligny center");
		
		spinnerYraw = new JSpinner();
		spinnerYraw.setMaximumSize(new Dimension(70, 20));
		spinnerYraw.setMinimumSize(new Dimension(40, 20));
		spinnerYraw.setModel(new SpinnerNumberModel(0, 0, 0, 1));
		panelCoord.add(spinnerYraw, "flowx,cell 3 0,growx,aligny center");
		
		JLabel lblZ = new JLabel("Z");
		panelCoord.add(lblZ, "cell 4 0,alignx center,aligny center");
		
		spinnerZraw = new JSpinner();
		spinnerZraw.setMaximumSize(new Dimension(70, 20));
		spinnerZraw.setMinimumSize(new Dimension(40, 20));
		spinnerZraw.setModel(new SpinnerNumberModel(0, 0, 0, 1));
		panelCoord.add(spinnerZraw, "cell 5 0,growx,aligny center");
		
		JLabel lblMin = new JLabel("Min");
		panelCoord.add(lblMin, "cell 0 1,alignx center");
		
		spinnerMin = new JSpinner();
		spinnerMin.setMinimumSize(new Dimension(40, 20));
		spinnerMin.setMaximumSize(new Dimension(70, 20));
		spinnerMin.setModel(new SpinnerNumberModel(new Float(0), new Float(0), new Float(0), new Float(1)));
		panelCoord.add(spinnerMin, "cell 1 1,growx,aligny center");
		
		JLabel lblMax = new JLabel("Max");
		panelCoord.add(lblMax, "cell 2 1,alignx center,aligny center");
		
		spinnerMax = new JSpinner();
		spinnerMax.setMinimumSize(new Dimension(40, 20));
		spinnerMax.setMaximumSize(new Dimension(70, 20));
		spinnerMax.setModel(new SpinnerNumberModel(new Float(0), new Float(0), new Float(0), new Float(1)));
		panelCoord.add(spinnerMax, "cell 3 1,grow");
		
		comboBoxLUT = new JComboBox();
		comboBoxLUT.setMinimumSize(new Dimension(40, 20));
		comboBoxLUT.setMaximumSize(new Dimension(100, 20));
		comboBoxLUT.setModel(new DefaultComboBoxModel(ALUT.values()));
		comboBoxLUT.setSelectedIndex(2);
		panelCoord.add(comboBoxLUT, "cell 4 1,growx,aligny center");
		titleOverlay = new TitledBorder(null, DEFAULT_OVERLAY_TITLE, TitledBorder.LEADING, TitledBorder.TOP, null, new Color(210, 105, 30));
		panelOverlay.setBorder(titleOverlay);
		add(panelOverlay, "cell 0 2,grow");
		
		panelOverlay.setLayout(new MigLayout("", "[grow][grow][grow][grow][grow]", "[][]"));
		
		labelOverlayMin = new JLabel("Min");
		labelOverlayMin.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelOverlay.add(labelOverlayMin, "cell 0 0,alignx center");
		
		spinnerOverlayMin = new JSpinner();
		spinnerOverlayMin.setMinimumSize(new Dimension(40, 20));
		spinnerOverlayMin.setMaximumSize(new Dimension(70, 20));
		panelOverlay.add(spinnerOverlayMin, "cell 1 0,grow");
		
		labelOverlayMax = new JLabel("Max");
		labelOverlayMax.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelOverlay.add(labelOverlayMax, "cell 2 0,alignx center");
		
		spinnerOverlayMax = new JSpinner();
		spinnerOverlayMax.setMinimumSize(new Dimension(40, 20));
		spinnerOverlayMax.setMaximumSize(new Dimension(70, 20));
		panelOverlay.add(spinnerOverlayMax, "cell 3 0,grow");
		
		sliderAlpha = new JSlider();
		sliderAlpha.setPreferredSize(new Dimension(150, 23));
		panelOverlay.add(sliderAlpha, "cell 4 0,grow");
		
		comboBoxOverlayLUT = new JComboBox();
		comboBoxOverlayLUT.setModel(new DefaultComboBoxModel(ALUT.values()));
		comboBoxOverlayLUT.setSelectedIndex(2);
		comboBoxOverlayLUT.setMinimumSize(new Dimension(40, 20));
		comboBoxOverlayLUT.setMaximumSize(new Dimension(100, 20));
		panelOverlay.add(comboBoxOverlayLUT, "cell 0 1 2 1,growx,aligny center");
		lockOverlayPanel();
		
		//event
		spinnerXraw.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent c) {
				if(interceptXrawSpinner){
					// on empeche les overflow
					interceptXrawSpinner = false;
					return;
				}
				
				getViewer().setXYZfromMricron(new Integer[]{(int) spinnerXraw.getValue(),(int) spinnerYraw.getValue(),(int) spinnerZraw.getValue()});
				interceptXrawSpinner = false;
			}
		});
		spinnerYraw.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent c) {
				if(interceptYrawSpinner){
					// on empeche les overflow
					interceptYrawSpinner = false;
					return;
				}
				getViewer().setXYZfromMricron(new Integer[]{(int) spinnerXraw.getValue(),(int) spinnerYraw.getValue(),(int) spinnerZraw.getValue()});
				interceptYrawSpinner = false;
			}
		});
		spinnerZraw.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent c) {
				if(interceptZrawSpinner){
					// on empeche les overflow
					interceptZrawSpinner = false;
					return;
				}
				getViewer().setXYZfromMricron(new Integer[]{(int) spinnerXraw.getValue(),(int) spinnerYraw.getValue(),(int) spinnerZraw.getValue()});
				interceptZrawSpinner = false;
			}
		});
		spinnerMin.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent c) {
				getViewer().setDisplayMinMax(((double) spinnerMin.getValue())/coefficientsImage[1]-coefficientsImage[0],
						((double) spinnerMax.getValue())/coefficientsImage[1]-coefficientsImage[0]);
			}
		});
		spinnerMax.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent c) {
				getViewer().setDisplayMinMax(((double) spinnerMin.getValue())/coefficientsImage[1]-coefficientsImage[0], 
						((double) spinnerMax.getValue())/coefficientsImage[1]-coefficientsImage[0]);
			}
		});
		spinnerOverlayMax.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent c) {
				getViewer().setDisplayOverlayMinMax(((double) spinnerOverlayMin.getValue())/coefficientsOverlay[1]-coefficientsOverlay[0],
						((double) spinnerOverlayMax.getValue())/coefficientsOverlay[1]-coefficientsOverlay[0]);
			}
		});
		spinnerOverlayMin.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent c) {
				getViewer().setDisplayOverlayMinMax(((double) spinnerOverlayMin.getValue())/coefficientsOverlay[1]-coefficientsOverlay[0], 
						((double) spinnerOverlayMax.getValue())/coefficientsOverlay[1]-coefficientsOverlay[0]);
			}
		});
		comboBoxLUT.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ALUT selectedLut = (ALUT) comboBoxLUT.getSelectedItem();
				getViewer().setLUT(selectedLut);
				
			}
		});
		comboBoxOverlayLUT.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ALUT selectedLut = (ALUT) comboBoxOverlayLUT.getSelectedItem();
				getViewer().setOverlayLUT(selectedLut);
				
			}
		});
		sliderAlpha.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent c) {
				getViewer().setOverlayAlpha(((float) sliderAlpha.getValue())/100.0f);
			}
		});
	}

	
	public ViewerPanel getViewer() {
		return viewer;
	}

	public JSpinner getSpinnerXraw() {
		return spinnerXraw;
	}

	public void setSpinnerXraw(JSpinner spinnerXraw) {
		this.spinnerXraw = spinnerXraw;
	}

	public JSpinner getSpinnerYraw() {
		return spinnerYraw;
	}

	public void setSpinnerYraw(JSpinner spinnerYraw) {
		this.spinnerYraw = spinnerYraw;
	}

	public JSpinner getSpinnerZraw() {
		return spinnerZraw;
	}

	public void setSpinnerZraw(JSpinner spinnerZraw) {
		this.spinnerZraw = spinnerZraw;
	}

	public void setViewer(ViewerPanel viewer) {
		this.viewer = viewer;
	}
	


	public JLabel getLblValue() {
		return lblValue;
	}


	public void setLblValue(JLabel lblValue) {
		this.lblValue = lblValue;
	}


	/**
	 * reset l'infoviewer
	 */
	public void reset(){
		interceptXrawSpinner = false;
		interceptYrawSpinner = false;
		interceptZrawSpinner = false;
		lockOverlayPanel();

		setVoxelSize(new double[]{0.0,0.0,0.0});
		setFilename("None");
		setAlignedCoord(new float[]{0.0f,0.0f,0.0f});
		setSpinnerParams(new Integer[]{1,1,1}, new Integer[]{1,1,1},new double[]{1,1,1});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				spinnerXraw.setModel(new SpinnerNumberModel(0, 0, 0, 1));
				spinnerYraw.setModel(new SpinnerNumberModel(0, 0, 0, 1));
				spinnerZraw.setModel(new SpinnerNumberModel(0, 0, 0, 1));
				spinnerMin.setModel(new SpinnerNumberModel(0, 0, 0, 1));
				spinnerMax.setModel(new SpinnerNumberModel(0, 0, 0, 1));
				
			}
		});
			
	}
	/**
	 * Parametre les spinner en fonction de la taille de l'image
	 * @param imageDim
	 */
	public void setSpinnerParams(final Integer[] imageDim,final Integer[] coord, final double[] minMax){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				coefficientsImage = getViewer().getAxialPanel().getCoefficients();
				if(coefficientsImage == null)
					coefficientsImage = new double[]{0.0,1.0};
				spinnerZraw.setModel(new SpinnerNumberModel((int)coord[2], 1, (int)imageDim[2], 1));
				spinnerYraw.setModel(new SpinnerNumberModel((int)coord[1], 1, (int)imageDim[1], 1));
				spinnerXraw.setModel(new SpinnerNumberModel((int)coord[0], 1, (int)imageDim[0], 1));
				spinnerMin.setModel(new SpinnerNumberModel((double)minMax[0], -Double.MAX_VALUE, Double.MAX_VALUE, 1));
				spinnerMax.setModel(new SpinnerNumberModel((double)minMax[1], -Double.MAX_VALUE, Double.MAX_VALUE, 1));
			}
		});
	}


	public void setRawCoord(Integer[] mricronCoord) {
		interceptXrawSpinner = true;
		interceptYrawSpinner = true;
		interceptZrawSpinner = true;
		spinnerXraw.setValue(mricronCoord[0]);
		spinnerYraw.setValue(mricronCoord[1]);
		spinnerZraw.setValue(mricronCoord[2]);
	}


	/**
	 * Affiche la valeur voxelValue dans le label de la valeur du voxel
	 * @param voxelValue
	 */
	public void setVoxelValue(float voxelValue) {
		getLblValue().setText(""+voxelValue);
	}
	
	/**
	 * Affiche la valeur voxelOverlayValue dans le label de la valeur du voxel en overlay
	 * @param voxelOverlayValue
	 */
	public void setVoxelOverlayValue(float voxelOverlayValue) {
		lblVoxelOverlayValue.setText("["+voxelOverlayValue+"]");
	}
	public void setAlignedCoord(float[] coord){
		lblAlignedval.setText("("+new DecimalFormat("#.##").format(coord[0])+" , "+new DecimalFormat("#.##").format(coord[1])+" , "+new DecimalFormat("#.##").format(coord[2])+")");
	}
	public void setVoxelSize(double[] size){
		voxelSize = size;
		lblVoxelsize.setText("Voxel size (mm): "+new DecimalFormat("#.##").format(size[0])+"   "+new DecimalFormat("#.##").format(size[1])+"   "+new DecimalFormat("#.##").format(size[2]));
	}
	public void setFilename(String name){
		titleValue.setTitle(name.substring(0,Math.min(50, name.length())));
	}


	public void setAlignedCoord(String x, String y, String z) {
		lblAlignedval.setText("("+x+" , "+y+" , "+z+")");
	}


	public ALUT getCurrentLUT() {
		return (ALUT) comboBoxLUT.getSelectedItem();
	}


	/**
	 * Params pour les spinners associes a l'overlay
	 * @param minMax
	 */
	public void setOverlaySpinnerParams(final double[] minMax) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				coefficientsOverlay = getViewer().getAxialPanel().getOverlayCoefficients();
				if(coefficientsOverlay == null)
					coefficientsOverlay = new double[]{0.0,1.0};
				spinnerOverlayMin.setModel(new SpinnerNumberModel((double)minMax[0], -Double.MAX_VALUE, Double.MAX_VALUE, 1));
				spinnerOverlayMax.setModel(new SpinnerNumberModel((double)minMax[1], -Double.MAX_VALUE, Double.MAX_VALUE, 1));
			}
		});
	}


	/**
	 * LUT de l'overlay
	 * @return
	 */
	public ALUT getCurrentOverlayLUT() {
		return (ALUT) comboBoxOverlayLUT.getSelectedItem();
	}


	/**
	 * Nom du fichier d'overlay
	 * @param filename
	 */
	public void setOverlayFilename(String filename) {
		titleOverlay.setTitle((DEFAULT_OVERLAY_TITLE+filename).substring(0,Math.min(50, (DEFAULT_OVERLAY_TITLE+filename).length())));
	}
	
	/**
	 * Masque le panel overlay
	 */
	public void lockOverlayPanel(){
		panelOverlay.setEnabled(false);
		spinnerOverlayMax.setEnabled(false);
		spinnerOverlayMin.setEnabled(false);
		comboBoxOverlayLUT.setEnabled(false);
		sliderAlpha.setEnabled(false);
		labelOverlayMin.setEnabled(false);
		labelOverlayMax.setEnabled(false);
		titleOverlay.setTitle(DEFAULT_OVERLAY_TITLE);
		lblVoxelOverlayValue.setVisible(false);
	}
	
	/**
	 * Affiche le panel overlay
	 */
	public void unlockOverlayPanel(){
		panelOverlay.setEnabled(true);
		spinnerOverlayMax.setEnabled(true);
		spinnerOverlayMin.setEnabled(true);
		comboBoxOverlayLUT.setEnabled(true);
		sliderAlpha.setEnabled(true);
		labelOverlayMin.setEnabled(true);
		labelOverlayMax.setEnabled(true);
		lblVoxelOverlayValue.setVisible(true);
		lblVoxelOverlayValue.setText("[NaN]");
	}

	public double getMinOverlay(){
		return ((double) spinnerOverlayMin.getValue())/coefficientsOverlay[1]-coefficientsOverlay[0];
		//return (double) spinnerOverlayMin.getValue();
	}
	public double getMaxOverlay(){
		return ((double) spinnerOverlayMax.getValue())/coefficientsOverlay[1]-coefficientsOverlay[0];
		//return (double) spinnerOverlayMax.getValue();
	}


	/**
	 * Renvoi min de l'image (de la colormap)
	 * @return
	 */
	public double getMin(){
		return ((double) spinnerMin.getValue())/coefficientsImage[1]-coefficientsImage[0];
		//return (double) spinnerMin.getValue();
	}
	/**
	 * Renvoi le max de al colormap actuelle
	 * @return
	 */
	public double getMax(){
		return ((double) spinnerMax.getValue())/coefficientsImage[1]-coefficientsImage[0];
		//return (double) spinnerMax.getValue();
	}


}
