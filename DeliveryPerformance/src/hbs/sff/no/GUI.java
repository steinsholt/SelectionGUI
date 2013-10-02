package hbs.sff.no;

import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JList;

public class GUI {

	private JFrame frame;

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
		
		ButtonGroup group = new ButtonGroup();
		
		springLayout.putConstraint(SpringLayout.WEST, panel_2, 499,
				SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel_2, 840,
				SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel_2, -10,
				SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(panel_2);
		SpringLayout sl_panel_2 = new SpringLayout();
		panel_2.setLayout(sl_panel_2);
		
		JList list = new JList();
		sl_panel_2.putConstraint(SpringLayout.NORTH, list, 84, SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.WEST, list, 10, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, list, 284, SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, list, 454, SpringLayout.WEST, panel_2);
		panel_2.add(list);
		
		JList list_1 = new JList();
		sl_panel_2.putConstraint(SpringLayout.NORTH, list_1, 26, SpringLayout.SOUTH, list);
		sl_panel_2.putConstraint(SpringLayout.WEST, list_1, 10, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, list_1, 226, SpringLayout.SOUTH, list);
		sl_panel_2.putConstraint(SpringLayout.EAST, list_1, 454, SpringLayout.WEST, panel_2);
		panel_2.add(list_1);
		
		JList list_2 = new JList();
		sl_panel_2.putConstraint(SpringLayout.NORTH, list_2, 26, SpringLayout.SOUTH, list_1);
		sl_panel_2.putConstraint(SpringLayout.WEST, list_2, 10, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, list_2, 226, SpringLayout.SOUTH, list_1);
		sl_panel_2.putConstraint(SpringLayout.EAST, list_2, 454, SpringLayout.WEST, panel_2);
		panel_2.add(list_2);
	}
}
