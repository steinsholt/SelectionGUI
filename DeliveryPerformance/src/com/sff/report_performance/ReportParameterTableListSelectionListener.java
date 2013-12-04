package com.sff.report_performance;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ReportParameterTableListSelectionListener implements ListSelectionListener{

	private ReportParameterTable table;
	private SelectionTable selectionTable;
	
	public ReportParameterTableListSelectionListener(ReportParameterTable table, SelectionTable selectionTable){
		this.table = table;
		this.selectionTable = selectionTable;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		
		boolean isAdjusting = e.getValueIsAdjusting();
		MyTableModel model = (MyTableModel) table.getModel();
		
		if(!lsm.isSelectionEmpty() && !isAdjusting){
			int[] selection = table.getSelectedRows();

			for (int i = 0; i < selection.length; i++) {
				selection[i] = table.convertRowIndexToModel(selection[i]);
			}
			
			model.removeRowInterval(selection, selectionTable);
			
			lsm.clearSelection();
			selectionTable.synchronizeHeader();
			table.synchronizeHeader();
		}
	}
}
