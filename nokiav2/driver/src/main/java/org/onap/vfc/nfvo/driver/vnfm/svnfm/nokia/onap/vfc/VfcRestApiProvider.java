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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.vfc;

import com.google.common.annotations.VisibleForTesting;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.MsbApiProvider;
import org.onap.vfccatalog.api.VnfpackageApi;
import org.onap.vnfmdriver.ApiClient;
import org.onap.vnfmdriver.api.NslcmApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Responsible for providing access to VF-C REST APIs
 */
@Component
public class VfcRestApiProvider {
    static final String NSLCM_API_SERVICE_NAME = "nslcm";
    static final String NSLCM_API_VERION = "v1";
    static final String NSCATALOG_SERVICE_NAME = "catalog";
    static final String NSCATALOG_API_VERSION = "v1";
    private final MsbApiProvider msbApiProvider;

    @Autowired
    VfcRestApiProvider(MsbApiProvider msbApiProvider) {
        this.msbApiProvider = msbApiProvider;
    }

    /**
     * @return API to access VF-C for granting & LCN API
     */
    public NslcmApi getNsLcmApi() {
        return buildNslcmApiClient().createService(NslcmApi.class);
    }

    @VisibleForTesting
    ApiClient buildNslcmApiClient() {
        ApiClient apiClient = new ApiClient();
        String correctedUrl = fixIncorrectUrl();
        if (!correctedUrl.endsWith("/")) {
            correctedUrl = correctedUrl + "/";
        }
        apiClient.setAdapterBuilder(apiClient.getAdapterBuilder().baseUrl(correctedUrl));
        return apiClient;
    }

    /**
     * @return API to access VF-C catalog API
     */
    public VnfpackageApi getVfcCatalogApi() {
        return buildCatalogApiClient().createService(VnfpackageApi.class);
    }

    @VisibleForTesting
    org.onap.vfccatalog.ApiClient buildCatalogApiClient() {
        org.onap.vfccatalog.ApiClient vfcApiClient = new org.onap.vfccatalog.ApiClient();
        String microServiceUrl = msbApiProvider.getMicroServiceUrl(NSCATALOG_SERVICE_NAME, NSCATALOG_API_VERSION);
        if (!microServiceUrl.endsWith("/")) {
            microServiceUrl = microServiceUrl + "/";
        }
        vfcApiClient.setAdapterBuilder(vfcApiClient.getAdapterBuilder().baseUrl(microServiceUrl));
        return vfcApiClient;
    }

    /**
     * The swagger schema definition is not consistent with MSB info. The MSB reports
     * the base path /restapi/nsclm/v1 (correct) and the paths defined in swagger is
     * /nsclm/v1 making all API calls /restapi/nsclm/v1/nsclm/v1 (incorrect)
     *
     * @return
     */
    private String fixIncorrectUrl() {
        String urlInMsb = msbApiProvider.getMicroServiceUrl(NSLCM_API_SERVICE_NAME, NSLCM_API_VERION);
        //FIXME VF-C exposes multiple APIs in the single swagger definition, since the base path of different
        //API is different the some API calls are incorrectly prefixed
        //VF-C team refuses to fix this in Amsterdam https://jira.onap.org/browse/VFC-597?filter=-2
        return urlInMsb.replaceFirst("/nslcm/v1", "");
    }
}
