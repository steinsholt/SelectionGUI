package hbs.sff.no;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.borland.dx.sql.dataset.Database;

public class ExcelDocumentCreator {
	private XSSFWorkbook workbook;
	private String output;
	private InputStream template;
	private XSSFSheet sheetTable;
	private XSSFSheet sheetProject;

	public ExcelDocumentCreator(){
		try {
			template = new FileInputStream("C:/Users/hbs/workspace/SelectionGUI/DeliveryPerformance/template/template.xlsx");
			createWorkbook();
			output = "C:/Users/hbs/Excel documents/test.xlsx";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void saveWorkbook() {
		try {
			workbook.setActiveSheet(0);
			FileOutputStream out = new FileOutputStream(new File(output));
			workbook.write(out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createWorkbook() {
		try {
			workbook = (XSSFWorkbook) WorkbookFactory.create(template);
			sheetTable = workbook.getSheet("Table");
			sheetTable.setZoom(70);
			sheetProject = workbook.getSheet("Project");
		} catch (InvalidFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public void createReport(List<List> customerData, 
			List<List> projectData, List<List> statusData){

		boolean allCust = false;
		boolean allProj = false;
		boolean allStat = false;

		if(customerData.get(0).contains("ALL")) allCust = true;
		if(projectData.get(0).contains("ALL")) allProj = true;
		if(statusData.get(0).contains("ALL")) allStat = true;

		List<List> temp_cust = new ArrayList<List>(customerData);
		if(!allCust)temp_cust.remove(0);
		List<List> temp_proj = new ArrayList<List>(projectData);
		if(!allProj)temp_proj.remove(0);
		List<List> temp_stat = new ArrayList<List>(statusData);
		if(!allStat)temp_stat.remove(0);
		try {
			Database db = Data.getConnection();
			StringBuilder query = new StringBuilder(5000);
			String basicStatement = "select \"Project\" = Project.pr_name,"
					+ " \"Client\" = customerList.assoc_name,"
					+ " \"Client Ref.\" = Tr_hdr.assoc_ref,"
					+ " \"Order Nr.\" = Tr_hdr.tr_no, "
					+ " \"Order Registration Date\" = convert(varchar(20), Tr_hdr.reg_date, 103),"		   
					+ " \"Item nr.\" = clientItemList.item_no,"
					+ " \"Client Art. code\" = clientItemList.local_id,"
					+ " \"Vendor nr.\" = clientItemList.vnd_no, "
					+ " \"Description\" = clientItemList.description,"
					+ " \"Supplier\" = supplierList.assoc_name ,"
					+ " \"QTY\" = clientItemList.qnt,"
					+ " \"Unit Price\" = clientItemList.price,"
					+ " \"Total Price\" = clientItemList.qnt*clientItemList.price,"
					+ " \"currency\" = Exchange.curr_name,"
					+ " \"CDD\" = convert(varchar(20), clientItemList.contract_date, 103),"
					+ " \"EDD\" = convert(varchar(20), clientItemList.estimate_date, 103),"
					+ " \"RFI\" = convert(varchar(20), clientItemList.rfi_date, 103)," 
					+ " \"CCD\" = convert(varchar(20), supplierItemList.contract_date, 103),"
					+ " \"ECD\" = convert(varchar(20), supplierItemList.estimate_date, 103),"
					+ " \"Item Status\" = Tr_dtl_status.tr_dtl_stname"
					+ " from vendor.dbo.Tr_hdr," 
					+ " vendor.dbo.Tr_dtl clientItemList left join vendor.dbo.Tr_dtl supplierItemList" 
					+ " on (clientItemList.vnd_no = supplierItemList.vnd_no"           
					+ " and clientItemList.item_no = supplierItemList.item_no"           
					+ " and clientItemList.suppl_tr_id = supplierItemList.tr_no" 
					+ " and supplierItemList.tr_dtl_status>0" 
					+ " and supplierItemList.vnd_no > 1"
					+ " )," 
					+ " vendor.dbo.Assoc customerList," 
					+ " vendor.dbo.Assoc supplierList," 
					+ " vendor.dbo.Project," 
					+ " vendor.dbo.Exchange," 
					+ " vendor.dbo.Tr_dtl_status"
					+ " where Tr_hdr.active_id = 87"
					+ " and Tr_hdr.tr_no = clientItemList.tr_no" 
					+ " and Tr_hdr.assoc_id = customerList.assoc_id"
					+ " and Tr_hdr.active_id = Project.project_id"
					+ " and clientItemList.suppl_id      = supplierList.assoc_id" 
					+ " and clientItemList.currency_id   = Exchange.currency_id"
					+ " and clientItemList.tr_dtl_status = Tr_dtl_status.tr_dtl_status";
			query.append(basicStatement);
			if(!allCust){
				query.append(" and customerList.assoc_id in (");
				for(List l : temp_cust){
					int id = Integer.parseInt((String) l.get(1));
					query.append(id + ", ");
				}
				query.delete(query.length()-2, query.length());
				query.append(")");
			}
			if(!allProj){
				query.append(" and Project.pr_name in (");
				for(List l : temp_proj){
					String name = (String) l.get(1);
					query.append("'" + name + "', ");
				}
				query.delete(query.length()-2, query.length());
				query.append(")");
			}
			if(!allStat){
				query.append(" and Tr_dtl_status.tr_dtl_stname in (");
				for(List l : temp_stat){
					String status = (String) l.get(1);
					query.append("'" + status + "', ");
				}
				query.delete(query.length()-2, query.length());
				query.append(")");
			}
			Statement st = db.getJdbcConnection().createStatement();
			st.setQueryTimeout(15);
			ResultSet rs = st.executeQuery(query.toString());


			populateSheetTable(rs);
			populateSheetProject();

			rs.close();
			st.close();
			db.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		saveWorkbook();
	}

	private void populateSheetProject() {
		// TODO: Does not account for the currency exchange rates
		int last = sheetTable.getLastRowNum() + 1;
		sheetProject.getRow(5).getCell(1).setCellFormula("SUM(Table!$M$2:$M$" + last + ")");

	}

	private void populateSheetTable(ResultSet rs) {
		try {
			while(rs.next()){
				Row row = sheetTable.createRow(rs.getRow());
				row.createCell(0).setCellValue(rs.getString("Project"));
				row.createCell(1).setCellValue(rs.getString("Client"));
				row.createCell(2).setCellValue(rs.getString("Client Ref."));
				row.createCell(3).setCellValue(rs.getInt("Order Nr."));
				setDateValues(rs.getString("Order Registration Date"), row, 4);
				row.createCell(5).setCellValue(rs.getString("Item nr."));
				row.createCell(6).setCellValue(rs.getString("Client Art. code"));
				row.createCell(7).setCellValue(rs.getInt("Vendor nr."));
				row.createCell(8).setCellValue(rs.getString("Description"));
				row.createCell(9).setCellValue(rs.getString("Supplier"));
				row.createCell(10).setCellValue(rs.getDouble("QTY"));
				row.createCell(11).setCellValue(rs.getDouble("Unit Price"));
				row.createCell(12).setCellValue(rs.getDouble("Total Price"));
				row.createCell(13).setCellValue(rs.getString("currency"));
				setDateValues(rs.getString("CDD"), row, 14);
				setDateValues(rs.getString("EDD"), row, 15);
				setDateValues(rs.getString("RFI"), row, 16);
				setDateValues(rs.getString("CCD"), row, 17);
				setDateValues(rs.getString("ECD"), row, 18);
				row.createCell(19).setCellValue(rs.getString("Item Status"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
//		for(int i = 0; i < 20; i++){
//			sheetTable.autoSizeColumn(i);
//		}
	}

	private void setDateValues(String value, Row row, int column) {
		Cell cell = row.createCell(column);
		if(value == null){
			cell.setCellValue("null");
		}
		else {
			cell.setCellValue(value);
		}
	}
	// TODO: Test if a null value int or double causes trouble
	// Item nr. is sometimes a string
	// implement "ALL" as select *
}
