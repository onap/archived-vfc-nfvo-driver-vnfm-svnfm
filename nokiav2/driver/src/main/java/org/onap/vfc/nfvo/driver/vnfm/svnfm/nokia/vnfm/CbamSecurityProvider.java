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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm;

import com.google.common.base.Joiner;
import com.google.common.io.BaseEncoding;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.StoreLoader;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing a token to access CBAM APIs
 */
@Component
public class CbamSecurityProvider {
    private static Logger logger = getLogger(CbamSecurityProvider.class);
    @Value("${trustedCertificates}")
    private String trustedCertificates;
    @Value("${skipCertificateVerification}")
    private boolean skipCertificateVerification;
    @Value("${skipHostnameVerification}")
    private boolean skipHostnameVerification;

    protected HostnameVerifier buildHostnameVerifier() {
        if (skipHostnameVerification) {
            return (hostname, session) -> true;
        } else {
            return new DefaultHostnameVerifier();
        }
    }

    protected SSLSocketFactory buildSSLSocketFactory() {
        try {
            TrustManager[] trustManagers = new X509TrustManager[]{buildTrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to create SSL socket factory", e);
        }
    }

    protected X509TrustManager buildTrustManager() {
        if (skipCertificateVerification) {
            return new AllTrustedTrustManager();
        } else {
            if (StringUtils.isEmpty(trustedCertificates)) {
                throw buildFatalFailure(logger, "If the skipCertificateVerification is set to false (default) the trustedCertificates can not be empty");
            }
            Set<String> trustedPems;
            String content;
            try {
                content = new String(BaseEncoding.base64().decode(trustedCertificates), StandardCharsets.UTF_8);
                trustedPems = StoreLoader.getCertifacates(content);
            } catch (Exception e) {
                throw buildFatalFailure(logger, "The trustedCertificates must be a base64 encoded collection of PEM certificates", e);
            }
            if (trustedPems.size() == 0) {
                throw buildFatalFailure(logger, "No certificate can be extracted from " + content);
            }
            try {
                KeyStore keyStore = StoreLoader.loadStore(Joiner.on("\n").join(trustedPems), randomUUID().toString(), randomUUID().toString());
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                return (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
            } catch (Exception e) {
                throw buildFatalFailure(logger, "Unable to create keystore", e);
            }
        }
    }

    private static class AllTrustedTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //no need to check certificates if everything is trusted
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //no need to check certificates if everything is trusted
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
