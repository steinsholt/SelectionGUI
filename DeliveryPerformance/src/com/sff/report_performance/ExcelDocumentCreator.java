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
import com.moyosoft.connector.ms.excel.AxisCrosses;
import com.moyosoft.connector.ms.excel.AxisType;
import com.moyosoft.connector.ms.excel.BorderWeight;
import com.moyosoft.connector.ms.excel.CategoryType;
import com.moyosoft.connector.ms.excel.Chart;
import com.moyosoft.connector.ms.excel.ChartLocation;
import com.moyosoft.connector.ms.excel.ChartObject;
import com.moyosoft.connector.ms.excel.ChartType;
import com.moyosoft.connector.ms.excel.Direction;
import com.moyosoft.connector.ms.excel.DisplayUnit;
import com.moyosoft.connector.ms.excel.Excel;
import com.moyosoft.connector.ms.excel.LineStyle;
import com.moyosoft.connector.ms.excel.Range;
import com.moyosoft.connector.ms.excel.ScaleType;
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
	private File output;
	private Worksheet sheetExchangeRate;
	private Worksheet sheetTable;
	private Worksheet sheetProject;
	private Worksheet sheetDelay;
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
		this.output = output;

		try {
			excel = new Excel();
			workbook = excel.getWorkbooks().add();

			sheetExchangeRate = new Worksheet(workbook);
			sheetExchangeRate.setName("Exchange");
			sheetProject = new Worksheet(workbook);
			sheetProject.setName("Project");
			sheetDelay = new Worksheet(workbook);
			sheetDelay.setName("Delay");
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

		excel.setVisible(true);
	}

	@Override //TODO: Does this not override something?
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

				/*
				 * Extracts data from the database and inserts into the "Table"-sheet.
				 */
				Set<String> currencySet = new HashSet<String>();
				Set<String> customerSet = new HashSet<String>();
				Set<String> projectSet = new HashSet<String>();
				while(dataSet.next()){
					setProgress(100 * processed++ / rowCount);
					progressField.setText("Adding row: " + processed);

					//TODO: Apache POI for loading data faster? Use query tables? sheet.getQueryTables.Add()
					for(int column = 0; column < dataSet.getColumnCount(); column++){
						Range cell = sheetTable.getCell(dataSet.getRow(), column);

						if(dataSet.getColumn(column).equals(dataSet.getColumn("currency"))){
							currencySet.add(dataSet.getString(column));
						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Client"))){
							customerSet.add(dataSet.getString(column));
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
				// Why does this fill down? Need only 1 fill down?

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
				//
				////				String currencyExcelReference = referenceMap.get("Currency");
				////				String totalValueExcelReference = referenceMap.get("Total Value");
				////				String projectExcelReference = referenceMap.get("Project");
				////				String quantityExcelReference = referenceMap.get("QTY");
				////				String delayExcelReference = referenceMap.get("Delay (RFI-CDD)");
				////				String RfiExcelReference = referenceMap.get("RFI");
				////				String CddExcelReference = referenceMap.get("CDD");
				////				
				////				CellStyle yellowBorderedStyle = workbook.createCellStyle();
				////				yellowBorderedStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				////				yellowBorderedStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
				////				yellowBorderedStyle.setBorderBottom(CellStyle.BORDER_THIN);
				////				yellowBorderedStyle.setBorderTop(CellStyle.BORDER_THIN);
				////				yellowBorderedStyle.setBorderRight(CellStyle.BORDER_THIN);
				////				yellowBorderedStyle.setBorderLeft(CellStyle.BORDER_THIN);
				//				
				//		
				
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

				int cellPointer = 0;

				//TODO: Add a percentage outside scope column
				
				sheetDelay.getCell(unitRow, cellPointer).setValue("No of Units");
				sheetDelay.getCell(itemRow, cellPointer).setValue("No of Items");
				sheetDelay.getCell(valueRow, cellPointer).setValue("Value [EUR]");
				sheetDelay.getCell(accUnitRow, cellPointer).setValue("Accumulated No of Units [%]");
				sheetDelay.getCell(accItemRow, cellPointer).setValue("Accumulated No of Items [%]");
				sheetDelay.getCell(accValueRow, cellPointer++).setValue("Accumulated Value [%]");
				sheetDelay.getCell(headerRow, cellPointer).setValue("Total Value");
				
				String unitFormula = "=SUMMER(Table!" + quantityAddress + ")";
				String itemFormula = "=ANTALL(Table!" + quantityAddress + ")";
				String totalValueFormula = "=SUMMER(Table!" + totalValueEurAddress + ")";
				
				sheetDelay.getCell(unitRow, cellPointer).setFormula(unitFormula);
				sheetDelay.getCell(itemRow, cellPointer).setFormula(itemFormula);
				sheetDelay.getCell(valueRow, cellPointer).setFormula(totalValueFormula);
				
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
				
				ChartObject delayChartObject = sheetDelay.getChartObjects().add(100, 200, 2000, 250);
				Chart delayChart = delayChartObject.getChart();
				delayChart.setChartType(ChartType.LINE);
				delayChart.setSourceData(sheetDelay.getRange("C7", endCell));
				delayChart.getAxis(AxisType.CATEGORY).setHasMajorGridlines(true); // X-axis = Category, Y-axis = Value
				delayChart.getAxis(AxisType.CATEGORY).setCategoryNames(sheetDelay.getRange("C3:AQ3"));
				delayChart.getAxis(AxisType.CATEGORY).setAxisBetweenCategories(false);
				delayChart.getSeries(0).setName("Accumulated No of Units");
				delayChart.getSeries(1).setName("Accumulated No of Items");
				delayChart.getSeries(2).setName("Accumulated Value");
				
				//				
				//				for(int row = -36; row <= 36; row++){ 
				//					sheetDelay.getCell(headerRow, cellPointer).setValue("Delay (RFI-CDD)");
				//					sheetDelay.getCell(delayRow, cellPointer).setValue(row);
				//					
				////					Cell unitCell = unitRow.createCell(cellPointer);
				////					unitCell.setCellFormula(ExcelHelper.formulaBuilder(sheetTable, delayExcelReference, Integer.toString(row), quantityExcelReference, IfFormula.SUMIF));
				////					currentUnitCellReference = new CellReference(unitCell);
				////					
				////					Cell itemCell = itemRow.createCell(cellPointer);
				////					itemCell.setCellFormula(ExcelHelper.formulaBuilder(sheetTable, delayExcelReference, Integer.toString(row), null, IfFormula.COUNTIF));
				////					currentItemCellReference = new CellReference(itemCell);
				////					
				////					Cell valueCell = valueRow.createCell(cellPointer);
				////					// TODO: Add sheet containing exchange rates!
				//////					valueCell.setCellFormula(ExcelHelper.formulaBuilder(sheetTable, delayExcelReference, Integer.toString(row), totalValueExcelReference, IfFormula.SUMIF));
				//////					currentValueCellReference = new CellReference(valueCell);
				////					
				////					Cell accUnitCell = accUnitRow.createCell(cellPointer);
				////					accUnitCell.setCellFormula("(" + currentUnitCellReference.formatAsString() + "/B4)+" + previousAccUnitCellReference.formatAsString());
				////					previousAccUnitCellReference = new CellReference(accUnitCell);
				////					
				////					Cell accItemCell = accItemRow.createCell(cellPointer);
				////					accItemCell.setCellFormula("(" + currentItemCellReference.formatAsString() + "/B5)+" + previousAccItemCellReference.formatAsString());
				////					previousAccItemCellReference = new CellReference(accItemCell);
				////					
				////					Cell accValueCell = accValueRow.createCell(cellPointer);
				////					accValueCell.setCellFormula("(" + currentValueCellReference.formatAsString() + "/B6)+" + previousAccValueCellReference.formatAsString());
				////					previousAccValueCellReference = new CellReference(accValueCell);
				//					
				//					cellPointer++;
				//				}
				//				
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
