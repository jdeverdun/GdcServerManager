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
	
	public InformationViewer(ViewerPanel v){
		setViewer(v);
		interceptXrawSpinner = false;
		interceptYrawSpinner = false;
		interceptZrawSpinner = false;
		
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Coordinates", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, "cell 0 0,grow");
		panel.setLayout(new MigLayout("", "[grow][grow][grow][grow][grow][grow]", "[]"));
		
		JLabel lblX = new JLabel("X");
		panel.add(lblX, "cell 0 0,alignx center,aligny center");
		
		spinnerXraw = new JSpinner();
		spinnerXraw.setModel(new SpinnerNumberModel(0, 0, 0, 1));
		panel.add(spinnerXraw, "cell 1 0,growx,aligny center");
		
		JLabel lblY = new JLabel("Y");
		panel.add(lblY, "cell 2 0,alignx center,aligny center");
		
		spinnerYraw = new JSpinner();
		spinnerYraw.setModel(new SpinnerNumberModel(0, 0, 0, 1));
		panel.add(spinnerYraw, "flowx,cell 3 0,growx,aligny center");
		
		JLabel lblZ = new JLabel("Z");
		panel.add(lblZ, "cell 4 0,alignx center,aligny center");
		
		spinnerZraw = new JSpinner();
		spinnerZraw.setModel(new SpinnerNumberModel(0, 0, 0, 1));
		panel.add(spinnerZraw, "cell 5 0,growx,aligny center");
		
		
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
}
