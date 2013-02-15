package display;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import javax.swing.JToolBar;
import javax.swing.JTable;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class MainWindow extends JFrame {
	public MainWindow() {
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		getContentPane().setLayout(new MigLayout("", "[][grow][][][grow][][][][][][][][][][][][][][][][][][][][][][][][][][132.00]", "[][grow][grow][][][][][][][][][][][][][][][][][][][][][]"));
		
		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, "cell 0 0 31 1,grow");
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, "cell 1 1 30 8,grow");
	}
	
	public static void main(String args[]){

		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				JFrame.setDefaultLookAndFeelDecorated(true);
				try {
					UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
				} catch (Exception e) {
					System.out.println("Substance Graphite failed to initialize");
				}
				MainWindow mw = new MainWindow();
				
				UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
				mw.setVisible(true);
				mw.setSize(1000, 700);
				mw.setLocationRelativeTo(null);
			}
		});
	}

}
