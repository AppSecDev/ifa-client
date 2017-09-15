/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;

import com.ibm.appscan.ifa.client.action.ApplyIfa;
import com.ibm.appscan.ifa.client.action.IfaAction;

public class IfaActionTest extends TestCase{

	@Test
	public void testHostsBadProto() throws IfaClientException{
		boolean _failed=false;
		ArrayList<String>hosts=new ArrayList<String>();
		hosts.add("htp://localhost:9080");
		try {
			new ApplyIfa(new File("."), null,null, _failed, _failed, hosts);
		} catch (Exception e){
			_failed=true;
		}
		assertTrue(_failed);
	}
	
	@Test
	public void testHostsBadHost() throws IfaClientException{
		boolean _failed=false;
		ArrayList<String>hosts=new ArrayList<String>();
		hosts.add("http://:9080");
		try {
			new ApplyIfa(new File("."), null,null, _failed, _failed, hosts);
		} catch (Exception e){
			_failed=true;
		}
		assertTrue(_failed);
		
	}
	@Test
	public void testHostsBadPort() throws IfaClientException{
		boolean _failed=false;
		ArrayList<String>hosts=new ArrayList<String>();
		hosts.add("http://test:908000");
		try {
			new ApplyIfa(new File("."), null, null,_failed, _failed, hosts);
		} catch (Exception e){
			_failed=true;
		}
		assertTrue(_failed);
		
	}
	
	@Test
	public void testHostsNoPort() throws IfaClientException{
		boolean _failed=false;
		ArrayList<String>hosts=new ArrayList<String>();
		hosts.add("http://test");
		try {
			new ApplyIfa(new File("."), null,null, _failed, _failed, hosts);
		} catch (Exception e){
			_failed=true;
		}
		assertTrue(_failed);
		
	}
	
	@Test
	public void testGoodHost() throws IfaClientException{
		boolean _failed=false;
		ArrayList<String>hosts=new ArrayList<String>();
		hosts.add("http://localhost:9080");
		try {
			new ApplyIfa(new File("."), null,null, _failed, _failed, hosts);
		} catch (Exception e){
			_failed=true;
		}
		assertFalse(_failed);
	}
	@Test
	public void testGoodHostTrainlingSlash() throws IfaClientException{
		boolean _failed=false;
		ArrayList<String>hosts=new ArrayList<String>();
		hosts.add("http://localhost:9080");
		try {
			IfaAction a=new ApplyIfa(new File("."), null,null, _failed, _failed, hosts);
			assertEquals(hosts.get(0)+"/", a.getHost());
		} catch (Exception e){
			_failed=true;
		}
		assertFalse(_failed);
	}
	
	@Test
	public void testGoodMultipleHost() throws IfaClientException{
		boolean _failed=false;
		ArrayList<String>hosts=new ArrayList<String>();
		hosts.add("http://localhost:9080/");
		hosts.add("http://localhost2:9080/");
		
		try {
			boolean[] found=new boolean[hosts.size()];
			for (int i=0;i<found.length;i++){
				found[i]=false;
			}
			for (int y=0;y<10;y++){
				IfaAction a=new ApplyIfa(new File("."), null, null,_failed, _failed, hosts);
				for (int i=0;i<hosts.size();i++){
					if (a.getHost().equals(hosts.get(i))){
						found[i]=true;
						break;
					}
				}
			}
			
			for (int i=0;i<found.length;i++){
				if (!found[i]){
					_failed=true;
				}
			}
		} catch (Exception e){
			_failed=true;
		}
		assertFalse(_failed);
	}
}
