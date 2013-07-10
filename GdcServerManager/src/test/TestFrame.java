package test;

import javax.swing.JFrame;
import javax.swing.JEditorPane;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class TestFrame extends JFrame {
	private JTextField txtTata;

	public TestFrame(){
		
		txtTata = new JTextField();
		txtTata.setEditable(false);
		txtTata.setText("tata");
		getContentPane().add(txtTata, BorderLayout.WEST);
		txtTata.setColumns(10);

	}
	public static void main(String[] args){
		TestFrame t = new TestFrame();
		t.setSize(400,400);
		t.setVisible(true);
		JFrame.setDefaultLookAndFeelDecorated(false);
		try {
	          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
	        } catch (Exception e) {
	          System.out.println("Substance Graphite failed to initialize");
	        }
		UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
	}
}
