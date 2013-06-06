package display.containers;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.border.TitledBorder;

import model.User;
import model.User.Acclvl;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;

import settings.WindowManager;

import dao.DataBaseAdminDAO;
import dao.MySQLDataBaseAdminDAO;
import dao.MySQLUserDAO;
import dao.MySQLUserViewDAO;
import dao.UserDAO;
import dao.UserViewDAO;

public class DeleteUserPanel extends PopupPanel {

	protected static String HEADERTXT = "<b>Delete</b><br /> Select user to remove.";
	private JComboBox comboBox;
	private JButton btnRemove;
	private JButton btnClose;
	
	
	public DeleteUserPanel(){
		super();
		
		descriptLabel = new JLabel("<html>"+DeleteUserPanel.HEADERTXT +"</html>");
		mainPanel.add(descriptLabel, "flowy,cell 0 0,grow");
		lblWarning = new JLabel("");
		lblWarning.setVisible(false);
		mainPanel.add(lblWarning, "cell 0 0,aligny center");
		
		
		// 
		comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(getUserList()));
		add(comboBox, "cell 0 1 2 1,growx");
		
		btnRemove = new JButton("Remove");
		add(btnRemove, "cell 0 2,alignx center");
		
		btnClose = new JButton("Close");
		add(btnClose, "cell 1 2,alignx center");
		
		progressPanel = new ProgressPanel();
		progressPanel.setVisible(false);
		add(progressPanel, "cell 0 3 2 1,growx,aligny baseline");
		progressPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		// Event
		btnRemove.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getLblWarning().setText("");
				getLblWarning().setVisible(false);
				setLock(true);
				progressPanel.setVisible(true);
				String login = (String) getComboBox().getSelectedItem();
				DataBaseAdminDAO ddao = new MySQLDataBaseAdminDAO();
				UserDAO udao = new MySQLUserDAO();
				UserViewDAO uvdao = new MySQLUserViewDAO();
				
				// On supprime l'utilisateur
				User user;
				try {
					user = udao.retrieveUser(login);
					udao.removeUser(user);
					uvdao.removeUser(user);
					ddao.removeUser(user);
					setLock(false);
					progressPanel.setVisible(false);
					getPopupWindow().hide();
				} catch (SQLException e1) {
					setWarning("SQL Error");
					WindowManager.mwLogger.log(Level.SEVERE, "btnRemove SQL Error",e1);
					getLblWarning().setVisible(true);
					progressPanel.setVisible(false);
					setLock(false);
				}
			}
		});
		
		btnClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPopupWindow().hide();
				
			}
		});

	}
	
	public JComboBox getComboBox() {
		return comboBox;
	}

	public void setComboBox(JComboBox comboBox) {
		this.comboBox = comboBox;
	}

	public JButton getBtnRemove() {
		return btnRemove;
	}

	public void setBtnRemove(JButton btnRemove) {
		this.btnRemove = btnRemove;
	}

	public JButton getBtnClose() {
		return btnClose;
	}

	public void setBtnClose(JButton btnClose) {
		this.btnClose = btnClose;
	}

	private String[] getUserList() {
		UserDAO udao = new MySQLUserDAO();
		Collection<User> users;
		try {
			users = udao.retrieveAll();
			String[] logins = new String[users.size()];
			User[] uArray = (User[]) users.toArray(new User[users.size()]);
			for(int i = 0; i<users.size();i++)
				logins[i] = uArray[i].getLogin();
			return logins;
		} catch (SQLException e) {
			setWarning("SQL Error");
			getLblWarning().setVisible(true);
			WindowManager.mwLogger.log(Level.SEVERE, "getUserList SQL Error",e);
			return null;
		}
		
	}
	
	private void setLock(boolean b){
		getComboBox().setEnabled(!b);
		getBtnRemove().setEnabled(!b);
		
	}


}
