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

import com.google.common.io.BaseEncoding;
import com.nokia.cbam.catalog.v1.api.DefaultApi;
import com.nokia.cbam.lcm.v32.ApiClient;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

/**
 * Responsible for providing client to access CBAM REST API
 */
@Component
public class CbamRestApiProvider {
    public static final String NOKIA_LCN_API_VERSION = "3.2";
    public static final String NOKIA_LCM_API_VERSION = "3.2";
    private final DriverProperties driverProperties;
    private final CbamTokenProvider tokenProvider;
    private final VnfmInfoProvider vnfmInfoProvider;

    @Value("${trustedCertificates}")
    private String trustedCertificates;
    @Value("${skipCertificateVerification}")
    private boolean skipCertificateVerification;

    @Autowired
    public CbamRestApiProvider(DriverProperties driverProperties, CbamTokenProvider cbamTokenProvider, VnfmInfoProvider vnfmInfoProvider) {
        this.driverProperties = driverProperties;
        this.tokenProvider = cbamTokenProvider;
        this.vnfmInfoProvider = vnfmInfoProvider;
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @return API to access CBAM LCM API
     */
    public VnfsApi getCbamLcmApi(String vnfmId) {
        return new VnfsApi(getLcmApiClient(vnfmId));
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @return API to access the operation executions
     */
    public OperationExecutionsApi getCbamOperationExecutionApi(String vnfmId) {
        return new OperationExecutionsApi(getLcmApiClient(vnfmId));
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @return API to access CBAM LCN subscription API
     */
    public SubscriptionsApi getCbamLcnApi(String vnfmId) {
        com.nokia.cbam.lcn.v32.ApiClient apiClient = new com.nokia.cbam.lcn.v32.ApiClient();
        if (!skipCertificateVerification) {
            apiClient.setSslCaCert(new ByteArrayInputStream(BaseEncoding.base64().decode(trustedCertificates)));
        } else {
            apiClient.setVerifyingSsl(false);
        }
        apiClient.setBasePath(driverProperties.getCbamLcnUrl());
        apiClient.setAccessToken(tokenProvider.getToken(vnfmId));
        return new SubscriptionsApi(apiClient);
    }

    /**
     * @param vnfmId the identifier of the VNFM
     * @return API to access CBAM catalog API
     */
    public DefaultApi getCbamCatalogApi(String vnfmId) {
        com.nokia.cbam.catalog.v1.ApiClient apiClient = new com.nokia.cbam.catalog.v1.ApiClient();
        if (!skipCertificateVerification) {
            apiClient.setSslCaCert(new ByteArrayInputStream(BaseEncoding.base64().decode(trustedCertificates)));
        } else {
            apiClient.setVerifyingSsl(false);
        }
        apiClient.setBasePath(driverProperties.getCbamCatalogUrl());
        apiClient.setAccessToken(tokenProvider.getToken(vnfmId));
        return new DefaultApi(apiClient);
    }

    private ApiClient getLcmApiClient(String vnfmId) {
        VnfmInfo vnfmInfo = vnfmInfoProvider.getVnfmInfo(vnfmId);
        ApiClient apiClient = new ApiClient();
        if (!skipCertificateVerification) {
            apiClient.setSslCaCert(new ByteArrayInputStream(BaseEncoding.base64().decode(trustedCertificates)));
        } else {
            apiClient.setVerifyingSsl(false);
        }
        apiClient.setAccessToken(tokenProvider.getToken(vnfmId));
        apiClient.setBasePath(vnfmInfo.getUrl());
        return apiClient;
    }
}
