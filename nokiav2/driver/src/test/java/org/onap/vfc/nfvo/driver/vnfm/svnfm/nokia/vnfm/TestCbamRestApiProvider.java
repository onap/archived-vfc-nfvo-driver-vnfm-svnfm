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

import com.nokia.cbam.catalog.v1.api.DefaultApi;
import com.nokia.cbam.lcm.v32.ApiClient;
import com.nokia.cbam.lcm.v32.api.OperationExecutionsApi;
import com.nokia.cbam.lcm.v32.api.VnfsApi;
import com.nokia.cbam.lcn.v32.api.SubscriptionsApi;
import java.util.ArrayList;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.Interceptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.msb.model.MicroServiceFullInfo;
import org.onap.msb.model.NodeInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.GenericExternalSystemInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.IpMappingProvider;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.springframework.core.env.Environment;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.*;

class ResultCaptor<T> implements Answer {
    private T result = null;

    public T getResult() {
        return result;
    }

    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        result = (T) invocationOnMock.callRealMethod();
        return result;
    }
}

public class TestCbamRestApiProvider extends TestBase {
    @Mock
    private Environment environment;
    @Mock
    private CbamTokenProvider cbamTokenProvider;
    @Mock
    private MicroServiceFullInfo microServiceInfo = new MicroServiceFullInfo();
    @Mock
    private Interceptor interceptor;
    @Mock
    private HostnameVerifier hostnameVerifier;
    private java.util.List<NodeInfo> nodes = new ArrayList<>();

    private CbamRestApiProvider cbamRestApiProvider;
    private CbamSecurityProvider cbamSecurityProvider;

    @Before
    public void init() {
        CbamSecurityProvider real = new CbamSecurityProvider();
        setFieldWithPropertyAnnotation(real, "${skipCertificateVerification}", true);
        setFieldWithPropertyAnnotation(real, "${skipHostnameVerification}", true);
        cbamSecurityProvider = spy(real);
        microServiceInfo.setNodes(nodes);
        cbamRestApiProvider = new CbamRestApiProvider(driverProperties, cbamTokenProvider, vnfmInfoProvider, cbamSecurityProvider);
        when(environment.getProperty(IpMappingProvider.IP_MAP, String.class, "")).thenReturn("");
        when(environment.getProperty(GenericExternalSystemInfoProvider.VNFM_INFO_CACHE_EVICTION_IN_MS, Long.class, Long.valueOf(10 * 60 * 1000))).thenReturn(10 * 60 * 1000L);
    }

    /**
     * test CBAM LCM API
     */
    @Test
    public void testCbamLcmApi() throws Exception {
        VnfmInfo expectedVnfmInfo = new VnfmInfo();
        when(vnfmInfoProvider.getVnfmInfo(VNFM_ID)).thenReturn(expectedVnfmInfo);
        expectedVnfmInfo.setUrl("https://cbamurl:123/d/");
        ResultCaptor<SSLSocketFactory> sslSocketFactoryResultCaptor = new ResultCaptor<>();
        doAnswer(sslSocketFactoryResultCaptor).when(cbamSecurityProvider).buildSSLSocketFactory();
        when(cbamSecurityProvider.buildHostnameVerifier()).thenReturn(hostnameVerifier);
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn(interceptor);
        //when
        ApiClient cbamLcmApi = cbamRestApiProvider.buildLcmApiClient(VNFM_ID);
        //verify
        assertEquals("https://cbamurl:123/d/", cbamLcmApi.getAdapterBuilder().build().baseUrl().toString());
        assertEquals(sslSocketFactoryResultCaptor.getResult(), cbamLcmApi.getOkBuilder().build().sslSocketFactory());
        Map<String, Interceptor> apiAuthorizations = cbamLcmApi.getApiAuthorizations();
        assertEquals(1, apiAuthorizations.size());
        assertEquals(interceptor, apiAuthorizations.values().iterator().next());
        assertEquals(hostnameVerifier, cbamLcmApi.getOkBuilder().build().hostnameVerifier());
    }

    /**
     * test CBAM catalog API
     */
    @Test
    public void testCbamCatalogApi() throws Exception {
        ResultCaptor<SSLSocketFactory> sslSocketFactoryResultCaptor = new ResultCaptor<>();
        doAnswer(sslSocketFactoryResultCaptor).when(cbamSecurityProvider).buildSSLSocketFactory();
        when(cbamSecurityProvider.buildHostnameVerifier()).thenReturn(hostnameVerifier);
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn(interceptor);
        when(driverProperties.getCbamCatalogUrl()).thenReturn("https://cbamurl:123/d/");
        //when
        com.nokia.cbam.catalog.v1.ApiClient cbamLcmApi = cbamRestApiProvider.buildCatalogApiClient(VNFM_ID);
        //verify
        String actual = cbamLcmApi.getAdapterBuilder().build().baseUrl().toString();
        assertEquals("https://cbamurl:123/d/", actual);
        assertEquals(sslSocketFactoryResultCaptor.getResult(), cbamLcmApi.getOkBuilder().build().sslSocketFactory());
        Map<String, Interceptor> apiAuthorizations = cbamLcmApi.getApiAuthorizations();
        assertEquals(1, apiAuthorizations.size());
        assertEquals(interceptor, apiAuthorizations.values().iterator().next());
        assertEquals(hostnameVerifier, cbamLcmApi.getOkBuilder().build().hostnameVerifier());
    }

    /**
     * test CBAM LCN API
     */
    @Test
    public void testCbamLcnApi() throws Exception {
        ResultCaptor<SSLSocketFactory> sslSocketFactoryResultCaptor = new ResultCaptor<>();
        doAnswer(sslSocketFactoryResultCaptor).when(cbamSecurityProvider).buildSSLSocketFactory();
        when(cbamSecurityProvider.buildHostnameVerifier()).thenReturn(hostnameVerifier);
        when(cbamTokenProvider.getToken(VNFM_ID)).thenReturn(interceptor);
        when(driverProperties.getCbamLcnUrl()).thenReturn("https://cbamurl:123/d/");
        //when
        com.nokia.cbam.lcn.v32.ApiClient cbamLcmApi = cbamRestApiProvider.buildLcnApiClient(VNFM_ID);
        //verify
        String actual = cbamLcmApi.getAdapterBuilder().build().baseUrl().toString();
        assertEquals("https://cbamurl:123/d/", actual);
        assertEquals(sslSocketFactoryResultCaptor.getResult(), cbamLcmApi.getOkBuilder().build().sslSocketFactory());
        Map<String, Interceptor> apiAuthorizations = cbamLcmApi.getApiAuthorizations();
        assertEquals(1, apiAuthorizations.size());
        assertEquals(interceptor, apiAuthorizations.values().iterator().next());
        assertEquals(hostnameVerifier, cbamLcmApi.getOkBuilder().build().hostnameVerifier());
    }

    /**
     * Test API wrapping for Catalog
     * (questionable benefit [ this is more less ensured by Java type safety) ]
     */
    @Test
    @Useless
    public void testCatalogAPiWrapping() {
        com.nokia.cbam.catalog.v1.ApiClient c = Mockito.mock(com.nokia.cbam.catalog.v1.ApiClient.class);
        class TestClasss extends CbamRestApiProvider {
            public TestClasss() {
                super(driverProperties, cbamTokenProvider, vnfmInfoProvider, cbamSecurityProvider);
            }

            @Override
            com.nokia.cbam.catalog.v1.ApiClient buildCatalogApiClient(String vnfmId) {
                return c;
            }
        }
        DefaultApi defaultApi = Mockito.mock(DefaultApi.class);
        //when
        when(c.createService(DefaultApi.class)).thenReturn(defaultApi);
        //verify
        TestClasss testInstnace = new TestClasss();
        assertNotNull(testInstnace.getCbamCatalogApi(VNFM_ID));
        assertEquals(defaultApi, testInstnace.getCbamCatalogApi(VNFM_ID));
    }

    /**
     * Test API wrapping for LCN
     * (questionable benefit [ this is more less ensured by Java type safety) ]
     */
    @Test
    @Useless
    public void testLcmAPiWrapping() {
        com.nokia.cbam.lcn.v32.ApiClient c = Mockito.mock(com.nokia.cbam.lcn.v32.ApiClient.class);
        class TestClasss extends CbamRestApiProvider {
            public TestClasss() {
                super(driverProperties, cbamTokenProvider, vnfmInfoProvider, cbamSecurityProvider);
            }

            @Override
            com.nokia.cbam.lcn.v32.ApiClient buildLcnApiClient(String vnfmId) {
                return c;
            }
        }
        SubscriptionsApi defaultApi = Mockito.mock(SubscriptionsApi.class);
        //when
        when(c.createService(SubscriptionsApi.class)).thenReturn(defaultApi);
        //verify
        TestClasss testInstnace = new TestClasss();
        assertNotNull(testInstnace.getCbamLcnApi(VNFM_ID));
        assertEquals(defaultApi, testInstnace.getCbamLcnApi(VNFM_ID));
    }

    /**
     * Test API wrapping for LCM
     * (questionable benefit [ this is more less ensured by Java type safety) ]
     */
    @Test
    @Useless
    public void testLcnAPiWrapping() {
        com.nokia.cbam.lcm.v32.ApiClient c = Mockito.mock(com.nokia.cbam.lcm.v32.ApiClient.class);
        class TestClasss extends CbamRestApiProvider {
            public TestClasss() {
                super(driverProperties, cbamTokenProvider, vnfmInfoProvider, cbamSecurityProvider);
            }

            @Override
            ApiClient buildLcmApiClient(String vnfmId) {
                return c;
            }
        }
        VnfsApi defaultApi = Mockito.mock(VnfsApi.class);
        //when
        when(c.createService(VnfsApi.class)).thenReturn(defaultApi);
        //verify
        TestClasss testInstnace = new TestClasss();
        assertNotNull(testInstnace.getCbamLcmApi(VNFM_ID));
        assertEquals(defaultApi, testInstnace.getCbamLcmApi(VNFM_ID));
    }

    /**
     * Test API wrapping for LCM
     * (questionable benefit [ this is more less ensured by Java type safety) ]
     */
    @Test
    @Useless
    public void testOperationExecutionsApiAPiWrapping() {
        com.nokia.cbam.lcm.v32.ApiClient c = Mockito.mock(com.nokia.cbam.lcm.v32.ApiClient.class);
        class TestClasss extends CbamRestApiProvider {
            public TestClasss() {
                super(driverProperties, cbamTokenProvider, vnfmInfoProvider, cbamSecurityProvider);
            }

            @Override
            ApiClient buildLcmApiClient(String vnfmId) {
                return c;
            }
        }
        OperationExecutionsApi defaultApi = Mockito.mock(OperationExecutionsApi.class);
        //when
        when(c.createService(OperationExecutionsApi.class)).thenReturn(defaultApi);
        //verify
        TestClasss testInstnace = new TestClasss();
        assertNotNull(testInstnace.getCbamOperationExecutionApi(VNFM_ID));
        assertEquals(defaultApi, testInstnace.getCbamOperationExecutionApi(VNFM_ID));
    }
}
