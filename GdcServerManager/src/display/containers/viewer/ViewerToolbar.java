package display.containers.viewer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Classe permettant de gerer les boutons associe a la toolbar
 * du viewerpanel
 * @author serge
 *
 */
public class ViewerToolbar extends JToolBar {

	private ViewerPanel viewer;
	private JButton btnFit;
	private JButton btnShrink;
	private JButton btnCrosshair;
	public ViewerToolbar(ViewerPanel v){
		setViewer(v);
		ImageIcon icon=new ImageIcon(ViewerToolbar.class.getResource("/images/fit.png"));
		Image img = icon.getImage();  
		Image newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		ImageIcon icon2 = new ImageIcon(newimg); 
		btnFit = new JButton(icon2);
		add(btnFit);
		
		icon=new ImageIcon(ViewerToolbar.class.getResource("/images/shrink.png"));
		img = icon.getImage();  
		newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		icon2 = new ImageIcon(newimg); 
		btnShrink = new JButton(icon2);
		add(btnShrink);
		
		icon=new ImageIcon(ViewerToolbar.class.getResource("/images/crosshair.png"));
		img = icon.getImage();  
		newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		icon2 = new ImageIcon(newimg); 
		btnCrosshair = new JButton(icon2);
		add(btnCrosshair);
		
		
		// Listener
		btnFit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getViewer().setKeepRatio(false);
			}
		});
		btnShrink.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getViewer().setKeepRatio(true);
			}
		});
		
		btnCrosshair.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(getViewer().isCrosshairVisible())
					getViewer().setCrosshairVisible(false);
				else
					getViewer().setCrosshairVisible(true);
			}
		});
	}

	public ViewerPanel getViewer() {
		return viewer;
	}

	public void setViewer(ViewerPanel viewer) {
		this.viewer = viewer;
	}
}
