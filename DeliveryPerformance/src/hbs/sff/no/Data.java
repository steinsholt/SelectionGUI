package hbs.sff.no;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	public void LoadData(){
		for(int i = 0; i < 10; i++){
			Object[] item = {false, "Random name"};
			statusData.add(item);
		}
		// TODO: Selects the data from the database and add to the List
	}
}
