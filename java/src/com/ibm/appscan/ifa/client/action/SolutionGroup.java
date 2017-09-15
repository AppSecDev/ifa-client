/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client.action;

import java.io.File;
import java.util.ArrayList;

import com.ibm.appscan.ifa.client.IfaClientException;
import com.ibm.appscan.ifa.client.Messages;
import com.ibm.appscan.ifa.client.RequestMethods.REQUEST_TYPE;

public class SolutionGroup extends IfaAction{

	public SolutionGroup(File f, File target_dir,boolean debug,boolean allow_self_signed,ArrayList<String> hosts) throws IfaClientException {
		super(f, target_dir,null,debug,allow_self_signed,hosts);
		if (target_dir==null){
			setTagetFile(new File(f.getPath()+"_groups"));
		}
		
	}

	@Override
	protected String getRestPath() {
		return "v1/fix-group-assessments";
	}

	@Override
	protected String getCurrentStage() {
		return Messages.getMessage("progress.ifa.groups", getFile().getName());
	}

	@Override
	protected REQUEST_TYPE getRequestType() {
		return REQUEST_TYPE.POST;
	}
	
}
