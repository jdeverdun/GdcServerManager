package display;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import model.DICOM;
import model.DicomImage;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import display.containers.RequestTableModel;
import exceptions.DicomException;


public class DicomInfoFrame extends JFrame{

	private File dicomFile;
	private JTable table;
	private BasicTableModel rqModel;



	public DicomInfoFrame(File file) {
		setDicomFile(file);
		table = new JTable();
		table.setName("");
		rqModel = new BasicTableModel();
		table.setModel(rqModel);
		table.setAutoCreateRowSorter(true);
		// on centre les colonnes
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		table.setDefaultRenderer(String.class, centerRenderer);
		getContentPane().add(new JScrollPane(table));
		
		
		loadDicomInfo();
		
		

	}
	
	private void loadDicomInfo(){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run(){
				FileInputStream fis;
				try {
					fis = new FileInputStream(dicomFile.toString());

					String header = new DICOM(fis).getInfo(dicomFile.toPath());
					if(header == null)
						throw new DicomException("Empty DICOM header");
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					String[] lines = header.split("\n");
					String[][] data = new String[lines.length][3];
					int count = 0;
					for(String line:lines){
						int ind = line.indexOf(" ");
						data[count][0] = line.split(" ")[0];
						data[count][1] = line.split(":")[0].substring(ind+1);
						data[count][2] = line.split(":")[1];
						count++;
					}
					getRqModel().fireTableStructureChanged();
					getRqModel().setData(data);
					
					getRqModel().fireTableDataChanged();
					createAndShowGUI();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				/*getRqModel().fireTableStructureChanged();
				getRqModel().setData(cdata);
				getRqModel().setFiles(cfiles);
				
				getRqModel().fireTableDataChanged();*/
			}
		});
	}
	public void createAndShowGUI(){
		JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	          //UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
        } catch (Exception e) {
          System.out.println("Substance Graphite failed to initialize");
        }
		//UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
		setTitle("DicomInfo");
		setSize(600, 600);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon(this.getClass().getResource("/images/logo32.png")).getImage());
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public BasicTableModel getRqModel() {
		return rqModel;
	}

	public void setRqModel(BasicTableModel rqModel) {
		this.rqModel = rqModel;
	}
	
	/**
	 * @return the dicomFile
	 */
	public File getDicomFile() {
		return dicomFile;
	}

	/**
	 * @param dicomFile the dicomFile to set
	 */
	public void setDicomFile(File dicomFile) {
		this.dicomFile = dicomFile;
	}

}

class BasicTableModel extends AbstractTableModel {
	private String[] columnNames = {"Code","Nom", "Valeur"};
    private Object[][] data = {
    {"", "", ""}
    };

    public int getColumnCount() {
        return columnNames.length;
    }

    public void setColumns(String[] colnames) {
    	columnNames = colnames;
	}

    public void setData(Object[][] data){
    	this.data = data;
    }
	public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
    	return data[row][col];
    }

    public Class getColumnClass(int c) {
    	return String.class;
        //return getValueAt(0, c).getClass();
    }

	public boolean isCellEditable(int row, int col) {
    	 return true;
	}

     public void setValueAt(Object value, int row, int col) {            
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}