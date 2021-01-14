package com.fun.tc.nc.until;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.motey.transformer.command.Signer;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class MyDatasetUtil {

	/**
	 * @Title: createDateset
	 * @Description: TODO(创建数据集，并弹出是否打开数据的对话框)
	 * @param @param revision
	 * @param @param file
	 * @param @throws TCException
	 * @param @throws IOException 参数
	 * @return void 返回类型
	 * @throws
	 */

	public static void createDateset(TCComponent tcc, String name, File file, String ref_name) throws Exception{
		String fileType = getFileType(file);
		String ref = getrefType(fileType);
		TCComponentDatasetType type = (TCComponentDatasetType) tcc.getSession().getTypeService().getTypeComponent("Dataset");
		TCComponentDataset dataset = type.create(name, "", fileType);
		String[] refs = new String[] { ref };
		String[] files = new String[] { file.getAbsolutePath() };
		dataset.setFiles(files, refs);
		if (ref_name != null && !ref_name.isEmpty()) {
			tcc.add(ref_name, dataset);
		}
	}
	
	public static TCComponentDataset createDateset(TCComponent tcc, String name, File file) throws Exception{
		String fileType = getFileType(file);
		String ref = getrefType(fileType);
		TCComponentDatasetType type = (TCComponentDatasetType) tcc.getSession().getTypeService().getTypeComponent("Dataset");
		TCComponentDataset dataset = type.create(name, "", fileType);
		String[] refs = new String[] { ref };
		String[] files = new String[] { file.getAbsolutePath() };
		dataset.setFiles(files, refs);
		return dataset;
	}
	
	public static void createDatesetByMENCMachining(TCComponent tcc, String name, File file) throws Exception {
		
		TCComponent activity = tcc.getRelatedComponent("root_activity");
		TCComponent program = activity.getRelatedComponent("contents");
		if (program == null) {
			String program_name = tcc.getProperty("item_id");
			TCSession session = tcc.getSession();
			program = RACCreateUtil.create(session, program_name, "MENCProgram", "MENCProgram", null);
			activity.add("contents", program);
		}
		TCComponent[] coms = program.getRelatedComponents("contents");
		boolean flag = false;
		TCComponent ds = null;
		// 当前数据集是否已存在
		for (TCComponent com : coms) {
			if (com instanceof TCComponentDataset) {
				if (name.equals(com.getProperty("object_name"))) {
					ds = com;
					flag = true;
					break;
				}
			}
		}
		// 没有相同名称数据集，直接创建
		if (!flag) {
			TCComponentDataset dataset = createDateset(tcc, name, file);
			program.add("contents", dataset);
			program.refresh();
			return;
		}
		int choice = JOptionPane.showConfirmDialog(AIFUtility.getActiveDesktop(), "上传的数据集( " + name + " )已存在，是否需要覆盖旧数据?", "提示", JOptionPane.YES_NO_OPTION);
		if (choice == 0) {
			TCComponentDataset dataset = createDateset(tcc, name, file);
			program.add("contents", dataset);
			program.remove("contents", ds);
			program.refresh();
		}
	}
	
	public static void createPDFDatesetByMENCMachining(TCComponent tcc, String name, File file) throws Exception {
		String ref_name = tcc.getDefaultPasteRelation();
		TCComponent[] coms = tcc.getRelatedComponents(ref_name);
		boolean flag = false;
		TCComponent ds = null;
		// 当前数据集是否已存在
		for (TCComponent com : coms) {
			if (com instanceof TCComponentDataset) {
				if (name.equals(com.getProperty("object_name"))) {
					ds = com;
					flag = true;
					break;
				}
			}
		}
		// 没有相同名称数据集，直接创建
		if (!flag) {
			TCComponentDataset dataset = createDateset(tcc, name, file);
			tcc.add(ref_name, dataset);
			tcc.refresh();
			return;
		}
		int choice = JOptionPane.showConfirmDialog(AIFUtility.getActiveDesktop(), "上传的数据集( " + name + " )已存在，是否需要覆盖旧数据?", "提示", JOptionPane.YES_NO_OPTION);
		if (choice == 0) {
			TCComponentDataset dataset = createDateset(tcc, name, file);
			tcc.add(ref_name, dataset);
			tcc.remove(ref_name, ds);
			tcc.refresh();
		}
	}
	
	public static List<TCComponentDataset> getDatesetByMENCMachining(TCComponent tcc) throws TCException{
		List<TCComponentDataset> datasets = new ArrayList<TCComponentDataset>();
		TCComponent activity = tcc.getRelatedComponent("root_activity");
		TCComponent[] programs = activity.getRelatedComponents("contents");
		if (programs != null && programs.length > 0) {
			for (TCComponent program : programs) {
				TCComponent[] coms = program.getRelatedComponents("contents");
				if (coms != null) {
					for (TCComponent com : coms) {
						String name = com.toString();
						if (com instanceof TCComponentDataset && (name.endsWith(".MPF") || name.endsWith(".mpf"))) {
							datasets.add((TCComponentDataset)com);
						}
					}
				}
			}
		}
		return datasets;
	}
	
	public static void sign(TCComponentDataset dataset, Map<String, String> values) throws Exception {
		String[] args = new String[3];
		TCFileUtil util = new TCFileUtil(dataset);
		args[0] = "-command=Signer";
		args[1] = "-doc=" + util.getFile();
		args[2] = "-info=" + getTextPath(values);
//		Main.main(args);
		new Signer().execute(args);
		util.updateFile();
	}
	
	public static String getTextPath(Map<String, String> values) throws IOException {
		File file = new File(System.getProperty("user.home") + File.separator + "info.txt");
		StringBuilder sb = new StringBuilder();
		for (String key : values.keySet()) {
			String value = values.get(key);
			if (key.isEmpty() || value.isEmpty()) {
				continue;
			}
			sb.append(key + "=" + value + "\n");
		}
		
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = new FileOutputStream(file);
			pw = new PrintWriter(fos);
			pw.write(sb.toString().toCharArray());
			pw.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
		return file.getAbsolutePath();
	}

	/**
	 * @Title: getrefType
	 * @Description: TODO(获取TC文件类型对应的关系类型)
	 * @param @param fileType
	 * @param @return
	 * @param @throws TCException 参数
	 * @return String 返回类型
	 * @throws
	 */

	public static String getrefType(String fileType) throws Exception {
		String refType = null;
		if (fileType.contains("MSExcel")) {
			refType = "excel";
		} else if (fileType.contains("MSWord")) {
			refType = "word";
		} else if (fileType.contains("MSPowerPoint")) {
			refType = "powerpoint";
		} else if (fileType.contains("Zip")) {
			refType = "ZIPFILE";
		} else if (fileType.contains("PDF")) {
			refType = "PDF_Reference";
		} else if (fileType.contains("JPEG")) {
			refType = "JPEG_Reference";
		} else if (fileType.contains("Text")) {
			refType = "Text";
		} else if (fileType.contains("SF8_DWG")) {
			refType = "SF8_DWG";
		} else if (fileType.contains("DXF")) {
			refType = "DXF";
		} else if (fileType.contains("SF8_CSV")) {
			refType = "SF8_CSV";
		} else if (fileType.contains("SF8_AP15")) {
			refType = "SF8_AP15";
		} else if (fileType.contains("SF8_MP4")) {
			refType = "SF8_MP4";
		} else if (fileType.contains("SF8_RAR")) {
			refType = "SF8_RAR";
		} else if (fileType.contains("SWDrw")) {
			refType = "DrwFile";
		} else if (fileType.contains("Image")) {
			refType = "Image";
		} else if (fileType.contains("SF8_WPS")) {
			refType = "SF8_WPS";
		} else if (fileType.contains("SF8_MWP")) {
			refType = "SF8_MWP";
		} else if (fileType.contains("SF8_EXB")) {
			refType = "SF8_EXB";
		}else if (fileType.contains("UGCAMPTP")) {
			refType = "Fnd0MPF";
		}
		

		if (refType == null) {
			throw new Exception("找不到引用类型");
		}
		return refType;
	}

	/**
	 * @Title: getFileType
	 * @Description: TODO(获取文件在TC对应的文件类型)
	 * @param @param file
	 * @param @return
	 * @param @throws TCException 参数
	 * @return String 返回类型
	 * @throws
	 */

	public static String getFileType(File file) throws Exception {
		String datesetType = null;
		if (file == null) {
			throw new TCException("找不到引用类型");
		}
		String fileName = file.getName();
		if (fileName.endsWith("xls")) {
			datesetType = "MSExcel";
		} else if (fileName.endsWith("xlsx")) {
			datesetType = "MSExcelX";
		} else if (fileName.endsWith("doc")) {
			datesetType = "MSWord";
		} else if (fileName.endsWith("docx")) {
			datesetType = "MSWordX";
		} else if (fileName.endsWith("ppt")) {
			datesetType = "MSPowerPoint";
		} else if (fileName.endsWith("pptx")) {
			datesetType = "MSPowerPointX";
		} else if (fileName.endsWith("zip")) {
			datesetType = "Zip";
		} else if (fileName.endsWith("pdf") || fileName.endsWith("PDF")) {
			datesetType = "PDF";
		} else if (fileName.endsWith("jpg")) {
			datesetType = "JPEG";
		} else if (fileName.endsWith("txt")) {
			datesetType = "Text";
		} else if (fileName.endsWith("dwg") || fileName.endsWith("DWG")) {
			datesetType = "SF8_DWG";
		} else if (fileName.endsWith("dxf")) {
			datesetType = "DXF";
		} else if (fileName.endsWith("rar")) {
			datesetType = "SF8_RAR";
		} else if (fileName.endsWith("mp4")) {
			datesetType = "SF8_MP4";
		} else if (fileName.endsWith("csv")) {
			datesetType = "SF8_CSV";
		} else if (fileName.endsWith("ap15")) {
			datesetType = "SF8_AP15";
		} else if (fileName.endsWith("SLDDRW")) {
			datesetType = "SWDrw";
		} else if (fileName.endsWith("png")) {
			datesetType = "Image";
		} else if (fileName.endsWith("wps")) {
			datesetType = "SF8_WPS";
		} else if (fileName.endsWith("mwp")) {
			datesetType = "SF8_MWP";
		} else if (fileName.endsWith("exb")) {
			datesetType = "SF8_EXB";
		} else if(fileName.endsWith("MPF") || fileName.endsWith("mpf")) {
			datesetType = "UGCAMPTP";
		} 
		
		if (datesetType == null) {
			throw new Exception("文件类型未定义");
		}
		return datesetType;
	}

}
