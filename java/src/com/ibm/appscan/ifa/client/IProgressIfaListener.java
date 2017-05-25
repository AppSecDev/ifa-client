/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client;

public interface IProgressIfaListener {

	public void handleProgress(int progress);
	public void handleProgressTitle(String current);
	public void handleProgressStage(String stage);
	public void handleComplete(String completed_url);
}
