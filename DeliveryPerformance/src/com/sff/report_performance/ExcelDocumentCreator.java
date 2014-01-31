package com.sff.report_performance;

import java.awt.Color;
import java.io.File;
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

import com.borland.dx.dataset.VariantException;
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
	private Worksheet sheetExchangeRate;
	private Worksheet sheetTable;
	private Worksheet sheetProject;
	private Worksheet sheetDelay;
	private Worksheet sheetDelPerformance;
	private Worksheet sheetDelMill;
	private List<List> customerData;
	private List<List> projectData;
	private List<List> statusData;
	private JTextField publishedOutput;
	private JTextField progressField;

	public ExcelDocumentCreator(List<List> customerData, List<List> projectData, List<List> statusData, JTextField publishedOutput, JTextField progressField, File output){
		this.customerData = customerData;
		this.projectData = projectData;
		this.statusData = statusData;
		this.publishedOutput = publishedOutput;
		this.progressField = progressField;

		try {
			excel = new Excel();
			workbook = excel.getWorkbooks().add();

			sheetExchangeRate = new Worksheet(workbook);
			sheetExchangeRate.setName("Exchange");
			sheetDelMill = new Worksheet(workbook);
			sheetDelMill.setName("DelMill");
			sheetDelPerformance = new Worksheet(workbook);
			sheetDelPerformance.setName("DelPerformance");
			sheetDelay = new Worksheet(workbook);
			sheetDelay.setName("Delay");
			sheetProject = new Worksheet(workbook);
			sheetProject.setName("Project");
			sheetTable = new Worksheet(workbook);
			sheetTable.setName("Table");

		} catch (ComponentObjectModelException | LibraryNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void saveWorkbook() {
		sheetTable.getColumns().autoFit();
		sheetProject.getColumns().autoFit();
		sheetDelay.getColumns().autoFit();
		sheetExchangeRate.getColumns().autoFit();
		sheetDelPerformance.getColumns().autoFit();
		sheetDelMill.getColumns().autoFit();

		excel.setVisible(true);
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

			publishedOutput.setText("Creating Table Sheet");
			while(!isCancelled()){

				/*
				 *  Inserts exchange rates into the "Exchange Rate"-sheet.
				 */

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

				sheetExchangeRate.getRange("A1").setValue("Currency Type");
				sheetExchangeRate.getRange("B1").setValue("Exchange Value to NOK");

				Iterator iter = exchangeMap.entrySet().iterator();
				int rowIndex = 1;
				while(iter.hasNext()){
					int columnIndex = 0;
					Map.Entry pairs = (Map.Entry) iter.next();
					sheetExchangeRate.getRange(rowIndex, columnIndex++).setValue((String) pairs.getKey());
					Range value = sheetExchangeRate.getRange(rowIndex++, columnIndex);
					value.setValue((Double) pairs.getValue());
					value.setName((String) pairs.getKey());
				}

				sheetExchangeRate.getRange("A1:B16").getBorders().setLineStyle(LineStyle.CONTINUOUS);
				
				/*
				 * Extracts data from the database and inserts into the "Table"-sheet.
				 */
				Set<String> projectSet = new HashSet<String>();
				Set<String> millSet = new HashSet<String>();
				while(dataSet.next()){
					setProgress(100 * processed++ / rowCount);
					progressField.setText("Adding row: " + processed);

					//TODO: Apache POI for loading data faster? Use query tables? sheet.getQueryTables.Add()
					for(int column = 0; column < dataSet.getColumnCount(); column++){
						Range cell = sheetTable.getCell(dataSet.getRow(), column);

//						if(dataSet.getColumn(column).equals(dataSet.getColumn("currency"))){
//							currencySet.add(dataSet.getString(column));
//						}
//						if(dataSet.getColumn(column).equals(dataSet.getColumn("Client"))){
//							customerSet.add(dataSet.getString(column));
//						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Supplier"))){
							millSet.add(dataSet.getString(column));
						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Project"))){
							projectSet.add(dataSet.getString(column));
						}
						try{
							String s = dataSet.getString(column);
							if(s.length()>0)cell.setValue(s);
							else cell.setValue("EMPTY");
						}catch(VariantException e){
							try{
								Double d = dataSet.getDouble(column);
								cell.setValue(d);
							}catch(VariantException v){
								int i = dataSet.getInt(column);
								cell.setValue(i);
							}
						}
					}
				}
				String[] columnNames = dataSet.getColumnNames(dataSet.getColumnCount()); 
				dataSet.close();

				// TODO: probably some better way to get next column
				char index = 'A';
				int cellNumber = 1;
				String topIndex = Character.toString(index) + Integer.toString(cellNumber);

				/*
				 * Creates header row in the "Table"-sheet
				 */

				for(String column : columnNames){
					sheetTable.getRange(topIndex).setValue(column);
					String end = sheetTable.getRange(topIndex).getEntireColumn().getEnd(Direction.DOWN).getAddress(false, false);
					sheetTable.getRange(topIndex, end).setName(column);
					topIndex = Character.toString(++index) + Integer.toString(cellNumber);
				}

				String lastRow = sheetTable.getRange("Project").getEntireColumn().getEnd(Direction.DOWN).getAddress(false, false).substring(1);
				String endIndex = Character.toString(index) + lastRow;

				/*
				 * Converts total value into value[eur]
				 */

				// TODO: Language preference
				// TODO: Progress bar update!

				topIndex = Character.toString(index) + Integer.toString(cellNumber);
				sheetTable.getRange(topIndex, endIndex).setName("Total_EUR");
				String startIndex = Character.toString(index) + Integer.toString(++cellNumber);
				sheetTable.getRange(topIndex).setValue("Total [EUR]");
				topIndex = Character.toString(index) + Integer.toString(cellNumber);
				sheetTable.getRange(topIndex).setFormula("=AVRUND(FINN.RAD(M2; Exchange!$A$2:$B$16;2; USANN) / FINN.RAD(\"EUR\"; Exchange!$A$2:$B$16;2; USANN) * T2;0)");

				/*
				 *  Creates the delay part of the "Table"-sheet.
				 */

				topIndex = Character.toString(++index) + Integer.toString(--cellNumber);
				sheetTable.getRange(topIndex).setValue("RFI-CDD");
				topIndex = Character.toString(index) + Integer.toString(++cellNumber);
				sheetTable.getRange(topIndex).setFormula("=HVIS(N2=\"EMPTY\"; 0; HVIS(P2=\"EMPTY\"; 0; ((N2-P2)/7)))"); //TODO: get address from columns instead of hardcoded values
				sheetTable.getRange(topIndex).setNumberFormat("Standard");
				sheetTable.getRange(topIndex, endIndex).setName("RFICDD");
				
				topIndex = Character.toString(++index) + Integer.toString(--cellNumber);
				sheetTable.getRange(topIndex).setValue("Delay(RFI-CDD)");
				topIndex = Character.toString(index) + Integer.toString(++cellNumber);
				endIndex = Character.toString(index) + lastRow;
				sheetTable.getRange(topIndex).setFormula("=AVRUND(V2;0)");
				sheetTable.getRange(topIndex, endIndex).setName("DelayRFICDD");
				
				topIndex = Character.toString(++index) + Integer.toString(--cellNumber);
				sheetTable.getRange(topIndex).setValue("RFI-Order Date");
				topIndex = Character.toString(index) + Integer.toString(++cellNumber);
				endIndex = Character.toString(index) + lastRow;
				sheetTable.getRange(topIndex).setFormula("=HVIS(P2=\"EMPTY\"; 0; HVIS(E2=\"EMPTY\"; 0; AVRUND(((P2-E2)/7);0)))");
				sheetTable.getRange(topIndex, endIndex).setName("RFIOrderDate");

				topIndex = Character.toString(++index) + Integer.toString(--cellNumber);
				sheetTable.getRange(topIndex).setValue("CDD-Order date");
				topIndex = Character.toString(index) + Integer.toString(++cellNumber);
				endIndex = Character.toString(index) + lastRow;
				sheetTable.getRange(topIndex).setFormula("=HVIS(N2=\"EMPTY\"; 0; HVIS(E2=\"EMPTY\"; 0; AVRUND(((N2-E2)/7);0)))");
				sheetTable.getRange(topIndex, endIndex).setName("CDDOrderdate");
				Range yellow = sheetTable.getRange(startIndex, endIndex);
				yellow.fillDown(); 
				yellow.getBorders().setLineStyle(LineStyle.CONTINUOUS);
				yellow.getInterior().setColor(Color.yellow);

				sheetTable.getRange("Total_Price").getInterior().setColor(Color.yellow);
				sheetTable.getRange("Total_Price").getBorders().setLineStyle(LineStyle.CONTINUOUS);
				String endRow = sheetTable.getRange("A1").getEntireRow().getEnd(Direction.TO_RIGHT).getAddress(false, false);
				sheetTable.getRange("A1", endRow).getInterior().setColor(Color.yellow);
				sheetTable.getRange("Total_Price").setNumberFormat("0");
				sheetTable.getRange("Unit_Price").setNumberFormat("0");
				sheetTable.getRange("Vendor_nr.").setNumberFormat("000000");
				sheetTable.getRows().autoFilter(1); // why 1 or 2?

				/*
				 * Inserts the formulae into the "Project"-sheet.
				 */

				int rowPointer = 0;
				setProgress(0);
				processed = 0;
				
				String totalValueAddress = sheetTable.getRange("Total_Price").getAddress(false, false);
				String currencyAddress = sheetTable.getRange("currency").getAddress(false, false);
				String projectAddress = sheetTable.getRange("Project").getAddress(false, false);
				String totalValueEurAddress = sheetTable.getRange("Total_EUR").getAddress(false, false);
				String totalValueEurAddressAbsolute = sheetTable.getRange("Total_EUR").getAddress(true, true);
				String quantityAddress = sheetTable.getRange("QTY").getAddress(false, false);
				String quantityAddressAbsolute = sheetTable.getRange("QTY").getAddress(true, true);
				String delayAddressAbsolute = sheetTable.getRange("RFICDD").getAddress(true, true);
				String roundedDelayAddressAbsolute = sheetTable.getRange("DelayRFICDD").getAddress(true, true);
				String rfiMinusOrderAddressAbsolute = sheetTable.getRange("RFIOrderDate").getAddress(true, true);
				String cddMinusOrderAddressAbsolute = sheetTable.getRange("CDDOrderdate").getAddress(true, true);
				
				for(String project : projectSet){
					progressField.setText("Processing project: " + processed);
					setProgress(100 * processed++ / projectSet.size());

					rowPointer+=2;
					int columnPointer = 0;

					sheetProject.getCell(rowPointer,columnPointer++).setValue("Project");
					sheetProject.getCell(rowPointer,columnPointer++).setValue("Properties");
					sheetProject.getCell(rowPointer,columnPointer++).setValue("Total Value");
					sheetProject.getCell(rowPointer,columnPointer++).setValue("Total [EUR]");
					sheetProject.getCell(rowPointer,columnPointer++).setValue("Total Items");
					sheetProject.getCell(rowPointer,columnPointer++).setValue("Value [%]");
					sheetProject.getCell(rowPointer,columnPointer).setValue("Item [%]");
					String leftHeaderEnd = sheetProject.getCell(rowPointer, columnPointer).getEntireRow().getEnd(Direction.TO_LEFT).getAddress(false, false);
					String rightHeaderEnd = sheetProject.getCell(rowPointer, columnPointer).getEntireRow().getEnd(Direction.TO_RIGHT).getAddress(false, false);
					sheetProject.getRange(leftHeaderEnd, rightHeaderEnd).getInterior().setColor(Color.yellow);

					// TODO: Name start and end ranges and use proper names
					int uniqueCurrencies = Integer.parseInt(sheetExchangeRate.getRange("A1").getEntireColumn().getEnd(Direction.DOWN).getAddress(false, false).substring(1));
					int totalRowIndex = rowPointer + uniqueCurrencies + 1;
					for(int currencyIndex = 1; currencyIndex < uniqueCurrencies; currencyIndex++){
						rowPointer++;
						columnPointer = 0;
						sheetProject.getCell(rowPointer, columnPointer++).setValue(project);
						String currency = sheetExchangeRate.getCell(currencyIndex, 0).getValue();
						sheetProject.getCell(rowPointer, columnPointer++).setValue("Total Value [" + currency + "]:");
						String totalValueFormula = "=SUMMER.HVIS.SETT(Table!" + totalValueAddress + ";Table!" + currencyAddress + ";\"" + currency + "\";Table!" + projectAddress + ";\"" + project + "\")";
						sheetProject.getCell(rowPointer, columnPointer++).setFormula(totalValueFormula);
						String totalValueEurFormula = "=SUMMER.HVIS.SETT(Table!" + totalValueEurAddress + ";Table!" + currencyAddress + ";\"" + currency + "\";Table!" + projectAddress + ";\"" + project + "\")";
						sheetProject.getCell(rowPointer, columnPointer++).setFormula(totalValueEurFormula);
						String quantityFormula = "=SUMMER.HVIS.SETT(Table!" + quantityAddress + ";Table!" + currencyAddress + ";\"" + currency + "\";Table!" + projectAddress + ";\"" + project + "\")";
						sheetProject.getCell(rowPointer, columnPointer++).setFormula(quantityFormula);
						String valuePercentFormula = "=D" + (rowPointer + 1) + "*100/D" + totalRowIndex;
						sheetProject.getCell(rowPointer, columnPointer++).setFormula(valuePercentFormula);
						String itemPercentFormula = "=E" + (rowPointer + 1) + "*100/E" + totalRowIndex;
						sheetProject.getCell(rowPointer, columnPointer++).setFormula(itemPercentFormula);
					}
					rowPointer++;
					columnPointer = 0;
					sheetProject.getCell(rowPointer, columnPointer++).setValue(project);
					sheetProject.getCell(rowPointer, columnPointer++).setValue("Total:");
					sheetProject.getCell(rowPointer, columnPointer++).setFormula("=SUMMER(C" + (totalRowIndex - uniqueCurrencies + 1) + ":C" + (totalRowIndex - 1) + ")");
					sheetProject.getCell(rowPointer, columnPointer++).setFormula("=SUMMER(D" + (totalRowIndex - uniqueCurrencies + 1) + ":D" + (totalRowIndex - 1) + ")");
					sheetProject.getCell(rowPointer, columnPointer++).setFormula("=SUMMER(E" + (totalRowIndex - uniqueCurrencies + 1) + ":E" + (totalRowIndex - 1) + ")");

					rowPointer++;
					sheetProject.getCell(rowPointer, 0).setValue(project);
					sheetProject.getCell(rowPointer, 1).setValue("Delivered to FA:");

					rowPointer++;
					sheetProject.getCell(rowPointer, 0).setValue(project);
					sheetProject.getCell(rowPointer, 1).setValue("Improved Deliveries:");

					rowPointer++;
					sheetProject.getCell(rowPointer, 0).setValue(project);
					sheetProject.getCell(rowPointer, 1).setValue("Value of Improved Deliveries as FA:");

					rowPointer++;
					sheetProject.getCell(rowPointer, 0).setValue(project);
					sheetProject.getCell(rowPointer, 1).setValue("Acceleration Cost:");

					rowPointer++;
					sheetProject.getCell(rowPointer, 0).setValue(project);
					sheetProject.getCell(rowPointer, 1).setValue("Items Delivered Outside Scope:");
				}
				String lastCellAddress = sheetProject.getCell(rowPointer, 6).getAddress(false, false);
				sheetProject.getRange("A3", lastCellAddress).getBorders().setLineStyle(LineStyle.CONTINUOUS);

				/*
				 * Populates the "Delay"-sheet
				 */

				rowPointer = 1;
				int headerRow = rowPointer++;
				int delayRow = rowPointer++;
				int unitRow = rowPointer++;
				int itemRow = rowPointer++;
				int valueRow = rowPointer++;
				int accUnitRow = rowPointer++;
				int accItemRow = rowPointer++;
				int accValueRow = rowPointer++;

				int columnPointer = 0;

				//TODO: Add a percentage outside scope column
				
				sheetDelay.getCell(unitRow, columnPointer).setValue("No of Units");
				sheetDelay.getCell(itemRow, columnPointer).setValue("No of Items");
				sheetDelay.getCell(valueRow, columnPointer).setValue("Value [EUR]");
				sheetDelay.getCell(accUnitRow, columnPointer).setValue("Accumulated No of Units [%]");
				sheetDelay.getCell(accItemRow, columnPointer).setValue("Accumulated No of Items [%]");
				sheetDelay.getCell(accValueRow, columnPointer++).setValue("Accumulated Value [%]");
				sheetDelay.getCell(headerRow, columnPointer).setValue("Total Value");
				
				String unitFormula = "=SUMMER(Table!" + quantityAddress + ")";
				String itemFormula = "=ANTALL(Table!" + quantityAddress + ")";
				String totalValueFormula = "=SUMMER(Table!" + totalValueEurAddress + ")";
				
				sheetDelay.getCell(unitRow, columnPointer).setFormula(unitFormula);
				sheetDelay.getCell(itemRow, columnPointer).setFormula(itemFormula);
				sheetDelay.getCell(valueRow, columnPointer).setFormula(totalValueFormula);
				
				rowPointer = 2;
				String endCell = "AQ" + Integer.toString(rowPointer++);
				sheetDelay.getCell(headerRow, 2).setValue("Delay");
				sheetDelay.getRange("C2", endCell).fillRight();
				sheetDelay.getRange("B2", endCell).getInterior().setColor(Color.yellow);
				
				endCell = "AQ" + Integer.toString(rowPointer++); 
				sheetDelay.getCell(delayRow, 2).setValue(-20);
				sheetDelay.getCell(delayRow, 3).setFormula("=C3 + 1");
				sheetDelay.getRange("D3", endCell).fillRight(); 
				
				endCell = "AQ" + Integer.toString(rowPointer++); 
				sheetDelay.getCell(unitRow, 2).setFormula("=SUMMERHVIS(Table!" + roundedDelayAddressAbsolute + ";C3;Table!" + quantityAddressAbsolute +")" );
				sheetDelay.getRange("C4", endCell).fillRight();
				
				endCell = "AQ" + Integer.toString(rowPointer++); 
				sheetDelay.getCell(itemRow, 2).setFormula("=ANTALL.HVIS(Table!" + roundedDelayAddressAbsolute + ";C3" +")" );
				sheetDelay.getRange("C5", endCell).fillRight();
				
				endCell = "AQ" + Integer.toString(rowPointer++); 
				sheetDelay.getCell(valueRow, 2).setFormula("=SUMMERHVIS(Table!" + roundedDelayAddressAbsolute + ";C3;Table!" + totalValueEurAddressAbsolute +")" );
				sheetDelay.getRange("C6", endCell).fillRight();
				
				endCell = "AQ" + Integer.toString(rowPointer++);
				sheetDelay.getCell(accUnitRow, 2).setFormula("=C4/B4");
				sheetDelay.getCell(accUnitRow, 3).setFormula("=(D4/$B$4)+C7");
				sheetDelay.getRange("D7", endCell).fillRight();
				
				endCell = "AQ" + Integer.toString(rowPointer++);
				sheetDelay.getCell(accItemRow, 2).setFormula("=C5/B5");
				sheetDelay.getCell(accItemRow, 3).setFormula("=(D5/$B$5)+C8");
				sheetDelay.getRange("D8", endCell).fillRight();
				
				endCell = "AQ" + Integer.toString(rowPointer++);
				sheetDelay.getCell(accValueRow, 2).setFormula("=C6/B6");
				sheetDelay.getCell(accValueRow, 3).setFormula("=(D6/$B$6)+C9");
				sheetDelay.getRange("D9", endCell).fillRight();
				
				sheetDelay.getRange("B7", endCell).setNumberFormat("0,00 %");
				sheetDelay.getRange("A2", endCell).getBorders().setLineStyle(LineStyle.CONTINUOUS);
				
				ChartObject delayChartObject = sheetDelay.getChartObjects().add(100, 200, 1000, 250); // Charts exists within chart objects, which in turn sets the size and location
				Chart delayChart = delayChartObject.getChart();
				delayChart.setChartType(ChartType.LINE);
				delayChart.setSourceData(sheetDelay.getRange("C7", endCell));
				delayChart.getAxis(AxisType.CATEGORY).setHasMajorGridlines(true); // X-axis = Category, Y-axis = Value
				delayChart.getAxis(AxisType.CATEGORY).setCategoryNames(sheetDelay.getRange("C3:AQ3"));
				delayChart.getAxis(AxisType.CATEGORY).setAxisBetweenCategories(false);
				delayChart.getAxis(AxisType.CATEGORY).setTickLabelSpacing(2);
				delayChart.getSeries(0).setName("Accumulated No of Units");
				delayChart.getSeries(1).setName("Accumulated No of Items");
				delayChart.getSeries(2).setName("Accumulated Value");
				
				/*
				 * Populates the DelPerformance sheet
				 */
				
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
				endCell = "BC" + Integer.toString(rowPointer++);
				sheetDelPerformance.getRange("C2", endCell).fillRight();
				
				sheetDelPerformance.getCell(cddWeekRow, 1).setValue("Total");
				sheetDelPerformance.getCell(cddWeekRow, 2).setValue(0);
				sheetDelPerformance.getCell(cddWeekRow, 3).setFormula("=C3+1");
				endCell = "BC" + Integer.toString(rowPointer++);
				sheetDelPerformance.getRange("D3", endCell).fillRight();
				sheetDelPerformance.getRange("C2", endCell).getInterior().setColor(Color.yellow);
				
				sheetDelPerformance.getCell(cddItemRow, 0).setValue("No of Contractual Items to Deliver");
				sheetDelPerformance.getCell(cddItemRow, 1).setFormula("=ANTALL(Table!" + quantityAddress + ")");
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
				sheetDelPerformance.getRange("C8", endCell).getInterior().setColor(Color.yellow);
				
				sheetDelPerformance.getCell(rfiItemRow, 0).setValue("No of Actual Items Delivered");
				sheetDelPerformance.getCell(rfiItemRow, 1).setFormula("=ANTALL(Table!" + quantityAddress + ")");
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
				
				Range performanceChartSourceData = excel.union(sheetDelPerformance.getRange("C5","BC5"), sheetDelPerformance.getRange("C11","BC11"));
				ChartObject delPerformanceChartObject = sheetDelPerformance.getChartObjects().add(50, 250, 500, 250);
				Chart delPerformanceChart = delPerformanceChartObject.getChart();
				delPerformanceChart.setChartType(ChartType.LINE);
				delPerformanceChart.setSourceData(performanceChartSourceData);
				delPerformanceChart.getAxis(AxisType.CATEGORY).setHasMajorGridlines(true); // X-axis = Category, Y-axis = Value
				delPerformanceChart.getAxis(AxisType.CATEGORY).setCategoryNames(sheetDelPerformance.getRange("C3:BC3"));
				delPerformanceChart.getAxis(AxisType.CATEGORY).setAxisBetweenCategories(false);
				delPerformanceChart.getAxis(AxisType.CATEGORY).setTickLabelSpacing(2);
				delPerformanceChart.getSeries(0).setName("Contractual Items");
				delPerformanceChart.getSeries(1).setName("Delivered Items");
				
				/*
				 * Populates the DelMill sheet
				 */
				rowPointer = 1;
				
				for(String mill : millSet){
					
					headerRow = rowPointer++;
					delayRow = rowPointer++;
					unitRow = rowPointer++;
					itemRow = rowPointer++;
					valueRow = rowPointer++;
					accUnitRow = rowPointer++;
					accItemRow = rowPointer++;
					accValueRow = rowPointer++;

					columnPointer = 0;

					//TODO: Add a percentage outside scope column
					
					if(mill.length()==0) mill = "EMPTY";
					sheetDelMill.getCell(headerRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(delayRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(unitRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(itemRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(valueRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(accUnitRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(accItemRow, columnPointer).setValue(mill);
					sheetDelMill.getCell(accValueRow, columnPointer++).setValue(mill);
					
					sheetDelMill.getCell(unitRow, columnPointer).setValue("No of Units");
					sheetDelMill.getCell(itemRow, columnPointer).setValue("No of Items");
					sheetDelMill.getCell(valueRow, columnPointer).setValue("Value [EUR]");
					sheetDelMill.getCell(accUnitRow, columnPointer).setValue("Accumulated No of Units [%]");
					sheetDelMill.getCell(accItemRow, columnPointer).setValue("Accumulated No of Items [%]");
					sheetDelMill.getCell(accValueRow, columnPointer++).setValue("Accumulated Value [%]");
					sheetDelMill.getCell(headerRow, columnPointer).setValue("Total Value");
					
					sheetDelMill.getCell(unitRow, columnPointer).setFormula(unitFormula);
					sheetDelMill.getCell(itemRow, columnPointer).setFormula(itemFormula);
					sheetDelMill.getCell(valueRow, columnPointer).setFormula(totalValueFormula);
					
					// TODO: MONDAY! Columns incremented "getCell(headerRow, 3)" the value to the right. Now switch all cases for "D3" to "D"+row and be prepared to fix errors
					
					endCell = "AQ" + Integer.toString(headerRow);
					sheetDelMill.getCell(headerRow, 3).setValue("Delay");
					sheetDelMill.getRange("D" + Integer.toString(headerRow), endCell).fillRight();
					sheetDelMill.getRange("C" + Integer.toString(headerRow), endCell).getInterior().setColor(Color.yellow);
					
					endCell = "AQ" + Integer.toString(delayRow); 
					sheetDelMill.getCell(delayRow, 3).setValue(-20);
					sheetDelMill.getCell(delayRow, 4).setFormula("=C" + Integer.toString(delayRow) + "1");
					sheetDelMill.getRange("D3", endCell).fillRight(); 
					
					endCell = "AQ" + Integer.toString(unitRow); 
					sheetDelMill.getCell(unitRow, 3).setFormula("=SUMMERHVIS(Table!" + roundedDelayAddressAbsolute + ";C" + Integer.toString(delayRow) + ";Table!" + quantityAddressAbsolute +")" );
					sheetDelMill.getRange("C4", endCell).fillRight();
					
					endCell = "AQ" + Integer.toString(itemRow); 
					sheetDelMill.getCell(itemRow, 3).setFormula("=ANTALL.HVIS(Table!" + roundedDelayAddressAbsolute + ";C" + Integer.toString(delayRow) +")" );
					sheetDelMill.getRange("C5", endCell).fillRight();
					
					endCell = "AQ" + Integer.toString(valueRow); 
					sheetDelMill.getCell(valueRow, 3).setFormula("=SUMMERHVIS(Table!" + roundedDelayAddressAbsolute + ";C" + Integer.toString(delayRow) + ";Table!" + totalValueEurAddressAbsolute +")" );
					sheetDelMill.getRange("C6", endCell).fillRight();
					
					endCell = "AQ" + Integer.toString(accUnitRow);
					sheetDelMill.getCell(accUnitRow, 3).setFormula("=C4/B4");
					sheetDelMill.getCell(accUnitRow, 4).setFormula("=(D4/$B$4)+C7");
					sheetDelMill.getRange("D7", endCell).fillRight();
					
					endCell = "AQ" + Integer.toString(accItemRow);
					sheetDelMill.getCell(accItemRow, 3).setFormula("=C5/B5");
					sheetDelMill.getCell(accItemRow, 4).setFormula("=(D5/$B$5)+C8");
					sheetDelMill.getRange("D8", endCell).fillRight();
					
					endCell = "AQ" + Integer.toString(accValueRow);
					sheetDelMill.getCell(accValueRow, 3).setFormula("=C6/B6");
					sheetDelMill.getCell(accValueRow, 4).setFormula("=(D6/$B$6)+C9");
					sheetDelMill.getRange("D9", endCell).fillRight();
					
					sheetDelMill.getRange("B7", endCell).setNumberFormat("0,00 %");
					sheetDelMill.getRange("A2", endCell).getBorders().setLineStyle(LineStyle.CONTINUOUS);
					
					//TODO: Name x-axis "[Weeks]"
					
//					ChartObject delMillChartObject = sheetDelay.getChartObjects().add(100, 200, 1000, 250); // Charts exists within chart objects, which in turn sets the size and location
//					Chart delMillChart = delMillChartObject.getChart();
//					delMillChart.setChartType(ChartType.LINE);
//					delMillChart.setSourceData(sheetDelay.getRange("C7", endCell));
//					delMillChart.getAxis(AxisType.CATEGORY).setHasMajorGridlines(true); // X-axis = Category, Y-axis = Value
//					delMillChart.getAxis(AxisType.CATEGORY).setCategoryNames(sheetDelay.getRange("C3:AQ3"));
//					delMillChart.getAxis(AxisType.CATEGORY).setAxisBetweenCategories(false);
//					delMillChart.getAxis(AxisType.CATEGORY).setTickLabelSpacing(2);
//					delMillChart.getSeries(0).setName("Accumulated No of Units");
//					delMillChart.getSeries(1).setName("Accumulated No of Items");
//					delMillChart.getSeries(2).setName("Accumulated Value");
					rowPointer+=10;
				}
				
				//				
				//				
				//				// Create a column for each unique rounded delay value
				//				
				//				
				//				/*
				//				 * Creates and populates the "Mill"-sheet
				//				 */
				////				sheetMill.createRow(2).createCell(0).setCellValue("Mill");
				////				sheetMill.createRow(3).createCell(0).setCellValue("Name:");
				////				sheetMill.createRow(4).createCell(0).setCellValue("Average Required Delivery [Weeks]:");
				////				sheetMill.createRow(5).createCell(0).setCellValue("Average Actual Delivery > CDD [Weeks]:");
				////				sheetMill.createRow(6).createCell(0).setCellValue("Average Actual Delivery [Weeks]:");
				////				sheetMill.createRow(7).createCell(0).setCellValue("No of Units:");
				////				sheetMill.createRow(8).createCell(0).setCellValue("No of Items:");
				////				sheetMill.createRow(9).createCell(0).setCellValue("Value:");
				////
				////				setProgress(0);
				////				processed = 0;
				////				int columnCount = customerSet.size();
				////
				////				int column = 2;
				////				for(String customer : customerSet){
				////
				////					sheetMill.getRow(2).createCell(column).setCellValue("Mill");
				////					sheetMill.getRow(3).createCell(column).setCellValue(customer);
				////					sheetMill.getRow(4).createCell(column).setCellFormula("AVERAGEIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$Y$2:$Y$" + lastRow + ")");
				////					sheetMill.getRow(5).createCell(column).setCellFormula("AVERAGEIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$W$2:$W$" + lastRow + ")");
				////					sheetMill.getRow(6).createCell(column).setCellFormula("AVERAGEIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$X$2:$X$" + lastRow + ")");
				////					sheetMill.getRow(7).createCell(column).setCellFormula("SUMIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$K$2:$K$" + lastRow + ")");
				////					sheetMill.getRow(8).createCell(column).setCellFormula("COUNTIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\""  + ")");
				////					sheetMill.getRow(9).createCell(column).setCellFormula("SUMIF(Table!$B$2:$B$" + lastRow + ",\"" + customer + "\","  + "Table!$U$2:$U$" + lastRow + ")");
				////
				////					column++;
				////
				////					setProgress(100 * ++processed / columnCount);
				////					progressField.setText("Creating Mill Graph: " + processed);
				////				}
				////
				////				ExcelHelper.autoSizeColumns(sheetMill);
				//
				//				// TODO: Create a helper class that takes in a sheet and sets font size in all cells
				//
				////				ExcelHelper.autoSizeColumns(sheetDelay);
				////				ExcelHelper.autoSizeColumns(sheetProject);

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

	private StringBuilder generateQuery(boolean allCustSelected,
			boolean allProjSelected, boolean allStatSelected,
			List<List> temp_cust, List<List> temp_proj, List<List> temp_stat) {

		/*
		 * The base statement is used no matter the user selection
		 */
		StringBuilder query = new StringBuilder(5000);
		String basicStatement = "select \"Project\" = Project.pr_name,"
				+ " \"Client\" = customerList.assoc_name,"
				+ " \"Client_Ref.\" = Tr_hdr.assoc_ref,"
				+ " \"Order_Nr.\" = Tr_hdr.tr_no, "
				+ " \"Order_Registration_Date\" = convert(varchar(20), Tr_hdr.reg_date, 104),"		   
				+ " \"Item_nr.\" = clientItemList.item_no,"
				+ " \"Client_Art_code\" = clientItemList.local_id,"
				+ " \"Vendor_nr.\" = clientItemList.vnd_no, "
				+ " \"Description\" = clientItemList.description,"
				+ " \"Supplier\" = supplierList.assoc_name ,"
				+ " \"QTY\" = clientItemList.qnt,"
				+ " \"Unit_Price\" = clientItemList.price,"
				+ " \"currency\" = Exchange.curr_name,"
				+ " \"CDD\" = convert(varchar(20), clientItemList.contract_date, 104),"
				+ " \"EDD\" = convert(varchar(20), clientItemList.estimate_date, 104),"
				+ " \"RFI\" = convert(varchar(20), clientItemList.rfi_date, 104)," 
				+ " \"CCD\" = convert(varchar(20), supplierItemList.contract_date, 104),"
				+ " \"ECD\" = convert(varchar(20), supplierItemList.estimate_date, 104),"
				+ " \"Item_Status\" = Tr_dtl_status.tr_dtl_stname,"
				+ " \"Total_Price\" = clientItemList.qnt*clientItemList.price"                     //TODO: This should be an excel formula in case of manual changes
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
