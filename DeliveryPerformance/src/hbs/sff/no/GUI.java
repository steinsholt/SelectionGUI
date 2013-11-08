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
	private Regex regex;
	private SpringLayout sl_panel_3;
	private JPanel panel_3;
	private JLabel lblSelected_1;
	private Font bold;

	public static enum Active{
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

		regex = new Regex();

		headerClick = true;
		addColumnNames();
		SpringLayout springLayout = createFrame();
		nullSelectionModel = new NullSelectionModel();
		partialSelectionModel = new PartialSelectionModel();
		partialSelectionModel.addListSelectionListener(new 
				ListSelectionListenerImpl());
		
		bold = new Font("Serif", Font.BOLD, 12);
		Font headline = new Font("Serif", Font.PLAIN, 24);
		Font subheadline = new Font("Serif", Font.PLAIN, 16);
		Font errorMessage = new Font("Serif", Font.PLAIN, 14);
		JPanel panel_1 = createPanelOne(springLayout);
		JPanel panel_2 = createPanelTwo(springLayout, panel_1);
		sl_panel = new SpringLayout();
		sl_panel.putConstraint(SpringLayout.NORTH, toolBar, 124, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, toolBar, 10, SpringLayout.WEST, panel);
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
		sl_panel_3 = new SpringLayout();
		panel_2.setLayout(sl_panel_3);
		JLabel lblSelected = createSelectedHeadline(headline, panel_2,
				sl_panel_3);

		createExitButton(panel_2);
		createHelpButton(panel_2, sl_panel_3, lblSelected);
		createReportButton(panel_2, sl_panel_3);				
		addScrollPaneOne(panel_2, sl_panel_3, lblSelected);		
		addScrollPane(panel_1, sl_panel);		
		addScrollPaneTwo(panel_2, sl_panel_3);		
		addScrollPaneThree(panel_2, sl_panel_3);	

		stmSelectCust = new SelectionTableModel(colNames_sComp);
		stmSelectProj = new SelectionTableModel(colNames_sProj);
		stmSelectStat = new SelectionTableModel(colNames_sStat);

		addTables();
		data = new Data();
		data.loadDataAtStartup();
		for(Object[] item : data.getStatusData()){
			stmSelectStat.addRow(Arrays.asList(item));
		}
		enableCustomerSelection();		
	}

	private void addColumnNames() {
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

		//		table_selection.setAutoCreateRowSorter(true);
		//		table_statuses.setAutoCreateRowSorter(true);
		//		table_customers.setAutoCreateRowSorter(true);
		//		table_projects.setAutoCreateRowSorter(true);

	}

	private void addScrollPaneThree(JPanel panel_2, SpringLayout sl_panel_2) {
		scrollPaneStatuses = new JScrollPane();
		sl_panel_3.putConstraint(SpringLayout.SOUTH, scrollPaneProjects, -6, SpringLayout.NORTH, scrollPaneStatuses);
		sl_panel_3.putConstraint(SpringLayout.WEST, scrollPaneStatuses, 9, SpringLayout.WEST, panel_3);
		sl_panel_3.putConstraint(SpringLayout.EAST, scrollPaneStatuses, 0, SpringLayout.EAST, btnHelp);
		sl_panel_3.putConstraint(SpringLayout.NORTH, scrollPaneStatuses, 558, SpringLayout.NORTH, panel_3);
		sl_panel_3.putConstraint(SpringLayout.SOUTH, scrollPaneStatuses, -27, SpringLayout.NORTH, btnGenerateReport);
		panel_2.add(scrollPaneStatuses);
	}

	private void addScrollPaneTwo(JPanel panel_2, SpringLayout springLayout) {
		scrollPaneProjects = new JScrollPane();
		sl_panel_3.putConstraint(SpringLayout.NORTH, scrollPaneProjects, 312, SpringLayout.NORTH, panel_3);
		sl_panel_3.putConstraint(SpringLayout.WEST, scrollPaneCustomers, 0, SpringLayout.WEST, scrollPaneProjects);
		sl_panel_3.putConstraint(SpringLayout.SOUTH, scrollPaneCustomers, -6, SpringLayout.NORTH, scrollPaneProjects);
		sl_panel_3.putConstraint(SpringLayout.WEST, scrollPaneProjects, 9, SpringLayout.WEST, panel_3);
		sl_panel_3.putConstraint(SpringLayout.EAST, scrollPaneProjects, -30, SpringLayout.EAST, panel_3);
		panel_2.add(scrollPaneProjects);
	}

	private void addScrollPane(JPanel panel_1, SpringLayout springLayout) {
		scrollPane = new JScrollPane();
		sl_panel.putConstraint(SpringLayout.SOUTH, toolBar, -25, SpringLayout.NORTH, scrollPane);
		sl_panel.putConstraint(SpringLayout.NORTH, scrollPane, 172, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, scrollPane, -33, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, scrollPane, 20, SpringLayout.EAST, panel);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panel_1);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 454, SpringLayout.WEST, panel_1);
		panel_1.add(scrollPane);
	}

	private void addScrollPaneOne(JPanel panel_2, SpringLayout sl_panel_2,
			JLabel lblSelected) {
		scrollPaneCustomers = new JScrollPane();
		sl_panel_3.putConstraint(SpringLayout.NORTH, scrollPaneCustomers, 24, SpringLayout.SOUTH, lblSelected_1);
		sl_panel_3.putConstraint(SpringLayout.EAST, scrollPaneCustomers, -30, SpringLayout.EAST, panel_3);
		panel_2.add(scrollPaneCustomers);
	}

	private void createReportButton(JPanel panel_2, SpringLayout sl_panel_2) {
		btnGenerateReport = new JButton("Generate report");
		sl_panel_2.putConstraint(SpringLayout.EAST, btnGenerateReport, -29, 
				SpringLayout.EAST, panel_2);
		btnGenerateReport.setForeground(Color.blue);
		btnGenerateReport.setFont(bold);
		btnGenerateReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DialogFrame dialogFrame = new DialogFrame();
				dialogFrame.setLocationRelativeTo(frame);
				dialogFrame.runReport(stmDisplayCust.getRowData(), 
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
		sl_panel_3.putConstraint(SpringLayout.NORTH, btnHelp, 10, SpringLayout.NORTH, lblSelected_1);
		sl_panel_3.putConstraint(SpringLayout.EAST, btnHelp, -29, SpringLayout.EAST, panel_3);
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel_2.add(btnHelp);
	}

	private void createExitButton(JPanel panel_2) {
	}

	private JLabel createSelectedHeadline(Font headline, JPanel panel_2,
			SpringLayout sl_panel_2) {
		lblSelected_1 = new JLabel("Report Parameters");
		sl_panel_2.putConstraint(SpringLayout.NORTH, lblSelected_1, 10, 
				SpringLayout.NORTH, panel_2);
		sl_panel_3.putConstraint(SpringLayout.WEST, lblSelected_1, 74, SpringLayout.WEST, panel_3);
		lblSelected_1.setFont(headline);
		panel_2.add(lblSelected_1);
		return lblSelected_1;
	}

	private void createErrorLabelTwo(Font errorMessage, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblInvalidInput_1 = new JLabel("Invalid input");
		sl_panel.putConstraint(SpringLayout.NORTH, lblInvalidInput_1, 10, SpringLayout.SOUTH, lblInvalidInput);
		sl_panel.putConstraint(SpringLayout.WEST, lblInvalidInput_1, 0, SpringLayout.WEST, btnSearch);
		sl_panel.putConstraint(SpringLayout.EAST, lblInvalidInput_1, 4, SpringLayout.EAST, btnSearch);
		lblInvalidInput_1.setForeground(Color.red);
		lblInvalidInput_1.setFont(errorMessage);
		lblInvalidInput_1.setVisible(false);
		panel_1.add(lblInvalidInput_1);
	}

	private void createErrorLabelOne(Font errorMessage, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblInvalidInput = new JLabel("Invalid input");
		sl_panel.putConstraint(SpringLayout.NORTH, lblInvalidInput, 48, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblInvalidInput, 0, SpringLayout.WEST, btnSearch);
		lblInvalidInput.setForeground(Color.red);
		lblInvalidInput.setFont(errorMessage);
		lblInvalidInput.setVisible(false);
		panel_1.add(lblInvalidInput);
	}

	private void createSearchButton(JPanel panel_1, SpringLayout sl_panel_1) {
		btnSearch = new JButton("Search");
		sl_panel.putConstraint(SpringLayout.WEST, btnSearch, 350, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, toolBar, -16, SpringLayout.WEST, btnSearch);
		sl_panel.putConstraint(SpringLayout.NORTH, btnSearch, 0, SpringLayout.NORTH, toolBar);
		btnSearch.setForeground(Color.blue);
		btnSearch.setFont(bold);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regex.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), data, nameField, idField);
				synchronizeHeader();
			}
		});
		panel_1.add(btnSearch);
	}

	private void createNameLabel(Font subheadline, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblName = new JLabel("Customer Name");
		sl_panel.putConstraint(SpringLayout.NORTH, lblName, 7, SpringLayout.SOUTH, lblID);
		sl_panel.putConstraint(SpringLayout.WEST, nameField, 29, SpringLayout.EAST, lblName);
		sl_panel.putConstraint(SpringLayout.WEST, lblName, 0, SpringLayout.WEST, toolBar);
		lblName.setFont(subheadline);
		panel_1.add(lblName);
	}

	private void createIdLabel(Font subheadline, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblID = new JLabel("Customer ID");
		sl_panel.putConstraint(SpringLayout.WEST, idField, 50, SpringLayout.EAST, lblID);
		sl_panel.putConstraint(SpringLayout.NORTH, lblID, 46, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblID, 0, SpringLayout.WEST, toolBar);
		lblID.setFont(subheadline);
		panel_1.add(lblID);
	}

	private void createNameField(JPanel panel_1, SpringLayout sl_panel_1) {
		nameField = new JTextField();
		sl_panel.putConstraint(SpringLayout.SOUTH, idField, -6, SpringLayout.NORTH, nameField);
		sl_panel.putConstraint(SpringLayout.EAST, nameField, 0, SpringLayout.EAST, toolBar);
		sl_panel.putConstraint(SpringLayout.NORTH, nameField, 77, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, nameField, -24, SpringLayout.NORTH, toolBar);
		panel_1.add(nameField);
		nameField.setColumns(10);
		nameField.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					regex.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), data, nameField, idField);
					synchronizeHeader();
				}
			}
		});
	}

	private void createIdField(JPanel panel_1, SpringLayout sl_panel_1,
			JLabel lblSelectCustomers) {
		idField = new JTextField();
		sl_panel.putConstraint(SpringLayout.NORTH, idField, 16, SpringLayout.SOUTH, lblSelection);
		sl_panel.putConstraint(SpringLayout.EAST, idField, 0, SpringLayout.EAST, toolBar);
		panel_1.add(idField);
		idField.setColumns(10);
		idField.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					regex.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), data, nameField, idField);
					synchronizeHeader();
				}
			}
		});
	}

	private JLabel createSelectionLabel(Font headline, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblSelection = new JLabel("Select Customers");
		sl_panel.putConstraint(SpringLayout.NORTH, lblSelection, 0, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblSelection, 0, SpringLayout.WEST, panel);
		lblSelection.setFont(headline);
		panel_1.add(lblSelection);
		return lblSelection;
	}

	private JPanel createPanelTwo(SpringLayout springLayout, JPanel panel_1) {
		panel_3 = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel_3, 10,
				SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_3, 6, 
				SpringLayout.EAST, panel_1);
		springLayout.putConstraint(SpringLayout.SOUTH, panel_3, 0, 
				SpringLayout.SOUTH, panel_1);
		springLayout.putConstraint(SpringLayout.EAST, panel_3, -10, 
				SpringLayout.EAST, frame.getContentPane());
		return panel_3;
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
		bStatuses = new JButton("Item Statuses");
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
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
		table_customers.setForeground(Color.black);
		table_projects.setForeground(Color.gray);
		table_statuses.setForeground(Color.gray);
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
		table_projects.setForeground(Color.black);
		table_customers.setForeground(Color.gray);
		table_statuses.setForeground(Color.gray);
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
		lblSelection.setText("Select Item Statuses");
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
		table_statuses.setForeground(Color.black);
		table_customers.setForeground(Color.gray);
		table_projects.setForeground(Color.gray);
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
		if(Active.getActiveDisplayModel()==stmDisplayCust) table_selection.getColumnModel().getColumn(1).setMaxWidth(100);
		table.getColumnModel().getColumn(0).setMaxWidth(80);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		TableColumn tc = table.getColumnModel().getColumn(0);
		tc.setCellEditor(table.getDefaultEditor(Boolean.class));
		tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		return tc;
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

