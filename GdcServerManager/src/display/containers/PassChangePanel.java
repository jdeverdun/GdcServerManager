package display.containers;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import model.User;
import model.User.Acclvl;
import net.miginfocom.swing.MigLayout;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;

import javax.swing.JPasswordField;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import settings.UserProfile;
import settings.WindowManager;

import dao.DataBaseAdminDAO;
import dao.MySQLDataBaseAdminDAO;
import dao.MySQLUserDAO;
import dao.UserDAO;
import display.Authentificator;
import display.containers.viewer.ViewerPanel;

public class PassChangePanel extends PopupPanel {
	private static String HEADERTXT = "<b>Password update</b><br /> Your password is unsafe, please change it.";
	private JLabel lblPassTwo;
	private JButton btnOk;
	private JButton btnCancel;
	private JPasswordField passwordField1;
	private JPasswordField passwordField2;
	
	public PassChangePanel(){
		super();
		descriptLabel = new JLabel("<html>"+PassChangePanel.HEADERTXT +"</html>");
		getMainPanel().add(descriptLabel, "flowy,cell 0 0,grow");
		
		lblWarning = new JLabel("");
		lblWarning.setVisible(false);
		mainPanel.add(lblWarning, "cell 0 0,aligny center");
		
		JLabel lblPassfirst = new JLabel("New Password");
		lblPassfirst.setFont(new Font("Tahoma", Font.PLAIN, 11));
		add(lblPassfirst, "cell 0 1,alignx center,aligny center");
		
		passwordField1 = new JPasswordField();
		add(passwordField1, "cell 1 1,growx");
		
		lblPassTwo = new JLabel("Again");
		add(lblPassTwo, "cell 0 2,alignx center,aligny center");
		
		passwordField2 = new JPasswordField();
		add(passwordField2, "cell 1 2,growx");
		
		btnOk = new JButton("Ok");
		add(btnOk, "cell 0 3,alignx center,aligny center");
		
		btnCancel = new JButton("Cancel");
		add(btnCancel, "cell 1 3,alignx center,aligny center");
		
		progressPanel = new ProgressPanel();
		progressPanel.setVisible(false);
		add(progressPanel, "cell 0 4 2 1,growx,aligny baseline");
		progressPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		
		// Event
		btnOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(passwordFieldIsOk()){
					// On lock l'interface
					setLock(true);
					progressPanel.setVisible(true);
					
					// On recupere le mot de passe
					String newPass = new String(getPasswordField1().getPassword());
					
					// On met à jours l'utilisateur local
					User ulocal = UserProfile.CURRENT_USER;
					UserDAO udao = new MySQLUserDAO();
					ulocal.setFirstConnect(0);
					
					try {
						ulocal.setPassword(udao.encryptPass(newPass));
					} catch (SQLException e2) {
						setWarning("Error with SQL connection.");
						WindowManager.mwLogger.log(Level.SEVERE, "btnOk SQL error.",e2);
						setLock(false);
						progressPanel.setVisible(false);
						return;
					}
					
					// On met à jours l'utilisateur sur la db
					DataBaseAdminDAO ddao = new MySQLDataBaseAdminDAO();
					try {
						udao.updateUser(ulocal);
						ddao.setPasswordForCurrentUser(ulocal.getPassword());
						UserProfile.CURRENT_USER = ulocal;
						getPopupWindow().hide();
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
								if(WindowManager.MAINWINDOW!=null)
									WindowManager.MAINWINDOW.dispose();
								if(WindowManager.AUTHENTIFICATOR!=null && WindowManager.AUTHENTIFICATOR.isDisplayable())
									WindowManager.AUTHENTIFICATOR.dispose();
								JFrame.setDefaultLookAndFeelDecorated(true);
								try {
							          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
							        } catch (Exception e) {
							          System.out.println("Substance Graphite failed to initialize");
							        }
								Authentificator auth = new Authentificator();
								UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
								auth.setVisible(true);
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										JDialog.setDefaultLookAndFeelDecorated(true);
										JOptionPane.showMessageDialog(WindowManager.AUTHENTIFICATOR,
												"Password changed, please log in again.",
											    "Success",
											    JOptionPane.INFORMATION_MESSAGE);
									}
								});
							}
						});
					} catch (SQLException e1) {
						setWarning("SQL Error");
						WindowManager.mwLogger.log(Level.SEVERE, "btnOk SQL error.",e1);
						setLock(false);
						progressPanel.setVisible(false);
					}
				}else{
					setWarning("Password incorrect");
					setLock(false);
					progressPanel.setVisible(false);
				}
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPopupWindow().hide();
			}
		});
	}
	
	
	protected void setLock(boolean b) {
		getPasswordField1().setEnabled(!b);
		getPasswordField2().setEnabled(!b);
		getBtnOk().setEnabled(!b);
	}


	public Popup getPopup() {
		return popup;
	}
	public void setPopup(Popup popup) {
		this.popup = popup;
	}
	
	public JLabel getLblPassTwo() {
		return lblPassTwo;
	}


	public void setLblPassTwo(JLabel lblPassTwo) {
		this.lblPassTwo = lblPassTwo;
	}


	public JButton getBtnOk() {
		return btnOk;
	}


	public void setBtnOk(JButton btnOk) {
		this.btnOk = btnOk;
	}


	public JButton getBtnCancel() {
		return btnCancel;
	}


	public void setBtnCancel(JButton btnCancel) {
		this.btnCancel = btnCancel;
	}


	public JPasswordField getPasswordField1() {
		return passwordField1;
	}


	public void setPasswordField1(JPasswordField passwordField1) {
		this.passwordField1 = passwordField1;
	}


	public JPasswordField getPasswordField2() {
		return passwordField2;
	}


	public void setPasswordField2(JPasswordField passwordField2) {
		this.passwordField2 = passwordField2;
	}


	// Methodes
	/**
	 * Test si les 2 champs mot de passe sont identiques
	 * @return
	 */
	protected boolean passwordFieldIsOk() {
		return Arrays.equals(getPasswordField1().getPassword(),getPasswordField2().getPassword());
	}
	
}
