package com.sff.report_performance;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sff.report_performance.GUI.Active;

public class SingleSelectionListener implements ListSelectionListener{
	private JTable table ;
	
	public SingleSelectionListener(JTable table){
		this.table = table;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		if(!lsm.isSelectionEmpty())Active.getDisplayField().setText((String) table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()));
	}
}
