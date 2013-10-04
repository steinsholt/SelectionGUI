package hbs.sff.no;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.TableColumn;

public class GUI {

	private JFrame frame;
	private JButton btnExit;
	private JButton btnHelp;
	private JButton btnGenerateReport;
	private JTextField textField;
	private JTextField textField_1;
	private JLabel lblCustomerId;
	private JLabel lblCustomerName;
	private JButton btnSearch;
	private JLabel lblInvalidInput;
	private JLabel lblInvalidInput_1;
	private JScrollPane scrollPane;
	private JTable table_selection;
	private JScrollPane scrollPane_1;
	private JTable table_customers;
	private JScrollPane scrollPane_2;
	private JTable table_projects;
	private JScrollPane scrollPane_3;
	private JTable table_statuses;
	private SelectionTableModel stm_comp;
	private SelectionTableModel stm_proj;
	private SelectionTableModel stm_stat;

	public JFrame getFrame() {
		return frame;
	}

	public GUI() {
		initialize();
	}

	private void initialize() {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		SpringLayout springLayout = createFrame();

		Font headline = new Font("Serif", Font.PLAIN, 24);
		Font subheadline = new Font("Serif", Font.PLAIN, 16);
		Font errorMessage = new Font("Serif", Font.PLAIN, 14);

		JPanel panel_1 = createPanelOne(springLayout);
		JPanel panel_2 = createPanelTwo(springLayout, panel_1);

		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);

		JLabel lblSelectCustomers = createCustomerLabel(headline, panel_1,
				sl_panel_1);
		createIdField(panel_1, sl_panel_1, lblSelectCustomers);
		createNameField(panel_1, sl_panel_1);
		createIdLabel(subheadline, panel_1, sl_panel_1);
		createNameLabel(subheadline, panel_1, sl_panel_1);
		createSearchButton(panel_1, sl_panel_1);		
		createErrorLabelOne(errorMessage, panel_1, sl_panel_1);		
		createErrorLabelTwo(errorMessage, panel_1, sl_panel_1);		

		frame.getContentPane().add(panel_2);
		SpringLayout sl_panel_2 = new SpringLayout();
		panel_2.setLayout(sl_panel_2);

		JLabel lblSelected = createSelectedHeadline(headline, panel_2,
				sl_panel_2);

		createExitButton(panel_2);
		createHelpButton(panel_2, sl_panel_2, lblSelected);
		createReportButton(panel_2, sl_panel_2);				

		addScrollPaneOne(panel_2, sl_panel_2, lblSelected);		
		addTableCustomers();				
		addScrollPane(panel_1, sl_panel_1);		
		addTableSelection();		
		addScrollPaneTwo(panel_2, sl_panel_2);		
		addTableProjects();		
		addScrollPaneThree(panel_2, sl_panel_2);		
		addTableStatuses();		
	}

	private void addTableStatuses() {
		String[] colNames_stat = {"", "Status"};
		Object[][] data = {{true, "ALL"}};
		stm_stat = new SelectionTableModel(colNames_stat, data);
		table_statuses = new JTable(stm_stat);
		table_statuses.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table_statuses.setAutoCreateRowSorter(true);
		table_statuses.getColumnModel().getColumn(0).setMaxWidth(80);
		table_statuses.getTableHeader().setReorderingAllowed(false);
		table_statuses.getTableHeader().setResizingAllowed(false);
		TableColumn tc_1 = table_statuses.getColumnModel().getColumn(0);
		tc_1.setCellEditor(table_statuses.getDefaultEditor(Boolean.class));
		tc_1.setCellRenderer(table_statuses.getDefaultRenderer(Boolean.class));
		scrollPane_3.setViewportView(table_statuses);
		scrollPane_3.getViewport().setBackground(Color.white);
	}

	private void addTableProjects() {
		String[] colNames_proj = {"", "Project"};
		Object[][] data = {{true, "ALL"}};
		stm_proj = new SelectionTableModel(colNames_proj, data);
		table_projects = new JTable(stm_proj);
		table_projects.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table_projects.setAutoCreateRowSorter(true);
		table_projects.getColumnModel().getColumn(0).setMaxWidth(80);
		table_projects.getTableHeader().setReorderingAllowed(false);
		table_projects.getTableHeader().setResizingAllowed(false);
		TableColumn tc_1 = table_projects.getColumnModel().getColumn(0);
		tc_1.setCellEditor(table_projects.getDefaultEditor(Boolean.class));
		tc_1.setCellRenderer(table_projects.getDefaultRenderer(Boolean.class));
		scrollPane_2.setViewportView(table_projects);
		scrollPane_2.getViewport().setBackground(Color.white);
	}

	private Object[][] addTableCustomers() {
		String[] colNames_comp = {"", "ID", "Company"};
		Object[][] data = {{true, "ALL", ""}};
		stm_comp = new SelectionTableModel(colNames_comp, data);
		table_customers = new JTable(stm_comp);
		table_customers.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table_customers.setAutoCreateRowSorter(true);
		table_customers.getColumnModel().getColumn(0).setMaxWidth(80);
		table_customers.getTableHeader().setReorderingAllowed(false);
		table_customers.getTableHeader().setResizingAllowed(false);
		TableColumn tc_1 = table_customers.getColumnModel().getColumn(0);
		tc_1.setCellEditor(table_customers.getDefaultEditor(Boolean.class));
		tc_1.setCellRenderer(table_customers.getDefaultRenderer(Boolean.class));
		scrollPane_1.setViewportView(table_customers);
		scrollPane_1.getViewport().setBackground(Color.white);
		return data;
	}

	private void addTableSelection() {
		String[] colNames_sComp = {"", "ID", "Company"};
		Object[][] data = {};
		stm_comp = new SelectionTableModel(colNames_sComp, data);
		table_selection = new JTable(stm_comp);
		table_selection.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table_selection.getColumnModel().getColumn(0).setMaxWidth(80);
		table_selection.getTableHeader().setReorderingAllowed(false);
		table_selection.getTableHeader().setResizingAllowed(false);
		TableColumn tc_1 = table_selection.getColumnModel().getColumn(0);
		tc_1.setCellEditor(table_selection.getDefaultEditor(Boolean.class));
		tc_1.setCellRenderer(table_selection.getDefaultRenderer(Boolean.class));
		tc_1.setHeaderRenderer(new CheckBoxHeader(new MyItemListener()));
		scrollPane.setViewportView(table_selection);
		scrollPane.getViewport().setBackground(Color.white);
	}

	private void addScrollPaneThree(JPanel panel_2, SpringLayout sl_panel_2) {
		scrollPane_3 = new JScrollPane();
		sl_panel_2.putConstraint(SpringLayout.NORTH, scrollPane_3, 27, 
				SpringLayout.SOUTH, scrollPane_2);
		sl_panel_2.putConstraint(SpringLayout.WEST, scrollPane_3, 10, 
				SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, scrollPane_3, 227, 
				SpringLayout.SOUTH, scrollPane_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, scrollPane_3, 454, 
				SpringLayout.WEST, panel_2);
		panel_2.add(scrollPane_3);
	}

	private void addScrollPaneTwo(JPanel panel_2, SpringLayout sl_panel_2) {
		scrollPane_2 = new JScrollPane();
		sl_panel_2.putConstraint(SpringLayout.NORTH, scrollPane_2, 318, 
				SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.WEST, scrollPane_2, 10, 
				SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, scrollPane_2, 230, 
				SpringLayout.SOUTH, scrollPane_1);
		sl_panel_2.putConstraint(SpringLayout.EAST, scrollPane_2, 454, 
				SpringLayout.WEST, panel_2);
		panel_2.add(scrollPane_2);
	}

	private void addScrollPane(JPanel panel_1, SpringLayout sl_panel_1) {
		scrollPane = new JScrollPane();
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
		scrollPane_1 = new JScrollPane();
		sl_panel_2.putConstraint(SpringLayout.NORTH, scrollPane_1, 46, 
				SpringLayout.SOUTH, lblSelected);
		sl_panel_2.putConstraint(SpringLayout.WEST, scrollPane_1, 10, 
				SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, scrollPane_1, -560, 
				SpringLayout.SOUTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, scrollPane_1, 454, 
				SpringLayout.WEST, panel_2);
		panel_2.add(scrollPane_1);
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
		lblInvalidInput_1.setForeground(Color.red);
		lblInvalidInput_1.setFont(errorMessage);
		lblInvalidInput_1.setVisible(false);
		sl_panel_1.putConstraint(SpringLayout.EAST, lblInvalidInput, 0,
				SpringLayout.EAST, lblInvalidInput_1);
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblInvalidInput_1, 10, 
				SpringLayout.NORTH, textField_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, lblInvalidInput_1, 6,
				SpringLayout.EAST, textField_1);
		panel_1.add(lblInvalidInput_1);
	}

	private void createErrorLabelOne(Font errorMessage, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblInvalidInput = new JLabel("Invalid input");
		lblInvalidInput.setForeground(Color.red);
		lblInvalidInput.setFont(errorMessage);
		lblInvalidInput.setVisible(false);
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblInvalidInput, 10, 
				SpringLayout.NORTH, textField);
		panel_1.add(lblInvalidInput);
	}

	private void createSearchButton(JPanel panel_1, SpringLayout sl_panel_1) {
		btnSearch = new JButton("Search");
		sl_panel_1.putConstraint(SpringLayout.WEST, btnSearch, 364,
				SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, btnSearch, -664, 
				SpringLayout.SOUTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, btnSearch, -26, 
				SpringLayout.EAST, panel_1);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel_1.add(btnSearch);
	}

	private void createNameLabel(Font subheadline, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblCustomerName = new JLabel("Customer name");
		sl_panel_1.putConstraint(SpringLayout.WEST, lblCustomerName, 10, 
				SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, textField_1, 43,
				SpringLayout.EAST, lblCustomerName);
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblCustomerName, 4, 
				SpringLayout.NORTH, textField_1);
		lblCustomerName.setFont(subheadline);
		panel_1.add(lblCustomerName);
	}

	private void createIdLabel(Font subheadline, JPanel panel_1,
			SpringLayout sl_panel_1) {
		lblCustomerId = new JLabel("Customer ID");
		sl_panel_1.putConstraint(SpringLayout.WEST, textField, 59, 
				SpringLayout.EAST, lblCustomerId);
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblCustomerId, 4, 
				SpringLayout.NORTH, textField);
		sl_panel_1.putConstraint(SpringLayout.WEST, lblCustomerId, 10, 
				SpringLayout.WEST, panel_1);
		lblCustomerId.setFont(subheadline);
		panel_1.add(lblCustomerId);
	}

	private void createNameField(JPanel panel_1, SpringLayout sl_panel_1) {
		textField_1 = new JTextField();
		sl_panel_1.putConstraint(SpringLayout.EAST, textField_1, -130, 
				SpringLayout.EAST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, textField, -6,
				SpringLayout.NORTH, textField_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, textField_1, -706,
				SpringLayout.SOUTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.NORTH, textField_1, 107,
				SpringLayout.NORTH, panel_1);
		panel_1.add(textField_1);
		textField_1.setColumns(10);
	}

	private void createIdField(JPanel panel_1, SpringLayout sl_panel_1,
			JLabel lblSelectCustomers) {
		textField = new JTextField();
		sl_panel_1.putConstraint(SpringLayout.NORTH, textField, 16, 
				SpringLayout.SOUTH, lblSelectCustomers);
		sl_panel_1.putConstraint(SpringLayout.EAST, textField, -130, 
				SpringLayout.EAST, panel_1);
		panel_1.add(textField);
		textField.setColumns(10);
	}

	private JLabel createCustomerLabel(Font headline, JPanel panel_1,
			SpringLayout sl_panel_1) {
		JLabel lblSelectCustomers = new JLabel("Select Customers");
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblSelectCustomers, 0, 
				SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, lblSelectCustomers, 0, 
				SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, lblSelectCustomers, 50, 
				SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, lblSelectCustomers, 203, 
				SpringLayout.WEST, panel_1);
		lblSelectCustomers.setFont(headline);
		panel_1.add(lblSelectCustomers);
		return lblSelectCustomers;
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
		JPanel panel_1 = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel_1, 10,
				SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_1, 10, 
				SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel_1, -10,
				SpringLayout.SOUTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel_1, -499, 
				SpringLayout.EAST, frame.getContentPane());
		panel_1.setBackground(Color.white);
		frame.getContentPane().add(panel_1);
		return panel_1;
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

	class MyItemListener implements ItemListener{
		public void itemStateChanged(ItemEvent e){
			Object source = e.getSource();
			if(source instanceof AbstractButton == false) return;
			boolean checked =  e.getStateChange() == ItemEvent.SELECTED;
			for(int x = 0, y = table_customers.getRowCount(); x < y; x++){
				table_customers.setValueAt(new Boolean(checked), x, 0);
			}
		}
	}
}
