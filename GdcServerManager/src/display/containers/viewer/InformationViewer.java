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

/**
 * Classe gerant l'affichage des infos sur le nifti en cours de vue
 * @author serge
 *
 */
public class InformationViewer extends JPanel {

	private ViewerPanel viewer;
	
	// pour empecher les stacks overflow entre le setvalue du spinner et le changeevent
	private boolean interceptXrawSpinner;
	private boolean interceptYrawSpinner;
	private boolean interceptZrawSpinner;
	
	// button
	private JSpinner spinnerXraw;
	private JSpinner spinnerYraw;
	private JSpinner spinnerZraw;
	private JSpinner spinnerXaligned;
	private JSpinner spinnerYaligned;
	private JSpinner spinnerZaligned;
	private JLabel lblValue;
	
	public InformationViewer(ViewerPanel v){
		setViewer(v);
		interceptXrawSpinner = false;
		interceptYrawSpinner = false;
		interceptZrawSpinner = false;
		
		setLayout(new MigLayout("", "[grow]", "[][grow]"));
		
		JPanel panelValue = new JPanel();
		add(panelValue, "cell 0 0,growx");
		panelValue.setBorder(new TitledBorder(null, "Value", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelValue.setLayout(new MigLayout("", "[26px,grow]", "[14px]"));
		
		lblValue = new JLabel("NaN");
		lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelValue.add(lblValue, "cell 0 0,alignx center,aligny center");
		
		JPanel panelCoord = new JPanel();
		panelCoord.setBorder(new TitledBorder(null, "Coordinates", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
		
		spinnerXaligned = new JSpinner();
		spinnerXaligned.setMaximumSize(new Dimension(70, 20));
		spinnerXaligned.setMinimumSize(new Dimension(40, 20));
		panelCoord.add(spinnerXaligned, "cell 1 1,growx");
		
		spinnerYaligned = new JSpinner();
		spinnerYaligned.setMaximumSize(new Dimension(70, 20));
		spinnerYaligned.setMinimumSize(new Dimension(40, 20));
		panelCoord.add(spinnerYaligned, "cell 3 1,growx");
		
		spinnerZaligned = new JSpinner();
		spinnerZaligned.setMaximumSize(new Dimension(70, 20));
		spinnerZaligned.setMinimumSize(new Dimension(40, 20));
		panelCoord.add(spinnerZaligned, "cell 5 1,growx");
		
		
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
	
	public JSpinner getSpinnerXAligned() {
		return spinnerXaligned;
	}


	public void setSpinnerXAligned(JSpinner spinnerXAligned) {
		this.spinnerXaligned = spinnerXAligned;
	}


	public JSpinner getSpinnerYAligned() {
		return spinnerYaligned;
	}


	public void setSpinnerYAligned(JSpinner spinnerYAligned) {
		this.spinnerYaligned = spinnerYAligned;
	}


	public JSpinner getSpinnerZAligned() {
		return spinnerZaligned;
	}


	public void setSpinnerZAligned(JSpinner spinnerZAligned) {
		this.spinnerZaligned = spinnerZAligned;
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
	}
	/**
	 * Parametre les spinner en fonction de la taille de l'image
	 * @param imageDim
	 */
	public void setSpinnerParams(final Integer[] imageDim,final Integer[] coord){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				spinnerZraw.setModel(new SpinnerNumberModel((int)coord[2], 1, (int)imageDim[2], 1));
				spinnerYraw.setModel(new SpinnerNumberModel((int)coord[1], 1, (int)imageDim[1], 1));
				spinnerXraw.setModel(new SpinnerNumberModel((int)coord[0], 1, (int)imageDim[0], 1));
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


	public void setVoxelValue(float voxelValue) {
		getLblValue().setText(""+voxelValue);
	}
}
