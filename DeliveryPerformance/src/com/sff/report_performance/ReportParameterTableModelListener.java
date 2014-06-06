package com.sff.report_performance;

import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class ReportParameterTableModelListener implements TableModelListener {
	private JTextField textField = null;
	private MyTableModel displayModel = null;
	private MyTableModel selectionModel;
	private DefaultTableModel defaultModel;
	private ReportParameterTable displayTable;
	
	public ReportParameterTableModelListener(JTextField textField, DefaultTableModel defaultModel){
		this.defaultModel = defaultModel;
		this.textField = textField;
	}
	
	public ReportParameterTableModelListener(MyTableModel displayModel, MyTableModel selectionModel, ReportParameterTable displayTable){
		this.displayModel = displayModel;
		this.selectionModel = selectionModel;
		this.displayTable = displayTable;
	}
	
	@Override
	public void tableChanged(TableModelEvent evt) {
		if(textField!=null) {
			textField.setText("ALL");
			defaultModel.setRowCount(0);
		}
		if(displayModel!=null) {
			selectionModel.removeAllRows();
			displayModel.removeAllRows();
			CheckBoxHeader checkBoxHeader = (CheckBoxHeader) displayTable.getColumnModel().getColumn(0).getHeaderRenderer();
			displayTable.setHeaderClicked(false);
			checkBoxHeader.setSelected(true);
			displayTable.setHeaderClicked(true);
			displayTable.getTableHeader().repaint();
			// after table change, if table is empty disable header and set selected. Enable and deselect on search
		}
	}
}
