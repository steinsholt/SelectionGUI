package com.sff.report_performance;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.borland.dx.dataset.VariantException;
import com.borland.dx.sql.dataset.Database;
import com.borland.dx.sql.dataset.QueryDataSet;
import com.borland.dx.sql.dataset.QueryDescriptor;

/*
 * This class creates the excel document in another thread, using a swing worker
 * to publish the process progress.
 */
@SuppressWarnings("rawtypes")
public class ExcelDocumentCreator extends SwingWorker<String, Integer> {
	private XSSFWorkbook workbook;
	private File output;
	private InputStream template;
	private XSSFSheet sheetTable;
	private XSSFSheet sheetProject;
	private XSSFSheet sheetMill;
	private List<List> customerData;
	private List<List> projectData;
	private List<List> statusData;
	private JTextField publishedOutput;
	private JTextField progressField;
	private FileOutputStream out;

	public ExcelDocumentCreator(List<List> customerData, List<List> projectData, List<List> statusData, JTextField publishedOutput, JTextField progressField, FileOutputStream out, File output){
		try {
			this.customerData = customerData;
			this.projectData = projectData;
			this.statusData = statusData;
			this.publishedOutput = publishedOutput;
			this.progressField = progressField;
			this.out = out;
			this.output = output;

			//TODO: relative path
			template = new FileInputStream("C:/Users/hbs/workspace/SelectionGUI/DeliveryPerformance/template/template.xlsx");
			workbook = (XSSFWorkbook) WorkbookFactory.create(template);

			sheetTable = workbook.getSheet("Table");
			sheetTable.setZoom(70);
			sheetProject = workbook.getSheet("Project");
			sheetMill = workbook.getSheet("Mill");

		} catch (IOException | InvalidFormatException e) {
			e.printStackTrace();
		}
	}

	private void saveWorkbook() {
		try {
			workbook.setActiveSheet(0);
			workbook.getSheetAt(0).showInPane((short)0, (short)0);
			workbook.write(out);
			out.close();
			Desktop.getDesktop().open(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String doInBackground() {
		try{

			boolean allCustSelected = customerData.size()==0 ? true : false;
			boolean allProjSelected = projectData.size()==0 ? true : false;
			boolean allStatSelected = statusData.size()==0 ? true : false;

			StringBuilder query = generateQuery(allCustSelected, allProjSelected, allStatSelected, customerData, projectData, statusData);

			Database db = DatabaseConnection.getDatabase();
			QueryDataSet dataSet = new QueryDataSet();
			dataSet.setQuery(new QueryDescriptor(db, query.toString()));
			dataSet.open();

			int rowCount = dataSet.getRowCount();
			int processed = 0;

			CellStyle decimalStyle = workbook.createCellStyle();
			CellStyle sixDigitStyle = workbook.createCellStyle();
			DataFormat format = workbook.createDataFormat();
			decimalStyle.setDataFormat(format.getFormat("##00.00"));
			sixDigitStyle.setDataFormat(format.getFormat("000000"));

			publishedOutput.setText("Generating Excel Document");
			while(!isCancelled()){
				CellStyle yellow = workbook.createCellStyle();
				yellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				yellow.setFillPattern(CellStyle.SOLID_FOREGROUND);
				Set<String> currencySet = new HashSet<String>();
				Set<String> customerSet = new HashSet<String>();
				Set<String> projectSet = new HashSet<String>();
				while(dataSet.next()){
					setProgress(100 * processed++ / rowCount);
					progressField.setText("Adding row: " + processed);

					/*
					 * Extracts data from the database and inserts into the "Table"-sheet.
					 */
					sheetTable.createRow(dataSet.getRow());
					for(int column = 0; column < dataSet.getColumnCount(); column++){
						Cell cell = sheetTable.getRow(dataSet.getRow()).createCell(column);
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Unit Price"))){
							cell.setCellStyle(decimalStyle);
						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Vendor nr."))){
							cell.setCellStyle(sixDigitStyle);
						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("currency"))){
							currencySet.add(dataSet.getString(column));
						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Client"))){
							customerSet.add(dataSet.getString(column));
						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Project"))){
							projectSet.add(dataSet.getString(column));
						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Total Price"))){
							cell.setCellStyle(yellow);
						}
						try{
							String s = dataSet.getString(column);
							if(s.length()>0)cell.setCellValue(s);
						}catch(VariantException e){
							try{
								Double d = dataSet.getDouble(column);
								cell.setCellValue(d);
							}catch(VariantException v){
								int i = dataSet.getInt(column);
								cell.setCellValue(i);
							}
						}
					}
				}
				dataSet.close();
				
				HashMap<String, String> referenceMap = ExcelHelper.createExcelReferenceList(sheetTable);

				CellStyle aqua = workbook.createCellStyle();
				aqua.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
				aqua.setFillPattern(CellStyle.SOLID_FOREGROUND);

				setProgress(0);
				processed = 0;
				int lastRow = sheetTable.getLastRowNum() + 1;

				/*
				 *  Creates the delay part of the "Table"-sheet.
				 */
				// TODO: If empty cell, do not insert formula
				for(int row = 1; row < lastRow; row++){
					int index = row + 1;
					int lastColumn = sheetTable.getRow(row).getLastCellNum();
					progressField.setText("Processing row: " + processed);
					setProgress(100 * processed++ / rowCount);

					Cell delay = sheetTable.getRow(row).createCell(lastColumn++);
					delay.setCellFormula(ExcelHelper.excelSubtractAndDivide(referenceMap.get("RFI"), referenceMap.get("CDD"), index, 7)); 
					delay.setCellStyle(aqua);

					Cell roundDelay = sheetTable.getRow(row).createCell(lastColumn++);
					roundDelay.setCellFormula(ExcelHelper.excelRound(referenceMap.get("EDD/RFI-CDD"), index, 0));
					roundDelay.setCellStyle(aqua);

					Cell changeName = sheetTable.getRow(row).createCell(lastColumn++);
					changeName.setCellFormula(ExcelHelper.excelSubtractDivideAndRound(referenceMap.get("RFI"), referenceMap.get("Order Reg Date"), index, 7, 0));
					changeName.setCellStyle(aqua);

					Cell changeThis = sheetTable.getRow(row).createCell(lastColumn++);
					changeThis.setCellFormula(ExcelHelper.excelSubtractDivideAndRound(referenceMap.get("CDD"), referenceMap.get("Order Reg Date"), index, 7, 0));
					changeThis.setCellStyle(aqua);
				}

				/*
				 * Inserts the formulae into the "Project"-sheet.
				 */
				CellStyle style = workbook.createCellStyle();
				style.setBorderBottom(CellStyle.BORDER_THIN);
				style.setBorderTop(CellStyle.BORDER_THIN);
				style.setBorderRight(CellStyle.BORDER_THIN);
				style.setBorderLeft(CellStyle.BORDER_THIN);

				int rowPointer = 0;
				setProgress(0);
				processed = 0;

				String currencyExcelReference = referenceMap.get("Currency");
				String totalValueExcelReference = referenceMap.get("Total Value");
				String projectExcelReference = referenceMap.get("Project");
				
				CellStyle yellowBordered = workbook.createCellStyle();
				yellowBordered.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				yellowBordered.setFillPattern(CellStyle.SOLID_FOREGROUND);
				yellowBordered.setBorderBottom(CellStyle.BORDER_MEDIUM);
				yellowBordered.setBorderTop(CellStyle.BORDER_MEDIUM);
				yellowBordered.setBorderRight(CellStyle.BORDER_MEDIUM);
				yellowBordered.setBorderLeft(CellStyle.BORDER_MEDIUM);
				
				// TODO: NB! Uses sell rate only
				Statement st = db.getJdbcConnection().createStatement();
				st.setQueryTimeout(60);
				ResultSet rs = st.executeQuery("SELECT vendor.dbo.Exchange.curr_name, vendor.dbo.Exchange_rate.sell_rate, "
						+ "vendor.dbo.Exchange_rate.exch_type FROM vendor.dbo.Exchange INNER JOIN vendor.dbo.Exchange_rate "
						+ "ON vendor.dbo.Exchange_rate.currency_id = vendor.dbo.Exchange.currency_id");	
				HashMap<String, Double> exchangeMap = new HashMap<String, Double>();
				while(rs.next()){
					String currencyName = rs.getString("curr_name").trim();
					double sellRate = rs.getDouble("sell_rate");
					double exch_type = (double) rs.getInt("exch_type");
					exchangeMap.put(currencyName, sellRate/exch_type);
				}	
				st.close();
				rs.close();
				
//				Iterator it = exchangeMap.entrySet().iterator();
//			    while (it.hasNext()) {
//			        Map.Entry pairs = (Map.Entry)it.next();
//			        System.out.println(pairs.getKey() + " = " + pairs.getValue());
//			        it.remove();
//			    }

				for(String project : projectSet){
					progressField.setText("Processing project: " + processed);
					setProgress(100 * processed++ / projectSet.size());
					// TODO: Create a method that takes a row and sets the style in all cells in the row
					
					rowPointer++;
					int cellPointer = 0;
					
					XSSFRow header = sheetProject.createRow(rowPointer++);  //TODO: create a header creation method
					header.createCell(cellPointer++).setCellValue(project);
					header.createCell(cellPointer++).setCellValue("Total");
					header.createCell(cellPointer++).setCellValue("Currency Exchange Rate");
					header.createCell(cellPointer++).setCellValue("Total [EUR]");
					header.createCell(cellPointer++).setCellValue("Total Items");
					header.createCell(cellPointer++).setCellValue("Value [%]");
					header.createCell(cellPointer++).setCellValue("Item [%]");
					ExcelHelper.setRowStyle(header, yellowBordered, workbook);
					
					// TODO: Only use values associated with the project
					for(String currency : currencySet){
						Row currencyRow = sheetProject.createRow(rowPointer++);
						currencyRow.createCell(0).setCellValue("Total Value [" + currency + "]:");
						currencyRow.createCell(1).setCellFormula(ExcelHelper.excelSumIfs(sheetTable, totalValueExcelReference, currencyExcelReference, currency, projectExcelReference, project));
						currencyRow.createCell(2).setCellValue(exchangeMap.get(currency) / exchangeMap.get("EUR"));
						currencyRow.createCell(3).setCellFormula("B" + rowPointer + "*C" + rowPointer);
						
					}
					int uniqueCurrencies = currencySet.size();
					
					Row totalRow = sheetProject.createRow(rowPointer++);
					totalRow.createCell(0).setCellValue("Total:");
					totalRow.createCell(3).setCellFormula("SUM(D" + (rowPointer - uniqueCurrencies) + ":D" + (rowPointer - 1) + ")");
					
					Row deliveredToFaRow = sheetProject.createRow(rowPointer++);
					deliveredToFaRow.createCell(0).setCellValue("Delivered to FA:");
					
					Row improvedDeliveriesRow = sheetProject.createRow(rowPointer++);
					improvedDeliveriesRow.createCell(0).setCellValue("Improved Deliveries:");
					
					Row valueDeliveriesFaRow = sheetProject.createRow(rowPointer++);
					valueDeliveriesFaRow.createCell(0).setCellValue("Value of Improved Deliveries as FA:");
					
					Row accelerationCostRow = sheetProject.createRow(rowPointer++);
					accelerationCostRow.createCell(0).setCellValue("Acceleration Cost:");
					
					Row itemsOutsideScopeRow = sheetProject.createRow(rowPointer++);
					itemsOutsideScopeRow.createCell(0).setCellValue("Items Delivered Outside Scope:");
				}
				
				/*
				 * Creates and populates the "Mill"-sheet
				 */
//				sheetMill.createRow(2).createCell(0).setCellValue("Mill");
//				sheetMill.createRow(3).createCell(0).setCellValue("Name:");
//				sheetMill.createRow(4).createCell(0).setCellValue("Average Required Delivery [Weeks]:");
//				sheetMill.createRow(5).createCell(0).setCellValue("Average Actual Delivery > CDD [Weeks]:");
//				sheetMill.createRow(6).createCell(0).setCellValue("Average Actual Delivery [Weeks]:");
//				sheetMill.createRow(7).createCell(0).setCellValue("No of Units:");
//				sheetMill.createRow(8).createCell(0).setCellValue("No of Items:");
//				sheetMill.createRow(9).createCell(0).setCellValue("Value:");
//
//				setProgress(0);
//				processed = 0;
//				int columnCount = customerSet.size();
//
//				int column = 2;
//				for(String customer : customerSet){
//
//					sheetMill.getRow(2).createCell(column).setCellValue("Mill");
//					sheetMill.getRow(3).createCell(column).setCellValue(customer);
//					sheetMill.getRow(4).createCell(column).setCellFormula("AVERAGEIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$Y$2:$Y$" + lastRow + ")");
//					sheetMill.getRow(5).createCell(column).setCellFormula("AVERAGEIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$W$2:$W$" + lastRow + ")");
//					sheetMill.getRow(6).createCell(column).setCellFormula("AVERAGEIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$X$2:$X$" + lastRow + ")");
//					sheetMill.getRow(7).createCell(column).setCellFormula("SUMIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$K$2:$K$" + lastRow + ")");
//					sheetMill.getRow(8).createCell(column).setCellFormula("COUNTIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\""  + ")");
//					sheetMill.getRow(9).createCell(column).setCellFormula("SUMIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$U$2:$U$" + lastRow + ")");
//
//					column++;
//
//					setProgress(100 * ++processed / columnCount);
//					progressField.setText("Creating Mill Graph: " + processed);
//				}
//
//				ExcelHelper.autoSizeColumns(sheetMill);

				// TODO: select * from vendor.dbo.Exchange  &&  select * from vendor.dbo.Exchange_rate
				// TODO: Create a helper class that takes in a sheet and sets font size in all cells

				publishedOutput.setText("Opening Excel Document");
				saveWorkbook();
				db.closeConnection();
				cancel(true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private StringBuilder generateQuery(boolean allCustSelected,
			boolean allProjSelected, boolean allStatSelected,
			List<List> temp_cust, List<List> temp_proj, List<List> temp_stat) {

		/*
		 * The base statement is used no matter the user selection
		 */
		StringBuilder query = new StringBuilder(5000);
		String basicStatement = "select \"Project\" = Project.pr_name,"
				+ " \"Client\" = customerList.assoc_name,"
				+ " \"Client Ref.\" = Tr_hdr.assoc_ref,"
				+ " \"Order Nr.\" = Tr_hdr.tr_no, "
				+ " \"Order Registration Date\" = convert(varchar(20), Tr_hdr.reg_date, 104),"		   
				+ " \"Item nr.\" = clientItemList.item_no,"
				+ " \"Client Art. code\" = clientItemList.local_id,"
				+ " \"Vendor nr.\" = clientItemList.vnd_no, "
				+ " \"Description\" = clientItemList.description,"
				+ " \"Supplier\" = supplierList.assoc_name ,"
				+ " \"QTY\" = clientItemList.qnt,"
				+ " \"Unit Price\" = clientItemList.price,"
				+ " \"currency\" = Exchange.curr_name,"
				+ " \"CDD\" = convert(varchar(20), clientItemList.contract_date, 104),"
				+ " \"EDD\" = convert(varchar(20), clientItemList.estimate_date, 104),"
				+ " \"RFI\" = convert(varchar(20), clientItemList.rfi_date, 104)," 
				+ " \"CCD\" = convert(varchar(20), supplierItemList.contract_date, 104),"
				+ " \"ECD\" = convert(varchar(20), supplierItemList.estimate_date, 104),"
				+ " \"Item Status\" = Tr_dtl_status.tr_dtl_stname,"
				+ " \"Total Price\" = clientItemList.qnt*clientItemList.price"                     //TODO: This should be an excel formula in case of manual changes
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
				+ " and clientItemList.suppl_id = supplierList.assoc_id" 
				+ " and clientItemList.currency_id = Exchange.currency_id"
				+ " and clientItemList.tr_dtl_status = Tr_dtl_status.tr_dtl_status";

		/*
		 * If the user have NOT selected all items in the list the method will
		 * specify the search to only include the selected items.
		 */
		query.append(basicStatement);
		if(!allCustSelected){
			query.append(" and customerList.assoc_id in (");
			for(List l : temp_cust){
				int id =  (int) l.get(1);
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
		return query;
	}
}
