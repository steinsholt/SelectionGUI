package com.sff.report_performance;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;

public class ReportParameterTable extends JTable {
	private static final long serialVersionUID = 1L;
	private PartialSelectionModel partialSelectionModel;
	private NullSelectionModel nullSelectionModel;
	
	public ReportParameterTable(PartialSelectionModel partialSelectionModel, NullSelectionModel nullSelectionModel, MyTableModel model){
		super(model);
		this.partialSelectionModel = partialSelectionModel;
		this.nullSelectionModel = nullSelectionModel;
	}
	
	@Override
	public void disable(){
		this.setBackground(Color.lightGray);
		this.setForeground(Color.gray);
		this.setSelectionModel(nullSelectionModel);
		((JViewport)this.getParent()).setBackground(Color.lightGray);
		this.getTableHeader().setForeground(Color.gray);
		((JComponent) this.getColumnModel().getColumn(0).getHeaderRenderer()).setEnabled(false);
	}
	@Override
	public void enable(){
		this.setBackground(Color.white);
		this.setForeground(Color.black);
		this.setSelectionModel(partialSelectionModel);
		((JViewport)this.getParent()).setBackground(Color.white);
		this.getTableHeader().setForeground(Color.black);
		if(this.getRowCount() > 0) ((JComponent) this.getColumnModel().getColumn(0).getHeaderRenderer()).setEnabled(true);
	}
	
	public void SynchronizeHeader(){
		TableColumn column = this.getColumnModel().getColumn(0);
		CheckBoxHeader checkBoxHeader = (CheckBoxHeader) column.getHeaderRenderer();
		if(this.getRowCount() > 0){
			checkBoxHeader.setEnabled(true);
			checkBoxHeader.setSelected(false);
			column.setHeaderValue("Remove All");
			this.getParent().getParent().repaint();
		}else{
			checkBoxHeader.setEnabled(false);
			checkBoxHeader.setSelected(true);
			column.setHeaderValue("Select All");
			this.getParent().getParent().repaint();
		}
	}
}
