package display;

import javax.swing.JFrame;
import java.awt.GridLayout;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

import settings.WindowManager;


import dao.MySQLUserDAO;
import dao.UserDAO;
import display.containers.ProgressPanel;


import model.User;
import net.miginfocom.swing.MigLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;



public class Authentificator extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static int WIDTH = 252;
	private static int HEIGHT = 220;
	
	private JTextField txtUsername;
	private JPasswordField passwordField;
	private JLabel lblPassword;
	private JLabel lblUsername;
	private JLabel lblDisclaimer;
	private JButton btnLogin;
	private JButton btnCancel;
	private ProgressPanel progressPanel;
	private boolean isActive;
	
	public Authentificator() {
		
		// quelques variables
		WindowManager.AUTHENTIFICATOR = this;
		isActive = true;
		
		// Display params
		
		
		setType(Type.POPUP);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setTitle("Authentificator");
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(new MigLayout("", "[48px,grow][68.00px][85.00px]", "[20px][14px][20px][19.00px][][][][][7.00]"));
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, "cell 0 0 1 8,grow");
		
		lblUsername = new JLabel("Username");
		panel.add(lblUsername, "cell 1 3,alignx left,aligny center");
		
		txtUsername = new JTextField();
		panel.add(txtUsername, "cell 1 4 2 1,growx,aligny center");
		txtUsername.setColumns(10);
		
		lblPassword = new JLabel("Password");
		panel.add(lblPassword, "cell 1 5,alignx left,aligny center");
		
		passwordField = new JPasswordField();
		
		panel.add(passwordField, "cell 1 6 2 1,growx,aligny center");
		
		btnLogin = new JButton("Login");

		panel.add(btnLogin, "cell 1 7,growx,aligny center");
		
		btnCancel = new JButton("Cancel");
		panel.add(btnCancel, "cell 2 7,growx,aligny center");
		
		JPanel panel_disclaimer = new JPanel();
		panel.add(panel_disclaimer, "cell 1 0 2 3,grow");
		GridBagLayout gbl_panel_disclaimer = new GridBagLayout();
		gbl_panel_disclaimer.columnWidths = new int[]{53, 105, 0};
		gbl_panel_disclaimer.rowHeights = new int[]{55, 0};
		gbl_panel_disclaimer.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_disclaimer.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_disclaimer.setLayout(gbl_panel_disclaimer);
		
		lblDisclaimer = new JLabel("<html><b>Disclaimer</b><br>Please login to access<br> to the software</html> ");
		GridBagConstraints gbc_lblDisclaimer = new GridBagConstraints();
		gbc_lblDisclaimer.fill = GridBagConstraints.BOTH;
		gbc_lblDisclaimer.gridwidth = 2;
		gbc_lblDisclaimer.insets = new Insets(0, 0, 0, 5);
		gbc_lblDisclaimer.gridx = 0;
		gbc_lblDisclaimer.gridy = 0;
		panel_disclaimer.add(lblDisclaimer, gbc_lblDisclaimer);
		
		progressPanel = new ProgressPanel();
		progressPanel.setVisible(false);
		panel.add(progressPanel, "cell 0 8 3 1,grow");
		
		// ---- Params JFrame ----
		setLocationRelativeTo(null);
		setSize(WIDTH,HEIGHT);
		setResizable(false);
		
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		// ====================== Event ==============================
		// on appui sur Entrée quand on est sur le pass -> tente le login
		passwordField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				if(key.getKeyCode() == KeyEvent.VK_ENTER){
					login();
				}
			}
		});
		
		// on se log (grise les 2 boutons login/cancel)
		btnLogin.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
			  login();
		  }
		});
		
		// on annule la tentative de connexion ou on quitte
		btnCancel.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
			  if(!isActive) 
				  setActive(true);
			  else
				  System.exit(0);
		  }
		});
	}

	public JTextField getTxtUsername() {
		return txtUsername;
	}

	public void setTxtUsername(JTextField txtUsername) {
		this.txtUsername = txtUsername;
	}

	public JPasswordField getPasswordField() {
		return passwordField;
	}

	public void setPasswordField(JPasswordField passwordField) {
		this.passwordField = passwordField;
	}

	public JLabel getLblPassword() {
		return lblPassword;
	}

	public void setLblPassword(JLabel lblPassword) {
		this.lblPassword = lblPassword;
	}

	public JLabel getLblUsername() {
		return lblUsername;
	}

	public void setLblUsername(JLabel lblUsername) {
		this.lblUsername = lblUsername;
	}

	public JLabel getLblDisclaimer() {
		return lblDisclaimer;
	}

	public void setLblDisclaimer(JLabel lblDisclaimer) {
		this.lblDisclaimer = lblDisclaimer;
	}

	public JButton getBtnLogin() {
		return btnLogin;
	}

	public void setBtnLogin(JButton btnLogin) {
		this.btnLogin = btnLogin;
	}

	public JButton getBtnCancel() {
		return btnCancel;
	}

	public void setBtnCancel(JButton btnCancel) {
		this.btnCancel = btnCancel;
	}

	public ProgressPanel getProgressPanel() {
		return progressPanel;
	}

	public void setProgressPanel(ProgressPanel progressPanel) {
		this.progressPanel = progressPanel;
	}

	private void setActive(boolean status) {
		txtUsername.setEnabled(status);
		passwordField.setEnabled(status);
		btnLogin.setEnabled(status);
		//btnCancel.setEnabled(status);
		progressPanel.setVisible(!status);
		isActive = status;
	}
	
	
	public void login(){
		setActive(false);
		Thread t = new Thread() {
	        public void run() {
				UserDAO udao = new MySQLUserDAO();
				try {
					User u = udao.connexion(txtUsername.getText(), passwordField.getText());
					if(u!=null){
						if(u.firstConnect()==1){
							changePassword(u);
						}
						dispose();
						//System.exit(0);
					}else{
						setActive(true);
					}
						
				} catch (Exception e) {
					e.printStackTrace();
					setActive(true);
				}
	        }
		};
		t.start();
	}
	
	// permet de demander a l'utilisateur de changer son mot de passe
	//
	private void changePassword(User u) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose(){
		txtUsername.setText("");
		passwordField.setText("");
		setActive(true);
		super.dispose();
	}
}
