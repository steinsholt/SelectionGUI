package com.sff.report_performance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
		frameIdMap.put("ALL", -1);
		categoryIdMap = new HashMap<String, Integer>();
		categoryIdMap.put("ALL", 0);
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
				frameIdMap.clear();
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

//				select distinct project_id, pr_name 
//				  from Tr_hdr, Project 
//				  where Tr_hdr.active_id = Project.project_id
//				  and Tr_hdr.fr_agr_cat_id = 1001 // where 1001 is the chosen fr_agr_cat_id
				
//				 select distinct Tr_category.category_id, category_name 
//				  from Tr_hdr, Tr_category 
//				  where Tr_hdr.category_id = Tr_category.category_id
//				  and Tr_hdr.assoc_id in (10935, 20106)
//				  and Tr_hdr.active_id in (85, 96)
//				  and Tr_hdr.fr_agr_cat_id = 1001
				
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
				categoryIdMap.clear();
				while(rs.next()){
					selectionModel.addRow(Arrays.asList(false, rs.getInt(1), rs.getString(2).trim()));
					categoryIdMap.put(rs.getString(2).trim(), rs.getInt(3)); 
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
				/*
				 * For each client name in display model get category from categoryIdMap
				 */

				queryConditions.setLength(0);
				HashSet<Integer> catIdSet = new HashSet<Integer>();

				if(displayModel.getRowCount() != 0){
					for(List rowDisplay : displayModel.getRowData()){
						catIdSet.add(categoryIdMap.get(rowDisplay.get(2)));
					}

					queryConditions.append("where category_id in " + Arrays.toString(catIdSet.toArray(new Integer[catIdSet.size()])).replace("[", "(").replace("]", ")"));
				}
				rs = st.executeQuery("select category_id, category_name "
						+ "from vendor.dbo.Tr_category "
						+ queryConditions
						+ " union select 0, ' ALL' "
						+ "order by category_name");

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