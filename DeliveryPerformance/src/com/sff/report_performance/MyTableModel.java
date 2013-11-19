package com.sff.report_performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;
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
		this.deleteIcon = new ImageIcon("C:/Users/hbs/workspace/SelectionGUI/DeliveryPerformance/img/delete_16.png");
	}

	public void addRow(List rowData){
		if(!data.contains(rowData)){
			data.add(rowData);
			fireTableRowsInserted(data.size() - 1, data.size() - 1);
		}
	}

	public void addRowInterval(int min, int max, JTable table){
		MyTableModel model = (MyTableModel) table.getModel();
		for(int i = min; i <= max; i++){
			if(columnNames.size()==3) addRow(Arrays.asList(deleteIcon, model.getRow(i).get(1), model.getRow(i).get(2)));
			else addRow(Arrays.asList(deleteIcon, model.getRow(i).get(1)));
			table.setValueAt(true, i, 0);
		}
		model.fireTableDataChanged();
	}

	public void removeRow(int row){
		if(data.size()>0){
			data.remove(row);
			fireTableRowsDeleted(row, row);
		}
	}

	public void partialRemoval(int min, int max, JTable table){
		MyTableModel model = (MyTableModel) table.getModel();
		for(int i = min; i <= max; i++){
			List row = model.getRow(i); // create new class and use comparator or override equals
			List temp = new ArrayList(row);
			temp.set(0, deleteIcon);
			if(data.contains(temp)){    
				removeRow(data.indexOf(temp));
				table.setValueAt(false, i, 0);
			}
		}
		model.fireTableDataChanged();
	}

	public void removeRowInterval(int min, int max, JTable table){
		MyTableModel model = (MyTableModel) table.getModel();
		for(int i = min; i <= max; i++){
			List row = this.getRow(min);
			List temp = new ArrayList(row);
			temp.set(0, true);
			if(model.getRowData().contains(temp)){
				int index = model.getRowContaining(temp);
				model.setValueAt(false, index, 0);
			}
			if(data.size()>0) data.remove(min);
		}
		fireTableRowsDeleted(min, max);
		model.fireTableDataChanged();
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
