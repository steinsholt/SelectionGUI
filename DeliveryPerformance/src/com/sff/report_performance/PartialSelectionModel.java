package com.sff.report_performance;

import javax.swing.DefaultListSelectionModel;

public class PartialSelectionModel extends DefaultListSelectionModel {
	private static final long serialVersionUID = 1L;
	public int getSelectionMode() { return SINGLE_INTERVAL_SELECTION; } 
}
