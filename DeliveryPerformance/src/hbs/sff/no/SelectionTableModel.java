package hbs.sff.no;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SelectionTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private List<String> columnNames = new ArrayList<String>();
	private List<List> data = new ArrayList<List>();
	private Boolean editable = false;

	public SelectionTableModel(List<String> columnNames){
		this.columnNames = columnNames;
	}

	public void addRow(List rowData){
		if(!data.contains(rowData)){
			data.add(rowData);
			fireTableRowsInserted(data.size() - 1, data.size() - 1);
		}
	}

	public void addRowAt(List rowData, int index){
		if(!data.contains(rowData)){
			data.add(index, rowData);
			fireTableRowsInserted(data.size() - 1, data.size() - 1);
		}
	}

	public void removeRow(int row){
		if(data.size()>1){
			data.remove(row);
			fireTableRowsDeleted(row, row);
		}
	}

	public int getRowContaining(Object id){
		return data.indexOf(id);		
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

	public Class getColumnClass(int column) {
		return (getValueAt(0, column).getClass());
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public boolean isCellEditable(int row, int column) {
		if(editable){return (row == 0 && column == 0);}
		else return (false);
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
		fireTableCellUpdated(row, column);
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public List getRow(int i) {
		return data.get(i);
	}

	public void setRemoveAll(){
		if(columnNames.size()==3)data.add(0,Arrays.asList(false,"Remove all",""));
		else data.add(0,Arrays.asList(false,"Remove all"));
		data.remove(1);
	}

	public void setTrueAll(){
		if(columnNames.size()==3)data.add(0,Arrays.asList(true,"ALL",""));
		else data.add(0,Arrays.asList(true,"ALL"));
		data.remove(1);
	}
}
