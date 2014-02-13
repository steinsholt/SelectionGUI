package com.sff.report_performance;

import java.awt.Color;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.moyosoft.connector.ms.excel.ListObject;
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

			// TODO: Iterate over the worksheets and remove Ark1, Ark2 and Ark3
			//			for(Object worksheet : workbook.getWorksheets()){
			//				if(!(((Worksheet) worksheet)).getName().equals("Exchange")) (((Worksheet) worksheet)).delete();
			//			}

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
			dataSet.setQuery(new QueryDescriptor(db, "exec hbs.dbo.DeliveryPerformanceReport '" + query.toString() + "'"));
			dataSet.open();

			int rowCount = dataSet.getRowCount();
			int processed = 0;
			publishedOutput.setText("Creating Table Sheet");

			while(!isCancelled()){

				String s;
				Double d;
				int i;
				Timestamp time;
				Range cell = sheetTable.getRange("A1");
				Set<String> projectSet = new HashSet<String>();
				Set<String> millSet = new HashSet<String>();
				Set<String> currencySet = new HashSet<String>();
				while(dataSet.inBounds() && !isCancelled()){ //TODO: If canceled, do not open excel

					setProgress(100 * processed++ / rowCount);
					progressField.setText("Adding row: " + processed);
					for(int column = 0; column < dataSet.getColumnCount(); column++){
						cell = sheetTable.getCell(dataSet.getRow(), column);
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Supplier"))){
							millSet.add(dataSet.getString(column).trim());
						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("Project"))){
							projectSet.add(dataSet.getString(column).trim());
						}
						if(dataSet.getColumn(column).equals(dataSet.getColumn("currency"))){
							currencySet.add(dataSet.getString(column).trim());
						}
						try{ 
							//TODO: Inserting into range takes a long time, try to use arrays
							s = dataSet.getString(column);

							if(s.length()>0)cell.setValue(s.trim());
							else cell.setValue(" ");
						}catch(VariantException e){
							try{
								d = dataSet.getDouble(column);
								cell.setValue(d);
							}catch(VariantException v){
								try{
									i = dataSet.getInt(column);
									cell.setValue(i);
								}catch(VariantException a){
									time = dataSet.getTimestamp(column);
									cell.setValue(time);
								}
							}
						}
					}
					dataSet.next();
				}
				
				//TODO: Solution to the slow load times
//				double[][] m = new double[10][1];
//
//				for(int k=0; k<10; k++) {
//					for(int j=0; j<1; j++) {
//						m[k][j] = k*j;
//					}
//				}
//				long startTime = System.nanoTime();
//				sheetDelPerformance.getRange("A40:J50").setValues(m);
//
//				long endTime = System.nanoTime();
//				long duration = endTime - startTime;
//				System.out.println(duration);
				
				String[] columnNames = dataSet.getColumnNames(dataSet.getColumnCount()); 
				dataSet.close();

				String firstHeaderCell = "A1";
				String currentHeaderCell = firstHeaderCell;
				String formulaStartCell = "A2";
				String currentEndCell = "";

				/*
				 * Creates header row in the "Table"-sheet
				 */

				for(String column : columnNames){
					String headerName = column.replaceAll("\\s+", "");
					sheetTable.getRange(currentHeaderCell).setValue(headerName);
					String end = sheetTable.getRange(currentHeaderCell).getEntireColumn().getEnd(Direction.DOWN).getAddress();
					sheetTable.getRange(currentHeaderCell, end).setName(headerName);
					currentHeaderCell = sheetTable.getRange(headerName).getNext().getAddress();
					currentEndCell = sheetTable.getRange(end).getNext().getAddress();
					formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				}

				String totalValueRangeAddress = sheetTable.getRange("TotalPrice").getAddress(false, false);
				String currencyRangeAddress = sheetTable.getRange("currency").getAddress(false, false);
				String projectRangeAddress = sheetTable.getRange("Project").getAddress(false, false);
				String quantityRangeAddress = sheetTable.getRange("QTY").getAddress(false, false);
				String quantityRangeAddressAbsolute = sheetTable.getRange("QTY").getAddress(true, true);
				String supplierRangeAddressAbsolute = sheetTable.getRange("Supplier").getAddress(true, true);

				/*
				 * Converts total value into value[eur]
				 */

				// TODO: Language preference
				// TODO: Progress bar update!
				// TODO: get address from columns instead of hard coded values

				String startCell = formulaStartCell;
				sheetTable.getRange(currentHeaderCell, currentEndCell).setName("Total_EUR");
				sheetTable.getRange(currentHeaderCell).setValue("Total [EUR]");
				sheetTable.getRange(formulaStartCell).setFormula("=AVRUND(HVIS(Q2=\"EUR\";P2;(P2/8)*R2);0)");
				//TODO: Move to separate method
				currentEndCell = sheetTable.getRange(currentEndCell).getNext().getAddress();
				formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				currentHeaderCell = sheetTable.getRange(currentHeaderCell).getNext().getAddress();
				//				sheetTable.getRange(topIndex).setFormula("=AVRUND(FINN.RAD(M2; Exchange!$A$2:$B$16;2; USANN) / FINN.RAD(\"EUR\"; Exchange!$A$2:$B$16;2; USANN) * T2;0)");

				/*
				 *  Creates the delay part of the "Table"-sheet.
				 */

				sheetTable.getRange(currentHeaderCell, currentEndCell).setName("RFICDD");
				sheetTable.getRange(currentHeaderCell).setValue("RFI-CDD");
				sheetTable.getRange(formulaStartCell).setFormula("=HVIS(U2=\" \"; 0; HVIS(S2=\" \"; 0; ((U2-S2)/7)))"); 
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
				sheetTable.getRange(formulaStartCell).setFormula("=HVIS(U2=\" \"; 0; HVIS(G2=\" \"; 0; AVRUND(((U2-G2)/7);0)))");
				currentEndCell = sheetTable.getRange(currentEndCell).getNext().getAddress();
				formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				currentHeaderCell = sheetTable.getRange(currentHeaderCell).getNext().getAddress();

				sheetTable.getRange(currentHeaderCell, currentEndCell).setName("CDDOrderdate");
				sheetTable.getRange(currentHeaderCell).setValue("CDD-Order date");
				sheetTable.getRange(formulaStartCell).setFormula("=HVIS(S2=\" \"; 0; HVIS(G2=\" \"; 0; AVRUND(((S2-G2)/7);0)))");
				Range yellow = sheetTable.getRange(startCell, currentEndCell);
				yellow.fillDown(); 
				//				yellow.getBorders().setLineStyle(LineStyle.CONTINUOUS);
				//				yellow.getInterior().setColor(Color.yellow);

				currentEndCell = sheetTable.getRange(currentEndCell).getNext().getAddress();
				formulaStartCell = sheetTable.getRange(formulaStartCell).getNext().getAddress();
				currentHeaderCell = sheetTable.getRange(currentHeaderCell).getNext().getAddress();

				sheetTable.getRange("TotalPrice").setNumberFormat("0");
				sheetTable.getRange("UnitPrice").setNumberFormat("0");
				sheetTable.getRange("Vendornr.").setNumberFormat("000000");

				sheetTable.getRange(currentHeaderCell).setValue("Error Rows");
				sheetTable.getRange(formulaStartCell).setFormula("=HVISFEIL(HVIS(FINN.KOLONNE(\" \";A2:AC2;1;USANN)=\" \";\"** ERROR **\";\"\");\"\")");
				Range red = sheetTable.getRange(formulaStartCell, currentEndCell);
				red.fillDown();
				red.getInterior().setColor(Color.red);
				red.getBorders().setLineStyle(LineStyle.CONTINUOUS);

				sheetTable.getListObjects().add(); // Creates the excel table

				//				String endRight = sheetTable.getRange("A1").getEntireRow().getEnd(Direction.TO_RIGHT).getAddress(false, false);
				//				sheetTable.getRange("A1", endRight).getInterior().setColor(Color.yellow);

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

				// TODO: Do this properly with range names, getNext() etc
				excel.getWorksheets().getItem("Project").activate();
				String firstCell = "A1";
				sheetProject.getRange(firstCell).activate();

				for(String project : projectSet){
					progressField.setText("Processing project: " + processed);
					setProgress(100 * processed++ / projectSet.size());

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

					// TODO: Name start and end ranges and use proper names
					for(String currency : currencySet){
						excel.getActiveCell().getOffset(1).activate();
						Range currentCell = excel.getActiveCell();
						currentCell.setValue(project);
						currentCell = currentCell.getNext();
						currentCell.setValue("Total Value [" + currency + "]:");
						currentCell = currentCell.getNext();
						String totalValueFormula = "=SUMMER.HVIS.SETT(Table!" + totalValueRangeAddress + ";Table!" + currencyRangeAddress + ";\"" + currency + "\";Table!" + projectRangeAddress + ";\"" + project + "\")";
						currentCell.setNumberFormat("# #0"); // Thousand separator with 0 decimals
						currentCell.setFormula(totalValueFormula);
						currentCell = currentCell.getNext();
						String totalValueEurFormula = "=SUMMER.HVIS.SETT(Table!" + totalValueEurAddress + ";Table!" + currencyRangeAddress + ";\"" + currency + "\";Table!" + projectRangeAddress + ";\"" + project + "\")";
						currentCell.setNumberFormat("# #0");
						currentCell.setFormula(totalValueEurFormula);
						currentCell = currentCell.getNext();
						String quantityFormula = "=SUMMER.HVIS.SETT(Table!" + quantityRangeAddress + ";Table!" + currencyRangeAddress + ";\"" + currency + "\";Table!" + projectRangeAddress + ";\"" + project + "\")";
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

					currentCell.setValue(project);
					currentCell = currentCell.getNext();
					currentCell.setValue("Total:");
					currentCell = currentCell.getOffset(0, 2);
					currentCell.setFormula("=SUMMER(" + currentCell.getOffset(-currencySet.size()).getAddress() + ":" + currentCell.getOffset(-1).getAddress() + ")");
					currentCell = currentCell.getNext();
					currentCell.setFormula("=SUMMER(" + currentCell.getOffset(-currencySet.size()).getAddress() + ":" + currentCell.getOffset(-1).getAddress() + ")");
					excel.getActiveCell().getOffset(1).activate();

					currentCell = excel.getActiveCell();
					currentCell.setValue(project);
					currentCell = currentCell.getNext();
					currentCell.setValue("Delivered to FA:");
					excel.getActiveCell().getOffset(1).activate();

					currentCell = excel.getActiveCell();
					currentCell.setValue(project);
					currentCell = currentCell.getNext();
					currentCell.setValue("Improved Deliveries:");
					excel.getActiveCell().getOffset(1).activate();

					currentCell = excel.getActiveCell();
					currentCell.setValue(project);
					currentCell = currentCell.getNext();
					currentCell.setValue("Value of Improved Deliveries as FA:");
					excel.getActiveCell().getOffset(1).activate();

					currentCell = excel.getActiveCell();
					currentCell.setValue(project);
					currentCell = currentCell.getNext();
					currentCell.setValue("Acceleration Cost:");
					excel.getActiveCell().getOffset(1).activate();

					currentCell = excel.getActiveCell();
					currentCell.setValue(project);
					currentCell = currentCell.getNext();
					currentCell.setValue("Items Delivered Outside Scope:");
					excel.getActiveCell().getOffset(1).activate();

				}
				sheetProject.getRange(firstCell, excel.getActiveCell().getAddress()).autoFilter(1);
				excel.getActiveCell().getOffset(-1, 6).activate();
				sheetProject.getRange(firstCell, excel.getActiveCell().getAddress()).getBorders().setLineStyle(LineStyle.CONTINUOUS);

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

				sheetDelay.getCell(unitRow, columnPointer).setValue("No of Units");
				sheetDelay.getCell(itemRow, columnPointer).setValue("No of Items");
				sheetDelay.getCell(valueRow, columnPointer).setValue("Value [EUR]");
				sheetDelay.getCell(accUnitRow, columnPointer).setValue("Accumulated No of Units [%]");
				sheetDelay.getCell(accItemRow, columnPointer).setValue("Accumulated No of Items [%]");
				sheetDelay.getCell(accValueRow, columnPointer++).setValue("Accumulated Value [%]");
				sheetDelay.getCell(headerRow, columnPointer).setValue("Total Value");

				String unitFormula = "=SUMMER(Table!" + quantityRangeAddress + ")";
				String itemFormula = "=ANTALL(Table!" + quantityRangeAddress + ")";
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
				sheetDelay.getCell(unitRow, 2).setFormula("=SUMMERHVIS(Table!" + roundedDelayAddressAbsolute + ";C3;Table!" + quantityRangeAddressAbsolute +")" );
				sheetDelay.getRange("C4", endCell).fillRight();

				endCell = "AQ" + Integer.toString(rowPointer++); 
				sheetDelay.getCell(itemRow, 2).setFormula("=ANTALL.HVIS(Table!" + roundedDelayAddressAbsolute + ";C3" +")" );
				sheetDelay.getRange("C5", endCell).fillRight();

				endCell = "AQ" + Integer.toString(rowPointer++); 
				sheetDelay.getCell(valueRow, 2).setFormula("=SUMMERHVIS(Table!" + roundedDelayAddressAbsolute + ";C3;Table!" + totalValueEurAddressAbsolute +")" );
				sheetDelay.getRange("C6", endCell).fillRight();

				endCell = "AQ" + Integer.toString(rowPointer++);
				sheetDelay.getCell(accUnitRow, 1).setFormula("=" + endCell);
				sheetDelay.getCell(accUnitRow, 2).setFormula("=C4/B4");
				sheetDelay.getCell(accUnitRow, 3).setFormula("=(D4/$B$4)+C7");
				sheetDelay.getRange("D7", endCell).fillRight();

				endCell = "AQ" + Integer.toString(rowPointer++);
				sheetDelay.getCell(accItemRow, 1).setFormula("=" + endCell);
				sheetDelay.getCell(accItemRow, 2).setFormula("=C5/B5");
				sheetDelay.getCell(accItemRow, 3).setFormula("=(D5/$B$5)+C8");
				sheetDelay.getRange("D8", endCell).fillRight();

				endCell = "AQ" + Integer.toString(rowPointer++);
				sheetDelay.getCell(accValueRow, 1).setFormula("=" + endCell);
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
				sheetDelPerformance.getRange("C8", endCell).getInterior().setColor(Color.yellow);

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

				for(String mill : millSet){

					headerRow = rowPointer++;
					delayRow = rowPointer++;
					unitRow = rowPointer++;
					itemRow = rowPointer++;
					valueRow = rowPointer++;
					accUnitRow = rowPointer++;
					accItemRow = rowPointer++;
					accValueRow = rowPointer++;

					columnPointer = 7;

					//TODO: Add a percentage outside scope column

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

					unitFormula = "=SUMMERHVIS(Table!" + supplierRangeAddressAbsolute + ";\"" +  mill + "\";Table!" + quantityRangeAddressAbsolute + ")";
					itemFormula = "=ANTALL.HVIS(Table!" + supplierRangeAddressAbsolute + ";\"" +  mill + "\")";
					totalValueFormula = "=SUMMERHVIS(Table!" + supplierRangeAddressAbsolute + ";\"" +  mill + "\";Table!" + totalValueEurAddressAbsolute + ")";

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
		//	query.append(basicStatement);


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
		if(!allStatSelected){
			query.append(" and Tr_dtl_status.tr_dtl_stname in (");
			for(List l : temp_stat){
				String status = (String) l.get(1);
				query.append("''" + status + "'', ");
			}
			query.delete(query.length()-2, query.length());
			query.append(")");
		}
		return query;
	}
}
