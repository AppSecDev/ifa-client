/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

public class Job {
	
	private JSONObject m_object;
	
	public Job(JSONObject obj)  {
		m_object=obj;
	}
	
	public void refreshJob(JSONObject obj) {
		m_object=obj;
	}
	
	public String getJobId() throws JSONException{
		return m_object.getString("job_id");//$NON-NLS-1$
	}
	
	public Exception getError() throws JSONException{
		if (hasError()){
			return new Exception(m_object.getJSONObject("error").getString("error_message"));//$NON-NLS-1$
		}
		return null;
	}
	
	public double getProgress() throws JSONException{
		return m_object.getDouble("progress");//$NON-NLS-1$
	}
	
	public boolean isDone() throws JSONException{
		return m_object.getBoolean("complete");//$NON-NLS-1$
	}

	public String getStage() throws JSONException{
		return m_object.getString("stage");//$NON-NLS-1$
	}
	
	public boolean hasError() throws JSONException{
		if (m_object.has("error") && //$NON-NLS-1$
				m_object.getString("error").length()>0){//$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	public String getJobUrl() throws JSONException{
		return m_object.getString("job_url");//$NON-NLS-1$
	}
	
	
}
