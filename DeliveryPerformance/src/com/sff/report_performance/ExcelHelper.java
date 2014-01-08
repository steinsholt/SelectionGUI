package com.sff.report_performance;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHelper {

	// Iterates over all cells to find the number of columns
	public static void autoSizeColumns(XSSFSheet sheet){

		int currentRow = sheet.getFirstRowNum();
		int lastRow = sheet.getLastRowNum();
		int lastColumn = 0;

		while(currentRow < lastRow){
			XSSFRow row = sheet.getRow(currentRow);
			if(row != null) {
				int lastCell = row.getLastCellNum();
				if(lastCell > lastColumn) lastColumn = lastCell;
			}
			currentRow++;
		}

		for(int column = 0; column < lastColumn; column++){
			sheet.autoSizeColumn(column);
		}
	}

	public static void setRowStyle(XSSFRow row, CellStyle style, XSSFWorkbook workbook){
		int lastCell = row.getLastCellNum();
		for(int cell = 0; cell < lastCell; cell++){
			row.getCell(cell).setCellStyle(style);
		}
	}

	public static String formulaBuilder(XSSFSheet sheet, String range, String condition, String sumRange, IfFormula type){
		int lastRow = sheet.getLastRowNum() + 1;
		String sheetName = sheet.getSheetName();
		String formula = "";

		switch (type){
		case AVERAGEIF: 
			formula = "AVERAGEIF(" + sheetName + "!$" + range + "$2:$" + range + "$" + lastRow + ",\""+ condition + "\"," 
					+ sheetName + "!$" + sumRange + "$2:$" + sumRange + "$" + lastRow + ")"; 
			break;

		case COUNTIF:
			formula = "COUNTIF(" + sheetName + "!$" + range + "$2:$" + range + "$" + lastRow + ",\""+ condition + "\"" + ")";
			break;

		case SUMIF:
			formula = "SUMIF(" + sheetName + "!$" + range + "$2:$" + range + "$" + lastRow + ",\""+ condition + "\"," 
					+ sheetName + "!$" + sumRange + "$2:$" + sumRange + "$" + lastRow + ")";
			break;
		}

		return formula;
	}

	public static String excelSumIfs(XSSFSheet sheet, String sumRange, String criteriaRange1, String criteria1, String criteriaRange2, String criteria2){
		int lastRow = sheet.getLastRowNum() + 1;
		String sheetName = sheet.getSheetName();
		return "SUMIFS(" + sheetName + "!$" + sumRange + "$2:$" + sumRange + "$" + lastRow + "," + sheetName + "!$" + criteriaRange1 + "$2:$" + criteriaRange1 + "$" + lastRow + ",\""+ criteria1 + "\"," +
			sheetName + "!$" + criteriaRange2 + "$2:$" + criteriaRange2 + "$" + lastRow + ",\""+ criteria2 + "\""  + ")";
	}

	// IF($Q$44="", 0, IF($O$44="", 0, ($Q$44-$O$44)/7))
	public static String excelSubtractAndDivide(String column1, String column2, int row, int divide){
		return "IF($" + column1 + "$" + row + "=\"\",0,IF($" + column2 + "$" + row + "=\"\",0,"  + "($" + column1 + "$" + row + "-$" + column2 + "$" + row + ")/" + divide + "))";
	}

	public static String excelRound(String column, int row, int decimals){
		return "ROUND($" + column + "$" + row + "," + decimals + ")";
	}

	public static String excelSubtractDivideAndRound(String column1, String column2, int row, int divide, int decimals){
		return "IF($" + column1 + "$" + row + "=\"\",0,IF($" + column2 + "$" + row + "=\"\",0," + "(ROUND((($" + column1 + "$" + row + "-$" + column2 + "$" + row + ")/" + divide + ")," + decimals + "))))";
	}

	public static HashMap<String, String> createExcelReferenceList(XSSFSheet sheet){
		HashMap<String, String> excelReferencesMap = new HashMap<String, String>();
		for(Cell c : sheet.getRow(0)){
			excelReferencesMap.put(c.getStringCellValue(), CellReference.convertNumToColString(c.getColumnIndex()));
		}
		return excelReferencesMap;
	}

	public enum IfFormula{
		SUMIF, AVERAGEIF, COUNTIF;
	}

}
