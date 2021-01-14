package com.fun.tc.nc.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.fun.tc.nc.until.MyDatasetUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class UploadingReportHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) {

		try {
			
			InterfaceAIFComponent aifcomp = AIFUtility.getCurrentApplication().getTargetComponent();
			
			TCComponentBOPLine comp  = (TCComponentBOPLine) aifcomp;
			final TCComponentItemRevision rev = comp.getItemRevision();
			String type = rev.getType();
			if(type.equals("MENCMachining Revision")) {			 
				Display display = Display.getDefault();
				Shell shell = new Shell(display);
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterPath(System.getProperty("JAVA.HOME"));
				fd.setFilterExtensions(new String[]{"*.pdf"});
				fd.setFilterNames(new String[]{"PPT Files(*.pdf)"});
				String file = fd.open();
				if (file == null) {
					return null;
				}
				String file_name = fd.getFileName();
				if (!file_name.endsWith("仿真报告.pdf") && !file_name.endsWith("仿真报告.PDF")) {
					String name = file_name.substring(0, file_name.length() - 4);
					String suffix = file_name.substring(file_name.length() - 3, file_name.length());
					file_name = name + "仿真报告." + suffix;
				}
				final String name = file_name;
				final File files = new File(file);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							MyDatasetUtil.createPDFDatesetByMENCMachining(rev, name, files);
							MessageBox.post("上传成功","提示",MessageBox.INFORMATION);
						} catch (Exception e) {
							MessageBox.post(e);
							e.printStackTrace();
						}
					}
				}).start();
			}else {
				MessageBox.post("选择的不是工序版本！请选择工序版本进行上传操作！","错误",MessageBox.ERROR);
			}
		} catch (Exception e) {
			MessageBox.post(e);
			e.printStackTrace();
		}
		return null;	
	}

}
