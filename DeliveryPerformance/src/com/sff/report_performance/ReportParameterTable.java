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
	private Color lighterGray;
	private Color darkerGray;
	
	public ReportParameterTable(PartialSelectionModel partialSelectionModel, NullSelectionModel nullSelectionModel, MyTableModel model, Color lighterGray, Color darkerGray){
		super(model);
		this.partialSelectionModel = partialSelectionModel;
		this.nullSelectionModel = nullSelectionModel;
		this.lighterGray = lighterGray;
		this.darkerGray = darkerGray;
	}
	
	public PartialSelectionModel getPartialSelectionModel() {
		return partialSelectionModel;
	}

	@Override
	public void disable(){
		this.setBackground(lighterGray);
		this.setForeground(darkerGray);
		this.setSelectionModel(nullSelectionModel);
		((JViewport)this.getParent()).setBackground(lighterGray);
		this.getTableHeader().setForeground(darkerGray);
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
	
	public void synchronizeHeader(){
		TableColumn column = this.getColumnModel().getColumn(0);
		CheckBoxHeader checkBoxHeader = (CheckBoxHeader) column.getHeaderRenderer();
		if(this.getRowCount() > 0){
			checkBoxHeader.setEnabled(true);
			checkBoxHeader.setSelected(false);
			column.setHeaderValue("None");
			this.getParent().getParent().repaint();
		}else{
			checkBoxHeader.setEnabled(false);
			checkBoxHeader.setSelected(true);
			column.setHeaderValue("All");
			this.getParent().getParent().repaint();
		}
	}
}
