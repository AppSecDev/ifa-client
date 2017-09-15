/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.ZipException;

import javax.naming.SizeLimitExceededException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.xml.stream.XMLStreamException;

import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.ibm.appscan.ifa.client.AssessmentDetails;
import com.ibm.appscan.ifa.client.EventManagerIfa;
import com.ibm.appscan.ifa.client.IfaClientException;
import com.ibm.appscan.ifa.client.Job;
import com.ibm.appscan.ifa.client.Messages;
import com.ibm.appscan.ifa.client.RequestMethods.REQUEST_TYPE;
import com.ibm.appscan.ifa.client.Zip;

/**
 * Abstract connection object with common methods.
 *
 */
public abstract class IfaAction {
	static final int BUFFER = 2048;

	private String m_host=null;
	private File m_file;
	private File m_target_dir;
	private ArrayList<String>m_hosts=new ArrayList<String>();
	private int m_host_index=Integer.MAX_VALUE;
	private boolean m_debug=false;
	private boolean m_allow_self_signed=false;
	private String m_target_name;
	public IfaAction(File f, 
			File target_dir,
			String target_name,
			boolean debug,
			boolean allow_self_signed,ArrayList<String> hosts) throws IfaClientException {
		m_target_name=target_name;
		setFile(f);
		setTagetFile(target_dir);
		if (getFile()!=null){
			if (getTargetFile()==null&&getFile().getParentFile()!=null){
				setTagetFile(getFile().getParentFile());
			}
		}
		
		if (getTargetFile()==null){
			setTagetFile(new File("."));
		}
		m_debug=debug;
		m_allow_self_signed=allow_self_signed;
		for (String h:hosts){
			addHost(h);
		}
	}

	private void addHost(String host) throws IfaClientException{
		if (!host.endsWith("/")){
			host=host+"/";
		}
		try {
			URL url=new URL(host);
			if (url.getProtocol()==null||url.getProtocol().length()==0){
				throw new IfaClientException(Messages.getMessage("err.server.proto.missing",host));
			}
			if (url.getHost()==null||url.getHost().length()==0){
				throw new IfaClientException(Messages.getMessage("err.server.host.missing", host));
			}
			if (url.getPort()>65535){
				throw new IfaClientException(Messages.getMessage("err.server.port.invalid", host));
			}
			if (url.getPort()==-1){
				throw new IfaClientException(Messages.getMessage("err.server.port.missing", host));
			}
		} catch (MalformedURLException e) {
			throw new IfaClientException(e.getLocalizedMessage());
		}

		if (!m_hosts.contains(host)){
			m_hosts.add(host);
		}

	}

	public String getHost(){
		if (m_host==null){
			int index=new Random().nextInt(m_hosts.size());
			m_host_index=index;
			m_host=m_hosts.get(m_host_index);
		}
		return m_host;
	}

	public ArrayList<File>run() throws Exception{
		if (getFile().exists()){
			try {
				EventManagerIfa.fireProgressTitleChanged(getCurrentStage());
				Exception e = null;

				//Try up to 10 times to run the job
				for (int i=0;i<10;i++){
					try {
						return uploadAssessmentForIfa();

					}catch (IfaClientException ice){
						e=ice;
						break;
					}catch (ZipException ze){
						e=ze;
					} catch (HttpHostConnectException hce){
						//Host is down...Remove it from the list and continue on.
						e=hce;
						m_hosts.remove(m_host_index);
						if (m_hosts.size()==0){
							e= new IfaClientException(Messages.getMessage("err.servers.all.down"));
							break;
						}
					} catch (UnknownHostException uhe){
						//Host is unknown...Remove it from the list and continue on.
						e=uhe;
						m_hosts.remove(m_host_index);
						if (m_hosts.size()==0){
							e= new IfaClientException(Messages.getMessage("err.servers.all.down"));
							break;
						}
					} catch (SSLHandshakeException she){
						e=new IfaClientException(Messages.getMessage("err.self.signed.cert", getHost()));
					} catch (IOException ioe){
						e=ioe;
					} catch (NumberFormatException nme){
						e=nme;
					} catch (Exception ee){
						e=ee;
					}
				}
				if (e!=null){
					e.printStackTrace();
					throw e;
				}
			}finally {
				cleanUp();
			}
		} 
		return null;
	}
	protected void cleanUp(){

	}
	private boolean isResponseGood(ArrayList<File>files) throws FileNotFoundException, XMLStreamException{
		System.out.println();
		System.out.println(Messages.getMessage("process.verify"));
		for (File f:files){
			try {
			new AssessmentDetails(f);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return true;
	}
	protected void setFile(File f){
		m_file=f;
	}

	protected void setTagetFile(File f){
		m_target_dir=f;
	}
	public File getFile(){
		return m_file;
	}

	public File getTargetFile(){
		return m_target_dir;
	}
	protected abstract String getRestPath();

	protected abstract String getCurrentStage();

	protected abstract REQUEST_TYPE getRequestType();

	protected HttpClient getClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException{
		if (m_allow_self_signed){
			//Really shouldn't accept all conversations.
			// Trust all certs
			HttpClientBuilder b = HttpClientBuilder.create();

			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					return true;
				}

			}).build();
			b.setSslcontext( sslContext);

			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslSocketFactory)
					.build();

			PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
			b.setConnectionManager( connMgr);

			return b.build();
		} else {
			return HttpClientBuilder.create().build();
		}

	}

	public  URL getConnection(String rest, REQUEST_TYPE method) throws IfaClientException {
		try {
			return new URL(getHost()+"rest/ifa/"+rest);

		} catch (MalformedURLException e) {
			throw new IfaClientException(e);
		}

	}

	protected String getCurrentHost(){
		if (m_host==null){

		}
		return m_host;
	}

	protected String getResponse(HttpResponse response) throws IfaClientException{
		BufferedReader r;
		try {
			r = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuilder total = new StringBuilder();

			String line = null;

			while ((line = r.readLine()) != null) {
				total.append(line);
			}
			return total.toString();
		} catch (IllegalStateException e) {
			throw new IfaClientException(e);
		} catch (IOException e) {
			throw new IfaClientException(e);
		}
	}

	protected ArrayList<File> uploadAssessmentForIfa() 
			throws IfaClientException, 
			KeyManagementException, 
			ClientProtocolException,
			NoSuchAlgorithmException, 
			KeyStoreException, 
			IOException, 
			SizeLimitExceededException, 
			JSONException, 
			InterruptedException, XMLStreamException
			{

		File zip=null;
		File ret_zip=new File(new File("upifa.zip").getAbsolutePath());
		try
		{
			HttpPost post=getPost();

			zip=Zip.makeZipFile(getFile(), getTargetFile());
			/* example for adding an image part */
			FileEntity fileBody = new FileEntity(zip); //image should be a String
			//Set to request body
			post.setEntity(fileBody) ;
			String current_stage=Messages.getMessage("progress.payload.prep");
			EventManagerIfa.fireProgressStageChanged(current_stage);
			String job_id=getResponse(getClient().execute(post));
			System.out.println(Messages.getMessage("job.id", job_id));
			Job job=new Job(getJobStatus(job_id));
			while (!job.isDone()){
				job.refreshJob(getJobStatus(job.getJobId()));
				EventManagerIfa.fireProgressStageChanged(job.getStage());
				EventManagerIfa.fireProgressChanged((int)job.getProgress());
				Thread.sleep(500);
			}
			ArrayList<File>ret=null;
			try {
			ret=getCompletedJob(job);
			}catch (IfaClientException e){
				throw e;
			}catch (Exception e){
				e.printStackTrace();
			}
			if (ret.size()==0){
				throw new IfaClientException(Messages.getMessage("err.no.assessment.returned"));
			}
			if (isResponseGood(ret)){
				EventManagerIfa.fireProgressStageChanged(getCurrentStage());
				EventManagerIfa.fireCompleted(job.getJobUrl());
				return ret;
			}
			return null;
		} catch (SocketException e) {
			throw new IfaClientException(Messages.getMessage("err.server.no.response"));
		} catch (NoHttpResponseException e) {
			e.printStackTrace();
			throw new IfaClientException(Messages.getMessage("err.server.no.response"));
		} catch (SSLException e) {
			e.printStackTrace();
			throw new IfaClientException(Messages.getMessage("err.server.no.response"));
		}
		finally{
			if (zip!=null && zip.exists() && !m_debug){
				zip.delete();
			}
			if (ret_zip!=null){
				ret_zip.delete();
			}
		}
	}

	protected HttpPost getPost() throws IfaClientException{
		return new HttpPost(getConnection(
				getRestPath(),getRequestType()).toString());
	}
	
	protected HttpPost getPost(HashMap<String, String>query_parameters) throws IfaClientException{
		StringBuilder url=new StringBuilder();
		url.append(getConnection(
				getRestPath(),getRequestType()).toString());
		if (query_parameters.keySet().size()>0){
			url.append("?");
			for (String key:query_parameters.keySet()){
				url.append(key);
				url.append("=");
				url.append(query_parameters.get(key));
				url.append("&");
			}
			if (url.length()>0){
				url.deleteCharAt(url.length()-1);
			}
		}
		return new HttpPost(url.toString());
	}
	
	private JSONObject getJobStatus(String job_id) throws KeyManagementException, ClientProtocolException, NoSuchAlgorithmException, KeyStoreException, IOException, IfaClientException, JSONException{
		return new JSONObject(getResponse(getClient().execute(new HttpGet(getConnection(
				"v1/jobs/"+job_id,REQUEST_TYPE.GET).toString()))));
	}

	private ArrayList<File> getCompletedJob(Job job) throws KeyManagementException, ClientProtocolException, NoSuchAlgorithmException, KeyStoreException, IOException, IfaClientException, JSONException{
		if (!job.hasError()){
			HttpResponse response =getClient().execute(new HttpGet(job.getJobUrl()));
			return Zip.getUnzippedFile(response.getEntity().getContent(), 
					getTargetFile(), m_target_name);
		}
		throw new IfaClientException(job.getError());

	}
}
