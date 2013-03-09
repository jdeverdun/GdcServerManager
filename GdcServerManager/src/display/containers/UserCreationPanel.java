package display.containers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Popup;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;

import java.awt.Font;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import dao.DataBaseAdminDAO;
import dao.MySQLDataBaseAdminDAO;
import dao.MySQLUserDAO;
import dao.UserDAO;


import tools.Mailer;
import model.User;
import model.User.Acclvl;
import java.awt.FlowLayout;

public class UserCreationPanel extends JPanel {
	private static String HEADERTXT = "<b>User creation</b><br /> Please fill each field";
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
	private JButton btnCancel;
	private ProgressPanel progressPanel;
	private JLabel lblWarning;
	
	//private JButton 
	public UserCreationPanel() {
		setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new MigLayout("", "[89.00,grow][117.00,grow]", "[44.00,grow][][][][][][][][][6.00,grow,fill][][]"));
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, "cell 0 0 2 1,grow");
		panel.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));
		
		descriptLabel = new JLabel("<html>"+UserCreationPanel.HEADERTXT +"</html>");
		panel.add(descriptLabel, "flowy,cell 0 0,grow");
		
		lblWarning = new JLabel("");
		lblWarning.setVisible(false);
		panel.add(lblWarning, "cell 0 0,aligny center");
		
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
		
		btnCancel = new JButton("Cancel");
		add(btnCancel, "cell 1 7,alignx center,aligny center");
		
		progressPanel = new ProgressPanel();
		progressPanel.setVisible(false);
		add(progressPanel, "cell 0 8 2 1,growx,aligny baseline");
		progressPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		//progressPanel.setVisible(false);
		// Listener
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPopupWindow().hide();
			}
		});
		btnCreate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// on valide les champs
				int returnStatus = checkField();
				switch(returnStatus){
				case 0:
					Thread mailSender = new Thread(){
						public void run(){
							// On construit notre nouvel utilisateur
							Acclvl acclevel = (Acclvl) getComboBox().getSelectedItem();
							int level = -1;
							switch(acclevel){
							case SIMPLE:
								level = 1;
								break;
							case ADMIN:
								level = 3;
								break;
							}
							
							// On cree le nouvel utilisateur
							User u = new User(getTxtFirstname().getText(), getTxtLastName().getText(), getTxtMail().getText(), getTxtLogin().getText(), level);
							UserDAO udao = new MySQLUserDAO();
							try {
								u.setId(udao.idmax()+1);
							}catch(Exception e){
								e.printStackTrace();
							}
							
							// On s'assure qu'il est valide
							if(!u.isReadyForInsert()){
								setWarning("Code Error. Contact an admin");
								return;
							}
							// on essai d'inserer le nouvel utilisateur dans la bdd
							DataBaseAdminDAO dbdao = new MySQLDataBaseAdminDAO();
							try {
								dbdao.createUser(u);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
							try {
								int insertstatus = udao.newUser(u);
								switch(insertstatus){
								case 1:
									setWarning("Login already exists.");
									break;
								case 2:
									setWarning("SQL Error. Contact an admin");
									return;
								case 3:
									setWarning("Code Error. Contact an admin");
									return;
								}
							} catch (SQLException e) {
								setLock(false);
								progressPanel.setVisible(false);
								e.printStackTrace();
								return;
							}
							
							// On envoi le mail avec le mot de passe temporaire
							Mailer mailer = new Mailer(u.getEmail());
							
							boolean succeed = mailer.sendMail("GDC password", "Here is your temporary password : "+u.getPassword()+" \n Please change it ASAP.");
							if(succeed)
								getPopup().hide();
							else{
								// si le mail est pas partie
								setWarning("Error with mailing.");
								setLock(false);
								progressPanel.setVisible(false);
							}
								
						}
					};
					getLblWarning().setText("");
					getLblWarning().setVisible(false);
					mailSender.start();
					setLock(true);
					progressPanel.setVisible(true);
					
					break;
				case 1:
					setWarning("Only a-zA-Z0-9 characters allowed for Login.");
					break;
				case 2:
					setWarning("Incorrect email format.");
					break;
				}
			}

		});
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
	public JLabel getLblWarning() {
		return lblWarning;
	}
	public void setLblWarning(JLabel lblWarning) {
		this.lblWarning = lblWarning;
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
	
	// desactive/active tout les champs
	private void setLock(boolean locked) {
		boolean enabled = !locked;
		getTxtLogin().setEnabled(enabled);
		getTxtFirstname().setEnabled(enabled);
		getTxtLastName().setEnabled(enabled);
		getTxtMail().setEnabled(enabled);
		getComboBox().setEnabled(enabled);
		getBtnCreate().setEnabled(enabled);
	}
	
	
	
	
	
	// verifie que les champs ont ete bien remplit et formatté
	// et renvoi le status (0 = bien formatte, 1 = login malformatte, 2 = mail mal formatte)
	private int checkField() {
		// On check le login
		if(!getTxtLogin().getText().matches("^[a-zA-Z0-9]+$"))
			return 1;
		// On check le mail
		Pattern rfc2822 = Pattern.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
		if(!rfc2822.matcher(getTxtMail().getText()).matches()) {
		    return 2;
		}
		
		return 0;
	}
	
	// affiche un warning dans lblWarning
	private void setWarning(String txt){
		getLblWarning().setVisible(true);
		getLblWarning().setText("<html><font color=\"red\">"+txt+"</font></html>");
	}
}
