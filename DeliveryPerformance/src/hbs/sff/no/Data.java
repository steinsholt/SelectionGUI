package hbs.sff.no;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.borland.dx.sql.dataset.Database;

public class Data {
	private List<Object[]> statusData;
	private HashMap<Integer, String> customerData;
	private List<String> projectData;

	public enum Type{
		STATUS, CUSTOMER, PROJECT
	}

	public Data(){
		statusData = new ArrayList<Object[]>();
		projectData = new ArrayList<String>();
		customerData = new HashMap<Integer, String>();
	}

	public List<Object[]> getStatusData() {
		return statusData;
	}

	public HashMap<Integer, String> getCustomerData() {
		return customerData;
	}

	public List<String> getProjectData() {
		return projectData;
	}

	public int getSize(Type type){
		if(type==Type.STATUS)return statusData.size();
		else if(type==Type.CUSTOMER) return customerData.size();
		else return projectData.size();
	}

	public void LoadData(){
		Database db = new Database();

		db.setConnection(new com.borland.dx.sql.dataset.ConnectionDescriptor(
		                               "jdbc:sqlserver://" + "quillback" + ":1433;" +   // server name
		                               "DATABASENAME="     + "HBS",               // database name
		                                                     "hbs",               // login
		                                                     "hbs_sql",               // password
		                               false, "com.microsoft.sqlserver.jdbc.SQLServerDriver"));

		      db.setUseTransactions(false);
		      db.openConnection();

		try{
		          Statement st = db.getJdbcConnection().createStatement();
		          ResultSet rs = st.executeQuery("select top 1000 * from Status");		      
		          while(rs.next()){
		        	  String status = rs.getString("Status");
		        	  Object[] dataRow = {false, status};
		        	  statusData.add(dataRow);
		        	  System.out.println(rs.getString("Status"));
		          }		          
		          st.close();
		          rs.close();
		       }catch(Exception ex){
		          ex.printStackTrace();
		       }

	}
	// TODO: Selects the data from the database and add to the List
}

