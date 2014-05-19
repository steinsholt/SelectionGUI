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
}
