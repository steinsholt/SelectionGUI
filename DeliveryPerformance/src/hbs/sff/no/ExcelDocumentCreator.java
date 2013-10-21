package hbs.sff.no;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.borland.dx.sql.dataset.Database;

public class ExcelDocumentCreator {
	private XSSFWorkbook workbook;
	private String file;
	private XSSFSheet sheetTable;
	private XSSFSheet sheetProject;

	public ExcelDocumentCreator(){
		createWorkbook();
		file = "C:/Users/hbs/Excel documents/test.xlsx";
	}

	private void saveWorkbook() {
		try {
			FileOutputStream out = new FileOutputStream(new File(file));
			workbook.write(out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createWorkbook() {
		workbook = new XSSFWorkbook();
		sheetTable = workbook.createSheet("Table");
		sheetProject = workbook.createSheet("Project");
	}

	public void createReport(List<List> customerData, 
			List<List> projectData, List<List> statusData){
		
		customerData.remove(0);
		projectData.remove(0);
		statusData.remove(0);
		try {
			Database db = Data.getConnection();
			StringBuilder query = new StringBuilder(5000);
			String basicStatement = "select \"Project\" = Project.pr_name,"
					+ " \"Client\" = customerList.assoc_name,"
					+ " \"Client Ref.\" = Tr_hdr.assoc_ref,"
					+ " \"Order Nr.\" = Tr_hdr.tr_no, "
					+ " \"Order Registration Date\" = Tr_hdr.reg_date,"		   
					+ " \"Item nr.\" = clientItemList.item_no,"
					+ " \"Client Art. code\" = clientItemList.local_id,"
					+ " \"Vendor nr.\" = clientItemList.vnd_no, "
					+ " \"Description\" = clientItemList.description,"
					+ " \"Supplier\" = supplierList.assoc_name ,"
					+ " \"QTY\" = clientItemList.qnt,"
					+ " \"Unit Price\" = clientItemList.price,"
					+ " \"Total Price\" = clientItemList.qnt*clientItemList.price,"
					+ " \"currency\" = Exchange.curr_name,"
					+ " \"CDD\" = clientItemList.contract_date,"
					+ " \"EDD\" = clientItemList.estimate_date,"
					+ " \"RFI\" = clientItemList.rfi_date," 
					+ " \"CCD\" = supplierItemList.contract_date,"
					+ " \"ECD\" = supplierItemList.estimate_date,"
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
					+ " and clientItemList.tr_dtl_status = Tr_dtl_status.tr_dtl_status"
					+ " and customerList.assoc_id in (";
			query.append(basicStatement);
			for(List l : customerData){
				int id = Integer.parseInt((String) l.get(1));
				query.append(id + ", ");
			}
			query.delete(query.length()-2, query.length());
			query.append(") and Project.pr_name in (");
			for(List l : projectData){
				String name = (String) l.get(1);
				query.append("'" + name + "', ");
			}
			query.delete(query.length()-2, query.length());
			query.append(") and Tr_dtl_status.tr_dtl_stname in (");
			for(List l : statusData){
				String status = (String) l.get(1);
				query.append("'" + status + "', ");
			}
			query.delete(query.length()-2, query.length());
			query.append(")");
			Statement st = db.getJdbcConnection().createStatement();
			st.setQueryTimeout(15);
			ResultSet rs = st.executeQuery(query.toString());
			
			populateSheetTable(rs);
			
			rs.close();
			st.close();
			db.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		saveWorkbook();
	}

	private void populateSheetTable(ResultSet rs) {
		try {
			while(rs.next()){
				//TODO: fix date/time
				Row row = sheetTable.createRow(rs.getRow());
				row.createCell(0).setCellValue(rs.getString("Project"));
				row.createCell(1).setCellValue(rs.getString("Client"));
				row.createCell(2).setCellValue(rs.getString("Client Ref."));
				row.createCell(3).setCellValue(rs.getInt("Order Nr."));
//				row.createCell(4).setCellValue(rs.getDate("Order Registration Date"));
				row.createCell(5).setCellValue(rs.getString("Item nr."));
//				row.createCell(6).setCellValue(rs.getString("Client Art. code"));
				row.createCell(7).setCellValue(rs.getInt("Vendor nr."));
				row.createCell(8).setCellValue(rs.getString("Description"));
				row.createCell(9).setCellValue(rs.getString("Supplier"));
				row.createCell(10).setCellValue(rs.getDouble("QTY"));
				row.createCell(11).setCellValue(rs.getDouble("Unit Price"));
				row.createCell(12).setCellValue(rs.getDouble("Total Price"));
				row.createCell(13).setCellValue(rs.getString("currency"));
//				row.createCell(14).setCellValue(rs.getDate("CDD"));
//				row.createCell(15).setCellValue(rs.getDate("EDD"));
//				row.createCell(16).setCellValue(rs.getDate("RFI"));
//				row.createCell(17).setCellValue(rs.getDate("CCD"));
//				row.createCell(18).setCellValue(rs.getDate("ECD"));
				row.createCell(19).setCellValue(rs.getString("Item Status"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
