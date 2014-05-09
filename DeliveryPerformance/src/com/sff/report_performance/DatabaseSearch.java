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
	private HashMap<String, Integer> projectIdMap;

	public DatabaseSearch(){
		frameIdMap = new HashMap<String, Integer>();
		frameIdMap.put("ALL", -1);
		projectIdMap = new HashMap<String, Integer>();
	}

	@SuppressWarnings("rawtypes")
	public void executeSearch(MyTableModel selectionModel, MyTableModel clientModel, MyTableModel projectModel, DatabaseConnection data, JTextField nameField,
			JTextField idField, State state, DefaultTableModel model, JTextField frameAgrField) {
		try{
			String name = nameField.getText();
			String ID = idField.getText();
			String frameAgr = frameAgrField.getText();
			Database db = DatabaseConnection.getDatabase();
			Statement st = db.getJdbcConnection().createStatement();
			st.setQueryTimeout(60);
			ResultSet rs = null;
			String frameAgrCatId = "";
			String activeId = "";
			String assocId = "";

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
				if(!selectionModel.getRowData().isEmpty()){
					selectionModel.getRowData().clear();
					selectionModel.fireTableDataChanged();
				}
				
				frameAgrCatId = "";
				if(!frameAgr.equalsIgnoreCase("ALL")){
					frameAgrCatId = " and Tr_hdr.fr_agr_cat_id = " + frameIdMap.get(frameAgr);
				}

				rs = st.executeQuery("select distinct project_id, pr_name "
						+ "from Tr_hdr, Project "
						+ "where Tr_hdr.active_id = Project.project_id "
						+ "and pr_name like '" + name + "%'"
						+ frameAgrCatId);
				
				projectIdMap.clear();
				while(rs.next()){
					selectionModel.addRow(Arrays.asList(false, rs.getString(2).trim()));
					projectIdMap.put(rs.getString(2).trim(), rs.getInt(1));
				}

				for(List rowDisplay : projectModel.getRowData()){
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
				
				activeId = "";
				HashSet<Integer> projectIdSet = new HashSet<Integer>();
				if(projectModel.getRowCount() != 0){
					for(List rowDisplay : projectModel.getRowData()){
						projectIdSet.add(projectIdMap.get(rowDisplay.get(1)));
					}
					activeId = " and Tr_hdr.active_id in " + Arrays.toString(projectIdSet.toArray(new Integer[projectIdSet.size()])).replace("[", "(").replace("]", ")");
				}
				
				rs = st.executeQuery("select distinct Assoc.assoc_id, assoc_name"
						+ " from Tr_hdr, Assoc"
						+ " where Tr_hdr.assoc_id = Assoc.assoc_id"
						+ " and Assoc.assoc_id like '" + ID + "%'"
						+ " and assoc_name like '" + name + "%'" 
						+ frameAgrCatId
						+ activeId);
				while(rs.next()){
					selectionModel.addRow(Arrays.asList(false, rs.getInt(1), rs.getString(2).trim()));
				}

				for(List rowDisplay : clientModel.getRowData()){
					for(List rowSelect : selectionModel.getRowData()){
						if(rowDisplay.get(1).equals(rowSelect.get(1))){
							int index = selectionModel.getRowData().indexOf(rowSelect);
							selectionModel.setValueAt(true, index, 0);
						}
					}
				}
				break;

			case CATEGORY:
				assocId = "";
				HashSet<Integer> clientIdSet = new HashSet<Integer>();
				if(clientModel.getRowCount() != 0){
					for(List rowDisplay : clientModel.getRowData()){
						clientIdSet.add((Integer) rowDisplay.get(1));
					}
					assocId = " and Tr_hdr.assoc_id in " + Arrays.toString(clientIdSet.toArray(new Integer[clientIdSet.size()])).replace("[", "(").replace("]", ")");
				}
				
				rs = st.executeQuery("select distinct Tr_category.category_id, category_name"
						+ " from Tr_hdr, Tr_category"
						+ " where Tr_hdr.category_id = Tr_category.category_id"
						+ frameAgrCatId
						+ activeId
						+ assocId
						+ " union select 0, ' ALL'"
						+ " order by category_name");
				
				model.setRowCount(0); 
				while(rs.next()){
					model.addRow(new Object[]{rs.getString(2).trim()});
				}
				break;
			}

			st.close(); 
			db.closeConnection();
		}catch(PatternSyntaxException | SQLException e){
			e.printStackTrace();
		}
	}
}

//select distinct project_id, pr_name 
//from Tr_hdr, Project 
//where Tr_hdr.active_id = Project.project_id
//and Tr_hdr.fr_agr_cat_id = 1001 // where 1001 is the chosen fr_agr_cat_id

//select distinct Tr_category.category_id, category_name 
//from Tr_hdr, Tr_category 
//where Tr_hdr.category_id = Tr_category.category_id
//and Tr_hdr.assoc_id in (10935, 20106)
//and Tr_hdr.active_id in (85, 96)
//and Tr_hdr.fr_agr_cat_id = 1001