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

/**
 * Uploads a single assessment, or a directory of assessments for IFA processing.
 * @param f The assessment file to upload
 * @param target_dir The target directory to place. If this is null the parent directory
 * of f will be used.
 * @return The IFA treated assessment file
 * @throws IfaClientException If the IFA file cannot be fully processed. 
 * Does a check to make sure the returned assessment can be properly loaded.
 */
public class ApplyIfa extends IfaAction{

	
	public ApplyIfa(File f, File target_dir,boolean debug,boolean allow_self_signed,ArrayList<String> hosts) throws IfaClientException {
		super(f, target_dir,debug,allow_self_signed, hosts);
	}

	protected String getRestPath() {
		return "v1/triaged-assessments";
	}

	@Override
	protected String getCurrentStage() {
		return Messages.getMessage("progress.ifa.file", getFile().getName());
	}

	@Override
	protected REQUEST_TYPE getRequestType() {
		return REQUEST_TYPE.POST;
	}

}
