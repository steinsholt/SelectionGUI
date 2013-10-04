package hbs.sff.no;

import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

public class SelectionTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String columnNames[];
	private Object rowData[][];
	private HashMap<Integer, String> Entries;
	
	public SelectionTableModel(String[] columnNames, Object[][] rowData){
		this.columnNames = columnNames;
		this.rowData = rowData;
		Entries = new HashMap<Integer, String>();
	}
	
	public void addEntry(Integer key, String value){
		Entries.put(key, value);
	}
	
	public void voidLoadData(){
		// TODO: Selects the data from the database and adds to the HashMap
	}
	
	public Object[][] getRowData() {
		return rowData;
	}

	public void setRowData(Object[][] rowData) {
		this.rowData = rowData;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int column) {
		return (getValueAt(0, column).getClass());
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	
	public boolean isCellEditable(int row, int column) {
		return (column != 1);
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return rowData.length;
	}

	public Object getValueAt(int row, int column) {
		return rowData[row][column];
	}
	
	public void setValueAt(int row, int column, Object value) {
		rowData[row][column] = value;
	}
}
