package hbs.sff.no;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JList;
import javax.swing.JTable;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUI {

	private JFrame frame;
	private JTable table;
	private JTable table_1;
	private JTable table_2;
	private JTable table_3;
	private JButton btnExit;
	private JButton btnHelp;
	private JButton btnGenerateReport;

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
		Font serif = new Font("Serif", Font.PLAIN, 24); 
		frame = new JFrame();
		frame.setBounds(100, 100, 989, 906);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);

		JPanel panel_1 = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel_1, 25,
				SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_1, 10,
				SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel_1, -28,
				SpringLayout.SOUTH, frame.getContentPane());
		frame.getContentPane().add(panel_1);
		
		JPanel panel_2 = new JPanel();
		springLayout.putConstraint(SpringLayout.EAST, panel_1, -41,
				SpringLayout.WEST, panel_2);
		springLayout.putConstraint(SpringLayout.NORTH, panel_2, 0,
				SpringLayout.NORTH, panel_1);
		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);
		
		JLabel lblSelectCustomers = new JLabel("Select Customers");
		lblSelectCustomers.setFont(serif);
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblSelectCustomers, 10, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, lblSelectCustomers, 10, SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, lblSelectCustomers, 60, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, lblSelectCustomers, 213, SpringLayout.WEST, panel_1);
		panel_1.add(lblSelectCustomers);
		
		table_3 = new JTable();
		sl_panel_1.putConstraint(SpringLayout.NORTH, table_3, 49, SpringLayout.SOUTH, lblSelectCustomers);
		sl_panel_1.putConstraint(SpringLayout.WEST, table_3, 10, SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, table_3, 667, SpringLayout.SOUTH, lblSelectCustomers);
		sl_panel_1.putConstraint(SpringLayout.EAST, table_3, 438, SpringLayout.WEST, panel_1);
		panel_1.add(table_3);
		
		springLayout.putConstraint(SpringLayout.WEST, panel_2, 499,
				SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel_2, 840,
				SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel_2, -10,
				SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(panel_2);
		SpringLayout sl_panel_2 = new SpringLayout();
		panel_2.setLayout(sl_panel_2);
		
		table = new JTable();
		sl_panel_2.putConstraint(SpringLayout.NORTH, table, 71, SpringLayout.NORTH, panel_2);
		panel_2.add(table);
		
		table_1 = new JTable();
		sl_panel_2.putConstraint(SpringLayout.WEST, table, 0, SpringLayout.WEST, table_1);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, table, -28, SpringLayout.NORTH, table_1);
		sl_panel_2.putConstraint(SpringLayout.EAST, table, 0, SpringLayout.EAST, table_1);
		sl_panel_2.putConstraint(SpringLayout.NORTH, table_1, 299, SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, table_1, -316, SpringLayout.SOUTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.WEST, table_1, 10, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, table_1, 454, SpringLayout.WEST, panel_2);
		panel_2.add(table_1);
		
		table_2 = new JTable();
		sl_panel_2.putConstraint(SpringLayout.NORTH, table_2, 524, SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.WEST, table_2, 10, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, table_2, 225, SpringLayout.SOUTH, table_1);
		sl_panel_2.putConstraint(SpringLayout.EAST, table_2, 454, SpringLayout.WEST, panel_2);
		panel_2.add(table_2);
		
		JLabel lblSelected = new JLabel("Selected");
		lblSelected.setFont(serif);
		sl_panel_2.putConstraint(SpringLayout.NORTH, lblSelected, 22, SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, lblSelected, -209, SpringLayout.EAST, panel_2);
		panel_2.add(lblSelected);
		
		btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		sl_panel_2.putConstraint(SpringLayout.NORTH, btnExit, 0, SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, btnExit, 0, SpringLayout.EAST, panel_2);
		panel_2.add(btnExit);
		
		btnHelp = new JButton("Help");
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		sl_panel_2.putConstraint(SpringLayout.SOUTH, btnHelp, 0, SpringLayout.SOUTH, btnExit);
		sl_panel_2.putConstraint(SpringLayout.EAST, btnHelp, -6, SpringLayout.WEST, btnExit);
		panel_2.add(btnHelp);
		
		btnGenerateReport = new JButton("Generate report");
		btnGenerateReport.setForeground(Color.blue);
		btnGenerateReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		sl_panel_2.putConstraint(SpringLayout.SOUTH, btnGenerateReport, -10, SpringLayout.SOUTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, btnGenerateReport, 0, SpringLayout.EAST, table);
		panel_2.add(btnGenerateReport);
	}
}
