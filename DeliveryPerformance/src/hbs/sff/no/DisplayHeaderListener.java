package hbs.sff.no;

import hbs.sff.no.GUI.Active;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JTable;

public class DisplayHeaderListener implements ItemListener{
	
	private GUI gui;
	private JTable selection;
	
	public DisplayHeaderListener(GUI gui, JTable selection){
		this.gui = gui;
		this.selection = selection;
	}
	
	public void itemStateChanged(ItemEvent e){
		SelectionTableModel model = Active.getActiveDisplayModel();
		if(e.getStateChange() == ItemEvent.SELECTED && model != null){
			JTable display = Active.getActiveDisplayTable();
			if(model.getRowData().isEmpty()){
				((CheckBoxHeader) display.getColumnModel().getColumn(0).getHeaderRenderer()).setSelected(true);
			}
			else model.removeRowInterval(0, model.getRowData().size() - 1, selection);
			gui.synchDisplayHeaders(Active.getActiveDisplayTable());
		}
	}
}
