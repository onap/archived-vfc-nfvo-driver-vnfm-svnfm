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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.aai.api.CloudInfrastructureApi;
import org.onap.aai.api.ExternalSystemApi;
import org.onap.aai.api.NetworkApi;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

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

public class TestAAIRestApiProvider extends TestBase {
    private AAIRestApiProvider aaiRestApiProvider;
    @Mock
    private HostnameVerifier hostnameVerifier;
    private AaiSecurityProvider aaiSecurityProvider = spy(new AaiSecurityProvider());

    @Before
    public void init() {
        aaiRestApiProvider = new AAIRestApiProvider(msbApiProvider, aaiSecurityProvider);
    }

    /**
     * test building a client to access AAI API
     */
    @Test
    public void testApiClientBuilder() throws Exception {
        setField(aaiSecurityProvider, "skipCertificateVerification", true);
        setField(aaiSecurityProvider, "skipHostnameVerification", true);
        setFieldWithPropertyAnnotation(aaiRestApiProvider, "${aaiUsername}", "username");
        setFieldWithPropertyAnnotation(aaiRestApiProvider, "${aaiPassword}", "aaiPassword");
        ResultCaptor<SSLSocketFactory> sslSocketFactoryResultCaptor = new ResultCaptor<>();
        doAnswer(sslSocketFactoryResultCaptor).when(aaiSecurityProvider).buildSSLSocketFactory();
        when(msbApiProvider.getMicroServiceUrl(AAIRestApiProvider.AAIService.NETWORK.getServiceName(), "v11")).thenReturn("http://1.2.3.4/a/");
        when(aaiSecurityProvider.buildHostnameVerifier()).thenReturn(hostnameVerifier);
        //when
        org.onap.aai.ApiClient apiClient = aaiRestApiProvider.buildApiClient(AAIRestApiProvider.AAIService.NETWORK);
        //verify
        assertEquals("http://1.2.3.4/a/", apiClient.getAdapterBuilder().build().baseUrl().toString());
        assertEquals(sslSocketFactoryResultCaptor.getResult(), apiClient.getOkBuilder().build().sslSocketFactory());
        assertEquals(hostnameVerifier, apiClient.getOkBuilder().build().hostnameVerifier());
        Response resp = new Response.Builder().message("a").code(200).protocol(Protocol.HTTP_1_0).request(new Request.Builder().url("http://1.2.3.4/d").build()).build();
        Request authenticate = apiClient.getOkBuilder().build().authenticator().authenticate(null, resp);
        assertEquals("Basic dXNlcm5hbWU6YWFpUGFzc3dvcmQ=", authenticate.headers().get("Authorization"));
    }

    /**
     * is slash is missing from micro service URL it is added
     */
    @Test
    public void testApiClientBuilderMissingSlash() throws Exception {
        setField(aaiSecurityProvider, "skipCertificateVerification", true);
        setField(aaiSecurityProvider, "skipHostnameVerification", true);
        setFieldWithPropertyAnnotation(aaiRestApiProvider, "${aaiUsername}", "username");
        setFieldWithPropertyAnnotation(aaiRestApiProvider, "${aaiPassword}", "aaiPassword");
        ResultCaptor<SSLSocketFactory> sslSocketFactoryResultCaptor = new ResultCaptor<>();
        doAnswer(sslSocketFactoryResultCaptor).when(aaiSecurityProvider).buildSSLSocketFactory();
        when(msbApiProvider.getMicroServiceUrl(AAIRestApiProvider.AAIService.NETWORK.getServiceName(), "v11")).thenReturn("http://1.2.3.4/a");
        when(aaiSecurityProvider.buildHostnameVerifier()).thenReturn(hostnameVerifier);
        //when
        org.onap.aai.ApiClient apiClient = aaiRestApiProvider.buildApiClient(AAIRestApiProvider.AAIService.NETWORK);
        //verify
        assertEquals("http://1.2.3.4/a/", apiClient.getAdapterBuilder().build().baseUrl().toString());
        assertEquals(sslSocketFactoryResultCaptor.getResult(), apiClient.getOkBuilder().build().sslSocketFactory());
        assertEquals(hostnameVerifier, apiClient.getOkBuilder().build().hostnameVerifier());
        Response resp = new Response.Builder().message("a").code(200).protocol(Protocol.HTTP_1_0).request(new Request.Builder().url("http://1.2.3.4/d").build()).build();
        Request authenticate = apiClient.getOkBuilder().build().authenticator().authenticate(null, resp);
        assertEquals("Basic dXNlcm5hbWU6YWFpUGFzc3dvcmQ=", authenticate.headers().get("Authorization"));
    }

    /**
     * test building a client to access AAI API
     */
    @Test
    public void testApiClientBuilderForCloud() throws Exception {
        setField(aaiSecurityProvider, "skipCertificateVerification", true);
        setField(aaiSecurityProvider, "skipHostnameVerification", true);
        setFieldWithPropertyAnnotation(aaiRestApiProvider, "${aaiUsername}", "username");
        setFieldWithPropertyAnnotation(aaiRestApiProvider, "${aaiPassword}", "aaiPassword");
        ResultCaptor<SSLSocketFactory> sslSocketFactoryResultCaptor = new ResultCaptor<>();
        doAnswer(sslSocketFactoryResultCaptor).when(aaiSecurityProvider).buildSSLSocketFactory();
        when(msbApiProvider.getMicroServiceUrl(AAIRestApiProvider.AAIService.CLOUD.getServiceName(), "v11")).thenReturn("http://1.2.3.4/a/");
        when(aaiSecurityProvider.buildHostnameVerifier()).thenReturn(hostnameVerifier);
        //when
        org.onap.aai.ApiClient apiClient = aaiRestApiProvider.buildApiClient(AAIRestApiProvider.AAIService.CLOUD);
        //verify
        assertEquals("http://1.2.3.4/a/", apiClient.getAdapterBuilder().build().baseUrl().toString());
        assertEquals(sslSocketFactoryResultCaptor.getResult(), apiClient.getOkBuilder().build().sslSocketFactory());
        assertEquals(hostnameVerifier, apiClient.getOkBuilder().build().hostnameVerifier());
        Response resp = new Response.Builder().message("a").code(200).protocol(Protocol.HTTP_1_0).request(new Request.Builder().url("http://1.2.3.4/d").build()).build();
        Request authenticate = apiClient.getOkBuilder().build().authenticator().authenticate(null, resp);
        assertEquals("Basic dXNlcm5hbWU6YWFpUGFzc3dvcmQ=", authenticate.headers().get("Authorization"));
    }

    /**
     * test building a client to access AAI API
     */
    @Test
    public void testApiClientBuilderForExternalSystems() throws Exception {
        setField(aaiSecurityProvider, "skipCertificateVerification", true);
        setField(aaiSecurityProvider, "skipHostnameVerification", true);
        setFieldWithPropertyAnnotation(aaiRestApiProvider, "${aaiUsername}", "username");
        setFieldWithPropertyAnnotation(aaiRestApiProvider, "${aaiPassword}", "aaiPassword");
        ResultCaptor<SSLSocketFactory> sslSocketFactoryResultCaptor = new ResultCaptor<>();
        doAnswer(sslSocketFactoryResultCaptor).when(aaiSecurityProvider).buildSSLSocketFactory();
        when(msbApiProvider.getMicroServiceUrl(AAIRestApiProvider.AAIService.ESR.getServiceName(), "v11")).thenReturn("http://1.2.3.4/a/");
        when(aaiSecurityProvider.buildHostnameVerifier()).thenReturn(hostnameVerifier);
        //when
        org.onap.aai.ApiClient apiClient = aaiRestApiProvider.buildApiClient(AAIRestApiProvider.AAIService.ESR);
        //verify
        assertEquals("http://1.2.3.4/a/", apiClient.getAdapterBuilder().build().baseUrl().toString());
        assertEquals(sslSocketFactoryResultCaptor.getResult(), apiClient.getOkBuilder().build().sslSocketFactory());
        assertEquals(hostnameVerifier, apiClient.getOkBuilder().build().hostnameVerifier());
        Response resp = new Response.Builder().message("a").code(200).protocol(Protocol.HTTP_1_0).request(new Request.Builder().url("http://1.2.3.4/d").build()).build();
        Request authenticate = apiClient.getOkBuilder().build().authenticator().authenticate(null, resp);
        assertEquals("Basic dXNlcm5hbWU6YWFpUGFzc3dvcmQ=", authenticate.headers().get("Authorization"));
    }

    /**
     * Test API wrapping for NetworkApi
     * (questionable benefit [ this is more less ensured by Java type safety) ]
     */
    @Test
    public void testNetworkApiAPiWrapping() {
        org.onap.aai.ApiClient c = Mockito.mock(org.onap.aai.ApiClient.class);
        class TestClasss extends AAIRestApiProvider {
            public TestClasss() {
                super(msbApiProvider, aaiSecurityProvider);
            }

            @Override
            org.onap.aai.ApiClient buildApiClient(AAIRestApiProvider.AAIService service) {
                return c;
            }
        }
        NetworkApi defaultApi = Mockito.mock(NetworkApi.class);
        when(c.createService(NetworkApi.class)).thenReturn(defaultApi);
        //verify
        TestClasss testInstnace = new TestClasss();
        assertEquals(defaultApi, testInstnace.getNetworkApi());
    }

    /**
     * Test API wrapping for CloudInfrastructureApi
     * (questionable benefit [ this is more less ensured by Java type safety) ]
     */
    @Test
    public void testCloudInfrastructureApiWrapping() {
        org.onap.aai.ApiClient c = Mockito.mock(org.onap.aai.ApiClient.class);
        class TestClasss extends AAIRestApiProvider {
            public TestClasss() {
                super(msbApiProvider, aaiSecurityProvider);
            }

            @Override
            org.onap.aai.ApiClient buildApiClient(AAIRestApiProvider.AAIService service) {
                return c;
            }
        }
        CloudInfrastructureApi defaultApi = Mockito.mock(CloudInfrastructureApi.class);
        when(c.createService(CloudInfrastructureApi.class)).thenReturn(defaultApi);
        //verify
        TestClasss testInstnace = new TestClasss();
        assertEquals(defaultApi, testInstnace.getCloudInfrastructureApi());
    }

    /**
     * Test API wrapping for ExternalSystemApi
     * (questionable benefit [ this is more less ensured by Java type safety) ]
     */
    @Test
    public void testExternalSystemApiWrapping() {
        org.onap.aai.ApiClient c = Mockito.mock(org.onap.aai.ApiClient.class);
        class TestClasss extends AAIRestApiProvider {
            public TestClasss() {
                super(msbApiProvider, aaiSecurityProvider);
            }

            @Override
            org.onap.aai.ApiClient buildApiClient(AAIRestApiProvider.AAIService service) {
                return c;
            }
        }
        ExternalSystemApi defaultApi = Mockito.mock(ExternalSystemApi.class);
        when(c.createService(ExternalSystemApi.class)).thenReturn(defaultApi);
        //verify
        TestClasss testInstnace = new TestClasss();
        assertEquals(defaultApi, testInstnace.getExternalSystemApi());
    }
}
