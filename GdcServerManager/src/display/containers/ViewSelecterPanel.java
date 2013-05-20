package display.containers;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import settings.SystemSettings;
import settings.WindowManager;

import es.vocali.util.AESCrypt;

/**
 * Permet de selectionner le nifti a afficher depuis le request panel
 * @author Mobilette
 *
 */
public class ViewSelecterPanel extends PopupPanel {

	protected static String HEADERTXT = "<b>View nifti</b><br /> Select a nifti to view.";

	private File[] niftis;
	
	private JComboBox comboBoxNifti;
	private JButton btnView;
	private JButton btnClose;

	
	/**
	 * Constructeur 
	 * le mode definit le type du panel 
	 * mode = 0 => creation de liens
	 * mode = 1 => suppression de liens
	 * @param mode
	 */
	public ViewSelecterPanel(File[] niftis){
		super();
		if(niftis==null){
			getPopupWindow().hide();
			return;
		}
		this.niftis = niftis;
		descriptLabel = new JLabel("<html>"+ViewSelecterPanel.HEADERTXT +"</html>");
		mainPanel.add(descriptLabel, "flowy,cell 0 0,grow");
		lblWarning = new JLabel("");
		lblWarning.setVisible(false);
		mainPanel.add(lblWarning, "cell 0 0,aligny center");
		String[] list = new String[this.niftis.length];
		for(int i = 0 ; i < this.niftis.length ; i++){
			list[i] = this.niftis[i].getName().substring(0,this.niftis[i].getName().length()-AESCrypt.ENCRYPTSUFFIX.length());
		}
		
		progressPanel = new ProgressPanel();
		progressPanel.setVisible(false);
		add(progressPanel, "cell 0 5 2 1,growx,aligny baseline");
		
		
		
		// 
		comboBoxNifti = new JComboBox();
		comboBoxNifti.setModel(new DefaultComboBoxModel(list));
		add(comboBoxNifti, "cell 0 3,growx");
		

		btnView = new JButton("View");
		add(btnView, "flowx,cell 0 4,alignx center");
		
		btnClose = new JButton("Close");
		add(btnClose, "cell 0 4,alignx center");
		btnClose.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				getPopupWindow().hide();
			}
		});
		
		btnView.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(true);
				Thread tr = new Thread(new Runnable() {
						
					@Override
					public void run() {
						try {
							getPopupWindow().hide();
							WindowManager.MAINWINDOW.getOngletPane().setSelectedComponent(WindowManager.MAINWINDOW.getViewerPanel());
							WindowManager.MAINWINDOW.getViewerPanel().reset();
							FileManager.copyAndDecrypt(getNiftis()[comboBoxNifti.getSelectedIndex()], SystemSettings.SERVER_INFO.getTempDir().toFile());
							while(!SystemSettings.DECRYPT_DAEMON.isWaiting()){
								try {
									Thread.sleep(200);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							File copiedFile = new File(SystemSettings.SERVER_INFO.getTempDir()+File.separator+getNiftis()[comboBoxNifti.getSelectedIndex()].getName().substring(0, getNiftis()[comboBoxNifti.getSelectedIndex()].getName().length()-4));
							WindowManager.MAINWINDOW.getViewerPanel().open(copiedFile.toPath());
							copiedFile.deleteOnExit();
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
							WindowManager.MAINWINDOW.getProgressBarPanel().setVisible(false);
						}
					}
				});
				tr.start();
			}
		});

		progressPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
	}


	public File[] getNiftis() {
		return niftis;
	}


	public void setNiftis(File[] niftis) {
		this.niftis = niftis;
	}
	
	/**
	 * On verifie qu'il n'y a qu'une image a afficher et si c'est le cas on ouvre directement cette image
	 */
	public void checkToClose(){
		// on ouvre directement le nifti si il n'y en a qu'une
		if(this.niftis.length==1)
			btnView.doClick();
	}
}
