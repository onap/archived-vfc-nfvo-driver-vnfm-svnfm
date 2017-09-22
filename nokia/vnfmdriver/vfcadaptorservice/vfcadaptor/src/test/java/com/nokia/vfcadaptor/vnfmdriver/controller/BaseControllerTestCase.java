/*
 * Copyright 2016-2017, Nokia Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nokia.vfcadaptor.vnfmdriver.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

public class BaseControllerTestCase {

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String AUTH = "auth";

	private static final String UTF_8 = "utf-8";

	protected static String serviceUrl = "http://127.0.0.1:8080/AppSenseAnalysisSystem";

	protected static String baseUrl;

	protected static String pictureServerRootUrl = "http://localhost";

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	protected boolean isHttpsProtocol = false;

	@BeforeClass
	public static void beforeClass() throws Exception {
		baseUrl = serviceUrl;
	}
	
	public static org.apache.http.client.HttpClient wrapClient(org.apache.http.client.HttpClient base) {  
        try {  
            SSLContext ctx = SSLContext.getInstance("TLS");  
            X509TrustManager tm = new X509TrustManager() {  
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
                    return null;  
                }  
                public void checkClientTrusted(  
                        java.security.cert.X509Certificate[] chain,  
                        String authType)  
                        throws java.security.cert.CertificateException {  
                    // TODO Auto-generated method stub  
                      
                }  
                public void checkServerTrusted(  
                        java.security.cert.X509Certificate[] chain,  
                        String authType)  
                        throws java.security.cert.CertificateException {  
                    // TODO Auto-generated method stub  
                      
                }  
            };  
            ctx.init(null, new TrustManager[] { tm }, null);  
            SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
            SchemeRegistry registry = new SchemeRegistry();  
            registry.register(new Scheme("https", 8089, ssf));  
            ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(registry);  
            return new DefaultHttpClient(mgr, base.getParams());  
        } catch (Exception ex) {  
            ex.printStackTrace();  
            return null;  
        }  
    }
	
	protected String sendPostMsg(String message, String url) throws UnsupportedEncodingException,
			IOException, ClientProtocolException {
	    
		HttpClient httpclient = new DefaultHttpClient();
		if(isHttpsProtocol)
		{
		    httpclient = wrapClient(httpclient);
		}
		HttpPost httppost = new HttpPost(url);
		StringEntity myEntity = new StringEntity(message, UTF_8);
		String auth = "";
		httppost.addHeader(AUTH, auth);
		httppost.addHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//		httppost.addHeader(CONTENT_TYPE, MediaType.TEXT_XML_VALUE);
		httppost.setEntity(myEntity);
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resEntity = response.getEntity();
		String responseContent = "";
		if (resEntity != null) {
			responseContent = EntityUtils.toString(resEntity, "UTF-8");
			EntityUtils.consume(resEntity);
		}
		httpclient.getConnectionManager().shutdown();
		return responseContent;
	}
	
	protected String sendGetMsg(String message, String url) throws UnsupportedEncodingException,
	IOException, ClientProtocolException {

      HttpClient httpclient = new DefaultHttpClient();
     if(isHttpsProtocol)
     {
     httpclient = wrapClient(httpclient);
     }
     HttpGet  httpGet = new  HttpGet(url);
     StringEntity myEntity = new StringEntity(message, UTF_8);
     String auth = "";
     httpGet.addHeader(AUTH, auth);
     httpGet.addHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        //httppost.addHeader(CONTENT_TYPE, MediaType.TEXT_XML_VALUE);
     //((HttpResponse) httpGet).setEntity(myEntity);
  HttpResponse response = httpclient.execute(httpGet);
  HttpEntity resEntity = response.getEntity();
   String responseContent = "";
   if (resEntity != null) {
	responseContent = EntityUtils.toString(resEntity, "UTF-8");
	responseContent.replaceAll("\r", "");//
	EntityUtils.consume(resEntity);
   }
   httpclient.getConnectionManager().shutdown();
   return responseContent;
   }
}
