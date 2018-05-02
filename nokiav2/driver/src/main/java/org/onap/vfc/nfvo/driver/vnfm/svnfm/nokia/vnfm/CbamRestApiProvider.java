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
import com.nokia.cbam.catalog.v1.api.DefaultApi;
import com.nokia.cbam.lcm.v32.ApiClient;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vnfmdriver.model.VnfmInfo;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.GenericExternalSystemInfoProvider.convert;

/**
 * Responsible for providing client to access CBAM REST API
 */
public class CbamRestApiProvider {
    public static final String NOKIA_LCN_API_VERSION = "3.2";
    public static final String NOKIA_LCM_API_VERSION = "3.2";
    public static final String AUTH_NAME = "test";
    private final CbamTokenProvider tokenProvider;
    private final VnfmInfoProvider vnfmInfoProvider;
    private final CbamSecurityProvider cbamSecurityProvider;

    CbamRestApiProvider(CbamTokenProvider cbamTokenProvider, VnfmInfoProvider vnfmInfoProvider, CbamSecurityProvider cbamSecurityProvider) {
        this.tokenProvider = cbamTokenProvider;
        this.vnfmInfoProvider = vnfmInfoProvider;
        this.cbamSecurityProvider = cbamSecurityProvider;
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @return API to access CBAM LCM API
     */
    public VnfsApi getCbamLcmApi(String vnfmId) {
        return buildLcmApiClient(vnfmId).createService(VnfsApi.class);
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @return API to access the operation executions
     */
    public OperationExecutionsApi getCbamOperationExecutionApi(String vnfmId) {
        return buildLcmApiClient(vnfmId).createService(OperationExecutionsApi.class);
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @return API to access CBAM LCN subscription API
     */
    public SubscriptionsApi getCbamLcnApi(String vnfmId) {
        return buildLcnApiClient(vnfmId).createService(SubscriptionsApi.class);
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @return API to access CBAM catalog API
     */
    public DefaultApi getCbamCatalogApi(String vnfmId) {
        return buildCatalogApiClient(vnfmId).createService(DefaultApi.class);
    }

    @VisibleForTesting
    com.nokia.cbam.lcn.v32.ApiClient buildLcnApiClient(String vnfmId) {
        com.nokia.cbam.lcn.v32.ApiClient apiClient = new com.nokia.cbam.lcn.v32.ApiClient();
        apiClient.getOkBuilder().sslSocketFactory(cbamSecurityProvider.buildSSLSocketFactory(), cbamSecurityProvider.buildTrustManager());
        apiClient.getOkBuilder().hostnameVerifier(cbamSecurityProvider.buildHostnameVerifier());
        apiClient.addAuthorization(AUTH_NAME, tokenProvider.getToken(vnfmId));
        apiClient.setAdapterBuilder(apiClient.getAdapterBuilder().baseUrl(convert(vnfmInfoProvider.getVnfmInfo(vnfmId)).getLcnUrl()));
        return apiClient;
    }

    @VisibleForTesting
    com.nokia.cbam.catalog.v1.ApiClient buildCatalogApiClient(String vnfmId) {
        com.nokia.cbam.catalog.v1.ApiClient apiClient = new com.nokia.cbam.catalog.v1.ApiClient();
        apiClient.getOkBuilder().sslSocketFactory(cbamSecurityProvider.buildSSLSocketFactory(), cbamSecurityProvider.buildTrustManager());
        apiClient.getOkBuilder().hostnameVerifier(cbamSecurityProvider.buildHostnameVerifier());
        apiClient.addAuthorization(AUTH_NAME, tokenProvider.getToken(vnfmId));
        apiClient.setAdapterBuilder(apiClient.getAdapterBuilder().baseUrl(convert(vnfmInfoProvider.getVnfmInfo(vnfmId)).getCatalogUrl()));
        return apiClient;
    }

    @VisibleForTesting
    ApiClient buildLcmApiClient(String vnfmId) {
        VnfmInfo vnfmInfo = vnfmInfoProvider.getVnfmInfo(vnfmId);
        ApiClient apiClient = new ApiClient();
        apiClient.getOkBuilder().sslSocketFactory(cbamSecurityProvider.buildSSLSocketFactory(), cbamSecurityProvider.buildTrustManager());
        apiClient.getOkBuilder().hostnameVerifier(cbamSecurityProvider.buildHostnameVerifier());
        apiClient.addAuthorization(AUTH_NAME, tokenProvider.getToken(vnfmId));
        apiClient.setAdapterBuilder(apiClient.getAdapterBuilder().baseUrl(convert(vnfmInfoProvider.getVnfmInfo(vnfmId)).getLcmUrl()));
        return apiClient;
    }
}
