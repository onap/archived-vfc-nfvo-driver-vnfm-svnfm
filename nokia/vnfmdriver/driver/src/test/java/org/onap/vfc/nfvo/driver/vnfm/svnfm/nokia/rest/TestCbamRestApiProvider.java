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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.rest;

import com.nokia.cbam.catalog.v1.api.DefaultApi;
import com.nokia.cbam.lcm.v32.ApiClient;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.msb.sdk.discovery.entity.MicroServiceFullInfo;
import org.onap.msb.sdk.discovery.entity.NodeInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.CbamTokenProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.impl.TestBase;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.TestUtil;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.springframework.core.env.Environment;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TestCbamRestApiProvider extends TestBase {
    @Mock
    private Environment environment;
    @Mock
    private CbamTokenProvider cbamTokenProvider;
    private MicroServiceFullInfo microServiceInfo = new MicroServiceFullInfo();
    private Set<NodeInfo> nodes = new HashSet<>();

    private CbamRestApiProvider cbamRestApiProvider;

    @Before
    public void init() {
        microServiceInfo.setNodes(nodes);
        CbamRestApiProvider real = new CbamRestApiProvider(driverProperties, cbamTokenProvider, vnfmInfoProvider);
        setField(real, "trustedCertificates", "mytrustedCertificates");
        setField(real, "skipCertificateVerification", true);
        cbamRestApiProvider = spy(real);
        when(environment.getProperty(IpMappingProvider.IP_MAP, String.class, "")).thenReturn("");
        when(environment.getProperty(VnfmInfoProvider.VNFM_INFO_CACHE_EVICTION_IN_MS, Long.class, Long.valueOf(10 * 60 * 1000))).thenReturn(10 * 60 * 1000L);
    }

    /**
     * test CBAM LCM API retrieval without SSL verification
     */
    @Test
    public void testCbamLcmApi() throws Exception {
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        expectedVnfmInfo.setUrl("https://cbamUrl:123/d");
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        //when
        VnfsApi cbamLcmApi = cbamRestApiProvider.getCbamLcmApi(VNFM_ID);
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
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        expectedVnfmInfo.setUrl("https://cbamUrl:123/d");
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        setField(cbamRestApiProvider, "skipCertificateVerification", false);
        setField(cbamRestApiProvider, "trustedCertificates", Base64.getEncoder().encodeToString(TestUtil.loadFile("unittests/sample.cert.pem")));
        //when
        VnfsApi cbamLcmApi = cbamRestApiProvider.getCbamLcmApi(VNFM_ID);
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
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        when(driverProperties.getCbamCatalogUrl()).thenReturn("https://1.2.3.4/path");
        //when
        DefaultApi cbamCatalogApi = cbamRestApiProvider.getCbamCatalogApi(VNFM_ID);
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
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        when(driverProperties.getCbamCatalogUrl()).thenReturn("https://1.2.3.4/path");
        setField(cbamRestApiProvider, "skipCertificateVerification", false);
        setField(cbamRestApiProvider, "trustedCertificates", Base64.getEncoder().encodeToString(TestUtil.loadFile("unittests/sample.cert.pem")));
        //when
        DefaultApi cbamLcmApi = cbamRestApiProvider.getCbamCatalogApi(VNFM_ID);
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
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        when(driverProperties.getCbamLcnUrl()).thenReturn("https://1.2.3.4/path");
        //when
        SubscriptionsApi cbamLcnApi = cbamRestApiProvider.getCbamLcnApi(VNFM_ID);
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
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        when(driverProperties.getCbamLcnUrl()).thenReturn("https://1.2.3.4/path");
        setField(cbamRestApiProvider, "skipCertificateVerification", false);
        setField(cbamRestApiProvider, "trustedCertificates", Base64.getEncoder().encodeToString(TestUtil.loadFile("unittests/sample.cert.pem")));
        //when
        SubscriptionsApi cbamLcnApi = cbamRestApiProvider.getCbamLcnApi(VNFM_ID);
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
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        expectedVnfmInfo.setUrl("https://cbamUrl:123/d");
        //when
        OperationExecutionsApi cbamLcnApi = cbamRestApiProvider.getCbamOperationExecutionApi(VNFM_ID);
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
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn("myToken");
        setField(cbamRestApiProvider, "skipCertificateVerification", false);
        setField(cbamRestApiProvider, "trustedCertificates", Base64.getEncoder().encodeToString(TestUtil.loadFile("unittests/sample.cert.pem")));
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(nsLcmApi.queryVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        expectedVnfmInfo.setUrl("https://cbamUrl:123/d");
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        //when
        OperationExecutionsApi cbamLcnApi = cbamRestApiProvider.getCbamOperationExecutionApi(VNFM_ID);
        //verify
        ApiClient apiClient = cbamLcnApi.getApiClient();
        assertEquals("https://cbamUrl:123/d", apiClient.getBasePath());
        assertNotNull(apiClient.getSslCaCert());
        assertEquals("myToken", ((com.nokia.cbam.lcm.v32.auth.OAuth) apiClient.getAuthentication("OauthClient")).getAccessToken());
        assertEquals(2, cbamLcnApi.getApiClient().getAuthentications().size());
        assertTrue(cbamLcnApi.getApiClient().isVerifyingSsl());
    }
}
