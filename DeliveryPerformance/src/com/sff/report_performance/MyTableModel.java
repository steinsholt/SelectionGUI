package com.sff.report_performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/*
 * This class allows for specialized removal and insertion not implemented
 * in standard table models.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MyTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private List<String> columnNames = new ArrayList<String>();
	private List<List> data = new ArrayList<List>();
	private ImageIcon deleteIcon;

	public MyTableModel(List<String> columnNames){
		this.columnNames = columnNames;
		this.deleteIcon = new ImageIcon("C:/vendorLogistics/delete_14.png");
	}
	
	public MyTableModel(List<String> columnNames, TableModelListener tableModelListener){
		this.columnNames = columnNames;
		this.deleteIcon = new ImageIcon("C:/vendorLogistics/delete_14.png");
	}

	public void addRow(List rowData){
		if(!data.contains(rowData)){
			data.add(rowData);
			fireTableRowsInserted(data.size() - 1, data.size() - 1);
		}
	}

	public void addRowInterval(int[] selection, JTable table){

		MyTableModel model = (MyTableModel) table.getModel();
		for(int i : selection){
			if(columnNames.size()==3) addRow(Arrays.asList(deleteIcon, model.getRow(i).get(1), model.getRow(i).get(2)));
			else addRow(Arrays.asList(deleteIcon, model.getRow(i).get(1)));
			table.setValueAt(true, table.convertRowIndexToView(i), 0);
		}
	}

	public void removeRow(int row){
		if(data.size()>0){
			data.remove(row);
			fireTableRowsDeleted(row, row);
		}
	}

	public void partialRemoval(int[] selection, JTable table){
		MyTableModel model = (MyTableModel) table.getModel();
		for(int i : selection){
			List row = model.getRow(i); // create new class and use comparator or override equals
			List temp = new ArrayList(row);
			temp.set(0, deleteIcon);
			if(data.contains(temp)){    
				removeRow(data.indexOf(temp));
				table.setValueAt(false, table.convertRowIndexToView(i), 0);
			}
		}
	}

	public void clearSelection(JTable selectionTable){
		MyTableModel model = (MyTableModel) selectionTable.getModel();
		while(data.size()!=0){

			List row = this.getRow(0);
			List temp = new ArrayList(row);
			temp.set(0, true);
			if(model.getRowData().contains(temp)){
				int index = model.getRowContaining(temp);
				model.setValueAt(false, index, 0);
			}
			removeRow(0);
		}
	}

	public void removeRowInterval(int[] selection, JTable selectionTable){
		MyTableModel model = (MyTableModel) selectionTable.getModel();
		for(int i = selection.length; --i >= 0;){
			List row = this.getRow(selection[i]);
			List temp = new ArrayList(row);
			temp.set(0, true);
			if(model.getRowData().contains(temp)){
				int index = model.getRowContaining(temp);
				model.setValueAt(false, index, 0);
			}
			if(data.size()>0) data.remove(selection[i]);
		}
	}
	
	public void removeAllRows(){ // NEW
		data.clear();
		fireTableDataChanged();
	}

	public int getRowContaining(Object id){
		return data.indexOf(id);		
	}

	public List<List> getRowData() {
		return data;
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

	public int getColumnCount() {
		return columnNames.size();
	}

	public int getRowCount() {
		return data.size();
	}

	public Object getValueAt(int row, int column) {
		if(data.isEmpty())return false;
		else return data.get(row).get(column);
	}

	public void setValueAt(Object value, int row, int column){
		if(data.size() > row){
			data.get(row).set(column, value);
			fireTableCellUpdated(row, column);
		}
	}

	public List getRow(int i) {
		return data.get(i);
	}
}
