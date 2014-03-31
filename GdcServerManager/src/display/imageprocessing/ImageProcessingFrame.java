package display.imageprocessing;


import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import settings.WindowManager;

public class ImageProcessingFrame extends JFrame {
	
	// ----- Variables static --------
	public static final String TITLE = "Image Processing";
	public static final int WIDTH = 600;
	public static final int HEIGHT = 700;
	
	
	
	public ImageProcessingFrame(ArrayList<File> directories){
		super();
		// init de la fenetre
		createAndShowGUI();
		
		// Affiche les repertoires selectionnes
		for(File fi:directories)
			System.out.println(fi.getAbsolutePath());
		
	}

	private void createAndShowGUI() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(WIDTH,HEIGHT);
		this.setTitle(TITLE);
		this.setLocation(WindowManager.MAINWINDOW.getLocation());
		this.setIconImage(new ImageIcon(this.getClass().getResource("/images/logo32.png")).getImage());
		this.setVisible(true);
	}

}
