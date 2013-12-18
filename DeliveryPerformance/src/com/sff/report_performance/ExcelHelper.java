package com.sff.report_performance;

import org.apache.poi.xssf.usermodel.XSSFSheet;

public class ExcelHelper {
	
	public static void autoSizeColumns(XSSFSheet sheet){
		
		int firstRow = sheet.getFirstRowNum();
		int lastRow = sheet.getLastRowNum();
		int lastColumn = 0;
		
		while(firstRow < lastRow){
			int lastCell = sheet.getRow(firstRow).getLastCellNum();
			if(lastCell > lastColumn) lastColumn = lastCell;
			firstRow++;
		}
		
		for(int column = 0; column < lastColumn; column++){
			sheet.autoSizeColumn(column);
		}
	}
}
