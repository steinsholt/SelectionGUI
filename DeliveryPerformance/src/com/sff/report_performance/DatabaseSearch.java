package com.sff.report_performance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.borland.dx.sql.dataset.Database;
import com.sff.report_performance.GUI.State;

public class DatabaseSearch {
	private HashMap<String, Integer> frameIdMap;
	private HashMap<String, Integer> categoryIdMap;
	
	public DatabaseSearch(){
		frameIdMap = new HashMap<String, Integer>();
		categoryIdMap = new HashMap<String, Integer>();
	}
	
	@SuppressWarnings("rawtypes")
	public void executeSearch(MyTableModel selectionModel, MyTableModel displayModel, DatabaseConnection data, JTextField nameField,
			JTextField idField, State state, DefaultTableModel model, JTextField frameAgrField) {
		try{
			String name = nameField.getText();
			String ID = idField.getText();
			String frameAgr = frameAgrField.getText();
			Database db = DatabaseConnection.getDatabase();
			Statement st = db.getJdbcConnection().createStatement();
			st.setQueryTimeout(60);
			ResultSet rs = null;
			StringBuilder queryConditions = new StringBuilder("");

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
				}
				break;

			case PROJECT:
				queryConditions.setLength(0);
				if(!selectionModel.getRowData().isEmpty()){
					selectionModel.getRowData().clear();
					selectionModel.fireTableDataChanged();
				}
				
				if(!frameAgr.equalsIgnoreCase("ALL")){
					rs = st.executeQuery("select fr_agr_id from vendor.dbo.Frame_agr where fr_agr_cat_id like '" + frameIdMap.get(frameAgr) + "%'");
					queryConditions.append(" and fr_agr_id in (");
					
					while(rs.next()){
						queryConditions.append(rs.getInt(1) + ","); 
					}
					queryConditions.delete(queryConditions.length()-1, queryConditions.length());
					queryConditions.append(")");
				}
				
				rs = st.executeQuery("select pr_name, project_id from Project where pr_name like '" + name + "%'" + queryConditions);
				while(rs.next()){
					selectionModel.addRow(Arrays.asList(false, rs.getString(1).trim()));
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
				queryConditions.setLength(0);
				if(!selectionModel.getRowData().isEmpty()){
					selectionModel.getRowData().clear();
					selectionModel.fireTableDataChanged();
				}
				if(!frameAgr.equalsIgnoreCase("ALL")) queryConditions.append("and fr_agr_cat_id like '" + frameIdMap.get(frameAgr) + "%'");
				
				rs = st.executeQuery("select assoc_id, assoc_name, category_id"
						+ " from Assoc customerList"
						+ " where assoc_id like '" + ID + "%'"
						+ " and assoc_id<20000"
						+ " and assoc_name like '" + name + "%'" + queryConditions);
				while(rs.next()){
					selectionModel.addRow(Arrays.asList(false, rs.getInt(1), rs.getString(2).trim()));
					categoryIdMap.put(rs.getString(2), rs.getInt(3)); // TODO: Use this to narrow the category query
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
				queryConditions.setLength(0);
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