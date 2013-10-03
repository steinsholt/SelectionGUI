package hbs.sff.no;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class GUI {

	private JFrame frame;
	private JTable table;
	private JTable table_1;
	private JTable table_2;
	private JTable table_3;
	private JButton btnExit;
	private JButton btnHelp;
	private JButton btnGenerateReport;
	private JTextField textField;
	private JTextField textField_1;
	private JLabel lblCustomerId;
	private JLabel lblCustomerName;
	private JButton btnSearch;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
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

		Font headline = new Font("Serif", Font.PLAIN, 24);
		Font subheadline = new Font("Serif", Font.PLAIN, 16);

		frame = new JFrame();
		frame.setBounds(100, 100, 989, 906);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);
		frame.getContentPane().setBackground(Color.gray);

		JPanel panel_1 = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel_1, 10,
				SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_1, 10, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel_1, -10,
				SpringLayout.SOUTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel_1, -499, SpringLayout.EAST, frame.getContentPane());
		panel_1.setBackground(Color.white);
		frame.getContentPane().add(panel_1);

		JPanel panel_2 = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel_2, 10, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_2, 6, SpringLayout.EAST, panel_1);
		springLayout.putConstraint(SpringLayout.SOUTH, panel_2, 0, SpringLayout.SOUTH, panel_1);
		springLayout.putConstraint(SpringLayout.EAST, panel_2, -10, SpringLayout.EAST, frame.getContentPane());
		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);

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

		table_3 = new JTable();
		sl_panel_1.putConstraint(SpringLayout.SOUTH, table_3, -88, SpringLayout.SOUTH, panel_1);
		table_3.setBorder(BorderFactory.createLineBorder(Color.black));
		sl_panel_1.putConstraint(SpringLayout.WEST, table_3, 10, 
				SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, table_3, 438, 
				SpringLayout.WEST, panel_1);
		panel_1.add(table_3);

		textField = new JTextField();
		sl_panel_1.putConstraint(SpringLayout.NORTH, textField, 16, SpringLayout.SOUTH, lblSelectCustomers);
		sl_panel_1.putConstraint(SpringLayout.EAST, textField, -130, SpringLayout.EAST, panel_1);
		panel_1.add(textField);
		textField.setColumns(10);

		textField_1 = new JTextField();
		sl_panel_1.putConstraint(SpringLayout.SOUTH, textField, -6, SpringLayout.NORTH, textField_1);
		sl_panel_1.putConstraint(SpringLayout.NORTH, table_3, 48, SpringLayout.SOUTH, textField_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, textField_1, -706, SpringLayout.SOUTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.NORTH, textField_1, 107, SpringLayout.NORTH, panel_1);
		panel_1.add(textField_1);
		textField_1.setColumns(10);

		lblCustomerId = new JLabel("Customer ID");
		sl_panel_1.putConstraint(SpringLayout.WEST, textField, 59, SpringLayout.EAST, lblCustomerId);
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblCustomerId, 4, 
				SpringLayout.NORTH, textField);
		sl_panel_1.putConstraint(SpringLayout.WEST, lblCustomerId, 10, SpringLayout.WEST, panel_1);
		lblCustomerId.setFont(subheadline);
		panel_1.add(lblCustomerId);

		lblCustomerName = new JLabel("Customer name");
		sl_panel_1.putConstraint(SpringLayout.WEST, textField_1, 43, SpringLayout.EAST, lblCustomerName);
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblCustomerName, 4, SpringLayout.NORTH, textField_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, lblCustomerName, 0, SpringLayout.WEST, table_3);
		lblCustomerName.setFont(subheadline);
		panel_1.add(lblCustomerName);

		btnSearch = new JButton("Search");
		sl_panel_1.putConstraint(SpringLayout.WEST, btnSearch, 364, SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, textField_1, -30, SpringLayout.WEST, btnSearch);
		sl_panel_1.putConstraint(SpringLayout.NORTH, btnSearch, 113, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, btnSearch, -26, SpringLayout.EAST, panel_1);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel_1.add(btnSearch);
		frame.getContentPane().add(panel_2);
		SpringLayout sl_panel_2 = new SpringLayout();
		panel_2.setLayout(sl_panel_2);

		table = new JTable();
		table.setBorder(BorderFactory.createLineBorder(Color.black));
		sl_panel_2.putConstraint(SpringLayout.WEST, table, 10, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, table, -559, SpringLayout.SOUTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, table, -29, SpringLayout.EAST, panel_2);
		panel_2.add(table);

		table_1 = new JTable();
		table_1.setBorder(BorderFactory.createLineBorder(Color.black));
		sl_panel_2.putConstraint(SpringLayout.NORTH, table_1, 17, SpringLayout.SOUTH, table);
		sl_panel_2.putConstraint(SpringLayout.WEST, table_1, 0, SpringLayout.WEST, table);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, table_1, -327, 
				SpringLayout.SOUTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, table_1, 454, 
				SpringLayout.WEST, panel_2);
		panel_2.add(table_1);

		table_2 = new JTable();
		table_2.setBorder(BorderFactory.createLineBorder(Color.black));
		sl_panel_2.putConstraint(SpringLayout.NORTH, table_2, 21, SpringLayout.SOUTH, table_1);
		sl_panel_2.putConstraint(SpringLayout.WEST, table_2, 10, 
				SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, table_2, 454, 
				SpringLayout.WEST, panel_2);
		panel_2.add(table_2);

		JLabel lblSelected = new JLabel("Selected");
		sl_panel_2.putConstraint(SpringLayout.NORTH, table, 29, SpringLayout.SOUTH, lblSelected);
		sl_panel_2.putConstraint(SpringLayout.NORTH, lblSelected, 10, SpringLayout.NORTH, panel_2);
		lblSelected.setFont(headline);
		panel_2.add(lblSelected);

		btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel_2.add(btnExit);

		btnHelp = new JButton("Help");
		sl_panel_2.putConstraint(SpringLayout.EAST, lblSelected, -83, SpringLayout.WEST, btnHelp);
		sl_panel_2.putConstraint(SpringLayout.NORTH, btnExit, 0,
				SpringLayout.NORTH, btnHelp);
		sl_panel_2.putConstraint(SpringLayout.WEST, btnExit, 6, SpringLayout.EAST, btnHelp);
		sl_panel_2.putConstraint(SpringLayout.NORTH, btnHelp, 10, SpringLayout.NORTH, panel_2);
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel_2.add(btnHelp);

		btnGenerateReport = new JButton("Generate report");
		sl_panel_2.putConstraint(SpringLayout.SOUTH, table_2, -58, SpringLayout.NORTH, btnGenerateReport);
		sl_panel_2.putConstraint(SpringLayout.WEST, btnHelp, 0, SpringLayout.WEST, btnGenerateReport);
		btnGenerateReport.setForeground(Color.blue);
		btnGenerateReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		sl_panel_2.putConstraint(SpringLayout.SOUTH, btnGenerateReport, -10,
				SpringLayout.SOUTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, btnGenerateReport, 0,
				SpringLayout.EAST, table);
		panel_2.add(btnGenerateReport);
	}
}
