/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.client.methods.HttpPost;

import com.ibm.appscan.ifa.client.IfaClientException;
import com.ibm.appscan.ifa.client.Messages;
import com.ibm.appscan.ifa.client.RequestMethods.REQUEST_TYPE;

public abstract class Delta extends IfaAction{

	private File m_baseline;
	private File m_temp;
	public Delta(File f, File target_dir,String target_name, File baseline,boolean debug,boolean allow_self_signed,ArrayList<String> hosts) throws IfaClientException {
		super(f,target_dir,target_name,debug,allow_self_signed,hosts);
		if (f.isDirectory()){
			throw new IfaClientException(Messages.getMessage("err.diff.needs.file",f.getPath()));
		}
		if (f.getParentFile()!=null){
			m_temp=new File(f.getParentFile()+File.separator+UUID.randomUUID().toString());
		} else {
			m_temp=new File(File.separator+UUID.randomUUID().toString());
		}
		
		
		try {
			m_baseline=baseline;
			m_temp.mkdirs();
			
			copy(baseline, new File(m_temp,baseline.getName()));
			copy(f, new File(m_temp,f.getName()));
			setFile(m_temp);
		} catch (IOException e) {
			throw new IfaClientException(e);
		}
	}

	private void copy(File source, File dest) throws IOException{
		FileInputStream input = null;
		FileOutputStream output = null;
		
		try {
			input = new FileInputStream(source);
			FileChannel sourceChannel = input.getChannel();
			
			output = new FileOutputStream(dest);
			FileChannel destChannel = output.getChannel();
			
			sourceChannel.transferTo(0, source.length(), destChannel);
		}
		finally {
			try {
				input.close();
			} finally {
				output.close();
			}
		}
	}
	@Override
	protected String getCurrentStage() {
		return Messages.getMessage("progress.ifa.diff", m_baseline.getName());
	}

	@Override
	protected REQUEST_TYPE getRequestType() {
		return REQUEST_TYPE.POST;
	}

	@Override
	protected void cleanUp() {
		removeDir(m_temp);
	}
	
	private void removeDir(File dir){
		if (dir.isDirectory()){
			for (File f:dir.listFiles()){
				removeDir(f);
			}
		} 
		dir.delete();
	}
	
	@Override
	protected String getRestPath() {
		return "v1/delta-assessments";
	}
	
	protected HttpPost getPost() throws IfaClientException{
		HashMap<String, String>query_param=new HashMap<>();
		query_param.put("delta_type", getDeltaType());
	    HttpPost ret = getPost(query_param);
	    return ret;
	}
	
	protected abstract String getDeltaType();
}
