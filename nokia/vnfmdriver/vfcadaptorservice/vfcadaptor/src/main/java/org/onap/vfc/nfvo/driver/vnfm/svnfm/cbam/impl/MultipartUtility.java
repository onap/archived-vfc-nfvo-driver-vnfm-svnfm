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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.SslConfInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.util.CommonUtil;

import com.google.gson.Gson;
 
/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server.
 * @author www.codejava.net
 *
 */
public class MultipartUtility {
	private static final Logger logger = LoggerFactory.getLogger(MultipartUtility.class);
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpsURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
    private Gson gson = new Gson();
 
    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public MultipartUtility(String requestURL, String charset)
            throws CertificateException, IOException, NoSuchAlgorithmException, KeyManagementException{
        this.charset = charset;
         
        // creates a unique boundary based on time stamp
        boundary = "---" + System.currentTimeMillis() + "---";
        
        HostnameVerifier hv = new HostnameVerifier() {  
            public boolean verify(String urlHostName, SSLSession session) {  
                return true;  
            }  
        };  
        TrustManager[] trustAllCerts = new TrustManager[1];  
        TrustManager tm = new TrustManager() {
        	
        	    public X509Certificate[] getAcceptedIssuers() {  
        	        return null;  
        	    }  
        	  
        	    public boolean isServerTrusted(X509Certificate[] certs) {  
        	        return true;  
        	    }  
        	  
        	    public boolean isClientTrusted(X509Certificate[] certs) {  
        	        return true;  
        	    }  
        	  
        	    public void checkServerTrusted(X509Certificate[] certs, String authType)  
        	            throws CertificateException {  
        	        return;  
        	    }  
        	  
        	    public void checkClientTrusted(X509Certificate[] certs, String authType)  
        	            throws CertificateException {  
        	        return;  
        	    }  
        };  
        trustAllCerts[0] = tm;  
        SSLContext sslContext = SSLContext.getInstance("SSL");  
        String filePath = "/etc/conf/sslconf.json";
        String fileContent = CommonUtil.getJsonStrFromFile(filePath);
        sslContext.init(createKeyManager(gson.fromJson(fileContent, SslConfInfo.class)), createTrustManager(gson.fromJson(fileContent, SslConfInfo.class)), new SecureRandom());
        sslContext.init(null, trustAllCerts, null);
        sslContext.init(null, new TrustManager[] {new TrustAnyTrustManager()}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory()); 
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
         
        URL url = new URL(requestURL);
        httpConn = (HttpsURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
        httpConn.setRequestProperty("Test", "Bonjour");
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }
    
    private static class TrustAnyTrustManager implements X509TrustManager {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            // NOSONAR
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            // NOSONAR
        }
    }
    
    private KeyManager[] createKeyManager(SslConfInfo sslConf) {
        KeyManager[] kms = null;
        try {
            String CERT_STORE = "/etc/conf/server.p12";
            String CERT_STORE_PWD = "Changeme_123";
            String KEY_STORE_TYPE = "PKCS12";
            if(sslConf != null) {
                CERT_STORE = sslConf.getKeyStore();
                CERT_STORE_PWD= sslConf.getKeyStorePass();
                KEY_STORE_TYPE = sslConf.getKeyStoreType();
            }
            // load jks file
	    try(FileInputStream f_certStore = new FileInputStream(CommonUtil.getAppRoot() + CERT_STORE)){
		    KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
		    ks.load(f_certStore, CERT_STORE_PWD.toCharArray());
	    

            // init and create
            String alg = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory kmFact = KeyManagerFactory.getInstance(alg);
            kmFact.init(ks, CERT_STORE_PWD.toCharArray());

            kms = kmFact.getKeyManagers();
	    }
        } catch(Exception e) {
            logger.error("create KeyManager fail!", e);
        }
        return kms;
    }
    
    private TrustManager[] createTrustManager(SslConfInfo sslConf) {
        TrustManager[] tms = null;
        try {

            String TRUST_STORE = "/etc/conf/trust.jks";
            String TRUST_STORE_PWD = "Changeme_123";
            String TRUST_STORE_TYPE = "jks";
            if(sslConf != null) {
                TRUST_STORE = sslConf.getTrustStore();
                TRUST_STORE_PWD = sslConf.getTrustStorePass();
                TRUST_STORE_TYPE = sslConf.getTrustStoreType();
            }
            String jksFilePath1 =CommonUtil.getAppRoot() + TRUST_STORE;
            logger.info("jks path is " + jksFilePath1);
	    try(FileInputStream f_trustStore = new FileInputStream(jksFilePath1)){
		    KeyStore ks = KeyStore.getInstance(TRUST_STORE_TYPE);
		    ks.load(f_trustStore, TRUST_STORE_PWD.toCharArray());
	    

            String alg = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmFact = TrustManagerFactory.getInstance(alg);
            tmFact.init(ks);
            tms = tmFact.getTrustManagers();
	    }

        } catch(Exception e) {
            logger.error("create TrustManager fail!", e);
        }
        return tms;
    }
 
 
    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();
 
	try(FileInputStream inputStream = new FileInputStream(uploadFile)){
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
	}
         
        writer.append(LINE_FEED);
        writer.flush();    
    }
 
    /**
     * Adds a header field to the request.
     * @param name - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }
     
    /**
     * Completes the request and receives response from the server.
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public List<String> finish() throws IOException {
        List<String> response = new ArrayList<String>();
 
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();
 
        // checks server's status code first
        int status = httpConn.getResponseCode();
        logger.info("MultipartUtility --> finish " + httpConn.getResponseMessage());
        if (status == HttpsURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
 
        return response;
    }
}
