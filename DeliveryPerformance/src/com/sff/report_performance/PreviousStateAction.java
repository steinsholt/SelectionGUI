package com.sff.report_performance;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.table.TableColumn;

import com.sff.report_performance.GUI.Active;
import com.sff.report_performance.GUI.State;

public class PreviousStateAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private GUI gui;
	
	public PreviousStateAction(GUI gui){
		this.gui = gui;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		State state = Active.setPreviousState();
		
		switch(state){
		case FRAME:
			gui.getSelectionHeadline().setText("Select Frame Agreement");
			gui.getNameLabel().setText("Frame Agreement");
			gui.getPreviousStepButton().setEnabled(false);
			gui.getProjectTable().setEnabled(false);
			gui.getFrameAgrField().setBackground(Color.white);
			gui.getProjectsButton().setForeground(Color.lightGray);
			gui.getFrameAgrButton().setForeground(Color.black);
			
			gui.getScrollPane().setViewportView(gui.getSingleSelectionTable());
			break;
		case PROJECT:
			gui.getSelectionHeadline().setText("Select Projects");
			gui.getNameLabel().setText("Project Name");
			gui.getIdLabel().setVisible(false);
			gui.getIdField().setVisible(false);
			gui.getProjectTable().setEnabled(true);
			gui.getClientTable().setEnabled(false);
			gui.getClientsButton().setForeground(Color.lightGray);
			gui.getProjectsButton().setForeground(Color.black);
			
			gui.getIntervalSelectionTable().setModel(Active.getActiveSelectModel());
			TableColumn tc = gui.configureTableColumns(gui.getIntervalSelectionTable());
			tc.setHeaderRenderer(gui.getHeader());
			break;
		case CLIENT:
			gui.getNextStepButton().setEnabled(true);
			gui.getSelectionHeadline().setText("Select Clients");
			gui.getNameLabel().setText("Client Name");
			gui.getIdField().setVisible(true);
			gui.getIdLabel().setVisible(true);
			gui.getClientTable().setEnabled(true);
			gui.getCategoryField().setBackground(Color.lightGray); 
			gui.getClientsButton().setForeground(Color.black);
			gui.getCategoryButton().setForeground(Color.lightGray);
			
			gui.getScrollPane().setViewportView(gui.getIntervalSelectionTable());
			gui.getIntervalSelectionTable().setModel(Active.getActiveSelectModel());
			tc = gui.configureTableColumns(gui.getIntervalSelectionTable());
			tc.setHeaderRenderer(gui.getHeader());
			break;
		case CATEGORY:
			break;
		}
	}
}
