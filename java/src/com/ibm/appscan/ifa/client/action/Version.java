/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client.action;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;

import com.ibm.appscan.ifa.client.IfaClientException;
import com.ibm.appscan.ifa.client.Messages;
import com.ibm.appscan.ifa.client.RequestMethods.REQUEST_TYPE;

public class Version extends IfaAction{

	public Version(boolean allow_self_signed, ArrayList<String> hosts)
			throws IfaClientException {
		super(null, null, false, allow_self_signed, hosts);
	}

	@Override
	protected String getRestPath() {
		return "v1/version";
	}

	public String getVersion() throws IfaClientException{
		try {
			HttpGet g=new HttpGet(getConnection(
					getRestPath(),getRequestType()).toString());
			return getResponse(getClient().execute(g));
		} catch (HttpHostConnectException e) {
			throw new IfaClientException(Messages.getMessage("err.servers.all.down"));
		} catch (Exception e) {
			throw new IfaClientException(e);
		} 
	}
	@Override
	protected String getCurrentStage() {
		return "";
	}

	@Override
	protected REQUEST_TYPE getRequestType() {
		return REQUEST_TYPE.GET;
	}

}
