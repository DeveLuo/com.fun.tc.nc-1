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
import com.teamcenter.rac.kernel.TCComponent;
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
				if (file == null || file.isEmpty()) {
					return null;
				}
				TCComponent form = rev.getRelatedComponent("IMAN_master_form_rev");
				if (form == null) {
					MessageBox.post(rev.toDisplayString() + "属性表单不能为空！","提示",MessageBox.INFORMATION);
					return null;
				}
				final String name = form.getProperty("ae8gy_filename") + "仿真报告";
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
