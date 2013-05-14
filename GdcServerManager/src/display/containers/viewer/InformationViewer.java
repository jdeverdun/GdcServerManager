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
import java.text.DecimalFormat;

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
	private JLabel lblValue;
	private JLabel lblAlignedval;
	
	public InformationViewer(ViewerPanel v){
		setViewer(v);
		interceptXrawSpinner = false;
		interceptYrawSpinner = false;
		interceptZrawSpinner = false;
		
		setLayout(new MigLayout("", "[grow]", "[][grow]"));
		
		JPanel panelValue = new JPanel();
		add(panelValue, "cell 0 0,growx");
		panelValue.setBorder(new TitledBorder(null, "Value", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelValue.setLayout(new MigLayout("", "[26px,grow][grow]", "[14px]"));
		
		lblValue = new JLabel("NaN");
		lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelValue.add(lblValue, "cell 0 0,alignx center,aligny center");
		
		lblAlignedval = new JLabel("(NaN,NaN,NaN)");
		panelValue.add(lblAlignedval, "cell 1 0,alignx center");
		
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
	public void setAlignedCoord(float[] coord){
		lblAlignedval.setText("("+new DecimalFormat("#.##").format(coord[0])+" , "+new DecimalFormat("#.##").format(coord[1])+" , "+new DecimalFormat("#.##").format(coord[2])+")");
	}
}
