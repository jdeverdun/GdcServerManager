package display.containers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Popup;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class UserCreationPanel extends JPanel {
	private String desriptionText;
	private JLabel descriptLabel;
	private JTextField txtLogin;
	private JTextField txtLastName;
	private Popup popup;
	private JLabel lblLastName;
	private JLabel lblFirstName;
	private JTextField txtFirstname;
	
	//private JButton 
	public UserCreationPanel() {
		setLayout(new MigLayout("", "[89.00,grow][117.00,grow]", "[44.00][][][][][][][][][]"));
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, "cell 0 0 2 1,grow");
		panel.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));
		
		descriptLabel = new JLabel("<html><b>User creation</b><br /> Please fill each field</html>");
		panel.add(descriptLabel, "cell 0 0,grow");
		
		JLabel lblLogin = new JLabel("Login");
		lblLogin.setFont(new Font("Tahoma", Font.PLAIN, 11));
		add(lblLogin, "cell 0 1,alignx center,aligny center");
		
		txtLogin = new JTextField();
		add(txtLogin, "cell 1 1,grow");
		txtLogin.setColumns(10);
		txtLogin.setEditable(true);
		
		lblLastName = new JLabel("Last Name");
		add(lblLastName, "cell 0 2,alignx center,aligny center");
		txtLastName = new JTextField();
		add(txtLastName, "cell 1 2,growx");
		txtLastName.setColumns(10);
		
		lblFirstName = new JLabel("First Name");
		add(lblFirstName, "cell 0 3,alignx center,aligny center");
		
		txtFirstname = new JTextField();
		add(txtFirstname, "cell 1 3,growx");
		txtFirstname.setColumns(10);
		
		JButton btnCancel = new JButton("Cancel");
		add(btnCancel, "cell 1 9,alignx center,aligny center");
		
		
		
		// Listener
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPopupWindow().hide();
			}
		});
	}
	public void setPopupWindow(Popup popup) {
		this.popup = popup;
	}
	public Popup getPopupWindow(){
		return this.popup;
	}

}
