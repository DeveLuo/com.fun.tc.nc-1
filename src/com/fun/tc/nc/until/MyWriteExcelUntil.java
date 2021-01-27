package com.fun.tc.nc.until;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCSession;

public class MyWriteExcelUntil {

	public static String writeToolingCatalogueExcel(List<String[]>values) throws Exception {
		
		String path = "C:\\Temp\\工装明细表.xlsx";
		
		String temp_path = getTemplatePath("@@工装明细表模板@@.xlsx");
		if (temp_path.isEmpty()) {
			throw new Exception("@@工装明细表模板@@.xlsx未配置");
		}
		
		InputStream in = new FileInputStream(temp_path);
		
		Workbook wb = new XSSFWorkbook(in);		
		XSSFSheet sheet = (XSSFSheet) wb.getSheet("工装明细表");
	    for (int i = 0; i < values.size(); i++) {
	    	XSSFRow row = sheet.createRow(i+3);
	    	String[] value = values.get(i);
	    	for (int j = 0; j < value.length; j++) {
	    		XSSFCell cell = row.getCell(j);
	    		if (cell == null) {
	    			cell = row.createCell(j);
				}
			    cell.setCellValue(value[j]);
			}
		}
	    //将数据流程保存到excel
	    FileOutputStream fileOut = new FileOutputStream(path);
		wb.write(fileOut);
		in.close();
		fileOut.close();
		return path;
	}
	
	public static String getTemplatePath(String dataset_name) throws Exception {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		String path = "";
		TCComponentDatasetType type =  (TCComponentDatasetType) session.getTypeComponent("Dataset");
		TCComponentDataset dataset = type.find(dataset_name);
		if (dataset == null) {
			return path;
		}
		File file = RacDatasetUtil.getFile(dataset);
		if (file == null) {
			return path;
		}
		return file.getAbsolutePath();
	}
	public static String writeCNCProgramCatalogueExcel(List<String[]>values) throws Exception {
		
		String path = "C:\\Temp\\数控程序确认表.xlsx";
		
		String temp_path = getTemplatePath("@@数控程序确认表模板@@.xlsx");
		if (temp_path.isEmpty()) {
			throw new Exception("@@数控程序确认表模板@@.xlsx未配置");
		}
		
		InputStream in = new FileInputStream(temp_path);
		Workbook wb = new XSSFWorkbook(in);		
		XSSFSheet sheet = (XSSFSheet) wb.getSheet("数控程序确认表");
		for (int i = 0; i < values.size(); i++) {
	    	XSSFRow row = sheet.createRow(i+3);
	    	String[] value = values.get(i);
	    	for (int j = 0; j < value.length; j++) {
	    		XSSFCell cell = row.getCell(j);
	    		if (cell == null) {
	    			cell = row.createCell(j);
				}
			    cell.setCellValue(value[j]);
			}
		}
	    //将数据流程保存到excel
		FileOutputStream fileOut = new FileOutputStream(path);
		wb.write(fileOut);
		in.close();
		fileOut.close();	
		return path;
	}
	
	/**
	 * 将路径下文件上传至NewStuff
	 * @param path
	 * @throws Exception
	 */
	public static void addToNewStuff(String path) throws Exception {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		TCComponentFolder folder = session.getUser().getNewStuffFolder();
		File files = new File(path);
		String name = files.getName();
		MyDatasetUtil.createDateset(folder, name, files, "contents");
	}
	
}
