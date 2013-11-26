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
	private JButton btnGenerateReport;
	private JTextField idField;
	private JTextField nameField;
	private JLabel lblID;
	private JLabel lblName;
	private JButton btnSearch;
	private JScrollPane scrollPane;
	private SelectionTable selectionTable;
	private JScrollPane scrollPaneCustomers;
	private static ReportParameterTable customerTable;
	private JScrollPane scrollPaneProjects;
	private static ReportParameterTable projectTable;
	private JScrollPane scrollPaneStatuses;
	private static ReportParameterTable statusTable;
	private static MyTableModel stmDisplayCust;
	private static MyTableModel stmDisplayProj;
	private static MyTableModel stmDisplayStat;
	private static MyTableModel stmSelectCust;
	private static MyTableModel stmSelectProj;
	private static MyTableModel stmSelectStat;
	private NullSelectionModel nullSelectionModel;
	private PartialSelectionModel partialSelectionModel;
	private JToolBar toolBar;
	private JPanel selectionPanel;
	private SpringLayout sl_panel;
	private JButton bCustomers;
	private JButton bProjects;
	private JButton bStatuses;
	private CheckBoxHeader header;
	private DatabaseConnection databaseConnection;
	private List<String> colNames_sComp;
	private List<String> colNames_sProj;
	private List<String> colNames_sStat;
	private boolean headerClick;
	private SpringLayout sl_panel_3;
	private JPanel displayPanel;
	private Font bold;
	private SpringLayout springLayout_1;
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

	public boolean isHeaderClick() {
		return headerClick;
	}

	// TODO: Headers not disabled if Remove all and switching tabs
	private void initialize() {
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		headerClick = true;

		colNames_sComp = new ArrayList<String>();
		colNames_sComp.add("");
		colNames_sComp.add("ID");
		colNames_sComp.add("Customers");

		colNames_sProj = new ArrayList<String>();
		colNames_sProj.add("");
		colNames_sProj.add("Projects");

		colNames_sStat = new ArrayList<String>();
		colNames_sStat.add("");
		colNames_sStat.add("Item Statuses");

		SpringLayout springLayout = createFrame();
		nullSelectionModel = new NullSelectionModel();
		partialSelectionModel = new PartialSelectionModel();

		bold = new Font("Serif", Font.BOLD, 12);
		Font headline = new Font("Serif", Font.PLAIN, 24);
		Font subheadline = new Font("Serif", Font.PLAIN, 16);

		stmSelectCust = new MyTableModel(colNames_sComp);
		stmSelectProj = new MyTableModel(colNames_sProj);
		stmSelectStat = new MyTableModel(colNames_sStat);

		stmDisplayStat = new MyTableModel(colNames_sStat);
		stmDisplayProj = new MyTableModel(colNames_sProj);
		stmDisplayCust = new MyTableModel(colNames_sComp);

		selectionPanel = new JPanel();
		selectionPanel.setBackground(Color.white);
		frame.getContentPane().add(selectionPanel);

		sl_panel = new SpringLayout();
		selectionPanel.setLayout(sl_panel);
		scrollPane = new JScrollPane();
		sl_panel.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, selectionPanel);
		sl_panel.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, selectionPanel);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, selectionPanel);
		sl_panel.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, selectionPanel);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 454, SpringLayout.WEST, selectionPanel);
		selectionPanel.add(scrollPane);

		selectionTable = new SelectionTable(stmSelectCust);
		selectionTable.setName("selection");
		selectionTable.getSelectionModel().addListSelectionListener(new MyListSelectionListener(this, selectionTable));
		TableColumn tc = configureTableColumns(selectionTable);
		header = new CheckBoxHeader(new SelectionTableHeaderListener(this, selectionTable));
		tc.setHeaderRenderer(header);

		scrollPane.setViewportView(selectionTable);
		scrollPane.getViewport().setBackground(Color.white);

		partialSelectionModel.addListSelectionListener(new MyListSelectionListener(this, selectionTable));
		databaseConnection = new DatabaseConnection();
		databaseConnection.loadStatusData(stmSelectStat);

		JPanel reportPerformancePanel = new JPanel();
		{
			buttonPanel = new JPanel();
			sl_panel.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, buttonPanel);
			springLayout_1.putConstraint(SpringLayout.EAST, buttonPanel, -22, SpringLayout.EAST, scrollPane);
			springLayout_1.putConstraint(SpringLayout.WEST, buttonPanel, 0, SpringLayout.WEST, selectionPanel);
			springLayout_1.putConstraint(SpringLayout.NORTH, buttonPanel, 0, SpringLayout.NORTH, selectionPanel);
			selectionPanel.add(buttonPanel);
			buttonPanel.setPreferredSize(new Dimension(460,150));
			buttonPanel.setBackground(Color.white);
			SpringLayout sl_panel_5 = new SpringLayout();
			buttonPanel.setLayout(sl_panel_5);
			{
				selectionHeadline = new JLabel("Select Customers");
				sl_panel_5.putConstraint(SpringLayout.NORTH, selectionHeadline, 10, SpringLayout.NORTH, buttonPanel);
				sl_panel_5.putConstraint(SpringLayout.WEST, selectionHeadline, 10, SpringLayout.WEST, buttonPanel);
				selectionHeadline.setFont(headline);
				buttonPanel.add(selectionHeadline);
			}
			lblID = new JLabel("Customer ID");
			sl_panel_5.putConstraint(SpringLayout.NORTH, lblID, 6, SpringLayout.SOUTH, selectionHeadline);
			sl_panel_5.putConstraint(SpringLayout.WEST, lblID, 0, SpringLayout.WEST, selectionHeadline);
			buttonPanel.add(lblID);
			lblID.setFont(subheadline);
			lblName = new JLabel("Customer Name");
			sl_panel_5.putConstraint(SpringLayout.WEST, lblName, 0, SpringLayout.WEST, selectionHeadline);
			buttonPanel.add(lblName);
			lblName.setFont(subheadline);

			PlainDocument doc = MyDocumentFilter.createDocumentFilter();
			idField = new JTextField();
			idField.setDocument(doc);
			sl_panel_5.putConstraint(SpringLayout.NORTH, idField, 9, SpringLayout.SOUTH, selectionHeadline);
			sl_panel_5.putConstraint(SpringLayout.WEST, idField, 63, SpringLayout.EAST, lblID);
			sl_panel_5.putConstraint(SpringLayout.EAST, idField, -129, SpringLayout.EAST, buttonPanel);
			buttonPanel.add(idField);
			idField.setColumns(10);
			nameField = new JTextField();
			sl_panel_5.putConstraint(SpringLayout.NORTH, lblName, -3, SpringLayout.NORTH, nameField);
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
			
			bCustomers = new JButton();
			bCustomers.setBorder(BorderFactory.createSoftBevelBorder(0));
			
			bProjects = new JButton();
			bProjects.setBorder(BorderFactory.createSoftBevelBorder(0));
			
			bStatuses = new JButton();
			bStatuses.setBorder(BorderFactory.createSoftBevelBorder(0));

			btnSearch = new JButton("Search");
			sl_panel_5.putConstraint(SpringLayout.SOUTH, btnSearch, -8, SpringLayout.SOUTH, buttonPanel);
			sl_panel_5.putConstraint(SpringLayout.SOUTH, toolBar, 0, SpringLayout.SOUTH, btnSearch);
			sl_panel_5.putConstraint(SpringLayout.EAST, btnSearch, -10, SpringLayout.EAST, buttonPanel);
			buttonPanel.add(btnSearch);
			btnSearch.setForeground(Color.blue);
			btnSearch.setFont(bold);
			btnSearch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DatabaseSearch.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), databaseConnection, nameField, idField);
					synchronizeHeader();
				}
			});
			nameField.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e){
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						DatabaseSearch.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), databaseConnection, nameField, idField);
						synchronizeHeader();
					}
				}
			});
			idField.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e){
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						DatabaseSearch.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), databaseConnection, nameField, idField);
						synchronizeHeader();
					}
				}
			});
		}
		springLayout_1.putConstraint(SpringLayout.SOUTH, reportPerformancePanel, -10, SpringLayout.SOUTH, frame.getContentPane());
		frame.getContentPane().add(reportPerformancePanel);
		reportPerformancePanel.setLayout(new SpringLayout());

		JPanel helpPanel = new JPanel();
		helpPanel.setMinimumSize(new Dimension(100, 100));
		reportPerformancePanel.add(helpPanel);
		SpringLayout sl_panel_4 = new SpringLayout();
		helpPanel.setLayout(sl_panel_4);
		{
			button = new JButton("Help");
			sl_panel_4.putConstraint(SpringLayout.SOUTH, button, 0, SpringLayout.SOUTH, helpPanel);
			sl_panel_4.putConstraint(SpringLayout.EAST, button, -10, SpringLayout.EAST, helpPanel);
			helpPanel.add(button);
		}
		{
			label = new JLabel("Report Parameters");
			sl_panel_4.putConstraint(SpringLayout.WEST, label, 85, SpringLayout.WEST, helpPanel);
			sl_panel_4.putConstraint(SpringLayout.SOUTH, label, 0, SpringLayout.SOUTH, helpPanel);
			label.setFont(new Font("Serif", Font.PLAIN, 24));
			helpPanel.add(label);
		}

		displayPanel = new JPanel();
		reportPerformancePanel.add(displayPanel);
		sl_panel_3 = new SpringLayout();
		displayPanel.setLayout(sl_panel_3);

		scrollPaneCustomers = new JScrollPane();
		displayPanel.add(scrollPaneCustomers);	
		scrollPaneProjects = new JScrollPane();
		displayPanel.add(scrollPaneProjects);	
		scrollPaneStatuses = new JScrollPane();
		displayPanel.add(scrollPaneStatuses);	
		SpringUtilities.makeGrid(displayPanel,3,1,0,0,0,5);
		
		statusTable = new ReportParameterTable(partialSelectionModel, nullSelectionModel, stmDisplayStat);
		statusTable.setName("statuses");
		configureTableColumns(statusTable);
		scrollPaneStatuses.setViewportView(statusTable);
		statusTable.disable();
		
		projectTable = new ReportParameterTable(partialSelectionModel, nullSelectionModel,stmDisplayProj);
		projectTable.setName("projects");
		configureTableColumns(projectTable);
		scrollPaneProjects.setViewportView(projectTable);
		projectTable.disable();
		
		customerTable = new ReportParameterTable(partialSelectionModel, nullSelectionModel, stmDisplayCust);
		customerTable.setName("customers");
		configureTableColumns(customerTable);
		customerTable.getColumnModel().getColumn(1).setMaxWidth(50);
		scrollPaneCustomers.setViewportView(customerTable);
		customerTable.enable();

		// TODO: Fix overlapping panels after resize

		reportPanel = new JPanel();
		reportPanel.setMinimumSize(new Dimension(100,100));
		reportPerformancePanel.add(reportPanel);
		SpringLayout sl_panel_6 = new SpringLayout();
		reportPanel.setLayout(sl_panel_6);
		btnGenerateReport = new JButton();
		sl_panel_6.putConstraint(SpringLayout.EAST, btnGenerateReport, -5, SpringLayout.EAST, reportPanel);
		sl_panel_6.putConstraint(SpringLayout.SOUTH, btnGenerateReport, 0, SpringLayout.SOUTH, reportPanel);
		reportPanel.add(btnGenerateReport);

		btnGenerateReport.setAction(new GenerateReportAction(stmDisplayCust.getRowData(), stmDisplayProj.getRowData(), stmDisplayStat.getRowData(), frame));
		btnGenerateReport.setForeground(Color.blue);
		btnGenerateReport.setFont(bold);
		btnGenerateReport.setText("Generate Report");

		SpringUtilities.makeCompactGrid(reportPerformancePanel,3,1,0,0,0,5);
		SpringUtilities.makeGrid(frame.getContentPane(),1,2,0,0,10,10);
		
		EnableSelectionAction selectCustomers = new EnableSelectionAction(this, State.CUSTOMER);
		bCustomers.setAction(selectCustomers);
		bCustomers.setText("Select Customers");
		
		EnableSelectionAction selectProjects = new EnableSelectionAction(this, State.PROJECT);
		bProjects.setAction(selectProjects);
		bProjects.setText("Select Projects");
		
		EnableSelectionAction selectStatuses = new EnableSelectionAction(this, State.STATUS);
		bStatuses.setAction(selectStatuses);
		bStatuses.setText("Select Statuses");
		
		toolBar.add(bCustomers);
		toolBar.add(bProjects);
		toolBar.add(bStatuses);
		Active.setState(State.CUSTOMER);
	}

	private SpringLayout createFrame() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 900);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		springLayout_1 = new SpringLayout();
		frame.getContentPane().setLayout(springLayout_1);
		frame.getContentPane().setBackground(Color.white);
		return springLayout_1;
	}
	public TableColumn configureTableColumns(JTable table) {
		if(Active.getActiveDisplayModel()==stmDisplayCust) selectionTable.getColumnModel().getColumn(1).setMaxWidth(50);
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
			CheckBoxHeader checkboxHeader = new CheckBoxHeader(new ReportParameterTableHeaderListener(this, selectionTable));
			checkboxHeader.setEnabled(false);
			checkboxHeader.setSelected(true);
			tc.setHeaderRenderer(checkboxHeader);
		}
		return tc;
	}
	public void synchDisplayHeaders(JTable activeTable){
		TableColumn column = activeTable.getColumnModel().getColumn(0);
		CheckBoxHeader checkBoxHeader = (CheckBoxHeader) column.getHeaderRenderer();
		if(activeTable.getRowCount() > 0){
			checkBoxHeader.setEnabled(true);
			checkBoxHeader.setSelected(false);
			column.setHeaderValue("Remove All");
		}else{
			checkBoxHeader.setEnabled(false);
			checkBoxHeader.setSelected(true);
			column.setHeaderValue("Select All");
		}
	}
	public void synchronizeHeader(){
		boolean checked = true;
		if(selectionTable.getRowCount() == 0) header.setSelected(false);
		else{ 
			for(int x = 0; x < selectionTable.getRowCount(); x++){
				if(!(boolean) selectionTable.getValueAt(x, 0)){
					checked = false;
				}
			}
			headerClick = false;
			header.setSelected(checked);
			frame.getContentPane().repaint();
			headerClick = true;
		}
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
				display = stmDisplayCust;
				select = stmSelectCust;
				table = customerTable;
				activeState = state;
				break;
			case PROJECT:
				display = stmDisplayProj;
				select = stmSelectProj;
				table = projectTable;
				activeState = state;
				break;
			case STATUS:
				display = stmDisplayStat;
				select = stmSelectStat;
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
	public JLabel getLblID() {
		return lblID;
	}
	public JLabel getLblName() {
		return lblName;
	}
	public JButton getBtnSearch() {
		return btnSearch;
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
	public MyTableModel getStmDisplayCust() {
		return stmDisplayCust;
	}
	public MyTableModel getStmDisplayProj() {
		return stmDisplayProj;
	}
	public MyTableModel getStmDisplayStat() {
		return stmDisplayStat;
	}
	public MyTableModel getStmSelectCust() {
		return stmSelectCust;
	}
	public MyTableModel getStmSelectProj() {
		return stmSelectProj;
	}
	public MyTableModel getStmSelectStat() {
		return stmSelectStat;
	}
	public JButton getbCustomers() {
		return bCustomers;
	}
	public JButton getbProjects() {
		return bProjects;
	}
	public JButton getbStatuses() {
		return bStatuses;
	}
	public JLabel getSelectionHeadline() {
		return selectionHeadline;
	}	
	public TableCellRenderer getHeader() {
		return header;
	}
}
