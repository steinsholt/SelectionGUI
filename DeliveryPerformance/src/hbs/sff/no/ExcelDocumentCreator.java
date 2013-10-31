package hbs.sff.no;

import java.awt.Desktop;
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
	private File output;
	private InputStream template;
	private XSSFSheet sheetTable;
	private XSSFSheet sheetProject;

	public ExcelDocumentCreator(){
		try {
			template = new FileInputStream("C:/Users/hbs/workspace/SelectionGUI/DeliveryPerformance/template/template.xlsx");
			createWorkbook();
			output = File.createTempFile("temp", ".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveWorkbook() {
		try {
			workbook.setActiveSheet(0);
			FileOutputStream out = new FileOutputStream(output, false);
			workbook.write(out);
			out.close();
			Desktop.getDesktop().open(output);
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

		boolean allCustSelected = customerData.get(0).contains("ALL") ? true : false;
		boolean allProjSelected = projectData.get(0).contains("ALL") ? true : false;
		boolean allStatSelected = statusData.get(0).contains("ALL") ? true : false;

		List<List> temp_cust = new ArrayList<List>(customerData);
		List<List> temp_proj = new ArrayList<List>(projectData);
		List<List> temp_stat = new ArrayList<List>(statusData);
		
		if(!allCustSelected)temp_cust.remove(0);
		if(!allProjSelected)temp_proj.remove(0);
		if(!allStatSelected)temp_stat.remove(0);
		
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
					+ " where Tr_hdr.tr_status = 2"
					+ " and Tr_hdr.tr_no = clientItemList.tr_no" 
					+ " and Tr_hdr.assoc_id = customerList.assoc_id"
					+ " and Tr_hdr.active_id = Project.project_id"
					+ " and clientItemList.suppl_id      = supplierList.assoc_id" 
					+ " and clientItemList.currency_id   = Exchange.currency_id"
					+ " and clientItemList.tr_dtl_status = Tr_dtl_status.tr_dtl_status";
			query.append(basicStatement);
			if(!allCustSelected){
				query.append(" and customerList.assoc_id in (");
				for(List l : temp_cust){
					int id = Integer.parseInt((String) l.get(1));
					query.append(id + ", ");
				}
				query.delete(query.length()-2, query.length());
				query.append(")");
			}
			if(!allProjSelected){
				query.append(" and Project.pr_name in (");
				for(List l : temp_proj){
					String name = (String) l.get(1);
					query.append("'" + name + "', ");
				}
				query.delete(query.length()-2, query.length());
				query.append(")");
			}
			if(!allStatSelected){
				query.append(" and Tr_dtl_status.tr_dtl_stname in (");
				for(List l : temp_stat){
					String status = (String) l.get(1);
					query.append("'" + status + "', ");
				}
				query.delete(query.length()-2, query.length());
				query.append(")");
			}
			Statement st = db.getJdbcConnection().createStatement();
			st.setQueryTimeout(60);
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
				for(int column = 0; column < rs.getMetaData().getColumnCount(); column++){
					insertValue(rs, row, column);					
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void insertValue(ResultSet rs, Row row, int column){
		Cell cell = row.createCell(column);
		try{
			cell.setCellValue(rs.getDouble(column + 1));
		}catch(NumberFormatException | SQLException e){
			try {
				String s = rs.getString(column + 1) == null ? "" : rs.getString(column + 1);
				cell.setCellValue(s);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
}
