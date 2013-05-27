package display.containers;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.border.TitledBorder;

import model.Project;
import model.User;
import model.User.Acclvl;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;

import settings.WindowManager;

import dao.DataBaseAdminDAO;
import dao.MySQLDataBaseAdminDAO;
import dao.MySQLProjectDAO;
import dao.MySQLUserDAO;
import dao.MySQLUserProjectDAO;
import dao.MySQLUserViewDAO;
import dao.ProjectDAO;
import dao.UserDAO;
import dao.UserProjectDAO;
import dao.UserViewDAO;

/**
 * Fait le lien entre un utilisateur et un projet
 * permet aussi de supprimer ce lien
 * @author DEVERDUN Jeremy
 *
 */
public class LinkProjectPanel extends PopupPanel {

	protected static String HEADERTXT = "<b>Link project</b><br /> Select project to link to the specified user.";
	private int mode;
	
	private JComboBox comboBoxUser;
	private JComboBox comboBoxProject;
	private JButton btnLink;
	private JButton btnClose;
	private JLabel lblUser;
	private JLabel lblProject;
	private JButton btnUnlink;
	
	
	/**
	 * Constructeur 
	 * le mode definit le type du panel 
	 * mode = 0 => creation de liens
	 * mode = 1 => suppression de liens
	 * @param mode
	 */
	public LinkProjectPanel(int mode){
		super();
		if(mode!=0 && mode!=1){
			System.err.println("Incorrect mode for LinkProjectPanel.");
			return;
		}
		this.mode = mode;
		descriptLabel = new JLabel("<html>"+LinkProjectPanel.HEADERTXT +"</html>");
		mainPanel.add(descriptLabel, "flowy,cell 0 0,grow");
		lblWarning = new JLabel("");
		lblWarning.setVisible(false);
		mainPanel.add(lblWarning, "cell 0 0,aligny center");
		
		lblUser = new JLabel("User");
		add(lblUser, "cell 0 1,alignx center");
		
		lblProject = new JLabel("Project");
		add(lblProject, "cell 1 1,alignx center");
		
		
		// 
		comboBoxUser = new JComboBox();
		comboBoxUser.setModel(new DefaultComboBoxModel(getUserList()));
		add(comboBoxUser, "cell 0 2 1 1,growx");
		
		comboBoxProject = new JComboBox();
		
		// On remplit la comboxbox des projets
		if(getMode() == 0)
			comboBoxProject.setModel(new DefaultComboBoxModel(getProjectList()));
		else
			comboBoxProject.setModel(new DefaultComboBoxModel(getProjectsForSelectedUser()));
		
		add(comboBoxProject, "cell 1 2 1 1,growx");
		
		btnLink = new JButton("Link");
		add(btnLink, "flowx,cell 0 3,alignx center");
		
		btnClose = new JButton("Close");
		add(btnClose, "cell 1 3,alignx center");
		
		progressPanel = new ProgressPanel();
		progressPanel.setVisible(false);
		add(progressPanel, "cell 0 5 2 1,growx,aligny baseline");
		
		btnUnlink = new JButton("Unlink");
		if(getMode() == 0)
			btnUnlink.setVisible(false);
		else
			btnLink.setVisible(false);
		add(btnUnlink, "cell 0 3");
		progressPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		// Event
		btnLink.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getLblWarning().setText("");
				getLblWarning().setVisible(false);
				setLock(true);
				progressPanel.setVisible(true);
				String login = (String) getComboBoxUser().getSelectedItem();
				String projectname = (String) getComboBoxProject().getSelectedItem();
				UserProjectDAO updao = new MySQLUserProjectDAO();
	
				// On insere le tuple
				try {
					int returnstatus = updao.addLink(login, projectname);
					switch(returnstatus){
					case 0:
						getPopupWindow().hide();
						WindowManager.MAINWINDOW.getBtnRefresh().doClick();break;
					case 1:
						setWarning("Link already exists");
						getLblWarning().setVisible(true);
						progressPanel.setVisible(false);
						setLock(false);break;
					}
					
				} catch (SQLException e1) {
					setWarning("SQL Error");
					WindowManager.mwLogger.log(Level.WARNING, "btnLink SQL error.",e1);
					getLblWarning().setVisible(true);
					progressPanel.setVisible(false);
					setLock(false);
					
				}
			}
		});
		
		btnUnlink.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getLblWarning().setText("");
				getLblWarning().setVisible(false);
				setLock(true);
				progressPanel.setVisible(true);
				String login = (String) getComboBoxUser().getSelectedItem();
				String projectname = (String) getComboBoxProject().getSelectedItem();
				UserProjectDAO updao = new MySQLUserProjectDAO();
	
				// On insere le tuple
				try {
					int returnstatus = updao.removeLink(login, projectname);
					switch(returnstatus){
					case 0:
						getPopupWindow().hide();
						WindowManager.MAINWINDOW.getBtnRefresh().doClick();break;
					case 1:
						setWarning("Link didn't exist");
						getLblWarning().setVisible(true);
						progressPanel.setVisible(false);
						setLock(false);break;
					}
					
				} catch (SQLException e1) {
					setWarning("SQL Error");
					WindowManager.mwLogger.log(Level.WARNING, "btnUnlink SQL error.",e1);
					getLblWarning().setVisible(true);
					progressPanel.setVisible(false);
					setLock(false);
					
				}
			}
		});

		if(getMode() == 1){
			// On ajoute un listener pour mettre à jours la combobox projet
			// quand on selectionne un autre utilisateur
			comboBoxUser.addActionListener (new ActionListener () {
			    public void actionPerformed(ActionEvent e) {
			        // met à jours la liste des projet pour cet utilisateur
			    	comboBoxProject.removeAllItems();
			        for(String s:getProjectsForSelectedUser()){
			        	comboBoxProject.addItem(s);
			        }
			    }
			});
		}
		
		btnClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPopupWindow().hide();
				
			}
		});

	}
	

	public JComboBox getComboBoxUser() {
		return comboBoxUser;
	}

	public void setComboBoxUser(JComboBox comboBox) {
		this.comboBoxUser = comboBox;
	}

	public JComboBox getComboBoxProject() {
		return comboBoxProject;
	}

	public void setComboBoxProject(JComboBox comboBoxProject) {
		this.comboBoxProject = comboBoxProject;
	}

	public JButton getBtnLink() {
		return btnLink;
	}

	public void setBtnLink(JButton btnRemove) {
		this.btnLink = btnRemove;
	}

	public int getMode() {
		return mode;
	}



	public void setMode(int mode) {
		this.mode = mode;
	}



	public JLabel getLblUser() {
		return lblUser;
	}



	public void setLblUser(JLabel lblUser) {
		this.lblUser = lblUser;
	}



	public JLabel getLblProject() {
		return lblProject;
	}



	public void setLblProject(JLabel lblProject) {
		this.lblProject = lblProject;
	}



	public JButton getBtnUnlink() {
		return btnUnlink;
	}



	public void setBtnUnlink(JButton btnUnlink) {
		this.btnUnlink = btnUnlink;
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
			WindowManager.mwLogger.log(Level.WARNING, "getUserList SQL error.",e);
			return null;
		}
		
	}
	
	private String[] getProjectList() {
		ProjectDAO pdao = new MySQLProjectDAO();
		Collection<Project> projects;
		try {
			projects = pdao.retrieveAll();
			String[] projects_names = new String[projects.size()];
			Project[] uArray = (Project[]) projects.toArray(new Project[projects.size()]);
			for(int i = 0; i<projects.size();i++)
				projects_names[i] = uArray[i].getNom();
			return projects_names;
		} catch (SQLException e) {
			setWarning("SQL Error");
			getLblWarning().setVisible(true);
			WindowManager.mwLogger.log(Level.WARNING, "getProjectList SQL error.",e);
			return null;
		}
	}

	/**
	 * Recupere la liste des projets pour un utilisateur donnee
	 * @return
	 */
	private String[] getProjectsForSelectedUser() {
		UserProjectDAO updao = new MySQLUserProjectDAO();
		UserDAO udao = new MySQLUserDAO();
		try {
			User user = udao.retrieveUser((String) getComboBoxUser().getSelectedItem());
			Set<Project> projects = updao.getProjectsForUser(user.getId());
			Project[] pArray = (Project[]) projects.toArray(new Project[projects.size()]);
			String[] projects_names = new String[projects.size()];
			for(int i = 0; i<projects.size();i++)
				projects_names[i] = pArray[i].getNom();
			return projects_names;
		} catch (SQLException e) {
			setWarning("SQL Error");
			getLblWarning().setVisible(true);
			WindowManager.mwLogger.log(Level.WARNING, "getProjectsForSelectedUser SQL error.",e);
		}
		return null;
	}

	private void setLock(boolean b){
		getComboBoxUser().setEnabled(!b);
		getComboBoxProject().setEnabled(!b);
		getBtnLink().setEnabled(!b);
		btnUnlink.setEnabled(!b);
		
	}


}
