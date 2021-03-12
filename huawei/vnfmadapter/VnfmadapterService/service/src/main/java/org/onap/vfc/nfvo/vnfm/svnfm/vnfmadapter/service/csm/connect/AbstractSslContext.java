/*
 * Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.connect;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.SystemEnvVariablesFactory;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * SSL context
 * .</br>
 *
 * @author
 * @version VFC 1.0 Sep 14, 2016
 */
public class AbstractSslContext {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSslContext.class);

    protected AbstractSslContext() {
        // constructor
    }

    private static SSLContext getSSLContext() throws NoSuchAlgorithmException {
        return SSLContext.getInstance("TLSv1.2");
    }

    protected static SSLContext getAnonymousSSLContext() throws GeneralSecurityException {
        SSLContext sslContext = getSSLContext();
        sslContext.init(null, new TrustManager[] {new MyTrustManager()}, new SecureRandom());
        return sslContext;
    }

    protected static SSLContext getCertificateSSLContext() throws GeneralSecurityException {
        SSLContext sslContext = getSSLContext();
        JSONObject sslConf = null;
        try {
            sslConf = readSSLConfToJson();
        } catch(Exception e) {
            LOG.error("readSSLConfToJson error", e);
        }
        sslContext.init(createKeyManager(sslConf), createTrustManager(sslConf), new SecureRandom());
        return sslContext;
    }

    protected static KeyManager[] createKeyManager(JSONObject sslConf) {
        KeyManager[] kms = null;
        try {
            String CERT_STORE = "etc/conf/server.p12";
            String CERT_STORE_PASSWORD = "Changeme_123";  // NOSONAR
            String KEY_STORE_TYPE = "PKCS12";
            if(sslConf != null) {
                CERT_STORE = sslConf.getString("keyStore");
                CERT_STORE_PASSWORD = sslConf.getString("keyStorePass");
                KEY_STORE_TYPE = sslConf.getString("keyStoreType");
            }
            // load jks file
            try(FileInputStream f_certStore = new FileInputStream(CERT_STORE)) {
                KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
                ks.load(f_certStore, CERT_STORE_PASSWORD.toCharArray());

                // init and create
                String alg = KeyManagerFactory.getDefaultAlgorithm();
                KeyManagerFactory kmFact = KeyManagerFactory.getInstance(alg);
                kmFact.init(ks, CERT_STORE_PASSWORD.toCharArray());

                kms = kmFact.getKeyManagers();
            }
        } catch(Exception e) {
            LOG.error("create KeyManager fail!", e);
        }
        return kms;
    }

    protected static TrustManager[] createTrustManager(JSONObject sslConf) {
        TrustManager[] tms = null;
        try {

            String TRUST_STORE = "etc/conf/trust.jks";
            String TRUST_STORE_PASSWORD = "Changeme_123";  // NOSONAR
            String TRUST_STORE_TYPE = "jks";
            if(sslConf != null) {
                TRUST_STORE = sslConf.getString("trustStore");
                TRUST_STORE_PASSWORD = sslConf.getString("trustStorePass");
                TRUST_STORE_TYPE = sslConf.getString("trustStoreType");
            }
            try(FileInputStream f_trustStore = new FileInputStream(TRUST_STORE)) {
                KeyStore ks = KeyStore.getInstance(TRUST_STORE_TYPE);
                ks.load(f_trustStore, TRUST_STORE_PASSWORD.toCharArray());

                String alg = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmFact = TrustManagerFactory.getInstance(alg);
                tmFact.init(ks);
                tms = tmFact.getTrustManagers();
            }
        } catch(Exception e) {
            LOG.error("create TrustManager fail!", e);
        }
        return tms;
    }

    /**
     * readSSLConfToJson
     *
     * @return
     * @throws IOException
     * @since VFC 1.0
     */
    public static JSONObject readSSLConfToJson() throws IOException {
        JSONObject sslJson = null;

        String fileContent = "";

        String fileName = SystemEnvVariablesFactory.getInstance().getAppRoot()
                + System.getProperty(Constant.FILE_SEPARATOR) + "etc" + System.getProperty(Constant.FILE_SEPARATOR)
                + "conf" + System.getProperty(Constant.FILE_SEPARATOR) + "sslconf.json";

        try (InputStream ins = new FileInputStream(fileName)) {
            try(BufferedInputStream bins = new BufferedInputStream(ins)) {

                byte[] contentByte = new byte[ins.available()];
                int num = bins.read(contentByte);

                if(num > 0) {
                    fileContent = new String(contentByte);
                }
                sslJson = JSONObject.fromObject(fileContent);
            }
        } catch(FileNotFoundException e) {
            LOG.error(fileName + "is not found!", e);
        } catch(Exception e) {
            LOG.error("read sslconf file fail.please check if the 'sslconf.json' is exist.", e);
        }

        return sslJson;
    }

    private static class MyTrustManager implements X509TrustManager {
    	TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    	private MyTrustManager() throws NoSuchAlgorithmException{
    	}
    	
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        	try {
        		tmf.init((KeyStore)null);	
        	} catch (KeyStoreException e) {
        		throw new IllegalStateException(e);
        	}
        	
        	//Get hold of default trust manager
        	X509TrustManager x509Tm = null;
        	for(TrustManager tm: tmf.getTrustManagers())
        	{
        		if(tm instanceof X509TrustManager) {
        			x509Tm = (X509TrustManager) tm;
        			break;
        		}
        	}
        	
        	//Wrap it in your own class
        	final X509TrustManager finalTm = x509Tm;
        	finalTm.checkServerTrusted(certs, authType); 	
        	 	
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        	try {
        		tmf.init((KeyStore)null);	
        	} catch (KeyStoreException e) {
        		throw new IllegalStateException(e);
        	}
        	
        	//Get hold of default trust manager
        	X509TrustManager x509Tm = null;
        	for(TrustManager tm: tmf.getTrustManagers())
        	{
        		if(tm instanceof X509TrustManager) {
        			x509Tm = (X509TrustManager) tm;
        			break;
        		}
        	}
        	
        	//Wrap it in your own class
        	final X509TrustManager finalTm = x509Tm;
        	finalTm.checkClientTrusted(certs, authType);
        }
    }
}
