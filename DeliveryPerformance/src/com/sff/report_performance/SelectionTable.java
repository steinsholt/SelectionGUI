package com.sff.report_performance;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class SelectionTable extends JTable {
	private static final long serialVersionUID = 1L;

	public SelectionTable(MyTableModel model){
		super(model);
	}
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
		Component c = super.prepareRenderer(renderer, row, column);
		Color color = (boolean) this.getModel().getValueAt(row, 0) ? Color.BLUE : Color.BLACK;
		c.setForeground(color);
		return c;
	}
}
