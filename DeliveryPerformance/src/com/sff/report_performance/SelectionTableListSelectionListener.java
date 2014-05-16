package com.sff.report_performance;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sff.report_performance.GUI.Active;

public class SelectionTableListSelectionListener implements ListSelectionListener{

	private SelectionTable table;

	public SelectionTableListSelectionListener(SelectionTable table){
		this.table = table;
	}

	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		int min = lsm.getMinSelectionIndex();
		
		boolean isAdjusting = e.getValueIsAdjusting();
		MyTableModel model = Active.getActiveDisplayModel();

		if(!lsm.isSelectionEmpty() && !isAdjusting){

			int[] selection = table.getSelectedRows();

			for (int i = 0; i < selection.length; i++) {
				selection[i] = table.convertRowIndexToModel(selection[i]);
			}

			if((boolean) table.getValueAt(min, 0)) {
				model.partialRemoval(selection, table);
			}
			else {
				model.addRowInterval(selection, table);
			}
			
			lsm.clearSelection();
			table.synchronizeHeader();
			Active.getActiveDisplayTable().synchronizeHeader();
		}
	}
}
