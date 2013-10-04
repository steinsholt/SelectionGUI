package hbs.sff.no;

import javax.swing.table.AbstractTableModel;

public class SelectionTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String columnNames[];
	private Object rowData[][];
	
	
	public SelectionTableModel(String[] columnNames){
		
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
