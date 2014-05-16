package com.sff.report_performance;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextFieldDocumentListener implements DocumentListener{
	private MyTableModel displayModel;
	private MyTableModel selectionModel;
	
	public TextFieldDocumentListener(MyTableModel displayModel, MyTableModel selectionModel){
		this.displayModel = displayModel;
		this.selectionModel = selectionModel;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		displayModel.removeAllRows();
		selectionModel.removeAllRows();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	}
}
