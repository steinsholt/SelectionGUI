package hbs.sff.no;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class GUI {

	private JFrame frame;
	private JButton btnExit;
	private JButton btnHelp;
	private JButton btnGenerateReport;
	private JTextField idField;
	private JTextField nameField;
	private JLabel lblID;
	private JLabel lblName;
	private JButton btnSearch;
	private JLabel lblInvalidInput;
	private JLabel lblInvalidInput_1;
	private JScrollPane scrollPane;
	private JTable table_selection;
	private JScrollPane scrollPaneCustomers;
	private JTable table_customers;
	private JScrollPane scrollPaneProjects;
	private JTable table_projects;
	private JScrollPane scrollPaneStatuses;
	private JTable table_statuses;
	private SelectionTableModel stmDisplayCust;
	private SelectionTableModel stmDisplayProj;
	private SelectionTableModel stmDisplayStat;
	private SelectionTableModel stmSelectCust;
	private SelectionTableModel stmSelectProj;
	private SelectionTableModel stmSelectStat;
	private NullSelectionModel nullSelectionModel;
	private PartialSelectionModel partialSelectionModel;
	private JToolBar toolBar;
	private JPanel panel;
	private SpringLayout sl_panel;
	private JButton bCustomers;
	private JButton bProjects;
	private JButton bStatuses;
	private JLabel lblSelection;
	private CheckBoxHeader header;
	private Data data;
	private List<String> colNames_sComp;
	private List<String> colNames_sProj;
	private List<String> colNames_sStat;
	private boolean headerClick;

	public enum Active{
		STATUS, CUSTOMER, PROJECT;
		private static SelectionTableModel display;
		private static SelectionTableModel select;
		private static JTable table;

		public static SelectionTableModel getActiveDisplayModel(){
			return display;
		}

		public static SelectionTableModel getActiveSelectModel(){
			return select;
		}

		public static JTable getActiveDisplayTable(){
			return table;
		}

		public static void setActiveTableModels(SelectionTableModel dis, SelectionTableModel sel, JTable t){
			display = dis;
			select = sel;
			table = t;
		}
	}

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

		headerClick = true;
		addColumnNames();
		SpringLayout springLayout = createFrame();
		nullSelectionModel = new NullSelectionModel();
		partialSelectionModel = new PartialSelectionModel();
		partialSelectionModel.addListSelectionListener(new 
				ListSelectionListenerImpl());

		Font headline = new Font("Serif", Font.PLAIN, 24);
		Font subheadline = new Font("Serif", Font.PLAIN, 16);
		Font errorMessage = new Font("Serif", Font.PLAIN, 14);
		JPanel panel_1 = createPanelOne(springLayout);
		JPanel panel_2 = createPanelTwo(springLayout, panel_1);
		sl_panel = new SpringLayout();

		sl_panel.putConstraint(SpringLayout.WEST, toolBar, 10, 
				SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, toolBar, 334, 
				SpringLayout.WEST, panel);
		panel_1.setLayout(sl_panel);
		JLabel lblSelectCustomers = createSelectionLabel(headline, 
				panel_1, sl_panel);

		createIdField(panel_1, sl_panel, lblSelectCustomers);
		createNameField(panel_1, sl_panel);
		createIdLabel(subheadline, panel_1, sl_panel);
		createNameLabel(subheadline, panel_1, sl_panel);
		createSearchButton(panel_1, sl_panel);		
		createErrorLabelOne(errorMessage, panel_1, sl_panel);		
		createErrorLabelTwo(errorMessage, panel_1, sl_panel);		
		frame.getContentPane().add(panel_2);
		SpringLayout sl_panel_2 = new SpringLayout();
		panel_2.setLayout(sl_panel_2);
		JLabel lblSelected = createSelectedHeadline(headline, panel_2,
				sl_panel_2);

		createExitButton(panel_2);
		createHelpButton(panel_2, sl_panel_2, lblSelected);
		createReportButton(panel_2, sl_panel_2);				
		addScrollPaneOne(panel_2, sl_panel_2, lblSelected);		
		addScrollPane(panel_1, sl_panel);		
		addScrollPaneTwo(panel_2, sl_panel_2);		
		addScrollPaneThree(panel_2, sl_panel_2);	

		stmSelectCust = new SelectionTableModel(colNames_sComp);
		stmSelectProj = new SelectionTableModel(colNames_sProj);
		stmSelectStat = new SelectionTableModel(colNames_sStat);

		addTables();
		data = new Data();
		data.loadData();
		for(Object[] item : data.getStatusData()){
			stmSelectStat.addRow(Arrays.asList(item));
		}
		frame.setResizable(false);
		enableCustomerSelection();		
	}

	private void addColumnNames() {
		colNames_sComp = new ArrayList<String>();
		colNames_sComp.add("");
		colNames_sComp.add("ID");
		colNames_sComp.add("Customer");

		colNames_sProj = new ArrayList<String>();
		colNames_sProj.add("");
		colNames_sProj.add("Project");

		colNames_sStat = new ArrayList<String>();
		colNames_sStat.add("");
		colNames_sStat.add("Status");
	}

	private void addTables(){
		table_selection = new JTable(stmSelectCust){
			private static final long serialVersionUID = 1L;
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
				Component c = super.prepareRenderer(renderer, row, column);
				Color color = (boolean) table_selection.getModel().getValueAt(row, 0) ? Color.BLUE : Color.BLACK;
				// TODO: Change to setBackground or setForeground at will. Remember to also change colors above
				c.setForeground(color);
				return c;
			}
		};
		table_selection.getSelectionModel().
		addListSelectionListener(new ListSelectionListenerImpl());
		TableColumn tc = configureTableColumns(table_selection);
		header = new CheckBoxHeader(new MyItemListener());
		tc.setHeaderRenderer(header);
		//		table_selection.getTableHeader().setBackground(bg);   //TODO: This sets the header color

		scrollPane.setViewportView(table_selection);
		scrollPane.getViewport().setBackground(Color.white);

		stmDisplayStat = new SelectionTableModel(colNames_sStat);
		stmDisplayStat.setTrueAll();
		table_statuses = new JTable(stmDisplayStat);
		configureTableColumns(table_statuses);
		scrollPaneStatuses.setViewportView(table_statuses);

		stmDisplayProj = new SelectionTableModel(colNames_sProj);
		stmDisplayProj.setTrueAll();
		table_projects = new JTable(stmDisplayProj);
		configureTableColumns(table_projects);
		scrollPaneProjects.setViewportView(table_projects);

		stmDisplayCust = new SelectionTableModel(colNames_sComp);
		stmDisplayCust.setTrueAll();
		table_customers = new JTable(stmDisplayCust);
		configureTableColumns(table_customers);
		table_customers.getColumnModel().getColumn(1).setMaxWidth(100);
		scrollPaneCustomers.setViewportView(table_customers);
	}

	private void addScrollPaneThree(JPanel panel_2, SpringLayout sl_panel_2) {
		scrollPaneStatuses = new JScrollPane();
		sl_panel_2.putConstraint(SpringLayout.NORTH, scrollPaneStatuses, 27, 
				SpringLayout.SOUTH, scrollPaneProjects);
		sl_panel_2.putConstraint(SpringLayout.WEST, scrollPaneStatuses, 10, 
				SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, scrollPaneStatuses, 227, 
				SpringLayout.SOUTH, scrollPaneProjects);
		sl_panel_2.putConstraint(SpringLayout.EAST, scrollPaneStatuses, 454, 
				SpringLayout.WEST, panel_2);
		panel_2.add(scrollPaneStatuses);
	}

	private void addScrollPaneTwo(JPanel panel_2, SpringLayout sl_panel_2) {
		scrollPaneProjects = new JScrollPane();
		sl_panel_2.putConstraint(SpringLayout.NORTH, scrollPaneProjects, 318, 
				SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.WEST, scrollPaneProjects, 10, 
				SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, scrollPaneProjects, 230, 
				SpringLayout.SOUTH, scrollPaneCustomers);
		sl_panel_2.putConstraint(SpringLayout.EAST, scrollPaneProjects, 454, 
				SpringLayout.WEST, panel_2);
		panel_2.add(scrollPaneProjects);
	}

	private void addScrollPane(JPanel panel_1, SpringLayout sl_panel_1) {
		scrollPane = new JScrollPane();
		sl_panel.putConstraint(SpringLayout.SOUTH, toolBar, -13, 
				SpringLayout.NORTH, scrollPane);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		sl_panel_1.putConstraint(SpringLayout.NORTH, scrollPane, 13, 
				SpringLayout.SOUTH, btnSearch);
		sl_panel_1.putConstraint(SpringLayout.WEST, scrollPane, 10, 
				SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, scrollPane, -33, 
				SpringLayout.SOUTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, scrollPane, 454, 
				SpringLayout.WEST, panel_1);
		panel_1.add(scrollPane);
	}

	private void addScrollPaneOne(JPanel panel_2, SpringLayout sl_panel_2,
			JLabel lblSelected) {
		scrollPaneCustomers = new JScrollPane();
		sl_panel_2.putConstraint(SpringLayout.NORTH, scrollPaneCustomers, 46, 
				SpringLayout.SOUTH, lblSelected);
		sl_panel_2.putConstraint(SpringLayout.WEST, scrollPaneCustomers, 10, 
				SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, scrollPaneCustomers, -560, 
				SpringLayout.SOUTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, scrollPaneCustomers, 454, 
				SpringLayout.WEST, panel_2);
		panel_2.add(scrollPaneCustomers);
	}

	private void createReportButton(JPanel panel_2, SpringLayout sl_panel_2) {
		btnGenerateReport = new JButton("Generate report");
		sl_panel_2.putConstraint(SpringLayout.EAST, btnGenerateReport, -29, 
				SpringLayout.EAST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.WEST, btnHelp, 0, 
				SpringLayout.WEST, btnGenerateReport);
		btnGenerateReport.setForeground(Color.blue);
		btnGenerateReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ExcelDocumentCreator creator = new ExcelDocumentCreator();
				creator.createReport(stmDisplayCust.getRowData(), 
						stmDisplayProj.getRowData(), stmDisplayStat.getRowData());
			}
		});
		sl_panel_2.putConstraint(SpringLayout.SOUTH, btnGenerateReport, -10,
				SpringLayout.SOUTH, panel_2);
		panel_2.add(btnGenerateReport);
	}

	private void createHelpButton(JPanel panel_2, SpringLayout sl_panel_2,
			JLabel lblSelected) {
		btnHelp = new JButton("Help");
		sl_panel_2.putConstraint(SpringLayout.EAST, lblSelected, -83,
				SpringLayout.WEST, btnHelp);
		sl_panel_2.putConstraint(SpringLayout.NORTH, btnExit, 0,
				SpringLayout.NORTH, btnHelp);
		sl_panel_2.putConstraint(SpringLayout.WEST, btnExit, 6,
				SpringLayout.EAST, btnHelp);
		sl_panel_2.putConstraint(SpringLayout.NORTH, btnHelp, 10,
				SpringLayout.NORTH, panel_2);
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel_2.add(btnHelp);
	}

	private void createExitButton(JPanel panel_2) {
		btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		panel_2.add(btnExit);
	}

	private JLabel createSelectedHeadline(Font headline, JPanel panel_2,
			SpringLayout sl_panel_2) {
		JLabel lblSelected = new JLabel("Selected");
		sl_panel_2.putConstraint(SpringLayout.NORTH, lblSelected, 10, 
				SpringLayout.NORTH, panel_2);
		lblSelected.setFont(headline);
		panel_2.add(lblSelected);
		return lblSelected;
	}

	private void createErrorLabelTwo(Font errorMessage, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblInvalidInput_1 = new JLabel("Invalid input");
		sl_panel.putConstraint(SpringLayout.NORTH, lblInvalidInput_1, 6, SpringLayout.NORTH, nameField);
		sl_panel.putConstraint(SpringLayout.WEST, lblInvalidInput_1, 6, SpringLayout.EAST, nameField);
		sl_panel.putConstraint(SpringLayout.EAST, lblInvalidInput_1, 0, 
				SpringLayout.EAST, btnSearch);
		lblInvalidInput_1.setForeground(Color.red);
		lblInvalidInput_1.setFont(errorMessage);
		lblInvalidInput_1.setVisible(false);
		panel_1.add(lblInvalidInput_1);
	}

	private void createErrorLabelOne(Font errorMessage, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblInvalidInput = new JLabel("Invalid input");
		sl_panel.putConstraint(SpringLayout.WEST, lblInvalidInput, 340, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, idField, -6, SpringLayout.WEST, lblInvalidInput);
		sl_panel.putConstraint(SpringLayout.NORTH, lblInvalidInput, 6, SpringLayout.NORTH, idField);
		sl_panel.putConstraint(SpringLayout.EAST, lblInvalidInput, -36, SpringLayout.EAST, panel);
		lblInvalidInput.setForeground(Color.red);
		lblInvalidInput.setFont(errorMessage);
		lblInvalidInput.setVisible(false);
		panel_1.add(lblInvalidInput);
	}

	private void createSearchButton(JPanel panel_1, SpringLayout sl_panel_1) {
		btnSearch = new JButton("Search");
		sl_panel.putConstraint(SpringLayout.NORTH, toolBar, 0, 
				SpringLayout.NORTH, btnSearch);
		sl_panel_1.putConstraint(SpringLayout.WEST, btnSearch, 364,
				SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, btnSearch, -664, 
				SpringLayout.SOUTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, btnSearch, -26, 
				SpringLayout.EAST, panel_1);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeSearch();
			}
		});
		panel_1.add(btnSearch);
	}

	private void createNameLabel(Font subheadline, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblName = new JLabel("Customer Name");
		sl_panel.putConstraint(SpringLayout.WEST, nameField, 38, SpringLayout.EAST, lblName);
		sl_panel.putConstraint(SpringLayout.NORTH, lblName, 4, 
				SpringLayout.NORTH, nameField);
		sl_panel.putConstraint(SpringLayout.WEST, lblName, 0,
				SpringLayout.WEST, toolBar);
		lblName.setFont(subheadline);
		panel_1.add(lblName);
	}

	private void createIdLabel(Font subheadline, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblID = new JLabel("Customer ID");
		sl_panel.putConstraint(SpringLayout.WEST, idField, 59, SpringLayout.EAST, lblID);
		sl_panel.putConstraint(SpringLayout.NORTH, lblID, 4, SpringLayout.NORTH, idField);
		sl_panel.putConstraint(SpringLayout.WEST, lblID, 0, SpringLayout.WEST, toolBar);
		lblID.setFont(subheadline);
		panel_1.add(lblID);
	}

	private void createNameField(JPanel panel_1, SpringLayout sl_panel_1) {
		nameField = new JTextField();
		sl_panel.putConstraint(SpringLayout.NORTH, nameField, 6, SpringLayout.SOUTH, idField);
		sl_panel.putConstraint(SpringLayout.SOUTH, nameField, -19, SpringLayout.NORTH, toolBar);
		sl_panel.putConstraint(SpringLayout.EAST, nameField, -140, SpringLayout.EAST, panel);
		panel_1.add(nameField);
		nameField.setColumns(10);
		nameField.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					executeSearch();
				}
			}
		});
	}

	private void createIdField(JPanel panel_1, SpringLayout sl_panel_1,
			JLabel lblSelectCustomers) {
		idField = new JTextField();
		sl_panel.putConstraint(SpringLayout.NORTH, idField, 26, 
				SpringLayout.SOUTH, lblSelection);
		sl_panel.putConstraint(SpringLayout.SOUTH, idField, -747, 
				SpringLayout.SOUTH, panel);
		panel_1.add(idField);
		idField.setColumns(10);
		idField.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					executeSearch();
				}
			}
		});
	}

	private JLabel createSelectionLabel(Font headline, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblSelection = new JLabel("Select Customers");
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblSelection, 0, 
				SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, lblSelection, 0, 
				SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, lblSelection, 50, 
				SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, lblSelection, 203, 
				SpringLayout.WEST, panel_1);
		lblSelection.setFont(headline);
		panel_1.add(lblSelection);
		return lblSelection;
	}

	private JPanel createPanelTwo(SpringLayout springLayout, JPanel panel_1) {
		JPanel panel_2 = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel_2, 10,
				SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_2, 6, 
				SpringLayout.EAST, panel_1);
		springLayout.putConstraint(SpringLayout.SOUTH, panel_2, 0, 
				SpringLayout.SOUTH, panel_1);
		springLayout.putConstraint(SpringLayout.EAST, panel_2, -10, 
				SpringLayout.EAST, frame.getContentPane());
		return panel_2;
	}

	private JPanel createPanelOne(SpringLayout springLayout) {
		panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 10,
				SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel, 10, 
				SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel, -10,
				SpringLayout.SOUTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel, -499, 
				SpringLayout.EAST, frame.getContentPane());
		panel.setBackground(Color.white);
		frame.getContentPane().add(panel);

		toolBar = new JToolBar();
		toolBar.setLayout(new GridLayout());
		toolBar.setFloatable(false);
		addButtons(toolBar);
		panel.add(toolBar);
		return panel;
	}

	private void addButtons(JToolBar toolBar) {
		bCustomers = new JButton("Customers");
		bCustomers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableCustomerSelection();
			}
		});
		bCustomers.setSelected(true);
		bCustomers.setBorder(BorderFactory.createSoftBevelBorder(0));
		toolBar.add(bCustomers);
		bProjects = new JButton("Projects");
		bProjects.setBorder(BorderFactory.createSoftBevelBorder(0));
		bProjects.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableProjectSelection();
			}
		});
		toolBar.add(bProjects);
		bStatuses = new JButton("Statuses");
		bStatuses.setBorder(BorderFactory.createSoftBevelBorder(0));
		bStatuses.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableStatusSelection();
			}
		});
		toolBar.add(bStatuses);
	}

	private SpringLayout createFrame() {
		frame = new JFrame();
		frame.setBounds(100, 100, 989, 906);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);
		frame.getContentPane().setBackground(Color.white);
		return springLayout;
	}

	private void enableCustomerSelection(){
		Active.setActiveTableModels(stmDisplayCust, stmSelectCust, table_customers);
		lblSelection.setText("Select Customers");
		lblName.setVisible(true);
		lblName.setText("Customer Name");
		lblID.setVisible(true);
		lblID.setText("Customer ID");
		nameField.setVisible(true);
		idField.setVisible(true);
		btnSearch.setVisible(true);

		table_selection.setModel(stmSelectCust);
		TableColumn tc = configureTableColumns(table_selection);		
		tc.setHeaderRenderer(header);

		table_customers.setBackground(Color.white);
		table_projects.setBackground(Color.lightGray);
		table_statuses.setBackground(Color.lightGray);
		scrollPaneProjects.getViewport().setBackground(Color.lightGray);
		scrollPaneStatuses.getViewport().setBackground(Color.lightGray);
		scrollPaneCustomers.getViewport().setBackground(Color.white);
		table_customers.setSelectionModel(partialSelectionModel);
		table_projects.setSelectionModel(nullSelectionModel);
		table_statuses.setSelectionModel(nullSelectionModel);
		nameField.setText("");
		nameField.grabFocus();

		bCustomers.setSelected(true);
		bProjects.setSelected(false);
		bStatuses.setSelected(false);
		synchronizeHeader();
	}

	private void enableProjectSelection(){
		Active.setActiveTableModels(stmDisplayProj, stmSelectProj, table_projects);
		lblSelection.setText("Select Projects");
		lblName.setVisible(true);
		lblName.setText("Project Name");
		lblID.setVisible(false);
		nameField.setVisible(true);
		idField.setVisible(false);
		btnSearch.setVisible(true);

		table_selection.setModel(stmSelectProj);
		TableColumn tc = configureTableColumns(table_selection);		
		tc.setHeaderRenderer(header);

		table_projects.setBackground(Color.white);
		table_customers.setBackground(Color.lightGray);
		table_statuses.setBackground(Color.lightGray);
		scrollPaneCustomers.getViewport().setBackground(Color.lightGray);
		scrollPaneStatuses.getViewport().setBackground(Color.lightGray);
		scrollPaneProjects.getViewport().setBackground(Color.white);
		table_projects.setSelectionModel(partialSelectionModel);
		table_customers.setSelectionModel(nullSelectionModel);
		table_statuses.setSelectionModel(nullSelectionModel);
		nameField.setText("");
		nameField.grabFocus();

		bCustomers.setSelected(false);
		bProjects.setSelected(true);
		bStatuses.setSelected(false);	
		synchronizeHeader();
	}

	private void enableStatusSelection(){
		Active.setActiveTableModels(stmDisplayStat, stmSelectStat, table_statuses);
		lblSelection.setText("Select Statuses");
		lblName.setVisible(false);
		lblID.setVisible(false);
		nameField.setVisible(false);
		idField.setVisible(false);
		btnSearch.setVisible(false);

		table_selection.setModel(stmSelectStat);
		TableColumn tc = configureTableColumns(table_selection);		
		tc.setHeaderRenderer(header);

		table_statuses.setBackground(Color.white);
		table_customers.setBackground(Color.lightGray);
		table_projects.setBackground(Color.lightGray);
		scrollPaneCustomers.getViewport().setBackground(Color.lightGray);
		scrollPaneProjects.getViewport().setBackground(Color.lightGray);
		scrollPaneStatuses.getViewport().setBackground(Color.white);
		table_statuses.setSelectionModel(partialSelectionModel);
		table_customers.setSelectionModel(nullSelectionModel);
		table_projects.setSelectionModel(nullSelectionModel);

		bCustomers.setSelected(false);
		bProjects.setSelected(false);
		bStatuses.setSelected(true);
		synchronizeHeader();
	}

	private TableColumn configureTableColumns(JTable table) {
		// TODO: set row sorter after searches 
		// watch out for interaction with select all
		// table.setAutoCreateRowSorter(true);
		if(Active.getActiveDisplayModel()==stmDisplayCust) table_selection.getColumnModel().getColumn(1).setMaxWidth(100);
		table.getColumnModel().getColumn(0).setMaxWidth(80);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		TableColumn tc = table.getColumnModel().getColumn(0);
		tc.setCellEditor(table.getDefaultEditor(Boolean.class));
		tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		return tc;
	}

	@SuppressWarnings("rawtypes")
	private void executeSearch() {
		try{
			if(!Active.getActiveSelectModel().getRowData().isEmpty()){
				Active.getActiveSelectModel().getRowData().clear();
				Active.getActiveSelectModel().fireTableDataChanged();
			}
			String name = nameField.getText().toLowerCase();
			String ID = idField.getText();
			if(Active.getActiveDisplayModel()==stmDisplayProj){
				for(Object[] item : data.getProjectData()){
					String project = ((String) item[1]).toLowerCase();
					Pattern p = Pattern.compile(".*" + name + ".*");
					Matcher m = p.matcher(project);
					if(m.matches()) Active.getActiveSelectModel().addRow(Arrays.asList(false, project));
				}
			}
			else{
				for(Object[] item : data.getCustomerData()){
					String customerName = ((String) item[2]).toLowerCase();
					String customerID = Integer.toString((int) item[1]);
					Pattern pName = Pattern.compile(".*" + name + ".*");
					Pattern pId = Pattern.compile(".*" + ID + ".*");
					Matcher mName = pName.matcher(customerName);
					Matcher mId = pId.matcher(customerID);
					if(ID.isEmpty()){
						if(mName.matches()) Active.getActiveSelectModel().addRow(Arrays.asList(false, customerID, customerName));
					}
					else if(!ID.isEmpty() && !name.isEmpty()){
						if(mName.matches() && mId.matches()) Active.getActiveSelectModel().addRow(Arrays.asList(false, customerID, customerName));
					}
					else{
						if(mId.matches()) Active.getActiveSelectModel().addRow(Arrays.asList(false, customerID, customerName));
					}
				}
			}
			for(List rowDisplay : Active.getActiveDisplayModel().getRowData()){
				SelectionTableModel model = Active.getActiveSelectModel();
				for(List rowSelect : model.getRowData()){
					if(rowDisplay.get(1).equals(rowSelect.get(1))){
						int index = model.getRowData().indexOf(rowSelect);
						model.setValueAt(true, index, 0);
					}
				}
			}
			synchronizeHeader();
		}catch(PatternSyntaxException e){
			e.printStackTrace();
		}
	}

	class MyItemListener implements ItemListener{
		public void itemStateChanged(ItemEvent e){
			if(headerClick){
				int min = 0;
				int max = table_selection.getRowCount() - 1;

				if(e.getStateChange() == ItemEvent.SELECTED
						&& (e.getSource() instanceof AbstractButton)){
					Active.getActiveDisplayModel().addRowInterval(min, max, table_selection);
				}
				else if(e.getStateChange() == ItemEvent.DESELECTED
						&& (e.getSource() instanceof AbstractButton)){
					Active.getActiveDisplayModel().partialRemoval(min, max, table_selection);
				}
			}
		}
	}

	class ListSelectionListenerImpl implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent e) {
			ListSelectionModel lsm = (ListSelectionModel)e.getSource();
			int min = lsm.getMinSelectionIndex();
			int max = lsm.getMaxSelectionIndex();
			boolean isAdjusting = e.getValueIsAdjusting();
			SelectionTableModel model = Active.getActiveDisplayModel();

			if(!lsm.isSelectionEmpty() && !isAdjusting){
				if(e.getSource() == table_selection.getSelectionModel()){
					if((boolean) table_selection.getValueAt(min, 0)) model.partialRemoval(min, max, table_selection);
					else model.addRowInterval(min, max, table_selection);
				}
				else if(e.getSource() == Active.getActiveDisplayTable().getSelectionModel()){
					model.removeRowInterval(min, max, table_selection);
				}
				lsm.clearSelection();
				synchronizeHeader();
			}
		}
	}

	private void synchronizeHeader(){
		boolean checked = true;
		if(table_selection.getRowCount() == 0) header.setSelected(false);
		else{ 
			for(int x = 0; x < table_selection.getRowCount(); x++){
				if(!(boolean) table_selection.getValueAt(x, 0)){
					checked = false;
				}
			}
			headerClick = false;
			header.setSelected(checked);
			frame.getContentPane().repaint();
			headerClick = true;
		}
	}
}

// TODO: Look into separating creating buttons and actions. Single Responsibility Principle

