package display.containers;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import model.Project;

import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;

import es.vocali.util.AESCrypt;

/**
 * Permet de selectionner le project pour lequel il faut afficher les stats
 * @author Mobilette
 *
 */
public class ProjectStatsSelecter extends PopupPanel {

	protected static String HEADERTXT = "<b>Project statistics</b><br /> Select a project.";

	
	private JComboBox comboBoxProject;
	private JButton btnOk;
	private JButton btnClose;

	
	/**
	 * Constructeur 
	 * le mode definit le type du panel 
	 * mode = 0 => creation de liens
	 * mode = 1 => suppression de liens
	 * @param mode
	 */
	public ProjectStatsSelecter(){
		super();
		descriptLabel = new JLabel("<html>"+ProjectStatsSelecter.HEADERTXT +"</html>");
		mainPanel.add(descriptLabel, "flowy,cell 0 0,grow");
		lblWarning = new JLabel("");
		lblWarning.setVisible(false);
		mainPanel.add(lblWarning, "cell 0 0,aligny center");
		String[] list = new String[UserProfile.CURRENT_USER.getProjects().size()];
		int i = 0;
		for(Project proj:UserProfile.CURRENT_USER.getProjects()){
			list[i++] = proj.getNom();
		}
		
		progressPanel = new ProgressPanel();
		progressPanel.setVisible(false);
		add(progressPanel, "cell 0 5 2 1,growx,aligny baseline");
		
		
		
		// 
		comboBoxProject = new JComboBox();
		comboBoxProject.setModel(new DefaultComboBoxModel(list));
		add(comboBoxProject, "cell 0 3,growx");
		

		btnOk = new JButton("Ok");
		add(btnOk, "flowx,cell 0 4,alignx center");
		
		btnClose = new JButton("Close");
		add(btnClose, "cell 0 4,alignx center");
		btnClose.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				getPopupWindow().hide();
			}
		});
		
		btnOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(true);
				Thread tr = new Thread(new Runnable() {
						
					@Override
					public void run() {
						try {
							getPopupWindow().hide();
							WindowManager.MAINWINDOW.addStatisticPane((String)comboBoxProject.getSelectedItem());
							WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
						} catch (final Exception e) {
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									JOptionPane.showMessageDialog(WindowManager.MAINWINDOW,
										    "Exception : "+e.toString(),
										    "Openning error",
										    JOptionPane.ERROR_MESSAGE);
								}
							});
							WindowManager.mwLogger.log(Level.SEVERE, "addStatisticPane Error",e);
							WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
						}
					}
				});
				tr.start();
			}
		});

		progressPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
	}
}
