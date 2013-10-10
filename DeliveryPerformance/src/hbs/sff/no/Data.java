package hbs.sff.no;

import java.util.HashMap;
import java.util.Map;

public class Data {
	private HashMap<Integer, String> statusData;
	private HashMap<Integer, String> customerData;
	private HashMap<Integer, String> projectData;

	public enum Type{
		STATUS, CUSTOMER, PROJECT
	}

	public Data(){
		statusData = new HashMap<Integer, String>();
		projectData = new HashMap<Integer, String>();
		customerData = new HashMap<Integer, String>();

	}
	
	public HashMap<Integer, String> getStatusData() {
		return statusData;
	}

	public HashMap<Integer, String> getCustomerData() {
		return customerData;
	}

	public HashMap<Integer, String> getProjectData() {
		return projectData;
	}

	public void LoadData(){
		for(int i = 0; i < 10; i++)statusData.put(i, "Status");
		for(int i = 0; i < 10; i++)projectData.put(i, "Project");
		for(int i = 0; i < 10; i++)customerData.put(i, "Customer");
		// TODO: Selects the data from the database and adds to the HashMap
	}
}
