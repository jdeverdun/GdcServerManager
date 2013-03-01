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
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import model.User.Acclvl;

public class UserCreationPanel extends JPanel {
	private String desriptionText;
	private JLabel descriptLabel;
	private JTextField txtLogin;
	private JTextField txtLastName;
	private Popup popup;
	private JLabel lblLastName;
	private JLabel lblFirstName;
	private JTextField txtFirstname;
	private JTextField txtMail;
	private JLabel lblEmail;
	private JLabel lblAccountLevel;
	private JComboBox comboBox;
	private JButton btnCreate;
	
	//private JButton 
	public UserCreationPanel() {
		setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
		
		lblEmail = new JLabel("email");
		add(lblEmail, "cell 0 4,alignx center,aligny center");
		
		txtMail = new JTextField();
		add(txtMail, "cell 1 4,growx");
		txtMail.setColumns(10);
		
		lblAccountLevel = new JLabel("Account level");
		add(lblAccountLevel, "cell 0 5,alignx trailing,aligny center");
		
		comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(Acclvl.values()));
		add(comboBox, "cell 1 5,growx");
		
		btnCreate = new JButton("Create");
		add(btnCreate, "cell 0 7,alignx center,aligny center");
		
		JButton btnCancel = new JButton("Cancel");
		add(btnCancel, "cell 1 7,alignx center,aligny center");
		
		
		
		// Listener
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPopupWindow().hide();
			}
		});
	}
	public String getDesriptionText() {
		return desriptionText;
	}
	public void setDesriptionText(String desriptionText) {
		this.desriptionText = desriptionText;
	}
	public JLabel getDescriptLabel() {
		return descriptLabel;
	}
	public void setDescriptLabel(JLabel descriptLabel) {
		this.descriptLabel = descriptLabel;
	}
	public JTextField getTxtLogin() {
		return txtLogin;
	}
	public void setTxtLogin(JTextField txtLogin) {
		this.txtLogin = txtLogin;
	}
	public JTextField getTxtLastName() {
		return txtLastName;
	}
	public void setTxtLastName(JTextField txtLastName) {
		this.txtLastName = txtLastName;
	}
	public Popup getPopup() {
		return popup;
	}
	public void setPopup(Popup popup) {
		this.popup = popup;
	}
	public JLabel getLblLastName() {
		return lblLastName;
	}
	public void setLblLastName(JLabel lblLastName) {
		this.lblLastName = lblLastName;
	}
	public JLabel getLblFirstName() {
		return lblFirstName;
	}
	public void setLblFirstName(JLabel lblFirstName) {
		this.lblFirstName = lblFirstName;
	}
	public JTextField getTxtFirstname() {
		return txtFirstname;
	}
	public void setTxtFirstname(JTextField txtFirstname) {
		this.txtFirstname = txtFirstname;
	}
	public JTextField getTxtMail() {
		return txtMail;
	}
	public void setTxtMail(JTextField txtMail) {
		this.txtMail = txtMail;
	}
	public JLabel getLblEmail() {
		return lblEmail;
	}
	public void setLblEmail(JLabel lblEmail) {
		this.lblEmail = lblEmail;
	}
	public JLabel getLblAccountLevel() {
		return lblAccountLevel;
	}
	public void setLblAccountLevel(JLabel lblAccountLevel) {
		this.lblAccountLevel = lblAccountLevel;
	}
	public JComboBox getComboBox() {
		return comboBox;
	}
	public void setComboBox(JComboBox comboBox) {
		this.comboBox = comboBox;
	}
	public JButton getBtnCreate() {
		return btnCreate;
	}
	public void setBtnCreate(JButton btnCreate) {
		this.btnCreate = btnCreate;
	}
	public void setPopupWindow(Popup popup) {
		this.popup = popup;
	}
	public Popup getPopupWindow(){
		return this.popup;
	}

}
