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

	public void LoadData(){
		for(int i = 0; i < 10; i++)statusData.put(i, "Status");
		for(int i = 0; i < 10; i++)projectData.put(i, "Project");
		for(int i = 0; i < 10; i++)customerData.put(i, "Customer");
		// TODO: Selects the data from the database and adds to the HashMap
	}

	public Object[][] putData(Object[][] data, Type type){
		switch(type){
		case STATUS:
			for (Map.Entry<Integer, String> entry : statusData.entrySet()){
				// TODO: Load status data
				data[entry.getKey()][1] = entry.getValue();
				data[entry.getKey()][0] = new Boolean(false);
			}
			break;
		case CUSTOMER:
			for (Map.Entry<Integer, String> entry : customerData.entrySet()){
				// TODO: Load customer data
				data[entry.getKey()][1] = entry.getKey();
				data[entry.getKey()][2] = entry.getValue();
				data[entry.getKey()][0] = new Boolean(false);
			}
			break;
		case PROJECT:
			for (Map.Entry<Integer, String> entry : projectData.entrySet()){
				// TODO: Load project data
				data[entry.getKey()][1] = entry.getValue();
				data[entry.getKey()][0] = new Boolean(false);
			}	
			break;
		}
		return data;
	}

	public int getSize(Type type){
		switch(type){
		case STATUS:
			return statusData.size();
		case CUSTOMER:
			return customerData.size();
		case PROJECT:
			return projectData.size();
		default: return 0;
		}
	}
}
