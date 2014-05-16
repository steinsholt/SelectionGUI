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
	
	public ReportParameterTableModelListener(JTextField textField, DefaultTableModel defaultModel){
		this.defaultModel = defaultModel;
		this.textField = textField;
	}
	
	public ReportParameterTableModelListener(MyTableModel displayModel, MyTableModel selectionModel){
		this.displayModel = displayModel;
		this.selectionModel = selectionModel;
	}
	
	@Override
	public void tableChanged(TableModelEvent arg0) {
		if(textField!=null) {
			textField.setText("ALL");
			defaultModel.setRowCount(0);
		}
		if(displayModel!=null) {
			selectionModel.removeAllRows();
			displayModel.removeAllRows();
		}
	}
}
