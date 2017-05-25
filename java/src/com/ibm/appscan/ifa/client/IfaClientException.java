/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client;

public class IfaClientException extends Exception{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8155565818813746876L;

	public IfaClientException(Throwable e) {
		super(e);
	}

	public IfaClientException(String response) {
		super(response);
	}
}
