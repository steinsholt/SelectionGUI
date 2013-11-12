package hbs.sff.no;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

/*
 * This custom JFrame is shown during the excel document creation. The progress
 * made during the creation is posted to this frame and shown on the progress
 * bar and in the text fields.
 */
public class DialogFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField stateField;
	private JTextField progressField;
	private JProgressBar progressBar;
	private ExcelDocumentCreator creator;

	public DialogFrame(){
		this.setVisible(true);
		this.setResizable(false);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent evt){
				creator.cancel(true);
				DialogFrame.this.dispose();
			}
		});
		setTitle("Creating report");
		setBounds(100, 100, 560, 189);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
		sl_contentPane.putConstraint(SpringLayout.NORTH, panel, -5, SpringLayout.NORTH, contentPane);
		panel.setBackground(Color.white);
		sl_contentPane.putConstraint(SpringLayout.WEST, panel, -5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, panel, 5, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, panel, -50, SpringLayout.SOUTH, contentPane);
		contentPane.add(panel);
		
		JPanel panel_1 = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.WEST, panel_1, -5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, panel_1, 5, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, panel_1, 90, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, panel_1, 5, SpringLayout.EAST, contentPane);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		progressBar = new JProgressBar();
		sl_panel.putConstraint(SpringLayout.NORTH, progressBar, -50, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, progressBar, 20, SpringLayout.WEST, panel);
		panel.add(progressBar);
		
		stateField = new JTextField("Querying Database");
		sl_panel.putConstraint(SpringLayout.EAST, progressBar, 247, SpringLayout.EAST, stateField);
		sl_panel.putConstraint(SpringLayout.NORTH, stateField, 10, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, stateField, 10, SpringLayout.WEST, panel);
		stateField.setBorder(BorderFactory.createEmptyBorder());
		panel.add(stateField);
		stateField.setColumns(10);
		
		progressField = new JTextField();
		sl_panel.putConstraint(SpringLayout.SOUTH, progressBar, -6, SpringLayout.NORTH, progressField);
		sl_panel.putConstraint(SpringLayout.WEST, progressField, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, progressField, -257, SpringLayout.EAST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, stateField, 0, SpringLayout.EAST, progressField);
		progressField.setBorder(BorderFactory.createEmptyBorder());
		sl_panel.putConstraint(SpringLayout.SOUTH, progressField, -10, SpringLayout.SOUTH, panel);
		panel.add(progressField);
		progressField.setColumns(10);
		contentPane.add(panel_1);
		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				creator.cancel(true);
			}
		});
		sl_panel_1.putConstraint(SpringLayout.SOUTH, btnCancel, -10, SpringLayout.SOUTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, btnCancel, -10, SpringLayout.EAST, panel_1);
		panel_1.add(btnCancel);
		this.requestFocus();
	}
	
	@SuppressWarnings("rawtypes")
	public void runReport(List<List> customerData, List<List> projectData, List<List> statusData, FileOutputStream out, File output){
		creator = new ExcelDocumentCreator(customerData, 
				projectData, statusData, stateField, progressField, out, output);
		creator.addPropertyChangeListener(new PropertyChangeListener(){
			@Override
            public void propertyChange(PropertyChangeEvent evt) {
				if(creator.isDone()) DialogFrame.this.dispose();
				
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setValue((Integer) evt.getNewValue());
                    progressBar.setIndeterminate(false);
                }
            }
		});
		creator.execute();
		progressBar.setIndeterminate(true);
	}
}
