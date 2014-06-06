package com.sff.report_performance;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;

public class TextFieldDocumentListener implements DocumentListener{
	private MyTableModel displayModel;
	private MyTableModel selectionModel;
	private ReportParameterTable displayTable;
	
	public TextFieldDocumentListener(MyTableModel displayModel, MyTableModel selectionModel, ReportParameterTable displayTable){
		this.displayModel = displayModel;
		this.selectionModel = selectionModel;
		this.displayTable = displayTable;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		displayModel.removeAllRows();
		selectionModel.removeAllRows();
		TableColumn column = displayTable.getColumnModel().getColumn(0);
		CheckBoxHeader checkBoxHeader = (CheckBoxHeader) column.getHeaderRenderer();
		displayTable.setHeaderClicked(false);
		checkBoxHeader.setSelected(true);
		displayTable.setHeaderClicked(true);
		displayTable.getTableHeader().repaint();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	}
}
