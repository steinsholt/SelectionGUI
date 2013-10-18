package hbs.sff.no;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelDocumentCreator {
	private XSSFWorkbook workbook;
	private String file;
	
	public ExcelDocumentCreator(){
		createWorkbook();
		file = "C:/Users/hbs/Excel documents/test.xlsx";
	}

	private void saveWorkbook() {
		try {
			FileOutputStream out = new FileOutputStream(new File(file));
			workbook.write(out);
			out.close();
			System.out.println("Test document saved");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createWorkbook() {
		workbook = new XSSFWorkbook();
		XSSFSheet sheetTable = workbook.createSheet("Table");
		XSSFSheet sheetProject = workbook.createSheet("Project");
	}
	
	public void createReport(List<List> customerData, 
			List<List> projectData, List<List> statusData){
		// TODO: use data to create sql query. insert results into sheets. save workbook
		
		
		
		
		saveWorkbook();
	}
}
