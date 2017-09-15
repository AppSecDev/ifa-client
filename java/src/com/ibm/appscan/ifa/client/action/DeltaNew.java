/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client.action;

import java.io.File;
import java.util.ArrayList;

import com.ibm.appscan.ifa.client.IfaClientException;

public class DeltaNew extends Delta{

	public DeltaNew(File f, File target_dir, String target_name,File baseline, boolean debug,
			boolean allow_self_signed, ArrayList<String> hosts)
			throws IfaClientException {
		super(f, target_dir,target_name, baseline, debug, allow_self_signed, hosts);
	}

	@Override
	protected String getDeltaType() {
		return "new";
	}

}
