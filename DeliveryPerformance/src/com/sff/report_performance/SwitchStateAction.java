//package com.sff.report_performance;
//
//import java.awt.event.ActionEvent;
//
//import javax.swing.AbstractAction;
//import javax.swing.table.TableColumn;
//
//import com.sff.report_performance.GUI.Active;
//import com.sff.report_performance.GUI.State;
//
//public class SwitchStateAction extends AbstractAction{
//	private static final long serialVersionUID = 1L;
//	private GUI gui;
//
//	public SwitchStateAction(GUI gui){
//		this.gui = gui;
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent arg0) {
//
//		State state = Active.getState();
																						//		TODO: Remove class when finished with it
//		switch(state){
//		case FRAME:
//			gui.getSelectionHeadline().setText("Select Frame Agreement");
//			gui.getPreviousStepButton().setEnabled(false);
//			gui.getNameLabel().setVisible(false);
//			gui.getNameField().setVisible(false);
//			gui.getSearchButton().setVisible(false);
//			gui.getProjectTable().setEnabled(false);
//			
////			gui.getFrameAgrButton().setSelected(true);
////			gui.getFrameAgrButton().getFont().deriveFont(Font.BOLD);
//			break;
//		case PROJECT:
//			gui.getSelectionHeadline().setText("Select Projects");
//			gui.getPreviousStepButton().setEnabled(true);
//			gui.getNameLabel().setVisible(true);
//			gui.getNameLabel().setText("Project Name");
//			gui.getIdLabel().setVisible(false);
//			gui.getNameField().setVisible(true);
//			gui.getSearchButton().setVisible(true);
//			gui.getProjectTable().setEnabled(true);
//			gui.getClientTable().setEnabled(false);
//			break;
//		case CLIENT:
//			gui.getSelectionHeadline().setText("Select Clients");
//			gui.getNextStepButton().setEnabled(true);
//			gui.getNameLabel().setVisible(true);
//			gui.getNameLabel().setText("Client Name");
//			gui.getIdLabel().setVisible(true);
//			gui.getNameField().setVisible(true);
//			gui.getSearchButton().setVisible(true);
//			gui.getProjectTable().setEnabled(false);
//			gui.getClientTable().setEnabled(true);
//			break;
//		case CATEGORY:
//			gui.getSelectionHeadline().setText("Select Category");
//			gui.getNextStepButton().setEnabled(false);
//			gui.getNameLabel().setVisible(false);
//			gui.getIdLabel().setVisible(false);
//			gui.getNameField().setVisible(false);
//			gui.getSearchButton().setVisible(false);
//			gui.getClientTable().setEnabled(false);
//			break;
//		}
//		
////		gui.getSelectionTable().setModel(Active.getActiveSelectModel());
////		TableColumn tc = gui.configureTableColumns(gui.getSelectionTable());		
////		tc.setHeaderRenderer(gui.getHeader());
//		
//		
//
////		boolean isCustomerEnabled = Active.getState() == State.CLIENT;
////		boolean isProjectEnabled = Active.getState() == State.PROJECT;
////		boolean isStatusEnabled = Active.getState() == State.CATEGORY;
////
////		if(isCustomerEnabled)gui.getSelectionHeadline().setText("Select Customers");
////		else if(isProjectEnabled)gui.getSelectionHeadline().setText("Select Projects");
////		else gui.getSelectionHeadline().setText("Select Category");
////
////		if(isStatusEnabled)gui.getNameLabel().setVisible(false);
////		else gui.getNameLabel().setVisible(true);
////
////		if(isCustomerEnabled)gui.getNameLabel().setText("Customer Name");
////		else gui.getNameLabel().setText("Project Name");
////
////		if(isCustomerEnabled)gui.getIdLabel().setVisible(true);
////		else gui.getIdLabel().setVisible(false);
////
////		if(isStatusEnabled)gui.getNameField().setVisible(false);
////		else gui.getNameField().setVisible(true);
////
////		if(isCustomerEnabled)gui.getIdField().setVisible(true);
////		else gui.getIdField().setVisible(false);
////
////		if(isStatusEnabled)gui.getSearchButton().setVisible(false);
////		else gui.getSearchButton().setVisible(true);
////							
////		gui.getSelectionTable().setModel(Active.getActiveSelectModel());
////		TableColumn tc = gui.configureTableColumns(gui.getSelectionTable());		
////		tc.setHeaderRenderer(gui.getHeader());
////
////		if(!isCustomerEnabled)gui.getCustomerTable().disable();
////		if(!isProjectEnabled)gui.getProjectTable().disable();
////		if(!isStatusEnabled)gui.getCategoryTable().disable();
////
////		Active.getActiveDisplayTable().enable();
////
////		gui.getNameField().setText("");
////		gui.getNameField().requestFocusInWindow();
////
////		gui.getIdField().setText("");
////
////		if(isCustomerEnabled)gui.getCustomersButton().setSelected(true);
////		else gui.getCustomersButton().setSelected(false);
////		if(isProjectEnabled)gui.getProjectsButton().setSelected(true);
////		else gui.getProjectsButton().setSelected(false);
////		if(isStatusEnabled)gui.getCategoryButton().setSelected(true);
////		else gui.getCategoryButton().setSelected(false);
//
//		gui.getSelectionTable().synchronizeHeader();
//	}
//}
