package display.containers;

import java.io.File;

import javax.swing.table.AbstractTableModel;

public class RequestTableModel extends AbstractTableModel {
	private File[] files;
	private static final String[] DEFAULT_COLUMN_NAME =  {"Project", "Patient", "Date", "Protocol", "Serie"};
	private String[] columnNames = {"Project", "Patient", "AcquisitionDate", "Protocol", "Serie"};
    private Object[][] data = {
    {"", "", "", "", ""}
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

    public File getFileAt(int row){
    	if(files==null || files.length==0)
    		return null;
    	return files[row];
    }
    public Object getValueAt(int row, int col) {
    	return data[row][col];
    }

    public Class getColumnClass(int c) {
    	return String.class;
        //return getValueAt(0, c).getClass();
    }

	public boolean isCellEditable(int row, int col) {
    	 return false;
	}

     public void setValueAt(Object value, int row, int col) {            
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
    public void setFiles(File[] fi){
    	files = fi;
    }
}