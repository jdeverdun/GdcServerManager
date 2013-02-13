package display;

import javax.swing.JFrame;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import net.miginfocom.swing.MigLayout;
import java.awt.event.KeyEvent;
import javax.swing.JTextPane;


public class Authentificator extends JFrame {
	private JTextField txtUsername;
	private JPasswordField passwordField;
	public Authentificator() {
		setTitle("Authentificator");
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(new MigLayout("", "[48px,grow][68.00px][85.00px]", "[20px][14px][20px][23px][][][][]"));
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, "cell 0 0 1 8,grow");
		
		JLabel lblUsername = new JLabel("Username");
		panel.add(lblUsername, "cell 1 3,alignx left,aligny center");
		
		txtUsername = new JTextField();
		txtUsername.setText("Username");
		panel.add(txtUsername, "cell 1 4 2 1,growx,aligny center");
		txtUsername.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password");
		panel.add(lblPassword, "cell 1 5,alignx left,aligny center");
		
		passwordField = new JPasswordField();
		panel.add(passwordField, "cell 1 6 2 1,growx,aligny center");
		
		JButton btnLogin = new JButton("Login");
		panel.add(btnLogin, "cell 1 7,growx,aligny center");
		
		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel, "cell 2 7,growx,aligny center");
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, "cell 1 0 2 3,grow");
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{53, 105, 0};
		gbl_panel_1.rowHeights = new int[]{55, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblDisclaimer = new JLabel("<html><b>Disclaimer</b><br>Please login to access<br> to the software</html> ");
		GridBagConstraints gbc_lblDisclaimer = new GridBagConstraints();
		gbc_lblDisclaimer.fill = GridBagConstraints.BOTH;
		gbc_lblDisclaimer.gridwidth = 2;
		gbc_lblDisclaimer.insets = new Insets(0, 0, 0, 5);
		gbc_lblDisclaimer.gridx = 0;
		gbc_lblDisclaimer.gridy = 0;
		panel_1.add(lblDisclaimer, gbc_lblDisclaimer);
	}

}
