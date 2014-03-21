package com.sff.report_performance;

import com.borland.dx.sql.dataset.Database;

public class DatabaseConnection {
	private static Database db;

	public DatabaseConnection(){
		db = new Database();
		db.setConnection(new com.borland.dx.sql.dataset.ConnectionDescriptor(
				"jdbc:sqlserver://" + "quillback" + ":1433;" +   // server name
						"DATABASENAME="     + "vendor",               // database name
						"hbs",               // login
						"hbs_sql",               // password
						false, "com.microsoft.sqlserver.jdbc.SQLServerDriver"));

		db.setUseTransactions(false);
	}

	public static Database getDatabase(){
		db.openConnection();
		return db;
	}

//	// TODO: I don't like this here. Does not fit the class name
//	public void loadStatusData(MyTableModel selectionModel){
//		getDatabase();
//
//		try{
//			Statement st = db.getJdbcConnection().createStatement();
//			st.setQueryTimeout(60);
//			ResultSet rs = st.executeQuery("select tr_dtl_stname from Tr_dtl_status");		      
//			while(rs.next()){
//				selectionModel.addRow(Arrays.asList(false, rs.getString(1).trim()));
//			}	
//			st.close();
//			rs.close();
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//		db.closeConnection();
//	}
}
