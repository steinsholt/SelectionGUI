package com.sff.report_performance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
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
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.PlainDocument;

public class GUI {

	private JFrame frame;
	private JButton generateReportButton;
	private JTextField idField;
	private JTextField nameField;
	private JLabel idLabel;
	private JLabel nameLabel;
	private JButton searchButton;
	private JScrollPane scrollPane;
	private SelectionTable selectionTable;
	private JScrollPane scrollPaneCustomers;
	private static ReportParameterTable customerTable;
	private JScrollPane scrollPaneProjects;
	private static ReportParameterTable projectTable;
	private JScrollPane scrollPaneStatuses;
	private static ReportParameterTable statusTable;
	private static MyTableModel reportParameterCustomerModel;
	private static MyTableModel reportParameterProjectModel;
	private static MyTableModel reportParameterStatusModel;
	private static MyTableModel selectCustomerModel;
	private static MyTableModel selectProjectModel;
	private static MyTableModel selectStatusModel;
	private NullSelectionModel nullSelectionModel;
	private JToolBar toolBar;
	private JPanel selectionPanel;
	private SpringLayout selectionPanelLayout;
	private JButton customerButton;
	private JButton projectButton;
	private JButton statusButton;
	private CheckBoxHeader header;
	private DatabaseConnection databaseConnection;
	private List<String> customerColumnNames;
	private List<String> projectColumnNames;
	private List<String> statusColumnNames;
	private SpringLayout displayPanelLayout;
	private JPanel displayPanel;
	private Font bold;
	private SpringLayout reportPerformancePanelLayout;
	private JButton button;
	private JLabel label;
	private JPanel buttonPanel;
	private JLabel selectionHeadline;
	private JPanel reportPanel;

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

		customerColumnNames = new ArrayList<String>();
		customerColumnNames.add("");
		customerColumnNames.add("ID");
		customerColumnNames.add("Customers");

		projectColumnNames = new ArrayList<String>();
		projectColumnNames.add("");
		projectColumnNames.add("Projects");

		statusColumnNames = new ArrayList<String>();
		statusColumnNames.add("");
		statusColumnNames.add("Item Statuses");

		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 900);
		frame.setMinimumSize(new Dimension(700,600));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		reportPerformancePanelLayout = new SpringLayout();
		frame.getContentPane().setLayout(reportPerformancePanelLayout);
		frame.getContentPane().setBackground(Color.white);
		
		nullSelectionModel = new NullSelectionModel();

		bold = new Font("Serif", Font.BOLD, 12);
		Font headline = new Font("Serif", Font.PLAIN, 24);
		Font subheadline = new Font("Serif", Font.PLAIN, 16);

		selectCustomerModel = new MyTableModel(customerColumnNames);
		selectProjectModel = new MyTableModel(projectColumnNames);
		selectStatusModel = new MyTableModel(statusColumnNames);

		reportParameterStatusModel = new MyTableModel(statusColumnNames);
		reportParameterProjectModel = new MyTableModel(projectColumnNames);
		reportParameterCustomerModel = new MyTableModel(customerColumnNames);

		selectionPanel = new JPanel();
		selectionPanel.setBackground(Color.white);
		frame.getContentPane().add(selectionPanel);

		selectionPanelLayout = new SpringLayout();
		selectionPanel.setLayout(selectionPanelLayout);
		scrollPane = new JScrollPane();
		selectionPanelLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, selectionPanel);
		selectionPanelLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, selectionPanel);
		reportPerformancePanelLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, selectionPanel);
		selectionPanelLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, selectionPanel);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		reportPerformancePanelLayout.putConstraint(SpringLayout.EAST, scrollPane, 454, SpringLayout.WEST, selectionPanel);
		selectionPanel.add(scrollPane);

		selectionTable = new SelectionTable(selectCustomerModel);
		selectionTable.setName("selection");
		selectionTable.getSelectionModel().addListSelectionListener(new SelectionTableListSelectionListener(selectionTable));
		TableColumn tc = configureTableColumns(selectionTable);
		header = new CheckBoxHeader(new SelectionTableHeaderListener(selectionTable));
		tc.setHeaderRenderer(header);

		scrollPane.setViewportView(selectionTable);
		scrollPane.getViewport().setBackground(Color.white);

		databaseConnection = new DatabaseConnection();
		databaseConnection.loadStatusData(selectStatusModel);

		final JPanel reportPerformancePanel = new JPanel();

		buttonPanel = new JPanel();
		selectionPanelLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, buttonPanel);
		reportPerformancePanelLayout.putConstraint(SpringLayout.EAST, buttonPanel, -22, SpringLayout.EAST, scrollPane);
		reportPerformancePanelLayout.putConstraint(SpringLayout.WEST, buttonPanel, 0, SpringLayout.WEST, selectionPanel);
		reportPerformancePanelLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 0, SpringLayout.NORTH, selectionPanel);
		selectionPanel.add(buttonPanel);
		buttonPanel.setPreferredSize(new Dimension(460,150));
		buttonPanel.setBackground(Color.white);
		SpringLayout sl_panel_5 = new SpringLayout();
		buttonPanel.setLayout(sl_panel_5);

		selectionHeadline = new JLabel("Select Customers");
		sl_panel_5.putConstraint(SpringLayout.NORTH, selectionHeadline, 10, SpringLayout.NORTH, buttonPanel);
		sl_panel_5.putConstraint(SpringLayout.WEST, selectionHeadline, 10, SpringLayout.WEST, buttonPanel);
		selectionHeadline.setFont(headline);
		buttonPanel.add(selectionHeadline);

		idLabel = new JLabel("Customer ID");
		sl_panel_5.putConstraint(SpringLayout.NORTH, idLabel, 6, SpringLayout.SOUTH, selectionHeadline);
		sl_panel_5.putConstraint(SpringLayout.WEST, idLabel, 0, SpringLayout.WEST, selectionHeadline);
		buttonPanel.add(idLabel);
		idLabel.setFont(subheadline);
		nameLabel = new JLabel("Customer Name");
		sl_panel_5.putConstraint(SpringLayout.WEST, nameLabel, 0, SpringLayout.WEST, selectionHeadline);
		buttonPanel.add(nameLabel);
		nameLabel.setFont(subheadline);

		PlainDocument doc = MyDocumentFilter.createDocumentFilter();
		idField = new JTextField();
		idField.setDocument(doc);
		sl_panel_5.putConstraint(SpringLayout.NORTH, idField, 9, SpringLayout.SOUTH, selectionHeadline);
		sl_panel_5.putConstraint(SpringLayout.WEST, idField, 63, SpringLayout.EAST, idLabel);
		sl_panel_5.putConstraint(SpringLayout.EAST, idField, -129, SpringLayout.EAST, buttonPanel);
		buttonPanel.add(idField);
		idField.setColumns(10);
		
		nameField = new JTextField();
		sl_panel_5.putConstraint(SpringLayout.NORTH, nameLabel, -3, SpringLayout.NORTH, nameField);
		sl_panel_5.putConstraint(SpringLayout.NORTH, nameField, 6, SpringLayout.SOUTH, idField);
		sl_panel_5.putConstraint(SpringLayout.WEST, nameField, 0, SpringLayout.WEST, idField);
		sl_panel_5.putConstraint(SpringLayout.EAST, nameField, 0, SpringLayout.EAST, idField);
		buttonPanel.add(nameField);
		nameField.setColumns(10);

		toolBar = new JToolBar();
		sl_panel_5.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, selectionHeadline);
		sl_panel_5.putConstraint(SpringLayout.SOUTH, toolBar, 10, SpringLayout.SOUTH, buttonPanel);
		sl_panel_5.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, idField);
		buttonPanel.add(toolBar);
		toolBar.setLayout(new GridLayout());
		toolBar.setFloatable(false);

		customerButton = new JButton();
		customerButton.setBorder(BorderFactory.createSoftBevelBorder(0));

		projectButton = new JButton();
		projectButton.setBorder(BorderFactory.createSoftBevelBorder(0));

		statusButton = new JButton();
		statusButton.setBorder(BorderFactory.createSoftBevelBorder(0));

		searchButton = new JButton("Search");
		sl_panel_5.putConstraint(SpringLayout.SOUTH, searchButton, -8, SpringLayout.SOUTH, buttonPanel);
		sl_panel_5.putConstraint(SpringLayout.SOUTH, toolBar, 0, SpringLayout.SOUTH, searchButton);
		sl_panel_5.putConstraint(SpringLayout.EAST, searchButton, -10, SpringLayout.EAST, buttonPanel);
		buttonPanel.add(searchButton);
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

		frame.getContentPane().add(reportPerformancePanel);
		reportPerformancePanel.setLayout(new SpringLayout());

		final JPanel helpPanel = new JPanel();
		helpPanel.setPreferredSize(new Dimension(100, 75));
		reportPerformancePanel.add(helpPanel);
		SpringLayout sl_panel_4 = new SpringLayout();
		helpPanel.setLayout(sl_panel_4);

		button = new JButton("Help");
		sl_panel_4.putConstraint(SpringLayout.SOUTH, button, 0, SpringLayout.SOUTH, helpPanel);
		sl_panel_4.putConstraint(SpringLayout.EAST, button, -10, SpringLayout.EAST, helpPanel);
		helpPanel.add(button);

		label = new JLabel("Report Parameters");
		sl_panel_4.putConstraint(SpringLayout.WEST, label, 85, SpringLayout.WEST, helpPanel);
		sl_panel_4.putConstraint(SpringLayout.SOUTH, label, 0, SpringLayout.SOUTH, helpPanel);
		label.setFont(new Font("Serif", Font.PLAIN, 24));
		helpPanel.add(label);

		displayPanel = new JPanel();
		reportPerformancePanel.add(displayPanel);
		displayPanelLayout = new SpringLayout();
		displayPanel.setLayout(displayPanelLayout);

		scrollPaneCustomers = new JScrollPane();
		displayPanel.add(scrollPaneCustomers);	
		scrollPaneProjects = new JScrollPane();
		displayPanel.add(scrollPaneProjects);	
		scrollPaneStatuses = new JScrollPane();
		displayPanel.add(scrollPaneStatuses);	
		SpringUtilities.makeGrid(displayPanel,3,1,0,0,0,5);

		PartialSelectionModel statusSelectionModel = new PartialSelectionModel();
		statusTable = new ReportParameterTable(statusSelectionModel, nullSelectionModel, reportParameterStatusModel);
		statusSelectionModel.addListSelectionListener(new ReportParameterTableListSelectionListener(statusTable,selectionTable));
		statusTable.setName("statuses");
		configureTableColumns(statusTable);
		scrollPaneStatuses.setViewportView(statusTable);
		statusTable.disable();

		PartialSelectionModel projectSelectionModel = new PartialSelectionModel();
		projectTable = new ReportParameterTable(projectSelectionModel, nullSelectionModel,reportParameterProjectModel);
		projectSelectionModel.addListSelectionListener(new ReportParameterTableListSelectionListener(projectTable,selectionTable));
		projectTable.setName("projects");
		configureTableColumns(projectTable);
		scrollPaneProjects.setViewportView(projectTable);
		projectTable.disable();

		PartialSelectionModel customerSelectionModel = new PartialSelectionModel();
		customerTable = new ReportParameterTable(customerSelectionModel, nullSelectionModel, reportParameterCustomerModel);
		customerSelectionModel.addListSelectionListener(new ReportParameterTableListSelectionListener(customerTable,selectionTable));
		customerTable.setName("customers");
		configureTableColumns(customerTable);
		customerTable.getColumnModel().getColumn(1).setMaxWidth(50);
		scrollPaneCustomers.setViewportView(customerTable);
		customerTable.enable();

		// TODO: Fix overlapping panels after resize. GUI looks horrible at certain screen sizes.

		reportPanel = new JPanel();
		reportPanel.setPreferredSize(new Dimension(100,75));
		reportPerformancePanel.add(reportPanel);
		SpringLayout reportPanelLayout = new SpringLayout();
		reportPanel.setLayout(reportPanelLayout);
		generateReportButton = new JButton();
		reportPanelLayout.putConstraint(SpringLayout.EAST, generateReportButton, -5, SpringLayout.EAST, reportPanel);
		reportPanelLayout.putConstraint(SpringLayout.SOUTH, generateReportButton, 0, SpringLayout.SOUTH, reportPanel);
		reportPanel.add(generateReportButton);

		generateReportButton.setAction(new GenerateReportAction(reportParameterCustomerModel.getRowData(), reportParameterProjectModel.getRowData(), reportParameterStatusModel.getRowData(), frame));
		generateReportButton.setForeground(Color.blue);
		generateReportButton.setFont(bold);
		generateReportButton.setText("Generate Report");

		SpringUtilities.makeCompactGrid(reportPerformancePanel,3,1,0,5,5,5);
		SpringUtilities.makeGrid(frame.getContentPane(),1,2,0,0,10,10);
		
		EnableSelectionAction selectCustomers = new EnableSelectionAction(this, State.CUSTOMER);
		customerButton.setAction(selectCustomers);
		customerButton.setText("Select Customers");

		EnableSelectionAction selectProjects = new EnableSelectionAction(this, State.PROJECT);
		projectButton.setAction(selectProjects);
		projectButton.setText("Select Projects");

		EnableSelectionAction selectStatuses = new EnableSelectionAction(this, State.STATUS);
		statusButton.setAction(selectStatuses);
		statusButton.setText("Select Statuses");

		toolBar.add(customerButton);
		toolBar.add(projectButton);
		toolBar.add(statusButton);

		Active.setState(State.CUSTOMER);
		selectionTable.setAutoCreateRowSorter(true);
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

	public enum State {CUSTOMER, PROJECT, STATUS};
	public static class Active{

		private static MyTableModel display;
		private static MyTableModel select;
		private static ReportParameterTable table;
		private static State activeState;

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

		public static void setState(State state){
			switch (state){
			case CUSTOMER:
				display = reportParameterCustomerModel;
				select = selectCustomerModel;
				table = customerTable;
				activeState = state;
				break;
			case PROJECT:
				display = reportParameterProjectModel;
				select = selectProjectModel;
				table = projectTable;
				activeState = state;
				break;
			case STATUS:
				display = reportParameterStatusModel;
				select = selectStatusModel;
				table = statusTable;
				activeState = state;
				break;
			}
		}
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
		return customerTable;
	}
	public ReportParameterTable getProjectTable() {
		return projectTable;
	}
	public ReportParameterTable getStatusTable() {
		return statusTable;
	}
	public MyTableModel getReportParameterCustomerModel() {
		return reportParameterCustomerModel;
	}
	public MyTableModel getReportParameterProjectModel() {
		return reportParameterProjectModel;
	}
	public MyTableModel getReportParameterStatusModel() {
		return reportParameterStatusModel;
	}
	public MyTableModel getSelectCustomerModel() {
		return selectCustomerModel;
	}
	public MyTableModel getSelectProjectModel() {
		return selectProjectModel;
	}
	public MyTableModel getSelectStatusModel() {
		return selectStatusModel;
	}
	public JButton getCustomersButton() {
		return customerButton;
	}
	public JButton getProjectsButton() {
		return projectButton;
	}
	public JButton getStatusesButton() {
		return statusButton;
	}
	public JLabel getSelectionHeadline() {
		return selectionHeadline;
	}	
	public TableCellRenderer getHeader() {
		return header;
	}
}
