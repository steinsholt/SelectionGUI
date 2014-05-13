package com.sff.report_performance;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.table.TableColumn;

import com.sff.report_performance.GUI.Active;
import com.sff.report_performance.GUI.State;

public class NextStageAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private GUI gui;
	private Color lighterGray;
	private Color darkerGray;
	
	public NextStageAction(GUI gui, Color lighterGray, Color darkerGray){
		this.gui = gui;
		this.lighterGray = lighterGray;
		this.darkerGray = darkerGray;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		State state = Active.setNextState();
		
		switch(state){
		case FRAME:
			break;
		case PROJECT:
			gui.getPreviousStepButton().setEnabled(true);
			gui.getSelectionHeadline().setText("Select Projects");
			gui.getNameLabel().setText("Project Name");
			gui.getProjectTable().setEnabled(true);
			gui.getFrameAgrField().setBackground(lighterGray);
			gui.getFrameAgrField().setDisabledTextColor(darkerGray);
			gui.getProjectsButton().setForeground(Color.black);
			gui.getFrameAgrButton().setForeground(Color.lightGray);
			
			gui.getScrollPane().setViewportView(gui.getIntervalSelectionTable());
			gui.getIntervalSelectionTable().setModel(Active.getActiveSelectModel());
			TableColumn tc = gui.configureTableColumns(gui.getIntervalSelectionTable());
			tc.setHeaderRenderer(gui.getHeader());
			
			break;
		case CLIENT:
			gui.getSelectionHeadline().setText("Select Clients");
			gui.getNextStepButton().setEnabled(true);
			gui.getNameLabel().setText("Client Name");
			gui.getIdLabel().setVisible(true);
			gui.getIdField().setVisible(true);
			gui.getProjectTable().setEnabled(false);
			gui.getClientTable().setEnabled(true);
			gui.getProjectsButton().setForeground(Color.lightGray);
			gui.getClientsButton().setForeground(Color.black);
			
			gui.getIntervalSelectionTable().setModel(Active.getActiveSelectModel());
			tc = gui.configureTableColumns(gui.getIntervalSelectionTable());
			tc.setHeaderRenderer(gui.getHeader());
			break;
		case CATEGORY:
			gui.getNextStepButton().setEnabled(false);
			gui.getSelectionHeadline().setText("Select Category");
			gui.getNextStepButton().setEnabled(false);
			gui.getNameLabel().setText("Category");
			gui.getIdLabel().setVisible(false);
			gui.getIdField().setVisible(false);
			gui.getClientTable().setEnabled(false);
			gui.getCategoryField().setBackground(Color.white);
			gui.getCategoryField().setDisabledTextColor(Color.black);
			gui.getClientsButton().setForeground(Color.lightGray);
			gui.getCategoryButton().setForeground(Color.black);
			
			gui.getSingleSelectionTable().setModel(Active.getActiveSimpleSelectModel());
			gui.getScrollPane().setViewportView(gui.getSingleSelectionTable());
			
			break;
		}
	}
}

////gui.getSelectionTable().setModel(Active.getActiveSelectModel());
////TableColumn tc = gui.configureTableColumns(gui.getSelectionTable());		
////tc.setHeaderRenderer(gui.getHeader());
