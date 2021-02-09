package com.fun.tc.nc.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.fun.tc.nc.until.MyDatasetUtil;
import com.fun.tc.nc.until.PropertyLOV;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentFormType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.stylesheet.InterfacePropertyComponent;
import com.teamcenter.rac.stylesheet.PropertyNameLabel;
import com.teamcenter.rac.stylesheet.PropertyTextField;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;

public class CreateCAEDialog extends AbstractAIFDialog implements ActionListener {

	private static final long serialVersionUID = -3032849889897906912L;
	
	TCComponentItemRevision processRev;
	
	String type = "MENCMachining";
	
	String formType = "MENCMachining Revision Master";
	
	TCComponentFormType masterType;

	private JButton okButton;

	private JButton closeButton;

	private JTextField idText;

	private JTextField revText;

	private JTextField nameText;

	private JButton assignButton;
	
	TCComponentItemType itemType;
	
	TCComponentBOMLine line;

	TCSession session;
	
	String relationName = "AE8RelNC";
	
	TCComponentItemRevision relationRev;
	
	String MENCMachiningTemplate = "MENCMachiningTemplate";
	
	String MENCMachiningMasterSYNCOperationMasterProps = "MENCMachiningMasterSYNCOperationMasterProps";
	
	List<InterfacePropertyComponent> coms = new ArrayList<InterfacePropertyComponent>();

	private JButton assignB1;

	private JButton assignB2;

	private PropertyTextField assign_text1;

	private PropertyTextField assign_text2;

	private JButton selectButton;

	private TCComponent form;

	private PropertyTextField jc_name;

	private PropertyTextField sb_no;

	private PropertyTextField os;

	private PropertyLOV assign_lov1;
	
	TCComponentItemRevision partRev;

	private PropertyTextField gy_filename;

	private PropertyLOV gy_filename_unit;

	private PropertyTextField gx_no;
	
	TCComponentItemRevision relationParentRev;

	private TCComponent parentForm;
	
	public CreateCAEDialog(TCComponentBOMLine line, TCComponentItemRevision relationRev, TCComponentItemRevision relationParentRev) throws Exception {
		super(AIFUtility.getActiveDesktop());
		setTitle("新建数控工序");
		this.line = line;
		this.processRev = line.getItemRevision();
		this.relationRev = relationRev;
		this.relationParentRev = relationParentRev;
		session = line.getSession();
		itemType = (TCComponentItemType) session.getTypeComponent(type);
		masterType = (TCComponentFormType) session.getTypeComponent(formType);
		String name = relationRev.getProperty("object_name");
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new PropertyLayout());
		titlePanel.setBorder(BorderFactory.createTitledBorder("MENCMachining"));
		titlePanel.add("1.1.center.center", new JLabel("  ID/版本.名称："));
		JPanel createPanel = new JPanel();
		idText = new JTextField(12);
		revText = new JTextField(3);
		nameText = new JTextField(24);
		nameText.setText(name + "数控工序");
		idText.setEditable(false);
		revText.setEditable(false);
		nameText.setEnabled(false);
		assignButton = new JButton("指派");
		assignButton.addActionListener(this);
		createPanel.add(idText);
		createPanel.add(new JLabel("/"));
		createPanel.add(revText);
		createPanel.add(new JLabel("."));
		createPanel.add(nameText);
		createPanel.add(assignButton);
		titlePanel.add("1.2.center.center", createPanel);
			
		form = relationRev.getRelatedComponent("IMAN_master_form_rev");
		parentForm = relationParentRev.getRelatedComponent("IMAN_master_form_rev");
		JSplitPane splitPane =  new JSplitPane();
		splitPane.setBorder(BorderFactory.createTitledBorder("相关信息"));
		JPanel machiningPanel = createLeftPanel(masterType);
		JPanel relationPanel = createRightPanel(masterType);
		splitPane.setLeftComponent(machiningPanel);
		splitPane.setRightComponent(relationPanel);
//		splitPane.setDividerLocation(0.5D);
		
		JPanel buttonPanel = new JPanel();
		okButton = new JButton("确认");
		closeButton = new JButton("关闭");
		okButton.addActionListener(this);
		closeButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(closeButton);
		setLayout(new BorderLayout());
		add(titlePanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		add(splitPane, BorderLayout.CENTER);
		centerToScreen();
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		try {
			if (obj.equals(okButton)) {
				String id = idText.getText();
				String revID = revText.getText();
				if (id.isEmpty() || revID.isEmpty()) {
					MessageBox.post(this, "请指派ID", "提示", MessageBox.INFORMATION);
					return;
				}
				gy_filename.setUIFValue((String)gy_filename.getEditableValue() + (String)gy_filename_unit.getEditableValue() + (String)gx_no.getEditableValue());

				TCComponentForm form = createForm(id + "/" + revID);
				TCComponentItem machining = itemType.create(id, revID, type, nameText.getText(), "", null, null, form);
				TCComponentItemRevision rev = machining.getLatestItemRevision();
				TCComponentBOMLine subLine = line.addBOMLine(line, rev, null);
				if (partRev != null) {
					subLine.addBOMLine(subLine, partRev, null);
				}
				line.window().save();
				if (relationRev != null) {
					relationRev.add(relationName, rev);
				}
				rev.setRelated("IMAN_METarget", processRev.getRelatedComponents("IMAN_METarget"));
				List<TCComponentDataset> templates = getTemplates();
				for (TCComponentDataset template : templates) {
					String cx_name = form.getProperty("ae8gy_filename");
					String name = template.getProperty("object_name");
					TCComponentDataset dataset = template.saveAs(cx_name + name);
					rev.add(rev.getDefaultPasteRelation(), dataset);
					if (dataset.getType().startsWith("MSWord")) {
						MyDatasetUtil.sign(dataset, getValues());
					}
				}
				MessageBox.post(this, machining + "创建成功", "提示", MessageBox.INFORMATION);
				dispose();
			} else if (obj.equals(assignButton)) {
				assign();
			} else if (obj.equals(closeButton)) {
				dispose();
			} else if (obj.equals(assignB1)) {
				assign1();
			} else if (obj.equals(assignB2)) {
				assign2();
			} else if (obj.equals(selectButton)) {
				SwingUtilities.invokeLater(new SelectDeviceDialog(this));
			}
		} catch (Exception e) {
			MessageBox.post(this, e);
			e.printStackTrace();
		}
		
	}
	
	public List<TCComponentDataset> getTemplates() throws TCException {
		List<TCComponentDataset> datasets = new ArrayList<TCComponentDataset>();
		String id = session.getPreferenceService().getStringValue(MENCMachiningTemplate);
		if (id == null || id.isEmpty()) {
			return datasets;
		}
		@SuppressWarnings("deprecation")
		TCComponentItem item = itemType.find(id);
		if (item == null ) {
			return datasets;
		}
		TCComponentItemRevision rev = item.getLatestItemRevision();
		TCComponent[] coms = rev.getRelatedComponents("IMAN_specification");
		for (TCComponent com : coms) {
			if (com instanceof TCComponentDataset) {
				datasets.add((TCComponentDataset)com);
			}
		}
		
		return datasets;
	}
	
	public JPanel createLeftPanel(TCComponentFormType masterType) throws Exception {
		JPanel machiningPanel = new JPanel();
		machiningPanel.setBorder(BorderFactory.createTitledBorder("数控工序信息"));
		machiningPanel.setLayout(new PropertyLayout());
		machiningPanel.setBackground(Color.WHITE);
		int row = 1;
		
		String property_name = "ae8cx_no";
		TCPropertyDescriptor desc = masterType.getPropertyDescriptor(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			assign_text1 = new PropertyTextField();
			assign_text1.setProperty(property_name);
			assign_text1.load(desc);
//			assign_text1.setModifiable(false);
			assign_text1.setColumns(16);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", assign_text1);
			coms.add(assign_text1);
			
			property_name = "ae8cx_no_unit";
			desc = masterType.getPropertyDescriptor(property_name);
			assign_lov1 =  new PropertyLOV(getLOV(property_name));
			assign_lov1.setProperty(property_name);
			assign_lov1.load(desc);
			machiningPanel.add(row + ".3.center.center", (Component) assign_lov1);
			coms.add(assign_lov1);
			
			assignB1 = new JButton("指派");
			assignB1.addActionListener(this);
			machiningPanel.add(row + ".4.center.center", assignB1);
			row++;
		}
		
		property_name = "ae8qrb_no";
		desc = masterType.getPropertyDescriptor(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			assign_text2 = new PropertyTextField();
			assign_text2.setProperty(property_name);
			assign_text2.load(desc);
//			assign_text2.setModifiable(false);
			assign_text2.setColumns(16);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", assign_text2);
			coms.add(assign_text2);
			
			property_name = "ae8cx_no_unit";
			desc = masterType.getPropertyDescriptor(property_name);
			InterfacePropertyComponent assign_lov2 =  new PropertyLOV(getLOV(property_name));
			assign_lov2.setProperty(property_name);
			assign_lov2.load(desc);
			machiningPanel.add(row + ".3.center.center", (Component) assign_lov2);
			coms.add(assign_lov2);
			
			assignB2 = new JButton("指派");
			assignB2.addActionListener(this);
			machiningPanel.add(row + ".4.center.center", assignB2);
			row++;
		}
		
		property_name = "ae8gy_filename";
		desc = masterType.getPropertyDescriptor(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			gy_filename =  new PropertyTextField();
			gy_filename.setProperty(property_name);
			gy_filename.load(desc);
			gy_filename.setModifiable(false);
			gy_filename.setUIFValue(form.getProperty("ae8part_no"));
			gy_filename.setColumns(16);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) gy_filename);
			coms.add(gy_filename);
			
			property_name = "ae8gy_filename_unit";
			desc = masterType.getPropertyDescriptor(property_name);
			gy_filename_unit = new PropertyLOV(getLOV(property_name));;
			gy_filename_unit.setProperty(property_name);
			gy_filename_unit.load(desc);
			machiningPanel.add(row + ".3.center.center", (Component) gy_filename_unit);
			coms.add(gy_filename_unit);
			
			property_name = "ae8gx_no1";
			desc = masterType.getPropertyDescriptor(property_name);
			gx_no = new PropertyTextField();
			gx_no.setProperty(property_name);
			gx_no.load(desc);
			((PropertyTextField)gx_no).setColumns(4);
			gx_no.setUIFValue(form.getProperty("ae8gx_no"));
			gx_no.setModifiable(false);
			
			machiningPanel.add(row + ".4.center.center", (Component) gx_no);
			coms.add(gx_no);
			row++;
		}
		
		property_name = "ae8cx_state";
		desc = masterType.getPropertyDescriptor(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			InterfacePropertyComponent com =  new PropertyLOV(getLOV(property_name));
			com.setProperty(property_name);
			com.load(desc);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) com);
			coms.add(com);
			row++;
		}
		
		property_name = "ae8cxfz";
		desc = masterType.getPropertyDescriptor(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			InterfacePropertyComponent com = new PropertyLOV(getLOV(property_name));
			com.setProperty(property_name);
			com.load(desc);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) com);
			coms.add(com);
			row++;
		}
		
		selectButton = new JButton("选择");
		selectButton.addActionListener(this);
		machiningPanel.add(row + ".1.center.center", new JLabel("设备选择:"));
		machiningPanel.add(row + ".2.center.center", selectButton);
		row++;
		
		property_name = "ae8sb_no";
		desc = masterType.getPropertyDescriptor(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			jc_name =  new PropertyTextField();
			jc_name.setProperty(property_name);
			jc_name.load(desc);
			jc_name.setModifiable(false);
			jc_name.setColumns(16);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) jc_name);
			coms.add(jc_name);
			row++;
		}
		
		property_name = "ae8jc_id";
		desc = masterType.getPropertyDescriptor(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			sb_no =  new PropertyTextField();
			sb_no.setProperty(property_name);
			sb_no.load(desc);
			sb_no.setModifiable(false);
			sb_no.setColumns(16);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) sb_no);
			coms.add(sb_no);
			row++;
		}
		
		property_name = "ae8os";
		desc = masterType.getPropertyDescriptor(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			os =  new PropertyTextField();
			os.setProperty(property_name);
			os.load(desc);
			os.setModifiable(false);
			os.setColumns(16);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) os);
			coms.add(os);
			row++;
		}
		return machiningPanel;
		
	}
	
	public String[] getLOV(String property_name) {
		String[] values = session.getPreferenceService().getStringValues(property_name + "_lov");
		return values == null ? new String[0]: values;
	}
	
	public JPanel createRightPanel(TCComponentFormType masterType) throws Exception {
		JPanel relationPanel = new JPanel();
		relationPanel.setBorder(BorderFactory.createTitledBorder("关联数控工序信息"));
		relationPanel.setLayout(new PropertyLayout());
		relationPanel.setBackground(Color.WHITE);
		int row = 1;
		Map<String, String> properties = getSyncValues();
		for (String property_name : properties.keySet()) {
			TCPropertyDescriptor desc = masterType.getPropertyDescriptor(property_name);
			if (desc == null) {
				System.out.println("数控工序版本表单（"+masterType.getType()+ "）中没有属性：" + property_name);
				continue;
			}
			String value = properties.get(property_name);
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			PropertyTextField textField =  new PropertyTextField();
			textField.setProperty(property_name);
			textField.load(desc);
			textField.setUIFValue(value);
			textField.setEditable(false);
			textField.setColumns(24);
			relationPanel.add(row + ".1.center.center", lable);
			relationPanel.add(row + ".2.center.center", textField);
			row++;
			coms.add(textField);
		}
		return relationPanel;
	}
	
	public TCComponentForm createForm(String name) throws TCException {
		TCComponentForm masterForm = masterType.create(name, "", formType);
		Map<String, String> properties = getValues();
		masterForm.setProperties(properties);
		return masterForm;
	}
	
	public Map<String, String> getSyncValues()throws TCException{
		Map<String, String> properties = new LinkedHashMap<String, String>();
		String[] values = session.getPreferenceService().getStringValues(MENCMachiningMasterSYNCOperationMasterProps);
		if (values != null) {
			for (String value : values) {
				String[] info = value.split("=");
				if (info.length != 2) {
					continue;
				}
				String name = info[0];
				String value_name = info[1];
				if (value_name.startsWith("revision.")) {
					value_name = value_name.replace("revision.", "");
					properties.put(name, relationRev.getProperty(value_name));
				} else if(value_name.startsWith("parentmaster.")) {
					value_name = value_name.replace("parentmaster.", "");
					properties.put(name, parentForm.getProperty(value_name));
				} else {
					properties.put(name, form.getProperty(value_name));
				}
			}
		}
		return properties;
	}
	
	public Map<String, String> getValues() throws TCException{
		Map<String, String> properties = new HashMap<String, String>();
		for (InterfacePropertyComponent com : coms) {
			Object obj = com.getEditableValue();
			if (obj == null) {
				obj = "";
			}
			properties.put(com.getProperty(), obj.toString());
		}
		return properties;
	}
	
	public void assign() throws TCException {
		String id = idText.getText();
		String revID = revText.getText();
		if (id.isEmpty() && revID.isEmpty()) {
			id = itemType.getNewID();
			revID = itemType.getNewRev(null);
			idText.setText(id);
			revText.setText(revID);
			idText.setEditable(false);
			revText.setEditable(false);
		}
	}
	
	public void assign1() throws TCException {
		String str = assign_text1.getText();
		String unit = (String)assign_lov1.getEditableValue();
		if (str.isEmpty()) {
			assign_text1.setText(getNumber("AE8CXIDRule", 2, unit));
		}
	}
	
	public void assign2() throws TCException {
		String str = assign_text2.getText();
		String unit = (String)assign_lov1.getEditableValue();
		if (str.isEmpty()) {
			assign_text2.setText(getNumber("AE8QRBIDRule",3,unit));
		}
	}
	
	public String getNumber(String type, int insert, String unit) throws TCException {
		TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(type);
		String id = itemType.getNewID();
		StringBuilder sb = new StringBuilder(id);
		sb.insert(insert, unit);
		return sb.toString();
	}
	
	public void syncDeviceValue(TCComponent com) throws TCException {
		setPart(com);
		String id = "";
		String no = "";
		String type = "";
		if (com != null) {
			TCComponent form = com.getRelatedComponent("IMAN_master_form_rev");
			id = com.getProperty("item_id");
			no = form.getProperty("ae8device_no");
			type = form.getProperty("ae8control_type");
		}
		jc_name.setText(id);
		sb_no.setText(no);
		os.setText(type);
	}
	
	public void setPart(TCComponent com) throws TCException {
		TCComponentItemRevision rev = null;
		if (com instanceof TCComponentItemRevision) {
			rev = (TCComponentItemRevision) com;
		} else if (com instanceof TCComponentItem) {
			rev = ((TCComponentItem) com).getLatestItemRevision();
		}
		this.partRev = rev;
	}
}
