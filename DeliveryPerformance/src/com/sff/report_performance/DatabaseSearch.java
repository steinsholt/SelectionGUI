package com.sff.report_performance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.borland.dx.sql.dataset.Database;
import com.sff.report_performance.GUI.State;

public class DatabaseSearch {
	
	// switch on state, instead of synching displayModel synch on textField when needed.
	@SuppressWarnings("rawtypes")
	public static void executeCheckboxSearch(MyTableModel selectionModel, MyTableModel displayModel, DatabaseConnection data, JTextField nameField,
			JTextField idField) {
		try{
			/*
			 * Clears the table after previous searches
			 */
			if(!selectionModel.getRowData().isEmpty()){
				selectionModel.getRowData().clear();
				selectionModel.fireTableDataChanged();
			}
			String name = nameField.getText();
			String ID = idField.getText();
			Database db = DatabaseConnection.getDatabase();
			Statement st = db.getJdbcConnection().createStatement();
			st.setQueryTimeout(60);
			if(selectionModel.getColumnCount()==2){
				ResultSet rs = st.executeQuery("select pr_name from Project where pr_name like '" + name + "%'");
				while(rs.next()){
					selectionModel.addRow(Arrays.asList(false, rs.getString(1).trim()));
				}
			}
			else{
				ResultSet rs = st.executeQuery("select assoc_id, assoc_name"
						+ " from Assoc customerList"
						+ " where assoc_id like '" + ID + "%'"
						+ " and assoc_id<20000"
						+ " and assoc_name like '" + name + "%'");
				while(rs.next()){
					selectionModel.addRow(Arrays.asList(false, rs.getInt(1), rs.getString(2).trim()));
				}
			}
			/*
			 * Synchronizes the currently selectable lists
			 */
			for(List rowDisplay : displayModel.getRowData()){
				for(List rowSelect : selectionModel.getRowData()){
					if(rowDisplay.get(1).equals(rowSelect.get(1))){
						int index = selectionModel.getRowData().indexOf(rowSelect);
						selectionModel.setValueAt(true, index, 0);
					}
				}
			}
			st.close(); //TODO : newly added
			db.closeConnection();
		}catch(PatternSyntaxException | SQLException e){
			e.printStackTrace();
		}
	}
	public static void executeStandardSearch(DatabaseConnection data, JTextField nameField, DefaultTableModel model, State state){
		try {
			model.setRowCount(0); 
			String name = nameField.getText();
			Database db = DatabaseConnection.getDatabase();
			Statement st = db.getJdbcConnection().createStatement();
			st.setQueryTimeout(10);
			ResultSet rs = null;
			
			switch(state){
			case CATEGORY:
				rs = st.executeQuery("");
				break;
			case FRAME:
				rs = st.executeQuery("select fr_agr_cat_id, frame_cat_name "
						+ "from vendor.dbo.Frame_agr_catalog "
						+ "where fr_agr_cat_id>0 "
						+ "and frame_cat_name like '" + name + "%'" 
						+ "union select -1, ' ALL' order by frame_cat_name");
				break;
			default:break;
			}
			while(rs.next()){
				model.addRow(new Object[]{rs.getInt(1),rs.getString(2).trim()}); // Perhaps the model is missing columns? try adding random names to the model at initialization
			}
			st.close();
			db.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
