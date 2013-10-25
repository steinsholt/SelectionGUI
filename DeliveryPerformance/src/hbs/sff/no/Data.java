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
			st.setQueryTimeout(60);
			ResultSet rs = st.executeQuery("select tr_dtl_stname from Tr_dtl_status");		      
			while(rs.next()){
				String status = rs.getString("tr_dtl_stname").trim();
				Object[] dataRow = {false, status};
				statusData.add(dataRow);
			}	
			rs = st.executeQuery("select assoc_id, assoc_name from Assoc customerList");
 			while(rs.next()){
				String name = rs.getString("assoc_name").trim();
				int ID = rs.getInt("assoc_id");
				Object[] dataRow = {false, ID, name};
				customerData.add(dataRow);
			}
			rs = st.executeQuery("select pr_name from Project");
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
	
	public void unusedSearch(){
//		ResultSet rs = st.executeQuery("select \"Project\" = Project.pr_name,"
		//					+ " \"Customer\" = customerList.assoc_name,"
		//					+ " \"Customer ID\" = customerList.assoc_id,"
		//					+ " \"Item Status\" = Tr_dtl_status.tr_dtl_stname"
		//					+ " from vendor.dbo.Tr_hdr,"
		//					+ " vendor.dbo.Tr_dtl clientItemList left join vendor.dbo.Tr_dtl supplierItemList" 
		//					+ " on (clientItemList.vnd_no = supplierItemList.vnd_no"           
		//					+ " and clientItemList.item_no = supplierItemList.item_no"           
		//					+ " and clientItemList.suppl_tr_id = supplierItemList.tr_no" 
		//					+ " and supplierItemList.tr_dtl_status>0" 
		//					+ " and supplierItemList.vnd_no > 1"
		//					+ " ),"
		//					+ " vendor.dbo.Assoc customerList," 
		//					+ " vendor.dbo.Assoc supplierList," 
		//					+ " vendor.dbo.Project," 
		//					+ " vendor.dbo.Tr_dtl_status"
		//					+ " where Tr_hdr.tr_no = clientItemList.tr_no" 
		//					+ " and Tr_hdr.assoc_id = customerList.assoc_id"
		//					+ " and Tr_hdr.active_id = Project.project_id"
		//					+ " and clientItemList.suppl_id = supplierList.assoc_id"
		//					+ " and clientItemList.tr_dtl_status = Tr_dtl_status.tr_dtl_status"
		//					+ " order by Tr_hdr.active_id,"
		//					+ " Tr_hdr.assoc_id,"
		//					+ " Tr_hdr.tr_no"
		//					);
		//			while(rs.next()){
		//				String status = rs.getString("Item Status").trim();
		//				String customer = rs.getString("Customer").trim();
		//				int ID = rs.getInt("Customer ID");
		//				String project = rs.getString("Project").trim();
		//				Object[] statusRow = {false, status};
		//				Object[] customerRow = {false, ID, customer};
		//				Object[] projectRow = {false, project};
		//				statusData.add(statusRow);
		//				customerData.add(customerRow);
		//				projectData.add(projectRow);
		//			}
	}
}
