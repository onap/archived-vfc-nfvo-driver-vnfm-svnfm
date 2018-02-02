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

import com.nokia.cbam.catalog.v1.api.DefaultApi;
import com.nokia.cbam.lcm.v32.ApiClient;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.msb.sdk.discovery.entity.MicroServiceFullInfo;
import org.onap.msb.sdk.discovery.entity.NodeInfo;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.CbamTokenProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.TestBase;
import org.onap.vfccatalog.api.VnfpackageApi;
import org.onap.vnfmdriver.ApiException;
import org.onap.vnfmdriver.api.NslcmApi;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.springframework.core.env.Environment;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.RestApiProvider.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestRestApiProvider extends TestBase {
    @Mock
    private Environment environment;
    @Mock
    private CbamTokenProvider cbamTokenProvider;
    private MicroServiceFullInfo microServiceInfo = new MicroServiceFullInfo();
    private Set<NodeInfo> nodes = new HashSet<>();
    @InjectMocks
    private RestApiProvider restApiProvider;

    @Before
    public void init() {
        setField(RestApiProvider.class, "logger", logger);
        microServiceInfo.setNodes(nodes);
        restApiProvider = Mockito.spy(restApiProvider);
        setField(restApiProvider, "trustedCertificates", "mytrustedCertificates");
        setField(restApiProvider, "skipCertificateVerification", true);
        setField(restApiProvider, "skipHostnameVerification", true);
        setField(restApiProvider, "messageBusIp", "mymessageBusIp");
        setField(restApiProvider, "messageBusPort", "123");
        when(environment.getProperty(RestApiProvider.IP_MAP, String.class, "")).thenReturn("");
        when(environment.getProperty(RestApiProvider.VNFM_INFO_CACHE_EVICTION_IN_MS, Long.class, Long.valueOf(10 * 60 * 1000))).thenReturn(10 * 60 * 1000L);
        doReturn(msbClient).when(restApiProvider).getMsbClient();
    }

    /**
     * test Spring bean initialization: the ipMap property is parsed
     */
    @Test
    public void testParameterProcessing() throws Exception {
        when(environment.getProperty(RestApiProvider.IP_MAP, String.class, "")).thenReturn("1->2,2->3");
        //when
        restApiProvider.afterPropertiesSet();
        //verify
        assertEquals("2", restApiProvider.mapPrivateIpToPublicIp("1"));
        assertEquals("3", restApiProvider.mapPrivateIpToPublicIp("2"));
    }

    /**
     * test the static http client wrapping
     */
    @Test
    public void testHttpClientWrapping() {
        assertNotNull(restApiProvider.getHttpClient());
    }

    /**
     * test MSB client is created based on driver properties
     */
    @Test
    public void testMsbClient() {
        doCallRealMethod().when(restApiProvider).getMsbClient();
        //when
        MSBServiceClient msbClient = restApiProvider.getMsbClient();
        //verify
        assertEquals("mymessageBusIp:123", msbClient.getMsbSvrAddress());

    }

    /**
     * test VF-C NSLCM API retrieval
     */
    @Test
    public void testNsLcmApi() throws Exception {
        NodeInfo dockerAccessPoint = new NodeInfo();
        dockerAccessPoint.setIp("172.1.2.3");
        NodeInfo externalAccessPoint = new NodeInfo();
        externalAccessPoint.setIp("1.2.3.4");
        externalAccessPoint.setPort("1234");
        microServiceInfo.setUrl("/lead/nslcm/v1");
        when(environment.getProperty(RestApiProvider.IP_MAP, String.class, "")).thenReturn("1.2.3.4->2.3.4.5");
        nodes.add(dockerAccessPoint);
        nodes.add(externalAccessPoint);
        when(msbClient.queryMicroServiceInfo(NSLCM_API_SERVICE_NAME, NSLCM_API_VERION)).thenReturn(microServiceInfo);

        //when
        NslcmApi nsLcmApi = restApiProvider.getNsLcmApi();
        //verify
        assertEquals("http://1.2.3.4:1234/lead", nsLcmApi.getApiClient().getBasePath());
        assertNull(nsLcmApi.getApiClient().getSslCaCert());
        assertEquals(0, nsLcmApi.getApiClient().getAuthentications().size());
    }

    /**
     * test VF-C NSLCM API retrieval
     */
    @Test
    public void testQueryVnfmInfo() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        restApiProvider.afterPropertiesSet();
        VnfmInfo expectedVnfmInfo = Mockito.mock(VnfmInfo.class);
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        //when
        VnfmInfo vnfmInfo = restApiProvider.queryVnfmInfo(VNFM_ID);
        //verify
        assertEquals(expectedVnfmInfo, vnfmInfo);
        //when
        VnfmInfo vnfmInfo2 = restApiProvider.queryVnfmInfo(VNFM_ID);
        //verify (VF-C API not called again)
        verify(nsLcmApi).queryVnfmInfo(VNFM_ID);
    }

    /**
     * test VF-C catalog API retrieval
     */
    @Test
    public void testNsCatalogApi() throws Exception {
        NodeInfo dockerAccessPoint = new NodeInfo();
        dockerAccessPoint.setIp("172.1.2.3");
        NodeInfo externalAccessPoint = new NodeInfo();
        externalAccessPoint.setIp("1.2.3.4");
        externalAccessPoint.setPort("1234");
        microServiceInfo.setUrl("/lead/v1");
        when(environment.getProperty(RestApiProvider.IP_MAP, String.class, "")).thenReturn("1.2.3.4->2.3.4.5");
        nodes.add(dockerAccessPoint);
        nodes.add(externalAccessPoint);

        when(msbClient.queryMicroServiceInfo(NSCATALOG_SERVICE_NAME, NSLCM_API_VERION)).thenReturn(microServiceInfo);
        //when
        VnfpackageApi nsCatalogApi = restApiProvider.getOnapCatalogApi();
        //verify
        assertEquals("http://1.2.3.4:1234/lead/v1", nsCatalogApi.getApiClient().getBasePath());
        assertNull(nsCatalogApi.getApiClient().getSslCaCert());
        assertEquals(0, nsCatalogApi.getApiClient().getAuthentications().size());
    }

    /**
     * test CBAM LCM API retrieval without SSL verification
     */
    @Test
    public void testCbamLcmApi() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        restApiProvider.afterPropertiesSet();
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        expectedVnfmInfo.setUrl("https://cbamUrl:123/d");
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        //when
        VnfsApi cbamLcmApi = restApiProvider.getCbamLcmApi(VNFM_ID);
        //verify
        ApiClient apiClient = cbamLcmApi.getApiClient();
        assertEquals("https://cbamUrl:123/d", apiClient.getBasePath());
        assertNull(apiClient.getSslCaCert());
        assertEquals("myToken", ((com.nokia.cbam.lcm.v32.auth.OAuth) apiClient.getAuthentication("OauthClient")).getAccessToken());
        assertEquals(2, cbamLcmApi.getApiClient().getAuthentications().size());
        assertTrue(!cbamLcmApi.getApiClient().isVerifyingSsl());
    }

    /**
     * test CBAM LCM API retrieval with SSL verification
     */
    @Test
    public void testCbamLcmApiWithSslVerfy() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        restApiProvider.afterPropertiesSet();
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        expectedVnfmInfo.setUrl("https://cbamUrl:123/d");
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        setField(restApiProvider, "skipCertificateVerification", false);
        setField(restApiProvider, "trustedCertificates", Base64.getEncoder().encodeToString(TestUtil.loadFile("unittests/sample.cert.pem")));
        //when
        VnfsApi cbamLcmApi = restApiProvider.getCbamLcmApi(VNFM_ID);
        //verify
        ApiClient apiClient = cbamLcmApi.getApiClient();
        assertEquals("https://cbamUrl:123/d", apiClient.getBasePath());
        assertNotNull(apiClient.getSslCaCert());
        assertEquals("myToken", ((com.nokia.cbam.lcm.v32.auth.OAuth) apiClient.getAuthentication("OauthClient")).getAccessToken());
        assertEquals(2, cbamLcmApi.getApiClient().getAuthentications().size());
        assertTrue(cbamLcmApi.getApiClient().isVerifyingSsl());
    }

    /**
     * test CBAM Catalog API retrieval without SSL verification
     */
    @Test
    public void testCbamCatalogApi() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        restApiProvider.afterPropertiesSet();
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        when(driverProperties.getCbamCatalogUrl()).thenReturn("https://1.2.3.4/path");
        //when
        DefaultApi cbamCatalogApi = restApiProvider.getCbamCatalogApi(VNFM_ID);
        //verify
        com.nokia.cbam.catalog.v1.ApiClient apiClient = cbamCatalogApi.getApiClient();
        assertEquals("https://1.2.3.4/path", apiClient.getBasePath());
        assertNull(apiClient.getSslCaCert());
        assertEquals("myToken", ((com.nokia.cbam.catalog.v1.auth.OAuth) apiClient.getAuthentication("OauthClient")).getAccessToken());
        assertEquals(2, cbamCatalogApi.getApiClient().getAuthentications().size());
        assertTrue(!cbamCatalogApi.getApiClient().isVerifyingSsl());
    }

    /**
     * test CBAM Catalog API retrieval with SSL verification
     */
    @Test
    public void testCbamCatalogApiWithSslVerfy() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        restApiProvider.afterPropertiesSet();
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        when(driverProperties.getCbamCatalogUrl()).thenReturn("https://1.2.3.4/path");
        setField(restApiProvider, "skipCertificateVerification", false);
        setField(restApiProvider, "trustedCertificates", Base64.getEncoder().encodeToString(TestUtil.loadFile("unittests/sample.cert.pem")));
        //when
        DefaultApi cbamLcmApi = restApiProvider.getCbamCatalogApi(VNFM_ID);
        //verify
        com.nokia.cbam.catalog.v1.ApiClient apiClient = cbamLcmApi.getApiClient();
        assertEquals("https://1.2.3.4/path", apiClient.getBasePath());
        assertNotNull(apiClient.getSslCaCert());
        assertEquals("myToken", ((com.nokia.cbam.catalog.v1.auth.OAuth) apiClient.getAuthentication("OauthClient")).getAccessToken());
        assertEquals(2, cbamLcmApi.getApiClient().getAuthentications().size());
        assertTrue(cbamLcmApi.getApiClient().isVerifyingSsl());
    }

    /**
     * test CBAM Lcn API retrieval without SSL verification
     */
    @Test
    public void testCbamLcnApi() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        restApiProvider.afterPropertiesSet();
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        when(driverProperties.getCbamLcnUrl()).thenReturn("https://1.2.3.4/path");
        //when
        SubscriptionsApi cbamLcnApi = restApiProvider.getCbamLcnApi(VNFM_ID);
        //verify
        com.nokia.cbam.lcn.v32.ApiClient apiClient = cbamLcnApi.getApiClient();
        assertEquals("https://1.2.3.4/path", apiClient.getBasePath());
        assertNull(apiClient.getSslCaCert());
        assertEquals("myToken", ((com.nokia.cbam.lcn.v32.auth.OAuth) apiClient.getAuthentication("OauthClient")).getAccessToken());
        assertEquals(2, cbamLcnApi.getApiClient().getAuthentications().size());
        assertTrue(!cbamLcnApi.getApiClient().isVerifyingSsl());
    }

    /**
     * test CBAM Lcn API retrieval with SSL verification
     */
    @Test
    public void testCbamLcnApiWithSslVerfy() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        restApiProvider.afterPropertiesSet();
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        when(driverProperties.getCbamLcnUrl()).thenReturn("https://1.2.3.4/path");
        setField(restApiProvider, "skipCertificateVerification", false);
        setField(restApiProvider, "trustedCertificates", Base64.getEncoder().encodeToString(TestUtil.loadFile("unittests/sample.cert.pem")));
        //when
        SubscriptionsApi cbamLcnApi = restApiProvider.getCbamLcnApi(VNFM_ID);
        //verify
        com.nokia.cbam.lcn.v32.ApiClient apiClient = cbamLcnApi.getApiClient();
        assertEquals("https://1.2.3.4/path", apiClient.getBasePath());
        assertNotNull(apiClient.getSslCaCert());
        assertEquals("myToken", ((com.nokia.cbam.lcn.v32.auth.OAuth) apiClient.getAuthentication("OauthClient")).getAccessToken());
        assertEquals(2, cbamLcnApi.getApiClient().getAuthentications().size());
        assertTrue(cbamLcnApi.getApiClient().isVerifyingSsl());
    }

    /**
     * test CBAM operation exeution API retrieval without SSL verification
     */
    @Test
    public void testCbamOpexApi() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        restApiProvider.afterPropertiesSet();
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        expectedVnfmInfo.setUrl("https://cbamUrl:123/d");
        //when
        OperationExecutionsApi cbamLcnApi = restApiProvider.getCbamOperationExecutionApi(VNFM_ID);
        //verify
        ApiClient apiClient = cbamLcnApi.getApiClient();
        assertEquals("https://cbamUrl:123/d", apiClient.getBasePath());
        assertNull(apiClient.getSslCaCert());
        assertEquals("myToken", ((com.nokia.cbam.lcm.v32.auth.OAuth) apiClient.getAuthentication("OauthClient")).getAccessToken());
        assertEquals(2, cbamLcnApi.getApiClient().getAuthentications().size());
        assertTrue(!cbamLcnApi.getApiClient().isVerifyingSsl());
    }

    /**
     * test CBAM operation execution API retrieval with SSL verification
     */
    @Test
    public void testCbamOpexApiWithSslVerfy() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        restApiProvider.afterPropertiesSet();
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        setField(restApiProvider, "skipCertificateVerification", false);
        setField(restApiProvider, "trustedCertificates", Base64.getEncoder().encodeToString(TestUtil.loadFile("unittests/sample.cert.pem")));
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        expectedVnfmInfo.setUrl("https://cbamUrl:123/d");
        //when
        OperationExecutionsApi cbamLcnApi = restApiProvider.getCbamOperationExecutionApi(VNFM_ID);
        //verify
        ApiClient apiClient = cbamLcnApi.getApiClient();
        assertEquals("https://cbamUrl:123/d", apiClient.getBasePath());
        assertNotNull(apiClient.getSslCaCert());
        assertEquals("myToken", ((com.nokia.cbam.lcm.v32.auth.OAuth) apiClient.getAuthentication("OauthClient")).getAccessToken());
        assertEquals(2, cbamLcnApi.getApiClient().getAuthentications().size());
        assertTrue(cbamLcnApi.getApiClient().isVerifyingSsl());
    }

    /**
     * if unable to query VNFM info the error is propagated
     */
    @Test
    public void testunableToQueryVnfmInfo() throws Exception {
        doReturn(nsLcmApi).when(restApiProvider).getNsLcmApi();
        NodeInfo dockerAccessPoint = new NodeInfo();
        dockerAccessPoint.setIp("172.1.2.3");
        NodeInfo externalAccessPoint = new NodeInfo();
        externalAccessPoint.setIp("1.2.3.4");
        externalAccessPoint.setPort("1234");
        microServiceInfo.setUrl("/lead/nslcm/v1");
        when(environment.getProperty(RestApiProvider.IP_MAP, String.class, "")).thenReturn("1.2.3.4->2.3.4.5");
        nodes.add(dockerAccessPoint);
        nodes.add(externalAccessPoint);
        MSBServiceClient sss = restApiProvider.getMsbClient();
        when(msbClient.queryMicroServiceInfo(NSLCM_API_SERVICE_NAME, NSLCM_API_VERION)).thenReturn(microServiceInfo);
        ApiException expectedException = new ApiException();
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenThrow(expectedException);
        restApiProvider.afterPropertiesSet();
        //when
        try {
            restApiProvider.queryVnfmInfo(VNFM_ID);
            fail();
        } catch (Exception e) {
            assertEquals("Unable to query VNFM info for myVnfId", e.getMessage());
            verify(logger).error(eq("Unable to query VNFM info for myVnfId"), Mockito.any(Exception.class));
        }
    }


    /**
     * error is propagated if no suitable microservice endpoint is found
     */
    @Test
    public void testNoSuitabelMicroservice() throws Exception {
        NodeInfo dockerAccessPoint = new NodeInfo();
        dockerAccessPoint.setIp("172.1.2.3");
        microServiceInfo.setServiceName("serviceName");
        microServiceInfo.setVersion("v1");
        microServiceInfo.setUrl("/lead/nslcm/v1");
        when(environment.getProperty(RestApiProvider.IP_MAP, String.class, "")).thenReturn("1.2.3.4->2.3.4.5");
        nodes.add(dockerAccessPoint);
        when(msbClient.queryMicroServiceInfo(NSLCM_API_SERVICE_NAME, NSLCM_API_VERION)).thenReturn(microServiceInfo);

        //when
        try {
            restApiProvider.getNsLcmApi();
            fail();
        } catch (Exception e) {
            assertEquals("The serviceName service with v1 does not have any valid nodes[172.1.2.3:null  ttl:]", e.getMessage());
            verify(logger).error("The serviceName service with v1 does not have any valid nodes[172.1.2.3:null  ttl:]");
        }
    }


    /**
     * if unable to get microservice info the error is propagated
     */
    @Test
    public void testUnableQueryMicroserviInfo() throws Exception {
        RouteException expectedException = new RouteException();
        when(msbClient.queryMicroServiceInfo(NSLCM_API_SERVICE_NAME, NSLCM_API_VERION)).thenThrow(expectedException);

        //when
        try {
            restApiProvider.getNsLcmApi();
            fail();
        } catch (Exception e) {
            assertEquals("Unable to get micro service URL for nslcm with version v1", e.getMessage());
            verify(logger).error("Unable to get micro service URL for nslcm with version v1", expectedException);
        }
    }
}
