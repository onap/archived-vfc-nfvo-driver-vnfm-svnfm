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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import okhttp3.*;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.StoreLoader;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.UserVisibleError;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.fatalFailure;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

/**
 * Responsible for providing a token to access CBAM APIs
 */
@Component
//even if the value for grant type an user password is the same they do not mean the same thing
//the duplication of this is intentional
@SuppressWarnings("squid:S1192")
public class CbamTokenProvider {
    public static final int MAX_RETRY_COUNT = 5;
    public static final String GRANT_TYPE = "password";
    public static final String CLIENT_SECRET = "password";
    private static final String CBAM_TOKEN_URL = "/realms/cbam/protocol/openid-connect/token";
    private static Logger logger = getLogger(CbamTokenProvider.class);
    private final VnfmInfoProvider vnfmInfoProvider;
    @Value("${cbamKeyCloakBaseUrl}")
    private String cbamKeyCloakBaseUrl;
    @Value("${cbamUsername}")
    private String username;
    @Value("${cbamPassword}")
    private String password;
    @Value("${trustedCertificates}")
    private String trustedCertificates;
    @Value("${skipCertificateVerification}")
    private boolean skipCertificateVerification;
    @Value("${skipHostnameVerification}")
    private boolean skipHostnameVerification;
    private volatile CurrentToken token;

    @Autowired
    CbamTokenProvider(VnfmInfoProvider vnfmInfoProvider) {
        this.vnfmInfoProvider = vnfmInfoProvider;
    }

    /**
     * @return the token to access CBAM APIs (ex. 123456)
     */
    public String getToken(String vnfmId) {
        VnfmInfo vnfmInfo = vnfmInfoProvider.getVnfmInfo(vnfmId);
        return getToken(vnfmInfo.getUserName(), vnfmInfo.getPassword());
    }

    private String getToken(String clientId, String clientSecret) {
        logger.trace("Requesting token for accessing CBAM API");
        synchronized (this) {
            long now = SystemFunctions.systemFunctions().currentTimeMillis();
            if (token == null || token.refreshAfter < now) {
                if (token == null) {
                    logger.debug("No token: getting first token");
                } else {
                    logger.debug("Token expired {} ms ago", (now - token.refreshAfter));
                }
                refresh(clientId, clientSecret);
            } else {
                logger.debug("Token will expire in {} ms", (now - token.refreshAfter));
            }
        }
        return token.token.accessToken;
    }

    private void refresh(String clientId, String clientSecret) {
        FormBody body = new FormBody.Builder()
                .add("grant_type", GRANT_TYPE)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("username", username)
                .add(CLIENT_SECRET, password).build();
        Request request = new Request.Builder().url(cbamKeyCloakBaseUrl + CBAM_TOKEN_URL).addHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE).post(body).build();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        SSLSocketFactory sslSocketFac = buildSSLSocketFactory();
        HostnameVerifier hostnameVerifier = buildHostnameVerifier();
        OkHttpClient client = builder.sslSocketFactory(sslSocketFac).hostnameVerifier(hostnameVerifier).build();
        Exception lastException = null;
        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            try {
                Response response = execute(client.newCall(request));
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    TokenResponse tokenResponse = new Gson().fromJson(json, TokenResponse.class);
                    //token is scheduled to be refreshed in the half time before expiring
                    token = new CurrentToken(tokenResponse, getTokenRefreshTime(tokenResponse));
                    return;
                } else {
                    fatalFailure(logger, "Bad response from CBAM KeyStone");
                }
            } catch (Exception e) {
                lastException = e;
                logger.warn("Unable to get token to access CBAM API (" + (i + 1) + "/" + MAX_RETRY_COUNT + ")", e);
            }
        }
        throw fatalFailure(logger, "Unable to get token to access CBAM API (giving up retries)", lastException);
    }

    @VisibleForTesting
    Response execute(Call call) throws IOException {
        return call.execute();
    }

    /**
     * - a new token is requested after the half of the time has expired till which the currently
     * used token is valid
     *
     * @param token the currently held token
     * @return the point in time after which a new token must be requested
     */
    private long getTokenRefreshTime(TokenResponse token) {
        return SystemFunctions.systemFunctions().currentTimeMillis() + token.expiresIn * (1000 / 2);
    }

    private HostnameVerifier buildHostnameVerifier() {
        if (skipHostnameVerification) {
            return (hostname, session) -> true;
        } else {
            return new DefaultHostnameVerifier();
        }
    }

    @VisibleForTesting
    SSLSocketFactory buildSSLSocketFactory() {
        try {
            TrustManager[] trustManagers = buildTrustManager();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw fatalFailure(logger, "Unable to create SSL socket factory", e);
        }
    }

    @VisibleForTesting
    TrustManager[] buildTrustManager() throws KeyStoreException, NoSuchAlgorithmException {
        if (skipCertificateVerification) {
            return new TrustManager[]{new AllTrustedTrustManager()};
        } else {
            if (StringUtils.isEmpty(trustedCertificates)) {
                throw new IllegalArgumentException("If the skipCertificateVerification is set to false (default) the trustedCertificates can not be empty");
            }
            Set<String> trustedPems;
            try {
                trustedPems = StoreLoader.getCertifacates(new String(BaseEncoding.base64().decode(trustedCertificates), StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new UserVisibleError("The trustedCertificates must be a base64 encoded collection of PEM certificates", e);
            }
            KeyStore keyStore = StoreLoader.loadStore(Joiner.on("\n").join(trustedPems), randomUUID().toString(), randomUUID().toString());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();

        }
    }

    private static class CurrentToken {
        private final TokenResponse token;
        private final long refreshAfter;

        CurrentToken(TokenResponse token, long refreshAfter) {
            this.refreshAfter = refreshAfter;
            this.token = token;
        }
    }

    static class AllTrustedTrustManager implements X509TrustManager {
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

    /**
     * Represents the token received from CBAM
     */
    private static class TokenResponse {
        @SerializedName("access_token")
        String accessToken;
        @SerializedName("expires_in")
        int expiresIn;
        @SerializedName("id_token")
        String tokenId;
        @SerializedName("not-before-policy")
        int notBeforePolicy;
        @SerializedName("refresh_expires_in")
        int refreshExpiresIn;
        @SerializedName("refresh_token")
        String refreshToken;
        @SerializedName("session_state")
        String sessionState;
        @SerializedName("token_type")
        String tokenType;
    }
}
