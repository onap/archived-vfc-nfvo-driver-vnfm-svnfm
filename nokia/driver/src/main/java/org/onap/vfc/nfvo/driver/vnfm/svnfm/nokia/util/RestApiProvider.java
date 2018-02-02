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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.nokia.cbam.catalog.v1.api.DefaultApi;
import com.nokia.cbam.lcm.v32.ApiClient;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.msb.sdk.discovery.entity.MicroServiceFullInfo;
import org.onap.msb.sdk.discovery.entity.NodeInfo;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.CbamTokenProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.DriverProperties;
import org.onap.vfccatalog.api.VnfpackageApi;
import org.onap.vnfmdriver.api.NslcmApi;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.lang.Integer.valueOf;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.fatalFailure;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provides APIs to access external systems
 */
@Component
public class RestApiProvider implements InitializingBean {
    public static final String NOKIA_LCM_API_VERSION = "3.2";
    public static final String NOKIA_LCN_API_VERSION = "3.2";
    public static final String NSLCM_API_SERVICE_NAME = "nslcm";
    public static final String NSLCM_API_VERION = "v1";
    public static final String NSCATALOG_SERVICE_NAME = "catalog";
    public static final String NSCATALOG_API_VERSION = "v1";
    public static final String VNFM_INFO_CACHE_EVICTION_IN_MS = "vnfmInfoCacheEvictionInMs";
    static final String IP_MAP = "ipMap";
    private static org.slf4j.Logger logger = getLogger(RestApiProvider.class);
    @Value("${trustedCertificates}")
    private String trustedCertificates;
    @Value("${skipCertificateVerification}")
    private boolean skipCertificateVerification;
    @Value("${skipHostnameVerification}")
    private boolean skipHostnameVerification;
    @Value("${messageBusIp}")
    private String messageBusIp;
    @Value("${messageBusPort}")
    private String messageBusPort;
    @Autowired
    private DriverProperties driverProperties;
    @Autowired
    private CbamTokenProvider tokenProvider;
    @Autowired
    private Environment environment;
    private LoadingCache<String, VnfmInfo> vnfmInfoCache;
    private Map<String, String> ipMap = new HashMap<>();

    /**
     * After the Bean has been initialized the IP mapping and the VMFM cache is initialized
     * It is done in this phase because the logic requires the the @Value anoted fields to
     * be specified
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Splitter.on(",").trimResults().omitEmptyStrings().split(environment.getProperty(IP_MAP, String.class, "")).forEach(new Consumer<String>() {
            @Override
            public void accept(String item) {
                ArrayList<String> ip = Lists.newArrayList(Splitter.on("->").trimResults().split(item));
                ipMap.put(ip.get(0), ip.get(1));
            }
        });
        vnfmInfoCache = CacheBuilder.newBuilder().expireAfterWrite(environment.getProperty(VNFM_INFO_CACHE_EVICTION_IN_MS, Long.class, Long.valueOf(10 * 60 * 1000)), TimeUnit.MILLISECONDS).concurrencyLevel(1).build(new CacheLoader<String, VnfmInfo>() {
            @Override
            public VnfmInfo load(String vnfmId) throws Exception {
                NslcmApi nsLcmApi = getNsLcmApi();
                return nsLcmApi.queryVnfmInfo(vnfmId);
            }
        });
    }

    /**
     * Wraps the static call (required for being able to test)
     *
     * @return the default HTTP client
     */
    public CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    /*
     * @param vnfmId the identifier of the VNFM
     * @return the cached VNFM
     */
    public VnfmInfo queryVnfmInfo(String vnfmId) {
        try {
            return vnfmInfoCache.get(vnfmId);
        } catch (Exception e) {
            throw fatalFailure(logger, "Unable to query VNFM info for " + vnfmId, e);
        }
    }

    /**
     * @return API to access VF-C for granting & LCN API
     */
    public NslcmApi getNsLcmApi() {
        org.onap.vnfmdriver.ApiClient apiClient = new org.onap.vnfmdriver.ApiClient();
        String urlInMsb = getMicroServiceUrl(NSLCM_API_SERVICE_NAME, NSLCM_API_VERION);
        //FIXME the swagger schema definition is not consistent with MSB info
        //MSB reports the base path /api/nsclm/v1 (correct) and the paths defined in swagger
        // is /nsclm/v1 making all API calls /api/nsclm/v1/nsclm/v1
        String correctedUrl = urlInMsb.replaceFirst("/nslcm/v1", "");
        apiClient.setBasePath(correctedUrl);
        return new NslcmApi(apiClient);
    }

    /**
     * @return API to access VF-C catalog API
     */
    public VnfpackageApi getOnapCatalogApi() {
        org.onap.vfccatalog.ApiClient vfcApiClient = new org.onap.vfccatalog.ApiClient();
        vfcApiClient.setBasePath(getMicroServiceUrl(NSCATALOG_SERVICE_NAME, NSCATALOG_API_VERSION));
        return new VnfpackageApi(vfcApiClient);
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

    /**
     * @param vnfmId the identifier of the VNFM
     * @return API to access the operation executions
     */
    public OperationExecutionsApi getCbamOperationExecutionApi(String vnfmId) {
        return new OperationExecutionsApi(getLcmApiClient(vnfmId));
    }

    /**
     * @return API to access ONAP MSB
     */
    public MSBServiceClient getMsbClient() {
        return new MSBServiceClient(messageBusIp, valueOf(messageBusPort));
    }

    /**
     * Map IP addresses based on configuration parameter ipMap
     *
     * @param ip the original IP address
     * @return the mapped IP addresss
     */
    public String mapPrivateIpToPublicIp(String ip) {
        return ipMap.getOrDefault(ip, ip);
    }

    private ApiClient getLcmApiClient(String vnfmId) {
        VnfmInfo vnfmInfo = queryVnfmInfo(vnfmId);
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

    private String getMicroServiceUrl(String name, String version) {
        MicroServiceFullInfo microServiceFullInfo = getMicroserviceInfo(name, version);
        String protocol = "http://"; //FIXME the enable_ssl field should be used, but it is not available in SDK
        String ipAnPort = getNodeIpAnPort(microServiceFullInfo);
        //the field name in A&AI is misleading the URL is relative path postfixed to http(s)://ip:port
        String fullUrl = protocol + ipAnPort + microServiceFullInfo.getUrl();
        return fullUrl;
    }

    private MicroServiceFullInfo getMicroserviceInfo(String name, String version) throws RuntimeException {
        try {
            return getMsbClient().queryMicroServiceInfo(name, version);
        } catch (RouteException e) {
            throw fatalFailure(logger, "Unable to get micro service URL for " + name + " with version " + version, e);
        }
    }

    private String getNodeIpAnPort(MicroServiceFullInfo microServiceFullInfo) {
        for (NodeInfo nodeInfo : microServiceFullInfo.getNodes()) {
            if (!nodeInfo.getIp().startsWith("172.")) { // FIXME how to know which of the multiple addresses to use?
                return mapPrivateIpToPublicIp(nodeInfo.getIp()) + ":" + nodeInfo.getPort();
            }
        }
        throw fatalFailure(logger, "The " + microServiceFullInfo.getServiceName() + " service with " + microServiceFullInfo.getVersion() + " does not have any valid nodes" + microServiceFullInfo.getNodes());
    }
}
