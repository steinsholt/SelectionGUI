package com.sff.report_performance;

import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class ReportParameterTableModelListener implements TableModelListener {
	private JTextField textField = null;
	private MyTableModel displayModel = null;
	private MyTableModel selectionModel;
	
	public ReportParameterTableModelListener(JTextField textField){
		this.textField = textField;
	}
	
	public ReportParameterTableModelListener(MyTableModel displayModel, MyTableModel selectionModel){
		this.displayModel = displayModel;
		this.selectionModel = selectionModel;
	}
	
	@Override
	public void tableChanged(TableModelEvent arg0) {
		if(textField!=null) textField.setText("ALL");
		if(displayModel!=null) {
			selectionModel.removeAllRows();
			displayModel.removeAllRows();
		}
	}
}
