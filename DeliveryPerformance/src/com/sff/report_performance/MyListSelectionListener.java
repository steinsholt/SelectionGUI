package com.sff.report_performance;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sff.report_performance.GUI.Active;

public class MyListSelectionListener implements ListSelectionListener{
	
	private SelectionTable table;
	
	public MyListSelectionListener(SelectionTable table){
		this.table = table;
	}
	
	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		int min = lsm.getMinSelectionIndex();
		int max = lsm.getMaxSelectionIndex();
		boolean isAdjusting = e.getValueIsAdjusting();
		MyTableModel model = Active.getActiveDisplayModel();

		if(!lsm.isSelectionEmpty() && !isAdjusting){
			if(e.getSource() == table.getSelectionModel()){
				if((boolean) table.getValueAt(min, 0)) model.partialRemoval(min, max, table);
				else model.addRowInterval(min, max, table);
			}
			else if(e.getSource() == Active.getActiveDisplayTable().getSelectionModel()){
				model.removeRowInterval(min, max, table);
			}
			lsm.clearSelection();
			table.synchronizeHeader();
			Active.getActiveDisplayTable().SynchronizeHeader();
		}
	}
}
