package hbs.sff.no;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
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
	private JPanel leftPanel;
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
	private Regex regex;
	private SpringLayout sl_panel_3;
	private JPanel displayPanel;
	private Font bold;
	private SpringLayout springLayout_1;
	private JButton button;
	private JLabel label;
	private JPanel buttonPanel;
	private JLabel label_1;
	private JPanel reportPanel;

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
		partialSelectionModel.addListSelectionListener(new ListSelectionListenerImpl());

		bold = new Font("Serif", Font.BOLD, 12);
		Font headline = new Font("Serif", Font.PLAIN, 24);
		Font subheadline = new Font("Serif", Font.PLAIN, 16);
		//		Font errorMessage = new Font("Serif", Font.PLAIN, 14);

		leftPanel = new JPanel();
		leftPanel.setBackground(Color.white);
		frame.getContentPane().add(leftPanel);

		sl_panel = new SpringLayout();
		leftPanel.setLayout(sl_panel);
		scrollPane = new JScrollPane();
		sl_panel.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, leftPanel);
		sl_panel.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, leftPanel);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, leftPanel);
		sl_panel.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, leftPanel);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 454, SpringLayout.WEST, leftPanel);
		leftPanel.add(scrollPane);

		stmSelectCust = new SelectionTableModel(colNames_sComp);
		stmSelectProj = new SelectionTableModel(colNames_sProj);
		stmSelectStat = new SelectionTableModel(colNames_sStat);

		addTables();
		databaseConnection = new DatabaseConnection();
		databaseConnection.loadStatusData(stmSelectStat);

		JPanel rightPanel = new JPanel();
		{
			buttonPanel = new JPanel();
			sl_panel.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, buttonPanel);
			springLayout_1.putConstraint(SpringLayout.EAST, buttonPanel, -22, SpringLayout.EAST, scrollPane);
			springLayout_1.putConstraint(SpringLayout.WEST, buttonPanel, 0, SpringLayout.WEST, leftPanel);
			springLayout_1.putConstraint(SpringLayout.NORTH, buttonPanel, 0, SpringLayout.NORTH, leftPanel);
			leftPanel.add(buttonPanel);
			buttonPanel.setPreferredSize(new Dimension(460,150));
			buttonPanel.setBackground(Color.white);
			SpringLayout sl_panel_5 = new SpringLayout();
			buttonPanel.setLayout(sl_panel_5);
			{
				label_1 = new JLabel("Select Customers");
				sl_panel_5.putConstraint(SpringLayout.NORTH, label_1, 10, SpringLayout.NORTH, buttonPanel);
				sl_panel_5.putConstraint(SpringLayout.WEST, label_1, 10, SpringLayout.WEST, buttonPanel);
				label_1.setFont(headline);
				buttonPanel.add(label_1);
			}
			lblID = new JLabel("Customer ID");
			sl_panel_5.putConstraint(SpringLayout.NORTH, lblID, 6, SpringLayout.SOUTH, label_1);
			sl_panel_5.putConstraint(SpringLayout.WEST, lblID, 0, SpringLayout.WEST, label_1);
			buttonPanel.add(lblID);
			lblID.setFont(subheadline);
			lblName = new JLabel("Customer Name");
			sl_panel_5.putConstraint(SpringLayout.WEST, lblName, 0, SpringLayout.WEST, label_1);
			buttonPanel.add(lblName);
			lblName.setFont(subheadline);
			
			PlainDocument doc = createDocumentFilter();
			idField = new JTextField();
			idField.setDocument(doc);
			sl_panel_5.putConstraint(SpringLayout.NORTH, idField, 9, SpringLayout.SOUTH, label_1);
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
			sl_panel_5.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, label_1);
			sl_panel_5.putConstraint(SpringLayout.SOUTH, toolBar, 10, SpringLayout.SOUTH, buttonPanel);
			sl_panel_5.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, idField);
			buttonPanel.add(toolBar);
			toolBar.setLayout(new GridLayout());
			toolBar.setFloatable(false);
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
			btnSearch = new JButton("Search");
			sl_panel_5.putConstraint(SpringLayout.SOUTH, btnSearch, -8, SpringLayout.SOUTH, buttonPanel);
			sl_panel_5.putConstraint(SpringLayout.SOUTH, toolBar, 0, SpringLayout.SOUTH, btnSearch);
			sl_panel_5.putConstraint(SpringLayout.EAST, btnSearch, -10, SpringLayout.EAST, buttonPanel);
			buttonPanel.add(btnSearch);
			btnSearch.setForeground(Color.blue);
			btnSearch.setFont(bold);
			btnSearch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					regex.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), databaseConnection, nameField, idField);
					synchronizeHeader();
				}
			});
			nameField.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e){
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						regex.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), databaseConnection, nameField, idField);
						synchronizeHeader();
					}
				}
			});
			idField.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e){
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						regex.executeSearch(Active.getActiveSelectModel(), Active.getActiveDisplayModel(), databaseConnection, nameField, idField);
						synchronizeHeader();
					}
				}
			});
		}
		springLayout_1.putConstraint(SpringLayout.SOUTH, rightPanel, -10, SpringLayout.SOUTH, frame.getContentPane());
		frame.getContentPane().add(rightPanel);
		rightPanel.setLayout(new SpringLayout());

		JPanel helpPanel = new JPanel();
		helpPanel.setMinimumSize(new Dimension(100, 100));
		rightPanel.add(helpPanel);
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
		rightPanel.add(displayPanel);
		sl_panel_3 = new SpringLayout();
		displayPanel.setLayout(sl_panel_3);

		scrollPaneCustomers = new JScrollPane();
		displayPanel.add(scrollPaneCustomers);	
		scrollPaneProjects = new JScrollPane();
		displayPanel.add(scrollPaneProjects);	
		scrollPaneStatuses = new JScrollPane();
		displayPanel.add(scrollPaneStatuses);	
		SpringUtilities.makeGrid(displayPanel,3,1,0,0,0,5);
		table_statuses = new JTable(stmDisplayStat);
		table_statuses.setName("statuses");
		configureTableColumns(table_statuses);
		scrollPaneStatuses.setViewportView(table_statuses);
		table_projects = new JTable(stmDisplayProj);
		table_projects.setName("projects");
		configureTableColumns(table_projects);
		scrollPaneProjects.setViewportView(table_projects);
		table_customers = new JTable(stmDisplayCust);
		table_customers.setName("customers");
		configureTableColumns(table_customers);
		table_customers.getColumnModel().getColumn(1).setMaxWidth(50);
		scrollPaneCustomers.setViewportView(table_customers);

		// TODO: Fix overlapping panels after resize
		
		reportPanel = new JPanel();
		reportPanel.setMinimumSize(new Dimension(100,100));
		rightPanel.add(reportPanel);
		SpringLayout sl_panel_6 = new SpringLayout();
		reportPanel.setLayout(sl_panel_6);
		btnGenerateReport = new JButton("Generate report");
		sl_panel_6.putConstraint(SpringLayout.EAST, btnGenerateReport, -5, SpringLayout.EAST, reportPanel);
		reportPanel.add(btnGenerateReport);
		btnGenerateReport.setForeground(Color.blue);
		btnGenerateReport.setFont(bold);
		btnGenerateReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean isFileUnlocked = false;
				FileOutputStream out = null;
				File output = null;
				String directory = "C:/vendorLogistics/Logistics/vendorUser/";
				String fileName = "report.xlsx";
				try{
					output = new File(directory + fileName);
					out = new FileOutputStream(output, false);
				    isFileUnlocked = true;
				}catch(IOException e){
					isFileUnlocked = false;
				}
				if(isFileUnlocked){
					@SuppressWarnings("unused")
					ProgressDialog dialogFrame = new ProgressDialog(stmDisplayCust.getRowData(), 
							stmDisplayProj.getRowData(), stmDisplayStat.getRowData(), out, output, frame);
				}
				else{
					JOptionPane.showMessageDialog(frame, "Please close file " + fileName + " before generating a new report");
				}
			}
		});
		sl_panel_6.putConstraint(SpringLayout.SOUTH, btnGenerateReport, 0, SpringLayout.SOUTH, reportPanel);
		SpringUtilities.makeCompactGrid(rightPanel, 3, 1, 0, 0, 0, 5);
		SpringUtilities.makeGrid(frame.getContentPane(),1,2,0,0,10,10);
		enableCustomerSelection();		
	}

	private PlainDocument createDocumentFilter() {
		PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new DocumentFilter() {
		    @Override
		    public void insertString(FilterBypass fb, int off, String str, AttributeSet attr) 
		        throws BadLocationException 
		    {
		        fb.insertString(off, str.replaceAll("\\D++", ""), attr);
		    } 
		    @Override
		    public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr) 
		        throws BadLocationException 
		    {
		        fb.replace(off, len, str.replaceAll("\\D++", ""), attr); 
		    }
		});
		return doc;
	}

	public JFrame getFrame() {
		return frame;
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
				c.setForeground(color);
				return c;
			}
		};
		table_selection.setName("selection");
		table_selection.getSelectionModel().addListSelectionListener(new ListSelectionListenerImpl());
		TableColumn tc = configureTableColumns(table_selection);
		header = new CheckBoxHeader(new SelectionHeaderListener());
		tc.setHeaderRenderer(header);

		scrollPane.setViewportView(table_selection);
		scrollPane.getViewport().setBackground(Color.white);

		stmDisplayStat = new SelectionTableModel(colNames_sStat);
		stmDisplayProj = new SelectionTableModel(colNames_sProj);
		stmDisplayCust = new SelectionTableModel(colNames_sComp);

		//		table_selection.setAutoCreateRowSorter(true);
		//		table_statuses.setAutoCreateRowSorter(true);
		//		table_customers.setAutoCreateRowSorter(true);
		//		table_projects.setAutoCreateRowSorter(true);

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

	private void enableCustomerSelection(){
		Active.setActiveTableModels(stmDisplayCust, stmSelectCust, table_customers);
		label_1.setText("Select Customers");
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
		label_1.setText("Select Projects");
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
		label_1.setText("Select Item Statuses");
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
		if(Active.getActiveDisplayModel()==stmDisplayCust) table_selection.getColumnModel().getColumn(1).setMaxWidth(50);
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
			CheckBoxHeader checkboxHeader = new CheckBoxHeader(new DisplayHeaderListener());
			checkboxHeader.setEnabled(false);
			checkboxHeader.setSelected(true);
			tc.setHeaderRenderer(checkboxHeader);
		}
		return tc;
	}
	
	private void synchDisplayHeaders(JTable activeTable){
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
	
	// TODO: disable headers when table not active, setEnabled(false)
	// TODO: synch display headers
	class DisplayHeaderListener implements ItemListener{
		public void itemStateChanged(ItemEvent e){
			SelectionTableModel model = Active.getActiveDisplayModel();
			if(e.getStateChange() == ItemEvent.SELECTED && model != null){
				JTable table = Active.getActiveDisplayTable();
				if(model.getRowData().isEmpty()){
					((CheckBoxHeader) table.getColumnModel().getColumn(0).getHeaderRenderer()).setSelected(true);
				}
				else model.removeRowInterval(0, model.getRowData().size() - 1, table_selection);
				synchDisplayHeaders(Active.getActiveDisplayTable());
			}
		}
	}

	class SelectionHeaderListener implements ItemListener{
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
				synchDisplayHeaders(Active.getActiveDisplayTable());
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
				synchDisplayHeaders(Active.getActiveDisplayTable());
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
}

// TODO: Look into separating creating buttons and actions. Single Responsibility Principle

