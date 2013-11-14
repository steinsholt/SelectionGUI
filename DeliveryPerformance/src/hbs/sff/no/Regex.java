package hbs.sff.no;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.JTextField;

import com.borland.dx.sql.dataset.Database;

/*
 * This class is used to match the user search input to the data loaded from the
 * database, then display it in the tables and finally synchronize the 
 * associated tables
 */
public class Regex {
	@SuppressWarnings("rawtypes")
	public void executeSearch(SelectionTableModel selectionModel, SelectionTableModel displayModel, DatabaseConnection data, JTextField nameField,
			JTextField idField) {
		try{
			/*
			 * Clears the table after previous searches
			 */
			if(!selectionModel.getRowData().isEmpty()){
				selectionModel.getRowData().clear();
				selectionModel.fireTableDataChanged();
			}
			/*
			 * Checks the current selection model then attempts to match the 
			 * user input with the customer name and/or customer id
			 */
			String name = nameField.getText().toLowerCase();
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
		}catch(PatternSyntaxException | SQLException e){
			e.printStackTrace();
		}
	}
}
