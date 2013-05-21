package display.containers;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import settings.SystemSettings;
import settings.UserProfile;
import settings.WindowManager;
import javax.swing.border.TitledBorder;
import javax.swing.SwingConstants;

import daemon.DecryptDaemon;
import daemon.DicomDaemon;
import daemon.DicomNode;
import daemon.NiftiDaemon;
import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.Icon;


/**
 * Classe permettant de visualiser l'etat des Thread
 * Decrypter, Encrypter, DicomWorker, Nifti ...
 * 
 * Permet de relancer des Thread, de les stopper etc
 * @author DEVERDUN Jeremy
 *
 */
public class ServerStatusPanel extends JPanel {

	// Attributs
	private static final int DEFAULT_REFRESH_TIME = 2000;
	private int refreshTime; // temps d'attente en ms entre chaque refresh
	
	private boolean dicomDaemonAlive;
	private boolean dicomDispatcherAlive;
	private boolean dicomNodeAlive;
	private boolean niftiDaemonAlive;
	private boolean encrypterAlive;
	private boolean decrypterAlive;
	private boolean missingDaemonAlive;
	
	// buttons
	private JButton btnDicomdaemonstatus;
	private JLabel btnDicomdispatcherstatus;
	private JButton btnDicomnodestatus;
	private JButton btnNiftidaemonstatus;
	private JLabel btnEncrypterdaemonstatus;
	private JButton btnDecrypterdaemonstatus;
	private JLabel lblCommentdicomdaemon;
	private JLabel lblCommentdicomdispatcher;
	private JLabel lblCommentdicomnode;
	private JLabel lblCommentniftidaemon;
	private JLabel lblCommentencrypter;
	private JLabel lblCommentdecrypter;
	private JLabel lblWarningdicomdaemon;
	private JLabel lblWarningdicomdispatcher;
	private JLabel lblWarningdicomnode;
	private JLabel lblWarningniftidaemon;
	private JLabel lblWarningencrypter;
	private JLabel lblWarningdecrypter;
	private JLabel lblService;
	private JLabel lblStatus;
	private JLabel lblComment;
	private JLabel lblWarning;
	private JSeparator jsStaCom;
	private JSeparator jsComWarn;
	
	// image
	private ImageIcon iconGreen;
	private ImageIcon iconRed;
	private ImageIcon iconYellow;
	private JLabel lblMissingDaemon;
	private JLabel labelMissingDaemonStatus;
	private JLabel labelCommentMissingDaemon;
	private JLabel labelWarningMissingDaemon;

	
	// Constructeur
	public ServerStatusPanel(){
		
		ImageIcon icon=new ImageIcon(ServerStatusPanel.class.getResource("/images/green_light.png"));
		Image img = icon.getImage();  
		Image newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		iconGreen = new ImageIcon(newimg); 
		
		icon=new ImageIcon(ServerStatusPanel.class.getResource("/images/red_light.png"));
		img = icon.getImage();  
		newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		iconRed = new ImageIcon(newimg); 
		
		icon=new ImageIcon(ServerStatusPanel.class.getResource("/images/yellow_light.png"));
		img = icon.getImage();  
		newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
		iconYellow = new ImageIcon(newimg); 
		
		// Les icones a utilise pour les bouton selon le status des thread
		ImageIcon iconDicomDaemon = iconRed;
		ImageIcon iconDicomDispatcher = iconRed;
		ImageIcon iconDicomNode = iconRed;
		ImageIcon iconNiftiDaemon = iconRed;
		ImageIcon iconDecrypter = iconRed;
		ImageIcon iconEncrypter = iconRed;
		ImageIcon iconMissingDaemon = iconRed;
		if(SystemSettings.DICOM_DAEMON!=null)
			dicomDaemonAlive = SystemSettings.DICOM_DAEMON.isAlive();
		else
			dicomDaemonAlive = false;
		if(dicomDaemonAlive)
			iconDicomDaemon = iconGreen;
		if(SystemSettings.DICOM_DISPATCHER!=null)
			dicomDispatcherAlive = SystemSettings.DICOM_DISPATCHER.isAlive();
		else
			dicomDispatcherAlive = false;
		if(dicomDispatcherAlive)
			iconDicomDispatcher = iconGreen;
		if(SystemSettings.DICOM_NODE!=null)
			dicomNodeAlive = SystemSettings.DICOM_NODE.isAlive();
		else
			dicomNodeAlive = false;
		if(dicomNodeAlive)
			iconDicomNode = iconGreen;
		if(SystemSettings.NIFTI_DAEMON!=null)
			niftiDaemonAlive = SystemSettings.NIFTI_DAEMON.isAlive();
		else
			niftiDaemonAlive = false;
		if(niftiDaemonAlive)
			iconNiftiDaemon = iconGreen;
		if(SystemSettings.ENCRYPT_DAEMON!=null)
			encrypterAlive = SystemSettings.ENCRYPT_DAEMON.isAlive();
		else
			encrypterAlive = false;
		if(encrypterAlive)
			iconEncrypter = iconGreen;
		if(SystemSettings.DECRYPT_DAEMON!=null)
			decrypterAlive = SystemSettings.DECRYPT_DAEMON.isAlive();
		else
			decrypterAlive = false;
		if(decrypterAlive)
			iconDecrypter = iconGreen;
		if(SystemSettings.MISSING_DAEMON!=null)
			missingDaemonAlive = SystemSettings.MISSING_DAEMON.isAlive();
		else
			missingDaemonAlive = false;
		if(missingDaemonAlive)
			iconMissingDaemon = iconGreen;
		setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setRefreshTime(DEFAULT_REFRESH_TIME);
		setLayout(new MigLayout("", "[96.00][][][][grow][][grow]", "[][][][grow][][grow][][grow][][grow][][grow][][grow][][grow]"));
		
		lblService = new JLabel("Service");
		add(lblService, "cell 0 0,alignx center");
		
		lblStatus = new JLabel("Status");
		add(lblStatus, "cell 2 0,alignx center");
		
		lblComment = new JLabel("Comment");
		add(lblComment, "cell 4 0,alignx center");
		
		lblWarning = new JLabel("Warning");
		lblWarning.setForeground(Color.RED);
		add(lblWarning, "cell 6 0,alignx center");
		
		JSeparator js = new JSeparator(JSeparator.HORIZONTAL);
		add(js, "cell 0 1 7 1,growx");
		

		
		JLabel lblDecrypter = new JLabel("Decrypter");
		lblDecrypter.setToolTipText("Decrypt files");
		add(lblDecrypter, "cell 0 2,growx");
		JSeparator jsVert = new JSeparator(JSeparator.VERTICAL);
		add(jsVert, "cell 1 2 1 14,growy");
		
		btnDecrypterdaemonstatus = new JButton(iconDecrypter);
		add(btnDecrypterdaemonstatus, "cell 2 2,alignx left,growy");
		
		jsStaCom = new JSeparator(SwingConstants.VERTICAL);
		add(jsStaCom, "cell 3 2 1 14,growy");
		
		lblCommentdecrypter = new JLabel("");
		add(lblCommentdecrypter, "cell 4 2,growx");
		
		jsComWarn = new JSeparator(SwingConstants.VERTICAL);
		add(jsComWarn, "cell 5 2 1 14,growy");
		
		lblWarningdecrypter = new JLabel("");
		lblWarningdecrypter.setForeground(Color.RED);
		add(lblWarningdecrypter, "cell 6 2,growx");
		
		JLabel lblDicomDispatcher = new JLabel("Dicom Dispatcher");
		lblDicomDispatcher.setToolTipText("Dispatch dicom files from dicom daemon");
		add(lblDicomDispatcher, "cell 0 8,growx");
		
		btnDicomdispatcherstatus = new JLabel(iconDicomDispatcher);
		add(btnDicomdispatcherstatus, "cell 2 8,alignx center,growy");
		
		lblCommentdicomdispatcher = new JLabel("");
		add(lblCommentdicomdispatcher, "cell 4 8,growx");
		
		lblWarningdicomdispatcher = new JLabel("");
		lblWarningdicomdispatcher.setForeground(Color.RED);
		add(lblWarningdicomdispatcher, "cell 6 8,growx");
		
		JLabel lblDicomNode = new JLabel("Dicom Node");
		lblDicomNode.setToolTipText("Node to receive dicom");
		add(lblDicomNode, "cell 0 4,growx");
		
		btnDicomnodestatus = new JButton(iconDicomNode);
		add(btnDicomnodestatus, "cell 2 4,alignx left,growy");
		
		lblCommentdicomnode = new JLabel("");
		add(lblCommentdicomnode, "cell 4 4,growx");
		
		lblWarningdicomnode = new JLabel("");
		lblWarningdicomnode.setForeground(Color.RED);
		add(lblWarningdicomnode, "cell 6 4,growx");
		
		lblMissingDaemon = new JLabel("Missing Daemon");
		add(lblMissingDaemon, "cell 0 12,alignx left,aligny center");
		
		labelMissingDaemonStatus = new JLabel(iconMissingDaemon);
		add(labelMissingDaemonStatus, "cell 2 12,growx");
		
		labelCommentMissingDaemon = new JLabel("");
		add(labelCommentMissingDaemon, "cell 4 12,alignx left");
		
		labelWarningMissingDaemon = new JLabel("");
		labelWarningMissingDaemon.setForeground(Color.RED);
		add(labelWarningMissingDaemon, "cell 6 12,alignx left");
		
		JLabel lblNiftiDaemon = new JLabel("Nifti Daemon");
		lblNiftiDaemon.setToolTipText("Convert dicom to nifti");
		add(lblNiftiDaemon, "cell 0 14,growx");
		
		btnNiftidaemonstatus = new JButton(iconNiftiDaemon);
		add(btnNiftidaemonstatus, "cell 2 14,alignx left,growy");
		lblCommentniftidaemon = new JLabel("");
		add(lblCommentniftidaemon, "cell 4 14,growx");
		
		lblWarningniftidaemon = new JLabel("");
		lblWarningniftidaemon.setForeground(Color.RED);
		add(lblWarningniftidaemon, "cell 6 14,growx");
		
		JLabel lblDicomEncrypter = new JLabel("Encrypter");
		lblDicomEncrypter.setToolTipText("Encrypt files");
		add(lblDicomEncrypter, "cell 0 10,growx");
		
		btnEncrypterdaemonstatus = new JLabel(iconEncrypter);
		add(btnEncrypterdaemonstatus, "cell 2 10,alignx center,growy");
		
		lblCommentencrypter = new JLabel("");
		add(lblCommentencrypter, "cell 4 10,growx");
		
		lblWarningencrypter = new JLabel("");
		lblWarningencrypter.setForeground(Color.RED);
		add(lblWarningencrypter, "cell 6 10,growx");
		
		
		btnDicomdaemonstatus = new JButton(iconDicomDaemon);
		add(btnDicomdaemonstatus, "flowx,cell 2 6,alignx left,growy");
		
		lblWarningdicomdaemon = new JLabel("");
		lblWarningdicomdaemon.setForeground(Color.RED);
		add(lblWarningdicomdaemon, "flowx,cell 6 6,growx");
		JLabel lblDicomDaemon = new JLabel("Dicom Daemon");
		lblDicomDaemon.setToolTipText("Capture copy event from buffer dir");
		add(lblDicomDaemon, "cell 0 6,growx");
		
		lblCommentdicomdaemon = new JLabel("");
		add(lblCommentdicomdaemon, "cell 4 6,growx");
		
		if(UserProfile.CURRENT_USER.getLevel()!=3){
			// on masque les daemon non utilise si on est pas admin
			lblDicomDaemon.setVisible(false);
			lblDicomDispatcher.setVisible(false);
			lblNiftiDaemon.setVisible(false);
			lblDicomNode.setVisible(false);
			lblDicomEncrypter.setVisible(false);
			lblMissingDaemon.setVisible(false);
			btnDicomdaemonstatus.setVisible(false);
			btnDicomdispatcherstatus.setVisible(false);
			btnDicomnodestatus.setVisible(false);
			btnEncrypterdaemonstatus.setVisible(false);
			btnNiftidaemonstatus.setVisible(false);
			labelMissingDaemonStatus.setVisible(false);
			lblCommentdicomdaemon.setVisible(false);
			lblCommentdicomdispatcher.setVisible(false);
			lblCommentdicomnode.setVisible(false);
			lblCommentencrypter.setVisible(false);
			lblCommentniftidaemon.setVisible(false);
			labelCommentMissingDaemon.setVisible(false);
			lblWarningdicomdaemon.setVisible(false);
			lblWarningdicomdispatcher.setVisible(false);
			lblWarningdicomnode.setVisible(false);
			lblWarningencrypter.setVisible(false);
			lblWarningniftidaemon.setVisible(false);
			labelWarningMissingDaemon.setVisible(false);
		}
		
		
		btnDecrypterdaemonstatus.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(isDecrypterAlive()){
					if(SystemSettings.DECRYPT_DAEMON!=null && SystemSettings.DECRYPT_DAEMON.isAlive()){
						SystemSettings.DECRYPT_DAEMON.setStop(true);
						lblCommentdecrypter.setText("Stopping ... ");
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								while(SystemSettings.DECRYPT_DAEMON.isAlive()){
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								setDecrypterAlive(false);
								lblCommentdecrypter.setText("Stopped");
								lblWarningdecrypter.setText("");
								btnDecrypterdaemonstatus.setIcon(iconRed);
							}
						});
						
					}else{
						setDecrypterAlive(false);
						lblCommentdecrypter.setText("Stopped");
						lblWarningdecrypter.setText("");
						btnDecrypterdaemonstatus.setIcon(iconRed);
					}
					
				}else{
					if(SystemSettings.DECRYPT_DAEMON!=null && SystemSettings.DECRYPT_DAEMON.isAlive()){
						setDecrypterAlive(true);
					}else{
						SystemSettings.DECRYPT_DAEMON = new DecryptDaemon();
						SystemSettings.DECRYPT_DAEMON.start();
					}
					setDecrypterAlive(true);
					lblCommentdecrypter.setText("Started");
					lblWarningdecrypter.setText("");
					btnDecrypterdaemonstatus.setIcon(iconGreen);
				}
			}
		});
		
		btnNiftidaemonstatus.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(isNiftiDaemonAlive()){
					if(SystemSettings.NIFTI_DAEMON!=null && SystemSettings.NIFTI_DAEMON.isAlive()){
						SystemSettings.NIFTI_DAEMON.setStop(true);
						lblCommentniftidaemon.setText("Stopping ... ");
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								while(SystemSettings.NIFTI_DAEMON.isAlive()){
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								setNiftiDaemonAlive(false);
								lblCommentniftidaemon.setText("Stopped");
								lblWarningniftidaemon.setText("");
								btnNiftidaemonstatus.setIcon(iconRed);
							}
						});
						
					}else{
						setNiftiDaemonAlive(false);
						lblCommentniftidaemon.setText("Stopped");
						lblWarningniftidaemon.setText("");
						btnNiftidaemonstatus.setIcon(iconRed);
					}
					
				}else{
					if(SystemSettings.NIFTI_DAEMON!=null && SystemSettings.NIFTI_DAEMON.isAlive()){
						setNiftiDaemonAlive(true);
					}else{
						SystemSettings.NIFTI_DAEMON = new NiftiDaemon(SystemSettings.SERVER_INFO);
						SystemSettings.NIFTI_DAEMON.start();
					}
					setNiftiDaemonAlive(true);
					lblCommentniftidaemon.setText("Started");
					lblWarningniftidaemon.setText("");
					btnNiftidaemonstatus.setIcon(iconGreen);
				}
			}
		});
		btnDicomdaemonstatus.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(isDicomDaemonAlive()){
					if(SystemSettings.DICOM_DAEMON!=null && SystemSettings.DICOM_DAEMON.isAlive()){
						SystemSettings.DICOM_DAEMON.setStop(true);
						lblCommentdicomdaemon.setText("Stopping ... ");
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								while(SystemSettings.DICOM_DAEMON.isAlive() && SystemSettings.ENCRYPT_DAEMON.isAlive() && 
										SystemSettings.DICOM_DISPATCHER.isAlive()){
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								setDicomDaemonAlive(false);
								lblCommentdicomdaemon.setText("Stopped");
								lblWarningdicomdaemon.setText("");
								btnDicomdaemonstatus.setIcon(iconRed);
							}
						});
						
					}else{
						setDicomDaemonAlive(false);
						lblCommentdicomdaemon.setText("Stopped");
						lblWarningdicomdaemon.setText("");
						btnDicomdaemonstatus.setIcon(iconRed);
					}
					
				}else{
					if(SystemSettings.DICOM_DAEMON!=null && SystemSettings.DICOM_DAEMON.isAlive()){
						setDicomDaemonAlive(true);
					}else{
						SystemSettings.DICOM_DAEMON = new DicomDaemon(SystemSettings.SERVER_INFO);
						SystemSettings.DICOM_DAEMON.start();
					}
					setDicomDaemonAlive(true);
					lblCommentdicomdaemon.setText("Started");
					lblWarningdicomdaemon.setText("");
					btnDicomdaemonstatus.setIcon(iconGreen);
				}
			}
		});
		btnDicomnodestatus.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(isDicomNodeAlive()){
					if(SystemSettings.DICOM_NODE!=null && SystemSettings.DICOM_NODE.isAlive()){
						SystemSettings.DICOM_NODE.stop();
						lblCommentdicomnode.setText("Stopping ... ");
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								while(SystemSettings.DICOM_NODE.isAlive()){
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								setDicomNodeAlive(false);
								lblCommentdicomnode.setText("Stopped");
								lblWarningdicomnode.setText("");
								btnDicomnodestatus.setIcon(iconRed);
							}
						});
						
					}else{
						setDicomNodeAlive(false);
						lblCommentdicomnode.setText("Stopped");
						lblWarningdicomnode.setText("");
						btnDicomnodestatus.setIcon(iconRed);
					}
					
				}else{
					if(SystemSettings.DICOM_NODE!=null && SystemSettings.DICOM_NODE.isAlive()){
						setDicomNodeAlive(true);
					}else{
						SystemSettings.DICOM_NODE = new DicomNode();
						SystemSettings.DICOM_NODE.start();
					}
					setDicomNodeAlive(true);
					lblCommentdicomnode.setText("Started");
					lblWarningdicomnode.setText("");
					btnDicomnodestatus.setIcon(iconGreen);
				}
			}
		});
	}
	public ServerStatusPanel(int resfreshtime){
		super();
		setRefreshTime(resfreshtime);
	}
	
	
	// Accesseurs
	public int getRefreshTime() {
		return refreshTime;
	}
	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}
	public JButton getBtnDicomdaemonstatus() {
		return btnDicomdaemonstatus;
	}
	public void setBtnDicomdaemonstatus(JButton btnDicomdaemonstatus) {
		this.btnDicomdaemonstatus = btnDicomdaemonstatus;
	}
	public JLabel getBtnDicomdispatcherstatus() {
		return btnDicomdispatcherstatus;
	}
	public void setBtnDicomdispatcherstatus(JLabel btnDicomdispatcherstatus) {
		this.btnDicomdispatcherstatus = btnDicomdispatcherstatus;
	}
	public JButton getBtnDicomnodestatus() {
		return btnDicomnodestatus;
	}
	public void setBtnDicomnodestatus(JButton btnDicomnodestatus) {
		this.btnDicomnodestatus = btnDicomnodestatus;
	}
	public JButton getBtnNiftidaemonstatus() {
		return btnNiftidaemonstatus;
	}
	public void setBtnNiftidaemonstatus(JButton btnNiftidaemonstatus) {
		this.btnNiftidaemonstatus = btnNiftidaemonstatus;
	}
	public JLabel getBtnEncrypterdaemonstatus() {
		return btnEncrypterdaemonstatus;
	}
	public void setBtnEncrypterdaemonstatus(JLabel btnEncrypterdaemonstatus) {
		this.btnEncrypterdaemonstatus = btnEncrypterdaemonstatus;
	}
	public JButton getBtnDecrypterdaemonstatus() {
		return btnDecrypterdaemonstatus;
	}
	public void setBtnDecrypterdaemonstatus(JButton btnDecrypterdaemonstatus) {
		this.btnDecrypterdaemonstatus = btnDecrypterdaemonstatus;
	}
	public boolean isDicomDaemonAlive() {
		return dicomDaemonAlive;
	}
	public void setDicomDaemonAlive(boolean dicomDaemonAlive) {
		if(this.dicomDaemonAlive != dicomDaemonAlive){
			this.dicomDaemonAlive = dicomDaemonAlive;
			if(this.dicomDaemonAlive)
				btnDicomdaemonstatus.setIcon(iconGreen);
			else
				btnDicomdaemonstatus.setIcon(iconRed);
		}
	}
	public boolean isDicomDispatcherAlive() {
		return dicomDispatcherAlive;
	}
	public void setDicomDispatcherAlive(boolean dicomDispatcherAlive) {
		if(this.dicomDispatcherAlive != dicomDispatcherAlive){
			this.dicomDispatcherAlive = dicomDispatcherAlive;
			if(this.dicomDispatcherAlive)
				btnDicomdispatcherstatus.setIcon(iconGreen);
			else
				btnDicomdispatcherstatus.setIcon(iconRed);
		}
	}
	public boolean isDicomNodeAlive() {
		return dicomNodeAlive;
	}
	public void setDicomNodeAlive(boolean dicomNodeAlive) {
		if(this.dicomNodeAlive != dicomNodeAlive){
			this.dicomNodeAlive = dicomNodeAlive;
			if(this.dicomNodeAlive)
				btnDicomnodestatus.setIcon(iconGreen);
			else
				btnDicomnodestatus.setIcon(iconRed);
		}
	}
	public boolean isEncrypterAlive() {
		return encrypterAlive;
	}
	public void setEncrypterAlive(boolean encrypterAlive) {
		if(this.encrypterAlive != encrypterAlive){
			this.encrypterAlive = encrypterAlive;
			if(this.encrypterAlive)
				btnEncrypterdaemonstatus.setIcon(iconGreen);
			else
				btnEncrypterdaemonstatus.setIcon(iconRed);
		}
	}
	public boolean isDecrypterAlive() {
		return decrypterAlive;
	}
	public void setDecrypterAlive(boolean decrypterAlive) {
		if(this.decrypterAlive != decrypterAlive){
			this.decrypterAlive = decrypterAlive;
			if(this.encrypterAlive)
				btnDecrypterdaemonstatus.setIcon(iconGreen);
			else
				btnDecrypterdaemonstatus.setIcon(iconRed);
		}
	}
	public boolean isNiftiDaemonAlive() {
		return niftiDaemonAlive;
	}
	public void setNiftiDaemonAlive(boolean niftiDaemonAlive) {
		if(this.niftiDaemonAlive != niftiDaemonAlive){
			this.niftiDaemonAlive = niftiDaemonAlive;
			if(this.niftiDaemonAlive)
				btnNiftidaemonstatus.setIcon(iconGreen);
			else
				btnNiftidaemonstatus.setIcon(iconRed);
		}
	}
	public boolean isMissingDaemonAlive() {
		return missingDaemonAlive;
	}
	public void setMissingDaemonAlive(boolean missingDaemonAlive) {
		if(this.missingDaemonAlive != missingDaemonAlive){
			this.missingDaemonAlive = missingDaemonAlive;
			if(this.missingDaemonAlive)
				labelMissingDaemonStatus.setIcon(iconGreen);
			else
				labelMissingDaemonStatus.setIcon(iconRed);
		}
	}
	public JLabel getLblCommentdicomdaemon() {
		return lblCommentdicomdaemon;
	}
	public void setLblCommentdicomdaemon(JLabel lblCommentdicomdaemon) {
		this.lblCommentdicomdaemon = lblCommentdicomdaemon;
	}
	public JLabel getLblCommentdicomdispatcher() {
		return lblCommentdicomdispatcher;
	}
	public void setLblCommentdicomdispatcher(JLabel lblCommentdicomdispatcher) {
		this.lblCommentdicomdispatcher = lblCommentdicomdispatcher;
	}
	public JLabel getLblCommentdicomnode() {
		return lblCommentdicomnode;
	}
	public void setLblCommentdicomnode(JLabel lblCommentdicomnode) {
		this.lblCommentdicomnode = lblCommentdicomnode;
	}
	public JLabel getLblCommentniftidaemon() {
		return lblCommentniftidaemon;
	}
	public void setLblCommentniftidaemon(JLabel lblCommentniftidaemon) {
		this.lblCommentniftidaemon = lblCommentniftidaemon;
	}
	
	public JLabel getLblCommentMissingdaemon() {
		return labelCommentMissingDaemon;
	}
	public void setLblCommentMissingdaemon(JLabel labelCommentMissingDaemon) {
		this.labelCommentMissingDaemon = labelCommentMissingDaemon;
	}
	public JLabel getLblCommentencrypter() {
		return lblCommentencrypter;
	}
	public void setLblCommentencrypter(JLabel lblCommentencrypter) {
		this.lblCommentencrypter = lblCommentencrypter;
	}
	public JLabel getLblCommentdecrypter() {
		return lblCommentdecrypter;
	}
	public void setLblCommentdecrypter(JLabel lblCommentdecrypter) {
		this.lblCommentdecrypter = lblCommentdecrypter;
	}
	public JLabel getLblWarningdicomdaemon() {
		return lblWarningdicomdaemon;
	}
	public void setLblWarningdicomdaemon(JLabel lblWarningdicomdaemon) {
		this.lblWarningdicomdaemon = lblWarningdicomdaemon;
	}
	public JLabel getLblWarningdicomdispatcher() {
		return lblWarningdicomdispatcher;
	}
	public void setLblWarningdicomdispatcher(JLabel lblWarningdicomdispatcher) {
		this.lblWarningdicomdispatcher = lblWarningdicomdispatcher;
	}
	public JLabel getLblWarningdicomnode() {
		return lblWarningdicomnode;
	}
	public void setLblWarningdicomnode(JLabel lblWarningdicomnode) {
		this.lblWarningdicomnode = lblWarningdicomnode;
	}
	public JLabel getLblWarningniftidaemon() {
		return lblWarningniftidaemon;
	}
	public void setLblWarningniftidaemon(JLabel lblWarningniftidaemon) {
		this.lblWarningniftidaemon = lblWarningniftidaemon;
	}
	public JLabel getLblWarningMissingDaemon() {
		return labelWarningMissingDaemon;
	}
	public void setLblWarningMissingdaemon(JLabel lblWarningMissingdaemon) {
		this.labelWarningMissingDaemon = lblWarningMissingdaemon;
	}
	public JLabel getLblWarningencrypter() {
		return lblWarningencrypter;
	}
	public void setLblWarningencrypter(JLabel lblWarningencrypter) {
		this.lblWarningencrypter = lblWarningencrypter;
	}
	public JLabel getLblWarningdecrypter() {
		return lblWarningdecrypter;
	}
	public void setLblWarningdecrypter(JLabel lblWarningdecrypter) {
		this.lblWarningdecrypter = lblWarningdecrypter;
	}
	
	/**
	 * Permet de gerer les erreurs critiques (de passer le bouton en jaune)
	 * @param string
	 */
	public void setCritical(JButton btn) {
		btn.setIcon(iconYellow);
		
	}
	
	/**
	 * Permet de gerer les erreurs critiques (de passer le bouton en jaune)
	 * @param string
	 */
	public void setCritical(JLabel btn) {
		btn.setIcon(iconYellow);
		
	}
	
	
	// Methodes
}
