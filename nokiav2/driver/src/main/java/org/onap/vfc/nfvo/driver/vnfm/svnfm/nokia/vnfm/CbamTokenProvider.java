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
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.GenericExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.VnfmCredentials;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.VnfmUrls;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.SystemFunctions;
import org.slf4j.Logger;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

/**
 * Responsible for providing a token to access CBAM APIs
 */
//even if the value for grant type an user password is the same they do not mean the same thing
//the duplication of this is intentional
@SuppressWarnings("squid:S1192")
public class CbamTokenProvider extends CbamSecurityProvider {
    public static final int MAX_RETRY_COUNT = 5;
    public static final String GRANT_TYPE = "password";
    public static final String CLIENT_SECRET = "password";
    private static final String CBAM_TOKEN_URL = "realms/cbam/protocol/openid-connect/token";
    private static Logger logger = getLogger(CbamTokenProvider.class);
    private final VnfmInfoProvider vnfmInfoProvider;
    private volatile CurrentToken token;

    CbamTokenProvider(VnfmInfoProvider vnfmInfoProvider) {
        this.vnfmInfoProvider = vnfmInfoProvider;
    }

    /**
     * @return the token to access CBAM APIs (ex. 123456)
     */
    public Interceptor getToken(String vnfmId) {
        return new OauthInterceptor(getTokenInternal(vnfmId));
    }

    private String getTokenInternal(String vnfmId) {
        logger.trace("Requesting token for accessing CBAM API");
        synchronized (this) {
            long now = SystemFunctions.systemFunctions().currentTimeMillis();
            if (token == null || token.refreshAfter < now) {
                if (token == null) {
                    logger.debug("No token: getting first token");
                } else {
                    logger.debug("Token expired {} ms ago", (now - token.refreshAfter));
                }
                refresh(vnfmId);
            } else {
                logger.debug("Token will expire in {} ms", (now - token.refreshAfter));
            }
        }
        return token.token.accessToken;
    }

    private void refresh(String vnfmId) {
        VnfmUrls vnfmUrls = GenericExternalSystemInfoProvider.convert(vnfmInfoProvider.getVnfmInfo(vnfmId));
        VnfmCredentials vnfmCredentials = GenericExternalSystemInfoProvider.convertToCredentials(vnfmInfoProvider.getVnfmInfo(vnfmId));

        FormBody body = new FormBody.Builder()
                .add("grant_type", GRANT_TYPE)
                .add("client_id", vnfmCredentials.getClientId())
                .add("client_secret", vnfmCredentials.getClientSecret())
                .add("username", vnfmCredentials.getUsername())
                .add(CLIENT_SECRET, vnfmCredentials.getPassword()).build();
        String cbamKeyCloakBaseUrl = vnfmUrls.getAuthUrl();
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
                    throw buildFatalFailure(logger, "Bad response from CBAM KeyStone");
                }
            } catch (Exception e) {
                lastException = e;
                logger.warn("Unable to get token to access CBAM API (" + (i + 1) + "/" + MAX_RETRY_COUNT + ")", e);
            }
        }
        throw buildFatalFailure(logger, "Unable to get token to access CBAM API (giving up retries)", lastException);
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

    private static class OauthInterceptor implements Interceptor {
        private final String token;

        OauthInterceptor(String token) {
            this.token = token;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request.Builder builder = request.newBuilder();
            builder.addHeader("Authorization", "Bearer " + token);
            Request request1 = builder.build();
            return chain.proceed(request1);
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
