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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct;

import com.google.common.annotations.VisibleForTesting;
import okhttp3.Credentials;
import okhttp3.Request;
import org.onap.aai.ApiClient;
import org.onap.aai.api.CloudInfrastructureApi;
import org.onap.aai.api.ExternalSystemApi;
import org.onap.aai.api.NetworkApi;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.MsbApiProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager.SERVICE_NAME;

/**
 * Responsible for providing access to AAI APIs.
 * Handles authentication and mandatory parameters.
 */
@Component
public class AAIRestApiProvider {
    private final MsbApiProvider msbApiProvider;
    private final AaiSecurityProvider aaiSecurityProvider;
    @Value("${aaiUsername}")
    private String aaiUsername;
    @Value("${aaiPassword}")
    private String aaiPassword;

    @Autowired
    AAIRestApiProvider(MsbApiProvider msbApiProvider, AaiSecurityProvider aaiSecurityProvider) {
        this.msbApiProvider = msbApiProvider;
        this.aaiSecurityProvider = aaiSecurityProvider;
    }

    /**
     * @return API to access the cloud infrastructure
     */
    public CloudInfrastructureApi getCloudInfrastructureApi() {
        return buildApiClient(AAIService.CLOUD).createService(CloudInfrastructureApi.class);
    }

    /**
     * @return API to access the external systems
     */
    public ExternalSystemApi getExternalSystemApi() {
        return buildApiClient(AAIService.ESR).createService(ExternalSystemApi.class);
    }

    /**
     * @return API to access the networking
     */
    public NetworkApi getNetworkApi() {
        return buildApiClient(AAIService.NETWORK).createService(NetworkApi.class);

    }

    @VisibleForTesting
    ApiClient buildApiClient(AAIService service) {
        ApiClient apiClient = new ApiClient();
        apiClient.getOkBuilder().sslSocketFactory(aaiSecurityProvider.buildSSLSocketFactory(), aaiSecurityProvider.buildTrustManager());
        apiClient.getOkBuilder().hostnameVerifier(aaiSecurityProvider.buildHostnameVerifier());
        apiClient.getOkBuilder().addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("X-FromAppId", SERVICE_NAME).build();
            return chain.proceed(request);
        });
        apiClient.getOkBuilder().authenticator((route, response) -> {
            String credential = Credentials.basic(aaiUsername, aaiPassword);
            return response.request().newBuilder().header("Authorization", credential).build();
        });
        String url = msbApiProvider.getMicroServiceUrl(service.getServiceName(), "v11");
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        apiClient.getAdapterBuilder().baseUrl(url);
        return apiClient;
    }

    enum AAIService {
        NETWORK {
            String getServiceName() {
                return "aai-network";
            }
        },
        ESR {
            String getServiceName() {
                return "aai-externalSystem";
            }
        },
        CLOUD {
            String getServiceName() {
                return "aai-cloudInfrastructure";
            }
        };

        abstract String getServiceName();
    }
}
