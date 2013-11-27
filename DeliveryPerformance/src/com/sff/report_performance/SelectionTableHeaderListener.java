package com.sff.report_performance;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;

import com.sff.report_performance.GUI.Active;

public class SelectionTableHeaderListener implements ItemListener{
	
	private SelectionTable table;
	
	public SelectionTableHeaderListener(SelectionTable table){
		this.table = table;
	}
	
	public void itemStateChanged(ItemEvent e){
		if(table.isHeaderClicked()){
			int min = 0;
			int max = table.getRowCount() - 1;

			if(e.getStateChange() == ItemEvent.SELECTED
					&& (e.getSource() instanceof AbstractButton)){
				Active.getActiveDisplayModel().addRowInterval(min, max, table);
			}
			else if(e.getStateChange() == ItemEvent.DESELECTED
					&& (e.getSource() instanceof AbstractButton)){
				Active.getActiveDisplayModel().partialRemoval(min, max, table);
			}
			Active.getActiveDisplayTable().SynchronizeHeader();
		}
	}
}