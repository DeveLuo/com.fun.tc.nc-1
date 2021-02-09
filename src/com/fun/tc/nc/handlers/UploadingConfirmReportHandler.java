package com.fun.tc.nc.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.fun.tc.nc.until.MyDatasetUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCTaskState;
import com.teamcenter.rac.util.MessageBox;

public class UploadingConfirmReportHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) {
		try {
			final TCComponent com = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
			if(!com.getType().equals("MENCMachining Revision")) {	
				MessageBox.post("请选择数控程序集版本进行上传！","提示",MessageBox.INFORMATION);
				return null;
			}
			TCComponentProcess process = com.getCurrentJob();
			TCComponentTask task = null;
			if (process != null) {
				TCComponentTask root = process.getRootTask();
				task = root.getSubtask("上传确认报告");
				if (task == null) {
					MessageBox.post("当前流程无“上传确认报告”节点，无法上传！","提示",MessageBox.INFORMATION);
					return null;
				}
				if (!task.getState().equals(TCTaskState.STARTED)) {
					MessageBox.post("当前节点不是“上传确认报告”，无法上传！","提示",MessageBox.INFORMATION);
					return null;
				}
			}
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
			fd.setFilterPath(System.getProperty("JAVA.HOME"));
			fd.setFilterExtensions(new String[]{"*.pdf"});
			fd.setFilterNames(new String[]{"PPT Files(*.pdf)"});
			String file = fd.open();
			if (file == null || file.isEmpty()) {
				return null;
			}
			TCComponent form = com.getRelatedComponent("IMAN_master_form_rev");
			if (form == null) {
				MessageBox.post(com.toDisplayString() + "属性表单不能为空！","提示",MessageBox.INFORMATION);
				return null;
			}
			final String name = form.getProperty("ae8gy_filename") + "确认报告";
			final File files = new File(file);
			final TCComponentTask upload_task = task;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						MyDatasetUtil.uploadPDFByMENCMachining(com, upload_task, name, files);
						
					} catch (Exception e) {
						MessageBox.post(e);
						e.printStackTrace();
					}
				}
			}).start();
			
		} catch (Exception e) {
			MessageBox.post(e);
			e.printStackTrace();
		}
		return null;	
	}

}
