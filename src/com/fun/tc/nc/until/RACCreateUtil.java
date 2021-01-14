package com.fun.tc.nc.until;

import java.util.Map;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;

public class RACCreateUtil {

	@SuppressWarnings("unchecked")
	public static TCComponent create(TCSession session, String name, String desc, String type, Map<String, String> properties)  throws Exception {
		DataManagementService dm = DataManagementService.getService(session);
		CreateIn createIn = new CreateIn();
		createIn.data.boName = type;
		createIn.data.stringProps.put("object_name", name);
		createIn.data.stringProps.put("object_desc", desc);
		if (properties != null && properties.size() > 0) {
			Set<String> property_names = properties.keySet();
			for (String property_name : property_names) {
				createIn.data.stringProps.put(property_name, properties.get(property_name));
			}
		}
		
		CreateIn createIns[] = new CreateIn[] { createIn };
		CreateResponse response = dm.createObjects(createIns);
		if (response == null || response.serviceData.sizeOfPartialErrors() > 0) {
			throw new Exception(response.serviceData.getPartialError(0).getMessages()[0]);
		}
		return response.serviceData.getCreatedObject(0);
	}
}
