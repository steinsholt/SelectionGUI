package com.sff.report_performance;

import java.awt.Color;
import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.borland.dx.sql.dataset.Database;
import com.borland.dx.sql.dataset.QueryDataSet;
import com.borland.dx.sql.dataset.QueryDescriptor;
import com.moyosoft.connector.com.ComponentObjectModelException;
import com.moyosoft.connector.exception.LibraryNotFoundException;
import com.moyosoft.connector.ms.excel.AxisType;
import com.moyosoft.connector.ms.excel.Chart;
import com.moyosoft.connector.ms.excel.ChartObject;
import com.moyosoft.connector.ms.excel.ChartType;
import com.moyosoft.connector.ms.excel.Direction;
import com.moyosoft.connector.ms.excel.Excel;
import com.moyosoft.connector.ms.excel.LineStyle;
import com.moyosoft.connector.ms.excel.ListRows;
import com.moyosoft.connector.ms.excel.Range;
import com.moyosoft.connector.ms.excel.Workbook;
import com.moyosoft.connector.ms.excel.Worksheet;

/*
 * This class creates the excel document in another thread, using a swing worker
 * to publish the process progress.
 */
@SuppressWarnings("rawtypes")
public class ExcelDocumentCreator extends SwingWorker<String, Integer> {
	private Excel excel;
	private Workbook workbook;
	private Worksheet sheetTable;
	private Worksheet sheetProject;
	private Worksheet sheetMill;
	private Worksheet sheetDelay;
	private Worksheet sheetDelPerformance;
	private Worksheet sheetDelMill;
	private Worksheet sheetItemMill;
	private Worksheet sheetValueMill;
	private Worksheet sheetNoUnits;
	private List<List> customerData;
	private List<List> projectData;
	private JTextField publishedOutput;
	private JTextField progressField;
	private JTextField category; 
	private JTextField frameAgreement;
	private JCheckBox markErrorsCheckBox;

	public ExcelDocumentCreator(List<List> customerData, List<List> projectData, JTextField frameAgreement, JCheckBox markErrorsCheckBox, JTextField category, JTextField publishedOutput, JTextField progressField, File output){
		this.customerData = customerData;
		this.projectData = projectData;
		this.publishedOutput = publishedOutput;
		this.progressField = progressField;
		this.frameAgreement = frameAgreement;
		this.category = category;
		this.markErrorsCheckBox = markErrorsCheckBox;

		try {
			/*
			 * Creates the workbook and adds the sheets
			 */
			excel = new Excel();
			workbook = excel.getWorkbooks().add();

			sheetNoUnits = new Worksheet(workbook);
			sheetNoUnits.setName("NoUnits");
			sheetValueMill = new Worksheet(workbook);
			sheetValueMill.setName("ValueMill");
			sheetItemMill = new Worksheet(workbook);
			sheetItemMill.setName("ItemMill");
			sheetDelMill = new Worksheet(workbook);
			sheetDelMill.setName("DelMill");
			sheetDelPerformance = new Worksheet(workbook);
			sheetDelPerformance.setName("DelPerformance");
			sheetDelay = new Worksheet(workbook);
			sheetDelay.setName("Delay");
			sheetMill = new Worksheet(workbook);
			sheetMill.setName("Mill");
			sheetProject = new Worksheet(workbook);
			sheetProject.setName("Project");
			sheetTable = new Worksheet(workbook);
			sheetTable.setName("Table");

			/*
			 * Excel provides 3 default sheets, these are removed after adding the sheets above
			 */
			workbook.getWorksheets().getItem("Ark1").delete();
			workbook.getWorksheets().getItem("Ark2").delete();
			workbook.getWorksheets().getItem("Ark3").delete();

		} catch (ComponentObjectModelException | LibraryNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void saveWorkbook() {
		/*
		 * Before saving and opening the excel file the width of all columns are set to fit the longest entry
		 */
		sheetTable.getColumns().autoFit();
		sheetProject.getColumns().autoFit();
		sheetDelay.getColumns().autoFit();
		sheetDelPerformance.getColumns().autoFit();
		sheetDelMill.getColumns().autoFit();
		sheetMill.getColumns().autoFit();
		excel.getWorksheets().getItem("Table").activate();

		excel.setVisible(true);
	}

	@Override
	protected String doInBackground() {
		try{

			/*
			 * 
			 */
			boolean allCustSelected = customerData.size()==0 ? true : false;
			boolean allProjSelected = projectData.size()==0 ? true : false;
			boolean allCatSelected = category.getText().equals("ALL") ? true : false;
			boolean allFrameAgrSelected = frameAgreement.getText().equals("ALL") ? true : false;

			/*
			 * The query is built and executed. Afterwards the data is loaded into a data set 
			 */
			StringBuilder query = generateQuery(allCustSelected, allProjSelected, allCatSelected, allFrameAgrSelected, customerData, projectData, category, frameAgreement);

			Database db = DatabaseConnection.getDatabase();
			QueryDataSet dataSet = new QueryDataSet();
			dataSet.setQuery(new QueryDescriptor(db, "exec hbs.dbo.DeliveryPerformanceReport '" + query.toString() + "'"));
			dataSet.open();

			int rowCount = dataSet.getRowCount();
			int processed = 0;
			publishedOutput.setText("Creating Table Sheet");

			while(!isCancelled() && rowCount!=0){
				// TODO: Language preference?
				// TODO: get address from columns instead of hard coded values
				// TODO: set standard number format in ItemNr. column
				/*
				 * All data are loaded into 2D-arrays to shorten execution time. One array for each column in the data set ensures less communication between
				 * the application and Excel.
				 */
				String[][] frameAgr = new String[rowCount][1];
				String[][] project = new String[rowCount][1];
				String[][] client = new String[rowCount][1];
				String[][] category = new String[rowCount][1];
				String[][] clientRef = new String[rowCount][1];
				int[][] orderNr = new int[rowCount][1];
				String[][] orderRegDate = new String[rowCount][1];
				String[][] orderCdd = new String[rowCount][1];
				String[][] itemNr = new String[rowCount][1];
				String[][] clientArtCode = new String[rowCount][1];
				int[][] vendorNr = new int[rowCount][1];
				String[][] description = new String[rowCount][1];
				String[][] supplier = new String[rowCount][1];
				double[][] qty = new double[rowCount][1];
				double[][] unitPrice = new double[rowCount][1];
				double[][] totalPrice = new double[rowCount][1];
				String[][] currency = new String[rowCount][1];
				double[][] cRate = new double[rowCount][1];
				String[][] cdd = new String[rowCount][1];
				String[][] edd = new String[rowCount][1];
				String[][] rfi = new String[rowCount][1];
				String[][] ccd = new String[rowCount][1];
				String[][] ecd = new String[rowCount][1];
				String[][] itemStatus = new String[rowCount][1];

				/*
				 * Sets only allow unique entries. Thus when we need to display one graph per supplier we utilize the sets.
				 */
				Set<String> projectSet = new HashSet<String>();
				Set<String> millSet = new HashSet<String>();
				Set<String> currencySet = new HashSet<String>();

				/*
				 * We iterate over the data set and insert the results into the arrays while publishing the progress to the progress bar.
				 */
				while(dataSet.inBounds() && !isCancelled()){
					setProgress(100 * processed / rowCount);
					progressField.setText("Adding row: " + processed);

					int column = 0;

					frameAgr[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					projectSet.add(correctSpecialCharacters(dataSet.getString(column).trim()));
					project[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					client[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					category[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					clientRef[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					orderNr[processed][0] = dataSet.getInt(column++);
					orderRegDate[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					orderCdd[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					itemNr[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					clientArtCode[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					vendorNr[processed][0] = dataSet.getInt(column++);
					description[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					millSet.add(correctSpecialCharacters(dataSet.getString(column).trim()));
					supplier[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					qty[processed][0] = dataSet.getDouble(column++); 
					unitPrice[processed][0] = dataSet.getDouble(column++);
					totalPrice[processed][0] = dataSet.getDouble(column++);
					currencySet.add(correctSpecialCharacters(dataSet.getString(column).trim()));
					currency[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					cRate[processed][0] = dataSet.getDouble(column++);
					cdd[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					edd[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					rfi[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					ccd[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					ecd[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());
					itemStatus[processed][0] = correctSpecialCharacters(dataSet.getString(column++).trim());

					processed++;
					dataSet.next();
				}
				String[] columnNames = dataSet.getColumnNames(dataSet.getColumnCount()); 
				dataSet.close();

				/*
				 * The data in the arrays are inserted into the "Table"-sheet
				 * Note that currently .setValues will interpret UTF-8 as Windows-1252 characters. By inserting value cell by cell the characters
				 * are interpreted correctly but the application will slow down greatly. 
				 */
				// TODO: If empty report, let user know?
				sheetTable.getRange("A1", "A"+rowCount).setValues(frameAgr);
				sheetTable.getRange("B1", "B"+rowCount).setValues(project);
				sheetTable.getRange("C1", "C"+rowCount).setValues(client);
				sheetTable.getRange("D1", "D"+rowCount).setValues(category);
				sheetTable.getRange("E1", "E"+rowCount).setValues(clientRef);
				sheetTable.getRange("F1", "F"+rowCount).setValues(orderNr);
				sheetTable.getRange("G1", "G"+rowCount).setValues(orderRegDate);
				sheetTable.getRange("H1", "H"+rowCount).setValues(orderCdd);
				sheetTable.getRange("I1", "I"+rowCount).setValues(itemNr);
				sheetTable.getRange("J1", "J"+rowCount).setValues(clientArtCode);
				sheetTable.getRange("K1", "K"+rowCount).setValues(vendorNr);
				sheetTable.getRange("L1", "L"+rowCount).setValues(description);
				sheetTable.getRange("M1", "M"+rowCount).setValues(supplier); 
				sheetTable.getRange("N1", "N"+rowCount).setValues(qty);
				sheetTable.getRange("O1", "O"+rowCount).setValues(unitPrice);
				sheetTable.getRange("P1", "P"+rowCount).setValues(totalPrice);
				sheetTable.getRange("Q1", "Q"+rowCount).setValues(currency);
				sheetTable.getRange("R1", "R"+rowCount).setValues(cRate);
				sheetTable.getRange("S1", "S"+rowCount).setValues(cdd);
				sheetTable.getRange("T1", "T"+rowCount).setValues(edd);
				sheetTable.getRange("U1", "U"+rowCount).setValues(rfi);
				sheetTable.getRange("V1", "V"+rowCount).setValues(ccd);
				sheetTable.getRange("W1", "W"+rowCount).setValues(ecd);
				sheetTable.getRange("X1", "X"+rowCount).setValues(itemStatus);

				/*
				 * To account for the incorrect interpretation of UTF-8 characters the actual characters are translated back to the expected characters.
				 * Ref: http://www.i18nqa.com/debug/utf8-debug.html
				 */
//				Range entireTableSheet = sheetTable.getRange("A1:X"+rowCount);
//				if(entireTableSheet.find("Ø")!=null) entireTableSheet.replace("Ø", "�");
//				if(entireTableSheet.find("ø")!=null) entireTableSheet.replace("ø", "�");
//				if(entireTableSheet.find("Å")!=null) entireTableSheet.replace("Å", "�");
//				if(entireTableSheet.find("å")!=null) entireTableSheet.replace("å", "�");
//				if(entireTableSheet.find("Æ")!=null) entireTableSheet.replace("Æ", "�");
//				if(entireTableSheet.find("æ")!=null) entireTableSheet.replace("æ", "�");
//				if(entireTableSheet.find("Ü")!=null) entireTableSheet.replace("Ü", "�");
//				if(entireTableSheet.find("ü")!=null) entireTableSheet.replace("ü", "�");
//				if(entireTableSheet.find("Ä")!=null) entireTableSheet.replace("Ä", "�");
//				if(entireTableSheet.find("ä")!=null) entireTableSheet.replace("ä", "�");
//				if(entireTableSheet.find("é")!=null) entireTableSheet.replace("é", "�");
//				if(entireTableSheet.find("è")!=null) entireTableSheet.replace("è", "�");
//				if(entireTableSheet.find("É")!=null) entireTableSheet.replace("É", "�");
//				if(entireTableSheet.find("È")!=null) entireTableSheet.replace("È", "�");
//				if(entireTableSheet.find("–")!=null) entireTableSheet.replace("–", "�");
//				if(entireTableSheet.find("Ò")!=null) entireTableSheet.replace("Ò", "�");
//				if(entireTableSheet.find("ò")!=null) entireTableSheet.replace("ò", "�");

				String firstHeaderCell = "A1";
				String currentHeaderCell = firstHeaderCell;
				String formulaStartCell = "A2";
				String currentEndCell = sheetTable.getRange(currentHeaderCell).getEntireColumn().getEnd(Direction.DOWN).getAddress(); 

				/*
				 * Creates the header row in the "Table"-sheet by using the column names in the data set.
				 */
				for(String column : columnNames){
					String headerName = column.replaceAll("\\s+", "");
					sheetTable.getRange(currentHeaderCell).setValue(headerName);
					sheetTable.getRange(currentHeaderCell, currentEndCell).setName(headerName);
					currentHeaderCell = sheetTable.getRange(headerName).getNext().getAddress();
					currentEndCell = sheetTable.getRange(currentEndCell).getNext().getAddress();
					formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				}

				/*
				 * Gets the address of excel ranges used later in formulas.
				 */
				String totalValueRangeAddress = sheetTable.getRange("TotalPrice").getAddress(false, false);
				String currencyRangeAddress = sheetTable.getRange("currency").getAddress(false, false);
				String projectRangeAddress = sheetTable.getRange("Project").getAddress(false, false);
				String quantityRangeAddress = sheetTable.getRange("QTY").getAddress(false, false);
				String quantityRangeAddressAbsolute = sheetTable.getRange("QTY").getAddress(true, true);
				String supplierRangeAddressAbsolute = sheetTable.getRange("Supplier").getAddress(true, true);

				/*
				 * Converts total value into EURO. The NOK to EURO conversion rate is retrieved from the database. The period is replaced
				 * by a comma as we are using a Norwegian version of Excel. This is not needed when inserting a double directly into a cell,
				 * however when creating a string formula it seems needed.  
				 */
				Statement st = db.getJdbcConnection().createStatement();
				st.setQueryTimeout(60);
				ResultSet rs = st.executeQuery("SELECT vendor.dbo.Exchange_rate.sell_rate FROM vendor.dbo.Exchange_rate WHERE vendor.dbo.Exchange_rate.currency_id=99");
				rs.next();
				Double sellRate = rs.getDouble("sell_rate");
				String convertedSellRate = sellRate.toString().replace(".", ","); 
				st.close();
				rs.close();

				String startCell = formulaStartCell;
				sheetTable.getRange(currentHeaderCell, currentEndCell).setName("Total_EUR");
				sheetTable.getRange(currentHeaderCell).setValue("Total [EUR]");
				sheetTable.getRange(formulaStartCell).setFormula("=AVRUND(HVIS(Q2=\"EUR\";P2;(P2/" + convertedSellRate + ")*R2);0)"); 
				currentEndCell = sheetTable.getRange(currentEndCell).getNext().getAddress();
				formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				currentHeaderCell = sheetTable.getRange(currentHeaderCell).getNext().getAddress();

				/*
				 *  Creates the delay part of the "Table"-sheet. The formulas are put into one cell and then by using fillDown all cells are given
				 */
				sheetTable.getRange(currentHeaderCell, currentEndCell).setName("RFICDD");
				sheetTable.getRange(currentHeaderCell).setValue("RFI-CDD");
				sheetTable.getRange(formulaStartCell).setFormula("=HVIS(U2=\"\"; 0; HVIS(S2=\"\"; 0; ((U2-S2)/7)))"); 
				sheetTable.getRange(formulaStartCell).setNumberFormat("Standard");
				currentEndCell = sheetTable.getRange(currentEndCell).getNext().getAddress();
				formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				currentHeaderCell = sheetTable.getRange(currentHeaderCell).getNext().getAddress();

				sheetTable.getRange(currentHeaderCell, currentEndCell).setName("DelayRFICDD");
				sheetTable.getRange(currentHeaderCell).setValue("Delay(RFI-CDD)");
				sheetTable.getRange(formulaStartCell).setFormula("=AVRUND(Z2;0)");
				currentEndCell = sheetTable.getRange(currentEndCell).getNext().getAddress();
				formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				currentHeaderCell = sheetTable.getRange(currentHeaderCell).getNext().getAddress();

				sheetTable.getRange(currentHeaderCell, currentEndCell).setName("RFIOrderDate");
				sheetTable.getRange(currentHeaderCell).setValue("RFI-Order Date");
				sheetTable.getRange(formulaStartCell).setFormula("=HVIS(U2=\"\"; 0; HVIS(G2=\"\"; 0; AVRUND(((U2-G2)/7);0)))");
				currentEndCell = sheetTable.getRange(currentEndCell).getNext().getAddress();
				formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				currentHeaderCell = sheetTable.getRange(currentHeaderCell).getNext().getAddress();

				sheetTable.getRange(currentHeaderCell, currentEndCell).setName("CDDOrderdate");
				sheetTable.getRange(currentHeaderCell).setValue("CDD-Order date");
				sheetTable.getRange(formulaStartCell).setFormula("=HVIS(S2=\"\"; 0; HVIS(G2=\"\"; 0; AVRUND(((S2-G2)/7);0)))");
				Range yellow = sheetTable.getRange(startCell, currentEndCell);
				yellow.fillDown();  

				currentEndCell = sheetTable.getRange(currentEndCell).getNext().getAddress();
				formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				currentHeaderCell = sheetTable.getRange(currentHeaderCell).getNext().getAddress();

				sheetTable.getRange("TotalPrice").setNumberFormat("0");
				sheetTable.getRange("UnitPrice").setNumberFormat("0");
				sheetTable.getRange("Vendornr.").setNumberFormat("000000");

				/*
				 * Pseudo: For each row in Table
				 * If (Conditions) set
				 * column ("AD") interior red and error text
				 * 
				 * Conditions:
				 * If ItemStatus is Delivered or RFI Notified and missing RFI
				 * If ItemStatus is �On Hold�
				 * If ItemStatus is created
				 * If missing CDD or EDD
				 * If ItemStatus is NOT Delivered set EDD = RFI, unless historical date.
				 */

				sheetTable.getListObjects().add(); // Creates the excel table

				/*
				 * Colors rows red based on certain criteria 
				 */

				ListRows rows = sheetTable.getListObjects().getItem(0).getListRows();
				int count = rows.getCount();
				int row = 1;
				setProgress(0);
				processed = 0;
				DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.yyyy");

				// TODO: get values as array, manipulate data and post back
				if(markErrorsCheckBox.isSelected()){
					while(row++ < count){ 
						progressField.setText("Marking erroneus rows");
						setProgress(100 * ++processed / count);

						String itemStatusValue = sheetTable.getRange("ItemStatus").getRows().getItem(row).getValue();
						String cddValue = sheetTable.getRange("CDD").getRows().getItem(row).getValue();
						String eddValue = sheetTable.getRange("EDD").getRows().getItem(row).getValue();
						String rfiValue = sheetTable.getRange("RFI").getRows().getItem(row).getValue();

						Boolean isHistoricalDate = false;
						Boolean isEmpty = false;

						if(rfiValue.equalsIgnoreCase("")) {
							isEmpty = true;
						}else{
							DateTime historical = fmt.parseDateTime(rfiValue);
							if(historical.isBeforeNow()) isHistoricalDate = true;
						}
						if(!itemStatusValue.equalsIgnoreCase("Delivered") && !isHistoricalDate && !isEmpty){
							sheetTable.getRange("EDD").getRows().getItem(row).setValue(rfiValue); 
						}
						if(itemStatusValue.equalsIgnoreCase("Delivered") || itemStatusValue.equalsIgnoreCase("RFI Notified")) {
							if(rfiValue.equalsIgnoreCase("")) rows.getItem(row-1).getRange().getInterior().setColor(Color.red);
						}
						if(itemStatusValue.equalsIgnoreCase("On Hold") || itemStatusValue.equalsIgnoreCase("Created")) {
							rows.getItem(row-1).getRange().getInterior().setColor(Color.red);
						}
						if(cddValue.equalsIgnoreCase("") || eddValue.equalsIgnoreCase(" ")) {
							rows.getItem(row-1).getRange().getInterior().setColor(Color.red);
						}
					}
				}

				/*
				 * Inserts the formulae into the "Project"-sheet.
				 */

				int rowPointer = 0;
				setProgress(0);
				processed = 0;

				String totalValueEurAddress = sheetTable.getRange("Total_EUR").getAddress(false, false);
				String totalValueEurAddressAbsolute = sheetTable.getRange("Total_EUR").getAddress(true, true);
				String delayAddressAbsolute = sheetTable.getRange("RFICDD").getAddress(true, true);
				String roundedDelayAddressAbsolute = sheetTable.getRange("DelayRFICDD").getAddress(true, true);
				String rfiMinusOrderAddressAbsolute = sheetTable.getRange("RFIOrderDate").getAddress(true, true);
				String cddMinusOrderAddressAbsolute = sheetTable.getRange("CDDOrderdate").getAddress(true, true);

				excel.getWorksheets().getItem("Project").activate();
				String firstCell = "A1";
				sheetProject.getRange(firstCell).activate();

				for(String projectName : projectSet){
					publishedOutput.setText("Creating Project Sheet");
					progressField.setText("Processing project: " + processed);
					setProgress(100 * ++processed / projectSet.size());

					Range headerRange = excel.getActiveCell();
					headerRange.setValue("Project");
					headerRange = headerRange.getNext();
					headerRange.setValue("Properties");
					headerRange = headerRange.getNext();
					headerRange.setValue("Total Value");
					headerRange = headerRange.getNext();
					headerRange.setValue("Total [EUR]");
					String totalCellAddress = headerRange.getOffset(currencySet.size()+1).getAddress();
					headerRange = headerRange.getNext();
					headerRange.setValue("Total Items");
					String totalItemsAddress = headerRange.getOffset(currencySet.size()+1).getAddress();
					headerRange = headerRange.getNext();
					headerRange.setValue("Value [%]");
					headerRange = headerRange.getNext();
					headerRange.setValue("Item [%]");

					String leftHeaderEnd = excel.getActiveCell().getEntireRow().getEnd(Direction.TO_LEFT).getAddress(false, false);
					String rightHeaderEnd = excel.getActiveCell().getEntireRow().getEnd(Direction.TO_RIGHT).getAddress(false, false);
					sheetProject.getRange(leftHeaderEnd, rightHeaderEnd).getInterior().setColor(Color.yellow);

					for(String currencyName : currencySet){
						excel.getActiveCell().getOffset(1).activate();
						Range currentCell = excel.getActiveCell();
						currentCell.setValue(projectName);
						currentCell = currentCell.getNext();
						currentCell.setValue("Total Value [" + currencyName + "]:");
						currentCell = currentCell.getNext();
						String totalValueFormula = "=SUMMER.HVIS.SETT(Table!" + totalValueRangeAddress + ";Table!" + currencyRangeAddress + ";\"" + currencyName + "\";Table!" + projectRangeAddress + ";\"" + projectName + "\")";
						currentCell.setNumberFormat("# #0"); // Thousand separator with 0 decimals
						currentCell.setFormula(totalValueFormula);
						currentCell = currentCell.getNext();
						String totalValueEurFormula = "=SUMMER.HVIS.SETT(Table!" + totalValueEurAddress + ";Table!" + currencyRangeAddress + ";\"" + currencyName + "\";Table!" + projectRangeAddress + ";\"" + projectName + "\")";
						currentCell.setNumberFormat("# #0");
						currentCell.setFormula(totalValueEurFormula);
						currentCell = currentCell.getNext();
						String quantityFormula = "=SUMMER.HVIS.SETT(Table!" + quantityRangeAddress + ";Table!" + currencyRangeAddress + ";\"" + currencyName + "\";Table!" + projectRangeAddress + ";\"" + projectName + "\")";
						currentCell.setNumberFormat("# #0");
						currentCell.setFormula(quantityFormula);
						currentCell = currentCell.getNext();
						String valuePercentFormula = "=HVIS("+ totalCellAddress + "=0;0;" + currentCell.getOffset(0, -2).getAddress() + "/" + totalCellAddress +")";
						currentCell.setNumberFormat("0,00 %");
						currentCell.setFormula(valuePercentFormula);
						currentCell = currentCell.getNext();
						String itemPercentFormula = "=HVIS(" + totalItemsAddress + "=0;0;" + currentCell.getOffset(0, -2).getAddress() + "/" + totalItemsAddress +")";
						currentCell.setNumberFormat("0,00 %");
						currentCell.setFormula(itemPercentFormula);
					}
					excel.getActiveCell().getOffset(1).activate();
					Range currentCell = excel.getActiveCell();

					currentCell.setValue(projectName);
					currentCell = currentCell.getNext();
					currentCell.setValue("Total:");
					currentCell = currentCell.getOffset(0, 2);
					currentCell.setFormula("=SUMMER(" + currentCell.getOffset(-currencySet.size()).getAddress() + ":" + currentCell.getOffset(-1).getAddress() + ")");
					currentCell = currentCell.getNext();
					currentCell.setFormula("=SUMMER(" + currentCell.getOffset(-currencySet.size()).getAddress() + ":" + currentCell.getOffset(-1).getAddress() + ")");
					excel.getActiveCell().getOffset(1).activate();

				}
				sheetProject.getRange(firstCell, excel.getActiveCell().getAddress()).autoFilter(1);
				excel.getActiveCell().getOffset(-1, 6).activate();
				sheetProject.getRange(firstCell, excel.getActiveCell().getAddress()).getBorders().setLineStyle(LineStyle.CONTINUOUS);

				/*
				 * Populates the "Delay"-sheet
				 */

				progressField.setText("Creating the Delay sheet");

				excel.getWorksheets().getItem("Delay").activate();
				sheetDelay.getRange(firstCell).activate();
				Range currentCellDelay = excel.getActiveCell();
				String previousCellAddress = "";
				String endCellAddress = "";

				int scopeRange = 40; // Creates a range of negative 20 to positive 20

				currentCellDelay.setValue(" ");
				currentCellDelay = currentCellDelay.getNext();
				currentCellDelay.setValue("Total");
				currentCellDelay = currentCellDelay.getNext();

				// Creates a row of ascending integers
				currentCellDelay.setValue(-scopeRange/2);
				previousCellAddress = currentCellDelay.getAddress(false, false);
				String categoryRangeAddress = currentCellDelay.resize(1, scopeRange+1).getAddress(false, false);
				currentCellDelay = currentCellDelay.getNext();
				currentCellDelay.setFormula("=" + previousCellAddress + "+1");
				currentCellDelay.resize(1, scopeRange).fillRight();

				// Creates the units row
				excel.getActiveCell().getOffset(1).activate();
				currentCellDelay = excel.getActiveCell();
				currentCellDelay.setValue("No of Units");
				currentCellDelay = currentCellDelay.getNext();
				currentCellDelay.setFormula("=SUMMER(Table!" + quantityRangeAddress + ")");
				String totalNoUnitsAddress = currentCellDelay.getAddress();
				currentCellDelay = currentCellDelay.getNext();
				currentCellDelay.setFormula("=SUMMERHVIS(Table!" + roundedDelayAddressAbsolute + ";C1;Table!" + quantityRangeAddressAbsolute +")"); // TODO: Change from C1 to .offset()
				currentCellDelay.resize(1, scopeRange+1).fillRight();

				// Creates the items row
				excel.getActiveCell().getOffset(1).activate();
				currentCellDelay = excel.getActiveCell();
				currentCellDelay.setValue("No of Items");
				currentCellDelay = currentCellDelay.getNext();
				currentCellDelay.setFormula("=ANTALL(Table!" + quantityRangeAddress + ")");
				String totalNoItemsAddress = currentCellDelay.getAddress();
				currentCellDelay = currentCellDelay.getNext();
				currentCellDelay.setFormula("=ANTALL.HVIS(Table!" + roundedDelayAddressAbsolute + ";C1" +")");
				currentCellDelay.resize(1, scopeRange+1).fillRight();

				// Creates the value row
				excel.getActiveCell().getOffset(1).activate();
				currentCellDelay = excel.getActiveCell();
				currentCellDelay.setValue("Value[EUR]");
				currentCellDelay = currentCellDelay.getNext();
				currentCellDelay.setFormula("=SUMMER(Table!" + totalValueEurAddress + ")");
				String totalValueAddress = currentCellDelay.getAddress();
				currentCellDelay = currentCellDelay.getNext();
				currentCellDelay.setFormula("=SUMMERHVIS(Table!" + roundedDelayAddressAbsolute + ";C1;Table!" + totalValueEurAddressAbsolute +")");
				currentCellDelay.resize(1, scopeRange+1).fillRight();

				// Creates the row that calculates the accumulated number of units
				excel.getActiveCell().getOffset(1).activate();
				currentCellDelay = excel.getActiveCell();
				currentCellDelay.setValue("Accumulated No of Units [%]");
				currentCellDelay = currentCellDelay.getNext();
				endCellAddress = currentCellDelay.getOffset(0, scopeRange).getAddress();
				currentCellDelay.setFormula("=" + endCellAddress);
				currentCellDelay.setNumberFormat("0,00 %");
				currentCellDelay = currentCellDelay.getNext();
				String noUnitsPerWeekAddress = currentCellDelay.getOffset(-3).getAddress();				
				currentCellDelay.setFormula("=" + noUnitsPerWeekAddress + "/" + totalNoUnitsAddress);		// Number of units per week divided by the total amount of units
				previousCellAddress = currentCellDelay.getAddress(false, false);
				currentCellDelay = currentCellDelay.getNext();
				String chartStartAddress = currentCellDelay.getAddress(false, false);
				noUnitsPerWeekAddress = currentCellDelay.getOffset(-3).getAddress(false, false);
				currentCellDelay.setFormula("=(" + noUnitsPerWeekAddress + "/" + totalNoUnitsAddress +")+" + previousCellAddress); // Accumulated no units
				currentCellDelay.resize(1, scopeRange).fillRight();
				currentCellDelay.resize(1, scopeRange).setNumberFormat("0,00 %");

				// Creates the row that calculates the accumulated number of items
				excel.getActiveCell().getOffset(1).activate();
				currentCellDelay = excel.getActiveCell();
				currentCellDelay.setValue("Accumulated No of Items [%]");
				currentCellDelay = currentCellDelay.getNext();
				endCellAddress = currentCellDelay.getOffset(0, scopeRange).getAddress();
				currentCellDelay.setFormula("=" + endCellAddress);
				currentCellDelay.setNumberFormat("0,00 %");
				currentCellDelay = currentCellDelay.getNext();
				noUnitsPerWeekAddress = currentCellDelay.getOffset(-3).getAddress();				
				currentCellDelay.setFormula("=" + noUnitsPerWeekAddress + "/" + totalNoItemsAddress);		
				previousCellAddress = currentCellDelay.getAddress(false, false);
				currentCellDelay = currentCellDelay.getNext();
				noUnitsPerWeekAddress = currentCellDelay.getOffset(-3).getAddress(false, false);
				currentCellDelay.setFormula("=(" + noUnitsPerWeekAddress + "/" + totalNoItemsAddress +")+" + previousCellAddress); 
				currentCellDelay.resize(1, scopeRange).fillRight();
				currentCellDelay.resize(1, scopeRange).setNumberFormat("0,00 %");

				// Creates the row that calculates the accumulated value
				excel.getActiveCell().getOffset(1).activate();
				currentCellDelay = excel.getActiveCell();
				currentCellDelay.setValue("Accumulated Value [%]");
				currentCellDelay = currentCellDelay.getNext();
				endCellAddress = currentCellDelay.getOffset(0, scopeRange).getAddress();
				currentCellDelay.setFormula("=" + endCellAddress);
				currentCellDelay.setNumberFormat("0,00 %");
				currentCellDelay = currentCellDelay.getNext();
				noUnitsPerWeekAddress = currentCellDelay.getOffset(-3).getAddress();				
				currentCellDelay.setFormula("=" + noUnitsPerWeekAddress + "/" + totalValueAddress);		
				previousCellAddress = currentCellDelay.getAddress(false, false);
				currentCellDelay = currentCellDelay.getNext();
				noUnitsPerWeekAddress = currentCellDelay.getOffset(-3).getAddress(false, false);
				currentCellDelay.setFormula("=(" + noUnitsPerWeekAddress + "/" + totalValueAddress +")+" + previousCellAddress); 
				currentCellDelay.resize(1, scopeRange).fillRight();
				currentCellDelay.resize(1, scopeRange).setNumberFormat("0,00 %");
				String chartEndAddress = currentCellDelay.getEnd(Direction.TO_RIGHT).getAddress(false, false);

				progressField.setText("Creating the Delay chart");

				ChartObject delayChartObject = sheetDelay.getChartObjects().add(100, 200, 1000, 250); // Charts exists within chart objects, which in turn sets the size and location
				Chart delayChart = delayChartObject.getChart();
				delayChart.setChartType(ChartType.LINE);
				delayChart.setSourceData(sheetDelay.getRange(chartStartAddress, chartEndAddress));
				delayChart.getAxis(AxisType.CATEGORY).setHasMajorGridlines(true); // X-axis = Category, Y-axis = Value
				delayChart.getAxis(AxisType.CATEGORY).setCategoryNames(sheetDelay.getRange(categoryRangeAddress));
				delayChart.getAxis(AxisType.CATEGORY).setAxisBetweenCategories(false);
				delayChart.getAxis(AxisType.CATEGORY).setTickLabelSpacing(2);
				delayChart.getSeries(0).setName("Accumulated No of Units");
				delayChart.getSeries(1).setName("Accumulated No of Items");
				delayChart.getSeries(2).setName("Accumulated Value");

				sheetDelay.getListObjects().add().setShowAutoFilter(false);

				/*
				 * Populates the DelPerformance sheet
				 */

				progressField.setText("Creating the Performance sheet");

				rowPointer = 1;
				int cddHeaderRow = rowPointer++;
				int cddWeekRow = rowPointer++;
				int cddItemRow = rowPointer++;
				int cddAccItemRow = rowPointer++;

				int rfiHeaderRow = rowPointer+=2;
				int rfiWeekRow = ++rowPointer;
				int rfiItemRow = ++rowPointer;
				int rfiAccItemRow = ++rowPointer;

				rowPointer = 2;
				sheetDelPerformance.getCell(cddHeaderRow, 2).setValue("CDD-Order Date");
				String endCell = "BC" + Integer.toString(rowPointer++);
				sheetDelPerformance.getRange("C2", endCell).fillRight();

				sheetDelPerformance.getCell(cddWeekRow, 1).setValue("Total");
				sheetDelPerformance.getCell(cddWeekRow, 2).setValue(0);
				sheetDelPerformance.getCell(cddWeekRow, 3).setFormula("=C3+1");
				endCell = "BC" + Integer.toString(rowPointer++);
				sheetDelPerformance.getRange("D3", endCell).fillRight();
				Color blue = new Color(0,128,255);
				sheetDelPerformance.getRange("C2", endCell).getInterior().setColor(blue);

				sheetDelPerformance.getCell(cddItemRow, 0).setValue("No of Contractual Items to Deliver");
				sheetDelPerformance.getCell(cddItemRow, 1).setFormula("=ANTALL(Table!" + quantityRangeAddress + ")");
				sheetDelPerformance.getCell(cddItemRow, 2).setFormula("=ANTALL.HVIS(Table!" + cddMinusOrderAddressAbsolute + ";C3)");
				endCell = "BC" + Integer.toString(rowPointer++);
				sheetDelPerformance.getRange("C4", endCell).fillRight();

				sheetDelPerformance.getCell(cddAccItemRow, 0).setValue("Accumulated Contractual Items to Deliver");
				sheetDelPerformance.getCell(cddAccItemRow, 2).setFormula("=C4/B4");
				sheetDelPerformance.getCell(cddAccItemRow, 3).setFormula("=(D4/$B$4)+C5");
				endCell = "BC" + Integer.toString(rowPointer++);
				sheetDelPerformance.getRange("D5", endCell).fillRight();
				sheetDelPerformance.getRange("B5").setFormula("=" + endCell);
				sheetDelPerformance.getRange("B5", endCell).setNumberFormat("0,00 %");
				sheetDelPerformance.getRange("A2", endCell).getBorders().setLineStyle(LineStyle.CONTINUOUS);

				sheetDelPerformance.getCell(rfiHeaderRow, 2).setValue("RFI-Order Date");
				endCell = "BC" + Integer.toString(rowPointer+=2);
				sheetDelPerformance.getRange("C8", endCell).fillRight();

				sheetDelPerformance.getCell(rfiWeekRow, 1).setValue("Total");
				sheetDelPerformance.getCell(rfiWeekRow, 2).setValue(0);
				sheetDelPerformance.getCell(rfiWeekRow, 3).setFormula("=C9+1");
				endCell = "BC" + Integer.toString(++rowPointer);
				sheetDelPerformance.getRange("D9", endCell).fillRight();
				sheetDelPerformance.getRange("C8", endCell).getInterior().setColor(Color.red);

				sheetDelPerformance.getCell(rfiItemRow, 0).setValue("No of Actual Items Delivered");
				sheetDelPerformance.getCell(rfiItemRow, 1).setFormula("=ANTALL(Table!" + quantityRangeAddress + ")");
				sheetDelPerformance.getCell(rfiItemRow, 2).setFormula("=ANTALL.HVIS(Table!" + rfiMinusOrderAddressAbsolute + ";C9)");
				endCell = "BC" + Integer.toString(++rowPointer);
				sheetDelPerformance.getRange("C10", endCell).fillRight();

				sheetDelPerformance.getCell(rfiAccItemRow, 0).setValue("Accumulated Actual Items Delivered");
				sheetDelPerformance.getCell(rfiAccItemRow, 2).setFormula("=C10/B10");
				sheetDelPerformance.getCell(rfiAccItemRow, 3).setFormula("=(D10/$B$10)+C11");
				endCell = "BC" + Integer.toString(++rowPointer);
				sheetDelPerformance.getRange("D11", endCell).fillRight();
				sheetDelPerformance.getRange("B11").setFormula("=" + endCell);
				sheetDelPerformance.getRange("B11", endCell).setNumberFormat("0,00 %");
				sheetDelPerformance.getRange("A8", endCell).getBorders().setLineStyle(LineStyle.CONTINUOUS);

				progressField.setText("Creating the Performance Chart");

				Range performanceChartSourceData = excel.union(sheetDelPerformance.getRange("C5","BC5"), sheetDelPerformance.getRange("C11","BC11"));
				ChartObject delPerformanceChartObject = sheetDelPerformance.getChartObjects().add(50, 250, 500, 250);
				Chart delPerformanceChart = delPerformanceChartObject.getChart();
				delPerformanceChart.setChartType(ChartType.LINE);
				delPerformanceChart.setSourceData(performanceChartSourceData);
				delPerformanceChart.getAxis(AxisType.CATEGORY).setHasMajorGridlines(true); // X-axis = Category, Y-axis = Value
				delPerformanceChart.getAxis(AxisType.CATEGORY).setCategoryNames(sheetDelPerformance.getRange("C3:BC3"));
				delPerformanceChart.getAxis(AxisType.CATEGORY).setAxisBetweenCategories(false);
				delPerformanceChart.getAxis(AxisType.CATEGORY).setTickLabelSpacing(2);
				delPerformanceChart.getAxis(AxisType.CATEGORY).setHasTitle(true);
				delPerformanceChart.getAxis(AxisType.CATEGORY).getAxisTitle().setText("Weeks");
				delPerformanceChart.getSeries(0).setName("Contractual Items");
				delPerformanceChart.getSeries(1).setName("Delivered Items");
				delPerformanceChart.setHasTitle(true);
				delPerformanceChart.getChartTitle().setText("Delivery");

				/*
				 * Populates the DelMill sheet
				 */

				rowPointer = 1;
				int chartX = 0;
				int chartY = 15;
				processed = 0;

				publishedOutput.setText("Creating Delay Mill Sheet");
				for(String mill : millSet){

					progressField.setText("Processing Delay Mill: " + processed);
					setProgress(100 * ++processed / millSet.size());

					int headerRow = rowPointer++;
					int delayRow = rowPointer++;
					int unitRow = rowPointer++;
					int itemRow = rowPointer++;
					int valueRow = rowPointer++;
					int accUnitRow = rowPointer++;
					int accItemRow = rowPointer++;
					int accValueRow = rowPointer++;

					int columnPointer = 7;

					if(mill.length()==0) mill = " ";
					sheetDelMill.getCell(headerRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(delayRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(unitRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(itemRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(valueRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(accUnitRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(accItemRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(accValueRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(accValueRow+1, columnPointer).setValue(mill);
					sheetDelMill.getCell(accValueRow+2, columnPointer).setValue(mill);
					sheetDelMill.getCell(accValueRow+3, columnPointer).setValue(mill);
					sheetDelMill.getCell(accValueRow+4, columnPointer).setValue(mill);
					sheetDelMill.getCell(accValueRow+5, columnPointer).setValue(mill);
					sheetDelMill.getCell(accValueRow+6, columnPointer++).setValue(mill);

					sheetDelMill.getCell(unitRow, columnPointer).setValue("No of Units");
					sheetDelMill.getCell(itemRow, columnPointer).setValue("No of Items");
					sheetDelMill.getCell(valueRow, columnPointer).setValue("Value [EUR]");
					sheetDelMill.getCell(accUnitRow, columnPointer).setValue("Accumulated No of Units [%]");
					sheetDelMill.getCell(accItemRow, columnPointer).setValue("Accumulated No of Items [%]");
					sheetDelMill.getCell(accValueRow, columnPointer++).setValue("Accumulated Value [%]");
					sheetDelMill.getCell(headerRow, columnPointer).setValue("Total Value");

					String unitFormula = "=SUMMERHVIS(Table!" + supplierRangeAddressAbsolute + ";\"" +  mill + "\";Table!" + quantityRangeAddressAbsolute + ")";
					String itemFormula = "=ANTALL.HVIS(Table!" + supplierRangeAddressAbsolute + ";\"" +  mill + "\")";
					String totalValueFormula = "=SUMMERHVIS(Table!" + supplierRangeAddressAbsolute + ";\"" +  mill + "\";Table!" + totalValueEurAddressAbsolute + ")";

					sheetDelMill.getCell(unitRow, columnPointer).setFormula(unitFormula);
					sheetDelMill.getCell(itemRow, columnPointer).setFormula(itemFormula);
					sheetDelMill.getCell(valueRow, columnPointer).setFormula(totalValueFormula);

					String endCol = "AY";
					endCell = endCol + Integer.toString(headerRow+1);
					sheetDelMill.getCell(headerRow, 10).setValue("Delay");
					sheetDelMill.getRange("K" + Integer.toString(headerRow+1), endCell).fillRight();
					sheetDelMill.getRange("J" + Integer.toString(headerRow+1), endCell).getInterior().setColor(Color.yellow);

					endCell = endCol + Integer.toString(delayRow+1); 
					sheetDelMill.getCell(delayRow, 10).setValue(-20);
					sheetDelMill.getCell(delayRow, 11).setFormula("=K" + Integer.toString(delayRow+1) + "+1");
					sheetDelMill.getRange("L" + Integer.toString(delayRow+1), endCell).fillRight(); 

					endCell = endCol + Integer.toString(unitRow+1); 
					unitFormula = "=SUMMER.HVIS.SETT(Table!" + quantityRangeAddressAbsolute + ";Table!" + supplierRangeAddressAbsolute + ";\"" + mill + "\";Table!" + roundedDelayAddressAbsolute + ";K" + Integer.toString(delayRow+1) + ")";
					sheetDelMill.getCell(unitRow, 10).setFormula(unitFormula);
					sheetDelMill.getRange("K" + Integer.toString(unitRow+1), endCell).fillRight();

					endCell = endCol + Integer.toString(itemRow+1);
					itemFormula = "=ANTALL.HVIS.SETT(Table!" + roundedDelayAddressAbsolute + ";K" + Integer.toString(delayRow+1) + ";Table!" + supplierRangeAddressAbsolute + ";\"" + mill +"\")";
					sheetDelMill.getCell(itemRow, 10).setFormula(itemFormula);
					sheetDelMill.getRange("K" + Integer.toString(itemRow+1), endCell).fillRight();

					endCell = endCol + Integer.toString(valueRow+1); 
					totalValueFormula = "=SUMMER.HVIS.SETT(Table!" + totalValueEurAddressAbsolute + ";Table!" + supplierRangeAddressAbsolute + ";\"" + mill + "\";Table!" + roundedDelayAddressAbsolute + ";K" + Integer.toString(delayRow+1) + ")";
					sheetDelMill.getCell(valueRow, 10).setFormula(totalValueFormula);
					sheetDelMill.getRange("K" + Integer.toString(valueRow+1), endCell).fillRight();

					//TODO: #DIV/0!
					endCell = endCol + Integer.toString(accUnitRow+1);
					sheetDelMill.getCell(accUnitRow, 9).setFormula("=" + endCell);
					sheetDelMill.getCell(accUnitRow, 10).setFormula("=K" + Integer.toString(unitRow+1) + "/J" + Integer.toString(unitRow+1));
					sheetDelMill.getCell(accUnitRow, 11).setFormula("=(L" + Integer.toString(unitRow+1) + "/$J$" + Integer.toString(unitRow+1) +")+K" + Integer.toString(accUnitRow+1));
					sheetDelMill.getRange("L" + Integer.toString(accUnitRow+1), endCell).fillRight();

					endCell = endCol + Integer.toString(accItemRow+1);
					sheetDelMill.getCell(accItemRow, 9).setFormula("=" + endCell);
					sheetDelMill.getCell(accItemRow, 10).setFormula("=K" + Integer.toString(itemRow+1) + "/J" + Integer.toString(itemRow+1));
					sheetDelMill.getCell(accItemRow, 11).setFormula("=(L" + Integer.toString(itemRow+1) + "/$J$" + Integer.toString(itemRow+1) + ")+K" + Integer.toString(accItemRow+1));
					sheetDelMill.getRange("L" + Integer.toString(accItemRow+1), endCell).fillRight();

					endCell = endCol + Integer.toString(accValueRow+1);
					sheetDelMill.getCell(accValueRow, 9).setFormula("=" + endCell);
					sheetDelMill.getCell(accValueRow, 10).setFormula("=K" + Integer.toString(valueRow+1) + "/J" + Integer.toString(valueRow+1));
					sheetDelMill.getCell(accValueRow, 11).setFormula("=(L" + Integer.toString(valueRow+1) + "/$J$" + Integer.toString(valueRow+1) + ")+K" + Integer.toString(accValueRow+1));
					sheetDelMill.getRange("L" + Integer.toString(accValueRow+1), endCell).fillRight();

					sheetDelMill.getRange("J" + Integer.toString(accUnitRow+1), endCell).setNumberFormat("0,00 %");
					sheetDelMill.getRange("H" + Integer.toString(headerRow+1), endCell).getBorders().setLineStyle(LineStyle.CONTINUOUS);

					ChartObject delMillChartObject = sheetDelMill.getChartObjects().add(chartX, chartY, 480, 210); // Charts exists within chart objects, which in turn sets the size and location
					Chart delMillChart = delMillChartObject.getChart();
					delMillChart.setChartType(ChartType.LINE);
					delMillChart.setSourceData(sheetDelMill.getRange("K" + Integer.toString(accUnitRow+1), endCell));
					delMillChart.getAxis(AxisType.CATEGORY).setHasMajorGridlines(true); // X-axis = Category, Y-axis = Value
					delMillChart.getAxis(AxisType.CATEGORY).setCategoryNames(sheetDelMill.getRange("K" + Integer.toString(delayRow+1) + ":" + endCol + Integer.toString(delayRow+1)));
					delMillChart.getAxis(AxisType.CATEGORY).setAxisBetweenCategories(false);
					delMillChart.getAxis(AxisType.CATEGORY).setTickLabelSpacing(2);
					delMillChart.getAxis(AxisType.CATEGORY).setHasTitle(true);
					delMillChart.getAxis(AxisType.CATEGORY).getAxisTitle().setText("Weeks");
					delMillChart.getSeries(0).setName("Accumulated No of Units");
					delMillChart.getSeries(1).setName("Accumulated No of Items");
					delMillChart.getSeries(2).setName("Accumulated Value");
					delMillChart.setHasTitle(true);
					delMillChart.getChartTitle().setText(mill);
					rowPointer+=10;
					chartY+=270;
				}

				sheetDelMill.getRange("H1").setValue("Filter");
				sheetDelMill.getRange("H1").getInterior().setColor(Color.green);
				sheetDelMill.getRange("H1", "H"+rowPointer).autoFilter(1);


				/*
				 * Creates and populates the "Mill"-sheet
				 */
				publishedOutput.setText("Creating Mill Sheet");
				String firstCellMill = "A1";
				excel.getWorksheets().getItem("Mill").activate();
				sheetMill.getRange(firstCellMill).activate();

				Range currentCellMill = excel.getActiveCell();
				currentCellMill.setValue("Mill");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setValue("Average Required Delivery");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setValue("Average Actual Delivery > CDD"); // ?????????
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setValue("Average Actual Delivery");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setValue("No of Units");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setValue("No of Items");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setValue("Value");

				excel.getActiveCell().getOffset(1).activate();
				currentCellMill = excel.getActiveCell();
				currentCellMill.setValue("Total");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setFormula("=AVRUND(GJENNOMSNITT(Table!" + cddMinusOrderAddressAbsolute + ");1)");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setFormula("=AVRUND(GJENNOMSNITT(Table!" + delayAddressAbsolute + ");1)");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setFormula("=AVRUND(GJENNOMSNITT(Table!" + rfiMinusOrderAddressAbsolute + ");1)");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setFormula("=SUMMER(Table!" + quantityRangeAddressAbsolute + ")");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setFormula("=ANTALL(Table!" + quantityRangeAddressAbsolute + ")");
				currentCellMill = currentCellMill.getNext();
				currentCellMill.setFormula("=SUMMER(Table!" + totalValueEurAddressAbsolute + ")");

				processed = 0;

				for(String mill : millSet){

					progressField.setText("Processing Mill: " + processed);
					setProgress(100 * ++processed / millSet.size());

					excel.getActiveCell().getOffset(1).activate();
					currentCellMill = excel.getActiveCell();
					currentCellMill.setValue(mill);
					currentCellMill = currentCellMill.getNext();
					currentCellMill.setFormula("=AVRUND(GJENNOMSNITTHVIS(Table!" + supplierRangeAddressAbsolute + ";\"" + mill + "\";Table!" + cddMinusOrderAddressAbsolute + ");1)");
					currentCellMill = currentCellMill.getNext();
					currentCellMill.setFormula("=AVRUND(GJENNOMSNITTHVIS(Table!" + supplierRangeAddressAbsolute + ";\"" + mill + "\";Table!" + delayAddressAbsolute + ");1)");
					currentCellMill = currentCellMill.getNext();
					currentCellMill.setFormula("=AVRUND(GJENNOMSNITTHVIS(Table!" + supplierRangeAddressAbsolute + ";\"" + mill + "\";Table!" + rfiMinusOrderAddressAbsolute + ");1)");
					currentCellMill = currentCellMill.getNext();
					currentCellMill.setFormula("=SUMMERHVIS(Table!" + supplierRangeAddressAbsolute + ";\"" + mill + "\";Table!" + quantityRangeAddressAbsolute + ")");
					currentCellMill = currentCellMill.getNext();
					currentCellMill.setFormula("=ANTALL.HVIS(Table!" + supplierRangeAddressAbsolute + ";\"" + mill + "\"" + ")");
					currentCellMill = currentCellMill.getNext();
					currentCellMill.setFormula("=SUMMERHVIS(Table!" + supplierRangeAddressAbsolute + ";\"" + mill + "\";Table!" + totalValueEurAddressAbsolute + ")");
				}

				String millRangeEnd = sheetMill.getRange("A1").getEnd(Direction.DOWN).getAddress(false, false);
				String millRange = "A3:" + millRangeEnd;

				String unitRangeEnd = sheetMill.getRange("E1").getEnd(Direction.DOWN).getAddress(false, false);
				String unitRange = "E3:" + unitRangeEnd;

				String itemRangeEnd = sheetMill.getRange("F1").getEnd(Direction.DOWN).getAddress(false, false);
				String itemRange = "F3:" + itemRangeEnd;

				String valueRangeEnd = sheetMill.getRange("G1").getEnd(Direction.DOWN).getAddress(false, false);
				String valueRange = "G3:" + valueRangeEnd;

				sheetMill.getListObjects().add().setShowAutoFilter(false);

				progressField.setText("Creating Pie Charts");

				/*
				 * Item Mill Chart Sheet
				 */

				ExcelHelper.create3DPieChart(0, 0, 1000, 600, sheetItemMill, sheetMill.getRange(itemRange), sheetMill.getRange(millRange), "Item Mill");

				/*
				 * Unit Mill Chart Sheet
				 */

				ExcelHelper.create3DPieChart(0, 0, 1000, 600, sheetNoUnits, sheetMill.getRange(unitRange), sheetMill.getRange(millRange), "Unit Mill");

				/*
				 * Value mill Chart sheet
				 */

				ExcelHelper.create3DPieChart(0, 0, 1000, 600, sheetValueMill, sheetMill.getRange(valueRange), sheetMill.getRange(millRange), "Value Mill");

				/*
				 * Open excel and close DB connection
				 */

				publishedOutput.setText("Opening Excel Document");
				progressField.setText("");
				saveWorkbook();
				db.closeConnection();
				excel.dispose();
				cancel(true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private String correctSpecialCharacters(String value){
		
		value = value.replaceAll("�", "AA");
		value = value.replaceAll("�", "aa");
		value = value.replaceAll("�", "OE");
		value = value.replaceAll("�", "oe");
		value = value.replaceAll("�", "AE");
		value = value.replaceAll("�", "ae");
		value = value.replaceAll("�", "OE");
		value = value.replaceAll("�", "oe");
		value = value.replaceAll("�", "AA");
		value = value.replaceAll("�", "aa");
		
		return value;
	}
	
	
	private StringBuilder generateQuery(boolean allCustSelected,
			boolean allProjSelected, boolean allCategoriesSelected, boolean allFrameAgrSelected, 
			List<List> temp_cust, List<List> temp_proj, JTextField categoryName, JTextField frameAgrName) {

		StringBuilder query = new StringBuilder(5000);

		if(!allCustSelected){
			query.append(" and customerList.assoc_id in (");
			for(List l : temp_cust){
				int id =  (int) l.get(1);
				query.append(id + ", ");
			}
			query.delete(query.length()-2, query.length());
			query.append(")");
		}
		if(!allProjSelected){ //TODO: Have to use ID instead of name
			query.append(" and Project.pr_name in (");
			for(List l : temp_proj){
				String name = (String) l.get(1);
				query.append("''" + name + "'', ");
			}
			query.delete(query.length()-2, query.length());
			query.append(")");
		}
		if(!allCategoriesSelected){
			query.append(" and Tr_category.category_name like ''" + categoryName.getText() + "''");
		}
		if(!allFrameAgrSelected){
			query.append(" and Frame_agr_catalog.frame_cat_name like ''" + frameAgrName.getText() + "''");
		}
		return query;
	}
}
