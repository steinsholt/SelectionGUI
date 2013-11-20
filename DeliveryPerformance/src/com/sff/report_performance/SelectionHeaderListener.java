package com.sff.report_performance;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.JTable;

import com.sff.report_performance.GUI.Active;

public class SelectionHeaderListener implements ItemListener{
	
	private GUI gui;
	private JTable table;
	
	public SelectionHeaderListener(GUI gui, JTable table){
		this.gui = gui;
		this.table = table;
	}
	
	public void itemStateChanged(ItemEvent e){
		if(gui.isHeaderClick()){
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
			gui.synchDisplayHeaders(Active.getActiveDisplayTable());
		}
	}
}