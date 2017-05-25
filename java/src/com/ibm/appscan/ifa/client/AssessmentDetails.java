/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class AssessmentDetails {

	private XMLStreamReader m_reader;
	private File m_file;

	private int m_high_findings=0;
	private int m_med_findings=0;
	private int m_low_findings=0;
	private int m_excluded_findings=0;
	private String m_app_name;
	public AssessmentDetails(File f) throws FileNotFoundException, XMLStreamException {
		m_file=f;
		load();
	}

	public int getHighFindingCount(){
		return m_high_findings;
	}
	public int getMedFindingCount(){
		return m_med_findings;
	}
	public int getLowFindingCount(){
		return m_low_findings;
	}
	public int getFindingCount(){
		return m_high_findings+
				m_med_findings+
				m_low_findings;
	}
	
	public int getExcludedFindingCount(){
		return m_excluded_findings;
	}
	
	public String getApplicationName(){
		return m_app_name;
	}
	
	private XMLStreamReader getReader() throws FileNotFoundException, XMLStreamException {
		if (m_reader==null){
			m_reader=XMLInputFactory.newInstance().createXMLStreamReader(
					new BufferedInputStream(new FileInputStream(m_file)));
		}
		return m_reader;
	}

	private void load() throws FileNotFoundException, XMLStreamException{
			while(getReader().hasNext()){
				int event = getReader().next();
				switch(event){
				case XMLStreamConstants.START_ELEMENT: 
					if ("AssessmentStats".equals(getReader().getLocalName())){
						m_high_findings=Integer.parseInt(getReader().getAttributeValue(null, "total_high_finding"));
						m_med_findings=Integer.parseInt(getReader().getAttributeValue(null, "total_med_finding"));
						m_low_findings=Integer.parseInt(getReader().getAttributeValue(null, "total_low_finding"));
						m_excluded_findings=Integer.parseInt(getReader().getAttributeValue(null, "total_excluded_findings"));
					}
					if ("Application".equals(getReader().getLocalName())){
						m_app_name=getReader().getAttributeValue(null, "name");
						return;
					}
				}
			}
		}
	}
