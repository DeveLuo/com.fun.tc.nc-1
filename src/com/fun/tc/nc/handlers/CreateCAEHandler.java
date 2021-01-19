package com.fun.tc.nc.handlers;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.fun.tc.nc.dialogs.CreateCAEDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class CreateCAEHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InterfaceAIFComponent aifCom = AIFUtility.getCurrentApplication().getTargetComponent();
		if (aifCom == null) {
			MessageBox.post("请选择BOPLine进行操作", "提示", MessageBox.INFORMATION);
			return null;
		}
		if (!(aifCom instanceof TCComponentBOPLine)) {
			MessageBox.post("请选择BOPLine进行操作", "提示", MessageBox.INFORMATION);
			return null;
		}
		try {
			TCComponentBOPLine line = (TCComponentBOPLine)aifCom;
			TCComponentItemRevision processRev = line.getItemRevision();
			if (!"AE8Operation Revision".equals(processRev.getType())) {
				MessageBox.post("请选择机加工序进行操作", "提示", MessageBox.INFORMATION);
				return null;
			}
			TCComponentBOMLine t_line = line.getCachedParent();
			if (t_line == null) {
				MessageBox.post("当前机加工序在视图账中无机加工艺！", "提示", MessageBox.INFORMATION);
				return null;
			}
			
			TCComponentItemRevision rev = t_line.getItemRevision();
			if (!"AE8Process Revision".equals(rev.getType())) {
				MessageBox.post("当前机加工序父项不是机加工艺！", "提示", MessageBox.INFORMATION);
				return null;
			}
			
			TCComponent[] refComs = rev.getRelatedComponents("AE8_ASSONCMEP");
			TCComponentItemRevision process = null;
			for (TCComponent refCom : refComs) {
				if ("MEProcessRevision".equals(refCom.getType())) {
					process = (TCComponentItemRevision) refCom;
					break;
				}
			}
			if (process == null) {
				MessageBox.post("请先为" + rev + "创建数控程序集！", "提示", MessageBox.INFORMATION);
				return null;
			}
			TCComponentBOMLine top = t_line.getCachedParent();
			if (top == null) {
				MessageBox.post("当前机加工序在视图中无总工艺！", "提示", MessageBox.INFORMATION);
				return null;
			}
			AIFComponentContext[] contents = top.getChildren();
			TCComponentBOMLine p_line = null;
			for (AIFComponentContext context : contents) {
				TCComponentBOMLine bomline = (TCComponentBOMLine) context.getComponent();
				if (process.equals(bomline.getItemRevision())) {
					p_line = bomline;
					break;
				}
			}
			if(p_line == null) {
				MessageBox.post("(" + rev + "）关联数控（"+ process + "）不在当前视图中！", "提示", MessageBox.INFORMATION);
				return null;
			}
			SwingUtilities.invokeLater(new CreateCAEDialog(p_line, processRev, rev));
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}
	
	public TCComponentItemRevision getRelation(TCComponentItemRevision rev) throws TCException {
		TCComponentItemRevision relationRev = null;
		AIFComponentContext[] contexts = rev.getPrimary();
		TCComponentItemRevision process = null;
		for (AIFComponentContext context : contexts) {
			TCComponent com = (TCComponent) context.getComponent();
			if ("AE8_ASSONCMEP".equals(context.getContext()) && "AE8Process Revision".equals(com.getType())) {
				process = (TCComponentItemRevision) com;
				break;
			}
		}
		if (process != null) {
			TCComponent[] views = process.getRelatedComponents("view");
			for (TCComponent view : views) {
				String type = view.getType();
				if ("AE8Operation".equals(type)) {
					relationRev = ((TCComponentItem)view).getLatestItemRevision();
					break;
				}				
			}
		}
		return relationRev;
	}
}
