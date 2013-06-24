package display.containers.viewer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;

import display.MainWindow;
import display.SettingsFrame;

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
	private JButton btnOverlay;
	private JPopupMenu overlayPmenu;
	private JMenuItem overlayAddMitem;
	private JMenuItem overlayRemoveMitem;
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
		
		icon=new ImageIcon(MainWindow.class.getResource("/images/overlay.png"));
		img = icon.getImage();  
		newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		icon2 = new ImageIcon(newimg); 
		btnOverlay = new JButton(icon2);
		add(btnOverlay);
		overlayPmenu = new JPopupMenu();
		icon=new ImageIcon(MainWindow.class.getResource("/images/overlayadd.png"));
		img = icon.getImage();  
		newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		icon2 = new ImageIcon(newimg); 
		overlayAddMitem = new JMenuItem(icon2);//"Add");
		icon=new ImageIcon(MainWindow.class.getResource("/images/overlayremove.png"));
		img = icon.getImage();  
		newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		icon2 = new ImageIcon(newimg); 
		overlayRemoveMitem = new JMenuItem(icon2);//"Remove");
		overlayPmenu.add(overlayAddMitem);
		overlayPmenu.add(overlayRemoveMitem);
		
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
		btnOverlay.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent Me) {
				overlayPmenu.show(ViewerToolbar.this, btnOverlay.getX(), btnOverlay.getY()+btnOverlay.getHeight());
			}
		});
		overlayAddMitem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String defdir = SystemSettings.APP_DIR.toString();
				if(UserProfile.LAST_SELECTED_DIR != null){
					defdir = UserProfile.LAST_SELECTED_DIR.toString();
				}
				JFileChooser fc = new JFileChooser(defdir);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return "Nifti images (nii,hdr/img,nii.gz)";
					}
					
					@Override
					public boolean accept(File f) {
						if(f==null || f.toString()==null)
							return false;
						if(f.isDirectory())
							return true;
						if(f.toString().endsWith(".nii") || f.toString().endsWith(".img") || 
								f.toString().endsWith(".hdr") || 
								f.toString().endsWith(".nii.gz"))
							return true;
						return false;
						
					}
				});
				int retval = fc.showOpenDialog(ViewerToolbar.this);
	            if (retval == JFileChooser.APPROVE_OPTION) {
	            	final File file = fc.getSelectedFile();
	            	WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(true);
	            	revalidate();
	            	Thread tr = new Thread(new Runnable() {
						@Override
						public void run() {
							getViewer().openOverlay(file.toPath());
							WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
						}
					});
	            	tr.start();
	            }
			}
		});
		overlayRemoveMitem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getViewer().resetOverlay();
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
