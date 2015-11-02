package display.containers;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.border.TitledBorder;

import model.Patient;
import model.Project;
import model.ServerInfo;
import model.User;
import model.User.Acclvl;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;

import settings.SystemSettings;
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
import dao.project.AcquisitionDateDAO;
import dao.project.MySQLAcquisitionDateDAO;
import dao.project.MySQLPatientDAO;
import dao.project.MySQLProtocolDAO;
import dao.project.MySQLSerieDAO;
import dao.project.PatientDAO;
import dao.project.ProtocolDAO;
import dao.project.SerieDAO;
import es.vocali.util.AESCrypt;

/**
 * Fait le lien entre un utilisateur et un projet
 * permet aussi de supprimer ce lien
 * @author DEVERDUN Jeremy
 *
 */
public class ReassignProjectPanel extends PopupPanel {

	protected static String HEADERTXT = "<b>Reassign project</b><br /> Move a patient to another project.";
	private JComboBox comboBoxProject;
	private JButton btnReassign;
	private JButton btnClose;
	private JLabel lblProject;
	private Path patient; // chemin vers le patient a deplacer
	
	/**
	 * Constructeur 
	 * @param mode
	 */
	public ReassignProjectPanel(Path pat){
		super();
		this.patient = pat;
		descriptLabel = new JLabel("<html>"+ReassignProjectPanel.HEADERTXT +"<br />["+patient.toString()+"]</html>");
		mainPanel.add(descriptLabel, "flowy,cell 0 0,grow");
		lblWarning = new JLabel("");
		lblWarning.setVisible(false);
		mainPanel.add(lblWarning, "cell 0 0,aligny center");
		
		lblProject = new JLabel("New Project");
		add(lblProject, "cell 0 1,alignx center");
		comboBoxProject = new JComboBox();
		// On remplit la comboxbox des projets
		comboBoxProject.setModel(new DefaultComboBoxModel(getProjectList()));
		
		
		
		add(comboBoxProject, "cell 1 1,growx");
		
		progressPanel = new ProgressPanel();
		progressPanel.setVisible(false);
		add(progressPanel, "cell 0 5 2 1,growx,aligny baseline");
		
		btnReassign = new JButton("Reassign");
		add(btnReassign, "cell 0 2,alignx center");
		
		// Event
		btnReassign.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getLblWarning().setText("");
				getLblWarning().setVisible(false);
				setLock(true);
				progressPanel.setVisible(true);
				Thread tr = new Thread(new Runnable() {
					
					@Override
					public void run() {
						String projectname = (String) getComboBoxProject().getSelectedItem();
						ProjectDAO updao = new MySQLProjectDAO();
						PatientDAO pdao = new MySQLPatientDAO();
						String[] parts = patient.toString().split(Pattern.quote(File.separator));
						int serverdirlen = (SystemSettings.SERVER_INFO.getServerDir().toString().split(Pattern.quote(File.separator))).length +1;// +1 pour NRI-ANALYSE et NRI-DICOM
						if(parts.length==(serverdirlen)) 
							return;
						if(!patient.getFileName().toString().contains("..")){
							int count = 0;
							for(int i = serverdirlen;i <parts.length;i++){
								if(!parts[i].isEmpty()){
									count++;
								}else{
									setWarning("Not a patient directory");
									WindowManager.mwLogger.log(Level.WARNING, "Not a patient directory");
									getLblWarning().setVisible(true);
									progressPanel.setVisible(false);
									setLock(false);
									return;
								}
							}
							String project = null;
							String pat = null;
							switch(count){
							case 1:// on delete un projet complet
								project = parts[serverdirlen];
								break;
							case 2: // delete d'un patient
								project = parts[serverdirlen];
								pat = parts[serverdirlen+1];
								break;
							default:
								setWarning("Not a patient directory");
								WindowManager.mwLogger.log(Level.WARNING, "Not a patient directory");
								getLblWarning().setVisible(true);
								progressPanel.setVisible(false);
								setLock(false);
								return;
							}
							
							// On insere le tuple
							try {
								Path npatPath =  Paths.get(patient.toString().replace(project, projectname)); // nouveau chemin vers le patient
								Path npatReciPath; // nouveau chemin vers le patient dans le repertoire associe (dicom -> analyse) 
								if(npatPath.getParent().toFile().canWrite()){
									Project lproj = updao.retrieveProject(project);// on recupere l'id du projet d'origine du patient
									Project lprojTo = updao.retrieveProject(projectname);// on recupere l'id du projet dans lequel on doit mettre le patient
									Patient lpat = pdao.retrievePatient(pat, lproj.getId());
									boolean changeOk = pdao.changeProject(lpat,lprojTo);
									if(!changeOk){
										setWarning("Patient already exists in new project.");
										WindowManager.mwLogger.log(Level.WARNING, "Patient already exists in new project.");
										getLblWarning().setVisible(true);
										progressPanel.setVisible(false);
										setLock(false);
										return;
									}
									
									if(patient.toString().contains(ServerInfo.NRI_DICOM_NAME)){
										Files.move(patient,npatPath);
										npatReciPath = Paths.get(npatPath.toString().replace(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME));
										if(new File(patient.toString().replace(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME)).exists())
											Files.move(Paths.get(patient.toString().replace(ServerInfo.NRI_DICOM_NAME, ServerInfo.NRI_ANALYSE_NAME)),npatReciPath);
									}else{
										Files.move(patient,npatPath);
										npatReciPath = Paths.get(npatPath.toString().replace(ServerInfo.NRI_ANALYSE_NAME,ServerInfo.NRI_DICOM_NAME));
										if(new File(patient.toString().replace(ServerInfo.NRI_ANALYSE_NAME, ServerInfo.NRI_DICOM_NAME)).exists())
											Files.move(Paths.get(patient.toString().replace(ServerInfo.NRI_ANALYSE_NAME, ServerInfo.NRI_DICOM_NAME)),npatReciPath);
									}
									getPopupWindow().hide();
									WindowManager.MAINWINDOW.getBtnRefresh().doClick();
								}else{
									setWarning("Can't write in this directory : "+npatPath.toString());
									WindowManager.mwLogger.log(Level.WARNING, "Can't write in this directory : "+npatPath.toString());
									getLblWarning().setVisible(true);
									progressPanel.setVisible(false);
									setLock(false);
								}
								
							} catch (SQLException e1) {
								setWarning("SQL Error");
								WindowManager.mwLogger.log(Level.WARNING, "btnReassign SQL error.",e1);
								getLblWarning().setVisible(true);
								progressPanel.setVisible(false);
								setLock(false);
								
							} catch (IOException e1) {
								WindowManager.mwLogger.log(Level.SEVERE, "btnReassign SQL error.",e1);
								getLblWarning().setVisible(true);
								progressPanel.setVisible(false);
								setLock(false);
							}
						}
					}
				});
				tr.start();
			}
		});
		
		btnClose = new JButton("Close");
		add(btnClose, "cell 1 2,alignx center");
		
				
				btnClose.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						getPopupWindow().hide();
						
					}
				});
		progressPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

	}
	

	
	public JComboBox getComboBoxProject() {
		return comboBoxProject;
	}

	public void setComboBoxProject(JComboBox comboBoxProject) {
		this.comboBoxProject = comboBoxProject;
	}

	public JButton getBtnReassign() {
		return btnReassign;
	}

	public void setBtnReassign(JButton btn) {
		this.btnReassign = btn;
	}

	public JLabel getLblProject() {
		return lblProject;
	}

	public void setLblProject(JLabel lblProject) {
		this.lblProject = lblProject;
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

	private void setLock(boolean b){
		getComboBoxProject().setEnabled(!b);
		getBtnReassign().setEnabled(!b);		
	}


}
