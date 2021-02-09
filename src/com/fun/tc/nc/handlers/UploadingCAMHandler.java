package com.fun.tc.nc.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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

public class UploadingCAMHandler extends AbstractHandler{

	private Shell parent;
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		
		try {
			InterfaceAIFComponent aifcomp = AIFUtility.getCurrentApplication().getTargetComponent();
			
			TCComponentBOPLine comp  = (TCComponentBOPLine) aifcomp;
			final TCComponentItemRevision rev = comp.getItemRevision();
			String type = rev.getType();
			if(type.equals("MENCMachining Revision")) {	
				Display display = Display.getDefault();	
				parent = new Shell(display,SWT.SHELL_TRIM);
				FileDialog fd = new FileDialog(parent, SWT.OPEN | SWT.MULTI);
				fd.setFilterExtensions(new String[]{"*.MPF","*.*"});
				fd.setFilterNames(new String[]{"MPF Files(*.MPF)","所有文件"});
				fd.open();
				final String[] fileNames = fd.getFileNames();
				final String filterPath = fd.getFilterPath();
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							if (fileNames != null && fileNames.length > 0) {
								for (int i = 0; i < fileNames.length; i++) {
									String dir = filterPath+"\\"+fileNames[i];
									File file = new File(dir);
									MyDatasetUtil.createDatesetByMENCMachining(rev, fileNames[i], file);
								}
								MessageBox.post("上传成功","提示",MessageBox.INFORMATION);
							}
						} catch (Exception e) {
							MessageBox.post(e);
							e.printStackTrace();
						}
						
					}
				}).start();
			} else {
				MessageBox.post("选择的不是工序版本，请选择工序版本进行上传操作！","错误",1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post("上传出错:"+e.toString(),"错误",1);
		}
		
		return null;
	}

}
