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

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.PlainDocument;

import net.miginfocom.swing.MigLayout;

public class GUI {

	private JFrame frame;
	private JTextField idField;
	private JTextField nameField;
	private static JTextField categoryField;
	private static JTextField frameAgrField;
	private JLabel idLabel;
	private JLabel nameLabel;
	private JLabel frameAgrLabel;
	private JLabel categoryLabel;
	private JLabel selectionHeadlineLabel;
	private JLabel reportParameterLabel;
	private JLabel markErrorsLabel;
	private JScrollPane scrollPane;
	private JScrollPane scrollPaneCustomers;
	private JScrollPane scrollPaneProjects;
	private SelectionTable intervalSelectionTable;
	private static ReportParameterTable clientTable;
	private static ReportParameterTable projectTable;
	private static MyTableModel reportParameterClientModel;
	private static MyTableModel reportParameterProjectModel;
	private static MyTableModel selectClientModel;
	private static MyTableModel selectProjectModel;
	private static DefaultTableModel selectCategoryModel;
	private static DefaultTableModel selectFrameAgrModel;
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
	private JPanel backgroundPanel;
	private JPanel selectionPanel;
	private JPanel displayPanel;
	private JPanel buttonPanel;
	private JPanel reportPanel;
	private JPanel navigationPanel;
	private JPanel helpPanel;
	private Font bold;
	private JTable singleSelectionTable;
	private ActionListener searchButtonListener;
	private KeyAdapter SearchKeyListener;
	private DatabaseSearch databaseSearch;
	private JCheckBox markErrorsCheckBox;
	private Color lighterGray = new Color(200,200,200);
	private Color darkerGray = new Color(75,75,75);

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

		databaseSearch = new DatabaseSearch();

		searchButtonListener = new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				databaseSearch.executeSearch(Active.getActiveSelectModel(), reportParameterClientModel, reportParameterProjectModel, databaseConnection, nameField, idField, Active.getState(), Active.getActiveSimpleSelectModel(), frameAgrField);
				if(Active.getState().equals(State.CLIENT) || Active.getState().equals(State.PROJECT))intervalSelectionTable.synchronizeHeader();
			}
		};

		SearchKeyListener = new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					databaseSearch.executeSearch(Active.getActiveSelectModel(), reportParameterClientModel, reportParameterProjectModel, databaseConnection, nameField, idField, Active.getState(), Active.getActiveSimpleSelectModel(), frameAgrField);
					if(Active.getState().equals(State.CLIENT) || Active.getState().equals(State.PROJECT))intervalSelectionTable.synchronizeHeader();
				}
			}
		};

		clientColumnNames = new ArrayList<String>();
		clientColumnNames.add("");
		clientColumnNames.add("ID");
		clientColumnNames.add("Clients");

		projectColumnNames = new ArrayList<String>();
		projectColumnNames.add("");
		projectColumnNames.add("Projects");

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setBackground(Color.white);

		bold = new Font("Serif", Font.BOLD, 12);
		Font headline = new Font("Serif", Font.PLAIN, 24);
		Font subheadline = new Font("Serif", Font.PLAIN, 16);

		selectClientModel = new MyTableModel(clientColumnNames, "Select client Model");
		selectProjectModel = new MyTableModel(projectColumnNames, "Select project Model");
		selectFrameAgrModel = new DefaultTableModel();
		selectFrameAgrModel.addColumn("Name");
		selectCategoryModel = new DefaultTableModel();
		selectCategoryModel.addColumn("Name");

		reportParameterProjectModel = new MyTableModel(projectColumnNames, "Report Parameter Project Model");
		reportParameterClientModel = new MyTableModel(clientColumnNames, "Report Parameter Client Model");
		
		categoryField = new JTextField();
		intervalSelectionTable = new SelectionTable(selectClientModel);
		
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
		
		ReportParameterTableModelListener clientModelListener = new ReportParameterTableModelListener(categoryField, selectCategoryModel);
		reportParameterClientModel.addTableModelListener(clientModelListener);
		
		ReportParameterTableModelListener projectModelListener = new ReportParameterTableModelListener(reportParameterClientModel, selectClientModel, clientTable);
		reportParameterProjectModel.addTableModelListener(projectModelListener);

		intervalSelectionTable.setAutoCreateRowSorter(true);
		frame.pack();

		DatabaseConnection db = new DatabaseConnection();
	}

	private JPanel createDisplayPanel() {

		displayPanel = new JPanel(new MigLayout("fill"));
		displayPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		frameAgrLabel = new JLabel("Frame Agreement");
		displayPanel.add(frameAgrLabel, "split, center");

		frameAgrField = new JTextField();
		frameAgrField.setEnabled(false);
		frameAgrField.setBackground(Color.white);
		frameAgrField.setDisabledTextColor(Color.black);
		frameAgrField.setText("ALL");
		
		displayPanel.add(frameAgrField, "wrap, width :180:");

		scrollPaneProjects = new JScrollPane();
		displayPanel.add(scrollPaneProjects, "wrap, grow");

		scrollPaneCustomers = new JScrollPane();
		displayPanel.add(scrollPaneCustomers, "wrap, grow");

		categoryLabel = new JLabel("Category");
		displayPanel.add(categoryLabel, "split, center");

		categoryField.setEnabled(false);
		categoryField.setBackground(lighterGray);
		categoryField.setDisabledTextColor(darkerGray);
		categoryField.setText("ALL");
		displayPanel.add(categoryField, "width :180:");

		nullSelectionModel = new NullSelectionModel();
		PartialSelectionModel projectListSelectionModel = new PartialSelectionModel();
		projectTable = new ReportParameterTable(projectListSelectionModel, nullSelectionModel,reportParameterProjectModel, lighterGray, darkerGray);
		projectListSelectionModel.addListSelectionListener(new ReportParameterTableListSelectionListener(projectTable,intervalSelectionTable));
		projectTable.setName("projects");
		configureTableColumns(projectTable);
		scrollPaneProjects.setViewportView(projectTable);
		projectTable.setEnabled(false);

		PartialSelectionModel customerListSelectionModel = new PartialSelectionModel();
		clientTable = new ReportParameterTable(customerListSelectionModel, nullSelectionModel, reportParameterClientModel, lighterGray, darkerGray);
		customerListSelectionModel.addListSelectionListener(new ReportParameterTableListSelectionListener(clientTable,intervalSelectionTable));
		clientTable.setName("clients");
		configureTableColumns(clientTable);
		clientTable.getColumnModel().getColumn(1).setMaxWidth(50);
		scrollPaneCustomers.setViewportView(clientTable);
		clientTable.setEnabled(false);
		
		frameAgrField.getDocument().addDocumentListener(new TextFieldDocumentListener(reportParameterProjectModel, selectProjectModel, projectTable));

		return displayPanel;
	}

	private JPanel createReportPanel() {
		reportPanel = new JPanel(new MigLayout("fillx, insets 0"));
		generateReportButton = new JButton();
		markErrorsCheckBox = new JCheckBox();
		markErrorsLabel = new JLabel("Mark erroneous rows");
		reportPanel.add(markErrorsCheckBox, "split 2, center");
		reportPanel.add(markErrorsLabel, "push");
		reportPanel.add(generateReportButton);

		generateReportButton.setAction(new GenerateReportAction(reportParameterClientModel.getRowData(), reportParameterProjectModel.getRowData(), frameAgrField, markErrorsCheckBox, categoryField, frame));
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

		idLabel = new JLabel("Client ID");
		buttonPanel.add(idLabel, "split, left, gapright 40, width :110:");
		idLabel.setFont(subheadline);
		idLabel.setVisible(false);

		PlainDocument doc = MyDocumentFilter.createDocumentFilter();
		idField = new JTextField();
		idField.setDocument(doc);
		idField.setVisible(false);
		buttonPanel.add(idField, "wrap, width :183:");

		nameLabel = new JLabel("Frame Agreement");
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
		searchButton.addActionListener(searchButtonListener);
		nameField.addKeyListener(SearchKeyListener);
		idField.addKeyListener(SearchKeyListener);
		
		return buttonPanel;
	}

	private JPanel createNavigationPanel() {
		navigationPanel = new JPanel(new MigLayout("fillx, insets 0"));
		previousStepButton = new JButton();
		previousStepButton.setAction(new PreviousStateAction(this, lighterGray, darkerGray));
		previousStepButton.setEnabled(false);
		previousStepButton.setText("Back");
		previousStepButton.setHorizontalTextPosition(AbstractButton.RIGHT);
		previousStepButton.setIcon(new ImageIcon("C:/vendorLogistics/previous_16.png"));
		nextStepButton = new JButton();
		nextStepButton.setAction(new NextStageAction(this, lighterGray, darkerGray));
		nextStepButton.setText("Next");
		nextStepButton.setHorizontalTextPosition(AbstractButton.LEFT);
		nextStepButton.setIcon(new ImageIcon("C:/vendorLogistics/next_16.png"));
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

		intervalSelectionTable.setName("selection");
		intervalSelectionTable.getSelectionModel().addListSelectionListener(new SelectionTableListSelectionListener(intervalSelectionTable));
		TableColumn tc = configureTableColumns(intervalSelectionTable);
		header = new CheckBoxHeader(new SelectionTableHeaderListener(intervalSelectionTable)); 
		header.setSelected(true);
		header.setEnabled(false);
		tc.setHeaderRenderer(header);

		singleSelectionTable = new JTable(selectFrameAgrModel);
		singleSelectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		singleSelectionTable.getSelectionModel().addListSelectionListener(new SingleSelectionListener(singleSelectionTable));
		scrollPane.setViewportView(singleSelectionTable);
		scrollPane.getViewport().setBackground(Color.white);

		return scrollPane;
	}

	public TableColumn configureTableColumns(JTable table) { 
		if(table.getColumnCount()==3) intervalSelectionTable.getColumnModel().getColumn(1).setMaxWidth(75);
		table.getColumnModel().getColumn(0).setMinWidth(60); 
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		TableColumn tc = table.getColumnModel().getColumn(0);
		tc.setHeaderValue("ALL");
		if(table.getName().equals("selection")){
			tc.setCellEditor(table.getDefaultEditor(Boolean.class));
			tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		}else{
			tc.setCellRenderer(table.getDefaultRenderer(Icon.class));
			CheckBoxHeader checkboxHeader = new CheckBoxHeader(new ReportParameterTableHeaderListener(intervalSelectionTable, (ReportParameterTable) table)); // new
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
	//TODO: Rename everything so it makes sense
	public static class Active{

		private static MyTableModel checkboxDisplayModel;
		private static MyTableModel checkboxSelectModel;
		private static DefaultTableModel simpleSelectModel = selectFrameAgrModel;
		private static ReportParameterTable table;
		private static State activeState = State.FRAME;
		private static JTextField displayField = frameAgrField;

		public static DefaultTableModel getActiveSimpleSelectModel() {
			return simpleSelectModel;
		}

		public static JTextField getDisplayField() {
			return displayField;
		}

		public static MyTableModel getActiveDisplayModel(){
			return checkboxDisplayModel;
		}

		public static MyTableModel getActiveSelectModel(){
			return checkboxSelectModel;
		}

		public static ReportParameterTable getActiveDisplayTable(){
			return table;
		}

		public static State getState(){
			return activeState;
		}

		// TODO: Move all logic into the actions
		public static State setNextState(){
			switch(activeState){
			case FRAME:
				checkboxDisplayModel = reportParameterProjectModel;
				checkboxSelectModel = selectProjectModel;
				table = projectTable;
				activeState = State.PROJECT;
				return State.PROJECT;
			case PROJECT:
				checkboxDisplayModel = reportParameterClientModel;
				checkboxSelectModel = selectClientModel;
				table = clientTable;
				activeState = State.CLIENT;
				return State.CLIENT;
			case CLIENT:
				activeState = State.CATEGORY;
				displayField = categoryField;
				simpleSelectModel = selectCategoryModel;
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
				displayField = frameAgrField;
				simpleSelectModel = selectFrameAgrModel;
				return State.FRAME;
			case CLIENT:
				checkboxDisplayModel = reportParameterProjectModel;
				checkboxSelectModel = selectProjectModel;
				table = projectTable;
				activeState = State.PROJECT;
				return State.PROJECT;
			case CATEGORY:
				checkboxDisplayModel = reportParameterClientModel;
				checkboxSelectModel = selectClientModel;
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

	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	public JTable getSingleSelectionTable() {
		return singleSelectionTable;
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
	public SelectionTable getIntervalSelectionTable() {
		return intervalSelectionTable;
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
	public ActionListener getCheckBoxSearch() {
		return searchButtonListener;
	}
	public KeyAdapter getCheckBoxKeySearch() {
		return SearchKeyListener;
	}
	public JCheckBox getMarkErrorsCheckBox() {
		return markErrorsCheckBox;
	}
}
