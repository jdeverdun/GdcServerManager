package display.containers;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import daemon.tools.nifti.Nifti_Reader;
import net.miginfocom.swing.MigLayout;

public class ViewerPanel extends JPanel{

	// Attributs
	private Nifti_Reader nifti;
	private int[] coord; // coordonnee actuelle du pointeur (x,y,z)

	
	private JLabel jl;
	public ViewerPanel(){
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		coord = new int[]{-1,-1,-1};
		nifti = null;
		jl = new JLabel();
		add(jl);
	}
	
	/**
	 * Ouvre un fichier nifti
	 * @param path
	 */
	public void open(Path path) {
		if(!path.endsWith(".nii") || !path.endsWith(".img") || !path.endsWith(".hdr") || !path.endsWith(".nii.gz"))
			return;
		if(nifti!=null)
			close();
		nifti = new Nifti_Reader(path.toFile());
		nifti.setSlice(20);
		BufferedImage bi = nifti.getBufferedImage();
		bi.flush();
		jl.setIcon(new ImageIcon(bi));
		revalidate();
	}
	
	/**
	 * Ferme l'image actuellement ouverte
	 */
	public void close(){
		nifti = null;
	}
	public static void main(String[] args){
		ViewerPanel v = new ViewerPanel();
		v.open(Paths.get("C:/Users/Analyse/Documents/MATLAB/Jeremy_these/DATA/ASL/30016/14946612_028_T1_MPRAGE_3D_20120502/c1r30016_____20120502_14946612_028_T1_MPRAGE_3D.nii"));
		JFrame testf = new JFrame("test");
		testf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		testf.setSize(400,400);
		testf.getContentPane().add(v);
		testf.setVisible(true);
	}


}
