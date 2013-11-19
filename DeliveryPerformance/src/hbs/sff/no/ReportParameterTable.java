package hbs.sff.no;

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.JViewport;

public class ReportParameterTable extends JTable {
	private static final long serialVersionUID = 1L;
	private PartialSelectionModel partialSelectionModel;
	private NullSelectionModel nullSelectionModel;
	
	public ReportParameterTable(PartialSelectionModel partialSelectionModel, NullSelectionModel nullSelectionModel, SelectionTableModel model){
		this.partialSelectionModel = partialSelectionModel;
		this.nullSelectionModel = nullSelectionModel;
		this.setModel(model);
	}
	
	public void disable(){
		this.setBackground(Color.lightGray);
		this.setForeground(Color.gray);
		this.setSelectionModel(nullSelectionModel);
		((JViewport)this.getParent()).setBackground(Color.lightGray);
	}
	public void enable(){
		this.setBackground(Color.white);
		this.setForeground(Color.black);
		this.setSelectionModel(partialSelectionModel);
		((JViewport)this.getParent()).setBackground(Color.white);
	}
}
