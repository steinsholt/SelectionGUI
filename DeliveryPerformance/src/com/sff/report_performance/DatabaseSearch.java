package com.sff.report_performance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.borland.dx.sql.dataset.Database;
import com.sff.report_performance.GUI.State;

public class DatabaseSearch {
	private HashMap<String, Integer> frameIdMap;
	private HashMap<String, Integer> projectMap;
	
	public DatabaseSearch(){
		frameIdMap = new HashMap<String, Integer>();
		projectMap = new HashMap<String, Integer>();
	}
	
	@SuppressWarnings("rawtypes")
	public void executeSearch(MyTableModel selectionModel, MyTableModel displayModel, DatabaseConnection data, JTextField nameField,
			JTextField idField, State state, DefaultTableModel model) {
		try{
			String name = nameField.getText();
			String ID = idField.getText();
			Database db = DatabaseConnection.getDatabase();
			Statement st = db.getJdbcConnection().createStatement();
			st.setQueryTimeout(60);
			ResultSet rs = null;
			String queryConditions = "";

			switch(state){
			case FRAME:
				rs = st.executeQuery("select fr_agr_cat_id, frame_cat_name "
						+ "from vendor.dbo.Frame_agr_catalog "
						+ "where fr_agr_cat_id>0 "
						+ "and frame_cat_name like '" + name + "%'" 
						+ "union select -1, ' ALL' order by frame_cat_name");
				
				model.setRowCount(0); 
				while(rs.next()){
					model.addRow(new Object[]{rs.getString(2).trim()});
					frameIdMap.put(rs.getString(2).trim(), rs.getInt(1));
					// TODO: add id to a list. use in project search with "where fr_agr_id like id"
				}
				break;

			case PROJECT:
				if(!selectionModel.getRowData().isEmpty()){
					selectionModel.getRowData().clear();
					selectionModel.fireTableDataChanged();
				}
				
				rs = st.executeQuery("select pr_name, project_id from Project where pr_name like '" + name + "%'" + queryConditions);
				while(rs.next()){
					selectionModel.addRow(Arrays.asList(false, rs.getString(1).trim()));
					projectMap.put(rs.getString(1).trim(), rs.getInt(2));
				}
				
				for(List rowDisplay : displayModel.getRowData()){
					for(List rowSelect : selectionModel.getRowData()){
						if(rowDisplay.get(1).equals(rowSelect.get(1))){
							int index = selectionModel.getRowData().indexOf(rowSelect);
							selectionModel.setValueAt(true, index, 0);
						}
					}
				}
				break;

			case CLIENT:
				if(!selectionModel.getRowData().isEmpty()){
					selectionModel.getRowData().clear();
					selectionModel.fireTableDataChanged();
				}
				
				rs = st.executeQuery("select assoc_id, assoc_name"
						+ " from Assoc customerList"
						+ " where assoc_id like '" + ID + "%'"
						+ " and assoc_id<20000"
						+ " and assoc_name like '" + name + "%'" + queryConditions);
				while(rs.next()){
					selectionModel.addRow(Arrays.asList(false, rs.getInt(1), rs.getString(2).trim()));
				}
				
				for(List rowDisplay : displayModel.getRowData()){
					for(List rowSelect : selectionModel.getRowData()){
						if(rowDisplay.get(1).equals(rowSelect.get(1))){
							int index = selectionModel.getRowData().indexOf(rowSelect);
							selectionModel.setValueAt(true, index, 0);
						}
					}
				}
				break;

			case CATEGORY:
				rs = st.executeQuery("select category_id, category_name "
						+ "from vendor.dbo.Tr_category "
						+ "union select 0, ' ALL' "
						+ "order by category_name" + queryConditions);
				
				model.setRowCount(0); 
				while(rs.next()){
					model.addRow(new Object[]{rs.getString(2).trim()});
				}
				break;
			}

			st.close(); //TODO : newly added
			db.closeConnection();
		}catch(PatternSyntaxException | SQLException e){
			e.printStackTrace();
		}
	}
}