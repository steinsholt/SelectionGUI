package com.sff.report_performance;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.table.TableColumn;

import com.sff.report_performance.GUI.Active;
import com.sff.report_performance.GUI.State;

public class EnableSelectionAction extends AbstractAction{
	private static final long serialVersionUID = 1L;
	private GUI gui;

	public EnableSelectionAction(GUI gui){
		this.gui = gui;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		boolean isCustomerEnabled = Active.getState() == State.CLIENT;
		boolean isProjectEnabled = Active.getState() == State.PROJECT;
		boolean isStatusEnabled = Active.getState() == State.CATEGORY;
		
		if(isCustomerEnabled)gui.getSelectionHeadline().setText("Select Customers");
		else if(isProjectEnabled)gui.getSelectionHeadline().setText("Select Projects");
		else gui.getSelectionHeadline().setText("Select Category");
		
		if(isStatusEnabled)gui.getNameLabel().setVisible(false);
		else gui.getNameLabel().setVisible(true);
		
		if(isCustomerEnabled)gui.getNameLabel().setText("Customer Name");
		else gui.getNameLabel().setText("Project Name");
		
		if(isCustomerEnabled)gui.getIdLabel().setVisible(true);
		else gui.getIdLabel().setVisible(false);
		
		if(isStatusEnabled)gui.getNameField().setVisible(false);
		else gui.getNameField().setVisible(true);
		
		if(isCustomerEnabled)gui.getIdField().setVisible(true);
		else gui.getIdField().setVisible(false);
		
		if(isStatusEnabled)gui.getSearchButton().setVisible(false);
		else gui.getSearchButton().setVisible(true);

		gui.getSelectionTable().setModel(Active.getActiveSelectModel());
		TableColumn tc = gui.configureTableColumns(gui.getSelectionTable());		
		tc.setHeaderRenderer(gui.getHeader());

		if(!isCustomerEnabled)gui.getCustomerTable().disable();
		if(!isProjectEnabled)gui.getProjectTable().disable();
		if(!isStatusEnabled)gui.getStatusTable().disable();
		
		Active.getActiveDisplayTable().enable();
		
		gui.getNameField().setText("");
		gui.getNameField().requestFocusInWindow();
		
		gui.getIdField().setText("");

		if(isCustomerEnabled)gui.getCustomersButton().setSelected(true);
		else gui.getCustomersButton().setSelected(false);
		if(isProjectEnabled)gui.getProjectsButton().setSelected(true);
		else gui.getProjectsButton().setSelected(false);
		if(isStatusEnabled)gui.getStatusesButton().setSelected(true);
		else gui.getStatusesButton().setSelected(false);
		
		gui.getSelectionTable().synchronizeHeader();
	}
}
