package com.sff.report_performance;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
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

import net.miginfocom.swing.MigLayout;

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
	private JButton previousStepButton;
	private JButton nextStepButton;
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
		
		backgroundPanel = new JPanel(new MigLayout("fill, width :800:, height :700:, flowy"));
		frame.getContentPane().add(backgroundPanel);

		JPanel buttonPanel = createButtonPanel(headline, subheadline);
		JPanel helpPanel = createHelpPanel();
		JScrollPane scrollPane = createSelectionTable();
		JPanel displayPanel = createDisplayPanel();
		JPanel navigationPanel = createNavigationPanel();
		JPanel reportPanel = createReportPanel();
		
		backgroundPanel.add(buttonPanel, "growx, spany 2");
		backgroundPanel.add(scrollPane, "grow, spany 3");
		backgroundPanel.add(navigationPanel, "growx, wrap"); 
		backgroundPanel.add(helpPanel, "growx");
		backgroundPanel.add(displayPanel, "grow, spany 4");
		backgroundPanel.add(reportPanel, "growx");

		selectionTable.setAutoCreateRowSorter(true);
		frame.pack();
	}
	
	private JPanel createDisplayPanel() {
		
		displayPanel = new JPanel(new MigLayout("fill"));
		displayPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		frameAgrLabel = new JLabel("Frame Agreement");
		displayPanel.add(frameAgrLabel, "split, center");
		
		frameAgrField = new JTextField();
		frameAgrField.setEnabled(false);
		frameAgrField.setBackground(Color.white);
		displayPanel.add(frameAgrField, "wrap, width :180:");
		
		scrollPaneProjects = new JScrollPane();
		displayPanel.add(scrollPaneProjects, "wrap, grow");
		
		scrollPaneCustomers = new JScrollPane();
		displayPanel.add(scrollPaneCustomers, "wrap, grow");
		
		categoryLabel = new JLabel("Category");
		displayPanel.add(categoryLabel, "split, center");
		
		categoryField = new JTextField();
		categoryField.setEnabled(false);
		categoryField.setBackground(Color.lightGray);
		displayPanel.add(categoryField, "width :180:");
		
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
		
		
		
		return displayPanel;
	}

	private JPanel createReportPanel() {
		reportPanel = new JPanel(new MigLayout("fillx, insets 0", "push[]"));
		generateReportButton = new JButton();
		reportPanel.add(generateReportButton);

//		generateReportButton.setAction(new GenerateReportAction(reportParameterClientModel.getRowData(), reportParameterProjectModel.getRowData(), reportParameterCategoryModel.getRowData(), frame));
		generateReportButton.setForeground(Color.blue);
		generateReportButton.setFont(bold);
		generateReportButton.setText("Generate Report");
		
		return reportPanel;
	}

	private JPanel createHelpPanel() {
		helpPanel = new JPanel(new MigLayout("fillx"));
		
		reportParameterLabel = new JLabel("Report Parameters");
		reportParameterLabel.setFont(new Font("Serif", Font.PLAIN, 24));
		helpPanel.add(reportParameterLabel, "center, push");
		helpButton = new JButton("Help");
		helpPanel.add(helpButton);
		
		return helpPanel;
	}

	private JPanel createButtonPanel(Font headline, Font subheadline) {
		buttonPanel = new JPanel(new MigLayout("fillx, insets 10 10 10 0"));
		buttonPanel.setBackground(Color.white);

		selectionHeadlineLabel = new JLabel("Select Frame Agreement");
		selectionHeadlineLabel.setFont(headline);
		buttonPanel.add(selectionHeadlineLabel, "span 2, wrap");

		idLabel = new JLabel("Customer ID");
		buttonPanel.add(idLabel, "split, left, gapright 40, width :110:");
		idLabel.setFont(subheadline);
		idLabel.setVisible(false);
		
		PlainDocument doc = MyDocumentFilter.createDocumentFilter();
		idField = new JTextField();
		idField.setDocument(doc);
		idField.setVisible(false);
		buttonPanel.add(idField, "wrap, width :183:");
		
		nameLabel = new JLabel("Customer Name");
		buttonPanel.add(nameLabel, "split, left, gapright 40, width :110:");
		nameLabel.setFont(subheadline);
		
		nameField = new JTextField();
		buttonPanel.add(nameField, "wrap, width :183:");

		toolBar = new JToolBar();
		buttonPanel.add(toolBar, "gaptop 20, span 2, push, width :325:, split, left");
		toolBar.setLayout(new GridLayout());
		toolBar.setFloatable(false);

		frameAgrButton = new JButton();
		frameAgrButton.setBorder(BorderFactory.createSoftBevelBorder(0));
		frameAgrButton.setEnabled(false);
		frameAgrButton.setForeground(Color.black);
		
		clientButton = new JButton();
		clientButton.setBorder(BorderFactory.createSoftBevelBorder(0));
		clientButton.setEnabled(false);

		projectButton = new JButton();
		projectButton.setBorder(BorderFactory.createSoftBevelBorder(0));
		projectButton.setEnabled(false);

		categoryButton = new JButton();
		categoryButton.setBorder(BorderFactory.createSoftBevelBorder(0));
		categoryButton.setEnabled(false);
		
		clientButton.setText("Clients");
		projectButton.setText("Projects");
		categoryButton.setText("Category");
		frameAgrButton.setText("Frame Agr.");
		
		toolBar.add(frameAgrButton);
		toolBar.add(projectButton);
		toolBar.add(clientButton);
		toolBar.add(categoryButton);

		searchButton = new JButton("Search");
		buttonPanel.add(searchButton, "gaptop 20, gapright 5, gapleft 20");
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
		return buttonPanel;
	}

	private JPanel createNavigationPanel() {
		navigationPanel = new JPanel(new MigLayout("fillx, insets 0"));
		previousStepButton = new JButton();
		previousStepButton.setAction(new PreviousStateAction(this));
		previousStepButton.setEnabled(false);
		previousStepButton.setText("Back");
		nextStepButton = new JButton();
		nextStepButton.setAction(new NextStageAction(this));
		nextStepButton.setText("Next");
		navigationPanel.add(previousStepButton, "split, right");
		navigationPanel.add(nextStepButton);
		
		return navigationPanel;
	}

	private JScrollPane createSelectionTable() {
		selectionPanel = new JPanel();
		selectionPanel.setBackground(Color.white);
		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		selectionPanel.add(scrollPane);

		selectionTable = new SelectionTable(selectClientModel);
		selectionTable.setName("selection");
		selectionTable.getSelectionModel().addListSelectionListener(new SelectionTableListSelectionListener(selectionTable));
		TableColumn tc = configureTableColumns(selectionTable);
		header = new CheckBoxHeader(new SelectionTableHeaderListener(selectionTable));
		tc.setHeaderRenderer(header);
		scrollPane.setViewportView(selectionTable);
		scrollPane.getViewport().setBackground(Color.white);
		
		return scrollPane;
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
		int singleSelection = DefaultListSelectionModel.SINGLE_SELECTION;
		
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
		
		//TODO: Switch between selectionTable and JTable in the view port?
		// No need to create other listeners, simply use standard JTable with SINGLE_SELECTION
		// Move all logic into the actions
		public static State setNextState(){
			switch(activeState){
			case FRAME:
				display = reportParameterProjectModel;
				select = selectProjectModel;
				table = projectTable;
				activeState = State.PROJECT;
				return State.PROJECT;
			case PROJECT:
				display = reportParameterClientModel;
				select = selectClientModel;
				table = clientTable;
				activeState = State.CLIENT;
				return State.CLIENT;
			case CLIENT:
				
				select = selectCategoryModel;
				activeState = State.CATEGORY;
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
				activeState = State.FRAME;
				return State.FRAME;
			case CLIENT:
				display = reportParameterProjectModel;
				select = selectProjectModel;
				table = projectTable;
				activeState = State.PROJECT;
				return State.PROJECT;
			case CATEGORY:
				display = reportParameterClientModel;
				select = selectClientModel;
				table = clientTable;
				activeState = State.CLIENT;
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
	public ReportParameterTable getClientTable() {
		return clientTable;
	}
	public ReportParameterTable getProjectTable() {
		return projectTable;
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
	public MyTableModel getSelectCategoryModel() {
		return selectCategoryModel;
	}
	public JButton getClientsButton() {
		return clientButton;
	}
	public JButton getProjectsButton() {
		return projectButton;
	}
	public JButton getCategoryButton() {
		return categoryButton;
	}
	public JButton getFrameAgrButton(){
		return frameAgrButton;
	}
	public JLabel getSelectionHeadline() {
		return selectionHeadlineLabel;
	}	
	public TableCellRenderer getHeader() {
		return header;
	}
	public JButton getPreviousStepButton() {
		return previousStepButton;
	}
	public JButton getNextStepButton() {
		return nextStepButton;
	}
	public JTextField getCategoryField() {
		return categoryField;
	}
	public JTextField getFrameAgrField() {
		return frameAgrField;
	}
	public JToolBar getToolBar() {
		return toolBar;
	}
}
