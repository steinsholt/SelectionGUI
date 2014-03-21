package com.sff.report_performance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.PlainDocument;

public class GUI {

	private JFrame frame;
	private JTextField idField;
	private JTextField nameField;
	private JTextField categoryField;
	private JTextField frameAgrField;
	private JLabel idLabel;
	private JLabel nameLabel;
	private JLabel frameAgrLabel;
	private JLabel categoryLabel;
	private JLabel selectionHeadlineLabel;
	private JLabel reportParameterLabel;
	private JScrollPane scrollPane;
	private JScrollPane scrollPaneCustomers;
	private JScrollPane scrollPaneProjects;
	private SelectionTable selectionTable;
	private static ReportParameterTable clientTable;
	private static ReportParameterTable projectTable;
	private static ReportParameterTable categoryTable;
	private static MyTableModel reportParameterClientModel;
	private static MyTableModel reportParameterProjectModel;
	private static MyTableModel selectClientModel;
	private static MyTableModel selectProjectModel;
	private static MyTableModel selectCategoryModel;
	private static MyTableModel selecFrameAgrModel;
	private NullSelectionModel nullSelectionModel;
	private JToolBar toolBar;
	private JButton generateReportButton;
	private JButton helpButton;
	private JButton frameAgrButton;
	private JButton clientButton;
	private JButton projectButton;
	private JButton categoryButton;
	private JButton searchButton;
	private CheckBoxHeader header;
	private DatabaseConnection databaseConnection;
	private List<String> clientColumnNames;
	private List<String> projectColumnNames;
	private List<String> categoryColumnNames;
	private List<String> frameAgrColumnNames;
	private JPanel backgroundPanel;;
	private JPanel selectionPanel;
	private JPanel displayPanel;
	private JPanel buttonPanel;
	private JPanel reportPanel;
	private JPanel navigationPanel;
	private JPanel helpPanel;
	private Font bold;

	public JFrame getFrame() {
		return frame;
	}

	public GUI() {
		initialize();
	}

	private void initialize() {
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		clientColumnNames = new ArrayList<String>();
		clientColumnNames.add("");
		clientColumnNames.add("ID");
		clientColumnNames.add("Clients");

		projectColumnNames = new ArrayList<String>();
		projectColumnNames.add("");
		projectColumnNames.add("Projects");

		categoryColumnNames = new ArrayList<String>();
		categoryColumnNames.add("");
		categoryColumnNames.add("Categories");
		
		frameAgrColumnNames = new ArrayList<String>();
		frameAgrColumnNames.add("");
		frameAgrColumnNames.add("Frame Agreements");

		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 900);
		frame.setMinimumSize(new Dimension(700, 600));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setBackground(Color.white);
		
		bold = new Font("Serif", Font.BOLD, 12);
		Font headline = new Font("Serif", Font.PLAIN, 24);
		Font subheadline = new Font("Serif", Font.PLAIN, 16);

		selectClientModel = new MyTableModel(clientColumnNames);
		selectProjectModel = new MyTableModel(projectColumnNames);
		selectCategoryModel = new MyTableModel(categoryColumnNames);
		selecFrameAgrModel = new MyTableModel(frameAgrColumnNames);

		reportParameterProjectModel = new MyTableModel(projectColumnNames);
		reportParameterClientModel = new MyTableModel(clientColumnNames);
		
		backgroundPanel = new JPanel(new GridBagLayout());
		frame.getContentPane().add(backgroundPanel);

		GridBagConstraints constraints = new GridBagConstraints();
		
		createSelectionTable(constraints);
		createNavigationPanel(constraints);
		createButtonPanel(headline, subheadline, constraints);
		createHelpPanel(constraints);
		createReportPanel(constraints);
		createDisplayPanel(constraints);

		selectionTable.setAutoCreateRowSorter(true);
		frame.pack();
	}

	private void createDisplayPanel(GridBagConstraints constraints) {
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		constraints.weighty = 1;
		displayPanel = new JPanel(new GridBagLayout());
		backgroundPanel.add(displayPanel, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		scrollPaneProjects = new JScrollPane();
		displayPanel.add(scrollPaneProjects, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		scrollPaneCustomers = new JScrollPane();
		displayPanel.add(scrollPaneCustomers, constraints);
		constraints.gridwidth = 1;
		
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		frameAgrLabel = new JLabel("Frame Agreement");
		displayPanel.add(frameAgrLabel, constraints);
		
		constraints.gridx = 1;
		constraints.gridy = 0;
		frameAgrField = new JTextField();
		frameAgrField.setColumns(15);
		displayPanel.add(frameAgrField, constraints);
		
		constraints.gridx = 1;
		constraints.gridy = 3;
		categoryField = new JTextField();
		categoryField.setColumns(15);
		displayPanel.add(categoryField, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 3;
		categoryLabel = new JLabel("Category");
		displayPanel.add(categoryLabel, constraints);
		
		nullSelectionModel = new NullSelectionModel();
		PartialSelectionModel projectSelectionModel = new PartialSelectionModel();
		projectTable = new ReportParameterTable(projectSelectionModel, nullSelectionModel,reportParameterProjectModel);
		projectSelectionModel.addListSelectionListener(new ReportParameterTableListSelectionListener(projectTable,selectionTable));
		projectTable.setName("projects");
		configureTableColumns(projectTable);
		scrollPaneProjects.setViewportView(projectTable);
		projectTable.disable();

		PartialSelectionModel customerSelectionModel = new PartialSelectionModel();
		clientTable = new ReportParameterTable(customerSelectionModel, nullSelectionModel, reportParameterClientModel);
		customerSelectionModel.addListSelectionListener(new ReportParameterTableListSelectionListener(clientTable,selectionTable));
		clientTable.setName("clients");
		configureTableColumns(clientTable);
		clientTable.getColumnModel().getColumn(1).setMaxWidth(50);
		scrollPaneCustomers.setViewportView(clientTable);
		clientTable.disable();
	}

	private void createReportPanel(GridBagConstraints constraints) {
		constraints.gridx = 1;
		constraints.gridy = 2;
		reportPanel = new JPanel();
		backgroundPanel.add(reportPanel, constraints);
		generateReportButton = new JButton();
		reportPanel.add(generateReportButton);

//		generateReportButton.setAction(new GenerateReportAction(reportParameterClientModel.getRowData(), reportParameterProjectModel.getRowData(), reportParameterCategoryModel.getRowData(), frame));
		generateReportButton.setForeground(Color.blue);
		generateReportButton.setFont(bold);
		generateReportButton.setText("Generate Report");
	}

	private void createHelpPanel(GridBagConstraints constraints) {
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.weightx = 0.5;
		constraints.weighty = 0;
		helpPanel = new JPanel();
		backgroundPanel.add(helpPanel,constraints);
		helpButton = new JButton("Help");
		helpPanel.add(helpButton);
		reportParameterLabel = new JLabel("Report Parameters");
		reportParameterLabel.setFont(new Font("Serif", Font.PLAIN, 24));
		helpPanel.add(reportParameterLabel);
	}

	private void createButtonPanel(Font headline, Font subheadline, GridBagConstraints constraints) {
		constraints.gridx = 0;
		constraints.gridy = 0;
		buttonPanel = new JPanel(new GridBagLayout());
		backgroundPanel.add(buttonPanel, constraints);
		buttonPanel.setBackground(Color.white);

		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.NONE;
		selectionHeadlineLabel = new JLabel("Select Frame Agreement");
		selectionHeadlineLabel.setFont(headline);
		buttonPanel.add(selectionHeadlineLabel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		idLabel = new JLabel("Customer ID");
		buttonPanel.add(idLabel, constraints);
		idLabel.setFont(subheadline);
		
		constraints.gridx = 0;
		constraints.gridy = 2;
		nameLabel = new JLabel("Customer Name");
		buttonPanel.add(nameLabel, constraints);
		nameLabel.setFont(subheadline);

		constraints.gridx = 1;
		constraints.gridy = 1;
		PlainDocument doc = MyDocumentFilter.createDocumentFilter();
		idField = new JTextField();
		idField.setColumns(15);
		idField.setDocument(doc);
		buttonPanel.add(idField, constraints);
		
		constraints.gridx = 1;
		constraints.gridy = 2;
		nameField = new JTextField();
		nameField.setColumns(15);
		buttonPanel.add(nameField, constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		toolBar = new JToolBar();
		buttonPanel.add(toolBar, constraints);
		toolBar.setLayout(new GridLayout());
		toolBar.setFloatable(false);

		frameAgrButton = new JButton();
		frameAgrButton.setBorder(BorderFactory.createSoftBevelBorder(0));
		
		clientButton = new JButton();
		clientButton.setBorder(BorderFactory.createSoftBevelBorder(0));

		projectButton = new JButton();
		projectButton.setBorder(BorderFactory.createSoftBevelBorder(0));

		categoryButton = new JButton();
		categoryButton.setBorder(BorderFactory.createSoftBevelBorder(0));
		
		clientButton.setText("Clients");
		projectButton.setText("Projects");
		categoryButton.setText("Category");
		frameAgrButton.setText("Frame Agr.");
		
		toolBar.add(frameAgrButton);
		toolBar.add(projectButton);
		toolBar.add(clientButton);
		toolBar.add(categoryButton);

		constraints.gridx = 1;
		constraints.gridy = 4;
		searchButton = new JButton("Search");
		buttonPanel.add(searchButton, constraints);
		searchButton.setForeground(Color.blue);
		searchButton.setFont(bold);
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DatabaseSearch.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), databaseConnection, nameField, idField);
				selectionTable.synchronizeHeader();
			}
		});
		nameField.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					DatabaseSearch.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), databaseConnection, nameField, idField);
					selectionTable.synchronizeHeader();
				}
			}
		});
		idField.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					DatabaseSearch.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), databaseConnection, nameField, idField);
					selectionTable.synchronizeHeader();
				}
			}
		});
	}

	private void createNavigationPanel(GridBagConstraints constraints) {
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 0.5;
		constraints.weighty = 0;
		navigationPanel = new JPanel();
		backgroundPanel.add(navigationPanel, constraints); 
		JButton previousStepButton = new JButton("Previous");
		navigationPanel.add(previousStepButton);
		JButton nextStepButton = new JButton("Next");
		navigationPanel.add(nextStepButton);
	}

	private void createSelectionTable(GridBagConstraints constraints) {
		constraints.insets = new Insets(2,2,2,2);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0.5;
		constraints.weighty = 1;
		selectionPanel = new JPanel();
		selectionPanel.setBackground(Color.white);
		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		selectionPanel.add(scrollPane);
		backgroundPanel.add(scrollPane, constraints);

		selectionTable = new SelectionTable(selectClientModel);
		selectionTable.setName("selection");
		selectionTable.getSelectionModel().addListSelectionListener(new SelectionTableListSelectionListener(selectionTable));
		TableColumn tc = configureTableColumns(selectionTable);
		header = new CheckBoxHeader(new SelectionTableHeaderListener(selectionTable));
		tc.setHeaderRenderer(header);
		scrollPane.setViewportView(selectionTable);
		scrollPane.getViewport().setBackground(Color.white);
	}

	public TableColumn configureTableColumns(JTable table) {
		if(table.getColumnCount()==3) selectionTable.getColumnModel().getColumn(1).setMaxWidth(50);
		table.getColumnModel().getColumn(0).setMinWidth(80); 
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		TableColumn tc = table.getColumnModel().getColumn(0);
		tc.setHeaderValue("Select All");
		if(table.getName().equals("selection")){
			tc.setCellEditor(table.getDefaultEditor(Boolean.class));
			tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		}else{
			tc.setCellRenderer(table.getDefaultRenderer(Icon.class));
			CheckBoxHeader checkboxHeader = new CheckBoxHeader(new ReportParameterTableHeaderListener(selectionTable));
			checkboxHeader.setEnabled(false);
			checkboxHeader.setSelected(true);
			tc.setHeaderRenderer(checkboxHeader);
		}
		return tc;
	}

	public enum State {
		FRAME, CLIENT, PROJECT, CATEGORY;
		public static final EnumSet<State> enumList = EnumSet.allOf(State.class);
		
		}
	public static class Active{

		private static MyTableModel display;
		private static MyTableModel select;
		private static ReportParameterTable table;
		private static State activeState = State.FRAME;
		
		public static MyTableModel getActiveDisplayModel(){
			return display;
		}

		public static MyTableModel getActiveSelectModel(){
			return select;
		}

		public static ReportParameterTable getActiveDisplayTable(){
			return table;
		}

		public static State getState(){
			return activeState;
		}
		
		public static State setNextState(){
			switch(activeState){
			case FRAME:
				display = reportParameterProjectModel;
				select = selectProjectModel;
				table = projectTable;
				return State.PROJECT;
			case PROJECT:
				display = reportParameterClientModel;
				select = selectClientModel;
				table = clientTable;
				return State.CLIENT;
			case CLIENT:
				
				return State.CATEGORY;
			case CATEGORY:
				return activeState;
			}
			return activeState;
		}
		
		public static State setPreviousState(){
			switch(activeState){
			case FRAME:
				return activeState;
			case PROJECT:
				
				return State.FRAME;
			case CLIENT:
				display = reportParameterProjectModel;
				select = selectProjectModel;
				table = projectTable;
				return State.PROJECT;
			case CATEGORY:
				display = reportParameterClientModel;
				select = selectClientModel;
				table = clientTable;
				return State.CLIENT;
			}
			return activeState;
		}

//		public static void setState(State state){
//			switch (state){
//			case CLIENT:
//				display = reportParameterClientModel;
//				select = selectClientModel;
//				table = clientTable;
//				activeState = state;
//				break;
//			case PROJECT:
//				display = reportParameterProjectModel;
//				select = selectProjectModel;
//				table = projectTable;
//				activeState = state;
//				break;
//			case CATEGORY:
//				display = reportParameterCategoryModel;
//				select = selectCategoryModel;
//				table = categoryTable;
//				activeState = state;
//				break;
//			case FRAME:
//				
//				break;
//			}
//		}
	}

	public JTextField getIdField() {
		return idField;
	}
	public JTextField getNameField() {
		return nameField;
	}
	public JLabel getIdLabel() {
		return idLabel;
	}
	public JLabel getNameLabel() {
		return nameLabel;
	}
	public JButton getSearchButton() {
		return searchButton;
	}
	public SelectionTable getSelectionTable() {
		return selectionTable;
	}
	public ReportParameterTable getCustomerTable() {
		return clientTable;
	}
	public ReportParameterTable getProjectTable() {
		return projectTable;
	}
	public ReportParameterTable getStatusTable() {
		return categoryTable;
	}
	public MyTableModel getReportParameterCustomerModel() {
		return reportParameterClientModel;
	}
	public MyTableModel getReportParameterProjectModel() {
		return reportParameterProjectModel;
	}
	public MyTableModel getSelectCustomerModel() {
		return selectClientModel;
	}
	public MyTableModel getSelectProjectModel() {
		return selectProjectModel;
	}
	public MyTableModel getSelectStatusModel() {
		return selectCategoryModel;
	}
	public JButton getCustomersButton() {
		return clientButton;
	}
	public JButton getProjectsButton() {
		return projectButton;
	}
	public JButton getStatusesButton() {
		return categoryButton;
	}
	public JLabel getSelectionHeadline() {
		return selectionHeadlineLabel;
	}	
	public TableCellRenderer getHeader() {
		return header;
	}
}
