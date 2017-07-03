/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

package org.openo.nfvo.vnfmadapter.service.csm.connect;

import net.sf.json.JSONObject;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.openo.baseservice.util.impl.SystemEnvVariablesFactory;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

/**
 * SSL context
 * .</br>
 *
 * @author
 * @version     NFVO 0.5  Sep 14, 2016
 */
public class AbstractSslContext {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSslContext.class);
    protected AbstractSslContext(){
        //constructor
    }

    private static SSLContext getSSLContext() throws NoSuchAlgorithmException {
        return SSLContext.getInstance("TLSv1.2");
    }

    protected static SSLContext getAnonymousSSLContext() throws GeneralSecurityException {
        SSLContext sslContext = getSSLContext();
        sslContext.init(null, new TrustManager[] {new TrustAnyTrustManager()}, new SecureRandom());
        return sslContext;
    }
    protected static SSLContext getCertificateSSLContext() throws GeneralSecurityException {
        SSLContext sslContext = getSSLContext();
        JSONObject   sslConf = null;
        try {
             sslConf = readSSLConfToJson();
        } catch (Exception e) {
            LOG.error("readSSLConfToJson error",e);
        }
        sslContext.init(createKeyManager(sslConf), createTrustManager(sslConf), new SecureRandom());
        return sslContext;
    }

    protected  static KeyManager[] createKeyManager(JSONObject sslConf) {
        KeyManager[] kms = null;
        try {
            String CERT_STORE="etc/conf/server.p12";
            String CERT_STORE_PASSWORD="Changeme_123";
            String KEY_STORE_TYPE = "PKCS12";
            if(sslConf != null){
                CERT_STORE = sslConf.getString("keyStore");
                CERT_STORE_PASSWORD = sslConf.getString("keyStorePass");
                KEY_STORE_TYPE = sslConf.getString("keyStoreType");
            }
            // load jks file
            FileInputStream f_certStore=new FileInputStream(CERT_STORE);
            KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
            ks.load(f_certStore, CERT_STORE_PASSWORD.toCharArray());
            f_certStore.close();

            // init and create
            String alg= KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory kmFact = KeyManagerFactory.getInstance(alg);
            kmFact.init(ks, CERT_STORE_PASSWORD.toCharArray());

            kms = kmFact.getKeyManagers();
        }  catch (Exception e) {
           LOG.error("create KeyManager fail!",e);
        }
        return kms;
    }
    protected  static TrustManager[] createTrustManager(JSONObject sslConf){
        TrustManager[] tms = null;
        try {

        String TRUST_STORE="etc/conf/trust.jks";
        String TRUST_STORE_PASSWORD="Changeme_123";
        String TRUST_STORE_TYPE = "jks";
        if(sslConf != null){
            TRUST_STORE = sslConf.getString("trustStore");
            TRUST_STORE_PASSWORD    = sslConf.getString("trustStorePass");
            TRUST_STORE_TYPE    = sslConf.getString("trustStoreType");
        }
        FileInputStream f_trustStore=new FileInputStream(TRUST_STORE);
        KeyStore ks = KeyStore.getInstance(TRUST_STORE_TYPE);
        ks.load(f_trustStore, TRUST_STORE_PASSWORD.toCharArray());
        f_trustStore.close();

        String alg=TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmFact=TrustManagerFactory.getInstance(alg);
        tmFact.init(ks);
        tms=tmFact.getTrustManagers();

        } catch (Exception e){
            LOG.error("create TrustManager fail!",e);
        }
        return  tms;
    }

    /**readSSLConfToJson
     * @return
     * @throws IOException
     * @since NFVO 0.5
     */
    public static JSONObject readSSLConfToJson() throws IOException {
        JSONObject  sslJson= null;
        InputStream ins = null;
        BufferedInputStream bins = null;
        String fileContent = "";

        String fileName = SystemEnvVariablesFactory.getInstance().getAppRoot() + System.getProperty("file.separator")
                + "etc" + System.getProperty("file.separator") + "conf" + System.getProperty("file.separator")
                + "sslconf.json";

        try {
            ins = new FileInputStream(fileName);
            bins = new BufferedInputStream(ins);

            byte[] contentByte = new byte[ins.available()];
            int num = bins.read(contentByte);

            if(num > 0) {
                fileContent = new String(contentByte);
            }
            sslJson = JSONObject.fromObject(fileContent);
        } catch(FileNotFoundException e) {
            LOG.error(fileName + "is not found!", e);
        } catch (Exception e){
            LOG.error("read sslconf file fail.please check if the 'sslconf.json' is exist.");
        }finally {
            if(ins != null) {
                ins.close();
            }
            if(bins != null) {
                bins.close();
            }
        }

        return sslJson;
    }
    private static class TrustAnyTrustManager implements X509TrustManager {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            //NOSONAR
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            //NOSONAR
        }
    }
}
