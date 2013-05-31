package display.containers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Popup;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JButton;

import display.ImportFrame;

import settings.WindowManager;

/**
 * Panel qui affiche un bouton cancel et une progressbar
 * fonctionne avec requestpanel et filetree
 * les fonctions appelee dependent du fait que filetree soit null
 * ou requestpanel null
 * @author Mobilette
 *
 */
public class WaitingBarPanel extends JPanel{

	private JLabel lblTitle;
	private ProgressPanel progressPanel;
	private JButton btnCancel;
	private Popup popup;
	private Thread runningThread;
	private FileManager filetree; // celui qui tourne dans le thread
	private RequestPanel rpanel;
	private ImportFrame importFrame; 
	
	public WaitingBarPanel(FileManager fileManager){
		this();
		setFiletree(fileManager);
		this.importFrame = null;
		rpanel = null;
	}
	public WaitingBarPanel(RequestPanel rq){
		this();
		filetree=null;
		rpanel = rq;
		this.importFrame = null;
	}
	public WaitingBarPanel(ImportFrame importFrame){
		this();
		filetree=null;
		rpanel = null;
		this.importFrame = importFrame;
	}
	public WaitingBarPanel(){
		
		setLayout(new MigLayout("", "[grow,center][]", "[][23.00,fill][]"));
		
		lblTitle = new JLabel("Waiting");
		add(lblTitle, "cell 0 0");
		
		progressPanel = new ProgressPanel();
		add(progressPanel, "cell 0 1,growx");
		
		btnCancel = new JButton("Cancel");
		add(btnCancel, "cell 0 2,alignx center");
		progressPanel.setVisible(true);

		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPopup().hide();
				if(getFiletree()!=null){
					// si on est sur un job d'un filetree
					getFiletree().terminateAction();
					WindowManager.MAINWINDOW.setLock(false);
				}
				if(rpanel!=null){
					// si on est sur un import depuis la bdd
					rpanel.terminateAction();
				}
				if(importFrame!=null){
					importFrame.stopImport();
				}
			}
		});
	}
	
	public JLabel getLblTitle() {
		return lblTitle;
	}

	public void setLblTitle(JLabel lblTitle) {
		this.lblTitle = lblTitle;
	}

	public void setTitle(String txt){
		txt = "<html>                 "+txt+"                 </html>";
		getLblTitle().setText(txt);
	}

	public Popup getPopup() {
		return popup;
	}

	public void setPopup(Popup popup) {
		this.popup = popup;
	}

	public Thread getRunningThread() {
		return runningThread;
	}

	public void setRunningThread(Thread runningThread) {
		this.runningThread = runningThread;
	}

	public FileManager getFiletree() {
		return filetree;
	}

	public void setFiletree(FileManager filetree) {
		this.filetree = filetree;
	}

}
