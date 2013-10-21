package hbs.sff.no;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.borland.dx.sql.dataset.Database;

public class Data {
	private List<Object[]> statusData;
	private List<Object[]> customerData;
	private List<Object[]> projectData;
	private static Database db;

	public enum Type{
		STATUS, CUSTOMER, PROJECT
	}

	public Data(){
		statusData = new ArrayList<Object[]>();
		projectData = new ArrayList<Object[]>();
		customerData = new ArrayList<Object[]>();
		
		db = new Database();
		db.setConnection(new com.borland.dx.sql.dataset.ConnectionDescriptor(
				"jdbc:sqlserver://" + "quillback" + ":1433;" +   // server name
						"DATABASENAME="     + "vendor",               // database name
						"hbs",               // login
						"hbs_sql",               // password
						false, "com.microsoft.sqlserver.jdbc.SQLServerDriver"));

		db.setUseTransactions(false);
	}

	public List<Object[]> getStatusData() {
		return statusData;
	}

	public List<Object[]> getCustomerData() {
		return customerData;
	}

	public List<Object[]> getProjectData() {
		return projectData;
	}

	public int getSize(Type type){
		if(type==Type.STATUS)return statusData.size();
		else if(type==Type.CUSTOMER) return customerData.size();
		else return projectData.size();
	}
	
	public static Database getConnection(){
		
		db.openConnection();
		return db;
	}

	public void loadData(){
		getConnection();

		try{
			Statement st = db.getJdbcConnection().createStatement();
			ResultSet rs = st.executeQuery("select top 1000 tr_dtl_stname from Tr_dtl_status");		      
			while(rs.next()){
				String status = rs.getString("tr_dtl_stname").trim();
				Object[] dataRow = {false, status};
				statusData.add(dataRow);
			}	
			rs = st.executeQuery("select top 1000 assoc_id, assoc_name from Assoc");
			while(rs.next()){
				String name = rs.getString("assoc_name").trim();
				int ID = rs.getInt("assoc_id");
				Object[] dataRow = {false, ID, name};
				customerData.add(dataRow);
			}
			rs = st.executeQuery("select top 1000 pr_name from Project");
			while(rs.next()){
				String name = rs.getString("pr_name").trim();
				Object[] dataRow = {false, name};
				projectData.add(dataRow);
			}
			st.close();
			rs.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		db.closeConnection();
	}
}
