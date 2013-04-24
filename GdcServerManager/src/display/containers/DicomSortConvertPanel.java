package display.containers;

import ij.plugin.DICOM;
import ij.util.DicomTools;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import model.DicomImage;
import model.daemon.CustomConversionSettings;
import net.miginfocom.swing.MigLayout;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import org.apache.commons.io.FileUtils;

import settings.SystemSettings;
import daemon.DicomDaemon;
import daemon.DicomJobDispatcher;
import daemon.DicomWorkerClient;
import daemon.NiftiDaemon;
import daemon.NiftiDaemon.FORMAT;
import display.SettingsFrame;

import java.awt.Dimension;
import java.awt.EventQueue;

public class DicomSortConvertPanel extends JPanel {

	// Attributs
	private List<File> droppedFiles;
	private DicomJobDispatcher ddaemon;
	private NiftiDaemon ndaemon;
	private Thread statusThread; // Thread permettant de mettre a jours le status de la conversion
	// boutons
	private JSplitPane splitPane;
	private JButton btnStart;
	private JButton btnCancel;
	private JTable table;
	private DefaultTableModel model;
	private JPanel panel;
	private JCheckBox chckbxSortDicom;
	private JCheckBox chckbxConvertToNifti;
	private JComboBox comboBox;
	private JButton btnReset;
	private ProgressPanel progressPanel;
	private JLabel lblStatus;
	private JScrollPane scrollPane;
	private JButton btnOutputDir;
	private JCheckBox chckbxProject;
	private JCheckBox chckbxDate;
	private JCheckBox chckbxProtocol;
	private JCheckBox chckbxPatientName;
	private JCheckBox chckbxSerie;
	private JLabel label;
	private JCheckBox chckb4d;
	



	public DicomSortConvertPanel(){
		droppedFiles = new ArrayList<File>();
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		model = new DefaultTableModel();
		table = new JTable(model);
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5);
		add(splitPane, "cell 0 0,grow");

		table = new JTable(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"File"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		
		
		
		scrollPane = new JScrollPane(table);
		// On gere le drop
		scrollPane.setDropTarget(new DropTarget() {
	        public synchronized void drop(DropTargetDropEvent evt) {
	            try {
	            	progressPanel.setVisible(true);
	                evt.acceptDrop(DnDConstants.ACTION_COPY);
	                final List<File> listFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	                Thread addThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							for(File fi:listFiles){
			                	try {
									addFile(fi);
								} catch (IOException e) {
									e.printStackTrace();
								}
			                }
							progressPanel.setVisible(false);
							setLock(false);
						}
					});
	                statusThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							while(progressPanel.isVisible()){
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								getLblStatus().setText(droppedFiles.size() + " Dicoms in list." );
							}
						}
					});
					statusThread.start();
	                addThread.start();
	                setLock(true);
	            } catch (Exception ex) {
	                ex.printStackTrace();
	                progressPanel.setVisible(false);
	            }
	        }
	    });
		
		splitPane.setLeftComponent(scrollPane);
		splitPane.setResizeWeight(0.8);
		panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new MigLayout("", "[grow][grow]", "[][][][]"));
		
		chckbxSortDicom = new JCheckBox("Sort Dicom");
		chckbxSortDicom.setSelected(true);
		panel.add(chckbxSortDicom, "flowx,cell 0 0,alignx left");
		btnStart = new JButton("Start");
		panel.add(btnStart, "flowx,cell 1 0,alignx left");
		
		chckbxConvertToNifti = new JCheckBox("Convert to nifti");
		chckbxConvertToNifti.setSelected(true);
		panel.add(chckbxConvertToNifti, "flowx,cell 0 1,alignx left");
		
		comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(FORMAT.values()));
		comboBox.setSelectedIndex(2);
		panel.add(comboBox, "cell 0 1,growx");
		
		progressPanel = new ProgressPanel();
		progressPanel.setPreferredSize(new Dimension(160, 10));
		progressPanel.setVisible(false);
		panel.add(progressPanel, "cell 0 2,alignx left");
		
		lblStatus = new JLabel("");
		panel.add(lblStatus, "cell 0 3");
		
		btnOutputDir = new JButton("Output dir");
		btnOutputDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel.add(btnOutputDir, "flowx,cell 1 1");
		
		btnCancel = new JButton("Cancel");
		btnCancel.setEnabled(false);
		panel.add(btnCancel, "cell 1 1,alignx left");
		
		btnReset = new JButton("reset");
		panel.add(btnReset, "cell 1 0,alignx left");
		
		label = new JLabel("|");
		panel.add(label, "cell 0 0");
		
		chckbxProject = new JCheckBox("Project");
		chckbxProject.setSelected(true);
		panel.add(chckbxProject, "cell 0 0");
		
		chckbxPatientName = new JCheckBox("Patient");
		chckbxPatientName.setEnabled(false);
		chckbxPatientName.setSelected(true);
		panel.add(chckbxPatientName, "cell 0 0");
		
		chckbxDate = new JCheckBox("Acquisition date");
		chckbxDate.setSelected(true);
		panel.add(chckbxDate, "cell 0 0");
		
		chckbxProtocol = new JCheckBox("Protocol");
		chckbxProtocol.setSelected(true);
		panel.add(chckbxProtocol, "cell 0 0");
		
		chckbxSerie = new JCheckBox("Serie");
		chckbxSerie.setEnabled(false);
		chckbxSerie.setSelected(true);
		panel.add(chckbxSerie, "cell 0 0");
		
		chckb4d = new JCheckBox("4D");
		panel.add(chckb4d, "cell 0 1");
		
		btnReset.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				stop();
				reset();
			}
		});
		
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				stop();
			}
		});
		
		btnStart.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				progressPanel.setVisible(true);
				setLock(true);
				if(chckbxConvertToNifti.isSelected()){
					int fmt = -1;
					switch((FORMAT)comboBox.getSelectedItem()){
					case SPM2:
						fmt = NiftiDaemon.ANALYZE_7_5;
						break;
					case SPM5:
						fmt = NiftiDaemon.SPM5_NIFTI;break;
					case NIFTI:
						fmt = NiftiDaemon.NIFTI_4D;break;
					case cNIFTI:
						fmt = NiftiDaemon.FSL_NIFTI;break;
					default:
						System.err.println("Unknow NIFTI FORMAT");
					}
					final CustomConversionSettings csettings = new CustomConversionSettings(false, chckbxProject.isSelected(),
							chckbxDate.isSelected(), chckbxProtocol.isSelected(),chckbxSortDicom.isSelected(),chckb4d.isSelected());
					ndaemon = new NiftiDaemon(SystemSettings.SERVER_INFO, fmt, csettings);
					ddaemon = new DicomJobDispatcher(SystemSettings.SERVER_INFO, csettings, ndaemon);

					
					
					statusThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							progressPanel.setVisible(true);
							while(true){
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								if(ddaemon!=null){
									getLblStatus().setText(ddaemon.getDicomToMove().size() + " Dicoms left." );
									if(ddaemon.getDicomToMove().isEmpty()){
										ddaemon.setStop(true);
										ddaemon = null;
										ndaemon.start();
									}
								}else{
									getLblStatus().setText(ndaemon.getDir2convert().size() + " Niftis left." );
									if(ndaemon.getDir2convert().isEmpty()){
										ndaemon.setStop(true);
										ndaemon = null;
										getLblStatus().setText("");
										progressPanel.setVisible(false);
										setLock(false);
										reset();
										break;
									}
								}
							}

							// si on ne doit pas garder les dicoms on les supprime
							if(!csettings.keepDicom() && DicomWorkerClient.DICOMDIR!=null){
								try {
									FileUtils.deleteDirectory(DicomWorkerClient.DICOMDIR.toFile());
								} catch (IOException e) {
									e.printStackTrace();
								}
								DicomWorkerClient.DICOMDIR=null;
							}
						}
					});
					statusThread.start();
					for(File fi:droppedFiles){
						ddaemon.addDicomToMove(fi.toPath());
					}
					ddaemon.start();
				}else{
					CustomConversionSettings csettings = new CustomConversionSettings(false, chckbxProject.isSelected(),
							chckbxDate.isSelected(), chckbxProtocol.isSelected(),chckbxSortDicom.isSelected(),chckb4d.isSelected());
					ddaemon = new DicomJobDispatcher(SystemSettings.SERVER_INFO, csettings, null);

					
					statusThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							progressPanel.setVisible(true);
							while(true){
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								if(ddaemon!=null){
									getLblStatus().setText(ddaemon.getDicomToMove().size() + " Dicoms left." );
									if(ddaemon.getDicomToMove().isEmpty()){
										ddaemon.setStop(true);
										ddaemon = null;
										getLblStatus().setText("");
										progressPanel.setVisible(false);
										setLock(false);
										reset();
										break;
									}
								}
							}
						}
					});
					statusThread.start();
					for(File fi:droppedFiles){
						ddaemon.addDicomToMove(fi.toPath());
					}
					ddaemon.start();
				}
				
			}
		});

		chckbxConvertToNifti.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				comboBox.setVisible(chckbxConvertToNifti.isSelected());
			}
		});
		
		
		// Affiche la fenetre des settings avec choix des repertoires
		btnOutputDir.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final SettingsFrame sf = new SettingsFrame(1);
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						sf.createAndShowGUI();
					}
				});
				
			}
		});
	}


	public JLabel getLblStatus() {
		return lblStatus;
	}


	public void setLblStatus(JLabel lblStatus) {
		this.lblStatus = lblStatus;
	}
	public JCheckBox getChckb4d() {
		return chckb4d;
	}


	public void setChckb4d(JCheckBox chckb4d) {
		this.chckb4d = chckb4d;
	}

	private void addFile(File fi) throws IOException{
		if(fi.isDirectory()){
			for(String cfile:fi.list())
				addFile(new File(fi.getAbsolutePath()+"/"+cfile));
			return;
		}
		if(!droppedFiles.contains(fi) && DicomImage.isDicom(fi)){
			droppedFiles.add(fi);
        	((DefaultTableModel) table.getModel()).addRow(new Object[]{fi.getParentFile().getName()+"/"+fi.getName()});
		}
	}
	
	
	private void setLock(boolean islock){
		table.setEnabled(!islock);
		chckbxConvertToNifti.setEnabled(!islock);
		btnReset.setEnabled(!islock);
		btnStart.setEnabled(!islock);
		btnCancel.setEnabled(islock);
		comboBox.setEnabled(!islock);
		scrollPane.setEnabled(!islock);
		btnOutputDir.setEnabled(!islock);
	}
	
	/**
	 * Arrete les daemon de la conversion
	 */
	private void stop(){
		if(ddaemon!=null){
			ddaemon.setStop(true);
			ddaemon = null;
		}
		if(ndaemon!=null){
			ndaemon.setStop(true);
			ndaemon = null;
		}
		// si on ne doit pas garder les dicoms on les supprime
		if(DicomWorkerClient.DICOMDIR!=null){
			try {
				FileUtils.deleteDirectory(DicomWorkerClient.DICOMDIR.toFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
			DicomWorkerClient.DICOMDIR=null;
		}
		if(statusThread!=null){
			setLock(false);
			getLblStatus().setText("");
			progressPanel.setVisible(false);
			statusThread.stop();
			statusThread = null;
		}
	}


	/**
	 * Reset le panel en nettoyant le tableau et la liste de fichier
	 */
	public void reset() {
		droppedFiles.clear();
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				((DefaultTableModel) table.getModel()).setRowCount(0);
			}
		});
		
		
	}
	
	
}
