package hbs.sff.no;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class SelectionTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private List<String> columnNames = new ArrayList<String>();
	private List<List> data = new ArrayList<List>();
	
	public SelectionTableModel(List<String> columnNames){
		this.columnNames = columnNames;;	
	}
	
	public void addRow(List rowData){
		data.add(rowData);
		fireTableRowsInserted(data.size() - 1, data.size() - 1);
	}
	
	public List<List> getRowData() {
		return data;
	}

	public void setRowData(List<List> data) {
		this.data = data;
	}

	public String getColumnName(int column) {
		return columnNames.get(column);
	}
	
	public Class<? extends Object> getColumnClass(int column) {
		return (getValueAt(0, column).getClass());
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}
	
	public boolean isCellEditable(int row, int column) {
		return (column == 0);
	}

	public int getColumnCount() {
		return columnNames.size();
	}

	public int getRowCount() {
		return data.size();
	}

	public Object getValueAt(int row, int column) {
		return data.get(row).get(column);
	}
	
	public void setValueAt(Object value, int row, int column){
		data.get(row).set(column, value);
	}
}
